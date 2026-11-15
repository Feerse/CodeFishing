#!/usr/bin/env python3
"""Mengecilkan beban aset di HP tanpa mengubah tampilan.

Masalah: PNG kecil di disk tetapi mekar saat didekode. Ikan 640 px yang ditampilkan 44-150 dp tetap
didekode penuh (hingga 1,5 MB per ikan; 58 ikan di Koleksi = puluhan MB di thread utama), dan peta
dirender dari SVG lewat CPU tiap kali layar peta dibuka (1000x2800, beberapa detik di HP menengah).

Yang dilakukan skrip ini (idempotent, aman dijalankan ulang):
  1. Mengecilkan aset kecil-tampil (ikan, alat, ikon, node, kapal peta, dst.) ke sisi terpanjang 448 px.
     Itu cukup untuk 150 dp pada layar 2,75x. Latar penuh (adegan_*, latar_*, preview_*, logo) tidak diubah.
     Berkas asli resolusi penuh disimpan di tools/aset_resolusi_penuh/ sebagai sumber untuk menjalankan ulang.
  2. Membuat thumbnail ikan `<nama>_kecil.png` (sisi terpanjang 256 px) untuk grid dan daftar.
  3. Merender peta SVG sekali ke WebP (peta_*.webp, 1000x2800) supaya aplikasi cukup mendekode gambar
     (puluhan milidetik) dan tidak perlu merender SVG saat dijalankan.

Jalankan: python3 2_Aset_dan_Konten/tools/optimalkan_aset.py && python3 2_Aset_dan_Konten/tools/sync_assets.py
Butuh: pillow, cairosvg.
"""
from __future__ import annotations

import io
import os
import shutil

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "..", "assets-source")
ASLI = os.path.join(HERE, "aset_resolusi_penuh")

SISI_MAKS = 448
SISI_KECIL = 256

# Awalan nama aset yang tampil kecil (<= 160 dp) dan aman dikecilkan.
AWALAN_KECIL = (
    "ikan_", "alat_", "kapal_peta", "pelampung", "kail", "ikon_", "lencana_", "node_",
    "gunung_es", "pulau_", "mercusuar", "kompas", "medali", "trofi", "bendera_",
)


def _ukuran_maks(im: Image.Image, sisi: int) -> Image.Image:
    w, h = im.size
    if max(w, h) <= sisi:
        return im
    k = sisi / max(w, h)
    return im.resize((max(1, round(w * k)), max(1, round(h * k))), Image.LANCZOS)


def _simpan_png(im: Image.Image, path: str) -> None:
    im.save(path, optimize=True)


def kecilkan_dan_thumbnail() -> tuple[int, int]:
    n_kecil = n_thumb = 0
    for folder in sorted(os.listdir(SRC)):
        d = os.path.join(SRC, folder)
        if not os.path.isdir(d) or not folder[:2].isdigit():
            continue
        for f in sorted(os.listdir(d)):
            if not f.endswith(".png") or f.endswith("_kecil.png"):
                continue
            nama = f[:-4]
            if not nama.startswith(AWALAN_KECIL):
                continue
            path = os.path.join(d, f)
            asli = os.path.join(ASLI, folder, f)
            # Sumber selalu berkas resolusi penuh (jika sudah pernah dikecilkan sebelumnya).
            sumber = asli if os.path.exists(asli) else path
            im = Image.open(sumber).convert("RGBA")
            if max(im.size) > SISI_MAKS and not os.path.exists(asli):
                os.makedirs(os.path.dirname(asli), exist_ok=True)
                shutil.copyfile(path, asli)
            besar = _ukuran_maks(im, SISI_MAKS)
            if besar.size != Image.open(path).size:
                _simpan_png(besar, path)
                n_kecil += 1
            if nama.startswith("ikan_"):
                _simpan_png(_ukuran_maks(im, SISI_KECIL), os.path.join(d, nama + "_kecil.png"))
                n_thumb += 1
    return n_kecil, n_thumb


def _render_svg_png(svg_path: str, lebar: int, tinggi: int) -> Image.Image:
    """Render SVG ke PIL. Utamakan cairosvg; bila pustaka cairo tidak ada (umum di Windows),
    pakai Chrome/Edge headless dengan hasil yang sama (selisih < 2/255 dibanding cairosvg)."""
    try:
        import cairosvg  # type: ignore

        png = cairosvg.svg2png(url=svg_path, output_width=lebar, output_height=tinggi)
        return Image.open(io.BytesIO(png)).convert("RGBA")
    except Exception:
        pass

    import subprocess
    import tempfile
    from urllib.request import pathname2url

    kandidat = [
        os.environ.get("CHROME_PATH", ""),
        r"C:\Program Files\Google\Chrome\Application\chrome.exe",
        r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
        r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
        r"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
        shutil.which("google-chrome") or "",
        shutil.which("chromium") or "",
        shutil.which("msedge") or "",
    ]
    exe = next((p for p in kandidat if p and os.path.exists(p)), None)
    if not exe:
        raise RuntimeError("Tidak ada cairosvg maupun Chrome/Edge untuk merender SVG")
    with tempfile.TemporaryDirectory() as tmp:
        keluar = os.path.join(tmp, "render.png")
        url = "file:///" + pathname2url(os.path.abspath(svg_path)).lstrip("/")
        subprocess.run(
            [exe, "--headless=new", "--disable-gpu", "--hide-scrollbars",
             "--force-device-scale-factor=1", f"--window-size={lebar},{tinggi}",
             f"--screenshot={keluar}", url],
            check=True, capture_output=True, timeout=180,
        )
        return Image.open(keluar).convert("RGBA")


def render_peta() -> list[str]:
    d = os.path.join(SRC, "01_Peta_Perairan")
    dibuat = []
    for f in sorted(os.listdir(d)):
        if not (f.startswith("peta_") and f.endswith(".svg")):
            continue
        im = _render_svg_png(os.path.join(d, f), 1000, 2800)
        latar = Image.new("RGBA", im.size, (255, 255, 255, 255))
        latar.alpha_composite(im)
        tujuan = os.path.join(d, f[:-4] + ".webp")
        latar.convert("RGB").save(tujuan, "WEBP", quality=90, method=6)
        dibuat.append(f"{os.path.basename(tujuan)} {os.path.getsize(tujuan) // 1024} KB")
    return dibuat


if __name__ == "__main__":
    k, t = kecilkan_dan_thumbnail()
    print(f"dikecilkan: {k} berkas, thumbnail dibuat/diperbarui: {t}")
    for baris in render_peta():
        print("peta:", baris)

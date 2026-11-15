#!/usr/bin/env python3
"""Membuat varian aset untuk mode terang.

  - Ikon UI bertile navy  -> tile terang + glyph gelap
  - Adegan/latar gelap    -> versi siang yang tetap jenuh (bukan pudar)
  - Ilustrasi editor kode -> palet terang yang tajam
  - Peta SVG gelap        -> peta_*_terang.svg (perairan dan antartika)

Jalankan dari mana saja:
  python 2_Aset_dan_Konten/tools/generate_tema_terang.py

Setelah itu jalankan sync_assets.py agar file masuk ke app/src/main/res.
"""
from __future__ import annotations

import os
import re
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SRC = os.path.join(ROOT, "assets-source")

# ---------------------------------------------------------------- ikon UI
# Warna sumber (tile/garis) -> warna untuk mode terang.
ICON_MAP = {
    (0x13, 0x3C, 0x4F): (0xEA, 0xF4, 0xF7),
    (0x24, 0x5A, 0x70): (0xC2, 0xD8, 0xE1),
    (0x23, 0x59, 0x6F): (0xC2, 0xD8, 0xE1),
    (0x8F, 0xB2, 0xB8): (0x54, 0x78, 0x84),
    (0x8D, 0xB7, 0xCA): (0x54, 0x78, 0x84),
    (0xE8, 0xF4, 0xF2): (0x10, 0x30, 0x3C),
    (0xE8, 0xF4, 0xF6): (0x10, 0x30, 0x3C),
    (0xF4, 0xF1, 0xE8): (0x10, 0x30, 0x3C),
    (0xFF, 0xFF, 0xFF): (0x10, 0x30, 0x3C),
    (0x7D, 0x98, 0xA0): (0x6A, 0x88, 0x92),
    (0xB2, 0xC6, 0xC9): (0x9A, 0xB0, 0xB7),
    (0x5F, 0x87, 0x96): (0x9F, 0xBE, 0xC9),
    (0x3C, 0x5C, 0x69): (0x7D, 0xA0, 0xAC),
    (0xA3, 0xC0, 0xC4): (0x5C, 0x7B, 0x85),
    (0x53, 0x72, 0x80): (0x4A, 0x6B, 0x77),
    (0x0C, 0x3A, 0x4F): (0xDC, 0xEE, 0xF5),
    (0x2B, 0x61, 0x77): (0xC6, 0xE0, 0xEA),
    (0x22, 0x56, 0x6C): (0xC6, 0xE0, 0xEA),
    (0x09, 0x2D, 0x3F): (0xA9, 0xCE, 0xDA),
    (0x0C, 0x2B, 0x3A): (0x0C, 0x2B, 0x3A),
}

# Ikon "kosong/petak" yang di mode gelap bergantung pada kontur lebih gelap dari isi.
# Peta umum di atas memutihkan kontur itu sehingga hilang (bintang kosong berhalo putih, dst).
# Peta khusus ini menjaga kontur tetap gelap dan isi berwarna sedang.
KONTRAS_ICONS = {"bintang_kosong", "umpan_kosong", "petak_laut", "petak_sampah", "petak_kapal_sekarang"}
ICON_MAP_KONTRAS = {
    (0x13, 0x3C, 0x4F): (0x3A, 0x66, 0x78),   # kontur bintang
    (0x24, 0x5A, 0x70): (0xB9, 0xD0, 0xDA),   # isi bintang (default)
    (0x23, 0x59, 0x6F): (0xB9, 0xD0, 0xDA),
    (0x30, 0x57, 0x6A): (0x2F, 0x5A, 0x6B),
    (0x2B, 0x61, 0x77): (0x5A, 0x8C, 0xA0),
    (0x22, 0x56, 0x6C): (0x5A, 0x8C, 0xA0),
    (0x0C, 0x3A, 0x4F): (0xDC, 0xEE, 0xF5),   # latar petak
    (0x09, 0x2D, 0x3F): (0xA9, 0xCE, 0xDA),
}
# Aksen per-aset yang menimpa isi default (pola/garis harus jelas di atas latar terang).
KONTRAS_TIMPA = {
    "umpan_kosong": {(0x24, 0x5A, 0x70): (0x4F, 0x7F, 0x90)},
    "petak_laut": {(0x24, 0x5A, 0x70): (0x5A, 0x8C, 0xA0)},
    "petak_sampah": {(0x24, 0x5A, 0x70): (0x5A, 0x8C, 0xA0)},
    "petak_kapal_sekarang": {(0x24, 0x5A, 0x70): (0x5A, 0x8C, 0xA0)},
}

UI_ICONS = [
    "ikon_hamburger",
    "ikon_home",
    "ikon_koleksi",
    "ikon_mode_belajar",
    "ikon_mode_expert",
    "ikon_akuarium",
    "ikon_pengaturan",
    "ikon_suara",
    "ikon_suara_mati",
    "ikon_musik",
]

EXTRA_ICONS = [
    ("01_Peta_Perairan", "kompas"),
    ("04_Umpan_Progres", "umpan_penuh"),
    ("04_Umpan_Progres", "umpan_kosong"),
    ("05_Ikan_Koleksi", "ikon_gembok"),
    ("08_Misi_Bintang", "bintang_kosong"),
    ("07_Sandbox", "petak_laut"),
    ("07_Sandbox", "petak_sampah"),
    ("07_Sandbox", "petak_kapal_sekarang"),
]

# ------------------------------------------------------------- adegan/latar
# (folder, nama, gama, saturasi)
#   gama     : pangkat pada kanal V (kecerahan). 1 = tetap gelap, makin kecil makin
#              terang. Rumus: V' = V**gama. Diterapkan pada V saja (bukan per kanal
#              RGB) agar hue dan jenuh warna tidak menyusut. Dipilih 0,52 karena
#              bayangan ikut terangkat (air jadi biru siang) tetapi gradasi atas-bawah
#              masih terlihat; kurva angkat yang lebih agresif membuat air tampak rata
#              dan terlalu menyala di layar penuh.
#   saturasi : pengali jenuh warna (1 = tetap). Dulu aset ini dibuat dengan gamma
#              per-kanal + campuran 30% ke putih, sehingga air, pasir, dan panel
#              kode kehilangan warna dan terlihat pudar dibanding versi gelap.
SCENES = [
    ("03_Studi_Kasus", "latar_bawah_laut", 0.52, 1.06),
    ("09_Akuarium", "latar_akuarium", 0.52, 1.06),
]

# Ilustrasi editor kode bukan gradasi, melainkan gambar datar beberapa warna.
# Mencerahkannya dengan kurva justru membuat panel abu-abu dan teks kehilangan
# kontras, jadi tiap warna dipetakan langsung ke padanan terangnya. Sisa piksel
# (tepi anti-alias) dicampur dari dua warna terdekat agar tidak bergaris.
EDITOR_SRC = ("07_Sandbox", "ilustrasi_editor_kode")
PETA_EDITOR = {
    (0x06, 0x14, 0x1C): (0xFF, 0xFF, 0xFF),   # panel
    (0x0A, 0x1F, 0x2B): (0xE7, 0xF1, 0xF5),   # kolom nomor baris
    (0x39, 0x26, 0x1F): (0xFA, 0xDD, 0xC9),   # baris tersorot
    (0x4B, 0x63, 0x6A): (0xC3, 0xD8, 0xE1),   # baris kode
    (0x24, 0x5A, 0x70): (0x8F, 0xB6, 0xC7),   # garis tepi
    (0x5F, 0xD3, 0xA0): (0x0B, 0x7A, 0x52),   # teks "for i in"
    (0x3D, 0x56, 0x66): (0x6F, 0x95, 0xA3),   # nomor baris
}

# ------------------------------------------------------------------- peta
PETA_MAP = {
    # Air: biru siang yang jenuh. Dulu hampir putih sehingga gunung es, pulau, dan siluet ikan
    # menyatu dengan latar (pudar). Sekarang kontrasnya setara versi gelap.
    "#123F55": "#C2E5F2",
    "#0D3448": "#AAD6E9",
    "#0B2F42": "#93C9DE",
    "#0A2433": "#7EBAD4",
    "#07171F": "#6AA9C7",
    "#030E14": "#1B4B60",   # siluet ikan / bayangan gelap
    "#000000": "#2F7EA0",   # vignette: tepi sedikit lebih dalam, bukan kabut putih
    "#081B26": "#F6FBFD",
    "#7FC9E0": "#2A86A5",   # garis ombak + cahaya lembut
    "#8FD4E8": "#2B86A3",
    "#BFE6F2": "#FFFFFF",   # pita cahaya lebar
    "#8FB2B8": "#3E7482",
    "#8DB7CA": "#3F7A92",
    "#DCEEF5": "#FFFFFF",   # sisi terang gunung es
    "#B5D4E2": "#9CC6D8",
    "#6E9BAF": "#4F8AA2",   # sisi gelap gunung es
    "#E8F4F6": "#FFFFFF",
    "#245A70": "#1E5568",
    "#F26A2E": "#D9531E",
    "#FFD166": "#D99A00",
    "#EAF7FB": "#FFFFFF",
    "#A9CDDB": "#8EC0D3",
}

THRESHOLD = 46


def jarak(a: tuple[int, int, int], b: tuple[int, int, int]) -> int:
    return sum((x - y) ** 2 for x, y in zip(a, b))


def recolor_icon(path: str, tujuan: str, peta: dict | None = None) -> None:
    peta = peta or ICON_MAP
    img = Image.open(path).convert("RGBA")
    px = img.load()
    kunci = list(peta.keys())
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            terdekat = min(kunci, key=lambda k: jarak((r, g, b), k))
            if jarak((r, g, b), terdekat) <= THRESHOLD:
                nr, ng, nb = peta[terdekat]
                px[x, y] = (nr, ng, nb, a)
    img.save(tujuan)
    print(f"ikon  {os.path.relpath(tujuan, ROOT)}")


def siang_scene(path: str, tujuan: str, gama: float, saturasi: float) -> None:
    """Terangkan adegan ke arah "siang" sambil mempertahankan warna dan gradasi.

    Kurva diterapkan pada kanal V (kecerahan) saja, bukan per kanal RGB: cara lama
    mengangkat tiap kanal sehingga selisih antar-kanal menyusut dan warna ikut
    memudar. Jenuh dinaikkan sedikit agar air tetap biru dan pasir tetap hangat.
    """
    img = Image.open(path).convert("RGBA")
    alpha = img.getchannel("A")
    h, s, v = img.convert("RGB").convert("HSV").split()
    lut_v = [int(round(255 * ((i / 255) ** gama))) for i in range(256)]
    lut_s = [min(255, int(round(i * saturasi))) for i in range(256)]
    rgb = Image.merge("HSV", (h, s.point(lut_s), v.point(lut_v))).convert("RGB")
    hasil = rgb.convert("RGBA")
    hasil.putalpha(alpha)
    hasil.save(tujuan)
    print(f"adegan {os.path.relpath(tujuan, ROOT)}")


def terangkan_palet(path: str, tujuan: str, peta: dict) -> None:
    """Ganti warna gambar datar sesuai [peta]; sisa warna dicampur dari dua terdekat."""
    img = Image.open(path).convert("RGBA")
    px = img.load()
    kunci = list(peta.keys())

    def jarak2(a, b):
        return sum((x - y) ** 2 for x, y in zip(a, b))

    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            dekat = sorted(((jarak2((r, g, b), k), k) for k in kunci), key=lambda t: t[0])[:2]
            (d1, k1), (d2, k2) = dekat
            if d2 == 0:
                w1 = 1.0
            else:
                w1 = (1 / (d1 + 1e-6)) / ((1 / (d1 + 1e-6)) + (1 / (d2 + 1e-6)))
            c1, c2 = peta[k1], peta[k2]
            px[x, y] = (
                int(round(c1[0] * w1 + c2[0] * (1 - w1))),
                int(round(c1[1] * w1 + c2[1] * (1 - w1))),
                int(round(c1[2] * w1 + c2[2] * (1 - w1))),
                a,
            )
    img.save(tujuan)
    print(f"ilustrasi {os.path.relpath(tujuan, ROOT)}")


def buat_peta() -> None:
    for nama in ("peta_perairan", "peta_antartika"):
        asal = os.path.join(SRC, "01_Peta_Perairan", nama + ".svg")
        if not os.path.exists(asal):
            print(f"peta  (lewati, tidak ada {nama}.svg)")
            continue
        tujuan = os.path.join(SRC, "01_Peta_Perairan", nama + "_terang.svg")
        svg = open(asal, encoding="utf-8").read()
        hasil = re.sub(
            r"#[0-9A-Fa-f]{6}",
            lambda m: PETA_MAP.get(m.group(0).upper(), m.group(0)),
            svg,
        )
        open(tujuan, "w", encoding="utf-8").write(hasil)
        print(f"peta  {os.path.relpath(tujuan, ROOT)}")


def main() -> None:
    for nama in UI_ICONS:
        asal = os.path.join(SRC, "10_Pendukung_UI", nama + ".png")
        recolor_icon(asal, os.path.join(SRC, "10_Pendukung_UI", nama + "_terang.png"))
    for folder, nama in EXTRA_ICONS:
        asal = os.path.join(SRC, folder, nama + ".png")
        peta = None
        if nama in KONTRAS_ICONS:
            peta = {**ICON_MAP_KONTRAS, **KONTRAS_TIMPA.get(nama, {})}
        tujuan = os.path.join(SRC, folder, nama + "_terang.png")
        recolor_icon(asal, tujuan, peta)
        if nama == "umpan_kosong":
            # Latar lingkaran (alpha 128) diredam; cacing (alpha 191) dan cincin (255) tetap tegas.
            im = Image.open(tujuan).convert("RGBA")
            a = im.getchannel("A").point(lambda v: int(v * 0.18) if 120 <= v <= 135 else v)
            im.putalpha(a)
            im.save(tujuan)
    for folder, nama, gama, saturasi in SCENES:
        asal = os.path.join(SRC, folder, nama + ".png")
        siang_scene(asal, os.path.join(SRC, folder, nama + "_terang.png"), gama, saturasi)
    folder, nama = EDITOR_SRC
    terangkan_palet(
        os.path.join(SRC, folder, nama + ".png"),
        os.path.join(SRC, folder, nama + "_terang.png"),
        PETA_EDITOR,
    )
    buat_peta()


if __name__ == "__main__":
    main()
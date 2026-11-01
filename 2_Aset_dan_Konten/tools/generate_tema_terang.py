#!/usr/bin/env python3
"""Membuat varian aset untuk mode terang.

  - Ikon UI bertile navy  -> tile terang + glyph gelap
  - Adegan/latar gelap    -> versi pastel yang lebih terang
  - Peta SVG gelap        -> peta_perairan_terang.svg

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
SCENES = [
    ("03_Studi_Kasus", "latar_bawah_laut", 0.36, 0.30),
    ("09_Akuarium", "latar_akuarium", 0.38, 0.28),
    ("07_Sandbox", "ilustrasi_editor_kode", 0.36, 0.30),
]

# ------------------------------------------------------------------- peta
PETA_MAP = {
    "#123F55": "#EAF7FB",
    "#0D3448": "#E2F1F8",
    "#0B2F42": "#D9ECF5",
    "#0A2433": "#CFE7F2",
    "#07171F": "#C4E0ED",
    "#030E14": "#6F96A5",
    "#081B26": "#F6FBFD",
    "#000000": "#FFFFFF",
    "#7FC9E0": "#2E7F9A",
    "#8FD4E8": "#3A8AA0",
    "#BFE6F2": "#4C93A8",
    "#8FB2B8": "#5E8794",
    "#DCEEF5": "#FFFFFF",
    "#B5D4E2": "#9CC4D3",
    "#6E9BAF": "#7BA5B5",
    "#E8F4F6": "#FFFFFF",
    "#245A70": "#2C5566",
    "#F26A2E": "#D9531E",
    "#FFD166": "#D99A00",
    "#EAF7FB": "#FFFFFF",
    "#A9CDDB": "#B9DCE8",
}

THRESHOLD = 46


def jarak(a: tuple[int, int, int], b: tuple[int, int, int]) -> int:
    return sum((x - y) ** 2 for x, y in zip(a, b))


def recolor_icon(path: str, tujuan: str) -> None:
    img = Image.open(path).convert("RGBA")
    px = img.load()
    kunci = list(ICON_MAP.keys())
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            terdekat = min(kunci, key=lambda k: jarak((r, g, b), k))
            if jarak((r, g, b), terdekat) <= THRESHOLD:
                nr, ng, nb = ICON_MAP[terdekat]
                px[x, y] = (nr, ng, nb, a)
    img.save(tujuan)
    print(f"ikon  {os.path.relpath(tujuan, ROOT)}")


def brighten_scene(path: str, tujuan: str, gamma: float, blend: float) -> None:
    img = Image.open(path).convert("RGBA")
    alpha = img.getchannel("A")
    rgb = img.convert("RGB")
    lut = [int(255 * ((i / 255) ** gamma)) for i in range(256)]
    rgb = rgb.point(lut * 3)
    tint = Image.new("RGB", rgb.size, (244, 248, 249))
    rgb = Image.blend(rgb, tint, blend)
    abu = rgb.convert("L")
    rgb = Image.blend(rgb, Image.merge("RGB", (abu, abu, abu)), 0.08)
    hasil = rgb.convert("RGBA")
    hasil.putalpha(alpha)
    hasil.save(tujuan)
    print(f"adegan {os.path.relpath(tujuan, ROOT)}")


def buat_peta() -> None:
    asal = os.path.join(SRC, "01_Peta_Perairan", "peta_perairan.svg")
    tujuan = os.path.join(SRC, "01_Peta_Perairan", "peta_perairan_terang.svg")
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
        recolor_icon(asal, os.path.join(SRC, folder, nama + "_terang.png"))
    for folder, nama, gamma, blend in SCENES:
        asal = os.path.join(SRC, folder, nama + ".png")
        brighten_scene(asal, os.path.join(SRC, folder, nama + "_terang.png"), gamma, blend)
    buat_peta()


if __name__ == "__main__":
    main()
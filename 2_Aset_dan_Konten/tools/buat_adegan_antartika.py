#!/usr/bin/env python3
"""Membuat varian adegan untuk peta Lautan Antartika (stage 11-20).

Studi kasus TIDAK diganti: aktor, kapal, dermaga, jala, bubu, tombak, dan pukat tetap sama dan
animasi memakai titik jangkar yang sama (lihat MemancingAdegan.kt). Skrip ini hanya MENAMBAH detail
khas Antartika di atas ilustrasi yang sudah bersih dari alat pancing:

  - bongkahan es mengapung dan lempeng es di air (tidak menutup zona lemparan / jalur ikan)
  - gunung es di cakrawala
  - salju di dermaga, atap kapal, dan ember; es menggantung di dermaga
  - aurora tipis di langit malam
  - bawah laut: langit-langit es dengan es menggantung dan bongkah es di dasar

Masukan : assets-source/03_Studi_Kasus/adegan_*.png  (hasil bersihkan_adegan.py)
Keluaran: assets-source/03_Studi_Kasus/adegan_*_antartika.png
Jalankan: python3 2_Aset_dan_Konten/tools/buat_adegan_antartika.py
          python3 2_Aset_dan_Konten/tools/sync_assets.py
Butuh: pillow, numpy.
"""
from __future__ import annotations

import math
import os
import random

from PIL import Image, ImageChops, ImageDraw, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
DIR = os.path.join(HERE, "..", "assets-source", "03_Studi_Kasus")

W, H = 1200, 907
SS = 3  # supersampling untuk tepi halus

# Palet es (selaras dengan gunung_es.png dan adegan_lubang_es.png)
ES_TERANG = (0xDC, 0xEE, 0xF5)
ES_SISI = (0xB5, 0xD4, 0xE2)
ES_BAYANG = (0x8F, 0xB2, 0xC4)
ES_DALAM = (0x6E, 0x9B, 0xAF)
ES_PUTIH = (0xF4, 0xFA, 0xFC)
AIR_DALAM = (0x2E, 0x7D, 0x96)


def kanvas() -> tuple[Image.Image, ImageDraw.ImageDraw]:
    im = Image.new("RGBA", (W * SS, H * SS), (0, 0, 0, 0))
    return im, ImageDraw.Draw(im, "RGBA")


def p(x: float, y: float) -> tuple[float, float]:
    return (x * SS, y * SS)


def poly(d, pts, fill):
    d.polygon([p(x, y) for x, y in pts], fill=fill)


def bongkah(d, cx, garis_air, lebar, tinggi, rnd: random.Random, alpha=255):
    """Bongkahan es bersisi-sisi, sebagian terendam di bawah garis air."""
    w, h = lebar, tinggi
    # bayangan terendam (tembus pandang)
    dalam = h * 0.55
    poly(d, [(cx - w * 0.5, garis_air), (cx - w * 0.36, garis_air + dalam * 0.8),
             (cx, garis_air + dalam), (cx + w * 0.38, garis_air + dalam * 0.7),
             (cx + w * 0.5, garis_air)], AIR_DALAM + (int(95 * alpha / 255),))
    # badan atas: puncak acak
    j = [rnd.uniform(-0.08, 0.08) for _ in range(6)]
    kiri = (cx - w * 0.5, garis_air)
    kanan = (cx + w * 0.5, garis_air)
    puncak = (cx + w * (0.04 + j[0]), garis_air - h)
    lekuk = (cx + w * (0.22 + j[1]), garis_air - h * (0.62 + j[2]))
    bahu_k = (cx - w * (0.30 + j[3]), garis_air - h * (0.55 + j[4]))
    bahu_r = (cx + w * (0.40 + j[5]), garis_air - h * 0.36)
    tengah = (cx + w * 0.02, garis_air)
    # sisi kiri (lebih gelap), sisi kanan (terang)
    poly(d, [kiri, bahu_k, puncak, tengah], ES_SISI + (alpha,))
    poly(d, [tengah, puncak, lekuk, bahu_r, kanan], ES_TERANG + (alpha,))
    # sorot putih di puncak
    poly(d, [puncak, (puncak[0] - w * 0.07, puncak[1] + h * 0.22),
             (puncak[0] + w * 0.06, puncak[1] + h * 0.2)], ES_PUTIH + (alpha,))
    # bayangan di sisi kiri
    poly(d, [kiri, (cx - w * 0.30, garis_air - h * 0.2), tengah], ES_BAYANG + (int(alpha * 0.8),))
    # garis air
    d.line([p(cx - w * 0.62, garis_air + 1), p(cx + w * 0.62, garis_air + 1)],
           fill=(0xDC, 0xEE, 0xF5, int(110 * alpha / 255)), width=int(2.2 * SS))


def lempeng(d, cx, garis_air, lebar, rnd: random.Random):
    """Lempeng es pipih yang hampir rata dengan air."""
    w = lebar
    t = max(6, w * 0.07)
    poly(d, [(cx - w * 0.5, garis_air), (cx - w * 0.42, garis_air - t * 1.3),
             (cx - w * 0.1, garis_air - t * 1.8), (cx + w * 0.32, garis_air - t * 1.4),
             (cx + w * 0.5, garis_air)], ES_TERANG + (255,))
    poly(d, [(cx - w * 0.5, garis_air), (cx + w * 0.5, garis_air),
             (cx + w * 0.44, garis_air + t), (cx - w * 0.4, garis_air + t * 1.1)], ES_BAYANG + (235,))
    d.line([p(cx - w * 0.6, garis_air + t * 1.25), p(cx + w * 0.6, garis_air + t * 1.25)],
           fill=(0xDC, 0xEE, 0xF5, 80), width=int(2 * SS))


def gunung_cakrawala(d, cx, tinggi, lebar, rnd: random.Random, y=445, warna=(0x5C, 0x8A, 0xA0), alpha=235):
    """Gunung es jauh di cakrawala (siluet kebiruan + tudung salju)."""
    w, h = lebar, tinggi
    a = (cx - w * 0.5, y)
    b = (cx - w * 0.18, y - h * 0.62)
    c = (cx + w * 0.02, y - h)
    e = (cx + w * 0.22, y - h * 0.55)
    f = (cx + w * 0.5, y)
    poly(d, [a, b, c, e, f], warna + (alpha,))
    poly(d, [(cx + w * 0.02, y - h), (cx + w * 0.22, y - h * 0.55), (cx + w * 0.5, y),
             (cx + w * 0.1, y)], (0x7F, 0xAB, 0xBE, alpha))
    poly(d, [c, (c[0] - w * 0.09, c[1] + h * 0.3), (c[0] - w * 0.02, c[1] + h * 0.24),
             (c[0] + w * 0.04, c[1] + h * 0.34), (c[0] + w * 0.1, c[1] + h * 0.26)],
         ES_TERANG + (alpha,))


def salju_garis(d, x0, x1, y, tebal, alpha=245, tonjol=True):
    """Lapisan salju di atas permukaan datar, dengan tepi bergelombang."""
    pts = [(x0, y)]
    n = max(4, int((x1 - x0) / 26))
    for i in range(n + 1):
        x = x0 + (x1 - x0) * i / n
        pts.append((x, y - tebal * (0.7 + 0.5 * math.sin(i * 1.9))))
    pts.append((x1, y))
    pts.append((x1, y + tebal * 0.45))
    pts.append((x0, y + tebal * 0.45))
    poly(d, pts, ES_PUTIH + (alpha,))


def es_menggantung(d, x0, x1, y, rnd: random.Random, panjang=34, jumlah=None, alpha=235):
    n = jumlah or max(3, int((x1 - x0) / 28))
    for i in range(n):
        x = x0 + (x1 - x0) * (i + rnd.uniform(0.15, 0.85)) / n
        w = rnd.uniform(5, 9)
        L = rnd.uniform(0.55, 1.0) * panjang
        poly(d, [(x - w, y), (x + w, y), (x + w * 0.1, y + L)], ES_TERANG + (alpha,))
        poly(d, [(x - w, y), (x, y), (x + w * 0.1, y + L)], ES_SISI + (alpha,))


def aurora(base: Image.Image, seed: int, atas=60, bawah=310, kuat=1.0):
    """Pita aurora kabur di langit (hanya di atas cakrawala)."""
    rnd = random.Random(seed)
    layer = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer, "RGBA")
    warna = [(0x3F, 0xD6, 0x9B), (0x6E, 0xA8, 0xFF), (0xB1, 0x8C, 0xFF)]
    for k, col in enumerate(warna):
        y0 = rnd.uniform(atas, atas + 120) + k * 26
        pts = []
        for i in range(0, W + 40, 40):
            y = y0 + 46 * math.sin(i / 170.0 + k * 1.3) + 24 * math.sin(i / 71.0 + k)
            pts.append((i, y))
        top = pts
        bot = [(x, y + rnd.uniform(70, 120)) for x, y in reversed(pts)]
        d.polygon(top + bot, fill=col + (int(44 * kuat),))
    layer = layer.filter(ImageFilter.GaussianBlur(26))
    # pudar mulus ke arah cakrawala (tanpa tepi keras)
    maska = Image.new("L", (1, H), 0)
    for y in range(H):
        maska.putpixel((0, y), int(255 * max(0.0, min(1.0, (430 - y) / 190.0))))
    maska = maska.resize((W, H))
    a = ImageChops.multiply(layer.getchannel("A"), maska)
    layer.putalpha(a)
    return Image.alpha_composite(base, layer)


def gabung(base: Image.Image, overlay: Image.Image) -> Image.Image:
    ov = overlay.resize((W, H), Image.LANCZOS)
    return Image.alpha_composite(base, ov)


# ---------------------------------------------------------------------------------- adegan
def _chunks(d, rnd, daftar, lempeng_daftar):
    for (cx, y, w, h) in daftar:
        bongkah(d, cx, y, w, h, rnd)
    for (cx, y, w) in lempeng_daftar:
        lempeng(d, cx, y, w, rnd)


def kapal(base, seed=3):
    rnd = random.Random(seed)
    im = aurora(base, seed)
    ov, d = kanvas()
    for cx, t, w in [(150, 80, 250), (330, 52, 150), (1040, 96, 300), (1170, 64, 170)]:
        gunung_cakrawala(d, cx, t, w, rnd)
    _chunks(d, rnd,
            [(70, 500, 70, 40), (230, 492, 88, 46), (1000, 560, 82, 48), (1045, 628, 150, 86),
             (1140, 700, 112, 62), (36, 742, 84, 56)],
            [(1010, 752, 210), (118, 648, 110)])
    salju_garis(d, 782, 888, 536, 9)         # atap kabin
    salju_garis(d, 628, 676, 631, 5, 235)    # pagar dek di haluan
    return gabung(im, ov)


def bubu(base, seed=5):
    rnd = random.Random(seed)
    im = aurora(base, seed)
    ov, d = kanvas()
    for cx, t, w in [(120, 70, 220), (360, 50, 140), (1060, 90, 280), (1175, 60, 150)]:
        gunung_cakrawala(d, cx, t, w, rnd)
    _chunks(d, rnd,
            [(60, 505, 80, 44), (210, 495, 80, 42), (1010, 565, 86, 50), (1050, 632, 150, 84),
             (1140, 706, 108, 60), (36, 748, 80, 52)],
            [(1000, 756, 200), (150, 655, 100)])
    salju_garis(d, 782, 888, 536, 9)
    return gabung(im, ov)


def dermaga(base, seed=7):
    rnd = random.Random(seed)
    im = aurora(base, seed)
    ov, d = kanvas()
    for cx, t, w in [(620, 60, 170), (940, 92, 260), (1120, 70, 180)]:
        gunung_cakrawala(d, cx, t, w, rnd)
    _chunks(d, rnd,
            [(1040, 600, 150, 84), (1130, 690, 120, 66), (236, 722, 100, 58), (980, 520, 70, 40),
             (560, 505, 64, 36)],
            [(1000, 760, 190), (60, 780, 130)])
    salju_garis(d, 0, 523, 516, 8)                  # papan dermaga
    es_menggantung(d, 6, 520, 546, rnd, 34)         # es di tepi bawah papan
    salju_garis(d, 183, 257, 470, 5, 240)           # bibir ember
    salju_garis(d, 90, 112, 392, 4, 240)            # tiang lampu
    return gabung(im, ov)


def cadik(base, seed=9):
    rnd = random.Random(seed)
    im = aurora(base, seed)
    ov, d = kanvas()
    for cx, t, w in [(120, 70, 230), (340, 50, 150), (1040, 90, 280), (1170, 60, 150)]:
        gunung_cakrawala(d, cx, t, w, rnd)
    _chunks(d, rnd,
            [(1000, 612, 150, 84), (1110, 700, 124, 68), (960, 518, 72, 42), (36, 748, 86, 56),
             (560, 500, 66, 38)],
            [(240, 770, 170), (1050, 760, 190)])
    salju_garis(d, 112, 232, 539, 6, 235)           # tepi lambung kiri
    return gabung(im, ov)


def penyelam(base, seed=11):
    rnd = random.Random(seed)
    ov, d = kanvas()
    # langit-langit es di permukaan (di atas jalur ikan y 540-640)
    pts = [(0, 0)]
    for i in range(0, W + 60, 60):
        pts.append((i, 70 + 22 * math.sin(i / 130.0) + rnd.uniform(-10, 14)))
    pts.append((W, 0))
    poly(d, pts, (0xDC, 0xEE, 0xF5, 215))
    pts2 = [(0, 0)] + [(x, y - 26) for x, y in pts[1:-1]] + [(W, 0)]
    poly(d, pts2, (0xF4, 0xFA, 0xFC, 200))
    es_menggantung(d, 10, W - 10, 78, rnd, 70, jumlah=18, alpha=215)
    # bongkah es yang mengambang di air atas
    for cx, y, w, h in [(180, 150, 80, 46), (520, 130, 60, 36), (880, 170, 96, 52), (1080, 120, 70, 40)]:
        bongkah(d, cx, y, w, h, rnd, alpha=170)
    # bongkah es bertumpu di dasar laut
    for cx, y, w, h in [(260, 835, 150, 90), (1010, 830, 170, 100), (640, 850, 110, 66)]:
        bongkah(d, cx, y, w, h, rnd, alpha=240)
    return gabung(base, ov)


def lubang_es(base, seed=13):
    rnd = random.Random(seed)
    ov, d = kanvas()
    # bongkahan es di hamparan es, jauh dari lubang dan jalur ikan (x 200-500, y 450-700)
    for cx, y, w, h in [(820, 560, 120, 80), (980, 640, 160, 100), (1110, 540, 90, 60),
                        (760, 760, 140, 80), (1060, 780, 170, 96), (540, 800, 120, 70),
                        (90, 800, 130, 74)]:
        bongkah(d, cx, y, w, h, rnd, alpha=250)
    # serpihan es kecil di tepi lubang
    for cx, y, w, h in [(120, 690, 30, 18), (352, 672, 26, 16)]:
        bongkah(d, cx, y, w, h, rnd, alpha=250)
    salju_garis(d, 447, 509, 513, 5, 245)           # bibir ember
    return gabung(base, ov)


def badai(base, seed=15):
    rnd = random.Random(seed)
    ov, d = kanvas()
    for cx, t, w in [(900, 110, 330), (1120, 80, 200), (760, 60, 170)]:
        gunung_cakrawala(d, cx, t, w, rnd, warna=(0x4C, 0x70, 0x86), alpha=230)
    _chunks(d, rnd,
            [(880, 628, 170, 96), (1030, 566, 112, 66), (1120, 706, 130, 72), (780, 742, 124, 66)],
            [(1000, 770, 210)])
    salju_garis(d, 447, 628, 405, 8)                # atap anjungan
    salju_garis(d, 140, 663, 515, 5, 230)           # pagar dek
    return gabung(base, ov)


ADEGAN = {
    "adegan_kapal": kapal,
    "adegan_dermaga": dermaga,
    "adegan_perahu_cadik": cadik,
    "adegan_penyelam": penyelam,
    "adegan_lubang_es": lubang_es,
    "adegan_bubu": bubu,
    "adegan_kapal_pukat_badai": badai,
}


def main():
    for nama, fn in ADEGAN.items():
        src = Image.open(os.path.join(DIR, nama + ".png")).convert("RGBA")
        out = fn(src)
        tujuan = os.path.join(DIR, nama + "_antartika.png")
        out.convert("RGB").save(tujuan, optimize=True)
        print("ok", os.path.basename(tujuan))


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Membuat aset pratinjau Segitiga Bermuda (peta berikutnya, belum dimainkan).

  gerbang_bermuda.png  - ikon node 404x420 di ujung peta Antartika (gaya node_*.png)
  preview_bermuda.png  - ilustrasi 960x540 untuk lembar pratinjau

Jalankan: python3 2_Aset_dan_Konten/tools/buat_bermuda.py
          python3 2_Aset_dan_Konten/tools/sync_assets.py
Butuh: pillow, numpy.
"""
from __future__ import annotations

import math
import os
import random

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
DIR = os.path.join(HERE, "..", "assets-source", "01_Peta_Perairan")

MINT = (0x5C, 0xE8, 0x9A)
MINT_TUA = (0x1F, 0x8F, 0x5A)
TEAL_GELAP = (0x07, 0x2A, 0x16)


# ------------------------------------------------------------------------------ ikon node
def gerbang(ss: int = 4) -> Image.Image:
    w, h = 404, 420
    im = Image.new("RGBA", (w * ss, h * ss), (0, 0, 0, 0))
    d = ImageDraw.Draw(im, "RGBA")
    cx, cy, r = 196 * ss, 196 * ss, 176 * ss
    # bayangan miring seperti node lain
    d.ellipse([cx - r + 16 * ss, cy - r + 22 * ss, cx + r + 16 * ss, cy + r + 22 * ss], fill=(4, 14, 18, 150))
    # cincin luar bercahaya
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=MINT_TUA + (255,))
    d.ellipse([cx - r + 14 * ss, cy - r + 14 * ss, cx + r - 14 * ss, cy + r - 14 * ss], fill=(0x08, 0x33, 0x19, 255))
    # kabut di dalam
    kabut = Image.new("RGBA", im.size, (0, 0, 0, 0))
    kd = ImageDraw.Draw(kabut, "RGBA")
    for i, (ox, oy, rr, a) in enumerate([(-60, 70, 90, 60), (50, 90, 100, 52), (0, 30, 70, 38)]):
        kd.ellipse([cx + (ox - rr) * ss, cy + (oy - rr * 0.5) * ss, cx + (ox + rr) * ss, cy + (oy + rr * 0.5) * ss],
                   fill=(0xBF, 0xF5, 0xE0, a))
    kabut = kabut.filter(ImageFilter.GaussianBlur(10 * ss))
    maska = Image.new("L", im.size, 0)
    ImageDraw.Draw(maska).ellipse([cx - r + 14 * ss, cy - r + 14 * ss, cx + r - 14 * ss, cy + r - 14 * ss], fill=255)
    kabut.putalpha(Image.fromarray((np.array(kabut.getchannel("A")) * (np.array(maska) / 255.0)).astype("uint8")))
    im = Image.alpha_composite(im, kabut)
    d = ImageDraw.Draw(im, "RGBA")
    # segitiga bercahaya
    tri = [(cx, cy - 98 * ss), (cx + 92 * ss, cy + 66 * ss), (cx - 92 * ss, cy + 66 * ss)]
    glow = Image.new("RGBA", im.size, (0, 0, 0, 0))
    ImageDraw.Draw(glow, "RGBA").line(tri + [tri[0]], fill=MINT + (230,), width=22 * ss, joint="curve")
    glow = glow.filter(ImageFilter.GaussianBlur(12 * ss))
    im = Image.alpha_composite(im, glow)
    d = ImageDraw.Draw(im, "RGBA")
    d.line(tri + [tri[0]], fill=(0xEA, 0xFF, 0xF6, 255), width=13 * ss, joint="curve")
    for x, y in tri:
        d.ellipse([x - 11 * ss, y - 11 * ss, x + 11 * ss, y + 11 * ss], fill=MINT + (255,))
    # tanda tanya di tengah: misteri
    d.arc([cx - 26 * ss, cy - 34 * ss, cx + 26 * ss, cy + 14 * ss], 200, 70, fill=(0xEA, 0xFF, 0xF6, 255), width=11 * ss)
    d.line([(cx + 20 * ss, cy + 6 * ss), (cx, cy + 22 * ss), (cx, cy + 32 * ss)], fill=(0xEA, 0xFF, 0xF6, 255), width=11 * ss, joint="curve")
    d.ellipse([cx - 7 * ss, cy + 44 * ss, cx + 7 * ss, cy + 58 * ss], fill=(0xEA, 0xFF, 0xF6, 255))
    # kilap melengkung di atas (seperti node lain)
    gloss = Image.new("RGBA", im.size, (0, 0, 0, 0))
    ImageDraw.Draw(gloss, "RGBA").pieslice([cx - r + 30 * ss, cy - r + 24 * ss, cx + r - 30 * ss, cy + 40 * ss],
                                           180, 360, fill=(255, 255, 255, 38))
    mk = Image.new("L", im.size, 0)
    ImageDraw.Draw(mk).ellipse([cx - r + 14 * ss, cy - r + 14 * ss, cx + r - 14 * ss, cy + r - 14 * ss], fill=255)
    gloss.putalpha(Image.fromarray((np.array(gloss.getchannel("A")) * (np.array(mk) / 255.0)).astype("uint8")))
    im = Image.alpha_composite(im, gloss)
    return im.resize((w, h), Image.LANCZOS)


# ------------------------------------------------------------------------- ilustrasi banner
def gradasi_vertikal(w, h, atas, bawah):
    t = np.linspace(0, 1, h)[:, None, None]
    a = np.array(atas, float)[None, None, :]
    b = np.array(bawah, float)[None, None, :]
    arr = (a * (1 - t) + b * t) * np.ones((1, w, 1))
    return Image.fromarray(arr.astype("uint8"), "RGB").convert("RGBA")


def siluet_alien(w, h, cx, cy) -> Image.Image:
    """Siluet humanoid kurus dengan kepala memanjang, berdiri di permukaan air.

    (cx, cy) adalah titik kaki siluet. Skala mengikuti tinggi peta (540 px -> ~250 px)
    supaya bentuknya tetap terbaca saat pratinjau dikecilkan di layar ponsel.
    """
    tinggi = h * 0.46
    skala = tinggi / 250.0
    layar = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(layar, "RGBA")
    gelap = (0x02, 0x0D, 0x09, 255)
    tepi = (0x2A, 0x7A, 0x58, 190)

    def P(x, y):
        return (cx + x * skala, cy + y * skala)

    # kaki: dua garis kurus, sedikit terbuka
    d.line([P(-10, -60), P(-16, 0)], fill=gelap, width=int(13 * skala))
    d.line([P(10, -60), P(18, 0)], fill=gelap, width=int(13 * skala))
    # badan: trapesium sempit lalu melebar di bahu
    d.polygon([P(-20, -150), P(20, -150), P(13, -58), P(-13, -58)], fill=gelap)
    # bahu
    d.ellipse([*P(-32, -168), *P(32, -140)], fill=gelap)
    # lengan panjang menggantung melewati lutut
    d.line([P(-28, -156), P(-44, -96), P(-38, -52)], fill=gelap, width=int(11 * skala), joint="curve")
    d.line([P(28, -156), P(44, -96), P(38, -52)], fill=gelap, width=int(11 * skala), joint="curve")    # leher panjang
    d.polygon([P(-8, -166), P(8, -166), P(6, -196), P(-6, -196)], fill=gelap)
    # kepala memanjang ke belakang (bentuk khas alien)
    d.ellipse([*P(-20, -246), *P(26, -190)], fill=gelap)
    # mata: dua celah menyala
    d.ellipse([*P(-13, -228), *P(-2, -216)], fill=(0xB4, 0xFF, 0xD8, 255))
    d.ellipse([*P(4, -230), *P(15, -218)], fill=(0xB4, 0xFF, 0xD8, 255))

    # garis tepi kehijauan supaya siluet tidak menyatu dengan laut gelap
    tepi_layar = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    td = ImageDraw.Draw(tepi_layar, "RGBA")
    td.ellipse([*P(-22, -250), *P(28, -188)], outline=tepi, width=max(1, int(2 * skala)))
    td.polygon([P(-20, -150), P(20, -150), P(13, -58), P(-13, -58)], outline=tepi,
               width=max(1, int(2 * skala)))
    return Image.alpha_composite(layar, tepi_layar)


def preview() -> Image.Image:
    w, h = 960, 540
    horizon = 300
    rnd = random.Random(11)
    # Palet hijau zamrud. Sebelumnya langit/laut memakai biru-es (#041015, #0F3A40) sehingga
    # pratinjau Bermuda terbaca seperti lanjutan Antartika, bukan perairan baru.
    langit = gradasi_vertikal(w, horizon, (0x07, 0x16, 0x0A), (0x13, 0x4A, 0x21))
    laut = gradasi_vertikal(w, h - horizon, (0x17, 0x66, 0x28), (0x06, 0x18, 0x0A))
    im = Image.new("RGBA", (w, h))
    im.paste(langit, (0, 0))
    im.paste(laut, (0, horizon))
    d = ImageDraw.Draw(im, "RGBA")
    # bintang dan bulan pucat kehijauan
    for _ in range(46):
        x, y = rnd.randint(0, w), rnd.randint(0, horizon - 40)
        d.ellipse([x, y, x + 2, y + 2], fill=(210, 245, 226, rnd.randint(70, 170)))
    d.ellipse([740, 52, 812, 124], fill=(0xD3, 0xF2, 0xDF, 255))
    d.ellipse([756, 66, 790, 100], fill=(0xBC, 0xE2, 0xC9, 255))
    # awan badai
    awan = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ad = ImageDraw.Draw(awan, "RGBA")
    for (x, y, rw, rh, a) in [(120, 90, 260, 60, 150), (380, 60, 300, 70, 130), (640, 120, 300, 64, 120),
                              (240, 170, 280, 54, 110), (560, 200, 280, 50, 100)]:
        ad.ellipse([x - rw / 2, y - rh / 2, x + rw / 2, y + rh / 2], fill=(0x05, 0x1C, 0x0C, a))
    awan = awan.filter(ImageFilter.GaussianBlur(14))
    im = Image.alpha_composite(im, awan)
    d = ImageDraw.Draw(im, "RGBA")
    # petir tipis
    d.polygon([(468, 40), (440, 130), (470, 126), (448, 206), (506, 108), (476, 112), (500, 40)],
              fill=(0xB4, 0xFF, 0xD8, 215))
    # garis ombak
    for k in range(7):
        y0 = horizon + 28 + k * 34
        pts = [(x, y0 + 5 * math.sin(x / 38.0 + k)) for x in range(0, w + 20, 20)]
        d.line(pts, fill=(0x7C, 0xE8, 0x9A, 26 + k * 3), width=3)
    # segitiga raksasa bercahaya (A kiri, B kanan, C puncak)
    A, B, C = (150, 470), (810, 470), (480, 150)
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow, "RGBA")
    gd.line([A, B, C, A], fill=MINT + (210,), width=12, joint="curve")
    glow = glow.filter(ImageFilter.GaussianBlur(10))
    im = Image.alpha_composite(im, glow)
    d = ImageDraw.Draw(im, "RGBA")
    # sisi putus-putus
    def garis_putus(p0, p1, dash=18, gap=12):
        L = math.hypot(p1[0] - p0[0], p1[1] - p0[1])
        n = int(L // (dash + gap))
        for i in range(n + 1):
            s0 = i * (dash + gap) / L
            s1 = min(1.0, (i * (dash + gap) + dash) / L)
            d.line([(p0[0] + (p1[0] - p0[0]) * s0, p0[1] + (p1[1] - p0[1]) * s0),
                    (p0[0] + (p1[0] - p0[0]) * s1, p0[1] + (p1[1] - p0[1]) * s1)],
                   fill=(0xE4, 0xFF, 0xEE, 235), width=5)
    for a, b in [(A, B), (B, C), (C, A)]:
        garis_putus(a, b)
    for x, y in (A, B, C):
        d.ellipse([x - 12, y - 12, x + 12, y + 12], fill=MINT + (255,))
        d.ellipse([x - 5, y - 5, x + 5, y + 5], fill=(0xE4, 0xFF, 0xEE, 255))
    # cahaya di belakang siluet alien supaya bentuknya terbaca dari jauh
    pendar = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    pd = ImageDraw.Draw(pendar, "RGBA")
    pd.ellipse([480 - 190, 330 - 175, 480 + 190, 330 + 175], fill=(0x55, 0xE8, 0x7C, 120))
    pd.ellipse([480 - 110, 330 - 120, 480 + 110, 330 + 120], fill=(0x8C, 0xF5, 0xA8, 150))
    im = Image.alpha_composite(im, pendar.filter(ImageFilter.GaussianBlur(38)))
    # pusaran air di tengah
    d = ImageDraw.Draw(im, "RGBA")
    for k in range(5):
        rx, ry = 150 - k * 24, 40 - k * 7
        d.arc([480 - rx, 470 - ry, 480 + rx, 470 + ry], 10 + k * 40, 330 + k * 40,
              fill=(0xB8, 0xF5, 0xC6, 150 - k * 20), width=4)
    # kabut tebal
    kabut = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    kd = ImageDraw.Draw(kabut, "RGBA")
    for (x, y, rw, rh, a) in [(160, 330, 420, 70, 90), (700, 350, 460, 76, 96), (480, 460, 700, 80, 90),
                              (300, 505, 520, 60, 70), (800, 485, 360, 60, 70)]:
        kd.ellipse([x - rw / 2, y - rh / 2, x + rw / 2, y + rh / 2], fill=(0xA8, 0xF0, 0xB8, a))
    kabut = kabut.filter(ImageFilter.GaussianBlur(26))
    im = Image.alpha_composite(im, kabut)
    # Siluet alien digambar setelah kabut agar tetap tajam dan terbaca. Sebelumnya digambar
    # sebelum kabut sehingga tubuhnya tertutup lapisan hijau muda dan nyaris tak terlihat.
    im = Image.alpha_composite(im, siluet_alien(w, h, 480, 452))
    # kompas melayang di pojok (jarum berputar liar)
    d = ImageDraw.Draw(im, "RGBA")
    cx, cy, r = 118, 118, 50
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(0x08, 0x33, 0x19, 235), outline=MINT + (255,), width=4)
    for ang in range(0, 360, 45):
        a = math.radians(ang)
        d.line([(cx + math.cos(a) * (r - 4), cy + math.sin(a) * (r - 4)),
                (cx + math.cos(a) * (r - 11), cy + math.sin(a) * (r - 11))], fill=(0xE4, 0xFF, 0xEE, 220), width=3)
    ang = math.radians(-35)
    tip = (cx + math.cos(ang) * 34, cy + math.sin(ang) * 34)
    tail = (cx - math.cos(ang) * 34, cy - math.sin(ang) * 34)
    d.line([tail, tip], fill=(0xE4, 0xFF, 0xEE, 255), width=5)
    d.polygon([tip, (cx + math.cos(ang + 2.6) * 10, cy + math.sin(ang + 2.6) * 10),
               (cx + math.cos(ang - 2.6) * 10, cy + math.sin(ang - 2.6) * 10)], fill=(0xFF, 0x6B, 0x81, 255))
    d.ellipse([cx - 5, cy - 5, cx + 5, cy + 5], fill=MINT + (255,))
    # vinyet gelap di tepi
    vg = np.zeros((h, w), float)
    yy, xx = np.mgrid[0:h, 0:w]
    vg = ((xx - w / 2) / (w / 2)) ** 2 + ((yy - h / 2) / (h / 2)) ** 2
    vg = np.clip((vg - 0.55) / 0.9, 0, 1) * 130
    vin = Image.new("RGBA", (w, h), (2, 10, 4, 0))
    vin.putalpha(Image.fromarray(vg.astype("uint8")))
    im = Image.alpha_composite(im, vin)
    return im.convert("RGB")



def main():
    gerbang().save(os.path.join(DIR, "gerbang_bermuda.png"), optimize=True)
    print("ok gerbang_bermuda.png")
    preview().save(os.path.join(DIR, "preview_bermuda.png"), optimize=True)
    print("ok preview_bermuda.png")


if __name__ == "__main__":
    main()

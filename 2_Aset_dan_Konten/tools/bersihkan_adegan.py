"""Menghapus alat pancing yang ikut tergambar permanen di ilustrasi adegan_*.png.

Alat (joran, tali, pelampung, tombak, jala, bubu, pukat) digambar dinamis oleh
MemancingScene.kt / MemancingGerak.kt. Jika tetap ada di PNG, hasilnya tampak ganda
(kail dobel). Selain menghapus alat, skrip menggeser kapal pada adegan_kapal dan
adegan_bubu sejauh 2 periode gelombang (420 px) ke kanan agar ada air terbuka untuk
lemparan; pola air tetap identik karena periodenya 210 px.

Selalu baca dari gambar ASLI (tools/adegan_asli), bukan dari hasil sebelumnya,
karena penggeseran kapal tidak idempoten.

Jalankan:  python3 2_Aset_dan_Konten/tools/bersihkan_adegan.py
           lalu  python3 2_Aset_dan_Konten/tools/sync_assets.py
Butuh: pillow, numpy, opencv-python-headless.
"""
import sys, numpy as np, cv2
from PIL import Image

P = 210  # periode gelombang latar (px). Geser kelipatan P => pola air identik.

def load(d, n): return np.array(Image.open(f"{d}/{n}.png").convert("RGB"))

def near(img, rgb, tol): return (np.abs(img.astype(int) - np.array(rgb)).max(axis=2) <= tol)

def box(m, x0, y0, x1, y1):
    o = np.zeros_like(m); o[y0:y1, x0:x1] = m[y0:y1, x0:x1]; return o

def dil(m, r):
    k = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (2*r+1, 2*r+1))
    return cv2.dilate(m.astype(np.uint8), k) > 0

def plate_air(img, base=880):
    """Pelat air murni: kolom periodik dari area air kosong di sisi kanan."""
    xs = base + (np.arange(img.shape[1]) - base) % P
    return img[:, xs]

def hapus(img, mask, plate_box=None, plate=None, r=4):
    out = cv2.inpaint(img, mask.astype(np.uint8) * 255, r, cv2.INPAINT_TELEA)
    if plate_box is not None:
        x0, y0, x1, y1 = plate_box
        sel = np.zeros(mask.shape, bool); sel[y0:y1, x0:x1] = True
        sel &= mask
        out[sel] = plate[sel]
    return out

def geser_kapal(img, plate, x0=175, x1=540, y0=468, y1=768, k=2):
    crop = img[y0:y1, x0:x1].copy()
    out = img.copy()
    out[y0:y1, x0:x1] = plate[y0:y1, x0:x1]
    out[y0:y1, x0 + k*P:x1 + k*P] = crop
    return out

def gabung(*ms):
    r = ms[0].copy()
    for m in ms[1:]: r |= m
    return r

WHITE_LINE = lambda im: (im.min(axis=2) >= 200) & ((im.max(axis=2) - im.min(axis=2)) <= 40)
BOBBER = lambda im: near(im,(255,255,255),6) | near(im,(229,56,79),6) | near(im,(242,106,46),6)
ROD = lambda im: dil(near(im,(122,75,38),45),2)

def kapal(d):
    im = load(d, "adegan_kapal")
    m = gabung(box(ROD(im),95,455,310,610), box(WHITE_LINE(im),100,455,170,720),
               box(BOBBER(im),95,670,160,725))
    m = dil(m, 3)
    im = hapus(im, m, (60,470,185,760), plate_air(im))
    return geser_kapal(im, plate_air(im))

def dermaga(d):
    im = load(d, "adegan_dermaga")
    line = box(WHITE_LINE(im)|near(im,(235,228,222),12),405,335,600,715)
    m = gabung(box(ROD(im),340,335,425,455), line, box(BOBBER(im),540,660,605,715))
    # jangan hapus piring putih ember/lampu: batasi di luar kotak ember
    m = dil(m, 3)
    return hapus(im, m, (500,560,620,725), plate_air(im, 700))

def cadik(d):
    im = load(d, "adegan_perahu_cadik")
    cor = np.zeros(im.shape[:2], np.uint8)
    pts = np.array([(300,456),(313,468),(364,528),(420,588),(437,600),(478,636),(513,660),(556,690)], np.int32)
    cv2.polylines(cor, [pts], False, 1, 12)
    rope = (cor > 0) & (im.min(axis=2) >= 185)
    net = np.zeros(im.shape[:2], bool)
    cv2.ellipse(net.view(np.uint8), (557,690), (92,34), 0, 0, 360, 1, -1)
    net = net & ~box(near(im,(188,204,206),8),470,640,500,670)
    m = dil(gabung(rope, net), 3)
    return hapus(im, m, (468,655,660,730), plate_air(im, 880))

def penyelam(d):
    im = load(d, "adegan_penyelam")
    sel = np.zeros(im.shape[:2], bool)
    sel[585:675, 418:645] = True
    gelap = (im[:, :, 1] < 48) & (im[:, :, 2] < 62)   # garis tepi mata tombak (air latar g>=55)
    m = (WHITE_LINE(im) | near(im,(143,178,184),10) | near(im,(232,244,242),10) | gelap) & sel
    m = dil(m, 4)
    return hapus(im, m, None, None, r=6)

def lubang_es(d):
    im = load(d, "adegan_lubang_es")
    m = gabung(box(ROD(im),308,440,392,500), box(WHITE_LINE(im)|near(im,(215,225,230),30),232,440,338,640),
               box(BOBBER(im),220,610,270,655))
    m = dil(m,3)
    inner = np.zeros(im.shape[:2], np.uint8)
    cv2.ellipse(inner, (244,639), (68,20), 0, 0, 360, 1, -1)
    dalam = m & (inner > 0)
    im[dalam] = (14,74,99)
    return hapus(im, m & ~dalam)

def bubu(d):
    im = load(d, "adegan_bubu")
    sel = np.zeros(im.shape[:2], bool)
    m = gabung(box(ROD(im),160,530,310,610), box(WHITE_LINE(im),120,535,185,700),
               box(BOBBER(im),100,670,165,725))
    tali = np.zeros(im.shape[:2], bool); tali[700:790, 125:140] = True
    trap = np.zeros(im.shape[:2], bool); trap[770:845, 85:180] = True
    m = dil(gabung(m, tali, trap), 3)
    # titik ujung joran (bulatan putih kecil)
    dot = np.zeros(im.shape[:2], bool); cv2.circle(dot.view(np.uint8),(172,546),11,1,-1)
    m = gabung(m, dot)
    im = hapus(im, m, (60,690,200,850), plate_air(im))
    return geser_kapal(im, plate_air(im))

def badai(d):
    im = load(d, "adegan_kapal_pukat_badai")
    line = box(WHITE_LINE(im),118,362,150,712)
    net = np.zeros(im.shape[:2], bool); net[690:800, 70:170] = True
    net &= ~box(near(im,(100,130,150),200),0,0,0,0)
    # hanya piksel jaring/pelampung, bukan gelombang terang di belakangnya
    netpix = (WHITE_LINE(im) | BOBBER(im) | near(im,(57,87,99),12) | near(im,(70,100,112),30)
              | near(im,(150,180,190),30)) & net
    m = dil(gabung(line, netpix), 4)
    return hapus(im, m, (70,690,175,805), plate_air(im, 880))

if __name__ == "__main__":
    import os
    ini = os.path.dirname(os.path.abspath(__file__))
    src = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ini, "adegan_asli")
    dst = sys.argv[2] if len(sys.argv) > 2 else os.path.join(ini, "..", "assets-source", "03_Studi_Kasus")
    for n, f in [("adegan_kapal",kapal),("adegan_dermaga",dermaga),("adegan_perahu_cadik",cadik),
                 ("adegan_penyelam",penyelam),("adegan_lubang_es",lubang_es),
                 ("adegan_bubu",bubu),("adegan_kapal_pukat_badai",badai)]:
        Image.fromarray(f(src)).save(f"{dst}/{n}.png", optimize=True); print("ok", n)

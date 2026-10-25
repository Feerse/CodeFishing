#!/usr/bin/env python3
"""Sinkronkan folder 2_Aset_dan_Konten ke dalam proyek Android.

  assets-source/NN_Folder/*.png -> app/src/main/res/drawable-nodpi   (+ buat core/art/Art.kt)
  audio/*.mp3|ogg               -> app/src/main/res/raw
  konten/*.json                 -> app/src/main/assets/content

Jalankan dari mana saja:  python3 2_Aset_dan_Konten/tools/sync_assets.py
Nama berkas: huruf kecil, angka, underscore, diawali huruf.
"""
import os, re, shutil, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)              # 2_Aset_dan_Konten
REPO = os.path.dirname(ROOT)              # root repo
SRC = os.path.join(ROOT, "assets-source")
AUDIO = os.path.join(ROOT, "audio")
KONTEN = os.path.join(ROOT, "konten")
RES = os.path.join(REPO, "app/src/main/res")
DRAW = os.path.join(RES, "drawable-nodpi")
RAW = os.path.join(RES, "raw")
CONTENT = os.path.join(REPO, "app/src/main/assets/content")
ART = os.path.join(REPO, "app/src/main/java/com/example/codefishing/core/art/Art.kt")

PNG_OK = re.compile(r"^[a-z][a-z0-9_]*\.png$")
RAW_OK = re.compile(r"^[a-z][a-z0-9_]*\.(mp3|ogg|wav|m4a)$")
KW = {"in", "is", "as", "do", "if", "for", "val", "var", "fun", "object", "class", "when", "try", "typealias", "interface"}


def sync_dir(files, dst, keep_ext):
    os.makedirs(dst, exist_ok=True)
    names = set()
    for p in files:
        shutil.copyfile(p, os.path.join(dst, os.path.basename(p)))
        names.add(os.path.basename(p))
    for f in os.listdir(dst):
        if f.lower().endswith(keep_ext) and f not in names:
            os.remove(os.path.join(dst, f))


errors, seen, groups = [], {}, []
for d in sorted(os.listdir(SRC)):
    p = os.path.join(SRC, d)
    if not (os.path.isdir(p) and re.match(r"^\d\d_", d)):
        continue
    names = []
    for f in sorted(os.listdir(p)):
        if not f.lower().endswith(".png"):
            continue
        if not PNG_OK.match(f):
            errors.append(f"Nama gambar tidak valid: {d}/{f}"); continue
        if f in seen:
            errors.append(f"Nama gambar ganda: {f} ({seen[f]} dan {d})"); continue
        seen[f] = d; names.append(f[:-4])
    groups.append((d, names))

audio = []
for f in sorted(os.listdir(AUDIO)) if os.path.isdir(AUDIO) else []:
    if f.startswith("."):
        continue
    if not RAW_OK.match(f):
        errors.append(f"Nama audio tidak valid: {f}"); continue
    audio.append(os.path.join(AUDIO, f))
if errors:
    print("\n".join(errors)); sys.exit(1)

sync_dir([os.path.join(SRC, d, n + ".png") for d, ns in groups for n in ns], DRAW, ".png")
sync_dir(audio, RAW, (".mp3", ".ogg", ".wav", ".m4a"))
sync_dir([os.path.join(KONTEN, f) for f in sorted(os.listdir(KONTEN)) if f.endswith(".json")], CONTENT, ".json")


def ident(n):
    return "`%s`" % n if n in KW else n


L = ["// FILE HASIL GENERATE oleh 2_Aset_dan_Konten/tools/sync_assets.py. Jangan diedit tangan.",
     '@file:Suppress("ObjectPropertyName", "unused")', "",
     "package com.example.codefishing.core.art", "",
     "import androidx.annotation.DrawableRes", "import com.example.codefishing.R", "", "object Art {"]
for d, names in groups:
    L.append(f"    // {d}")
    L += [f"    @DrawableRes val {ident(n)}: Int = R.drawable.{n}" for n in names]
    L.append("")
L.append("    /** Semua aset menurut folder. Dipakai Galeri Aset (debug) dan pencarian lewat nama. */")
L.append("    val GROUPS: List<Pair<String, List<Pair<String, Int>>>> = listOf(")
for d, names in groups:
    L.append(f'        "{d}" to listOf(')
    L += [f'            "{n}" to R.drawable.{n},' for n in names]
    L.append("        ),")
L += ["    )", "", "    private val byName: Map<String, Int> by lazy { GROUPS.flatMap { it.second }.toMap() }", "",
      '    /** Cari id drawable lewat nama tanpa .png, mis. Art.byName("ikan_teri"). */',
      "    @DrawableRes fun byName(name: String): Int? = byName[name]", "}"]
os.makedirs(os.path.dirname(ART), exist_ok=True)
open(ART, "w").write("\n".join(L) + "\n")
print(f"OK: {sum(len(n) for _, n in groups)} gambar, {len(audio)} audio, Art.kt dibuat")

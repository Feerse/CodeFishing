# 4. Logika Mode Expert (Sandbox)

Kode: `app/src/main/java/com/example/codefishing/expert/`

- `SandboxModel.kt`: `Dunia`, `Biaya` (gerak 1, lempar 1, lihat 0), jejak, tangkapan, sampah.
- `SandboxEngine.kt`: memuat 7 misi dari `assets/seed/misi.json`, menjalankan kode, mengevaluasi tujuan, menghitung bintang.
- `Scoring.kt`: bintang 3 bila ticks ≤ par, 2 bila ticks ≤ ceil(par × 1,5), selain itu 1; tanpa par = 3.
- `py/`: lexer, parser, AST, interpreter subset Python (variabel, ekspresi, if/elif/else, while, for/range, def/return, print).

Status: interpreter dan mesin misi sudah terhubung ke layar Sandbox. Setiap jalan disimpan ke `hasil_program`/`hasil_tangkapan`; hasil eksekusi tampil inline di editor, ikan yang tertangkap masuk koleksi, dan XP diberikan per ekor. Draf kode per misi dipertahankan selama sesi. Batas 1000 tick dan 10.000 langkah.

Misi dan petak ada di `2_Aset_dan_Konten/konten/seed/misi.json`, disalin ke `app/src/main/assets/seed/misi.json`. Aturan lengkap: `docs/ATURAN_GAME.md`.

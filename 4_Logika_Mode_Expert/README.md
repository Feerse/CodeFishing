# 4. Logika Mode Expert (Sandbox)

Kode: `app/src/main/java/com/example/codefishing/expert/`

- `SandboxModel.kt`: `Dunia`, `Biaya` (gerak 1, lempar 1, lihat 0), jejak, tangkapan, sampah.
- `SandboxEngine.kt`: memuat 7 misi dari `assets/seed/misi.json`, menjalankan kode, mengevaluasi tujuan, menghitung bintang.
- `Scoring.kt`: bintang 3 bila ticks ≤ par, 2 bila ticks ≤ ceil(par × 1,5), selain itu 1; tanpa par = 3.
- `EditorKode.kt`: helper murni untuk menyisipkan potongan kode di kursor, auto-indent setelah `:`, dan Shift+Tab.
- `PanduanExpert.kt`: isi Guidebook (perintah, sintaks, error umum, tombol sisip cepat, hint papan).
- `py/`: lexer, parser, AST, interpreter subset Python (variabel, ekspresi, if/elif/else, while, for/range, def/return, print).

Status: interpreter dan mesin misi sudah terhubung ke layar Sandbox. Papan petak laut tampil di atas editor untuk posisi awal dan akhir kapal; hasil eksekusi tampil inline di bawah tombol Jalankan, dan tombol **Misi Berikutnya** muncul setelah misi berhasil. Setiap jalan disimpan ke `hasil_program`/`hasil_tangkapan`; ikan yang tertangkap masuk koleksi, dan XP diberikan per ekor. Draf kode per misi dipertahankan selama sesi. Batas 1000 tick dan 10.000 langkah.

Guidebook Mode Expert tersedia sebagai bottom sheet tiga tab (Perintah, Sintaks, Misi) dari daftar misi, kartu Petunjuk, dan kartu error. Di bawah editor ada baris tombol sisip cepat; Enter dan Tab fisik memakai auto-indent 4 spasi. Tombol **Kode Awal** memuat ulang snippet starter misi (bukan solusi) dan menampilkan umpan balik inline.

Misi dan petak ada di `2_Aset_dan_Konten/konten/seed/misi.json`, disalin ke `app/src/main/assets/seed/misi.json`. Aturan lengkap: `docs/ATURAN_GAME.md`.

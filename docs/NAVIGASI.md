# Navigasi dan 14 layar

Sumber: `CodeFishing_DFD_ERD.md` (DFD K9) dan prototipe `5_Desainer_UIUX/prototype/codefishing.html`.
Navigasi di kode: `core/nav/` (`Screen`, `AppNavigator`, `DrawerItem`, `BottomItem`), dirangkai di `frontend/App.kt`.

## Aturan navigasi (hasil evaluasi dosen)

1. **Pengenalan** (layar 14) muncul **sekali** saat pertama dibuka dan menyebut Python. Bisa dilihat lagi dari Pengaturan. Tanpa toolbar.
2. **Toolbar seragam** di semua layar lain: ikon hamburger (kiri), judul, indikator umpan (kanan, hanya layar Mode Belajar). **Tidak ada tombol Kembali** di toolbar. Back sistem Android tetap berfungsi lewat `AppNavigator.back()`.
3. **Drawer** punya lima tujuan: Home, Mode Belajar (Peta), Mode Expert (Sandbox), Akuarium, Pengaturan. Memilih tujuan mengosongkan tumpukan (`resetTo`).
4. **Bottom navigation** punya empat tab: Home, Peta, Expert, Koleksi. Koleksi hanya ada di bottom navigation, tidak diulang di drawer.
5. Bottom navigation ditampilkan pada layar utama (Home, Pilih Mode, Peta, Materi, Hasil Stage, Sandbox, Hasil Program, Koleksi, Akuarium, Pengaturan) dan disembunyikan saat kuis/boss/umpan habis agar fokus.
6. UI minimalis: gambar lebih banyak, kalimat penjelasan sedikit.

## Daftar layar

| # | Layar | Kelas Kotlin | Spesifikasi | Tautan |
|---|---|---|---|---|
| 1 | Home | `HomeScreen` | Latar langit malam, logo, kartu pemain (level, XP, umpan, stage), tantangan harian, kartu Lanjutkan, progres koleksi | Lanjutkan → 04 / 07 / 12 sesuai posisi terakhir · Mulai Main → 02 · bottom nav |
| 2 | Pilih Mode | `PilihModeScreen` | Dua kartu bergambar: Belajar (Pemula) dan Sandbox (Expert), tanpa kalimat penjelasan | Belajar → 03 · Sandbox → 07 · bottom nav |
| 3 | Peta Perairan | `PetaScreen` | Peta nautical SVG vertikal 1000×2800 yang digeser atas-bawah: pulau, gunung es, mercusuar, kompas, rute berliku, kapal, 10 node stage dengan progress ring dan status, gerbang Antartika. Ketuk node untuk membuka lembar info | Stage 1–9 → 04 · Stage 10 (Boss) → 12 · bottom nav |
| 4 | Materi | `MateriScreen` | Judul dengan gambar ikan stage, zona, konsep, materi, contoh kode, output, trik nelayan | Ke Kuis → 05 · bottom nav |
| 5 | Kuis | `KuisScreen` | Progres 5 soal, kartu soal + kode PYTHON, 4 pilihan, umpan berkurang saat salah, pembahasan, combo/XP | Selesai → 06 · Umpan habis → 11 · Back sistem |
| 6 | Hasil Stage | `HasilStageScreen` | Ikon lulus/gagal, skor, benar/total, XP, ikan target + tier/bobot, bonus combo, rekor baru | Stage berikutnya → 04 / 12 · Peta → 03 · bottom nav |
| 7 | Sandbox | `SandboxScreen` | Daftar 7 misi + bintang; papan petak laut di atas editor yang menampilkan posisi awal dan posisi akhir kapal, editor kode Python, tombol Jalankan/Contoh, hasil inline berisi tick/ikan/sampah/XP, output, dan jejak gerak; draf kode per misi | Tetap di editor · Daftar → 07 · bottom nav |
| 8 | Hasil Program | `HasilProgramScreen` | Layar hasil tersimpan: ikon berhasil/gagal, bintang, tick/ikan/XP, error bila ada, hasil tangkapan, output, kode terakhir (tidak dibuka otomatis dari Sandbox) | Ubah Kode → 07 · Misi berikutnya → 07 · Akuarium → 13 · bottom nav |
| 9 | Koleksi | `KoleksiScreen` | Grid 48 ikan, filter tier, status dimiliki/terkunci, detail + konsep Python, beri makan | Akuarium → 13 · bottom nav |
| 10 | Pengaturan | `PengaturanScreen` | Statistik pemain, saklar efek suara/musik/haptik/pewarnaan, Lihat Pengenalan, reset progres, galeri aset (debug) | Pengenalan → 14 · bottom nav |
| 11 | Umpan Habis | `UmpanHabisScreen` | Hitung mundur besar, jumlah umpan, emas, tombol Lanjutkan aktif saat umpan ≥ 1, tukar umpan emas | Lanjutkan Kuis → 05 · Baca Materi → 04 · Peta → 03 |
| 12 | Stage Boss | `BossScreen` | Kuis boss 5 soal dengan ambang lulus 4/5 (80%), tanpa materi | Selesai → 06 · Peta → 03 |
| 13 | Akuarium | `AkuariumScreen` | Tangki berlapis kertas, ikan yang dimiliki berenang, ketuk untuk info, beri makan satu/semua | Koleksi → 09 · Peta → 03 · bottom nav |
| 14 | Pengenalan | `PengenalanScreen` | Empat halaman bergambar tanpa toolbar: Python sambil memancing, pilih umpan kode, tulis program, akuarium dan offline | Lanjut → halaman berikutnya · Lewati/Mulai → 01 |

Layar 99 `GaleriAset` hanya untuk debug (Pengaturan > Galeri Aset).

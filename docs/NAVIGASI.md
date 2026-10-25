# Navigasi dan 14 layar

Sumber: `5_Desainer_UIUX/prototype/codefishing.html` (tab Struktur Navigasi). Navigasi di kode: `core/nav/` (`Screen`, `AppNavigator`, `DrawerItem`), dirangkai di `frontend/App.kt`.

## Aturan navigasi (hasil evaluasi dosen)

1. **Pengenalan** (layar 14) muncul **sekali** saat pertama dibuka dan menyebut Python. Bisa dilihat lagi dari Pengaturan. Tanpa toolbar.
2. **Toolbar seragam** di semua layar lain: ikon hamburger (kiri), judul (tengah), indikator umpan (kanan, hanya layar Mode Belajar). **Tidak ada tombol Kembali** di toolbar. Back sistem Android tetap berfungsi lewat `AppNavigator.back()`.
3. **Drawer** punya enam tujuan: Home, Mode Belajar (Peta), Mode Expert (Sandbox), Akuarium, Koleksi, Pengaturan. Memilih tujuan mengosongkan tumpukan (`resetTo`).
4. UI minimalis: gambar lebih banyak, kalimat penjelasan sedikit.

## Daftar layar

| # | Layar | Kelas Kotlin | Spesifikasi | Tautan |
|---|---|---|---|---|
| 1 | Home | `HomeScreen` | Gambar kapal, logo, label Python, kartu Lanjutkan (kalau sudah ada progres), tombol Mulai Main | Lanjutkan → 04 / 05 / 07 / 12 sesuai posisi terakhir · Mulai Main → 02 · Menu ≡ → drawer |
| 2 | Pilih Mode | `PilihModeScreen` | Dua kartu bergambar: Belajar (Pemula) dan Sandbox (Expert), tanpa kalimat penjelasan | Belajar → 03 · Sandbox → 07 · Menu ≡ → drawer |
| 3 | Peta Perairan | `PetaScreen` | Peta 2D yang digeser: kapal pemancing, stage 1–10 sebagai titik di rute, tiap perairan punya kondisi air; ketuk titik untuk info lalu Berlayar | Stage 1–9 terbuka → 04 · Stage 10 (Boss) → 12 · Menu ≡ → drawer |
| 4 | Materi | `MateriScreen` | Judul dengan gambar ikan stage, bar progres, satu kalimat materi, contoh kode, keluaran | Ke Kuis → 05 · Menu ≡ → drawer |
| 5 | Kuis | `KuisScreen` | Atas: animasi nelayan dan hasil kode (ikon stage, ikan, beruntun). Tengah: soal satu kalimat dan editor kode berlabel PYTHON dengan slot kosong. Bawah: 4 pilihan umpan kode, tombol reset (ikon), dan Lempar Kail. Hasil muncul sebagai kartu: benar = ikan terpancing, salah = kail putus dan umpan berkurang | Stage selesai → 06 · Umpan habis → 11 · Menu ≡ → drawer |
| 6 | Hasil Stage | `HasilStageScreen` | Ikon lulus atau belum, persen progres, ikan yang didapat beserta grade, dua tombol | Stage berikutnya → 04 / 12 · Peta → 03 · Menu ≡ → drawer |
| 7 | Sandbox | `SandboxScreen` | Atas: petak laut berisi ikan dan sampah, kapal kertas, misi, ikan dan waktu (berikon), bintang. Tengah: editor Python berwarna dengan nomor baris dan tombol cepat. Bawah: konsol, tombol ikon Misi, Fungsi, ulang, kecepatan, dan Jalankan. Tanpa umpan | Selesai jalan → 08 · Akuarium → 13 · Misi / Fungsi → panel di layar ini · Menu ≡ → drawer |
| 8 | Hasil Program | `HasilProgramScreen` | Ikon berhasil atau gagal, tiga angka berikon (waktu, ikan, sampah), ikan hasil tangkapan, keluaran print bila ada. Penjelasan hanya muncul saat gagal atau error | Ubah Kode → 07 · Akuarium → 13 · Misi berikutnya → 07 · Menu ≡ → drawer |
| 9 | Koleksi | `KoleksiScreen` | Grid ikan 15 slot dengan warna sesuai kelangkaan; yang belum didapat abu-abu dengan ikon gembok. Hitungan ikan dan tombol Akuarium di atas | Akuarium → 13 · Menu ≡ → drawer |
| 10 | Pengaturan | `PengaturanScreen` | Saklar efek suara dan musik, tombol Lihat Pengenalan, dan reset progres | Lihat Pengenalan → 14 · Menu ≡ → drawer |
| 11 | Umpan Habis | `UmpanHabisScreen` | Hitung mundur besar dan ikon umpan; tombol Lanjutkan aktif saat umpan ≥ 1 | Lanjutkan Kuis → 05 · Baca Materi → 04 · Peta → 03 · Menu ≡ → drawer |
| 12 | Stage Boss | `BossScreen` | Gambar kapal pukat di badai, tiga kotak ikon (syarat lulus 70%, salah −1 umpan, umpan kamu); tanpa materi | Mulai Boss → 05 · Menu ≡ → drawer |
| 13 | Akuarium | `AkuariumScreen` | Tangki berlapis kertas dengan ikan vektor yang bergerak ala potongan kertas: ekor berengsel, berbalik seperti membalik kertas, gerakan patah-patah. Berisi ikan Koleksi Belajar dan hasil Sandbox. Ketuk ikan untuk info, Beri makan agar ikan berkumpul | Sandbox → 07 · Menu ≡ → drawer |
| 14 | Pengenalan | `PengenalanScreen` | Empat halaman bergambar tanpa toolbar: (1) Python sambil memancing, (2) pilih umpan kode, (3) tulis program di Mode Expert, (4) akuarium dan offline. Muncul hanya saat pertama kali dibuka, bisa dilihat lagi dari Pengaturan | Lanjut → halaman berikutnya · Lewati atau Mulai → 01 |

Layar 99 `GaleriAset` hanya untuk debug (Pengaturan > Galeri Aset).

# CodeFishing 🎣

Game Android edukasi offline untuk satu pemain: **belajar Python sambil memancing**. Proyek PPBM Kelompok 1. Kotlin + Jetpack Compose.

- **Mode Belajar (Pemula)**: 10 stage Python, kuis umpan kode, umpan (nyawa), koleksi ikan.
- **Mode Expert (Sandbox)**: kodekan kapal otomatis dengan Python untuk menyelesaikan studi kasus, dinilai bintang.
- **Akuarium** dan **Koleksi** 15 ikan. Musik latar berjalan di semua layar.

## Lima folder tim

| # | Folder tim | Isi di repo | Kode Kotlin ada di |
|---|---|---|---|
| 1 | [`1_Frontend_Compose`](1_Frontend_Compose/README.md) | Panduan tim | `app/src/main/java/com/example/codefishing/frontend/` (14 layar, toolbar, komponen) |
| 2 | [`2_Aset_dan_Konten`](2_Aset_dan_Konten/README.md) | **Sumber** gambar, audio, logo, dan JSON konten, plus skrip sinkron | hasil sinkron ke `app/src/main/res` dan `assets/content` |
| 3 | [`3_Logika_Mode_Pemula`](3_Logika_Mode_Pemula/README.md) | Panduan tim | `.../pemula/` (kuis, umpan, kelangkaan ikan) |
| 4 | [`4_Logika_Mode_Expert`](4_Logika_Mode_Expert/README.md) | Panduan tim | `.../expert/` (dunia sandbox, penjalan kode, bintang) |
| 5 | [`5_Desainer_UIUX`](5_Desainer_UIUX/README.md) | Prototipe HTML, panduan desain | `.../ui/theme/` (warna, font) |

Kode bersama (navigasi, audio, pembaca konten, penyimpanan) ada di `.../core/` dan dikelola manajer repo (Alief).

> Folder 1, 3, 4 sengaja hanya berisi panduan, karena kode Android harus berada di dalam `app/` agar Gradle bisa membangunnya.

## Menjalankan

1. Buka folder ini di Android Studio, tunggu Gradle sync (butuh internet pertama kali).
2. Pilih emulator/HP (Android 8.0+), klik Run.
3. Tes logika: `./gradlew test`.

## Dokumen

[Pembagian tugas](docs/PEMBAGIAN_TUGAS.md) · [Cara kontribusi Git](CONTRIBUTING.md) · [Navigasi 14 layar](docs/NAVIGASI.md) · [Aturan game](docs/ATURAN_GAME.md)

Tahap berikutnya: database/ERD (belum dikerjakan; progres sementara memakai SharedPreferences di `core/data/ProgressRepository`).

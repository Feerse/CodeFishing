# CodeFishing

Game Android edukasi offline untuk satu pemain: **belajar Python sambil memancing**. Proyek PPBM Kelompok 1. Kotlin + Jetpack Compose + Room.

- **Mode Belajar (Pemula)**: 10 stage Python, kuis 5 soal per stage, umpan (nyawa), boss 4/5, koleksi ikan.
- **Mode Expert (Sandbox)**: tulis program Python subset untuk menggerakkan kapal dan menyelesaikan 7 misi, dinilai bintang.
- **Akuarium** dan **Koleksi 48 ikan**: 15 ikan berilustrasi dan 33 placeholder “Segera hadir — menunggu aset”.
- **Tantangan harian** dan umpan emas.
- Sepenuhnya offline; aplikasi tidak meminta izin internet.

## Lima folder tim

| # | Folder tim | Isi di repo | Kode Kotlin ada di |
|---|---|---|---|
| 1 | [`1_Frontend_Compose`](1_Frontend_Compose/README.md) | Panduan tim | `app/src/main/java/com/example/codefishing/frontend/` (14 layar, toolbar, drawer, bottom nav, komponen) |
| 2 | [`2_Aset_dan_Konten`](2_Aset_dan_Konten/README.md) | **Sumber** gambar, audio, logo, JSON seed, generator | hasil sinkron ke `app/src/main/res` dan `app/src/main/assets/seed` |
| 3 | [`3_Logika_Mode_Pemula`](3_Logika_Mode_Pemula/README.md) | Panduan tim | `.../pemula/` (kuis, umpan, level, kelangkaan/bonus ikan) |
| 4 | [`4_Logika_Mode_Expert`](4_Logika_Mode_Expert/README.md) | Panduan tim | `.../expert/` (dunia sandbox, interpreter Python subset, evaluasi misi, bintang) |
| 5 | [`5_Desainer_UIUX`](5_Desainer_UIUX/README.md) | Prototipe HTML, panduan desain | `.../ui/theme/` (warna, font) |

Kode bersama (navigasi, audio, Room/ERD, seed) ada di `.../core/` dan dikelola manajer repo (Alief).

> Folder 1, 3, 4 sengaja hanya berisi panduan, karena kode Android harus berada di dalam `app/` agar Gradle bisa membangunnya.

## Menjalankan

1. Buka folder ini di Android Studio, tunggu Gradle sync (butuh internet pertama kali).
2. Pilih emulator/HP (Android 8.0+), klik Run.
3. Tes logika: `./gradlew test`.
4. Build APK debug: `./gradlew assembleDebug`.

### Catatan rilis

Versi Room ini menggantikan penyimpanan progres prototipe di `SharedPreferences` (`codefishing_progress`). Status pengenalan dan preferensi suara tetap dipertahankan, tetapi progres stage, umpan, bintang misi, dan koleksi lama **tidak dimigrasikan**; permainan dimulai dari progres baru di Room. Database Room memakai versi 1 tanpa migrasi destruktif, jadi data Room yang sudah ada tidak dihapus saat pembaruan konten seed.

## Dokumen

[Pembagian tugas](docs/PEMBAGIAN_TUGAS.md) · [Cara kontribusi Git](CONTRIBUTING.md) · [Navigasi 14 layar](docs/NAVIGASI.md) · [Aturan game](docs/ATURAN_GAME.md)

Skema database mengikuti `CodeFishing_DFD_ERD.md`: 16 tabel + 2 view Room. Konten kanonik ada di `2_Aset_dan_Konten/konten/seed/` dan dimuat oleh `core/db/SeedLoader`.

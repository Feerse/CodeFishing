# 1. Frontend Jetpack Compose

Kode: `app/src/main/java/com/example/codefishing/frontend/`
- `App.kt`: akar tampilan, memilih layar dan memasang toolbar
- `screens/`: 14 layar (satu layar satu berkas) + GaleriAset (debug)
- `components/`: `AppScaffold` (toolbar hamburger + drawer), `Gambar`, tombol, indikator umpan

Tugas: ganti `LayarSementara` dengan UI asli. Spesifikasi tiap layar ada di KDoc berkas dan `docs/NAVIGASI.md`.
Aturan: toolbar seragam, tanpa tombol Kembali, minimalis, banyak gambar. Pakai gambar lewat `Art.nama` dan suara lewat `LocalContainer.current.audio.play(Sfx.Benar)`.
Layar yang sudah jalan: Pengenalan, Home, Pilih Mode, Pengaturan, Akuarium (rangka), Galeri Aset.

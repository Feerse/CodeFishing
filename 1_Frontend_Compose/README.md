# 1. Frontend Jetpack Compose

Kode: `app/src/main/java/com/example/codefishing/frontend/`

- `App.kt`: akar tampilan, drawer 5 tujuan, bottom navigation 4 tab, routing hasil.
- `screens/`: 14 layar + GaleriAset (debug). Semua sudah memakai UI asli.
- `components/`: `AppScaffold`, `UI.kt` (kartu, chip tier, bintang, kode, proses, grid), `Komponen.kt` (tombol, indikator umpan).

Status: seluruh layar selesai. `LayarSementara` tidak lagi dipakai.

Aturan: toolbar seragam, tanpa tombol Kembali, minimalis, banyak gambar. Pakai gambar lewat `Art.nama` dan suara lewat `LocalContainer.current.audio.play(Sfx.Benar)`. Data dibaca/ditulis lewat `core/data/GameRepository`, bukan langsung ke DAO.

Layar utama: Home, Pilih Mode, Peta, Materi, Kuis, Hasil Stage, Sandbox, Hasil Program, Koleksi, Pengaturan, Umpan Habis, Boss, Akuarium, Pengenalan.

# CodeFishing — konteks proyek (untuk agen)

Game Android edukasi offline: belajar Python sambil memancing.
Kotlin + Jetpack Compose + Room + AndroidSVG. Package: `com.example.codefishing`.
Mode Belajar (Pemula): peta SVG vertikal, 10 stage Pasifik, kuis pilihan ganda, boss di stage 10.
Mode Expert: sandbox Python subset. Plus Akuarium, Koleksi ikan, tantangan harian.
Aturan game lengkap: `docs/ATURAN_GAME.md` (baca bagian yang relevan saja, jangan seluruhnya).

## Tugas aktif
Daftar tugas rinci dan statusnya ada di `TUGAS_HERMES.md`.
Kerjakan SATU tugas per giliran, sesuai urutan di file itu. Setelah selesai, centang di sana.

## Peta kode (app/src/main/java/com/example/codefishing/)
- `frontend/App.kt`        : host navigasi, memilih layar (`when`), toolbar, bottom nav
- `frontend/screens/`      : HomeScreen, PetaScreen, MateriScreen, KuisScreen, BossScreen, dst.
- `frontend/peta/`         : PetaGeometri.kt (posisi 10 stage), PetaSvg.kt (muat SVG)
- `pemula/`                : QuizEngine, UrutanSoal (acak soal), LevelRules, BaitRules
- `core/data/GameRepository.kt` : logika kuis/stage/umpan/ikan (file besar, baca per bagian)
- `core/db/`               : Room (Entities, Daos, SeedLoader). `SEED_VERSION` ada di SeedLoader.kt
- `core/nav/`              : AppNavigator (stack berbasis state), Screen
- `core/AppContainer.kt`   : inisialisasi DB + seed dijalankan saat start
- `ui/theme/Color.kt`      : palet terang/gelap (`LocalWarna`)

## Alur data konten (PENTING, jangan dilanggar)
Sumber kebenaran → generator → hasil. JANGAN edit file hasil secara manual.
1. Sumber: `2_Aset_dan_Konten/konten/{stages,ikan_config,misi}.json`
2. `python3 2_Aset_dan_Konten/tools/sync_assets.py`  → salin ke `app/src/main/assets/content` (+ aset gambar, Art.kt)
3. `node 2_Aset_dan_Konten/tools/generate_seed.js`   → buat seed ke `2_Aset_dan_Konten/konten/seed/` DAN `app/src/main/assets/seed/`
4. Peta: `node 2_Aset_dan_Konten/tools/generate_peta_svg.js` lalu `python3 2_Aset_dan_Konten/tools/generate_tema_terang.py` lalu `sync_assets.py`
Semua generator sudah diuji idempoten (dijalankan ulang tanpa perubahan = hasil identik).
`generate_seed.js` punya logika hardcode untuk 10 stage (boss, zona, materi). Ubah generatornya,
bukan seed-nya, karena seed akan tertimpa.
`core/art/Art.kt` dibuat otomatis oleh sync_assets.py. Jangan diedit tangan.
Kalau seed berubah, naikkan `SEED_VERSION` di SeedLoader.kt. Skema Room tidak diubah kecuali diminta.

## ABAIKAN (jangan dibaca, di-list, atau di-grep isinya)
- `build/`, `app/build/`, `.gradle/`, `.idea/`
- `app/src/main/res/drawable-nodpi/` dan `2_Aset_dan_Konten/assets-source/` (ratusan gambar; lihat nama file saja dengan `ls`)
- `5_Desainer_UIUX/prototype/codefishing.html` (200 KB), `CodeFishing_DFD_ERD.md` (74 KB, hanya via `grep -n`)
- `app/schemas/**` (JSON skema Room)
- Dilarang menghapus atau memindahkan file gambar/audio tanpa izin eksplisit.

## Aturan pencarian
Pakai `git grep -n` dan `git ls-files`. Baca file besar per rentang baris (`sed -n 100,160p`).
File besar: Art.kt (30 KB), GameRepository.kt (23 KB), PetaScreen.kt, AkuariumScreen.kt, PapanMisi.kt, MemancingScene.kt.

## Build dan tes
- Tes unit: `./gradlew testDebugUnitTest --offline`  (atau `./gradlew test`)
- Compile: `./gradlew assembleDebug --offline`
- Jalankan build/tes SETELAH satu tugas selesai, bukan setiap perubahan kecil.
- Jika Gradle gagal karena JDK atau Android SDK tidak ada: JANGAN mencoba memasang SDK.
  Lakukan verifikasi statis (baca ulang diff, cek import, jalankan generator + tes JSON bila bisa),
  lalu katakan jelas bahwa build belum terverifikasi dan harus dijalankan di Android Studio.
- Tes yang memeriksa jumlah (SeedValidationTest, AturanGameTest, dst.) harus diubah dengan sengaja
  mengikuti perubahan konten. Jangan melemahkan atau menghapus tes agar lolos.

## Aturan kerja
- Satu tugas kecil per giliran, ringkas perubahan, lalu berhenti dan tunggu "lanjut".
- Sebelum mengubah banyak file sekaligus, tulis rencana 3–6 baris dan tunggu persetujuan.
- Git: jika belum ada repo, `git init` + commit baseline. Satu commit per tugas (pesan dalam bahasa Indonesia).
- Jangan ubah ID soal, pilihan, stage, ikan yang sudah ada (data progres pemain merujuknya).
  Konten baru memakai ID lanjutan.
- Jangan menyentuh Mode Expert (`expert/`) kecuali tugas memintanya.
- Bahasa UI dan komentar kode: Indonesia, ikuti gaya yang sudah ada.
- Perbarui `docs/ATURAN_GAME.md` bila aturan game berubah.

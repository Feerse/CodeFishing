# TUGAS_HERMES.md — 6 tugas evaluasi CodeFishing

Cara kerja: kerjakan tugas BERIKUTNYA yang belum dicentang, satu per giliran. Setelah selesai:
1. jalankan tes/generator yang disebut di tugas itu,
2. commit (`git add -A && git commit -m "..."`),
3. centang tugas di bawah,
4. laporkan: file yang diubah, hasil tes, dan hal yang harus dites manual di emulator,
5. BERHENTI dan tunggu pengguna mengetik "lanjut".

Urutan dipilih dari yang paling kecil dan berisiko rendah ke yang paling besar.

## Status
- [x] **T6**: ❌ Hapus kompas dekoratif pojok kanan atas peta (arsip)
- [x] T1  Background Home mode gelap disamakan dengan mode terang
- [x] **T2**: Boss Mode Pemula - 10+ soal, acak urutan saat gagal
- [ ] T3  Transisi antar layar + menutupi proses booting
- [ ] T5  Peta Antartika (materi lanjutan), dikerjakan bertahap (5a → 5d)
- [ ] T4  Animasi pembuka (opsional, paling akhir)

---

## T6 — Hapus kompas pojok kanan atas peta
Temuan: kompas yang harus dihapus BUKAN tombol di PetaScreen. Itu kompas dekoratif yang tertanam di gambar SVG peta
(titik pusat `pusatKompas = [812, 175]` di `generate_peta_svg.js`, blok komentar `// Kompas dekoratif.`).
Kompas di kanan bawah (tombol "Geser ke soal terakhir", `Art.kompas` di PetaScreen.kt) HARUS TETAP ADA.

Langkah:
1. Di `2_Aset_dan_Konten/tools/generate_peta_svg.js`, hapus blok `add(...)` di bawah komentar `// Kompas dekoratif.` (grup `<g transform="translate(...)">` berisi lingkaran dan polygon).
2. Garis rhumb (garis tipis memancar dari `pusatKompas`) dibiarkan. Sebutkan di laporan agar pengguna bisa memutuskan apakah juga dihapus.
3. Regenerasi: `node 2_Aset_dan_Konten/tools/generate_peta_svg.js`, lalu `python3 2_Aset_dan_Konten/tools/generate_tema_terang.py`, lalu `python3 2_Aset_dan_Konten/tools/sync_assets.py`.
4. Verifikasi: `grep -c "translate(812 175)"` harus 0 pada KEEMPAT file: `assets-source/01_Peta_Perairan/peta_perairan.svg`, `peta_perairan_terang.svg`, `app/src/main/assets/peta/peta_perairan.svg`, `peta_perairan_terang.svg`. Pastikan `git diff --stat` hanya menyentuh generator dan 4 SVG itu.
5. Jika node/python tidak tersedia, hapus grup kompas langsung di keempat SVG (satu `<g transform="translate(812 175)">…</g>`) DAN di generator.

## T1 — Background Home mode gelap
Temuan: `frontend/screens/HomeScreen.kt` punya `if (warna.gelap) { Gambar(Art.latar_langit_malam…) + overlay gradien } else { gradien latarAtas→latarBawah + Art.ombak_pola alpha 0.22 }`.
Interpretasi (asumsi, sebutkan di laporan): mode gelap memakai KOMPOSISI latar yang sama dengan mode terang (gradien + pola ombak),
tetap dengan warna palet gelap supaya teks tetap terbaca. Jangan memaksa warna terang di tema gelap.

Langkah:
1. Hapus cabang `warna.gelap` pada latar Home sehingga selalu memakai cabang gradien + ombak.
2. Pastikan `warna.ombak` pada palet gelap (ui/theme/Color.kt) cukup terlihat di atas gradien gelap; bila terlalu samar, naikkan alpha khusus mode gelap (kecil saja).
3. Jangan hapus file `latar_langit_malam.png`. Jalankan `git grep -n latar_langit_malam` dan laporkan apakah masih dipakai layar lain.
4. Cek import yang jadi tidak terpakai di HomeScreen.kt (Brush, Color, ContentScale tetap dipakai oleh cabang lain?).

## T2 — Boss Mode Pemula: 10+ soal dan acak saat gagal
Temuan:
- Boss = stage 10. Saat ini hanya 5 soal, yang sebenarnya "dipinjam" dari stage 1,2,5,8,9: `generate_seed.js` baris ±211–255 memindahkan 5 soal boss lama ke stage lain, lalu menyusun komposisi stage 10 dari soal pertama stage 1,2,5,8,9. Total seed: 45 soal, 50 komposisi.
- `core/data/GameRepository.kt` `quiz()` memanggil `urutanSoal(rows, ulangi)`; `ulangi = percobaan > 0`; `percobaan` bertambah setiap `selesaikanStage` (lulus atau gagal). Jadi retry sudah diacak, tetapi percobaan pertama berurutan, dan tidak ada jaminan urutan berbeda dari sebelumnya.
- Ambang lulus boss 80% (hardcode `80` di GameRepository dan MateriScreen).
- Tertulis "5 soal" di: `MateriScreen.kt` ±baris 128, komentar `BossScreen.kt`, komentar `QuizEngine.kt`, `LevelRules.kt`, `docs/ATURAN_GAME.md`, `stage.json` (`jumlah_soal`).

Langkah:
1. Konten: boss punya minimal 10 soal pilihan ganda (4 opsi, tepat 1 benar; tes memeriksa ini), lebih sulit dari soal stage biasa: gabungan konsep stage 1–9, tebak keluaran, cari bug, telusuri nilai variabel, if di dalam while. Sumber: tambahkan ke `qs` stage boss di `2_Aset_dan_Konten/konten/stages.json` (format sama dengan soal lain; nilai `sc` dan `vn` harus yang sudah dipakai soal lain, cek dulu). Ambil boleh 5 soal boss lama + minimal 5 soal baru, atau 10 soal baru.
2. Generator: ubah `generate_seed.js` agar komposisi stage 10 berisi SEMUA soal boss (≥10). ID soal 1–45, ID pilihan, dan ID komposisi 46–50 yang sudah ada TIDAK boleh berubah (progres pemain dan sesi tersimpan merujuknya). Soal baru memakai ID mulai 46 (soal) dan lanjutan untuk pilihan dan komposisi (komposisi mulai 51). Setelah generator dijalankan, stage 1–9 harus identik dengan sebelumnya (`git diff` pada bagian itu kosong).
3. `jumlah_soal` stage 10 diperbarui sesuai jumlah soal boss. Ambang lulus 80% tetap (jadi 8 dari 10).
4. Acak: untuk stage boss, urutan soal diacak setiap sesi baru setelah ada percobaan gagal (dan boleh juga sejak percobaan pertama bila lebih sederhana; sebutkan pilihanmu). Tambahkan jaminan: urutan baru tidak sama persis dengan urutan sesi sebelumnya (acak ulang bila sama; boleh menambah parameter opsional di `urutanSoal`). Sesi yang di-resume (`SesiKuis`) tetap memakai urutan tersimpan.
5. Ganti semua teks/komentar hardcode "5 soal" untuk boss dengan jumlah sebenarnya (`stage.jumlah_soal` atau `soal.size`).
6. Naikkan `SEED_VERSION` (3 → 4) di `core/db/SeedLoader.kt`. Pastikan komposisi lama stage 10 tidak meninggalkan baris yatim (insert memakai REPLACE; periksa `SeedLoader`).
7. Tes: perbarui `SeedValidationTest` (jumlah soal, komposisi; stage 10 minimal 10, stage 1–9 tetap 5) dan `UrutanSoalTest` (tambah tes boss: acak saat gagal, urutan baru ≠ lama). Jalankan `./gradlew testDebugUnitTest --offline` bila bisa. Perbarui `docs/ATURAN_GAME.md`.
8. Catatan untuk laporan (jangan diubah tanpa izin): aturan "umpan berkurang bila ≥3 salah" dan ambang bonus combo 3/4/5 dibuat untuk 5 soal. Tunjukkan apakah masih masuk akal untuk 10 soal dan tanyakan pengguna.

## T3 — Transisi antar layar dan menutupi booting
Temuan:
- `frontend/App.kt` memilih layar dengan `when (saatIni.screen) { … }` tanpa animasi, jadi pindah layar terasa "kedip".
- Booting sebenarnya ada di `core/AppContainer.kt`: blok `init` menjalankan `runBlocking(Dispatchers.IO) { SeedLoader(...).seedIfNeeded() }` di thread utama saat `MainActivity.onCreate`. Ini menahan layar saat pertama kali jalan atau saat `SEED_VERSION` naik.
- PetaScreen memuat SVG di IO (ada fallback gradien), MateriScreen/KuisScreen memuat dari DB sehingga sempat kosong sesaat.

Langkah:
1. Transisi layar: bungkus isi `AppScaffold` (isi `when`) dengan `AnimatedContent`/`Crossfade` yang kuncinya layar aktif (plus `stage`/`misi` bila relevan). Gaya: fade-through singkat (±180–250 ms); maju = geser halus dari kanan, mundur/reset = fade. Jangan membuat state layar ter-reset (scroll Peta, draf Sandbox harus aman). Hormati padding dari scaffold.
2. Booting: pindahkan seed keluar dari thread utama. `AppContainer` dibuat cepat; seed dijalankan di coroutine dengan state `siap`. Selama belum siap tampilkan layar boot sederhana (latar sama dengan warna window, logo `Art.logo_codefishing`), lalu transisi halus ke `App()`. `App()` dan semua yang membaca DB baru boleh dikomposisi setelah `siap = true`. Pertahankan `window.setBackgroundDrawable(...)` di MainActivity agar tidak ada kilatan gelap.
3. Jangan menambah `delay` buatan untuk "menutupi" loading. Tampilkan layar boot hanya selama seed berjalan; durasi minimum untuk animasi baru diputuskan di T4.
4. Pastikan `BackHandler`, `nav.resetTo`, dan bottom nav tetap berfungsi. Tidak ada library baru.
5. Verifikasi: compile; jelaskan skenario tes manual (instal bersih, update dari versi lama dengan SEED_VERSION lebih tinggi, ganti tema saat di layar tertentu, tombol Back).

## T5 — Peta Antartika (materi lebih advance), bertahap
Temuan:
- Antartika saat ini hanya "Gerbang Antartika, segera hadir" (PetaScreen.kt) dan dekorasi es di bagian bawah SVG (dunia peta 1000 x 2800). `PetaGeometri.stage` berisi posisi tetap 10 stage; `JUMLAH_STAGE = 10`; ada teks "$jumlahSelesai/10", "Samudra Pasifik, 10 stage", dan aturan ikan Arwana/Hiu Merah yang mengacu "seluruh 10 stage".
- Generator seed hardcode 10 stage (`ZONA`, `materiRows = slice(0, 9)`, `trik_nelayan`, boss).
- Stage baru tanpa aset ikan: 33 ikan adalah placeholder gambar tanda tanya. Periksa apakah `ikan_target_id = null` aman di HasilStage; kalau tidak, pakai ikan placeholder dengan label "Segera hadir".
- `mulaiStage` di GameRepository membuat baris `progres_stage` secara lazy. Pastikan pemain lama (yang sudah punya data) bisa membuka stage 11+ setelah menamatkan stage 10, tanpa crash dan tanpa terkunci selamanya.

Draf topik lanjutan (urut dari dasar sebelumnya: variabel, tipe data, operator, if, while):
11 Perulangan for dan range · 12 List dasar (indeks, append) · 13 Loop di atas list · 14 Dictionary · 15 String method dan f-string · 16 Fungsi (def, return) · 17 Parameter dan nilai default · 18 Error dan try/except · 19 Gabungan list + fungsi · 20 Boss Ujian Antartika.
Dikerjakan "dicicil": hanya stage 11–13 pada giliran awal.

Sub-tahap (satu sub-tahap per giliran, berhenti di tiap sub-tahap):
- 5a DESAIN SAJA, tanpa mengubah kode: tulis `docs/RENCANA_ANTARTIKA.md` (maks 1 halaman) berisi daftar topik final stage 11–13, pilihan tampilan peta (opsi A: perpanjang SVG Pasifik; opsi B, lebih disarankan: peta kedua `peta_antartika` dengan geometri sendiri + penanda wilayah di PetaScreen), cara membuka Antartika (terbuka setelah stage 1–10 selesai), dampaknya pada `JUMLAH_STAGE`/skor/ikan, dan risiko pemain lama. BERHENTI dan minta persetujuan.
- 5b KONTEN: tambah stage 11–13 lewat sumber + generator (materi, 5 soal per stage dengan format yang sama, komposisi, zona baru misalnya "Polar"; ID lanjutan, SEED_VERSION naik). Tes seed diperbarui. Belum menyentuh UI.
- 5c LOGIKA: pembukaan Antartika setelah stage 10 selesai, perhitungan progres, aturan ikan, dan penanganan `progres_stage` pemain lama. Tes unit.
- 5d UI PETA: tampilan peta Antartika dan node stage 11–13, gerbang Antartika yang bisa dibuka. Perbarui teks "segera hadir" yang tidak berlaku lagi.

## T4 — Animasi pembuka (OPSIONAL, paling akhir)
Usulan animasi dari pengguna TIDAK ada di repo. Sebelum membuat apa pun, tanyakan SATU pertanyaan: "Jelaskan animasi awal yang kamu usulkan (atau tempel deskripsinya)."
Deskripsi pengguna (isi di sini bila sudah ada): [BELUM DIISI]

Jika pengguna menjawab "bebas": logo CodeFishing + riak/gelembung dengan aset yang sudah ada (`Art.logo_codefishing`, `Art.gelembung`, `Art.ombak_pola`), maksimal ±2 detik, bisa dilewati dengan ketukan.
Pasang di layar boot dari T3 (bukan layar baru). Gunakan animasi Compose (`rememberInfiniteTransition`/`animate*AsState`), tanpa library baru, tanpa aset baru. Hormati tema terang/gelap.

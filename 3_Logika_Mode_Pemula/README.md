# 3. Logika Mode Pemula (Mode Belajar)

Kode: `app/src/main/java/com/example/codefishing/pemula/`

- `QuizEngine.kt`: model soal/pilihan dan sesi 5 soal (benar, combo, XP, lulus).
- `BaitRules.kt`: umpan maks 5, pulih 1 per 15 menit, salah −1, perhitungan sisa waktu.
- `LevelRules.kt`: level = XP/100 + 1, rank, progres level, bobot ikan, tier, bonus combo 3/4/5.
- `BaitStore.kt` / `QuizSession.kt`: pembungkus lama; aplikasi utama memakai `GameRepository`.
- `Kelangkaan.kt`: label/warna kelangkaan.

Aturan lengkap: `docs/ATURAN_GAME.md`. Tes: `app/src/test/.../ScoringTest.kt`, `BaitRulesTest`, `QuizSessionTest`.

Status: aturan sudah terhubung ke Room melalui `core/data/GameRepository`: progres stage, riwayat jawaban, XP, umpan, koleksi, bonus combo, Arwana, Hiu Merah, dan tantangan harian.

Sumber soal kanonik ada di `2_Aset_dan_Konten/konten/seed/soal.json`; komposisi 5 soal per stage di `komposisi_kuis.json`.

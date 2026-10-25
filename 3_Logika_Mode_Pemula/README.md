# 3. Logika Mode Pemula (Mode Belajar)

Kode: `app/src/main/java/com/example/codefishing/pemula/`
- `QuizSession`: satu sesi kuis (jawab, persen, lulus, beruntun)
- `BaitRules`, `BaitStore`: umpan (maks 5, pulih 1 per 15 menit, salah -1)
- `Kelangkaan`: aturan kelangkaan ikan

Aturan lengkap: `docs/ATURAN_GAME.md`. Tes: `app/src/test/.../BaitRulesTest`, `QuizSessionTest`.
Tugas: lengkapi sesi kuis, simpan stage terbuka dan persen terbaik ke `ProgressRepository`, beri ikan sesuai kelangkaan. Tulis tanpa Compose supaya bisa dites.
Isi soal ada di `2_Aset_dan_Konten/konten/stages.json` (minta tim konten bila perlu diubah).

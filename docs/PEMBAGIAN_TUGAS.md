# Pembagian tugas

Lima peran. Nama pemegang adalah usulan dari percakapan tim, ubah bila berbeda.

| Folder tim | Pemegang | Tanggung jawab |
|---|---|---|
| `1_Frontend_Compose` | Fahmi | Layar Compose (`frontend/screens`), toolbar hamburger dan drawer, komponen, rangkaian navigasi, repo GitHub |
| `2_Aset_dan_Konten` | Alfian | Gambar, audio, logo, JSON konten (soal stage, misi, ikan), skrip sinkron |
| `3_Logika_Mode_Pemula` | Melfyn | Logika kuis, umpan, kelulusan stage, kelangkaan ikan (`pemula/`) |
| `4_Logika_Mode_Expert` | Alief | Dunia sandbox, penjalan kode Python, bintang (`expert/`). Alief juga mengelola semuanya, termasuk `core/` |
| `5_Desainer_UIUX` | Afwan | Prototipe dan desain (Figma/HTML), palet dan font, `ui/theme/` |

## Hubungan antar peran

- Desainer menetapkan tampilan, Frontend membuatnya di Compose memakai `ui/theme` dan aset.
- Frontend memanggil kelas di `pemula/` dan `expert/`. Layar Kuis memakai `QuizSession`, layar Sandbox memakai `CodeRunner`.
- Soal, misi, dan data ikan hidup di JSON `2_Aset_dan_Konten/konten/`, dibaca lewat `core/content`.

## Tugas awal

- **Frontend**: ganti `LayarSementara` di tiap layar dengan UI asli (daftar layar di `docs/NAVIGASI.md`).
- **Aset dan Konten**: cek 196 gambar lewat Galeri Aset (Pengaturan, build debug), ganti yang kurang, tambah audio baru.
- **Pemula**: lengkapi `QuizSession`, simpan hasil ke `ProgressRepository`, aturan ikan Epic/Rare/Legend/Mythic.
- **Expert**: tulis interpreter di `CodeRunner` (`expert/SandboxModel.kt`).
- **Desainer**: pasang font (Bricolage Grotesque, Inter, JetBrains Mono) dan rapikan `ui/theme`.

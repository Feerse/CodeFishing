# Pembagian tugas

Lima peran. Nama pemegang adalah usulan dari percakapan tim, ubah bila berbeda.

| Folder tim | Pemegang | Tanggung jawab |
|---|---|---|
| `1_Frontend_Compose` | Fahmi | Layar Compose (`frontend/screens`), toolbar hamburger + drawer + bottom nav, komponen, rangkaian navigasi, repo GitHub |
| `2_Aset_dan_Konten` | Alfian | Gambar, audio, logo, JSON konten seed (stage, soal, misi, ikan), generator `tools/generate_seed.js` |
| `3_Logika_Mode_Pemula` | Melfyn | Aturan kuis, umpan, kelulusan stage, kelangkaan/bonus ikan (`pemula/`) |
| `4_Logika_Mode_Expert` | Alief | Dunia sandbox, lexer/parser/interpreter Python subset, evaluasi misi, bintang (`expert/`). Alief juga mengelola `core/` |
| `5_Desainer_UIUX` | Afwan | Prototipe dan desain (Figma/HTML), palet dan font, `ui/theme/` |

## Hubungan antar peran

- Desainer menetapkan tampilan, Frontend membuatnya di Compose memakai `ui/theme` dan aset.
- Frontend memanggil fasad `core/data/GameRepository` untuk semua data Room, `pemula/` untuk aturan kuis/ikan, dan `expert/SandboxEngine` untuk menjalankan kode.
- Sumber konten kanonik ada di `2_Aset_dan_Konten/konten/seed/`; generator menyalinnya ke `app/src/main/assets/seed/`.
- Skema fisik 16 tabel + 2 view mengikuti `CodeFishing_DFD_ERD.md` dan berada di `core/db/`.

## Status implementasi

- **Frontend**: seluruh 14 layar sudah memakai UI asli; `LayarSementara` tidak dipakai lagi. Drawer 5 tujuan dan bottom nav 4 tab sudah aktif.
- **Aset dan Konten**: seed berisi 10 stage, 9 materi, 45 soal, 50 komposisi, 48 ikan (15 ilustrasi + 33 placeholder), dan 7 misi. Galeri Aset tetap tersedia di build debug.
- **Pemula**: `QuizEngine`, `BaitRules`, `LevelRules`, `FishRules`, dan `GameRepository` menyimpan progres ke Room. Combo bonus memakai ambang 3/4/5 karena satu stage 5 soal.
- **Expert**: interpreter Python subset (variabel, ekspresi, if/elif/else, while, for/range, def/return, print) beserta dunia, tick, trace, evaluasi misi, dan bintang 1,5× par.
- **Desainer**: tema dan aset tetap dipertahankan; teks UI mengikuti palet laut yang sudah ada.

## Pekerjaan lanjutan yang disarankan

- Menambah ilustrasi 33 ikan placeholder begitu aset dari tim Material Collecting tersedia.
- Menambah migrasi eksplisit bila nanti ada data pengguna versi rilis sebelumnya.
- Menjalankan uji di perangkat fisik/emulator untuk memeriksa suara, getar, dan tata letak berbagai ukuran layar.

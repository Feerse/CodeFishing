# 2. Aset dan Konten

Sumber kebenaran untuk semua gambar, audio, logo, dan data konten.

## Aset gambar/audio

Setelah mengubah aset, jalankan dari root repo:

    python3 2_Aset_dan_Konten/tools/sync_assets.py

Skrip menyalin ke `app/src/main/res` (gambar, audio), menyalin `peta_perairan.svg` ke `app/src/main/assets/peta`, membuat ulang `core/art/Art.kt`, dan menolak nama tidak valid (huruf kecil, angka, underscore, diawali huruf).

Foreground launcher memakai `assets-source/10_Pendukung_UI/ic_logo_fg_launcher.png` (logo sudah diberi padding, konten ±37% kanvas) dan disalin sebagai `app/src/main/res/drawable-xxxhdpi/ic_logo_fg.png`. Jangan memindahkannya ke `drawable-nodpi`: berkas 432 px di `nodpi` dianggap 432 dp sehingga ikon launcher terzoom dan terpotong.

Peta perairan vertikal dibuat ulang dengan:

    node 2_Aset_dan_Konten/tools/generate_peta_svg.js

Skrip tersebut menulis SVG sumber di `assets-source/01_Peta_Perairan/peta_perairan.svg` sekaligus salinan yang dibaca aplikasi di `app/src/main/assets/peta/peta_perairan.svg`.

| Folder | Isi |
|---|---|
| `assets-source/NN_Fitur/` | 197 PNG transparan (10 folder fitur + logo) + `peta_perairan.svg` + `ic_logo_fg_launcher.png`. Pakai PNG di kode: `Art.nama_file` |
| `audio/` | 13 audio: 2 musik latar (`bgm_umum` di semua layar, `bgm_aquarium` hanya di Akuarium) dan 11 efek suara |
| `logo/` | Logo resmi: SVG (sumber) dan PNG transparan |
| `tools/` | `sync_assets.py`, `generate_seed.js`, `generate_peta_svg.js` |

## Konten seed (Room)

Sumber kanonik: `konten/seed/*.json`.

    node 2_Aset_dan_Konten/tools/generate_seed.js

Generator membaca konten prototipe di `app/src/main/assets/content/`, lalu menulis:
- `2_Aset_dan_Konten/konten/seed/` — salinan untuk tim konten.
- `app/src/main/assets/seed/` — yang dibaca `SeedLoader` saat aplikasi pertama dibuka.

Isi seed: 10 stage, 9 materi, 45 soal (5 per stage), 50 komposisi kuis, 48 ikan (15 berilustrasi + 33 placeholder), dan 7 misi Sandbox. Setelah mengubah seed, naikkan `SeedLoader.SEED_VERSION` di `core/db/SeedLoader.kt` agar konten diperbarui saat aplikasi dibuka kembali.

15 ikan berilustrasi memakai aset `ikan_teri` … `ikan_hiu_merah`; 33 sisanya memakai `tanda_tanya_ikan` dengan label “Segera hadir — menunggu aset”.

## Audio

| Nama | Dipakai untuk (`Sfx` di `core/audio/AudioManager.kt`) |
|---|---|
| `bgm_umum` | Musik latar semua layar kecuali Akuarium, mengikuti saklar Musik di Pengaturan |
| `bgm_aquarium` | Musik latar khusus layar Akuarium (loop 30 detik) |
| `sfx_klik_1` / `sfx_klik_2` | Klik biasa / hamburger, drawer, tombol utama (`sfx_klik_1` kini versi final UI_Click1.mp3, bukan .ogg kasar) |
| `sfx_benar` / `sfx_salah_1` / `sfx_salah_2` | Jawaban benar / salah / stage gagal |
| `sfx_error` | Kode Sandbox error |
| `sfx_cipratan` | Lempar kail |
| `sfx_sukses_4` / `_2` / `_1` / `_3` | Ikan baru / misi selesai / stage lulus / 3 bintang dan Boss |

Pemetaan ke peristiwa adalah usulan awal; ubah di enum `Sfx`.

Lihat semua gambar di app: build debug, Pengaturan, Galeri Aset.

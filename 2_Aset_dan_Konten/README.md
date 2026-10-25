# 2. Aset dan Konten

Sumber kebenaran untuk semua gambar, audio, logo, dan data konten. Setelah mengubah apa pun di sini, jalankan dari root repo:

    python3 2_Aset_dan_Konten/tools/sync_assets.py

Skrip menyalin ke `app/src/main/res` (gambar, audio) dan `app/src/main/assets/content` (JSON), serta membuat ulang `core/art/Art.kt`. Skrip menolak nama tidak valid (harus huruf kecil, angka, underscore, diawali huruf).

| Folder | Isi |
|---|---|
| `assets-source/NN_Fitur/` | 197 PNG transparan (10 folder fitur + logo). Pakai di kode: `Art.nama_file` |
| `audio/` | 12 audio: `bgm_aquarium` (loop 30 dtk, diputar di semua layar) dan 11 efek suara |
| `konten/` | `stages.json` (soal 10 stage), `misi.json` (7 misi Sandbox), `ikan_config.json` (15 ikan, kelangkaan, aturan umpan) |
| `logo/` | Logo resmi: SVG (sumber) dan PNG transparan |
| `tools/` | `sync_assets.py` |

## Audio

| Nama | Dipakai untuk (`Sfx` di `core/audio/AudioManager.kt`) |
|---|---|
| `bgm_aquarium` | Musik latar semua layar, mengikuti saklar Musik di Pengaturan |
| `sfx_klik_1` / `sfx_klik_2` | Klik biasa / hamburger, drawer, tombol utama |
| `sfx_benar` / `sfx_salah_1` / `sfx_salah_2` | Jawaban benar / salah / stage gagal |
| `sfx_error` | Kode Sandbox error |
| `sfx_cipratan` | Lempar kail |
| `sfx_sukses_4` / `_2` / `_1` / `_3` | Ikan baru / misi selesai / stage lulus / 3 bintang dan Boss |

Pemetaan ke peristiwa adalah usulan awal; ubah di enum `Sfx`.

Lihat semua gambar di app: build debug, Pengaturan, Galeri Aset.

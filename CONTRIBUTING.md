# Cara berkontribusi

1. `git pull` dulu, lalu buat cabang: `git checkout -b fitur/<peran>-<topik>` (contoh `fitur/pemula-kuis`).
2. Kerja **hanya di folder perannya** (lihat README dan `docs/PEMBAGIAN_TUGAS.md`). Perlu menyentuh folder lain? Kabari pemiliknya.
3. Commit kecil, pesan singkat Bahasa Indonesia. Push cabang, buka Pull Request ke `main`. Alief yang merge.
4. Jangan push langsung ke `main`.

Aturan penting:
- Gambar, audio, dan JSON konten diubah di `2_Aset_dan_Konten/`, lalu jalankan `python3 2_Aset_dan_Konten/tools/sync_assets.py`. Jangan menaruh berkas langsung di `app/src/main/res`.
- `core/art/Art.kt` dibuat otomatis oleh skrip, jangan diedit tangan.
- Logika murni (kuis, umpan, bintang) tanpa Compose/Android supaya bisa dites di `app/src/test`.
- Tidak ada tombol Kembali di toolbar (keputusan dosen). Jangan tambah library tanpa izin Alief.

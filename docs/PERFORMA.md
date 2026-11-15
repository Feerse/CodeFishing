# Performa (frame drop, boot peta, memori)

Target uji: Samsung Galaxy A23 5G (Snapdragon 695, 8 GB, Android 14).

## Penyebab yang ditemukan

| # | Penyebab | Dampak | Perbaikan |
|---|---|---|---|
| 1 | `painterResource` mendekode PNG di **thread utama** setiap layar dibuka dan tidak menyimpan hasilnya | Tiap pindah menu mendekode ulang puluhan gambar → frame drop | `CacheGambar` (LRU berbatas memori) dipakai `Gambar`; aset yang pasti dipakai dipanaskan di thread latar saat layar pembuka |
| 2 | PNG kecil di disk tapi **mekar saat didekode**: ikan 640 px yang tampil 44–150 dp didekode penuh (grid Koleksi 58 ikan ≈ 61 MB) | Memori dan waktu dekode berlebih | Aset kecil-tampil dibatasi 448 px; thumbnail ikan 256 px (`*_kecil.png`) untuk grid/daftar (grid ≈ 10 MB). Latar penuh (adegan, latar) tidak diubah |
| 3 | **Peta dirender dari SVG lewat CPU** (AndroidSVG, bitmap 1000×2800) setiap layar peta dibuka | Boot menu peta lama (beberapa detik) dan bersaing dengan komposisi | Peta dirender **sekali** saat pembuatan aset ke `peta_*.webp` (±65 KB), aplikasi hanya mendekode (puluhan ms) dan menyimpan dua peta terakhir; peta lawan dimuat lebih dulu di latar |
| 4 | `MediaPlayer.create` sinkron di thread utama (buka aplikasi, masuk/keluar Akuarium) | Hitch saat ganti trek musik | Semua operasi musik latar di satu thread kerja |
| 5 | Transisi layar bawaan `AnimatedContent`: dua layar disusun sekaligus, 300 ms + animasi ukuran pegas | Jendela patah-patah lebih panjang | Fade 150/90 ms tanpa `SizeTransform` |
| 6 | Efek kelangkaan: jam animasi per kartu 60 fps, kanvas digambar tiap frame saat scroll; di tema terang 10 lapis gambar per ikan | Scroll Koleksi berat | Jam dibulatkan ±29 fps, efek dibekukan saat grid digulir, kontur tema terang dihitung sekali (`CacheGambar.buatKontur`) |
| 7 | Adegan memancing menyusun ulang seluruh adegan 60×/detik karena jam ombak dibaca saat komposisi | Beban komposisi tetap tinggi di Kuis | Jam ombak dibulatkan ±30 fps; napas dan kilat dibaca di fase gambar |
| 8 | **Menguji di build debug**: debuggable, tanpa profil optimasi, ART memakai JIT | Debug bisa beberapa kali lebih lambat dari release | Varian `release` bisa dipasang langsung (kunci debug) + `profileinstaller` |

## Cara mengukur dengan benar
1. Android Studio → **Build Variants** → pilih `release` → Run. (Jangan menilai patah-patah dari varian `debug`.)
2. Biarkan aplikasi dibuka sekali ±1 menit agar ART mengompilasi dengan profil, lalu ukur.
3. Developer options → **Profile HWUI rendering → On screen as bars**, atau `adb shell dumpsys gfxinfo com.example.codefishing framestats`.
4. Perhatikan jank saat: buka Peta pertama kali, pindah peta, buka Koleksi lalu scroll, masuk Kuis.

## Aturan saat menambah aset
- Ikan/ikon/alat yang tampil kecil: sisi terpanjang **≤ 448 px**; ikan wajib punya `*_kecil.png` (256 px). Jalankan `python3 2_Aset_dan_Konten/tools/optimalkan_aset.py` lalu `sync_assets.py`; ia melakukannya otomatis dan menyimpan aslinya di `tools/aset_resolusi_penuh/`.
- Peta baru: buat SVG di `assets-source/01_Peta_Perairan/`, jalankan `optimalkan_aset.py` (merender WebP). Jangan memuat SVG di runtime.
- Tampilkan gambar lewat `Gambar(...)` / `GambarKontras(...)` (memakai cache), bukan `painterResource` langsung.
- Gunakan `gambarIkanKecil(...)` untuk tampilan ≤ ±90 dp dan `gambarIkan(...)` hanya untuk tampilan besar.
- Animasi dekoratif yang berulang: baca state di fase gambar/layer (`Canvas {}`, `graphicsLayer {}`, `offset {}`), dan bekukan saat scroll.

`AsetPerformaTest` menjaga batas ukuran aset dan keberadaan peta WebP.

## Belum dilakukan (opsi lanjutan)
- Mengaktifkan R8 (`optimization { enable = true }`) + aturan keep; belum diuji sehingga sengaja dimatikan.
- Baseline profile khusus aplikasi lewat modul Macrobenchmark (butuh perangkat untuk menghasilkannya).
- Menghapus rekomposisi per-frame di `MemancingScene` sepenuhnya (butuh restrukturisasi adegan).

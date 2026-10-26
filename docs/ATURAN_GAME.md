# Aturan game CodeFishing

Dokumen ini adalah acuan perilaku yang sudah diterapkan di kode Room + Compose.
Semua permainan berjalan **offline** di perangkat; aplikasi tidak meminta izin internet.

## Mode Belajar (tim Logika Mode Pemula)

- 10 stage di Samudra Pasifik (Stage 10 = Boss). Perairan lain berstatus **Segera**.
- Tiap stage: Materi (satu kalimat, contoh kode, keluaran) lalu Kuis 5 soal pilihan ganda.
- **Umpan (nyawa)**: maksimum 5, pulih 1 umpan tiap 15 menit, jawaban salah mengurangi 1 umpan. Umpan habis membuka layar 11 (hitung mundur) sampai umpan >= 1.
- **Lulus stage**: skor benar/5 × 100 ≥ 70 untuk stage biasa. **Boss Stage 10 lulus di 4 dari 5 (80%)**.
- Lulus membuka stage berikutnya. Skor terbaik, combo tertinggi, dan percobaan disimpan di `progres_stage`.
- Jawaban benar memberi **+25 XP** dan menaikkan combo; jawaban salah mereset combo ke 0.
- Setiap stage memakai studi kasus (kapal, dermaga, selam, jala, es, bubu, trawler/boss).

### Ikan dan kelangkaan

Katalog berisi **48 ikan**: 15 ikan berilustrasi yang bisa didapatkan dan 33 ikan placeholder dengan label **“Segera hadir — menunggu aset”**.

| Kelangkaan | Warna | Cara dapat |
|---|---|---|
| Biasa | abu-abu | Ikan target Stage 1-7 |
| Langka | biru | Jawaban benar beruntun 3 (Kerapu), 4 (Barakuda), 5 (Tenggiri) |
| Epik | ungu | Ikan target Stage 8-9 |
| Legenda | emas | Gabus Stage 10; Arwana setelah seluruh stage 1-10 lulus |
| Mitos | merah | Gabus saat boss sempurna (naik satu tier); Hiu Merah setelah seluruh 10 stage sempurna (skor 100) |

Urutan 15 ikan berilustrasi: Teri, Bandeng, Bawal, Mujair, Nila, Lele, Patin, Gurame, Sepat, Gabus, Kerapu, Barakuda, Tenggiri, Arwana, Hiu Merah.
Karena satu stage hanya 5 soal, ambang combo bonus adalah 3/4/5 dan semua bonus yang tercapai diberikan pada hasil stage.

Ikan yang sama boleh ditangkap berulang; jumlah, bobot terbesar, tier terbaik, dan level/kenyang disimpan di `koleksi_ikan`.

### Tantangan harian

- Satu target ikan per tanggal, dipilih dari ikan yang stage-nya sudah selesai (fallback Stage 1).
- Target dianggap selesai saat ikan tersebut **ditambahkan ke koleksi pada hari itu** (lewat stage atau Sandbox).
- Hadiah **1 umpan emas** harus diklaim manual dari Beranda.
- Umpan emas juga bisa ditukar menjadi 1 umpan biasa saat umpan belum penuh.

## Mode Expert / Sandbox (tim Logika Mode Expert)

Pemain menulis subset Python lalu menjalankan misi secara offline. Kapal bergerak pada petak laut sesuai kode; setiap eksekusi disimpan di `hasil_program` dan `hasil_tangkapan`.

- Biaya waktu (ticks): `gerak(arah)` = **1**, `lempar()` = **1**, `lihat()` = **0**. `gerak` yang menabrak tepi tetap dihitung 1 tick.
- **Bintang** (par = waktu acuan): ticks ≤ par → 3 bintang; ticks ≤ ceil(par × 1,5) → 2 bintang; selain itu 1 bintang. Misi tanpa par: 3 bintang bila berhasil.
- **Papan misi di atas editor**: sebelum program dijalankan, papan menampilkan posisi awal kapal, ikan, dan sampah; setelah dijalankan, papan menampilkan posisi akhir kapal beserta status misi. Pemain tidak perlu menggulir ke bawah untuk melihat hasil gerak kapal.
- **Hasil ditampilkan inline di editor**: ringkasan misi, output, dan jejak gerak muncul di bawah tombol Jalankan. Pemain tidak dipindahkan ke layar hasil, sehingga bisa langsung mengubah kode dan mencoba lagi.
- **XP Sandbox diberikan per ekor** sesuai `nilai_xp` ikan yang berhasil diangkat. XP misi (`xp_reward`) hanya diberikan saat misi pertama kali sukses. Ikan yang sama tetap memberi XP saat ditangkap lagi.
- Draf kode per misi disimpan selama aplikasi berjalan, sehingga kode tidak kembali ke contoh saat berpindah misi/layar.
- Misi gagal/error menampilkan penjelasan. Misi berhasil menampilkan waktu, ikan, sampah (jika ada), keluaran `print`, dan jejak gerak.
- Ikan yang tertangkap di Sandbox masuk ke Akuarium/Koleksi; hasil gagal tetap dicatat sebagai riwayat.
- Pada **m5**, mengangkat sampah juga menggagalkan misi, bukan hanya menangkap ikan lain.
- Batas aman eksekusi adalah 1000 tick dan 10.000 langkah.

| Id | Misi | Grid | Tujuan | Par |
|---|---|---|---|---|
| m1 | Lempar pertama | 3x3 | Tangkap 1 ikan | 2 |
| m2 | Sebaris ikan | 5x5 | Tangkap 5 ikan di baris bawah | 14 |
| m3 | Sapu seluruh petak | 4x4 | Tangkap semua ikan (16) | 48 |
| m4 | Pilih-pilih | 5x5 | Tangkap semua nila, jangan angkat sampah | 36 |
| m5 | Berburu gurame | 6x6 | Tangkap semua gurame, jangan yang lain | 40 |
| m6 | Fungsi panen | 4x4 | Tangkap semua ikan memakai `def` | 36 |
| bebas | Bebas | 6x6 | Bebas: tangkap sesukamu untuk mengisi akuarium | tanpa par |

Tata letak petak ada di `2_Aset_dan_Konten/konten/seed/misi.json` (field `cells`), dan disalin ke `app/src/main/assets/seed/misi.json` oleh generator.

## Progres, reset, dan penyimpanan

- Data permainan (16 tabel + 2 view ERD) disimpan di Room `codefishing.db`.
- Onboarding dan preferensi audio tetap memakai SharedPreferences agar tema/pengenalan tidak ikut terhapus saat reset.
- Reset progres menghapus progres stage, koleksi, hasil program, tantangan harian, dan riwayat jawaban; pengaturan tetap disimpan.
- Seed konten dimuat sekali dari `app/src/main/assets/seed/`. Naikkan `SeedLoader.SEED_VERSION` bila konten berubah.

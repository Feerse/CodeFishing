# Aturan game (dari prototipe HTML)

Angka ini sudah dipakai di `2_Aset_dan_Konten/konten/ikan_config.json` dan di kode `pemula/`. Ubah di satu tempat saja.

## Mode Belajar (tim Logika Mode Pemula)

- 10 stage di Samudra Pasifik (Stage 10 = Boss). Perairan lain (Antartika, Atlantik) berstatus **Segera**.
- Tiap stage: Materi (satu kalimat, contoh kode, keluaran) lalu Kuis (pilih umpan kode untuk mengisi bagian kosong).
- **Umpan (nyawa)**: maksimum 5, pulih 1 umpan tiap 15 menit, jawaban salah mengurangi 1 umpan. Umpan habis membuka layar 11 (hitung mundur) sampai umpan >= 1.
- **Lulus stage**: persen jawaban benar >= 70%. Lulus membuka stage berikutnya.
- Tiap soal memakai studi kasus bergambar (kapal, dermaga, selam, jala, es, bubu, trawler/boss), disimpan di field `vn` pada `stages.json`.

### Ikan dan kelangkaan

| Kelangkaan | Warna | Cara dapat |
|---|---|---|
| Common | abu-abu | Ikan tiap Stage 1-9 |
| Rare | biru | Jawaban benar beruntun 5 (Kerapu), 10 (Barakuda), 15 (Tenggiri) |
| Epic | ungu | Stage yang sama diselesaikan tanpa kesalahan (naik dari Common) |
| Legend | emas | Arwana: seluruh stage 1-10 lulus (tafsiran prototipe) |
| Mythic | merah | Hiu Merah: seluruh stage lulus tanpa salah (tafsiran prototipe) |

Urutan 15 ikan koleksi: Teri, Bandeng, Bawal, Mujair, Nila, Lele, Patin, Gurame, Sepat, Gabus, Kerapu, Barakuda, Tenggiri, Arwana, Hiu Merah.

> Aturan Legend/Mythic adalah tafsiran di prototipe dan perlu dikonfirmasi tim.

## Mode Expert / Sandbox (tim Logika Mode Expert)

Pemain menyelesaikan studi kasus dengan kode Python bebas, boleh memakai fungsi pintas yang disediakan. Kapal bergerak otomatis di petak laut sesuai kode.

- Biaya waktu (ticks): `gerak(arah)` = 1, `lempar()` = 2, `lihat()` = 0.
- **Bintang** (par = waktu acuan): ticks <= par → 3 bintang; ticks <= ceil(par x 1,4) → 2 bintang; selain itu 1 bintang. Misi tanpa par: 3 bintang bila berhasil.
- Hasil gagal/error menampilkan penjelasan. Hasil berhasil menampilkan waktu, ikan, sampah, dan keluaran print.

| Id | Misi | Grid | Tujuan | Par |
|---|---|---|---|---|
| m1 | Lempar pertama | 3x3 | Tangkap 1 ikan | 2 |
| m2 | Sebaris ikan | 5x5 | Tangkap 5 ikan di baris bawah | 14 |
| m3 | Sapu seluruh petak | 4x4 | Tangkap semua ikan (16) | - |
| m4 | Pilih-pilih | 5x5 | Tangkap semua nila, jangan angkat sampah | - |
| m5 | Berburu gurame | 6x6 | Tangkap semua gurame, abaikan yang lain | - |
| m6 | Fungsi panen | 4x4 | Tangkap semua ikan memakai fungsi buatanmu | - |
| bebas | Bebas | 6x6 | Bebas: tangkap sesukamu untuk mengisi akuarium | - |

Tata letak petak ada di `2_Aset_dan_Konten/konten/misi.json` (field `cells`).

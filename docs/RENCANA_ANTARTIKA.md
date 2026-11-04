# Rencana Antartika - Stage 11–13

## Topik Materi

**Stage 11: Perulangan for dan range**
- Sintaks `for i in range(n)`
- `range(start, stop)` dan `range(start, stop, step)`
- Perbedaan for vs while
- 5 soal: hitung output loop, trace nilai i, prediksi jumlah iterasi, loop mundur, loop step 2

**Stage 12: List dasar (indeks, append)**
- Deklarasi list `ikan = ["tuna", "salmon"]`
- Akses elemen `ikan[0]`, `ikan[-1]`
- Method `append()`, `len()`
- 5 soal: akses indeks, IndexError, append hasil, trace list setelah operasi, gabungan list + loop sederhana

**Stage 13: Loop di atas list**
- `for item in list`
- `for i in range(len(list))`
- Update elemen list dalam loop
- 5 soal: iterasi list, hitung elemen kondisional, filter list, trace perubahan list, gabungan for + if + list

## Tampilan Peta

**Opsi B (DISARANKAN): Peta kedua terpisah**

Alasan:
- SVG Pasifik sudah 2800 px tinggi, perpanjang ke 4200+ px akan bikin scroll panjang, user bingung posisi
- Antartika konsep berbeda (materi lanjutan, ikon/tema berbeda)
- Pemisahan jelas: Pasifik = dasar (stage 1-10), Antartika = lanjutan (stage 11+)

Implementasi:
- Buat `Art.peta_antartika` (SVG baru, 1000x1500 px, tema es/salju)
- `PetaGeometri` tambah `stageAntartika: Map<Int, Offset>` untuk posisi stage 11-13
- `PetaScreen` state `petaAktif: PetaPasifik | PetaAntartika`, toggle via tombol/gerbang
- Gerbang Antartika di PetaScreen jadi tombol buka peta kedua (bukan dekorasi)

## Cara Membuka Antartika

**Syarat**: Stage 1–10 selesai (progres_stage.selesai = true untuk stage_id 1..10)

**UI**:
- Gerbang Antartika di peta Pasifik: terkunci (abu-abu + gembok) sampai 10 stage selesai
- Setelah stage 10 selesai: gerbang terbuka (hijau + animasi berkilau), tap → pindah ke peta Antartika
- Peta Antartika: tombol "Kembali ke Pasifik" di pojok

**Repository**:
- `GameRepository.petaAntartikaTerbuka(): Boolean` cek 10 stage selesai
- HasilStageScreen: setelah stage 10 selesai, tampilkan dialog "Antartika terbuka!"

## Dampak JUMLAH_STAGE / Skor / Ikan

**JUMLAH_STAGE**: 10 → 13 (bertahap, nanti 10 → 20 di fase berikutnya)

**Teks UI yang berubah**:
- `PetaScreen`: "Samudra Pasifik, 10 stage" → "Samudra Pasifik, 10 stage"
- Tambah: "Antartika, 3 stage" (atau "X/3 stage selesai")
- Progress indicator: "10/10" → "13/13" (atau split: Pasifik 10/10, Antartika 3/3)

**Ikan**:
- Stage 11-13: `ikan_target_id = null` (placeholder, aman di HasilStage cek sudah ada)
- Atau: gunakan 3 ikan placeholder baru (misal: Penguin, Seal, Orca) dengan gambar tanda tanya + label "Segera hadir"
- Aturan Arwana/Hiu Merah tetap mengacu "10 stage Pasifik", tidak wajib Antartika

**Skor**:
- Skor stage 11-13: sama seperti stage 1-9 (skor_lulus = 70, poin soal 20)
- Total skor maksimal: naik dari 1000 (10 stage × 100) → 1300 (13 stage × 100)

## Risiko Pemain Lama

**Masalah**: Pemain lama sudah punya `progres_stage` 1-10, database SEED_VERSION lama (4)

**Solusi**:
1. **SEED_VERSION 4 → 5**: trigger seed reload, stage 11-13 muncul di database
2. **`progres_stage` lazy**: `GameRepository.mulaiStage()` buat baris baru untuk stage 11-13 saat pertama dibuka (sudah aman di kode)
3. **Unlock Antartika**: cek 10 stage selesai, tidak peduli kapan pemain instal (sebelum/sesudah update)
4. **Migrasi tidak perlu**: Room database sudah handle tambahan stage otomatis

**Test manual wajib**:
- Instal app lama (SEED_VERSION 4, 10 stage), main sampai stage 5 selesai
- Update ke app baru (SEED_VERSION 5, 13 stage)
- Buka app: seed reload, peta Pasifik tetap 10 stage (gerbang terkunci), progres stage 1-5 utuh
- Selesaikan stage 6-10 → gerbang Antartika terbuka
- Buka Antartika → stage 11-13 muncul, main normal

## Catatan Implementasi

**5b (Konten)**:
- Edit `2_Aset_dan_Konten/konten/stages.json`: tambah stage 11-13 (zona: "Polar")
- Edit `generate_seed.js`: `ZONA.Polar = [11,12,13]`, `materiRows = slice(0, 12)` (materi ID 10-12)
- SEED_VERSION 5
- Test: `SeedValidationTest` 55→70 soal (15 soal baru), 10→13 stage

**5c (Logika)**:
- `GameRepository.petaAntartikaTerbuka()`, `unlock logic`
- `PetaGeometri.stageAntartika`, `JUMLAH_STAGE_PASIFIK = 10`, `JUMLAH_STAGE_TOTAL = 13`
- Test unit: unlock, progres pemain lama

**5d (UI)**:
- `Art.peta_antartika` SVG (koordinat dengan desainer atau gunakan placeholder gradien biru-putih)
- `PetaScreen`: enum `PetaAktif`, tombol toggle, render node stage 11-13
- Gerbang Antartika: kondisional terkunci/terbuka, tap handler
- Hapus teks "segera hadir" di gerbang

---

**Persetujuan diperlukan sebelum lanjut ke 5b.**

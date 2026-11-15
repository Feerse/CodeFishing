# Rencana Antartika - Stage 11–20 (10 Stage)

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

**Stage 14: Dictionary**
- Deklarasi `kapal = {"nama": "Titanic", "panjang": 269}`
- Akses value `kapal["nama"]`
- Method `keys()`, `values()`, `items()`
- 5 soal: akses key, KeyError, update value, iterasi dict, gabungan dict + if

**Stage 15: String method dan f-string**
- Method: `upper()`, `lower()`, `strip()`, `split()`, `replace()`
- F-string: `f"Nama: {nama}, umur: {umur}"`
- 5 soal: manipulasi string, format output, split/join, gabungan string + loop

**Stage 16: Fungsi (def, return)**
- Definisi fungsi `def hitung_luas(p, l):`
- Return value
- Memanggil fungsi
- 5 soal: trace fungsi sederhana, prediksi return, fungsi tanpa return (None), scope variabel

**Stage 17: Parameter dan nilai default**
- Parameter posisi vs keyword
- Nilai default `def sapa(nama, formal=True):`
- 5 soal: fungsi dengan default, panggilan berbeda argumen, trace parameter

**Stage 18: Error dan try/except**
- Jenis error: `ValueError`, `ZeroDivisionError`, `IndexError`
- Block try/except/else/finally
- 5 soal: prediksi error, trace try/except, gabungan error handling + input

**Stage 19: Gabungan list + fungsi**
- Fungsi yang terima/return list
- List comprehension sederhana `[x*2 for x in nums]`
- 5 soal: fungsi filter list, transform list, reduce list, gabungan konsep

**Stage 20: Boss Ujian Antartika**
- 10 soal gabungan semua konsep stage 11-19
- Skor lulus 80%
- Soal lebih kompleks: nested loop + list, fungsi + dict, error handling + file logic
- Format sama dengan Boss Pasifik (stage 10)

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

**JUMLAH_STAGE**: 10 → 20

**Teks UI yang berubah**:
- `PetaScreen`: "Samudra Pasifik, 10 stage" → tetap "Samudra Pasifik, 10 stage"
- Tambah: "Antartika, 10 stage" (atau "X/10 stage selesai")
- Progress indicator total: "10/10" → "20/20" (atau split: Pasifik 10/10, Antartika 10/10)
- `PetaGeometri.JUMLAH_STAGE`: 10 → 20

**Ikan**:
- Stage 11-20: `ikan_target_id = null` (placeholder, aman di HasilStage cek sudah ada)
- Atau: gunakan 10 ikan placeholder baru (misal: Penguin, Seal, Orca, Krill, Albatros, dll) dengan gambar tanda tanya + label "Segera hadir"
- Aturan Arwana/Hiu Merah: update "selesaikan 10 stage" → "selesaikan 20 stage" (atau tetap hanya Pasifik, tergantung desain reward)

**Skor**:
- Skor stage 11-19: sama seperti stage 1-9 (skor_lulus = 70, poin soal 20)
- Skor stage 20 (Boss): skor_lulus = 80, poin soal 25, total 10 soal
- Total skor maksimal: naik dari 1000 (10 stage × 100) → 2000 (20 stage × 100)

## Risiko Pemain Lama

**Masalah**: Pemain lama sudah punya `progres_stage` 1-10, database SEED_VERSION lama (4)

**Solusi**:
1. **SEED_VERSION 4 → 5**: trigger seed reload, stage 11-20 muncul di database
2. **`progres_stage` lazy**: `GameRepository.mulaiStage()` buat baris baru untuk stage 11-20 saat pertama dibuka (sudah aman di kode)
3. **Unlock Antartika**: cek 10 stage selesai, tidak peduli kapan pemain instal (sebelum/sesudah update)
4. **Migrasi tidak perlu**: Room database sudah handle tambahan stage otomatis

**Test manual wajib**:
- Instal app lama (SEED_VERSION 4, 10 stage), main sampai stage 5 selesai
- Update ke app baru (SEED_VERSION 5, 20 stage)
- Buka app: seed reload, peta Pasifik tetap 10 stage (gerbang terkunci), progres stage 1-5 utuh
- Selesaikan stage 6-10 → gerbang Antartika terbuka
- Buka Antartika → stage 11-20 muncul, main normal
- Boss stage 20: 10 soal, skor lulus 80%, acak seperti boss stage 10

## Catatan Implementasi

**5b (Konten)**:
- Edit `2_Aset_dan_Konten/konten/stages.json`: tambah stage 11-20 (zona: "Polar", stage 20 boss: true)
- Tambah 9 materi baru (ID 10-18 untuk stage 11-19)
- Tambah 45 soal regular (5 per stage 11-19) + 10 soal boss (stage 20) = 55 soal baru
- Edit `generate_seed.js`: 
  - `ZONA.Polar = [11,12,13,14,15,16,17,18,19,20]`
  - `materiRows = slice(0, 18)` (materi ID 1-18)
  - Boss stage 20: seperti stage 10, ambil 10 soal boss dari stages.json
- SEED_VERSION 5
- Test: `SeedValidationTest` 55→110 soal (55 soal baru), 10→20 stage, 2 boss (stage 10, 20)

**5c (Logika)**:
- `GameRepository.petaAntartikaTerbuka()`: cek stage 1-10 selesai
- `PetaGeometri.stageAntartika`: posisi 10 stage (11-20)
- `PetaGeometri.JUMLAH_STAGE`: 10 → 20
- `UrutanSoal`: boss stage 20 sama dengan stage 10 (always shuffle)
- Test unit: unlock Antartika, boss stage 20, progres pemain lama

**5d (UI)**:
- `Art.peta_antartika` SVG (1000x2800 px seperti Pasifik, tema es/salju, atau gunakan placeholder gradien)
- `PetaScreen`: 
  - Enum `PetaAktif { Pasifik, Antartika }`
  - State `petaAktif`, tombol toggle "Pasifik ↔ Antartika"
  - Render node stage 11-20 di peta Antartika
  - Gerbang Antartika: kondisional terkunci/terbuka (cek `petaAntartikaTerbuka()`), tap → switch ke peta Antartika
- Hapus teks "segera hadir" di gerbang dan HomeScreen
- Update "10/10 stage" → "20/20 stage" atau split "Pasifik: 10/10, Antartika: 10/10"

---

**Persetujuan diperlukan sebelum lanjut ke 5b.**

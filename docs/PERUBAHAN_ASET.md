# Perubahan: aset final menggantikan visual kasar

Ringkasan penggantian visual dan audio. **Logika permainan, Room, repositori, dan interpreter tidak diubah.**

## Audio
| Berkas | Perubahan |
|---|---|
| `sfx_klik_1` | `.ogg` kasar diganti `UI_Click1.mp3` (final). Berkas `.ogg` lama dihapus agar tidak bentrok nama resource. |
| `bgm_umum` | Baru. Musik latar semua layar. |
| `bgm_aquarium` | Kini hanya diputar di layar Akuarium. |
| `AudioManager` | Enum `Bgm { Umum, Akuarium }` dan `pilihBgm()`. `App.kt` memilih trek sesuai layar. |

## Visual (emoji dan gambar buatan kode diganti aset final)
| Tempat | Sebelumnya | Sekarang |
|---|---|---|
| Bintang (`Bintang`) | teks ★ ☆ | `bintang_penuh` / `bintang_kosong` |
| Hamburger | garis digambar kode | `ikon_hamburger` |
| Bottom nav dan Drawer | emoji ⌂ ⚓ ⌘ 🐟 / tanpa ikon | `ikon_home`, `ikon_mode_belajar`, `ikon_mode_expert`, `ikon_koleksi`, `ikon_akuarium`, `ikon_pengaturan` |
| Indikator umpan | 1 ikon + angka | 5 cacing (`umpan_penuh` / `umpan_kosong`) + "n/5" |
| Home | emoji 🎣 🪙 ⭐ | `umpan_penuh`, `lencana_plus_satu`, `bintang_penuh` |
| Kuis: adegan | kanvas buatan kode (satu adegan untuk semua soal) | `adegan_*` per stage dengan aksi berbeda: joran, jala, tombak, tali lubang es, tali bubu, dan pukat; plus `percikan_air`, `ledakan_kail_putus`, `kail_putus`, ikan target stage, hujan dan petir di Boss |
| Kuis: tanda benar/salah | teks ✓ ✕ | `ikon_berhasil` / `ikon_error` |
| Hasil Stage | lingkaran + teks ✓ ✕ | `medali_misi_selesai` / `trofi_boss` / `kail_putus` |
| Hasil Program | lingkaran + teks ✓ ✕ | `medali_misi_selesai` / `ikon_error` |
| Umpan Habis | emoji 🪙 | `jam_hitung_mundur`, `lencana_plus_satu`, indikator umpan |
| Peta: node | lingkaran kanvas + emoji 🔒 ★ ✓ | `node_aktif` / `node_selesai` / `node_terkunci` / `node_boss` |
| Peta: hiasan | tidak ada | `bendera_start`, `kompas` |
| Koleksi | ikan yang sama digelapkan + emoji 🔒 | `ikan_terkunci_*`, `ikon_gembok`, `lencana_*` per kelangkaan |
| Sandbox: petak | kotak berwarna + emoji 🗑 | `petak_laut`, `petak_sampah`, `petak_kapal_sekarang`, `kapal_atas_timur` |
| Sandbox: daftar misi | angka polos | `lencana_misi_1` sampai `6`, `lencana_misi_bebas` |
| Akuarium | ikan bergoyang di tempat | pasir, rumput laut, batu, karang, gelembung, ikan berenang kiri-kanan dan menghadap arah geraknya, serpihan makanan jatuh saat Beri Makan, musik khusus |

Gambar di `assets-source` sudah identik dengan paket aset baru; yang diganti adalah **kode yang belum memakainya**.

## Perbaikan animasi alat pancing Mode Belajar (kail ganda)

**Masalah.** Di Mode Belajar tampak dua kail: satu diam (ikut tergambar di PNG `adegan_*`) dan satu lagi
bergerak keluar jalur (digambar kode dengan koordinat yang tidak cocok dengan gambar, misalnya titik adegan
Bubu berada di langit). Saat hasil jawaban keluar, pelampung/umpan tertinggal di layar.

**Perbaikan.**
| Bagian | Perubahan |
|---|---|
| `adegan_*.png` (7 berkas) | Joran, tali, pelampung, tombak, jala, bubu, dan pukat **dihapus dari gambar**. Kapal pada `adegan_kapal` dan `adegan_bubu` digeser 420 px ke kanan agar ada air terbuka untuk lemparan. Dibuat ulang dengan `2_Aset_dan_Konten/tools/bersihkan_adegan.py` dari gambar asli di `tools/adegan_asli`. |
| `MemancingGerak.kt` (baru) | Satu-satunya sumber posisi alat per frame (`hitungKeadaan`). Murni matematika, mudah dites. |
| `MemancingAdegan.kt` | Titik jangkar dikoreksi mengikuti aktor di gambar baru. |
| `MemancingScene.kt` | Hanya menggambar hasil `hitungKeadaan`. Satu set alat saja. |
| `MemancingVektor.kt` | Tambah `gambarBatang` (joran lentur) dan `gambarKail` (kail J + umpan cacing). |
| `KuisScreen.kt` | Teruskan `berhasil` ke adegan; suara cipratan diputar saat alat menyentuh air. |

**Alat dan gerak per studi kasus.**
| Adegan | Alat | Lempar | Dapat (benar) | Lepas (salah) |
|---|---|---|---|---|
| Kapal, Dermaga | Joran + tali + pelampung + kail berumpan cacing | Ayun ke belakang, lecut ke depan, pelampung melengkung lalu mendarat dan memercik | Pelampung tenggelam, joran melengkung, ikan dihela ke dek/dermaga, kail kembali berumpan ke ujung joran | Tali putus, pelampung dan kail hanyut lalu hilang, ikan kabur, joran memantul |
| Lubang es | Joran pendek + tali + kail berumpan | Tali diturunkan ke lubang, jigging | Ikan keluar dari lubang, mendarat di es, kail kembali | Kail naik kosong (umpan habis), ikan menyelam |
| Penyelam | Senapan tombak + tali penarik | Membidik, menembak, jejak gelembung | Tombak kena, tali menarik ikan ke penyelam, tombak terpasang kembali | Tombak meleset, ikan kabur, tali menarik tombak kembali |
| Perahu cadik | Jala lempar + tali | Jala digenggam, dilempar berputar, mengembang rata di air | Jala menutup berisi ikan, ditarik ke perahu | Jala ditarik kosong, ikan lolos |
| Bubu | Bubu + tali dari rel + pelampung penanda | Tali ditarik, bubu naik dari dasar | Bubu menembus permukaan dan diangkat ke rel dengan ikan | Ikan lolos dari bubu |
| Kapal pukat (Boss) | Pukat + tali derek | Diturunkan dari derek buritan, mengembang di air | Pukat dihela naik, diayun ke dek | Pukat terangkat kosong, ikan lolos |

Kontinuitas dijaga: akhir Lempar = awal Dapat/Lepas, akhir Dapat = kondisi Diam. Dites di
`GerakPancingTest`.

## Pembaruan peta, adegan, tema terang, dan kelangkaan

### 1. Peta: toolbar, transisi berlayar, pratinjau Segitiga Bermuda
- Label peta Antartika yang salah ("Samudra Pasifik" + "‹ Kembali") diganti toolbar peta (`ToolbarPeta` di `PetaScreen.kt`): judul peta aktif (**Lautan Antartika**) + tab Pasifik/Antartika.
- Pindah peta memutar animasi berlayar (`frontend/peta/TransisiBerlayar.kt`): kapal melintas, peta berganti saat layar tertutup penuh. Reduce motion: hanya pudar singkat.
- Ujung peta Antartika memuat node **Segitiga Bermuda** (`PetaGeometri.gerbangBermuda`) yang hanya membuka lembar pratinjau; tidak ada peta Bermuda yang bisa dimainkan.
- Aset baru: `gerbang_bermuda.png`, `preview_bermuda.png` (dibuat `tools/buat_bermuda.py`).

### 2. Tema terang: kontras aset
| Masalah | Perbaikan |
|---|---|
| Peta terang hampir putih; gunung es, pulau, siluet ikan menyatu | Palet `PETA_MAP` di `generate_tema_terang.py` diganti ke biru siang jenuh; vignette tidak lagi kabut putih |
| `bintang_kosong_terang`, `umpan_kosong_terang`, `petak_*_terang` pucat (kontur dipetakan jadi putih) | Peta warna khusus `ICON_MAP_KONTRAS` |
| Ikan/benda pucat (teri, bandeng, albatros, gunung es, kail putus) menyatu dengan latar terang | `GambarKontras`: kontur gelap + bayangan di tema terang, identik dengan `Gambar` di tema gelap |
| Tali, ombak, salju, hujan di adegan memancing memakai warna tema (navy di tema terang) di atas ilustrasi yang selalu gelap | Warna tetap terang di `MemancingScene.kt` |
| Rute peta, garis tepi kartu, komentar kode terlalu samar | `petaGaris`, `garis`, `kodeKomentar` di `Color.kt`; alpha rute 0,8 di tema terang |

### 3. Studi kasus mengikuti peta
- Varian `adegan_*_antartika.png` (tools/buat_adegan_antartika.py): bongkahan es, gunung es di cakrawala, salju di dermaga/atap kapal, es menggantung, aurora, langit-langit es di bawah laut. Zona lemparan dan jalur ikan dijaga kosong.
- Lubang es dipindah ke Antartika (stage 12, 15, 19). Pasifik stage 5 memakai kapal.

### 4. Kelangkaan visual (`Kelangkaan.kt`)
| Tier | Bingkai | Ornamen | Efek |
|---|---|---|---|
| Biasa | polos tipis | - | - |
| Langka | siku kristal | permata sudut + puncak | kilau berkedip |
| Epik | cincin ganda (luar solid, dalam putus-putus) | bintang sudut + puncak | cahaya bernapas, bintang mengorbit |
| Legenda | emas tebal bergradien | mahkota di puncak | sinar berputar di belakang ikan, sapuan kilap, debu emas |
| Mitos | gradien pelangi berputar | nyala api sudut + puncak | pusaran busur, bara naik, halo berdenyut |
Dipakai di Koleksi (kartu + detail), Akuarium, Hasil Stage, Materi, lembar stage Peta, dan adegan kuis. `ChipTier` memakai lambang berbeda per tier.

## Performa: frame drop dan boot peta (lihat `docs/PERFORMA.md`)
- Aset: `optimalkan_aset.py` mengecilkan ikan/ikon/alat ke ≤448 px, membuat 118 thumbnail `*_kecil.png`, dan merender peta ke `peta_*.webp`. Gambar asli resolusi penuh ada di `tools/aset_resolusi_penuh/`.
- `PetaSvg` + dependensi AndroidSVG dihapus; diganti `PetaLatar` (dekode WebP + cache dua peta).
- `CacheGambar` + pemanasan saat layar pembuka; `Gambar`/`GambarKontras` memakai cache; `gambarIkanKecil`.
- Audio latar berjalan di thread kerja; transisi layar lebih ringan; efek kelangkaan dibekukan saat scroll.
- Toolbar peta bisa disembunyikan (penanda kecil untuk menampilkannya lagi; pilihan diingat).
- Build: varian `release` bisa dipasang langsung (kunci debug) + `profileinstaller`.


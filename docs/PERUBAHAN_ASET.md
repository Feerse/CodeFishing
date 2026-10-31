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
| Kuis: adegan | kanvas buatan kode (satu adegan untuk semua soal) | `adegan_*` per stage, `percikan_air`, `ledakan_kail_putus`, `kail_putus`, ikan target stage, hujan dan petir di Boss |
| Kuis: tanda benar/salah | teks ✓ ✕ | `ikon_berhasil` / `ikon_error` |
| Hasil Stage | lingkaran + teks ✓ ✕ | `tameng_lulus_70` / `trofi_boss` / `kail_putus` |
| Hasil Program | lingkaran + teks ✓ ✕ | `medali_misi_selesai` / `ikon_error` |
| Umpan Habis | emoji 🪙 | `jam_hitung_mundur`, `lencana_plus_satu`, indikator umpan |
| Peta: node | lingkaran kanvas + emoji 🔒 ★ ✓ | `node_aktif` / `node_selesai` / `node_terkunci` / `node_boss` |
| Peta: hiasan | tidak ada | `bendera_start`, `kompas` |
| Koleksi | ikan yang sama digelapkan + emoji 🔒 | `ikan_terkunci_*`, `ikon_gembok`, `lencana_*` per kelangkaan |
| Sandbox: petak | kotak berwarna + emoji 🗑 | `petak_laut`, `petak_sampah`, `petak_kapal_sekarang`, `kapal_atas_timur` |
| Sandbox: daftar misi | angka polos | `lencana_misi_1` sampai `6`, `lencana_misi_bebas` |
| Akuarium | ikan bergoyang di tempat | pasir, rumput laut, batu, karang, gelembung, ikan berenang kiri-kanan dan menghadap arah geraknya, serpihan makanan jatuh saat Beri Makan, musik khusus |

Gambar di `assets-source` sudah identik dengan paket aset baru; yang diganti adalah **kode yang belum memakainya**.

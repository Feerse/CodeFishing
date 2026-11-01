package com.example.codefishing.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Palet "Oceanic Deep Dive" (mode gelap)
val Primary = Color(0xFFF26A2E)      // oranye: aksi utama
val Secondary = Color(0xFF133C4F)    // biru laut: permukaan kartu
val Tertiary = Color(0xFF5FD3A0)     // hijau: berhasil
val Neutral = Color(0xFF0C2B3A)      // biru tua: latar
val OnDark = Color(0xFFF4F8F9)
val OnDarkMuted = Color(0xFF9DB5BF)
val ErrorRed = Color(0xFFFF6B81)

// Palet "Oceanic Daylight" (mode terang)
val PrimaryLight = Color(0xFFC2410C)
val OnPrimaryLight = Color(0xFFFFFFFF)
val SecondaryLight = Color(0xFF3D6472)
val TertiaryLight = Color(0xFF157A57)
val NeutralLight = Color(0xFFF4F8F9)
val OnLight = Color(0xFF0C2B3A)
val OnLightMuted = Color(0xFF4A6470)

// Warna kelangkaan ikan (mode gelap)
val RarityCommon = Color(0xFFB7C2C6)
val RarityRare = Color(0xFF6EA8FF)
val RarityEpic = Color(0xFFB18CFF)
val RarityLegend = Color(0xFFFFD166)
val RarityMythic = Color(0xFFFF6B81)

/**
 * Token warna semantik untuk bagian aplikasi yang tidak tercakup
 * MaterialTheme (gradien latar, kartu status, kode, peta, akuarium).
 */
@Immutable
data class WarnaCodeFishing(
    val gelap: Boolean,
    val latarAtas: Color,
    val latarBawah: Color,
    val kartu: Color,
    val kartuTinggi: Color,
    val kartuLembut: Color,
    val garis: Color,
    val suksesLatar: Color,
    val suksesTeks: Color,
    val peringatanLatar: Color,
    val peringatanTeks: Color,
    val bahayaLatar: Color,
    val bahayaTeks: Color,
    val kodeLatar: Color,
    val kodeTeks: Color,
    val kodeKataKunci: Color,
    val kodeKomentar: Color,
    val petaLatar: Color,
    val petaLatarBawah: Color,
    val petaTeks: Color,
    val petaTeksRedup: Color,
    val petaChipLatar: Color,
    val petaLabelLatar: Color,
    val petaGaris: Color,
    val petaNomorTeks: Color,
    val akuariumAtas: Color,
    val akuariumBawah: Color,
    val ombak: Color,
    val teksDiAdegan: Color,
    val teksDiAdeganRedup: Color,
    val rarityBiasa: Color,
    val rarityLangka: Color,
    val rarityEpik: Color,
    val rarityLegenda: Color,
    val rarityMitos: Color,
)

val WarnaGelap = WarnaCodeFishing(
    gelap = true,
    latarAtas = Color(0xFF0B2C3B),
    latarBawah = Color(0xFF071923),
    kartu = Secondary,
    kartuTinggi = Color(0xFF173F4E),
    kartuLembut = Color(0xFF123B4A),
    garis = Color(0x14FFFFFF),
    suksesLatar = Color(0xFF113D34),
    suksesTeks = Color(0xFF8BF0C7),
    peringatanLatar = Color(0xFF2A2A17),
    peringatanTeks = Color(0xFFFFD98A),
    bahayaLatar = Color(0xFF3C2327),
    bahayaTeks = Color(0xFFFFB3B3),
    kodeLatar = Color(0xFF071E29),
    kodeTeks = Color(0xFFE8F4F6),
    kodeKataKunci = Color(0xFFFFC46B),
    kodeKomentar = Color(0xFF7FB3C8),
    petaLatar = Color(0xFF071923),
    petaLatarBawah = Color(0xFF07171F),
    petaTeks = Color(0xFFE8F4F6),
    petaTeksRedup = Color(0xFF9DB5BF),
    petaChipLatar = Color(0xCC081B26),
    petaLabelLatar = Color(0xB3081B26),
    petaGaris = Color(0xFF8FB2B8),
    petaNomorTeks = Color(0xFFFFE7B0),
    akuariumAtas = Color(0xFF07212C),
    akuariumBawah = Color(0xFF041820),
    ombak = Color(0xFF7FC9E0),
    teksDiAdegan = Color(0xFFFFFFFF),
    teksDiAdeganRedup = Color(0xBFFFFFFF),
    rarityBiasa = RarityCommon,
    rarityLangka = RarityRare,
    rarityEpik = RarityEpic,
    rarityLegenda = RarityLegend,
    rarityMitos = RarityMythic,
)

val WarnaTerang = WarnaCodeFishing(
    gelap = false,
    latarAtas = Color(0xFFEAF6FA),
    latarBawah = Color(0xFFF7FBFC),
    kartu = Color(0xFFFFFFFF),
    kartuTinggi = Color(0xFFF1F8FA),
    kartuLembut = Color(0xFFE7F2F6),
    garis = Color(0x1F0C2B3A),
    suksesLatar = Color(0xFFE3F5EC),
    suksesTeks = Color(0xFF146B4C),
    peringatanLatar = Color(0xFFFFF4D6),
    peringatanTeks = Color(0xFF8A5B00),
    bahayaLatar = Color(0xFFFCE8EB),
    bahayaTeks = Color(0xFFA32A3A),
    kodeLatar = Color(0xFFF1F7F9),
    kodeTeks = Color(0xFF12333F),
    kodeKataKunci = Color(0xFFA64700),
    kodeKomentar = Color(0xFF4E7A88),
    petaLatar = Color(0xFFEAF7FB),
    petaLatarBawah = Color(0xFFDFF1F8),
    petaTeks = Color(0xFF12333F),
    petaTeksRedup = Color(0xFF4A6470),
    petaChipLatar = Color(0xF0EAF7FB),
    petaLabelLatar = Color(0xE6FFFFFF),
    petaGaris = Color(0xFF5E8794),
    petaNomorTeks = Color(0xFF8A5B00),
    akuariumAtas = Color(0xFFDFF3F8),
    akuariumBawah = Color(0xFFBCE6F0),
    ombak = Color(0xFF5E8794),
    teksDiAdegan = Color(0xFF12333F),
    teksDiAdeganRedup = Color(0xBF12333F),
    rarityBiasa = Color(0xFF5F7078),
    rarityLangka = Color(0xFF2C63C4),
    rarityEpik = Color(0xFF7444C9),
    rarityLegenda = Color(0xFF8A5B00),
    rarityMitos = Color(0xFFC42A44),
)

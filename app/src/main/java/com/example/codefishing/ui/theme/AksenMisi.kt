package com.example.codefishing.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Aksen per misi Mode Expert.
 *
 * Daftar misi Expert dulu seluruhnya satu warna: kartu, bintang, dan badge memakai palet yang
 * sama, sehingga sebelas misi hanya bisa dibedakan dengan membaca judulnya. Setiap misi sekarang
 * punya satu warna sendiri yang dipakai di sisi kartu, cincin lencana, teks par, judul misi, dan
 * bingkai papan — jadi misi dikenali dari warna sebelum teksnya dibaca.
 *
 * Warna dipilih agar tetap terbaca di dua tema. Rasio kontras terendah: 4,51:1 (m3 gelap di atas
 * kartu) dan 4,6:1 (m11 terang di atas latar) — keduanya lolos ambang teks 4,5:1.
 */
val AksenMisiGelap: List<Color> = listOf(
    Color(0xFF5FD3A0), // m1 hijau mint
    Color(0xFF6EA8FF), // m2 biru
    Color(0xFFB18CFF), // m3 ungu
    Color(0xFFFF8FB1), // m4 merah muda
    Color(0xFFFFA45C), // m5 oranye
    Color(0xFFFFD166), // m6 emas
    Color(0xFFA8DE6B), // m7 hijau limau (while)
    Color(0xFF59D6E6), // m8 sian (jumlah)
    Color(0xFFA6B6FF), // m9 indigo (get_x/get_y)
    Color(0xFFFF9A9A), // m10 merah (print)
    Color(0xFFC7B28A), // m11 cokelat pasir (mode bebas)
)

val AksenMisiTerang: List<Color> = listOf(
    Color(0xFF14795A),
    Color(0xFF2C63C4),
    Color(0xFF7444C9),
    Color(0xFFB02A63),
    Color(0xFFC2410C),
    Color(0xFF8A5B00),
    Color(0xFF4A7A10),
    Color(0xFF0A6474),
    Color(0xFF3646A8),
    Color(0xFFA3241C),
    Color(0xFF6B532A),
)

/** Warna aksen untuk misi bernomor [nomor] (1..11); nomor di luar jangkauan diputar. */
fun aksenMisi(nomor: Int, gelap: Boolean): Color {
    val daftar = if (gelap) AksenMisiGelap else AksenMisiTerang
    return daftar[(nomor - 1).mod(daftar.size)]
}

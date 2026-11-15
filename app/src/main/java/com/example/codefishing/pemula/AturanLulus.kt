package com.example.codefishing.pemula

/**
 * Jumlah jawaban benar minimal agar skor `benar * 100 / total` (pembulatan
 * ke bawah) mencapai [ambangPersen]. Misalnya 5 soal dengan ambang 70%
 * membutuhkan 4 jawaban benar, dan 10 soal dengan ambang 80% membutuhkan 8.
 */
fun minimalBenar(total: Int, ambangPersen: Int): Int {
    if (total <= 0) return 0
    if (ambangPersen <= 0) return 0
    return ((total * ambangPersen) + 99) / 100
}

package com.example.codefishing.pemula

/**
 * TIM: Logika Mode Pemula. Aturan umpan (nyawa) Mode Belajar, murni Kotlin supaya mudah diuji.
 * Aturan prototipe: maksimum umpan penuh, pulih 1 umpan tiap 15 menit, salah menjawab -1 umpan.
 */
data class BaitState(val jumlah: Int, val sejakMs: Long?)

object BaitRules {
    /** Terapkan pemulihan sesuai waktu yang sudah lewat. */
    fun pulihkan(s: BaitState, nowMs: Long, max: Int, regenMs: Long): BaitState {
        if (s.jumlah >= max) return BaitState(max, null)
        val sejak = s.sejakMs ?: return BaitState(s.jumlah, nowMs)
        val dapat = ((nowMs - sejak) / regenMs).toInt().coerceAtLeast(0)
        val baru = minOf(max, s.jumlah + dapat)
        return if (baru >= max) BaitState(max, null) else BaitState(baru, sejak + dapat * regenMs)
    }

    /** Kurangi 1 umpan (setelah dipulihkan). Tidak turun di bawah 0. */
    fun pakai(s: BaitState, nowMs: Long, max: Int, regenMs: Long): BaitState {
        val p = pulihkan(s, nowMs, max, regenMs)
        if (p.jumlah <= 0) return p
        return BaitState(p.jumlah - 1, p.sejakMs ?: nowMs)
    }

    /** Sisa milidetik sampai umpan berikutnya pulih, 0 bila penuh. */
    fun sisaMs(s: BaitState, nowMs: Long, max: Int, regenMs: Long): Long {
        val p = pulihkan(s, nowMs, max, regenMs)
        val sejak = p.sejakMs ?: return 0L
        return (regenMs - (nowMs - sejak)).coerceIn(0L, regenMs)
    }
}

package com.example.codefishing.pemula

import com.example.codefishing.core.content.GameConfig
import com.example.codefishing.core.data.ProgressRepository

/**
 * TIM: Logika Mode Pemula. Menyambungkan [BaitRules] ke penyimpanan ([ProgressRepository]).
 * Layar memanggil jumlah(), pakai(), dan sisaMs() saja.
 */
class BaitStore(
    private val progress: ProgressRepository,
    private val config: GameConfig,
    private val now: () -> Long = { System.currentTimeMillis() },
) {
    private fun muat(): BaitState {
        val j = if (progress.umpan < 0) config.umpanMax else progress.umpan
        val sejak = progress.umpanSejak.takeIf { it > 0L }
        return BaitState(j, sejak)
    }

    private fun simpan(s: BaitState) {
        progress.umpan = s.jumlah
        progress.umpanSejak = s.sejakMs ?: 0L
    }

    fun jumlah(): Int {
        val s = BaitRules.pulihkan(muat(), now(), config.umpanMax, config.regenMs)
        simpan(s)
        return s.jumlah
    }

    /** Kurangi 1 umpan (jawaban salah). Hasilnya jumlah umpan sekarang. */
    fun pakai(): Int {
        val s = BaitRules.pakai(muat(), now(), config.umpanMax, config.regenMs)
        simpan(s)
        return s.jumlah
    }

    fun sisaMs(): Long = BaitRules.sisaMs(muat(), now(), config.umpanMax, config.regenMs)
}

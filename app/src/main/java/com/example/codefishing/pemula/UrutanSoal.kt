package com.example.codefishing.pemula

import kotlin.random.Random

/**
 * Urutan soal untuk satu sesi kuis Mode Pemula.
 *
 * Percobaan pertama stage biasa tetap urutan kurikulum. Saat diulang, soal diacak.
 * Boss selalu diacak (bahkan percobaan pertama). Dengan 10 soal, probabilitas
 * mendapat urutan yang sama persis = 1/10! = 1/3,628,800 (sangat kecil).
 */
fun urutanSoal(
    soal: List<SoalKuis>,
    ulangi: Boolean,
    boss: Boolean = false,
    random: Random = Random.Default,
): List<SoalKuis> {
    if (soal.size <= 1) return soal
    if (!boss && !ulangi) return soal
    return soal.shuffled(random)
}
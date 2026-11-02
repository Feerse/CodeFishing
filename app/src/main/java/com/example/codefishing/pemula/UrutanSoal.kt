package com.example.codefishing.pemula

import kotlin.random.Random

/**
 * Urutan soal untuk satu sesi kuis Mode Pemula.
 *
 * Percobaan pertama tetap mengikuti urutan kurikulum. Saat stage diulang
 * (sudah pernah diselesaikan) soal diacak agar pemain tidak menghafal posisi soal.
 */
fun urutanSoal(
    soal: List<SoalKuis>,
    ulangi: Boolean,
    random: Random = Random.Default,
): List<SoalKuis> = if (ulangi && soal.size > 1) soal.shuffled(random) else soal
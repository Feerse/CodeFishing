package com.example.codefishing.expert

/**
 * TIM: Logika Mode Expert. Penilaian bintang Sandbox (sama dengan prototipe HTML):
 * 3 bintang bila ticks <= par, 2 bintang bila ticks <= ceil(par x 1,4), selain itu 1 bintang.
 * Misi tanpa par selalu 3 bintang bila berhasil.
 */
object Scoring {
    fun bintang(ticks: Int, par: Int?): Int {
        if (par == null) return 3
        if (ticks <= par) return 3
        val batasDua = kotlin.math.ceil(par * 1.4).toInt()
        return if (ticks <= batasDua) 2 else 1
    }
}

package com.example.codefishing.expert

/**
 * Penilaian bintang Sandbox sesuai DFD BR-09:
 * 3 bintang bila ticks <= par, 2 bintang bila ticks <= ceil(par x 1,5),
 * selain itu 1 bintang. Misi tanpa par selalu 3 bintang bila berhasil.
 */
object Scoring {
    fun bintang(ticks: Int, par: Int?): Int {
        if (par == null || par <= 0) return 3
        if (ticks <= par) return 3
        val batasDua = kotlin.math.ceil(par * 1.5).toInt()
        return if (ticks <= batasDua) 2 else 1
    }
}

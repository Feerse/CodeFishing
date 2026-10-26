package com.example.codefishing.expert

/**
 * Penilaian bintang Sandbox sesuai DFD BR-09:
 * 3 bintang bila ticks <= par, 2 bintang bila ticks <= ceil(par x 1,5),
 * selain itu 1 bintang. Misi tanpa par selalu 3 bintang bila berhasil.
 */
object Scoring {
    /** Par di luar 1..998 (mis. 999 pada mode Bebas) dianggap tanpa par. */
    fun parEfektif(par: Int?): Int? = par?.takeIf { it in 1..998 }

    fun bintang(ticks: Int, par: Int?): Int {
        val p = parEfektif(par)
        if (p == null) return 3
        if (ticks <= p) return 3
        val batasDua = kotlin.math.ceil(p * 1.5).toInt()
        return if (ticks <= batasDua) 2 else 1
    }
}

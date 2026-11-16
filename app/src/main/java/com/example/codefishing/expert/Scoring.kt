package com.example.codefishing.expert

/**
 * Penilaian bintang Sandbox sesuai DFD BR-09:
 * 3 bintang bila ticks <= par, 2 bintang bila ticks <= ceil(par x 1,5),
 * selain itu 1 bintang. Misi tanpa par selalu 3 bintang bila berhasil.
 */
object Scoring {
    /** Bonus XP sekali saja saat sebuah misi pertama kali mencapai 3 bintang. */
    const val BONUS_TIGA_BINTANG = 40

    /** Par di luar 1..998 (mis. 999 pada mode Bebas) dianggap tanpa par. */
    fun parEfektif(par: Int?): Int? = par?.takeIf { it in 1..998 }

    fun bintang(ticks: Int, par: Int?): Int {
        val p = parEfektif(par)
        if (p == null) return 3
        if (ticks <= p) return 3
        val batasDua = kotlin.math.ceil(p * 1.5).toInt()
        return if (ticks <= batasDua) 2 else 1
    }

    /**
     * Bonus XP untuk pencapaian 3 bintang pertama pada satu misi.
     *
     * Tanpa bonus, mengulang misi yang sudah selesai tidak memberi apa pun; hadiah ini memberi
     * alasan menyempurnakan program sampai tick-nya di bawah par, bukan sekadar lulus.
     *
     * @param sebelum bintang terbaik misi itu sebelum percobaan ini.
     */
    fun bonusTigaBintang(sukses: Boolean, bintang: Int, sebelum: Int): Int =
        if (sukses && bintang >= 3 && sebelum < 3) BONUS_TIGA_BINTANG else 0

    /**
     * Boleh tidaknya satu spesies membayar XP per ekor pada misi ini.
     *
     * XP hanya dibayar sekali untuk setiap pasangan misi + spesies, dan hanya bila misinya
     * berhasil. Tanpa batas ini, misi berisi 16 ikan memberi 400 XP setiap kali dijalankan,
     * sehingga level pemain bisa dinaikkan tanpa henti tanpa menulis program baru.
     *
     * @param sudahDibayar id ikan yang sudah pernah membayar XP di misi tersebut.
     */
    fun xpPerEkorBoleh(sukses: Boolean, sudahDibayar: Set<Int>, ikanId: Int): Boolean =
        sukses && ikanId !in sudahDibayar
}

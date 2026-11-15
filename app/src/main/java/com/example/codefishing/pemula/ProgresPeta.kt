package com.example.codefishing.pemula

/**
 * Aturan perpindahan peta belajar: Samudra Pasifik (stage 1-10) lalu Antartika (stage 11-20).
 * Dipakai GameRepository dan PetaScreen supaya ambang "10 stage" tidak ditulis ulang sebagai angka.
 */
object ProgresPeta {
    /** Nomor stage terakhir di Samudra Pasifik. */
    const val STAGE_PASIFIK_TERAKHIR = 10

    /** Nomor stage pertama di Antartika. */
    const val STAGE_ANTARTIKA_PERTAMA = STAGE_PASIFIK_TERAKHIR + 1

    fun diAntartika(nomor: Int): Boolean = nomor >= STAGE_ANTARTIKA_PERTAMA

    /** Nomor stage berikutnya, atau null bila sudah di stage terakhir. */
    fun nomorLanjut(nomor: Int, jumlahStage: Int): Int? = (nomor + 1).takeIf { it <= jumlahStage }

    /**
     * Antartika terbuka setelah seluruh stage Pasifik selesai.
     * [selesaiPasifik] adalah jumlah stage 1-10 yang berstatus selesai.
     */
    fun antartikaTerbuka(selesaiPasifik: Int): Boolean = selesaiPasifik >= STAGE_PASIFIK_TERAKHIR
}

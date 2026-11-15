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

    /**
     * Stage yang paling masuk akal dilanjutkan: yang belum selesai dan sudah terbuka, atau stage
     * terakhir bila semuanya selesai. [daftar] harus berurutan menurut nomor stage.
     *
     * Dipakai Home (tombol "Lanjutkan") dan GameRepository (saat masuk mode) supaya keduanya
     * memilih stage yang sama dan tidak ada aturan yang ditulis dua kali.
     */
    fun <T> stageAktif(
        daftar: List<T>,
        status: (T) -> String,
        terbuka: (T) -> Boolean,
    ): T? = daftar.firstOrNull { status(it) != "selesai" && terbuka(it) } ?: daftar.lastOrNull()

    /**
     * Nomor acuan untuk memilih peta awal: progres terjauh antara stage yang sedang aktif dan
     * posisi terakhir pemain.
     *
     * Memakai nilai terbesar supaya mengulang stage lama (posisi jadi mundur, mis. stage 2)
     * tidak menarik peta kembali ke Samudra Pasifik padahal pemain sudah di Antartika.
     */
    fun nomorAcuanPeta(progres: Int?, posisi: Int?): Int? =
        listOfNotNull(progres, posisi).maxOrNull()

    /** Peta yang harus dibuka lebih dulu: Antartika bila progres sudah melewati stage 10. */
    fun petaAntartika(antartikaTerbuka: Boolean, nomorAcuan: Int?): Boolean =
        antartikaTerbuka && (nomorAcuan ?: 0) > STAGE_PASIFIK_TERAKHIR
}

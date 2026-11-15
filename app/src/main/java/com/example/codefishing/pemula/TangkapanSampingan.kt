package com.example.codefishing.pemula

/**
 * Aturan tangkapan sampingan: ikan tanpa stage yang ikut tertangkap saat
 * menyelesaikan stage. Stage Pasifik mengundi ikan Biasa/Langka, stage
 * Antartika (laut dalam) mengundi Epik sampai Mitos.
 */
object TangkapanSampingan {
    /** Peluang mendapat satu ikan sampingan pada setiap stage yang lulus. */
    const val PELUANG = 0.35

    /** Syarat ikan hadiah khusus (combo/penyelesaian), bukan tangkapan sampingan. */
    const val SYARAT_KHUSUS = "Tantangan khusus"

    private val TIER_PASIFIK = setOf("biasa", "langka")
    private val TIER_LAUT_DALAM = setOf("epik", "legenda", "mitos")

    fun lautDalam(nomorStage: Int): Boolean = ProgresPeta.diAntartika(nomorStage)

    fun tierCocok(tier: String, lautDalam: Boolean): Boolean =
        tier in if (lautDalam) TIER_LAUT_DALAM else TIER_PASIFIK

    /**
     * Ikan layak diundi: tanpa stage, bukan hadiah khusus, punya ilustrasi,
     * dan tier-nya cocok dengan wilayah stage.
     */
    fun kandidat(
        asalStageId: Int?,
        tier: String,
        syaratUnlock: String?,
        gambar: String?,
        lautDalam: Boolean,
    ): Boolean =
        asalStageId == null &&
            syaratUnlock != SYARAT_KHUSUS &&
            !gambar.isNullOrBlank() &&
            gambar != "tanda_tanya_ikan" &&
            tierCocok(tier, lautDalam)

    /** Bobot undian: makin langka, makin kecil peluangnya. */
    fun bobot(tier: String): Int = when (tier) {
        "biasa" -> 50
        "langka" -> 30
        "epik" -> 12
        "legenda" -> 6
        "mitos" -> 2
        else -> 1
    }

    /**
     * Pilih satu kandidat memakai undian pada skala total bobot.
     * [undian] di luar rentang akan diambil yang terdekat (0 atau kandidat terakhir).
     */
    fun <T> pilih(kandidat: List<T>, bobot: (T) -> Int, undian: Int): T? {
        if (kandidat.isEmpty()) return null
        var sisa = undian.coerceAtLeast(0)
        for (k in kandidat) {
            sisa -= bobot(k).coerceAtLeast(1)
            if (sisa < 0) return k
        }
        return kandidat.last()
    }
}

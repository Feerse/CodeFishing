package com.example.codefishing.pemula

/** Aturan EXP dan level (DFD BR-04: level x 100). */
object LevelRules {
    fun levelUntukXp(xp: Int): Int = (xp.coerceAtLeast(0) / 100) + 1

    fun xpAwalLevel(level: Int): Int = (level.coerceAtLeast(1) - 1) * 100

    fun progresLevel(xp: Int): Float {
        val lv = levelUntukXp(xp)
        val awal = xpAwalLevel(lv)
        return ((xp - awal) / 100f).coerceIn(0f, 1f)
    }

    fun rank(level: Int): String = when {
        level <= 2 -> "Semut Laut"
        level <= 4 -> "Ikan Kecil"
        level <= 6 -> "Nelayan"
        level <= 8 -> "Kapten"
        level <= 10 -> "Albatros"
        else -> "Paus Biru"
    }
}

/** Aturan tangkapan ikan (DFD BR-05/BR-06). */
object FishRules {
    private val tier = listOf("biasa", "langka", "epik", "legenda", "mitos")

    fun bobot(nominal: Double, acak: Double = 1.0): Double {
        val faktor = 0.85 + (acak.coerceIn(0.0, 1.0) * 0.40)
        return ((nominal * faktor) * 10).toInt() / 10.0
    }

    fun bobotAcak(nominal: Double, random: kotlin.random.Random = kotlin.random.Random): Double =
        bobot(nominal, random.nextDouble())

    fun naikTier(t: String): String {
        val i = tier.indexOf(t)
        return if (i < 0 || i == tier.lastIndex) "mitos" else tier[i + 1]
    }

    fun tierValid(t: String): Boolean = t in tier

    fun tierIndex(t: String): Int = tier.indexOf(t).let { if (it < 0) 0 else it }

    fun lebihTinggi(a: String, b: String): Boolean = tierIndex(a) > tierIndex(b)

    /**
     * Ikan bonus dari jawaban benar beruntun.
     * Karena satu stage hanya 5 soal, ambangnya 3/4/5 dan semua yang tercapai diberikan.
     */
    fun bonusCombo(combo: Int): List<String> = buildList {
        if (combo >= 3) add("Kerapu")
        if (combo >= 4) add("Barakuda")
        if (combo >= 5) add("Tenggiri")
    }
}

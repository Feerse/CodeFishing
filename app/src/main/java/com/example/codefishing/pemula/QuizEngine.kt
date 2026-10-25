package com.example.codefishing.pemula

/** Model kuis yang sudah dipisahkan dari Room supaya bisa diuji sebagai Kotlin murni. */
data class PilihanKuis(
    val id: Int,
    val teks: String,
    val benar: Boolean,
)

data class SoalKuis(
    val id: Int,
    val teks: String,
    val kode: String,
    val pembahasan: String,
    val poin: Int,
    val pilihan: List<PilihanKuis>,
)

data class HasilJawab(
    val benar: Boolean,
    val pembahasan: String,
    val poin: Int,
    val combo: Int,
    val umpanBerkurang: Boolean,
)

/**
 * Satu sesi kuis 5 soal. Murni Kotlin.
 * Lulus stage >= 70%; boss memakai ambang 80% (4 dari 5).
 */
class QuizEngine(
    val soal: List<SoalKuis>,
    private val skorLulus: Int,
) {
    var indeks: Int = 0
        private set
    var jumlahBenar: Int = 0
        private set
    var jumlahSalah: Int = 0
        private set
    var combo: Int = 0
        private set
    var comboTertinggi: Int = 0
        private set
    var xp: Int = 0
        private set

    val total: Int get() = soal.size
    val selesai: Boolean get() = indeks >= total
    val soalSaatIni: SoalKuis? get() = soal.getOrNull(indeks)
    val persen: Int get() = if (total == 0) 0 else (jumlahBenar * 100) / total
    val lulus: Boolean get() = persen >= skorLulus
    val sempurna: Boolean get() = jumlahSalah == 0 && jumlahBenar == total && total > 0

    fun pilih(pilihanIndeks: Int): HasilJawab? {
        val q = soalSaatIni ?: return null
        val p = q.pilihan.getOrNull(pilihanIndeks) ?: return null
        if (p.benar) {
            jumlahBenar++
            combo++
            comboTertinggi = maxOf(comboTertinggi, combo)
            xp += q.poin
            indeks++
            return HasilJawab(true, q.pembahasan, q.poin, combo, false)
        }
        jumlahSalah++
        combo = 0
        indeks++
        return HasilJawab(false, q.pembahasan, 0, 0, true)
    }
}

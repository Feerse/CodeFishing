package com.example.codefishing.expert

/**
 * TIM: Logika Mode Expert. Model dunia Mode Expert (Sandbox).
 * Aturan biaya waktu (ticks): gerak() = 1, lempar() = 2, lihat() = 0.
 */
enum class Arah { Utara, Timur, Selatan, Barat }

object Biaya {
    const val GERAK = 1
    const val LEMPAR = 2
    const val LIHAT = 0
}

/** Isi satu petak. [ikan] berisi kunci spesies (mis. "teri") atau "sampah"; kosong = laut kosong. */
data class Petak(val isi: String) {
    val kosong: Boolean get() = isi.isEmpty()
}

class Dunia(val ukuran: Int, petak: List<String>) {
    private val grid: MutableList<Petak> = petak.map { Petak(it) }.toMutableList()
    var x: Int = 0
        private set
    var y: Int = 0
        private set
    var ticks: Int = 0
        private set
    var tertangkap: Int = 0
        private set

    fun petakDi(px: Int, py: Int): Petak = grid[py * ukuran + px]
}

data class HasilJalan(
    val berhasil: Boolean,
    val ticks: Int,
    val tertangkap: Int,
    val keluaran: List<String>,
    val pesanError: String? = null,
)

/**
 * Penjalan kode Python pemain. TODO(Expert): isi implementasi (interpreter subset Python
 * di Kotlin, atau library pihak ketiga yang berjalan offline). Layar Sandbox hanya memanggil antarmuka ini.
 */
fun interface CodeRunner {
    fun jalankan(kode: String, dunia: Dunia): HasilJalan
}

/** Pengganti sementara supaya layar bisa dirangkai sebelum interpreter selesai. */
object BelumAdaRunner : CodeRunner {
    override fun jalankan(kode: String, dunia: Dunia): HasilJalan =
        HasilJalan(false, 0, 0, emptyList(), "Penjalan kode belum dibuat (tugas tim Expert).")
}

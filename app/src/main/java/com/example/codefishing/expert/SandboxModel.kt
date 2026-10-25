package com.example.codefishing.expert

import com.example.codefishing.expert.py.Langkah

/**
 * TIM: Logika Mode Expert. Model dunia dan aturan tick.
 * DFD BR-08: gerak() = 1 tick, lempar() = 1 tick, lihat() = 0 tick.
 * Petak baris 0 adalah baris paling bawah; koordinat y bertambah ke Utara.
 */
enum class Arah { Utara, Timur, Selatan, Barat }

object Biaya {
    const val GERAK = 1
    const val LEMPAR = 1
    const val LIHAT = 0
}

data class Petak(val isi: String) {
    val kosong: Boolean get() = isi.isEmpty()
}

data class HasilLempar(val berhasil: Boolean, val isi: String, val sampah: Boolean)

class Dunia(val ukuran: Int, petak: List<String>) {
    private val grid: MutableList<Petak> = petak.map { Petak(it) }.toMutableList()
    private val awal = petak.toList()
    var x: Int = 0
        private set
    var y: Int = 0
        private set
    var ticks: Int = 0
        private set
    var tertangkap: Int = 0
        private set
    var sampah: Int = 0
        private set
    val tangkapan: MutableList<String> = mutableListOf()

    fun petakDi(px: Int, py: Int): Petak = grid[py * ukuran + px]

    fun gerak(arah: Arah): Boolean {
        ticks += Biaya.GERAK
        val nx = when (arah) {
            Arah.Timur -> x + 1
            Arah.Barat -> x - 1
            else -> x
        }
        val ny = when (arah) {
            Arah.Utara -> y + 1
            Arah.Selatan -> y - 1
            else -> y
        }
        if (nx !in 0 until ukuran || ny !in 0 until ukuran) return false
        x = nx
        y = ny
        return true
    }

    fun lempar(): HasilLempar {
        ticks += Biaya.LEMPAR
        val p = petakDi(x, y)
        if (p.kosong) return HasilLempar(false, "", false)
        if (p.isi == "sampah") {
            sampah++
            grid[y * ukuran + x] = Petak("")
            return HasilLempar(true, "sampah", true)
        }
        tertangkap++
        tangkapan.add(p.isi)
        grid[y * ukuran + x] = Petak("")
        return HasilLempar(true, p.isi, false)
    }

    fun lihat(): String = petakDi(x, y).isi.ifEmpty { "kosong" }

    val sisaIkan: Int get() = grid.count { !it.kosong && it.isi != "sampah" }

    fun sisaSpesies(spesies: String): Int = grid.count { it.isi == spesies }

    fun totalSpesies(spesies: String): Int = awal.count { it == spesies }

    val totalIkan: Int get() = awal.count { it.isNotEmpty() && it != "sampah" }
}

data class HasilJalan(
    val berhasil: Boolean,
    val ticks: Int,
    val tertangkap: Int,
    val keluaran: List<String>,
    val pesanError: String? = null,
    val trace: List<Langkah> = emptyList(),
    val tangkapan: List<String> = emptyList(),
    val sampah: Int = 0,
    val defDigunakan: Boolean = false,
    val langkah: Int = 0,
    val errorBaris: Int = 0,
)

fun interface CodeRunner {
    fun jalankan(kode: String, dunia: Dunia): HasilJalan
}

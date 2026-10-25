package com.example.codefishing.pemula

import com.example.codefishing.core.content.Stage

/**
 * TIM: Logika Mode Pemula. Satu sesi kuis untuk satu stage. Murni Kotlin (tanpa Compose/Android).
 * Layar Kuis (tim Frontend) hanya memanggil fungsi di sini dan menampilkan hasilnya.
 */
class QuizSession(val stage: Stage, private val ambangPersen: Int) {
    var indeks: Int = 0
        private set
    var jumlahBenar: Int = 0
        private set
    var jumlahSalah: Int = 0
        private set
    /** Jawaban benar beruntun saat ini (dipakai ikan Rare di 5, 10, 15 beruntun). */
    var beruntun: Int = 0
        private set
    var beruntunTerbaik: Int = 0
        private set

    val totalSoal: Int get() = stage.soal.size
    val selesai: Boolean get() = indeks >= totalSoal
    val soalSaatIni get() = stage.soal[indeks]

    data class Hasil(val benar: Boolean, val umpanBerkurang: Boolean, val keluaran: String)

    /** Jawab soal saat ini dengan opsi ke-[pilihan]. Lanjut ke soal berikutnya lewat [lanjut]. */
    fun jawab(pilihan: Int): Hasil {
        check(!selesai) { "Kuis sudah selesai" }
        val s = soalSaatIni
        val benar = pilihan == s.benar
        if (benar) {
            jumlahBenar++
            beruntun++
            if (beruntun > beruntunTerbaik) beruntunTerbaik = beruntun
        } else {
            jumlahSalah++
            beruntun = 0
        }
        return Hasil(benar, umpanBerkurang = !benar, keluaran = s.opsi[pilihan].keluaran)
    }

    fun lanjut() {
        if (!selesai) indeks++
    }

    val persen: Int get() = if (totalSoal == 0) 0 else jumlahBenar * 100 / totalSoal

    val lulus: Boolean get() = persen >= ambangPersen

    /** Tanpa kesalahan sama sekali: dasar kenaikan grade ikan Stage (Common -> Epic). */
    val sempurna: Boolean get() = selesai && jumlahSalah == 0
}

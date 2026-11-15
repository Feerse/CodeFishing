package com.example.codefishing

import com.example.codefishing.pemula.PilihanKuis
import com.example.codefishing.pemula.acakPilihan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AcakPilihanTest {
    private fun opsi() = listOf(
        PilihanKuis(11, "print", false),
        PilihanKuis(12, "echo", true),
        PilihanKuis(13, "show", false),
        PilihanKuis(14, "Print", false),
    )

    @Test
    fun semuaPilihanDanKunciTetapAda() {
        val hasil = acakPilihan(opsi(), soalId = 7)
        assertEquals(opsi().map { it.id }.toSet(), hasil.map { it.id }.toSet())
        assertEquals(1, hasil.count { it.benar })
        assertEquals("echo", hasil.first { it.benar }.teks)
    }

    @Test
    fun urutanSamaSetiapKaliSoalDibuka() {
        // Sesi tersimpan menyimpan indeks pilihan, jadi urutan tidak boleh berubah.
        assertEquals(acakPilihan(opsi(), 21), acakPilihan(opsi(), 21))
    }

    @Test
    fun posisiKunciTersebarMerata() {
        val jumlah = IntArray(4)
        (1..110).forEach { id ->
            val hasil = acakPilihan(opsi(), id)
            jumlah[hasil.indexOfFirst { it.benar }]++
        }
        jumlah.forEach { banyak ->
            val persen = banyak * 100.0 / 110
            assertTrue("Posisi kunci $persen% tidak wajar", persen in 15.0..35.0)
        }
    }

    @Test
    fun soalBerbedaTidakSelaluUrutanSama() {
        val urutan = (1..8).map { id -> acakPilihan(opsi(), id).map { it.id } }
        assertTrue(urutan.distinct().size > 1)
    }

    @Test
    fun satuPilihanTidakDiubah() {
        val tunggal = listOf(PilihanKuis(1, "hanya", true))
        assertEquals(tunggal, acakPilihan(tunggal, soalId = 99))
    }
}

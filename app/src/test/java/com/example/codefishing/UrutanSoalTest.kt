package com.example.codefishing

import com.example.codefishing.pemula.PilihanKuis
import com.example.codefishing.pemula.SoalKuis
import com.example.codefishing.pemula.urutanSoal
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class UrutanSoalTest {
    private fun soal(n: Int) = SoalKuis(
        id = n,
        teks = "Soal $n",
        kode = "",
        pembahasan = "",
        poin = 25,
        pilihan = listOf(PilihanKuis(n, "Benar", true)),
    )

    @Test
    fun percobaanPertamaTetapUrut() {
        val daftar = (1..5).map(::soal)
        assertEquals(daftar.map { it.id }, urutanSoal(daftar, ulangi = false).map { it.id })
    }

    @Test
    fun pengulanganMempertahankanSemuaSoal() {
        val daftar = (1..5).map(::soal)
        val hasil = urutanSoal(daftar, ulangi = true, random = Random(11))
        assertEquals(daftar.map { it.id }.toSet(), hasil.map { it.id }.toSet())
        assertEquals(daftar.size, hasil.size)
    }

    @Test
    fun satuSoalTidakPerluDiacak() {
        val daftar = listOf(soal(1))
        assertEquals(daftar, urutanSoal(daftar, ulangi = true))
    }

    @Test
    fun bossPercobaanPertamaDiacak() {
        val daftar = (1..10).map(::soal)
        val hasil = urutanSoal(daftar, ulangi = false, boss = true, random = Random(42))
        assertEquals(daftar.map { it.id }.toSet(), hasil.map { it.id }.toSet())
        // Probabilitas tetap urut = 1/10! sangat kecil, pasti berubah
        assert(hasil.map { it.id } != daftar.map { it.id })
    }

    @Test
    fun bossSelalu10Soal() {
        val daftar = (46..55).map(::soal)
        val hasil = urutanSoal(daftar, ulangi = true, boss = true, random = Random(123))
        assertEquals(10, hasil.size)
        assertEquals(daftar.map { it.id }.toSet(), hasil.map { it.id }.toSet())
    }
}
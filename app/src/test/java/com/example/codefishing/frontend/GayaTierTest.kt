package com.example.codefishing.frontend

import com.example.codefishing.frontend.components.BentukBingkai
import com.example.codefishing.frontend.components.Kelangkaan
import com.example.codefishing.frontend.components.gayaTier
import com.example.codefishing.frontend.components.kelangkaan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Menjaga agar tiap kelangkaan ikan punya identitas visual sendiri (bentuk, bukan hanya warna),
 * dan semakin langka semakin meriah.
 */
class GayaTierTest {
    private val semua = Kelangkaan.values().toList()

    @Test
    fun namaTierDipetakanKeKelangkaan() {
        assertEquals(Kelangkaan.BIASA, kelangkaan("biasa"))
        assertEquals(Kelangkaan.LANGKA, kelangkaan("langka"))
        assertEquals(Kelangkaan.EPIK, kelangkaan("epik"))
        assertEquals(Kelangkaan.LEGENDA, kelangkaan("legenda"))
        assertEquals(Kelangkaan.MITOS, kelangkaan("mitos"))
        assertEquals(Kelangkaan.BIASA, kelangkaan("tidak dikenal"))
    }

    @Test
    fun setiapTierPunyaBentukBingkaiYangBerbeda() {
        val bentuk = semua.map { gayaTier(it).bingkai }
        assertEquals(semua.size, bentuk.toSet().size)
    }

    @Test
    fun setiapTierPunyaKombinasiGayaYangUnik() {
        val kombinasi = semua.map {
            val g = gayaTier(it)
            listOf(g.bingkai, g.sudut, g.puncak, g.partikel)
        }
        assertEquals(semua.size, kombinasi.toSet().size)
    }

    @Test
    fun tierBiasaPolosTanpaEfek() {
        val g = gayaTier(Kelangkaan.BIASA)
        assertEquals(BentukBingkai.POLOS, g.bingkai)
        assertEquals(0, g.jumlahPartikel)
        assertEquals(0f, g.cahaya, 0f)
        assertTrue(!g.sinarBerputar && !g.sapuanKilap && !g.pusaran && !g.denyut)
    }

    @Test
    fun semakinLangkaSemakinMeriah() {
        val cahaya = semua.map { gayaTier(it).cahaya }
        assertEquals(cahaya.sorted(), cahaya)
        assertEquals(cahaya.size, cahaya.toSet().size)
        val partikel = semua.map { gayaTier(it).jumlahPartikel }
        assertEquals(partikel.sorted(), partikel)
        val tebal = semua.map { gayaTier(it).tebalDp }
        assertEquals(tebal.sorted(), tebal)
    }

    @Test
    fun efekKhasHanyaMilikTierPemiliknya() {
        assertTrue(gayaTier(Kelangkaan.LEGENDA).sinarBerputar && gayaTier(Kelangkaan.LEGENDA).sapuanKilap)
        assertTrue(gayaTier(Kelangkaan.MITOS).pusaran)
        assertTrue(gayaTier(Kelangkaan.EPIK).denyut)
        assertTrue(semua.filter { it != Kelangkaan.LEGENDA }.none { gayaTier(it).sinarBerputar })
        assertTrue(semua.filter { it != Kelangkaan.MITOS }.none { gayaTier(it).pusaran })
    }
}

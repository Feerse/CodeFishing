package com.example.codefishing.frontend

import com.example.codefishing.frontend.components.JenisAdegan
import com.example.codefishing.frontend.components.adeganBersalju
import com.example.codefishing.frontend.components.adeganStage
import com.example.codefishing.frontend.components.jenisAdeganStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdeganStageTest {
    private val jenisPasifik = (1..9).map { jenisAdeganStage(it, false) } + jenisAdeganStage(10, true)
    private val jenisAntartika = (11..19).map { jenisAdeganStage(it, false) } + jenisAdeganStage(20, true)

    @Test
    fun stagePasifikMemakaiAdeganYangSesuai() {
        assertEquals(JenisAdegan.Kapal, jenisAdeganStage(1, false))
        assertEquals(JenisAdegan.Dermaga, jenisAdeganStage(2, false))
        assertEquals(JenisAdegan.PerahuCadik, jenisAdeganStage(3, false))
        assertEquals(JenisAdegan.Selam, jenisAdeganStage(4, false))
        // Stage 5 dulu lubang es. Lubang es sekarang ciri khas Antartika, jadi Pasifik memakai kapal.
        assertEquals(JenisAdegan.Kapal, jenisAdeganStage(5, false))
        assertEquals(JenisAdegan.Bubu, jenisAdeganStage(6, false))
        assertEquals(JenisAdegan.PerahuCadik, jenisAdeganStage(7, false))
        assertEquals(JenisAdegan.Bubu, jenisAdeganStage(8, false))
        assertEquals(JenisAdegan.Selam, jenisAdeganStage(9, false))
        assertEquals(JenisAdegan.Badai, jenisAdeganStage(10, true))
    }

    @Test
    fun lubangEsHanyaAdaDiAntartika() {
        assertFalse(JenisAdegan.LubangEs in jenisPasifik)
        assertTrue(jenisAntartika.count { it == JenisAdegan.LubangEs } >= 2)
        assertEquals(JenisAdegan.LubangEs, jenisAdeganStage(12, false))
    }

    @Test
    fun stageAntartikaTidakMengganti_StudiKasus_HanyaMenambahDetail() {
        // Setiap studi kasus Antartika (selain lubang es) sudah ada di Pasifik; peta hanya menambah detail.
        val tanpaLubangEs = jenisAntartika.filter { it != JenisAdegan.LubangEs }.toSet()
        assertTrue(jenisPasifik.toSet().containsAll(tanpaLubangEs))
        assertEquals(JenisAdegan.Badai, jenisAdeganStage(20, true))
    }

    @Test
    fun antartikaMemakaiVarianEsDariAdeganYangSama() {
        // Kapal, dermaga, cadik, penyelam, bubu, dan badai punya varian *_antartika terpisah.
        assertNotEquals(adeganStage(1, false), adeganStage(11, false))
        assertNotEquals(adeganStage(2, false), adeganStage(13, false))
        assertNotEquals(adeganStage(3, false), adeganStage(17, false))
        assertNotEquals(adeganStage(4, false), adeganStage(14, false))
        assertNotEquals(adeganStage(6, false), adeganStage(16, false))
        assertNotEquals(adeganStage(10, true), adeganStage(20, true))
        // Pasifik tidak pernah memakai varian es.
        for (n in 1..10) assertFalse(adeganBersalju(n))
        for (n in 11..20) assertTrue(adeganBersalju(n))
    }

    @Test
    fun bossSelaluBadaiMeskiNomornyaBukanSepuluh() {
        assertEquals(JenisAdegan.Badai, jenisAdeganStage(1, true))
        assertEquals(JenisAdegan.Badai, jenisAdeganStage(99, true))
    }

    @Test
    fun adeganPenyelamTidakMemakaiAsetKapal() {
        assertNotEquals(adeganStage(1, false), adeganStage(4, false))
        assertNotEquals(adeganStage(1, false), adeganStage(9, false))
        assertEquals(adeganStage(4, false), adeganStage(9, false))
        assertEquals(adeganStage(14, false), adeganStage(18, false))
    }
}

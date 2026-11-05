package com.example.codefishing.frontend

import com.example.codefishing.frontend.components.JenisAdegan
import com.example.codefishing.frontend.components.adeganStage
import com.example.codefishing.frontend.components.jenisAdeganStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AdeganStageTest {
    @Test
    fun stagePasifikMemakaiAdeganYangSesuai() {
        assertEquals(JenisAdegan.Kapal, jenisAdeganStage(1, false))
        assertEquals(JenisAdegan.Dermaga, jenisAdeganStage(2, false))
        assertEquals(JenisAdegan.PerahuCadik, jenisAdeganStage(3, false))
        assertEquals(JenisAdegan.Selam, jenisAdeganStage(4, false))
        assertEquals(JenisAdegan.LubangEs, jenisAdeganStage(5, false))
        assertEquals(JenisAdegan.Bubu, jenisAdeganStage(6, false))
        assertEquals(JenisAdegan.PerahuCadik, jenisAdeganStage(7, false))
        assertEquals(JenisAdegan.Bubu, jenisAdeganStage(8, false))
        assertEquals(JenisAdegan.Selam, jenisAdeganStage(9, false))
        assertEquals(JenisAdegan.Badai, jenisAdeganStage(10, true))
    }

    @Test
    fun stageAntartikaTetapMemakaiKapalKecualiBoss() {
        for (nomor in 11..19) {
            assertEquals(JenisAdegan.Kapal, jenisAdeganStage(nomor, false))
        }
        assertEquals(JenisAdegan.Badai, jenisAdeganStage(20, true))
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
    }
}
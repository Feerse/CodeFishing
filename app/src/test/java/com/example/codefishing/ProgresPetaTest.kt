package com.example.codefishing

import com.example.codefishing.pemula.ProgresPeta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgresPetaTest {
    @Test
    fun antartikaBaruTerbukaSetelahSepuluhStagePasifikSelesai() {
        assertFalse(ProgresPeta.antartikaTerbuka(0))
        assertFalse(ProgresPeta.antartikaTerbuka(9))
        assertTrue(ProgresPeta.antartikaTerbuka(10))
        assertTrue(ProgresPeta.antartikaTerbuka(12))
    }

    @Test
    fun stageBerikutMelewatiBatasPasifikSampaiStageTerakhir() {
        assertEquals(2, ProgresPeta.nomorLanjut(1, 20))
        assertEquals(11, ProgresPeta.nomorLanjut(10, 20))
        assertEquals(20, ProgresPeta.nomorLanjut(19, 20))
        assertNull(ProgresPeta.nomorLanjut(20, 20))
    }

    @Test
    fun stageTerakhirTanpaPetaAntartikaTetapBerhenti() {
        assertEquals(10, ProgresPeta.nomorLanjut(9, 10))
        assertNull(ProgresPeta.nomorLanjut(10, 10))
    }

    @Test
    fun pembagianWilayahPeta() {
        assertEquals(10, ProgresPeta.STAGE_PASIFIK_TERAKHIR)
        assertEquals(11, ProgresPeta.STAGE_ANTARTIKA_PERTAMA)
        assertFalse(ProgresPeta.diAntartika(10))
        assertTrue(ProgresPeta.diAntartika(11))
        assertTrue(ProgresPeta.diAntartika(20))
    }
}

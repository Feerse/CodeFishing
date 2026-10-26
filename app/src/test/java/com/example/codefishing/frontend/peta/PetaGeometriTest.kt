package com.example.codefishing.frontend.peta

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PetaGeometriTest {
    @Test
    fun sepuluhStageTersusunDariAtasKeBawah() {
        assertEquals(10, PetaGeometri.stage.size)
        assertTrue(PetaGeometri.stage.all { it.y in 0f..PetaGeometri.TINGGI_DUNIA })
        for (i in 1 until PetaGeometri.stage.size) {
            assertTrue(
                "Stage ${i + 1} harus lebih ke selatan dari stage $i",
                PetaGeometri.stage[i].y > PetaGeometri.stage[i - 1].y,
            )
        }
        assertTrue(PetaGeometri.gerbang.y > PetaGeometri.stage.last().y)
    }

    @Test
    fun ruteSelesaiMengikutiJumlahStage() {
        assertEquals(1, PetaGeometri.ruteSelesai(0).size)
        assertEquals(4, PetaGeometri.ruteSelesai(3).size)
        assertEquals(11, PetaGeometri.ruteSelesai(10).size)
        assertEquals(11, PetaGeometri.ruteSelesai(99).size)
    }

    @Test
    fun nomorKapalMemakaiPosisiTerakhirLaluStageAktif() {
        assertEquals(7, PetaGeometri.nomorKapal(7, 3))
        assertEquals(3, PetaGeometri.nomorKapal(null, 3))
        assertEquals(1, PetaGeometri.nomorKapal(null, null))
        assertEquals(4, PetaGeometri.nomorKapal(99, 4))
    }

    @Test
    fun semuaTitikMasihDiDalamPeta() {
        PetaGeometri.ruteLengkap().forEach {
            assertTrue("x ${it.x}", it.x in 0f..PetaGeometri.LEBAR_DUNIA)
            assertTrue("y ${it.y}", it.y in 0f..PetaGeometri.TINGGI_DUNIA)
        }
    }

    @Test
    fun asetSvgPetaTersediaDanBerdimensiBenar() {
        val svg = listOf(
            File("src/main/assets/peta/peta_perairan.svg"),
            File("app/src/main/assets/peta/peta_perairan.svg"),
            File("../app/src/main/assets/peta/peta_perairan.svg"),
        ).firstOrNull { it.exists() }
        assertTrue("peta_perairan.svg harus ada di assets", svg != null)
        val teks = svg!!.readText()
        assertTrue(teks.contains("viewBox=\"0 0 1000 2800\""))
        assertTrue(teks.contains("width=\"1000\""))
        assertTrue(teks.contains("height=\"2800\""))
    }
}

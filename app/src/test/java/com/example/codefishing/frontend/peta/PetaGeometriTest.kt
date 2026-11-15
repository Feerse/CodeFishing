package com.example.codefishing.frontend.peta

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        // 10 stage selesai: garis progres menyambung ke gerbang Antartika.
        assertEquals(12, PetaGeometri.ruteSelesai(10).size)
        assertEquals(PetaGeometri.gerbang, PetaGeometri.ruteSelesai(10).last())
        assertEquals(12, PetaGeometri.ruteSelesai(99).size)
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
    fun sepuluhStageAntartikaTersusunDariUtaraKeSelatan() {
        assertEquals(10, PetaGeometri.stageAntartika.size)
        assertTrue(PetaGeometri.stageAntartika.all { it.y in 0f..PetaGeometri.TINGGI_DUNIA })
        for (i in 1 until PetaGeometri.stageAntartika.size) {
            assertTrue(
                "Stage Antartika ${i + 1} harus lebih ke selatan dari stage $i",
                PetaGeometri.stageAntartika[i].y > PetaGeometri.stageAntartika[i - 1].y,
            )
        }
        assertTrue(PetaGeometri.mulaiAntartika.y < PetaGeometri.stageAntartika.first().y)
        assertTrue(PetaGeometri.ujungAntartika.y > PetaGeometri.stageAntartika.last().y)
    }

    @Test
    fun ruteAntartikaMengikutiJumlahStage() {
        assertEquals(1, PetaGeometri.ruteSelesaiAntartika(0).size)
        assertEquals(4, PetaGeometri.ruteSelesaiAntartika(3).size)
        // 10 stage Antartika selesai: garis progres menyambung ke ujung selatan (Bermuda).
        assertEquals(12, PetaGeometri.ruteSelesaiAntartika(10).size)
        assertEquals(PetaGeometri.ujungAntartika, PetaGeometri.ruteSelesaiAntartika(10).last())
        assertEquals(12, PetaGeometri.ruteSelesaiAntartika(99).size)
        assertEquals(12, PetaGeometri.ruteLengkapAntartika().size)
    }

    @Test
    fun titikStageAntartikaMemakaiNomorLokal() {
        assertEquals(PetaGeometri.stageAntartika.first(), PetaGeometri.titikStageAntartika(1))
        assertEquals(PetaGeometri.stageAntartika.last(), PetaGeometri.titikStageAntartika(10))
        assertNull(PetaGeometri.titikStageAntartika(0))
        assertNull(PetaGeometri.titikStageAntartika(11))
    }

    @Test
    fun asetPetaTersediaDanBerdimensiBenar() {
        val peta = cariAset("peta_perairan.webp")
        assertTrue("peta_perairan.webp harus ada di assets", peta != null)
        assertEquals("Peta Pasifik harus 1000x2800", 1000 to 2800, ukuranWebp(peta!!))
    }

    @Test
    fun asetPetaAntartikaTersediaDanBerdimensiBenar() {
        listOf("peta_antartika.webp", "peta_antartika_terang.webp").forEach { nama ->
            val berkas = cariAset(nama)
            assertTrue("$nama harus ada di assets", berkas != null)
            assertEquals("$nama harus 1000x2800", 1000 to 2800, ukuranWebp(berkas!!))
        }
    }

    /**
     * Peta dirender sekali dari SVG saat pembuatan aset lalu disimpan sebagai WebP
     * (lihat 2_Aset_dan_Konten/tools/optimalkan_aset.py), jadi yang diperiksa di sini berkas WebP-nya.
     */
    private fun cariAset(nama: String): File? = listOf(
        File("src/main/assets/peta/$nama"),
        File("app/src/main/assets/peta/$nama"),
        File("../app/src/main/assets/peta/$nama"),
    ).firstOrNull { it.exists() }

    /** Membaca lebar x tinggi dari header WebP tanpa decoder. Null bila berkas bukan WebP valid. */
    private fun ukuranWebp(berkas: File): Pair<Int, Int>? {
        val b = berkas.readBytes()
        if (b.size < 30) return null
        fun teks(dari: Int, sampai: Int) = String(b, dari, sampai - dari, Charsets.US_ASCII)
        if (teks(0, 4) != "RIFF" || teks(8, 12) != "WEBP") return null
        return when (val chunk = teks(12, 16)) {
            // Lossy: 3 byte frame tag, kode mulai 9d 01 2a, lalu 14 bit lebar dan 14 bit tinggi.
            "VP8 " -> {
                if (b[23] != 0x9d.toByte() || b[24] != 0x01.toByte() || b[25] != 0x2a.toByte()) return null
                val w = (b[26].toInt() and 0xFF) or ((b[27].toInt() and 0xFF) shl 8)
                val h = (b[28].toInt() and 0xFF) or ((b[29].toInt() and 0xFF) shl 8)
                (w and 0x3FFF) to (h and 0x3FFF)
            }
            // Extended: lebar-1 dan tinggi-1 masing-masing 24 bit.
            "VP8X" -> {
                fun tiga(awal: Int) = (b[awal].toInt() and 0xFF) or
                    ((b[awal + 1].toInt() and 0xFF) shl 8) or
                    ((b[awal + 2].toInt() and 0xFF) shl 16)
                (tiga(24) + 1) to (tiga(27) + 1)
            }
            // Lossless: 0x2f, lalu 14 bit lebar-1 dan 14 bit tinggi-1.
            "VP8L" -> {
                if ((b[20].toInt() and 0xFF) != 0x2F) return null
                val bit = (b[21].toInt() and 0xFF) or
                    ((b[22].toInt() and 0xFF) shl 8) or
                    ((b[23].toInt() and 0xFF) shl 16) or
                    ((b[24].toInt() and 0xFF) shl 24)
                ((bit and 0x3FFF) + 1) to (((bit ushr 14) and 0x3FFF) + 1)
            }
            else -> null
        }
    }

    @Test
    fun gerbangBermudaAdalahUjungRuteAntartikaDanDiBawahStageTerakhir() {
        val b = PetaGeometri.gerbangBermuda
        assertEquals(PetaGeometri.ujungAntartika, b)
        assertEquals(b, PetaGeometri.ruteLengkapAntartika().last())
        assertTrue(b.y > PetaGeometri.stageAntartika.last().y)
        assertTrue(b.x in 0f..PetaGeometri.LEBAR_DUNIA)
        // Label (+34dp) dan teks "PERAIRAN BERIKUTNYA" di y=2700 harus tetap dalam dunia peta.
        assertTrue(b.y < 2700f && b.y < PetaGeometri.TINGGI_DUNIA)
    }
}

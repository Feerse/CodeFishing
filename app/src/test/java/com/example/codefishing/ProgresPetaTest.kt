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

    private data class Baris(val nomor: Int, val status: String, val terbuka: Boolean)

    private fun stageAktif(daftar: List<Baris>): Baris? =
        ProgresPeta.stageAktif(daftar, { it.status }, { it.terbuka })

    @Test
    fun stageAktifMemilihYangBelumSelesaiDanTerbuka() {
        val daftar = listOf(
            Baris(1, "selesai", true),
            Baris(2, "selesai", true),
            Baris(3, "terkunci", true),
            Baris(4, "terkunci", false),
        )
        assertEquals(3, stageAktif(daftar)?.nomor)
    }

    @Test
    fun stageAktifMelewatiStageYangMasihTerkunci() {
        // Stage 2 belum selesai tetapi belum terbuka; yang dipilih harus stage 3 yang sudah terbuka.
        val daftar = listOf(
            Baris(1, "selesai", true),
            Baris(2, "terkunci", false),
            Baris(3, "terkunci", true),
        )
        assertEquals(3, stageAktif(daftar)?.nomor)
    }

    @Test
    fun semuaSelesaiMemakaiStageTerakhir() {
        val daftar = listOf(
            Baris(1, "selesai", true),
            Baris(2, "selesai", true),
        )
        assertEquals(2, stageAktif(daftar)?.nomor)
    }

    @Test
    fun daftarKosongTidakPunyaStageAktif() {
        assertNull(stageAktif(emptyList()))
    }

    @Test
    fun acuanPetaMemakaiProgresTerjauh() {
        // Mengulang stage Pasifik (posisi 2) tidak boleh menarik peta kembali ke Pasifik
        // saat progres sudah di Antartika (stage aktif 15).
        assertEquals(15, ProgresPeta.nomorAcuanPeta(15, 2))
        assertEquals(15, ProgresPeta.nomorAcuanPeta(2, 15))
        assertEquals(7, ProgresPeta.nomorAcuanPeta(7, null))
        assertEquals(7, ProgresPeta.nomorAcuanPeta(null, 7))
        assertNull(ProgresPeta.nomorAcuanPeta(null, null))
    }

    @Test
    fun petaAwalIkutProgresDanKunciAntartika() {
        assertFalse(ProgresPeta.petaAntartika(false, 20))
        assertFalse(ProgresPeta.petaAntartika(true, 10))
        assertFalse(ProgresPeta.petaAntartika(true, null))
        assertTrue(ProgresPeta.petaAntartika(true, 11))
        assertTrue(ProgresPeta.petaAntartika(true, 20))
    }
}

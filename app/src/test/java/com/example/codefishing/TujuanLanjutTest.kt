package com.example.codefishing

import com.example.codefishing.pemula.JenisLanjut
import com.example.codefishing.pemula.MODE_BELAJAR
import com.example.codefishing.pemula.MODE_EXPERT
import com.example.codefishing.pemula.TujuanLanjut
import com.example.codefishing.pemula.tentukanTujuanLanjut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tombol "Lanjutkan" di Home tidak boleh berakhir di Pilih Mode selama masih ada stage yang bisa
 * dilanjutkan; hanya "Mulai Main" yang membuka Pilih Mode.
 */
class TujuanLanjutTest {
    @Test
    fun modeExpertKembaliKeSandbox() {
        assertEquals(
            TujuanLanjut(JenisLanjut.Sandbox),
            tentukanTujuanLanjut(
                modeTerakhir = MODE_EXPERT,
                posisiTerakhirId = 7,
                posisiTerakhirBoss = false,
                stageAktifId = 7,
                stageAktifBoss = false,
            ),
        )
    }

    @Test
    fun posisiTerakhirMenangAtasStageAktif() {
        val tujuan = tentukanTujuanLanjut(
            modeTerakhir = MODE_BELAJAR,
            posisiTerakhirId = 4,
            posisiTerakhirBoss = false,
            stageAktifId = 9,
            stageAktifBoss = false,
        )
        assertEquals(JenisLanjut.Stage, tujuan.jenis)
        assertEquals(4, tujuan.stageId)
        assertEquals(false, tujuan.boss)
    }

    @Test
    fun stageBossDipertahankan() {
        val tujuan = tentukanTujuanLanjut(
            modeTerakhir = MODE_BELAJAR,
            posisiTerakhirId = 10,
            posisiTerakhirBoss = true,
            stageAktifId = null,
            stageAktifBoss = false,
        )
        assertEquals(JenisLanjut.Stage, tujuan.jenis)
        assertEquals(10, tujuan.stageId)
        assertEquals(true, tujuan.boss)
    }

    @Test
    fun posisiKosongMemakaiStageAktif() {
        val tujuan = tentukanTujuanLanjut(
            modeTerakhir = MODE_BELAJAR,
            posisiTerakhirId = null,
            posisiTerakhirBoss = false,
            stageAktifId = 6,
            stageAktifBoss = false,
        )
        assertEquals(JenisLanjut.Stage, tujuan.jenis)
        assertEquals(6, tujuan.stageId)
    }

    @Test
    fun posisiDanStageAktifKosongBaruKePilihMode() {
        val tujuan = tentukanTujuanLanjut(
            modeTerakhir = null,
            posisiTerakhirId = null,
            posisiTerakhirBoss = false,
            stageAktifId = null,
            stageAktifBoss = false,
        )
        assertEquals(JenisLanjut.PilihMode, tujuan.jenis)
        assertNull(tujuan.stageId)
    }
}

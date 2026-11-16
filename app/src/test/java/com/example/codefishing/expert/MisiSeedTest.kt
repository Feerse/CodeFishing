package com.example.codefishing.expert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Uji langsung terhadap `seed/misi.json` yang dipakai aplikasi.
 *
 * Berbeda dari `SandboxEngineTest` yang memakai papan buatan, uji ini membaca berkas asli supaya
 * kesalahan pada data misi (nomor dobel, kode awal yang error, par yang mustahil) tertangkap
 * sebelum masuk ke APK.
 */
class MisiSeedTest {
    private val misi: List<MisiPeta> by lazy {
        val dir = listOf(
            File("src/main/assets/seed"),
            File("app/src/main/assets/seed"),
            File("../app/src/main/assets/seed"),
        ).first { it.exists() }
        SandboxContent.dariJson(File(dir, "misi.json").readText())
    }

    /** Jumlah ikan yang wajib ditangkap; satu ikan selalu butuh satu `lempar()`. */
    private fun wajibTangkap(m: MisiPeta): Int = when (m.goalTipe) {
        "catch" -> m.goalN
        "only" -> m.cells.count { it == (m.goalSpesies ?: "") }
        "all", "def", "cetak" -> m.cells.count { it.isNotEmpty() && it != "sampah" }
        else -> 0
    }

    @Test
    fun nomorKodeDanUrutanUnikBerurutan() {
        assertEquals(11, misi.size)
        assertEquals((1..11).toList(), misi.map { it.nomor })
        assertEquals(11, misi.map { it.kode }.toSet().size)
        // Mode Bebas selalu terakhir supaya tombol "misi berikutnya" berhenti di situ.
        assertEquals("bebas", misi.last().kode)
    }

    @Test
    fun kodeAwalSetiapMisiBerjalanTanpaError() {
        val engine = SandboxEngine()
        misi.forEach { m ->
            val run = engine.jalankan(m.kodeAwal, m)
            assertTrue("${m.kode}: ${run.hasil.pesanError}", run.hasil.berhasil)
        }
    }

    @Test
    fun setiapGoalMemakaiTipeYangDikenal() {
        val dikenal = setOf("catch", "all", "only", "def", "cetak", "free")
        misi.forEach { m ->
            assertTrue("${m.kode}: ${m.goalTipe}", m.goalTipe in dikenal)
        }
    }

    @Test
    fun parSesuaiKalibrasiDesain() {
        // Par hasil kalibrasi rute terpendek: 3 bintang menuntut program yang rapi, sedangkan
        // sapuan naif (mis. selalu balik ke kolom 0) hanya mendapat 2 bintang.
        val harapan = mapOf(
            "m1" to 2,
            "m2" to 11,
            "m3" to 38,
            "m4" to 38,
            "m5" to 40,
            "m6" to 38,
            "m7" to 12,
            "m8" to 28,
            "m9" to 9,
            "m10" to 26,
            "bebas" to null,
        )
        assertEquals(harapan.keys, misi.map { it.kode }.toSet())
        misi.forEach { m ->
            assertEquals("par ${m.kode}", harapan[m.kode], m.par)
        }
    }

    @Test
    fun parTidakDiBawahJumlahIkanWajib() {
        misi.forEach { m ->
            val par = m.par ?: return@forEach
            val wajib = wajibTangkap(m)
            assertTrue("${m.kode}: par $par < wajib $wajib", par >= wajib)
        }
    }

    @Test
    fun misiBaruMengajarkanTopikYangDijanjikan() {
        val perKode = misi.associateBy { it.kode }
        // m7 while, m8 jumlah(), m9 get_x()/get_y(), m10 print().
        assertTrue(perKode.getValue("m7").kodeAwal.contains("while"))
        assertTrue(perKode.getValue("m8").kodeAwal.contains("jumlah()"))
        assertTrue(perKode.getValue("m9").kodeAwal.contains("get_x()"))
        assertTrue(perKode.getValue("m10").kodeAwal.contains("print("))
        assertEquals("cetak", perKode.getValue("m10").goalTipe)
        assertEquals("free", perKode.getValue("bebas").goalTipe)
    }
}

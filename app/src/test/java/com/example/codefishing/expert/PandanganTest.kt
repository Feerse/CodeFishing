package com.example.codefishing.expert

import com.example.codefishing.expert.py.Langkah
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PandanganTest {
    private fun langkah(tick: Int, x: Int, y: Int, aksi: String) =
        Langkah(tick, x, y, aksi, 1, null)

    @Test
    fun papanTertutupSebelumAdaLangkah() {
        assertTrue(Pandangan.petakTerbuka(emptyList(), 4).isEmpty())
    }

    @Test
    fun hanyaLihatDanLemparYangMembukaPetak() {
        val trace = listOf(
            langkah(1, 0, 0, "gerak"),
            langkah(2, 1, 0, "lihat"),
            langkah(3, 2, 0, "lempar"),
        )
        // gerak() tidak membuka apa pun; lihat/lempar membuka petaknya sendiri.
        assertEquals(setOf(1, 2), Pandangan.petakTerbuka(trace, 4))
    }

    @Test
    fun indeksMengikutiBarisKaliUkuran() {
        val trace = listOf(
            langkah(1, 0, 2, "lihat"),
            langkah(2, 3, 1, "lempar"),
        )
        // y * ukuran + x pada papan 4x4.
        assertEquals(setOf(8, 7), Pandangan.petakTerbuka(trace, 4))
    }

    @Test
    fun sampaiTickMengikutiJalannyaAnimasi() {
        val trace = listOf(
            langkah(1, 0, 0, "lihat"),
            langkah(5, 1, 0, "lihat"),
            langkah(9, 2, 0, "lempar"),
        )
        assertEquals(setOf(0), Pandangan.petakTerbuka(trace, 3, sampaiTick = 1))
        assertEquals(setOf(0, 1), Pandangan.petakTerbuka(trace, 3, sampaiTick = 5))
        assertEquals(setOf(0, 1, 2), Pandangan.petakTerbuka(trace, 3, sampaiTick = 9))
        assertEquals(emptySet<Int>(), Pandangan.petakTerbuka(trace, 3, sampaiTick = 0))
    }

    @Test
    fun koordinatDiLuarPapanDiabaikan() {
        val trace = listOf(
            langkah(1, 4, 0, "lihat"),
            langkah(2, 0, 9, "lempar"),
            langkah(3, 0, 0, "lihat"),
        )
        assertEquals(setOf(0), Pandangan.petakTerbuka(trace, 4))
        assertTrue(Pandangan.petakTerbuka(trace, 0).isEmpty())
    }
}

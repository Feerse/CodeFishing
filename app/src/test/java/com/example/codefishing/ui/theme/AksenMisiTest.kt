package com.example.codefishing.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AksenMisiTest {
    @Test
    fun sebelasMisiPunyaSebelasWarnaBerbeda() {
        for (gelap in listOf(true, false)) {
            val warna = (1..11).map { aksenMisi(it, gelap) }
            assertEquals(11, warna.toSet().size)
        }
    }

    @Test
    fun nomorDiLuarJangkauanDiputar() {
        assertEquals(aksenMisi(1, true), aksenMisi(12, true))
        assertEquals(aksenMisi(11, true), aksenMisi(0, true))
        assertEquals(aksenMisi(1, false), aksenMisi(23, false))
    }

    @Test
    fun keduaTemaMemakaiPaletBerbeda() {
        val gelap = (1..11).map { aksenMisi(it, true) }
        val terang = (1..11).map { aksenMisi(it, false) }
        assertTrue(gelap.intersect(terang.toSet()).isEmpty())
    }
}

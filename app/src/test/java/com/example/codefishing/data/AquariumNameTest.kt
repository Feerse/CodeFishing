package com.example.codefishing.data

import com.example.codefishing.core.data.MAKS_NAMA_AKUARIUM
import com.example.codefishing.core.data.normalisasiNamaAkuarium
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AquariumNameTest {
    @Test
    fun spasiDirapikan() {
        assertEquals("Si Kumis", normalisasiNamaAkuarium("  Si Kumis  "))
    }

    @Test
    fun namaKosongBerartiNamaAsli() {
        assertNull(normalisasiNamaAkuarium("   "))
    }

    @Test
    fun namaTerlaluPanjangDipotong() {
        val hasil = normalisasiNamaAkuarium("a".repeat(60))
        assertEquals(MAKS_NAMA_AKUARIUM, hasil?.length)
    }
}
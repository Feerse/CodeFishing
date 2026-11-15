package com.example.codefishing

import com.example.codefishing.pemula.minimalBenar
import org.junit.Assert.assertEquals
import org.junit.Test

class AturanLulusTest {
    @Test
    fun stageBiasaButuhEmpatDariLima() {
        assertEquals(4, minimalBenar(5, 70))
    }

    @Test
    fun bossButuhDelapanDariSepuluh() {
        assertEquals(8, minimalBenar(10, 80))
    }

    @Test
    fun pembulatanSelaluKeAtas() {
        assertEquals(2, minimalBenar(4, 50))
        assertEquals(1, minimalBenar(1, 100))
        assertEquals(7, minimalBenar(10, 61))
    }

    @Test
    fun totalKosongAman() {
        assertEquals(0, minimalBenar(0, 70))
    }
}

package com.example.codefishing

import com.example.codefishing.pemula.xpJawabanBenar
import org.junit.Assert.assertEquals
import org.junit.Test

class PoinSoalTest {
    @Test
    fun xpMengikutiPoinSoal() {
        assertEquals(25, xpJawabanBenar(25))
        assertEquals(20, xpJawabanBenar(20))
    }

    @Test
    fun poinTidakValidTidakMemberiXp() {
        assertEquals(0, xpJawabanBenar(0))
        assertEquals(0, xpJawabanBenar(-5))
    }
}

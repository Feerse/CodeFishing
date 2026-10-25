package com.example.codefishing

import com.example.codefishing.expert.Scoring
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {
    @Test fun tigaBintangBilaDiBawahAtauSamaDenganPar() {
        assertEquals(3, Scoring.bintang(14, 14))
        assertEquals(3, Scoring.bintang(5, 14))
    }

    @Test fun duaBintangSampaiSeperempatLebihPanjang() {
        // ceil(14 x 1,4) = 20
        assertEquals(2, Scoring.bintang(20, 14))
        assertEquals(1, Scoring.bintang(21, 14))
    }

    @Test fun tanpaParSelaluTiga() {
        assertEquals(3, Scoring.bintang(999, null))
    }
}

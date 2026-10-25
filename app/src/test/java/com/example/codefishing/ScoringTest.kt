package com.example.codefishing

import com.example.codefishing.expert.Scoring
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {
    @Test fun tigaBintangBilaDiBawahAtauSamaDenganPar() {
        assertEquals(3, Scoring.bintang(14, 14))
        assertEquals(3, Scoring.bintang(5, 14))
    }

    @Test fun duaBintangSampaiSetengahLebihPanjang() {
        // Sesuai DFD BR-09: batas 2 bintang = ceil(par x 1,5) = 21.
        assertEquals(2, Scoring.bintang(21, 14))
        assertEquals(1, Scoring.bintang(22, 14))
    }

    @Test fun tanpaParSelaluTiga() {
        assertEquals(3, Scoring.bintang(999, null))
    }
}

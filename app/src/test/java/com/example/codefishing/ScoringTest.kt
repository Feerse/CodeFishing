package com.example.codefishing

import com.example.codefishing.expert.Scoring
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test fun xpPerEkorHanyaSekaliDanHanyaSaatSukses() {
        val mujair = 7
        // Run pertama misi berhasil: XP dibayar.
        assertTrue(Scoring.xpPerEkorBoleh(sukses = true, sudahDibayar = emptySet(), ikanId = mujair))
        // Run berikutnya untuk misi yang sama: spesies ini sudah dibayar, tidak lagi.
        assertFalse(Scoring.xpPerEkorBoleh(sukses = true, sudahDibayar = setOf(mujair), ikanId = mujair))
        // Spesies lain di misi yang sama tetap boleh.
        assertTrue(Scoring.xpPerEkorBoleh(sukses = true, sudahDibayar = setOf(mujair), ikanId = 8))
        // Misi gagal tidak membayar XP walau spesiesnya baru.
        assertFalse(Scoring.xpPerEkorBoleh(sukses = false, sudahDibayar = emptySet(), ikanId = mujair))
    }
}

package com.example.codefishing

import com.example.codefishing.pemula.BaitRules
import com.example.codefishing.pemula.BaitState
import org.junit.Assert.assertEquals
import org.junit.Test

class BaitRulesTest {
    private val max = 5
    private val regen = 900_000L

    @Test fun pakaiMengurangiDanMulaiHitungan() {
        val s = BaitRules.pakai(BaitState(5, null), 1000L, max, regen)
        assertEquals(BaitState(4, 1000L), s)
    }

    @Test fun pulihSatuUmpanSetelahLimaBelasMenit() {
        val s = BaitRules.pulihkan(BaitState(3, 0L), regen, max, regen)
        assertEquals(BaitState(4, regen), s)
    }

    @Test fun pulihPenuhMenghapusPenanda() {
        val s = BaitRules.pulihkan(BaitState(3, 0L), regen * 10, max, regen)
        assertEquals(BaitState(5, null), s)
    }

    @Test fun tidakTurunDiBawahNol() {
        val s = BaitRules.pakai(BaitState(0, 0L), 10L, max, regen)
        assertEquals(0, s.jumlah)
    }

    @Test fun sisaWaktuBenar() {
        assertEquals(regen - 1000L, BaitRules.sisaMs(BaitState(2, 0L), 1000L, max, regen))
    }
}

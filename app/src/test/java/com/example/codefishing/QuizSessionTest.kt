package com.example.codefishing

import com.example.codefishing.core.content.Opsi
import com.example.codefishing.core.content.Soal
import com.example.codefishing.core.content.Stage
import com.example.codefishing.pemula.QuizSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizSessionTest {
    private fun soal() = Soal(
        "t", "k", listOf(Opsi("a", "A"), Opsi("b", "B")), benar = 0, adegan = "", variasi = "", studiKasus = "",
    )

    private fun stage(n: Int) = Stage("T", "Teri", "x", "c", "o", false, List(n) { soal() })

    @Test fun lulusBilaPersenMencapaiAmbang() {
        val q = QuizSession(stage(10), 70)
        repeat(10) { i -> q.jawab(if (i < 7) 0 else 1); q.lanjut() }
        assertEquals(70, q.persen)
        assertTrue(q.lulus)
        assertFalse(q.sempurna)
    }

    @Test fun salahMemutuskanBeruntun() {
        val q = QuizSession(stage(3), 70)
        q.jawab(0); q.lanjut()
        q.jawab(1); q.lanjut()
        q.jawab(0); q.lanjut()
        assertEquals(1, q.beruntun)
        assertEquals(1, q.beruntunTerbaik)
    }

    @Test fun jawabSalahMengurangiUmpan() {
        val q = QuizSession(stage(1), 70)
        assertTrue(q.jawab(1).umpanBerkurang)
    }
}

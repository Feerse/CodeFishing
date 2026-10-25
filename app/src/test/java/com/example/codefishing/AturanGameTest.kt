package com.example.codefishing

import com.example.codefishing.pemula.FishRules
import com.example.codefishing.pemula.LevelRules
import com.example.codefishing.pemula.PilihanKuis
import com.example.codefishing.pemula.QuizEngine
import com.example.codefishing.pemula.SoalKuis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AturanGameTest {
    private fun soal(id: Int, benar: Int) = SoalKuis(
        id = id,
        teks = "Soal $id",
        kode = "",
        pembahasan = "Pembahasan",
        poin = 25,
        pilihan = (0..3).map { PilihanKuis(it, "Pilihan $it", it == benar) },
    )

    @Test
    fun bossLulusDenganEmpatDariLima() {
        val engine = QuizEngine((1..5).map { soal(it, 0) }, skorLulus = 80)
        repeat(4) { engine.pilih(0) }
        engine.pilih(1)
        assertEquals(80, engine.persen)
        assertTrue(engine.lulus)
        assertFalse(engine.sempurna)
    }

    @Test
    fun bossGagalDenganTigaDariLima() {
        val engine = QuizEngine((1..5).map { soal(it, 0) }, skorLulus = 80)
        repeat(3) { engine.pilih(0) }
        repeat(2) { engine.pilih(1) }
        assertEquals(60, engine.persen)
        assertFalse(engine.lulus)
    }

    @Test
    fun comboBonusTigaEmpatLima() {
        assertEquals(emptyList<String>(), FishRules.bonusCombo(2))
        assertEquals(listOf("Kerapu"), FishRules.bonusCombo(3))
        assertEquals(listOf("Kerapu", "Barakuda"), FishRules.bonusCombo(4))
        assertEquals(listOf("Kerapu", "Barakuda", "Tenggiri"), FishRules.bonusCombo(5))
    }

    @Test
    fun levelDanProgres() {
        assertEquals(1, LevelRules.levelUntukXp(0))
        assertEquals(1, LevelRules.levelUntukXp(99))
        assertEquals(2, LevelRules.levelUntukXp(100))
        assertEquals(0.5f, LevelRules.progresLevel(150), 0.001f)
    }

    @Test
    fun tierNaikDanTidakMelewatiMitos() {
        assertEquals("langka", FishRules.naikTier("biasa"))
        assertEquals("mitos", FishRules.naikTier("mitos"))
    }

    @Test
    fun bobotBeradaDalamRentang() {
        assertEquals(0.8, FishRules.bobot(1.0, 0.0), 0.001)
        assertEquals(1.2, FishRules.bobot(1.0, 1.0), 0.001)
    }
}

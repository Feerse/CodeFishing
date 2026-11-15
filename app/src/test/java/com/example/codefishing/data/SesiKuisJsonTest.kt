package com.example.codefishing.data

import com.example.codefishing.core.data.SesiKuis
import com.example.codefishing.core.data.keJson
import com.example.codefishing.core.data.sesiKuisDariJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SesiKuisJsonTest {
    @Test
    fun sesiKuisBertahanSetelahSerialisasi() {
        val sesi = SesiKuis(
            stageId = 4,
            soalIds = listOf(16, 17, 18, 19, 20),
            indeks = 2,
            benar = 2,
            salah = 0,
            combo = 2,
            comboMaks = 2,
            xp = 50,
            terpilih = 1,
            jawabanBenar = true,
        )
        assertEquals(sesi, sesiKuisDariJson(sesi.keJson()))
    }

    @Test
    fun dataRusakDiabaikan() {
        assertNull(sesiKuisDariJson("bukan json"))
    }
}
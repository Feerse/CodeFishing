package com.example.codefishing

import com.example.codefishing.core.data.EfekVisual
import com.example.codefishing.core.data.hemat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EfekVisualTest {
    @Test
    fun nilaiTersimpanDibacaKembali() {
        assertEquals(EfekVisual.Otomatis, EfekVisual.dari("otomatis"))
        assertEquals(EfekVisual.Penuh, EfekVisual.dari("penuh"))
        assertEquals(EfekVisual.Hemat, EfekVisual.dari("hemat"))
    }

    @Test
    fun nilaiTidakDikenalKembaliKeOtomatis() {
        assertEquals(EfekVisual.Otomatis, EfekVisual.dari(null))
        assertEquals(EfekVisual.Otomatis, EfekVisual.dari(""))
        assertEquals(EfekVisual.Otomatis, EfekVisual.dari("ultra"))
    }

    @Test
    fun hanyaHematYangMengurangiEfek() {
        assertTrue(EfekVisual.Hemat.hemat)
        assertFalse(EfekVisual.Penuh.hemat)
        assertFalse(EfekVisual.Otomatis.hemat)
    }

    @Test
    fun setiapPilihanPunyaLabelDanKeterangan() {
        assertEquals(3, EfekVisual.entries.size)
        EfekVisual.entries.forEach {
            assertTrue(it.label.isNotBlank())
            assertTrue(it.keterangan.isNotBlank())
        }
    }
}

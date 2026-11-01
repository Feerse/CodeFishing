package com.example.codefishing.data

import com.example.codefishing.core.data.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun nilaiTersimpanDibacaKembali() {
        assertEquals(ThemeMode.Sistem, ThemeMode.dari("sistem"))
        assertEquals(ThemeMode.Terang, ThemeMode.dari("terang"))
        assertEquals(ThemeMode.Gelap, ThemeMode.dari("gelap"))
    }

    @Test
    fun nilaiTidakDikenalKembaliKeSistem() {
        assertEquals(ThemeMode.Sistem, ThemeMode.dari(null))
        assertEquals(ThemeMode.Sistem, ThemeMode.dari(""))
        assertEquals(ThemeMode.Sistem, ThemeMode.dari("acak"))
    }

    @Test
    fun modeSistemMengikutiPerangkat() {
        assertTrue(ThemeMode.Sistem.gelap(sistemGelap = true))
        assertFalse(ThemeMode.Sistem.gelap(sistemGelap = false))
    }

    @Test
    fun pilihanManualMengalahkanPerangkat() {
        assertFalse(ThemeMode.Terang.gelap(sistemGelap = true))
        assertTrue(ThemeMode.Gelap.gelap(sistemGelap = false))
    }
}
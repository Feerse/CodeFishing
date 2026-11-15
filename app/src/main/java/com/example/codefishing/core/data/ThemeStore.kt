package com.example.codefishing.core.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Pilihan tema tampilan aplikasi. */
enum class ThemeMode(val nilai: String, val label: String) {
    Sistem("sistem", "Sistem"),
    Terang("terang", "Terang"),
    Gelap("gelap", "Gelap");

    /** Apakah mode ini memakai palet gelap pada kondisi sistem tertentu. */
    fun gelap(sistemGelap: Boolean): Boolean = when (this) {
        Terang -> false
        Gelap -> true
        Sistem -> sistemGelap
    }

    companion object {
        fun dari(nilai: String?): ThemeMode =
            entries.firstOrNull { it.nilai == nilai } ?: Sistem
    }
}

/**
 * Menyimpan pilihan tema di SharedPreferences agar bisa dibaca sebelum
 * window dibuat (splash langsung sesuai tema) tanpa menyentuh database game.
 */
class ThemeStore(context: Context) {
    private val sp = context.applicationContext
        .getSharedPreferences("codefishing_theme", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(ThemeMode.dari(sp.getString(KEY_TEMA, null)))
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun pilih(mode: ThemeMode) {
        _mode.value = mode
        sp.edit { putString(KEY_TEMA, mode.nilai) }
    }

    private companion object {
        const val KEY_TEMA = "tema"
    }
}
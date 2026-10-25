package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.LayarSementara

/**
 * Layar 11 Umpan Habis.
 * Frontend · Logika: Mode Pemula
 *
 * Spesifikasi prototipe (5_Desainer_UIUX/prototype/codefishing.html): Hitung mundur besar dan ikon umpan; tombol Lanjutkan aktif saat umpan ≥ 1.
 * Tautan: Lanjutkan Kuis → 05 · Baca Materi → 04 · Peta → 03 · Menu ≡ → drawer.
 */
@Composable
fun UmpanHabisScreen(nav: AppNavigator, padding: PaddingValues) {
    // TODO(Frontend): ganti LayarSementara dengan UI sebenarnya. Logika ada di pemula/ dan expert/.
    LayarSementara(
        padding = padding,
        pemilik = "Frontend, logika Mode Pemula",
        spesifikasi = "Hitung mundur besar dan ikon umpan; tombol Lanjutkan aktif saat umpan ≥ 1\n\nTautan: Lanjutkan Kuis → 05 · Baca Materi → 04 · Peta → 03 · Menu ≡ → drawer",
        tombol = listOf(
            "Lanjutkan Kuis" to { nav.replace(Screen.Kuis) },
            "Baca Materi" to { nav.replace(Screen.Materi) },
            "Peta" to { nav.resetTo(Screen.Peta) },
        ),
    )
}

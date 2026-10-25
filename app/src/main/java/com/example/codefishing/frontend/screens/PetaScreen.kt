package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.LayarSementara

/**
 * Layar 03 Peta Perairan.
 * Frontend · Logika: Mode Pemula
 *
 * Spesifikasi prototipe (5_Desainer_UIUX/prototype/codefishing.html): Peta 2D yang digeser: kapal pemancing, stage 1–10 sebagai titik di rute, tiap perairan punya kondisi air; ketuk titik untuk info lalu Berlayar.
 * Tautan: Stage 1–9 terbuka → 04 · Stage 10 (Boss) → 12 · Menu ≡ → drawer.
 */
@Composable
fun PetaScreen(nav: AppNavigator, padding: PaddingValues) {
    // TODO(Frontend): ganti LayarSementara dengan UI sebenarnya. Logika ada di pemula/ dan expert/.
    LayarSementara(
        padding = padding,
        pemilik = "Frontend, logika Mode Pemula",
        spesifikasi = "Peta 2D yang digeser: kapal pemancing, stage 1–10 sebagai titik di rute, tiap perairan punya kondisi air; ketuk titik untuk info lalu Berlayar\n\nTautan: Stage 1–9 terbuka → 04 · Stage 10 (Boss) → 12 · Menu ≡ → drawer",
        tombol = listOf(
            "Stage 1 (Materi)" to { nav.go(Screen.Materi, stage = 0) },
            "Stage 10 (Boss)" to { nav.go(Screen.Boss, stage = 9) },
        ),
    )
}

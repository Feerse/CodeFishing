package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.LayarSementara

/**
 * Layar 12 Stage Boss.
 * Frontend · Logika: Mode Pemula
 *
 * Spesifikasi prototipe (5_Desainer_UIUX/prototype/codefishing.html): Gambar kapal pukat di badai, tiga kotak ikon (syarat lulus 70%, salah −1 umpan, umpan kamu); tanpa materi.
 * Tautan: Mulai Boss → 05 · Menu ≡ → drawer.
 */
@Composable
fun BossScreen(nav: AppNavigator, padding: PaddingValues) {
    // TODO(Frontend): ganti LayarSementara dengan UI sebenarnya. Logika ada di pemula/ dan expert/.
    LayarSementara(
        padding = padding,
        pemilik = "Frontend, logika Mode Pemula",
        spesifikasi = "Gambar kapal pukat di badai, tiga kotak ikon (syarat lulus 70%, salah −1 umpan, umpan kamu); tanpa materi\n\nTautan: Mulai Boss → 05 · Menu ≡ → drawer",
        tombol = listOf(
            "Mulai Boss" to { nav.go(Screen.Kuis) },
        ),
    )
}

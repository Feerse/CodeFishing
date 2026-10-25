package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.LayarSementara

/**
 * Layar 09 Koleksi.
 * Frontend · Logika: Mode Pemula
 *
 * Spesifikasi prototipe (5_Desainer_UIUX/prototype/codefishing.html): Grid ikan 15 slot dengan warna sesuai kelangkaan; yang belum didapat abu-abu dengan ikon gembok. Hitungan ikan dan tombol Akuarium di atas.
 * Tautan: Akuarium → 13 · Menu ≡ → drawer.
 */
@Composable
fun KoleksiScreen(nav: AppNavigator, padding: PaddingValues) {
    // TODO(Frontend): ganti LayarSementara dengan UI sebenarnya. Logika ada di pemula/ dan expert/.
    LayarSementara(
        padding = padding,
        pemilik = "Frontend, logika Mode Pemula",
        spesifikasi = "Grid ikan 15 slot dengan warna sesuai kelangkaan; yang belum didapat abu-abu dengan ikon gembok. Hitungan ikan dan tombol Akuarium di atas\n\nTautan: Akuarium → 13 · Menu ≡ → drawer",
        tombol = listOf(
            "Akuarium" to { nav.go(Screen.Akuarium) },
        ),
    )
}

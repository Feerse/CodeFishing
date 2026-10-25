package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.LayarSementara

/**
 * Layar 06 Hasil Stage.
 * Frontend · Logika: Mode Pemula
 *
 * Spesifikasi prototipe (5_Desainer_UIUX/prototype/codefishing.html): Ikon lulus atau belum, persen progres, ikan yang didapat beserta grade, dua tombol.
 * Tautan: Stage berikutnya → 04 / 12 · Peta → 03 · Menu ≡ → drawer.
 */
@Composable
fun HasilStageScreen(nav: AppNavigator, padding: PaddingValues) {
    // TODO(Frontend): ganti LayarSementara dengan UI sebenarnya. Logika ada di pemula/ dan expert/.
    LayarSementara(
        padding = padding,
        pemilik = "Frontend, logika Mode Pemula",
        spesifikasi = "Ikon lulus atau belum, persen progres, ikan yang didapat beserta grade, dua tombol\n\nTautan: Stage berikutnya → 04 / 12 · Peta → 03 · Menu ≡ → drawer",
        tombol = listOf(
            "Stage berikutnya" to { nav.replace(Screen.Materi, stage = nav.current.stage + 1) },
            "Peta" to { nav.resetTo(Screen.Peta) },
        ),
    )
}

package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.LayarSementara

/**
 * Layar 04 Materi.
 * Frontend · Logika: Mode Pemula
 *
 * Spesifikasi prototipe (5_Desainer_UIUX/prototype/codefishing.html): Judul dengan gambar ikan stage, bar progres, satu kalimat materi, contoh kode, keluaran.
 * Tautan: Ke Kuis → 05 · Menu ≡ → drawer.
 */
@Composable
fun MateriScreen(nav: AppNavigator, padding: PaddingValues) {
    // TODO(Frontend): ganti LayarSementara dengan UI sebenarnya. Logika ada di pemula/ dan expert/.
    LayarSementara(
        padding = padding,
        pemilik = "Frontend, logika Mode Pemula",
        spesifikasi = "Judul dengan gambar ikan stage, bar progres, satu kalimat materi, contoh kode, keluaran\n\nTautan: Ke Kuis → 05 · Menu ≡ → drawer",
        tombol = listOf(
            "Ke Kuis" to { nav.go(Screen.Kuis) },
        ),
    )
}

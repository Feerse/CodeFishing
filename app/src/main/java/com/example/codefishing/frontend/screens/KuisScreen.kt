package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.LayarSementara

/**
 * Layar 05 Kuis.
 * Frontend · Logika: Mode Pemula
 *
 * Spesifikasi prototipe (5_Desainer_UIUX/prototype/codefishing.html): Atas: animasi nelayan dan hasil kode (ikon stage, ikan, beruntun). Tengah: soal satu kalimat dan editor kode berlabel PYTHON dengan slot kosong. Bawah: 4 pilihan umpan kode, tombol reset (ikon), dan Lempar Kail. Hasil muncul sebagai kartu: benar = ikan terpancing, salah = kail putus dan umpan berkurang.
 * Tautan: Stage selesai → 06 · Umpan habis → 11 · Menu ≡ → drawer.
 */
@Composable
fun KuisScreen(nav: AppNavigator, padding: PaddingValues) {
    // TODO(Frontend): ganti LayarSementara dengan UI sebenarnya. Logika ada di pemula/ dan expert/.
    LayarSementara(
        padding = padding,
        pemilik = "Frontend, logika Mode Pemula",
        spesifikasi = "Atas: animasi nelayan dan hasil kode (ikon stage, ikan, beruntun). Tengah: soal satu kalimat dan editor kode berlabel PYTHON dengan slot kosong. Bawah: 4 pilihan umpan kode, tombol reset (ikon), dan Lempar Kail. Hasil muncul sebagai kartu: benar = ikan terpancing, salah = kail putus dan umpan berkurang\n\nTautan: Stage selesai → 06 · Umpan habis → 11 · Menu ≡ → drawer",
        tombol = listOf(
            "Selesai (uji)" to { nav.replace(Screen.HasilStage) },
        ),
    )
}

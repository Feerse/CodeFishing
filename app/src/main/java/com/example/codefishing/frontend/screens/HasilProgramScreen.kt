package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.LayarSementara

/**
 * Layar 08 Hasil Program.
 * Frontend · Logika: Mode Expert
 *
 * Spesifikasi prototipe (5_Desainer_UIUX/prototype/codefishing.html): Ikon berhasil atau gagal, tiga angka berikon (waktu, ikan, sampah), ikan hasil tangkapan, keluaran print bila ada. Penjelasan hanya muncul saat gagal atau error.
 * Tautan: Ubah Kode → 07 · Akuarium → 13 · Misi berikutnya → 07 · Menu ≡ → drawer.
 */
@Composable
fun HasilProgramScreen(nav: AppNavigator, padding: PaddingValues) {
    // TODO(Frontend): ganti LayarSementara dengan UI sebenarnya. Logika ada di pemula/ dan expert/.
    LayarSementara(
        padding = padding,
        pemilik = "Frontend, logika Mode Expert",
        spesifikasi = "Ikon berhasil atau gagal, tiga angka berikon (waktu, ikan, sampah), ikan hasil tangkapan, keluaran print bila ada. Penjelasan hanya muncul saat gagal atau error\n\nTautan: Ubah Kode → 07 · Akuarium → 13 · Misi berikutnya → 07 · Menu ≡ → drawer",
        tombol = listOf(
            "Ubah Kode" to { nav.back() },
            "Misi berikutnya" to { nav.replace(Screen.Sandbox, misi = nav.current.misi + 1) },
        ),
    )
}

package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.LayarSementara

/**
 * Layar 07 Sandbox.
 * Frontend · Logika: Mode Expert
 *
 * Spesifikasi prototipe (5_Desainer_UIUX/prototype/codefishing.html): Atas: petak laut berisi ikan dan sampah, kapal kertas, misi, ikan dan waktu (berikon), bintang. Tengah: editor Python berwarna dengan nomor baris dan tombol cepat. Bawah: konsol, tombol ikon Misi, Fungsi, ulang, kecepatan, dan Jalankan. Tanpa umpan.
 * Tautan: Selesai jalan → 08 · Akuarium → 13 · Misi / Fungsi → panel di layar ini · Menu ≡ → drawer.
 */
@Composable
fun SandboxScreen(nav: AppNavigator, padding: PaddingValues) {
    // TODO(Frontend): ganti LayarSementara dengan UI sebenarnya. Logika ada di pemula/ dan expert/.
    LayarSementara(
        padding = padding,
        pemilik = "Frontend, logika Mode Expert",
        spesifikasi = "Atas: petak laut berisi ikan dan sampah, kapal kertas, misi, ikan dan waktu (berikon), bintang. Tengah: editor Python berwarna dengan nomor baris dan tombol cepat. Bawah: konsol, tombol ikon Misi, Fungsi, ulang, kecepatan, dan Jalankan. Tanpa umpan\n\nTautan: Selesai jalan → 08 · Akuarium → 13 · Misi / Fungsi → panel di layar ini · Menu ≡ → drawer",
        tombol = listOf(
            "Jalankan (uji)" to { nav.go(Screen.HasilProgram) },
        ),
    )
}

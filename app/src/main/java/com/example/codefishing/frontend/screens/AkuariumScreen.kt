package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.frontend.components.Gambar

/**
 * 13 Akuarium. Frontend.
 * Tangki berlapis kertas, ikan vektor berenang ala potongan kertas (ekor berengsel, berbalik seperti membalik kertas,
 * gerakan patah-patah). Berisi ikan Koleksi Belajar dan hasil Sandbox. Musik latar sudah diputar global (lihat AudioManager).
 */
@Composable
fun AkuariumScreen(nav: AppNavigator, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding)) {
        Gambar(Art.latar_akuarium, null, Modifier.fillMaxSize(), ContentScale.Crop)
        // TODO(Frontend): ikan berenang. Ambil ikan milik pemain dari ContentRepository.ikan + progres.
    }
}

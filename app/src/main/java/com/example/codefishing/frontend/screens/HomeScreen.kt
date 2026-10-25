package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama

/**
 * 01 Home. Frontend.
 * Kapal, logo, label Python, kartu Lanjutkan (bila ada progres), tombol Mulai Main.
 */
@Composable
fun HomeScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val adaProgres = c.progress.stageTerbuka > 0 || c.progress.bestPercent(0) > 0

    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Gambar(Art.kapal_pancing, "Kapal pemancing", Modifier.fillMaxWidth().height(180.dp))
        Gambar(Art.logo_codefishing, "Logo CodeFishing", Modifier.size(72.dp))
        Text("CodeFishing", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Text("Belajar Python sambil memancing", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (adaProgres) {
            // TODO(Frontend): arahkan ke layar terakhir (Materi/Kuis/Sandbox/Boss) sesuai posisi tersimpan.
            TombolSekunder("Lanjutkan", { c.audio.play(Sfx.KlikUtama); nav.go(Screen.Peta) })
        }
        TombolUtama("Mulai Main", { c.audio.play(Sfx.KlikUtama); nav.go(Screen.PilihMode) })
    }
}

package com.example.codefishing.frontend.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Gambar

/**
 * 02 Pilih Mode. Frontend.
 * Dua kartu bergambar: Belajar (Pemula) dan Sandbox (Expert), tanpa kalimat penjelasan.
 */
@Composable
fun PilihModeScreen(nav: AppNavigator, padding: PaddingValues) {
    val audio = LocalContainer.current.audio
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KartuMode("Belajar", "Pemula", Art.ikon_mode_belajar, Art.latar_bawah_laut) {
            audio.play(Sfx.Klik); nav.go(Screen.Peta)
        }
        KartuMode("Sandbox", "Expert", Art.ikon_mode_expert, Art.adegan_kapal) {
            audio.play(Sfx.Klik); nav.go(Screen.Sandbox)
        }
    }
}

@Composable
private fun KartuMode(judul: String, tag: String, ikon: Int, latar: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Gambar(latar, null, Modifier.fillMaxWidth().height(150.dp))
        Column(Modifier.padding(16.dp)) {
            Gambar(ikon, null, Modifier.height(28.dp))
            Text(judul, style = MaterialTheme.typography.titleLarge)
            Text(tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

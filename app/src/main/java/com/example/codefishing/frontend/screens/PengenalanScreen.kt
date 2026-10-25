package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama

private data class Halaman(val judul: String, val isi: String, val gambar: Int)

private val halaman = listOf(
    Halaman("Belajar Python sambil memancing", "Semua soal dan kode memakai Python.", Art.logo_codefishing),
    Halaman("Pilih umpan kode yang benar", "Jawaban benar menangkap ikan.", Art.kaleng_umpan),
    Halaman("Tulis programmu sendiri", "Mode Expert: kodemu menggerakkan kapal.", Art.ilustrasi_editor_kode),
    Halaman("Kumpulkan ikan di akuarium", "Semua jalan offline, tanpa internet.", Art.latar_akuarium),
)

/**
 * 14 Pengenalan. Frontend.
 * Empat halaman bergambar TANPA toolbar. Muncul sekali saat pertama dibuka, bisa dilihat lagi dari Pengaturan.
 */
@Composable
fun PengenalanScreen(onSelesai: () -> Unit) {
    var i by rememberSaveable { mutableIntStateOf(0) }
    val h = halaman[i]
    val terakhir = i == halaman.lastIndex

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Gambar(h.gambar, null, Modifier.fillMaxWidth().height(260.dp))
        }
        Text(h.judul, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            h.isi,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            halaman.indices.forEach { n ->
                Box(
                    Modifier.size(8.dp).clip(CircleShape).background(
                        if (n == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!terakhir) {
                TombolSekunder("Lewati", onSelesai, Modifier.weight(1f))
            }
            TombolUtama(if (terakhir) "Mulai" else "Lanjut", { if (terakhir) onSelesai() else i++ }, Modifier.weight(1f))
        }
    }
}

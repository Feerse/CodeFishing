package com.example.codefishing.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.core.db.MateriEntity
import com.example.codefishing.core.db.StageEntity
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.ChipTier
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.KodeBlok
import com.example.codefishing.frontend.components.Memuat
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan

data class DataMateri(
    val stage: StageEntity,
    val materi: MateriEntity?,
    val ikan: IkanEntity?,
)

@Composable
fun MateriScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val stageId = nav.current.stage.takeIf { it > 0 } ?: 1
    val data by produceState<DataMateri?>(null, stageId) {
        val stage = c.game.stageById(stageId)
        value = stage?.let {
            DataMateri(it, c.game.materi(it.id), it.ikan_target_id?.let { id -> c.game.ikan(id) })
        }
        if (stage != null) c.game.mulaiStage(stage.id)
    }

    val d = data
    if (d == null) {
        Memuat(padding)
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(Color(0xFF0B2C3B), Color(0xFF071923)))),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${d.stage.nomor}", color = Color(0xFF062B2A), style = MaterialTheme.typography.titleMedium)
                }
                Column(Modifier.weight(1f)) {
                    Text(d.stage.nama, style = MaterialTheme.typography.headlineSmall)
                    Text("${d.stage.zona} · ${d.stage.konsep}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (d.ikan != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Gambar(gambarIkan(d.ikan.gambar), d.ikan.nama, Modifier.size(64.dp))
                        ChipTier(d.ikan.tier)
                    }
                }
            }

            Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF123B4A)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Materi", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                    Text(d.materi?.isi_markdown ?: "Siapkan dirimu. Stage ini adalah ujian akhir perairan.", style = MaterialTheme.typography.bodyLarge)
                    d.materi?.contoh_kode?.takeIf { it.isNotBlank() }?.let {
                        KodeBlok(it)
                    }
                    d.materi?.output_contoh?.takeIf { it.isNotBlank() }?.let {
                        Text("Output: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (!d.materi?.trik_nelayan.isNullOrBlank()) {
                Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF2A2A17)) {
                    Text("Trik Nelayan", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFFD98A))
                    Spacer(Modifier.height(6.dp))
                    Text(d.materi!!.trik_nelayan!!, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Kartu(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Gambar(Art.umpan_penuh, "Umpan", Modifier.size(30.dp))
                    Text(
                        "5 soal · lulus ${if (d.stage.tipe == "boss") 80 else d.stage.skor_lulus}% · +${d.stage.xp_reward} XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TombolUtama(
                teks = if (d.stage.tipe == "boss") "Mulai Ujian Boss" else "Mulai Kuis",
                onClick = {
                    c.audio.play(Sfx.KlikUtama)
                    nav.go(if (d.stage.tipe == "boss") Screen.Boss else Screen.Kuis, d.stage.id)
                },
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

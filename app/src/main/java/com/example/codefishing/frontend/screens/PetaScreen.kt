package com.example.codefishing.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.core.db.StageStatusRow
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.ChipTier
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.gambarIkan

/**
 * 03 Peta Perairan: rute 10 stage. Stage terkunci tidak dapat dibuka.
 * Stage 10 bertipe boss diarahkan ke layar Boss.
 */
@Composable
fun PetaScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val stages by c.game.observeStages().collectAsState(emptyList())
    val ikan by produceState<List<IkanEntity>>(emptyList()) { value = c.game.semuaIkan() }
    val petaIkan = ikan.associateBy { it.id }
    val jumlahSelesai = stages.count { it.status == "selesai" }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A2433), Color(0xFF071923), Color(0xFF0B2D3B)),
                ),
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Gambar(Art.kapal_peta, "Kapal peta", Modifier.size(88.dp))
                    Column {
                        Text("Peta Perairan", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "$jumlahSelesai/10 stage selesai",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            items(stages, key = { it.id }) { stage ->
                KartuStage(
                    stage = stage,
                    ikan = stage.ikan_target_id?.let { petaIkan[it] },
                    onClick = {
                        c.audio.play(Sfx.Klik)
                        if (stage.terbuka == 1) {
                            nav.go(if (stage.tipe == "boss") Screen.Boss else Screen.Materi, stage.id)
                        }
                    },
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Petunjuk: selesaikan stage sebelumnya untuk membuka rute berikutnya.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun KartuStage(stage: StageStatusRow, ikan: IkanEntity?, onClick: () -> Unit) {
    val buka = stage.terbuka == 1
    val selesai = stage.status == "selesai"
    val warna = when {
        !buka -> Color(0xFF122C36)
        stage.tipe == "boss" -> Color(0xFF3A1F2B)
        selesai -> Color(0xFF123D3B)
        else -> Color(0xFF143C4D)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(warna, RoundedCornerShape(18.dp))
            .clickable(enabled = buka, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(42.dp)
                .background(
                    if (selesai) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (!buka) "🔒" else stage.nomor.toString(),
                color = if (selesai) Color(0xFF062B2A) else Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stage.nama, style = MaterialTheme.typography.titleMedium, color = if (buka) Color.White else Color.White.copy(alpha = 0.45f))
                if (stage.tipe == "boss") Text("BOSS", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF9E8A), fontWeight = FontWeight.Bold)
            }
            Text(
                "${stage.zona} · ${stage.konsep}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = if (buka) 0.7f else 0.35f),
            )
            if (stage.skor_terbaik > 0) {
                Text("Skor terbaik ${stage.skor_terbaik}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        if (ikan != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Gambar(
                    gambarIkan(ikan.gambar),
                    ikan.nama,
                    Modifier.size(52.dp),
                )
                ChipTier(ikan.tier)
            }
        }
    }
}

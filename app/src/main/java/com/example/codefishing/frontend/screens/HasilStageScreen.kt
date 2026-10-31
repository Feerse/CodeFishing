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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.HasilStageData
import com.example.codefishing.core.db.StageEntity
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Bintang
import com.example.codefishing.frontend.components.ChipTier
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.LabelNilai
import com.example.codefishing.frontend.components.Memuat
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan

@Composable
fun HasilStageScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val stageId = nav.current.stage.takeIf { it > 0 } ?: 1
    val hasil = c.game.hasilStageTerakhir?.takeIf { it.stage.id == stageId }
    val fallback by produceState<StageEntity?>(null, stageId, hasil) {
        value = c.game.stageById(stageId)
    }
    val stage = hasil?.stage ?: fallback

    if (stage == null) {
        Memuat(padding)
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                Brush.verticalGradient(
                    if (hasil?.lulus == true) listOf(Color(0xFF0C3A35), Color(0xFF071923))
                    else listOf(Color(0xFF3A2028), Color(0xFF071923)),
                ),
            ),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Gambar(
                when {
                    hasil?.lulus == true && stage.tipe == "boss" -> Art.trofi_boss
                    hasil?.lulus == true -> Art.tameng_lulus_70
                    else -> Art.kail_putus
                },
                if (hasil?.lulus == true) "Lulus" else "Belum lulus",
                Modifier.size(104.dp),
            )
            Text(
                if (hasil?.lulus == true) {
                    if (stage.tipe == "boss") "Boss Dikalahkan!" else "Stage Selesai!"
                } else if (hasil == null) stage.nama else "Belum Lulus",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(stage.nama, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

            if (hasil != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    LabelNilai("Skor", "${hasil.skor}%")
                    LabelNilai("Benar", "${hasil.benar}/${hasil.total}")
                    LabelNilai("XP", "+${hasil.xpDidapat}")
                }

                if (hasil.tangkapan != null) {
                    Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF123B4A)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                when {
                                    hasil.naikTier -> "Ikan naik tier!"
                                    hasil.tangkapan.baru -> "Ikan baru masuk koleksi"
                                    else -> "Ikan target tertangkap"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                            Gambar(gambarIkan(hasil.tangkapan.ikan.gambar), hasil.tangkapan.ikan.nama, Modifier.size(140.dp))
                            Text(hasil.tangkapan.ikan.nama, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                ChipTier(hasil.tangkapan.tier)
                                Text("${"%.1f".format(hasil.tangkapan.bobot)} kg", style = MaterialTheme.typography.labelLarge)
                                if (hasil.tangkapan.xpDidapat > 0) {
                                    Text("+${hasil.tangkapan.xpDidapat} XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                if (hasil.bonus.isNotEmpty()) {
                    Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF2B2B16)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Bonus combo", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFFD98A))
                            hasil.bonus.forEach { b ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Gambar(gambarIkan(b.ikan.gambar), b.ikan.nama, Modifier.size(44.dp))
                                    Text(b.ikan.nama, modifier = Modifier.weight(1f))
                                    ChipTier(b.tier)
                                }
                            }
                        }
                    }
                }

                if (hasil.rekorBaru) {
                    Text("Rekor baru!", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                }
            } else {
                Text(
                    "Skor terbaik stage ini belum tersedia. Selesaikan kuis untuk melihat hasil lengkap.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            if (hasil?.lulus == true && hasil.stageBerikut != null) {
                TombolUtama(
                    teks = if (hasil.stageBerikut == 10) "Menuju Boss" else "Stage Berikutnya",
                    onClick = {
                        c.audio.play(Sfx.KlikUtama)
                        nav.replace(if (hasil.stageBerikut == 10) Screen.Boss else Screen.Materi, hasil.stageBerikut)
                    },
                )
            } else if (hasil != null && !hasil.lulus) {
                TombolUtama(
                    teks = "Coba Lagi",
                    onClick = {
                        c.audio.play(Sfx.Klik)
                        nav.replace(Screen.Materi, stage.id)
                    },
                )
            }
            TombolSekunder("Kembali ke Peta", onClick = {
                c.audio.play(Sfx.Klik)
                nav.resetTo(Screen.Peta)
            })
            Bintang(if (hasil?.lulus == true) if (hasil.sempurna) 3 else 2 else 0)
            Spacer(Modifier.height(8.dp))
        }
    }
}

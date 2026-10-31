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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.TantanganData
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.BarProgres
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan
import com.example.codefishing.pemula.LevelRules
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val player by c.game.observePlayer().collectAsState(null)
    val stageList by c.game.observeStages().collectAsState(emptyList())
    val statistik by c.game.observeStatistik().collectAsState(null)
    var versiHarian by remember { mutableIntStateOf(0) }
    val harian by produceState<TantanganData?>(null, versiHarian) { value = c.game.pastikanHarian() }
    var umpan by remember { mutableIntStateOf(5) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { umpan = c.game.muatUmpan().sisa }

    val stageAktif = stageList.firstOrNull { it.status != "selesai" && it.terbuka == 1 }
        ?: stageList.lastOrNull()
    val posisi = player?.stage_terakhir_id?.let { id -> stageList.firstOrNull { it.id == id } }
    val judulLanjut = posisi?.nama ?: stageAktif?.nama ?: "Petualangan Baru"
    val xp = player?.xp ?: 0
    val level = player?.level ?: 1
    val terkumpul = statistik?.ikan_terkumpul ?: 0

    Box(Modifier.fillMaxSize().padding(padding)) {
        Gambar(Art.latar_langit_malam, null, Modifier.fillMaxSize(), ContentScale.Crop)
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xCC0C2B3A), Color(0xFF0C2B3A)),
                    ),
                ),
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Gambar(Art.logo_codefishing, "Logo CodeFishing", Modifier.size(52.dp))
                Column(Modifier.weight(1f)) {
                    Text("CodeFishing", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Belajar Python sambil memancing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Kartu(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Nakhoda Python", style = MaterialTheme.typography.titleMedium)
                            Text("Level $level · ${LevelRules.rank(level)}", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelLarge)
                        }
                        Text("$xp XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    BarProgres(LevelRules.progresLevel(xp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Gambar(Art.umpan_penuh, "Umpan", Modifier.size(22.dp))
                            Text("$umpan umpan", style = MaterialTheme.typography.labelMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Gambar(Art.lencana_plus_satu, "Umpan emas", Modifier.size(20.dp))
                            Text("${player?.umpan_emas ?: 0} emas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Gambar(Art.bintang_penuh, "Stage selesai", Modifier.size(20.dp))
                            Text("${stageList.count { it.status == "selesai" }} stage", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (harian != null) {
                Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF173F4E)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Gambar(gambarIkan(harian!!.ikan.gambar), harian!!.ikan.nama, Modifier.size(52.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Tantangan Harian", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text("Tangkap ${harian!!.ikan.nama}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                when (harian!!.row.status) {
                                    "selesai" -> "Selesai! Klaim 1 umpan emas."
                                    "diklaim" -> "Hadiah hari ini sudah diklaim."
                                    else -> "Selesaikan stage atau tangkap di Sandbox."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (harian!!.row.status == "selesai") {
                            TombolSekunder(
                                teks = "Klaim",
                                onClick = {
                                    c.audio.play(Sfx.Klik)
                                    scope.launch {
                                        if (c.game.klaimHarian()) {
                                            c.audio.play(Sfx.Benar)
                                            versiHarian++
                                            umpan = c.game.muatUmpan().sisa
                                        }
                                    }
                                    Unit
                                },
                                modifier = Modifier.size(width = 90.dp, height = 44.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                            )
                        }
                    }
                }
            }

            Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF133C4F)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Lanjutkan perjalanan", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                    Text(judulLanjut, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Stage ${posisi?.nomor ?: stageAktif?.nomor ?: 1} · ${posisi?.konsep ?: stageAktif?.konsep ?: "Pilih mode untuk mulai"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TombolUtama(
                        teks = "Mulai Main",
                        onClick = {
                            c.audio.play(Sfx.KlikUtama)
                            nav.go(Screen.PilihMode)
                        },
                    )
                    TombolSekunder(
                        teks = "Lanjutkan",
                        onClick = {
                            c.audio.play(Sfx.Klik)
                            if (player?.mode_terakhir == "expert") {
                                nav.go(Screen.Sandbox)
                            } else if (posisi != null) {
                                nav.go(if (posisi.tipe == "boss") Screen.Boss else Screen.Materi, posisi.id)
                            } else {
                                nav.go(Screen.PilihMode)
                            }
                        },
                    )
                }
            }

            Kartu(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Gambar(Art.ikan_hiu_merah, "Hiu Merah", Modifier.size(56.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Koleksi $terkumpul/48 ikan", style = MaterialTheme.typography.titleMedium)
                        Text("15 ikan berilustrasi siap ditangkap. Sisanya segera hadir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    CircularProgressIndicator(
                        progress = { terkumpul / 48f },
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.1f),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

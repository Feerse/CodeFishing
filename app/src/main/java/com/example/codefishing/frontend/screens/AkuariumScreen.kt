package com.example.codefishing.frontend.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.db.KoleksiLengkapView
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan
import kotlinx.coroutines.launch

/**
 * 13 Akuarium: ikan yang sudah dimiliki berenang di atas latar kertas.
 * Ikan yang belum dimiliki tidak ditampilkan di sini.
 */
@Composable
fun AkuariumScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val ikan by c.game.observeAkuarium().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var dipilih by remember { mutableStateOf<KoleksiLengkapView?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFF07212C)),
    ) {
        Gambar(Art.latar_akuarium, "Akuarium", Modifier.fillMaxSize(), ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Color(0x33000000)))

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val lebar = maxWidth
            val tinggi = maxHeight
            val transisi = rememberInfiniteTransition(label = "akuarium")
            ikan.forEachIndexed { i, k ->
                val durasi = 2600 + (i % 5) * 350
                val naikTurun by transisi.animateFloat(
                    initialValue = -7f,
                    targetValue = 7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durasi, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "ikan-$i",
                )
                val kiri = if (i % 2 == 0) 12f + (i * 13) % 55 else 45f + (i * 17) % 45
                val atas = 18f + ((i * 19) % 58)
                val ukuran = if (k.tier == "mitos" || k.tier == "legenda") 92.dp else 68.dp
                Gambar(
                    gambarIkan(k.gambar),
                    k.nama,
                    Modifier
                        .offset {
                            IntOffset(
                                ((lebar - ukuran) * (kiri / 100f)).roundToPx(),
                                ((tinggi - ukuran) * (atas / 100f) + naikTurun.dp).roundToPx(),
                            )
                        }
                        .size(ukuran)
                        .clickable { dipilih = k },
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Akuarium",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${ikan.size} jenis ikan berenang di sini",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val pilihan = dipilih?.let { lama -> ikan.firstOrNull { it.ikan_id == lama.ikan_id } ?: lama }
                if (pilihan != null) {
                    Kartu(Modifier.fillMaxWidth(), warna = Color(0xEE0C2B36)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Gambar(gambarIkan(pilihan.gambar), pilihan.nama, Modifier.size(54.dp))
                            Column(Modifier.weight(1f)) {
                                Text(pilihan.nama, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("Jumlah ×${pilihan.jumlah} · ${"%.1f".format(pilihan.bobot_terbesar)} kg · kenyang ${pilihan.kenyang}%", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                            }
                            TombolSekunder(
                                teks = "Beri Makan",
                                onClick = {
                                    c.audio.play(Sfx.Benar)
                                    scope.launch { c.game.beriMakan(pilihan.ikan_id) }
                                },
                                modifier = Modifier.size(width = 120.dp, height = 44.dp),
                            )
                        }
                    }
                } else if (ikan.isEmpty()) {
                    Kartu(Modifier.fillMaxWidth(), warna = Color(0xEE0C2B36)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Akuarium masih kosong", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("Tangkap ikan pertamamu di Mode Belajar atau Sandbox.", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            TombolUtama(
                                teks = "Buka Peta",
                                onClick = {
                                    c.audio.play(Sfx.Klik)
                                    nav.resetTo(Screen.Peta)
                                },
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        TombolSekunder(
                            teks = "Beri Makan Semua",
                            onClick = {
                                c.audio.play(Sfx.Benar)
                                scope.launch {
                                    ikan.forEach { c.game.beriMakan(it.ikan_id) }
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TombolSekunder(
                            teks = "Koleksi",
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                nav.resetTo(Screen.Koleksi)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

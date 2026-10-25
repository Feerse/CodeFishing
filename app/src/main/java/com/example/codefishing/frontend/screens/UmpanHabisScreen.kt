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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.UmpanUi
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.TombolSekunder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UmpanHabisScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val scope = rememberCoroutineScope()
    val stageId = nav.current.stage.takeIf { it > 0 } ?: 1
    var umpan by remember { mutableStateOf<UmpanUi?>(null) }
    var emas by remember { mutableIntStateOf(0) }
    var versi by remember { mutableIntStateOf(0) }

    LaunchedEffect(versi) {
        while (true) {
            umpan = c.game.muatUmpan()
            emas = c.game.player().umpan_emas
            delay(1000)
        }
    }

    val u = umpan
    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(Color(0xFF3A2028), Color(0xFF071923)))),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .size(130.dp)
                    .background(Color(0xFF1B3440), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Gambar(Art.ikon_umpan_habis, "Umpan habis", Modifier.size(92.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text("Umpan Habis", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Jawaban salah menghabiskan umpan. Umpan pulih satu setiap ${u?.intervalMenit ?: 15} menit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF123B4A)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if ((u?.sisa ?: 0) > 0) "Umpan tersedia: ${u?.sisa}" else formatWaktu(u?.sisaMs ?: 0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if ((u?.sisa ?: 0) > 0) MaterialTheme.colorScheme.primary else Color(0xFFFFB3B3),
                    )
                    Text("${u?.sisa ?: 0}/${u?.maks ?: 5} umpan · 🪙 $emas emas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    c.audio.play(Sfx.KlikUtama)
                    nav.replace(Screen.Kuis, stageId)
                },
                enabled = (u?.sisa ?: 0) > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Lanjutkan Kuis") }
            if ((u?.sisa ?: 0) < (u?.maks ?: 5) && emas > 0) {
                TombolSekunder("Tukar 1 umpan emas", {
                    c.audio.play(Sfx.Benar)
                    scope.launch {
                        if (c.game.tukarUmpanEmas()) versi++
                    }
                })
            }
            TombolSekunder("Baca Materi", {
                c.audio.play(Sfx.Klik)
                nav.replace(Screen.Materi, stageId)
            })
            TombolSekunder("Kembali ke Peta", {
                c.audio.play(Sfx.Klik)
                nav.resetTo(Screen.Peta)
            })
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Gambar(Art.chip_umpan_kode, null, Modifier.size(20.dp))
                Text("Umpan emas didapat dari tantangan harian.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatWaktu(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val menit = total / 60
    val detik = total % 60
    return "%02d:%02d".format(menit, detik)
}

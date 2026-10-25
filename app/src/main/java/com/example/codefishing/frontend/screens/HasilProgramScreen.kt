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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.HasilProgramData
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Bintang
import com.example.codefishing.frontend.components.ChipTier
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.KodeBlok
import com.example.codefishing.frontend.components.LabelNilai
import com.example.codefishing.frontend.components.Memuat
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan

@Composable
fun HasilProgramScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val id = nav.current.hasil
    val data by produceState<HasilProgramData?>(null, id) {
        value = if (id > 0) c.game.hasilProgram(id) else null
    }
    val d = data
    if (d == null) {
        Memuat(padding, "Memuat hasil program...")
        return
    }
    val sukses = d.hasil.status == "sukses"

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(
                Brush.verticalGradient(
                    if (sukses) listOf(Color(0xFF0C3A35), Color(0xFF071923))
                    else listOf(Color(0xFF3A2028), Color(0xFF071923)),
                ),
            ),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(76.dp)
                    .background(if (sukses) Color(0xFF1C6B58) else Color(0xFF7B303D), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (sukses) "✓" else "✕", style = MaterialTheme.typography.displaySmall)
            }
            Text(if (sukses) "Program Berhasil" else "Program Belum Berhasil", style = MaterialTheme.typography.headlineSmall)
            Bintang(d.hasil.bintang, ukuran = 24)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                LabelNilai("Tick", "${d.hasil.ticks}")
                LabelNilai("Ikan", "${d.tangkapan.sumOf { it.jumlah }}")
                LabelNilai("XP", "+${d.hasil.xp}")
            }

            if (!sukses && !d.hasil.error.isNullOrBlank()) {
                Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF41232A)) {
                    Text(d.hasil.error, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                }
            }

            if (d.tangkapan.isNotEmpty()) {
                Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF123B4A)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Hasil tangkapan", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                        d.tangkapan.forEach { t ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Gambar(gambarIkan(t.ikan.gambar), t.ikan.nama, Modifier.size(48.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(t.ikan.nama, style = MaterialTheme.typography.titleMedium)
                                    Text("${"%.1f".format(t.bobot)} kg × ${t.jumlah}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                ChipTier(t.tier)
                            }
                        }
                    }
                }
            }

            if (d.hasil.output.isNotBlank()) {
                KodeBlok(d.hasil.output, judul = "OUTPUT")
            }

            if (d.hasil.kode.isNotBlank()) {
                Kartu(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Kode terakhir", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            d.hasil.kode,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            TombolUtama(
                teks = "Ubah Kode",
                onClick = {
                    c.audio.play(Sfx.Klik)
                    nav.replace(Screen.Sandbox, misi = nav.current.misi)
                },
            )
            if (nav.current.misi in 1..6) {
                TombolSekunder(
                    teks = "Misi Berikutnya",
                    onClick = {
                        c.audio.play(Sfx.Klik)
                        nav.replace(Screen.Sandbox, misi = nav.current.misi + 1)
                    },
                )
            }
            TombolSekunder(
                teks = "Daftar Misi",
                onClick = {
                    c.audio.play(Sfx.Klik)
                    nav.resetTo(Screen.Sandbox)
                },
            )
            TombolSekunder(
                teks = "Buka Akuarium",
                onClick = {
                    c.audio.play(Sfx.Klik)
                    nav.resetTo(Screen.Akuarium)
                },
            )
            Text(
                if (sukses) "Ikan hasil tangkapan sudah masuk koleksi." else "Perbaiki kode lalu jalankan lagi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

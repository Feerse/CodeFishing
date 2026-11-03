package com.example.codefishing.frontend.screens

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.ThemeMode
import com.example.codefishing.core.db.PengaturanEntity
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.LabelNilai
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.ui.theme.LocalWarna
import kotlinx.coroutines.launch

@Composable
fun PengaturanScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val debug = (ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val tersimpan by c.game.observeSettings().collectAsState(null)
    val statistik by c.game.observeStatistik().collectAsState(null)
    val tema by c.theme.mode.collectAsState()
    val warna = LocalWarna.current
    var konfirmasi by remember { mutableStateOf(false) }

    val pengaturan = tersimpan ?: PengaturanEntity()

    fun ubah(baru: PengaturanEntity) {
        scope.launch { c.game.simpanPengaturan(baru) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(warna.latarAtas, warna.latarBawah)))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Kartu(Modifier.fillMaxWidth(), warna = warna.kartuLembut) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Nakhoda", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                statistik?.let { s ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        LabelNilai("Level", "${s.level}")
                        LabelNilai("XP", "${s.xp}")
                        LabelNilai("Stage", "${s.stage_selesai}/10")
                        LabelNilai("Ikan", "${s.ikan_terkumpul}/48")
                    }
                } ?: Text("Memuat statistik...", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text("Tampilan", style = MaterialTheme.typography.titleMedium)
        Text(
            "Pilih tema terang, gelap, atau ikuti pengaturan perangkat.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = tema == mode,
                    onClick = {
                        c.audio.play(Sfx.Klik)
                        c.theme.pilih(mode)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                    label = { Text(mode.label) },
                )
            }
        }

        Text("Preferensi", style = MaterialTheme.typography.titleMedium)
        BarisSaklar(Art.ikon_suara, Art.ikon_suara_terang, "Efek suara", pengaturan.efek_suara == 1) {
            c.progress.sfxOn = it
            if (it) c.audio.play(Sfx.Klik)
            ubah(pengaturan.copy(efek_suara = if (it) 1 else 0))
        }
        BarisSaklar(Art.ikon_musik, Art.ikon_musik_terang, "Musik", pengaturan.musik == 1) {
            c.progress.musicOn = it
            c.audio.onMusicSettingChanged()
            ubah(pengaturan.copy(musik = if (it) 1 else 0))
        }
        BarisSaklar(Art.ikon_suara, Art.ikon_suara_terang, "Getar (haptik)", pengaturan.haptik == 1) {
            ubah(pengaturan.copy(haptik = if (it) 1 else 0))
        }
        BarisSaklar(Art.ikon_mode_expert, Art.ikon_mode_expert_terang, "Pewarnaan sintaks", pengaturan.pewarnaan_sintaks == 1) {
            ubah(pengaturan.copy(pewarnaan_sintaks = if (it) 1 else 0))
        }

        Text("Tentang", style = MaterialTheme.typography.titleMedium)
        TombolSekunder("Lihat Pengenalan", {
            c.audio.play(Sfx.Klik)
            nav.go(Screen.Pengenalan)
        })
        TombolSekunder("Reset progres", {
            c.audio.play(Sfx.Klik)
            konfirmasi = true
        })
        if (debug) {
            TombolSekunder("Galeri Aset (debug)", {
                c.audio.play(Sfx.Klik)
                nav.go(Screen.GaleriAset)
            })
        }
        Text(
            "CodeFishing · offline · data tersimpan di perangkat",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (konfirmasi) {
        AlertDialog(
            onDismissRequest = { konfirmasi = false },
            title = { Text("Reset progres?") },
            text = { Text("Semua progres stage, ikan, hasil program, tantangan harian, dan nama ikan akan dihapus. Pengaturan tetap disimpan.") },
            confirmButton = {
                TextButton({
                    konfirmasi = false
                    scope.launch {
                        c.game.resetProgres()
                        c.progress.resetProgres()
                        c.audio.play(Sfx.Salah)
                        nav.resetTo(Screen.Home)
                    }
                }) { Text("Reset") }
            },
            dismissButton = { TextButton({ konfirmasi = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun BarisSaklar(
    ikon: Int,
    ikonTerang: Int,
    label: String,
    nilai: Boolean,
    onUbah: (Boolean) -> Unit,
) {
    val gelap = LocalWarna.current.gelap
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Gambar(if (gelap) ikon else ikonTerang, null, Modifier.size(28.dp))
        Text(label, Modifier.weight(1f))
        Switch(checked = nilai, onCheckedChange = onUbah)
    }
}

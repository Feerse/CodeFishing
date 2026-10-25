package com.example.codefishing.frontend.screens

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.TombolSekunder

/**
 * 10 Pengaturan. Frontend.
 * Saklar efek suara dan musik, tombol Lihat Pengenalan, reset progres.
 * Galeri Aset hanya tampil di build debug.
 */
@Composable
fun PengaturanScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val ctx = LocalContext.current
    var sfx by remember { mutableStateOf(c.progress.sfxOn) }
    var musik by remember { mutableStateOf(c.progress.musicOn) }
    var konfirmasi by remember { mutableStateOf(false) }
    val debug = (ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BarisSaklar(Art.ikon_suara, "Efek suara", sfx) {
            sfx = it; c.progress.sfxOn = it; if (it) c.audio.play(Sfx.Klik)
        }
        BarisSaklar(Art.ikon_musik, "Musik", musik) {
            musik = it; c.progress.musicOn = it; c.audio.onMusicSettingChanged()
        }
        TombolSekunder("Lihat Pengenalan", { c.audio.play(Sfx.Klik); nav.go(Screen.Pengenalan) })
        TombolSekunder("Reset progres", { c.audio.play(Sfx.Klik); konfirmasi = true })
        if (debug) TombolSekunder("Galeri Aset (debug)", { nav.go(Screen.GaleriAset) })
    }

    if (konfirmasi) {
        AlertDialog(
            onDismissRequest = { konfirmasi = false },
            title = { Text("Reset progres?") },
            text = { Text("Semua progres stage, ikan, dan bintang akan dihapus.") },
            confirmButton = {
                TextButton({ c.progress.resetProgres(); konfirmasi = false }) { Text("Reset") }
            },
            dismissButton = { TextButton({ konfirmasi = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun BarisSaklar(ikon: Int, label: String, nilai: Boolean, onUbah: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Gambar(ikon, null, Modifier.size(28.dp))
        Text(label, Modifier.weight(1f))
        Switch(checked = nilai, onCheckedChange = onUbah)
    }
}

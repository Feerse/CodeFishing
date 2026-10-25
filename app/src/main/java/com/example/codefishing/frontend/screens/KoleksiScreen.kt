package com.example.codefishing.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.core.db.KoleksiLengkapView
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.ChipTier
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.Kosong
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan
import com.example.codefishing.frontend.components.tierWarna
import kotlinx.coroutines.launch

private val filterTier = listOf(
    null to "Semua",
    "biasa" to "Biasa",
    "langka" to "Langka",
    "epik" to "Epik",
    "legenda" to "Legenda",
    "mitos" to "Mitos",
)

@Composable
fun KoleksiScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    var filter by remember { mutableStateOf<String?>(null) }
    val list by c.game.observeKoleksi(filter).collectAsState(emptyList())
    val statistik by c.game.observeStatistik().collectAsState(null)
    var dipilih by remember { mutableStateOf<KoleksiLengkapView?>(null) }
    val dimiliki = statistik?.ikan_terkumpul ?: list.count { it.dimiliki == 1 }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(Color(0xFF0B2432), Color(0xFF071923)))),
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Koleksi Ikan", style = MaterialTheme.typography.headlineMedium)
                Text("$dimiliki dari 48 ikan sudah masuk akuarium.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    filterTier.take(3).forEach { (value, label) -> FilterChip(value, label, filter) { filter = it } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    filterTier.drop(3).forEach { (value, label) -> FilterChip(value, label, filter) { filter = it } }
                }
            }

            if (list.isEmpty()) {
                Kosong("Belum ada ikan", "Selesaikan stage atau jalankan misi Sandbox untuk mengisi koleksi.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(list, key = { it.ikan_id }) { ikan ->
                        KartuIkan(ikan) {
                            c.audio.play(Sfx.Klik)
                            dipilih = ikan
                        }
                    }
                }
            }
        }
    }

    dipilih?.let { k ->
        Dialog(onDismissRequest = { dipilih = null }) {
            DetailIkan(k, onTutup = { dipilih = null }, onBeriMakan = {
                c.audio.play(Sfx.Benar)
                dipilih = null
                nav.resetTo(Screen.Akuarium)
            })
        }
    }
}

@Composable
private fun FilterChip(value: String?, label: String, aktif: String?, onClick: (String?) -> Unit) {
    val on = aktif == value
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = if (on) Color(0xFF062B2A) else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(
                if (on) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(50),
            )
            .clickable { onClick(value) }
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun KartuIkan(ikan: KoleksiLengkapView, onClick: () -> Unit) {
    val dimiliki = ikan.dimiliki == 1
    val warna = if (dimiliki) tierWarna(ikan.tier) else Color(0xFF30424A)
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF123340), RoundedCornerShape(16.dp))
            .border(1.dp, warna.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(92.dp), contentAlignment = Alignment.Center) {
            Gambar(
                gambarIkan(ikan.gambar),
                if (dimiliki) ikan.nama else "Ikan terkunci",
                Modifier.size(84.dp),
            )
            if (!dimiliki) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(Color(0xAA061A24), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🔒", fontSize = 26.sp)
                }
            }
        }
        Text(
            if (dimiliki) ikan.nama else "???",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            if (ikan.syarat_unlock?.contains("Segera hadir") == true) "Segera hadir" else if (dimiliki) "×${ikan.jumlah} · ${"%.1f".format(ikan.bobot_terbesar)} kg" else (ikan.syarat_unlock ?: "Belum ditemukan"),
            style = MaterialTheme.typography.labelSmall,
            color = if (dimiliki) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.42f),
            textAlign = TextAlign.Center,
        )
        if (dimiliki) ChipTier(ikan.tier)
    }
}

@Composable
private fun DetailIkan(ikan: KoleksiLengkapView, onTutup: () -> Unit, onBeriMakan: () -> Unit) {
    val c = LocalContainer.current
    val scope = rememberCoroutineScope()
    val detail by produceState<IkanEntity?>(null, ikan.ikan_id) { value = c.game.ikan(ikan.ikan_id) }
    val dimiliki = ikan.dimiliki == 1

    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF0E2C39), RoundedCornerShape(24.dp))
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Gambar(gambarIkan(ikan.gambar), if (dimiliki) ikan.nama else "Ikan terkunci", Modifier.size(150.dp))
        Text(if (dimiliki) ikan.nama else "Belum ditemukan", style = MaterialTheme.typography.headlineSmall)
        detail?.nama_ilmiah?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (dimiliki) ChipTier(ikan.tier)
        Text(ikan.deskripsi, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (!dimiliki) {
            Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF2A2A17)) {
                Text(ikan.syarat_unlock ?: "Selesaikan tantangan untuk membuka ikan ini.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFD98A))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Jumlah ×${ikan.jumlah}", style = MaterialTheme.typography.labelLarge)
                Text("Terberat ${"%.1f".format(ikan.bobot_terbesar)} kg", style = MaterialTheme.typography.labelLarge)
                Text("Kenyang ${ikan.kenyang}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
            }
            Text("Konsep: ${ikan.konsep_python}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            TombolSekunder(
                teks = "Beri Makan di Akuarium",
                onClick = {
                    scope.launch {
                        c.game.beriMakan(ikan.ikan_id)
                        onBeriMakan()
                    }
                },
            )
        }
        TombolUtama(teks = "Tutup", onClick = onTutup)
        Spacer(Modifier.height(4.dp))
    }
}

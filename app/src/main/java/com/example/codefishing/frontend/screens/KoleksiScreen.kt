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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.HasilTambahAkuarium
import com.example.codefishing.core.data.KAPASITAS_AKUARIUM
import com.example.codefishing.core.data.cekTambahAkuarium
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
import com.example.codefishing.frontend.components.gambarIkanTerkunci
import com.example.codefishing.frontend.components.lencanaTier
import com.example.codefishing.frontend.components.tierLabel
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.components.tierWarna
import com.example.codefishing.ui.theme.LocalWarna
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
    val warna = LocalWarna.current
    var filter by remember { mutableStateOf<String?>(null) }
    val list by c.game.observeKoleksi(filter).collectAsState(emptyList())
    val statistik by c.game.observeStatistik().collectAsState(null)
    val jumlahAkuarium by c.game.observeJumlahAkuarium().collectAsState(emptyMap())
    val totalAkuarium = jumlahAkuarium.values.sum()
    var dipilih by remember { mutableStateOf<KoleksiLengkapView?>(null) }
    val dimiliki = statistik?.ikan_terkumpul ?: list.count { it.dimiliki == 1 }
    val totalIkan by produceState<Int?>(null) { value = c.game.jumlahIkan() }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(warna.latarAtas, warna.latarBawah))),
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Koleksi Ikan", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "$dimiliki dari ${totalIkan ?: "…"} jenis ikan sudah dikoleksi · akuarium $totalAkuarium/${KAPASITAS_AKUARIUM} ekor",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                        KartuIkan(ikan, jumlahAkuarium[ikan.ikan_id] ?: 0) {
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
            DetailIkan(
                ikan = k,
                jumlahDiAkuarium = jumlahAkuarium[k.ikan_id] ?: 0,
                totalDiAkuarium = totalAkuarium,
                onTutup = { dipilih = null },
                onLihatAkuarium = {
                    c.audio.play(Sfx.Klik)
                    dipilih = null
                    nav.resetTo(Screen.Akuarium)
                },
            )
        }
    }
}

@Composable
private fun FilterChip(value: String?, label: String, aktif: String?, onClick: (String?) -> Unit) {
    val on = aktif == value
    val warna = LocalWarna.current
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(
                if (on) MaterialTheme.colorScheme.primary else warna.kartuLembut,
                RoundedCornerShape(50),
            )
            .clickable { onClick(value) }
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun KartuIkan(ikan: KoleksiLengkapView, jumlahAkuarium: Int, onClick: () -> Unit) {
    val dimiliki = ikan.dimiliki == 1
    val palet = LocalWarna.current
    val warnaTier = if (dimiliki) tierWarna(ikan.tier) else palet.petaTeksRedup
    Column(
        Modifier
            .fillMaxWidth()
            .background(palet.kartuLembut, RoundedCornerShape(16.dp))
            .border(1.dp, warnaTier.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(92.dp), contentAlignment = Alignment.Center) {
            Gambar(
                if (dimiliki) gambarIkan(ikan.gambar) else gambarIkanTerkunci(ikan.gambar),
                if (dimiliki) ikan.nama else "Ikan terkunci",
                Modifier.size(84.dp),
            )
            if (!dimiliki) {
                Gambar(
                    if (palet.gelap) Art.ikon_gembok else Art.ikon_gembok_terang,
                    "Terkunci",
                    Modifier.align(Alignment.BottomEnd).size(24.dp),
                )
            } else {
                Gambar(lencanaTier(ikan.tier), tierLabel(ikan.tier), Modifier.align(Alignment.TopStart).size(32.dp))
            }
        }
        Text(
            if (dimiliki) ikan.nama else "???",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            if (dimiliki) {
                val stat = "×${ikan.jumlah} · ${"%.1f".format(ikan.bobot_terbesar)} kg"
                if (jumlahAkuarium > 0) "$stat · $jumlahAkuarium di akuarium" else stat
            } else {
                ikan.syarat_unlock ?: "Belum ditemukan"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (dimiliki) MaterialTheme.colorScheme.onSurfaceVariant else palet.petaTeksRedup.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (dimiliki) ChipTier(ikan.tier)
    }
}

@Composable
private fun DetailIkan(
    ikan: KoleksiLengkapView,
    jumlahDiAkuarium: Int,
    totalDiAkuarium: Int,
    onTutup: () -> Unit,
    onLihatAkuarium: () -> Unit,
) {
    val c = LocalContainer.current
    val scope = rememberCoroutineScope()
    val detail by produceState<IkanEntity?>(null, ikan.ikan_id) { value = c.game.ikan(ikan.ikan_id) }
    val dimiliki = ikan.dimiliki == 1
    val palet = LocalWarna.current
    var pesan by remember { mutableStateOf<String?>(null) }
    val bisaTambah = cekTambahAkuarium(jumlahDiAkuarium, ikan.jumlah, totalDiAkuarium) ==
        HasilTambahAkuarium.Berhasil

    Column(
        Modifier
            .fillMaxWidth()
            .background(palet.kartuTinggi, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Gambar(
            if (dimiliki) gambarIkan(ikan.gambar) else gambarIkanTerkunci(ikan.gambar),
            if (dimiliki) ikan.nama else "Ikan terkunci",
            Modifier.size(150.dp),
        )
        Text(if (dimiliki) ikan.nama else "Belum ditemukan", style = MaterialTheme.typography.headlineSmall)
        detail?.nama_ilmiah?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (dimiliki) {
            Gambar(lencanaTier(ikan.tier), tierLabel(ikan.tier), Modifier.size(56.dp))
            ChipTier(ikan.tier)
        } else {
            Gambar(
                if (palet.gelap) Art.ikon_gembok else Art.ikon_gembok_terang,
                "Terkunci",
                Modifier.size(36.dp),
            )
        }
        Text(ikan.deskripsi, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (!dimiliki) {
            Kartu(Modifier.fillMaxWidth(), warna = palet.peringatanLatar) {
                Text(ikan.syarat_unlock ?: "Selesaikan tantangan untuk membuka ikan ini.", style = MaterialTheme.typography.bodySmall, color = palet.peringatanTeks)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Jumlah ×${ikan.jumlah}", style = MaterialTheme.typography.labelLarge)
                Text("Terberat ${"%.1f".format(ikan.bobot_terbesar)} kg", style = MaterialTheme.typography.labelLarge)
                Text("Kenyang ${ikan.kenyang}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
            }
            Kartu(Modifier.fillMaxWidth(), warna = palet.kartuLembut) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Akuarium", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "$jumlahDiAkuarium dari ${ikan.jumlah} ekor di akuarium · $totalDiAkuarium/${KAPASITAS_AKUARIUM} ekor terisi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TombolSekunder(
                            teks = "− Kurangi",
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                pesan = null
                                scope.launch { c.game.kurangiIkanDariAkuarium(ikan.ikan_id) }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = jumlahDiAkuarium > 0,
                        )
                        TombolSekunder(
                            teks = "+ Tambah",
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                scope.launch {
                                    pesan = when (c.game.tambahIkanKeAkuarium(ikan.ikan_id)) {
                                        HasilTambahAkuarium.Berhasil -> null
                                        HasilTambahAkuarium.Penuh -> "Akuarium penuh ($KAPASITAS_AKUARIUM/$KAPASITAS_AKUARIUM). Keluarkan ikan lain dulu."
                                        HasilTambahAkuarium.StokHabis -> "Semua ekor jenis ini sudah ada di akuarium."
                                        HasilTambahAkuarium.TidakDimiliki -> "Ikan ini belum ada di koleksi."
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = bisaTambah,
                        )
                    }
                    if (pesan != null) {
                        pesan?.let { p ->
                            Text(p, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                        }
                    } else if (totalDiAkuarium >= KAPASITAS_AKUARIUM) {
                        Text(
                            "Akuarium penuh. Keluarkan ikan lain dulu.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else if (jumlahDiAkuarium >= ikan.jumlah) {
                        Text(
                            "Semua ekor jenis ini sudah ada di akuarium.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text("Konsep: ${ikan.konsep_python}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            TombolSekunder(
                teks = "Beri Makan",
                onClick = {
                    c.audio.play(Sfx.Benar)
                    scope.launch { c.game.beriMakan(ikan.ikan_id) }
                },
            )
            if (jumlahDiAkuarium > 0) {
                TombolSekunder(teks = "Lihat di Akuarium", onClick = onLihatAkuarium)
            }
        }
        TombolUtama(teks = "Tutup", onClick = onTutup)
        Spacer(Modifier.height(4.dp))
    }
}

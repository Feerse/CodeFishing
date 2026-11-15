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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.codefishing.core.i18n.LocalTeks
import com.example.codefishing.core.i18n.t
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.gambarIkanTerkunciKecil
import com.example.codefishing.frontend.components.gambarIkanKecil
import com.example.codefishing.frontend.components.GambarKontras
import com.example.codefishing.frontend.components.BingkaiTier
import com.example.codefishing.frontend.components.AuraIkan
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
import com.example.codefishing.pemula.WilayahIkan
import com.example.codefishing.pemula.WilayahIkanAturan
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
    var wilayah by remember { mutableStateOf<WilayahIkan?>(null) }
    val list by c.game.observeKoleksi(filter).collectAsState(emptyList())
    val statistik by c.game.observeStatistik().collectAsState(null)
    val jumlahAkuarium by c.game.observeJumlahAkuarium().collectAsState(emptyMap())
    val totalAkuarium = jumlahAkuarium.values.sum()
    var dipilih by remember { mutableStateOf<KoleksiLengkapView?>(null) }
    val dimiliki = statistik?.ikan_terkumpul ?: list.count { it.dimiliki == 1 }
    val totalIkan by produceState<Int?>(null) { value = c.game.jumlahIkan() }

    // Peta asal tiap ikan. Nomor stage diambil dari katalog stage (bukan dari asal_stage_id yang
    // berupa id), lalu wilayahnya ditentukan WilayahIkanAturan.
    val stages by c.game.observeStageList().collectAsState(emptyList())
    val katalog by produceState<List<IkanEntity>>(emptyList()) { value = c.game.semuaIkan() }
    val wilayahIkan = remember(katalog, stages) {
        val nomor = stages.associate { it.id to it.nomor }
        katalog.associate { ikan ->
            ikan.id to WilayahIkanAturan.dari(nomor[ikan.asal_stage_id], ikan.tier, ikan.syarat_unlock)
        }
    }
    val tampil = remember(list, wilayah, wilayahIkan) {
        if (wilayah == null) list else list.filter { wilayahIkan[it.ikan_id] == wilayah }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(warna.latarAtas, warna.latarBawah))),
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t("Koleksi Ikan"), style = MaterialTheme.typography.headlineMedium)
                Text(
                    t(
                        "{} dari {} jenis ikan sudah dikoleksi · akuarium {}/{} ekor",
                        dimiliki,
                        totalIkan ?: "…",
                        totalAkuarium,
                        KAPASITAS_AKUARIUM,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    filterTier.take(3).forEach { (value, label) -> FilterChip(value, label, filter) { filter = it } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    filterTier.drop(3).forEach { (value, label) -> FilterChip(value, label, filter) { filter = it } }
                }
                // Empat chip peta berbagi lebar baris, tanpa gulir mendatar: chip yang tergulir
                // membuat ketukan di tepinya meleset. Angka progres pindah ke judul bagian supaya
                // label chip cukup satu kata.
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ChipSaring(wilayah == null, t("Semua"), Modifier.weight(1f)) { wilayah = null }
                    WilayahIkan.entries.forEach { w ->
                        ChipSaring(
                            aktif = wilayah == w,
                            teks = t(w.labelSingkat),
                            modifier = Modifier.weight(1f),
                        ) { wilayah = if (wilayah == w) null else w }
                    }
                }
            }

            if (tampil.isEmpty()) {
                Kosong(
                    t("Belum ada ikan"),
                    t("Selesaikan stage atau jalankan misi Sandbox untuk mengisi koleksi."),
                )
            } else {
                // Efek kelangkaan dibekukan selama grid digulir: menggambar ulang puluhan kanvas
                // beranimasi saat scroll adalah penyebab utama patah-patah di daftar panjang.
                val gridState = rememberLazyGridState()
                // Isi grid berganti setiap kali saringan peta/tier berubah. Tanpa gulir ke atas,
                // grid tetap di posisi lama, jadi bagian yang terlihat bukan bagian pertama peta
                // yang baru dipilih (mis. memilih "Semua" dari Khusus tetap menampilkan Khusus).
                LaunchedEffect(wilayah, filter) { gridState.scrollToItem(0) }
                val animasiKartu = !gridState.isScrollInProgress
                // Saat tidak ada filter peta, grid dibagi menjadi tiga bagian berjudul. Saat filter
                // peta aktif, judulnya hanya satu supaya daftar tidak terasa berulang.
                val kelompok = if (wilayah != null) {
                    listOf(wilayah!! to tampil)
                } else {
                    WilayahIkan.entries.map { w -> w to tampil.filter { wilayahIkan[it.ikan_id] == w } }
                }
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    kelompok.forEach { (w, anggota) ->
                        if (anggota.isEmpty()) return@forEach
                        item(key = "kepala_${w.name}", span = { GridItemSpan(maxLineSpan) }) {
                            KepalaWilayah(w, anggota.count { it.dimiliki == 1 }, anggota.size)
                        }
                        items(anggota, key = { it.ikan_id }) { ikan ->
                            KartuIkan(ikan, jumlahAkuarium[ikan.ikan_id] ?: 0, animasiKartu) {
                                c.audio.play(Sfx.Klik)
                                dipilih = ikan
                            }
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
                wilayah = wilayahIkan[k.ikan_id],
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

/**
 * Judul bagian grid koleksi: satu baris tipis beraksen, nama wilayah, dan progres wilayah itu.
 * Bentuknya sengaja bukan kartu supaya tidak bersaing dengan kartu ikan di bawahnya.
 */
@Composable
private fun KepalaWilayah(wilayah: WilayahIkan, dimiliki: Int, total: Int) {
    val palet = LocalWarna.current
    val aksen = warnaWilayah(wilayah)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(width = 4.dp, height = 18.dp)
                .background(aksen, RoundedCornerShape(50)),
        )
        Text(
            t(wilayah.label),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = aksen,
            modifier = Modifier.weight(1f),
        )
        Text(
            t("{}/{} jenis", dimiliki, total),
            style = MaterialTheme.typography.labelSmall,
            color = palet.petaTeksRedup,
        )
    }
}

@Composable
private fun warnaWilayah(wilayah: WilayahIkan): Color {
    val palet = LocalWarna.current
    return when (wilayah) {
        WilayahIkan.PASIFIK -> palet.suksesTeks
        WilayahIkan.ANTARTIKA -> palet.rarityLangka
        WilayahIkan.KHUSUS -> palet.rarityLegenda
    }
}

@Composable
private fun FilterChip(value: String?, label: String, aktif: String?, onClick: (String?) -> Unit) {
    ChipSaring(aktif == value, t(label)) { onClick(value) }
}

/**
 * Chip saring bersama untuk filter tier dan filter peta.
 *
 * [modifier] dipakai chip peta untuk berbagi lebar baris (`weight`), sehingga teks dipusatkan dan
 * dipotong satu baris alih-alih melebarkan chip.
 */
@Composable
private fun ChipSaring(
    aktif: Boolean,
    teks: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val warna = LocalWarna.current
    Text(
        teks,
        style = MaterialTheme.typography.labelMedium,
        color = if (aktif) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(
                if (aktif) MaterialTheme.colorScheme.primary else warna.kartuLembut,
                RoundedCornerShape(50),
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 7.dp),
    )
}

@Composable
private fun KartuIkan(ikan: KoleksiLengkapView, jumlahAkuarium: Int, animasi: Boolean, onClick: () -> Unit) {
    val dimiliki = ikan.dimiliki == 1
    val palet = LocalWarna.current
    // Ikan yang dimiliki dibingkai sesuai kelangkaannya (bentuk bingkai, ornamen, dan efek berbeda
    // tiap tier). Ikan terkunci memakai bingkai polos redup.
    val tierBingkai = if (dimiliki) ikan.tier else "biasa"
    BingkaiTier(
        tier = tierBingkai,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        animasi = animasi,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(Modifier.fillMaxWidth().height(92.dp), contentAlignment = Alignment.Center) {
                if (dimiliki) {
                    AuraIkan(ikan.tier, Modifier.size(92.dp), animasi = animasi) {
                        GambarKontras(gambarIkanKecil(ikan.gambar), ikan.nama, Modifier.size(84.dp))
                    }
                } else {
                    Gambar(gambarIkanTerkunciKecil(ikan.gambar), t("Ikan terkunci"), Modifier.size(84.dp))
                }
                if (!dimiliki) {
                    Gambar(
                        if (palet.gelap) Art.ikon_gembok else Art.ikon_gembok_terang,
                        t("Terkunci"),
                        Modifier.align(Alignment.BottomEnd).size(24.dp),
                    )
                } else {
                    Gambar(lencanaTier(ikan.tier), t(tierLabel(ikan.tier)), Modifier.align(Alignment.TopStart).size(32.dp))
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
                    val stat = t("×{} · {} kg", ikan.jumlah, "%.1f".format(ikan.bobot_terbesar))
                    if (jumlahAkuarium > 0) t("{} · {} di akuarium", stat, jumlahAkuarium) else stat
                } else {
                    ikan.syarat_unlock ?: t("Belum ditemukan")
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
}

@Composable
private fun DetailIkan(
    ikan: KoleksiLengkapView,
    wilayah: WilayahIkan?,
    jumlahDiAkuarium: Int,
    totalDiAkuarium: Int,
    onTutup: () -> Unit,
    onLihatAkuarium: () -> Unit,
) {
    val c = LocalContainer.current
    val scope = rememberCoroutineScope()
    val tr = LocalTeks.current
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
        if (dimiliki) {
            AuraIkan(ikan.tier, Modifier.size(190.dp)) {
                GambarKontras(gambarIkan(ikan.gambar), ikan.nama, Modifier.size(150.dp))
            }
        } else {
            Gambar(gambarIkanTerkunci(ikan.gambar), t("Ikan terkunci"), Modifier.size(150.dp))
        }
        Text(if (dimiliki) ikan.nama else t("Belum ditemukan"), style = MaterialTheme.typography.headlineSmall)
        detail?.nama_ilmiah?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (wilayah != null) {
            // Peta asal: satu-satunya petunjuk di layar ini tentang di mana ikan itu dicari.
            val aksen = warnaWilayah(wilayah)
            Text(
                t("Didapat di {}", t(wilayah.label)),
                Modifier
                    .background(aksen.copy(alpha = 0.14f), RoundedCornerShape(50))
                    .border(1.dp, aksen.copy(alpha = 0.45f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = aksen,
                fontWeight = FontWeight.Bold,
            )
        }
        if (dimiliki) {
            Gambar(lencanaTier(ikan.tier), t(tierLabel(ikan.tier)), Modifier.size(56.dp))
            ChipTier(ikan.tier)
        } else {
            Gambar(
                if (palet.gelap) Art.ikon_gembok else Art.ikon_gembok_terang,
                t("Terkunci"),
                Modifier.size(36.dp),
            )
        }
        Text(ikan.deskripsi, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        if (!dimiliki) {
            Kartu(Modifier.fillMaxWidth(), warna = palet.peringatanLatar) {
                Text(ikan.syarat_unlock ?: t("Selesaikan tantangan untuk membuka ikan ini."), style = MaterialTheme.typography.bodySmall, color = palet.peringatanTeks)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(t("Jumlah ×{}", ikan.jumlah), style = MaterialTheme.typography.labelLarge)
                Text(t("Terberat {} kg", "%.1f".format(ikan.bobot_terbesar)), style = MaterialTheme.typography.labelLarge)
                Text(t("Kenyang {}%", ikan.kenyang), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
            }
            Kartu(Modifier.fillMaxWidth(), warna = palet.kartuLembut) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(t("Akuarium"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        t(
                            "{} dari {} ekor di akuarium · {}/{} ekor terisi",
                            jumlahDiAkuarium,
                            ikan.jumlah,
                            totalDiAkuarium,
                            KAPASITAS_AKUARIUM,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TombolSekunder(
                            teks = t("− Kurangi"),
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                pesan = null
                                scope.launch { c.game.kurangiIkanDariAkuarium(ikan.ikan_id) }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = jumlahDiAkuarium > 0,
                        )
                        TombolSekunder(
                            teks = t("+ Tambah"),
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                scope.launch {
                                    pesan = when (c.game.tambahIkanKeAkuarium(ikan.ikan_id)) {
                                        HasilTambahAkuarium.Berhasil -> null
                                        HasilTambahAkuarium.Penuh -> tr.format(
                                            "Akuarium penuh ({}/{}). Keluarkan ikan lain dulu.",
                                            KAPASITAS_AKUARIUM,
                                            KAPASITAS_AKUARIUM,
                                        )
                                        HasilTambahAkuarium.StokHabis -> tr("Semua ekor jenis ini sudah ada di akuarium.")
                                        HasilTambahAkuarium.TidakDimiliki -> tr("Ikan ini belum ada di koleksi.")
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
                            t("Akuarium penuh. Keluarkan ikan lain dulu."),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else if (jumlahDiAkuarium >= ikan.jumlah) {
                        Text(
                            t("Semua ekor jenis ini sudah ada di akuarium."),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(t("Konsep: {}", ikan.konsep_python), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            TombolSekunder(
                teks = t("Beri Makan"),
                onClick = {
                    c.audio.play(Sfx.Benar)
                    scope.launch { c.game.beriMakan(ikan.ikan_id) }
                },
            )
            if (jumlahDiAkuarium > 0) {
                TombolSekunder(teks = t("Lihat di Akuarium"), onClick = onLihatAkuarium)
            }
        }
        TombolUtama(teks = t("Tutup"), onClick = onTutup)
        Spacer(Modifier.height(4.dp))
    }
}

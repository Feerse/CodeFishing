package com.example.codefishing.frontend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.expert.MisiPeta
import com.example.codefishing.expert.PanduanExpert
import com.example.codefishing.expert.PanduanPerintah
import com.example.codefishing.expert.PanduanSintaks
import com.example.codefishing.expert.TombolSisip
import com.example.codefishing.ui.theme.LocalWarna

private enum class TabPanduan(val label: String) {
    Perintah("Perintah"),
    Sintaks("Sintaks"),
    Misi("Misi"),
}

/**
 * Guidebook Mode Expert: referensi perintah, sintaks, dan semua misi.
 * Dibuka sebagai bottom sheet agar kode dan posisi editor tidak hilang.
 * [onSisip] null berarti mode baca saja (mis. dibuka dari daftar misi).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanduanExpertSheet(
    semuaMisi: List<MisiPeta>,
    misiAktif: Int?,
    bintang: Map<Int, Int>,
    onTutup: () -> Unit,
    onPilihMisi: (Int) -> Unit,
    onSisip: ((String, Int) -> Unit)? = null,
    bukaSintaks: Boolean = false,
) {
    val warna = LocalWarna.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tab by remember { mutableStateOf(if (bukaSintaks) TabPanduan.Sintaks else TabPanduan.Perintah) }

    ModalBottomSheet(
        onDismissRequest = onTutup,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp),
        ) {
            Text("Panduan Expert", style = MaterialTheme.typography.titleLarge)
            Text(
                "Referensi perintah, sintaks Python, dan petunjuk semua misi. Kamu tidak perlu keluar dari editor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabPanduan.entries.forEach { pilihan ->
                    FilterChip(
                        selected = tab == pilihan,
                        onClick = { tab = pilihan },
                        label = { Text(pilihan.label) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when (tab) {
                    TabPanduan.Perintah -> {
                        item { KartuPetunjukPapan() }
                        items(PanduanExpert.perintah, key = { it.nama }) { item ->
                            KartuPerintah(item, onSisip)
                        }
                    }

                    TabPanduan.Sintaks -> {
                        items(PanduanExpert.sintaks, key = { it.judul }) { item ->
                            KartuSintaks(item, onSisip)
                        }
                        item { JudulBagian("Error umum") }
                        items(PanduanExpert.masalah, key = { it.masalah }) { masalah ->
                            Kartu(Modifier.fillMaxWidth(), warna = warna.peringatanLatar) {
                                Text(
                                    masalah.masalah,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = warna.peringatanTeks,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(masalah.solusi, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        item { KartuBelumDidukung() }
                    }

                    TabPanduan.Misi -> {
                        item {
                            Text(
                                "Pilih misi untuk membukanya. Draf kode tiap misi tetap tersimpan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        items(semuaMisi, key = { it.kode }) { misi ->
                            KartuMisi(
                                misi = misi,
                                bintang = bintang[misi.dbId] ?: 0,
                                aktif = misi.nomor == misiAktif,
                                onClick = { onPilihMisi(misi.nomor) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KartuPetunjukPapan() {
    val warna = LocalWarna.current
    Kartu(Modifier.fillMaxWidth(), warna = warna.kartuLembut) {
        Text("Cara kerja papan", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        PanduanExpert.petunjukPapan.forEach { baris ->
            Text(
                "• $baris",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}

@Composable
private fun KartuPerintah(item: PanduanPerintah, onSisip: ((String, Int) -> Unit)?) {
    val warna = LocalWarna.current
    Kartu(Modifier.fillMaxWidth(), warna = warna.kartuLembut) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.nama,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            Text(
                item.biaya,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(item.ringkas, style = MaterialTheme.typography.bodyMedium)
        if (!item.hasil.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Hasil: ${item.hasil}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!item.contoh.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            KodeBlok(item.contoh, judul = "CONTOH")
        }
        val aksi = onSisip
        val sisip = item.sisip
        if (aksi != null && sisip != null) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = { aksi(sisip, item.kursorMundur) },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) { Text("Sisipkan") }
            }
        }
    }
}

@Composable
private fun KartuSintaks(item: PanduanSintaks, onSisip: ((String, Int) -> Unit)?) {
    val warna = LocalWarna.current
    Kartu(Modifier.fillMaxWidth(), warna = warna.kartuLembut) {
        Text(item.judul, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(item.ringkas, style = MaterialTheme.typography.bodyMedium)
        if (!item.contoh.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            KodeBlok(item.contoh, judul = "CONTOH")
        }
        val aksi = onSisip
        val sisip = item.sisip
        if (aksi != null && sisip != null) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = { aksi(sisip, item.kursorMundur) },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) { Text("Sisipkan") }
            }
        }
    }
}

@Composable
private fun KartuBelumDidukung() {
    val warna = LocalWarna.current
    Kartu(Modifier.fillMaxWidth(), warna = warna.bahayaLatar) {
        Text("Belum didukung", style = MaterialTheme.typography.titleMedium, color = warna.bahayaTeks)
        Spacer(Modifier.height(6.dp))
        PanduanExpert.belumDidukung.forEach { baris ->
            Text(
                "• $baris",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
    }
}

@Composable
private fun JudulBagian(teks: String) {
    Text(
        teks,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun KartuMisi(
    misi: MisiPeta,
    bintang: Int,
    aktif: Boolean,
    onClick: () -> Unit,
) {
    val warna = LocalWarna.current
    Kartu(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        warna = if (aktif) warna.kartuTinggi else warna.kartuLembut,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Misi ${misi.nomor} · ${misi.judul}", style = MaterialTheme.typography.titleMedium)
                Text(
                    misi.deskripsi,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Bintang(bintang, ukuran = 14)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (misi.par != null) "Par ${misi.par} tick" else "Tanpa par",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
            if (aktif) {
                Text(
                    "SEDANG DIBUKA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Petunjuk: ${misi.hint}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Baris tombol pintasan di bawah editor. */
@Composable
fun BarisPintasanKode(
    onSisip: (TombolSisip) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            "Sisip cepat",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(end = 4.dp),
        ) {
            items(PanduanExpert.tombolSisip, key = { it.label }) { tombol ->
                AssistChip(
                    onClick = { onSisip(tombol) },
                    enabled = enabled,
                    label = {
                        Text(
                            tombol.label,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                        )
                    },
                    modifier = Modifier.semantics { contentDescription = tombol.deskripsi },
                )
            }
        }
    }
}
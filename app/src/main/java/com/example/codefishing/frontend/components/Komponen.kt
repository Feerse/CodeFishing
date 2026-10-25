package com.example.codefishing.frontend.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.art.Art

/** Gambar dari katalog aset. Isi [deskripsi] dengan teks pembaca layar, atau null bila hanya hiasan. */
@Composable
fun Gambar(
    @DrawableRes id: Int,
    deskripsi: String?,
    modifier: Modifier = Modifier,
    skala: ContentScale = ContentScale.Fit,
) {
    Image(painterResource(id), contentDescription = deskripsi, modifier = modifier, contentScale = skala)
}

/** Indikator umpan di toolbar layar Mode Belajar. */
@Composable
fun IndikatorUmpan(jumlah: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Gambar(Art.umpan_penuh, "Umpan", Modifier.size(22.dp))
        Text("$jumlah", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun TombolUtama(teks: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth()) { Text(teks) }
}

@Composable
fun TombolSekunder(teks: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.fillMaxWidth()) { Text(teks) }
}

/**
 * Layar sementara untuk fitur yang belum dibuat. Menunjukkan pemilik, spesifikasi dari prototipe,
 * dan tombol uji navigasi. Hapus pemakaiannya saat layar asli selesai.
 */
@Composable
fun LayarSementara(
    padding: androidx.compose.foundation.layout.PaddingValues,
    pemilik: String,
    spesifikasi: String,
    tombol: List<Pair<String, () -> Unit>> = emptyList(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Belum dibuat. Pemilik: $pemilik", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(spesifikasi, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        tombol.forEach { (label, aksi) -> TombolSekunder(label, aksi) }
    }
}

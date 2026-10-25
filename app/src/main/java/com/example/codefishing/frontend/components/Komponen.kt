package com.example.codefishing.frontend.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().then(modifier)) { Text(teks) }
}

@Composable
fun TombolSekunder(teks: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().then(modifier)) { Text(teks) }
}

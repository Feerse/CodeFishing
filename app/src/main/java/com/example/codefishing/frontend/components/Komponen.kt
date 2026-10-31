package com.example.codefishing.frontend.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

/** Indikator umpan di toolbar layar Mode Belajar: lima cacing, terisi sebanyak sisa umpan. */
@Composable
fun IndikatorUmpan(jumlah: Int, maks: Int = 5) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(maks) { i ->
            Gambar(
                if (i < jumlah) Art.umpan_penuh else Art.umpan_kosong,
                if (i < jumlah) "Umpan terisi" else "Umpan kosong",
                Modifier.size(18.dp),
            )
        }
        Text(
            "$jumlah/$maks",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
fun TombolUtama(
    teks: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        contentPadding = contentPadding,
    ) { Text(teks) }
}

@Composable
fun TombolSekunder(
    teks: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        contentPadding = contentPadding,
    ) { Text(teks) }
}

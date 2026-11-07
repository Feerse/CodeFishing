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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.art.Art
import com.example.codefishing.ui.theme.LocalWarna

/**
 * Gambar dari katalog aset. Isi [deskripsi] dengan teks pembaca layar, atau null bila hanya hiasan.
 * [blendMode] dipakai bersama [warna]; `Modulate` menggelapkan tanpa menghapus bayangan aset.
 */
@Composable
fun Gambar(
    @DrawableRes id: Int,
    deskripsi: String?,
    modifier: Modifier = Modifier,
    skala: ContentScale = ContentScale.Fit,
    warna: Color? = null,
    blendMode: BlendMode = BlendMode.SrcIn,
    perataan: Alignment = Alignment.Center,
) {
    Image(
        painterResource(id),
        contentDescription = deskripsi,
        modifier = modifier,
        contentScale = skala,
        alignment = perataan,
        colorFilter = warna?.let { ColorFilter.tint(it, blendMode) },
    )
}

/** Indikator umpan di toolbar layar Mode Belajar: lima cacing, terisi sebanyak sisa umpan. */
@Composable
fun IndikatorUmpan(jumlah: Int, maks: Int = 5) {
    val gelap = LocalWarna.current.gelap
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(maks) { i ->
            Gambar(
                if (i < jumlah) {
                    if (gelap) Art.umpan_penuh else Art.umpan_penuh_terang
                } else {
                    if (gelap) Art.umpan_kosong else Art.umpan_kosong_terang
                },
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

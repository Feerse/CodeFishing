package com.example.codefishing.frontend.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
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
) {
    val filter = warna?.let { ColorFilter.tint(it, blendMode) }
    // Bitmap diambil dari CacheGambar: didekode sekali lalu dipakai ulang di semua layar, bukan
    // didekode ulang di thread utama tiap layar dibuka (painterResource tidak menyimpan hasilnya).
    val context = LocalContext.current
    val bitmap = remember(id) { CacheGambar.ambil(context, id) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = deskripsi,
            modifier = modifier,
            contentScale = skala,
            colorFilter = filter,
        )
    } else {
        Image(
            painterResource(id),
            contentDescription = deskripsi,
            modifier = modifier,
            contentScale = skala,
            colorFilter = filter,
        )
    }
}

/**
 * Gambar yang tetap tegas di tema terang. Aset ikan dan benda berwarna pucat (teri, bandeng,
 * albatros, gunung es, kail putus, dst.) dibuat untuk latar gelap dan menyatu dengan latar terang.
 * Di tema terang gambar diberi kontur gelap tipis dan bayangan lembut sehingga kontrasnya setara
 * tema gelap. Kontur dibuat sekali di thread latar ([CacheGambar.buatKontur]) lalu disimpan; selama
 * belum siap gambar polos yang tampil. Di tema gelap perilakunya sama persis dengan [Gambar].
 * Ketebalan kontur mengikuti ukuran gambar (±1,4% sisi terpanjang), jadi tampil ±1,4-2 dp.
 */
@Composable
fun GambarKontras(
    @DrawableRes id: Int,
    deskripsi: String?,
    modifier: Modifier = Modifier,
    skala: ContentScale = ContentScale.Fit,
) {
    if (LocalWarna.current.gelap) {
        Gambar(id, deskripsi, modifier, skala)
        return
    }
    val context = LocalContext.current
    // Nilai state harus selalu diambil ulang untuk [id] terbaru. `produceState` menyimpan state-nya
    // walau kunci berganti, jadi pola "hanya hitung bila value == null" membuat gambar lama tertinggal:
    // ikon chip peta tetap menampilkan ikon peta sebelumnya setelah peta berganti.
    val kontur by produceState(CacheGambar.daptKontur(id), id) {
        value = CacheGambar.daptKontur(id)
            ?: withContext(Dispatchers.Default) { CacheGambar.buatKontur(context, id) }
    }
    val k = kontur
    if (k == null) {
        Gambar(id, deskripsi, modifier, skala)
    } else {
        Image(
            bitmap = k.bitmap,
            contentDescription = deskripsi,
            // Bitmap berkontur lebih besar karena bantalan; diskalakan agar ikan tetap seukuran semula.
            modifier = modifier.graphicsLayer {
                scaleX = k.skala
                scaleY = k.skala
            },
            contentScale = skala,
        )
    }
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

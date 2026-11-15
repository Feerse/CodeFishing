package com.example.codefishing.frontend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.i18n.t
import com.example.codefishing.ui.theme.LocalWarna

@Composable
fun tierWarna(tier: String): Color {
    val warna = LocalWarna.current
    return when (tier) {
        "langka" -> warna.rarityLangka
        "epik" -> warna.rarityEpik
        "legenda" -> warna.rarityLegenda
        "mitos" -> warna.rarityMitos
        else -> warna.rarityBiasa
    }
}

fun tierLabel(tier: String): String = when (tier) {
    "langka" -> "Langka"
    "epik" -> "Epik"
    "legenda" -> "Legenda"
    "mitos" -> "Mitos"
    else -> "Biasa"
}

/** Ilustrasi ikan berwarna. Nama aset datang dari seed; tanpa aset memakai tanda tanya. */
fun gambarIkan(gambar: String?): Int =
    gambar?.let { Art.byName(it) } ?: Art.tanda_tanya_ikan

/** Siluet ikan yang belum dimiliki (aset ikan_terkunci_*), dengan cadangan ke versi berwarna. */
fun gambarIkanTerkunci(gambar: String?): Int {
    val nama = gambar ?: return Art.tanda_tanya_ikan
    val kunci = if (nama.startsWith("ikan_terkunci_")) nama
    else "ikan_terkunci_" + nama.removePrefix("ikan_")
    return Art.byName(kunci) ?: Art.byName(nama) ?: Art.tanda_tanya_ikan
}

/**
 * Thumbnail ikan (sisi terpanjang 256 px) untuk grid dan daftar yang menampilkan ikan <= ±90 dp.
 * Mendekode 1/4 piksel dibanding ukuran penuh sehingga grid 58 ikan jauh lebih ringan.
 * Memakai ukuran penuh bila thumbnail belum ada.
 */
fun gambarIkanKecil(gambar: String?): Int {
    val nama = gambar ?: return Art.tanda_tanya_ikan
    return Art.byName(nama + "_kecil") ?: gambarIkan(gambar)
}

/** Thumbnail siluet ikan terkunci; lihat [gambarIkanKecil]. */
fun gambarIkanTerkunciKecil(gambar: String?): Int {
    val nama = gambar ?: return Art.tanda_tanya_ikan
    val kunci = if (nama.startsWith("ikan_terkunci_")) nama
    else "ikan_terkunci_" + nama.removePrefix("ikan_")
    return Art.byName(kunci + "_kecil") ?: gambarIkanTerkunci(gambar)
}

/** Lencana kelangkaan bergambar (Common sampai Mythic) untuk tier ikan. */
fun lencanaTier(tier: String): Int = when (tier) {
    "langka" -> Art.lencana_rare
    "epik" -> Art.lencana_epic
    "legenda" -> Art.lencana_legend
    "mitos" -> Art.lencana_mythic
    else -> Art.lencana_common
}

@Composable
fun Kartu(
    modifier: Modifier = Modifier,
    warna: Color = MaterialTheme.colorScheme.surface,
    isi: @Composable () -> Unit,
) {
    // Column, bukan Box: isi kartu sering terdiri dari beberapa teks/Spacer.
    // Dengan Box semua child menumpuk di posisi yang sama.
    Column(
        modifier
            .background(warna, RoundedCornerShape(20.dp))
            .border(1.dp, LocalWarna.current.garis, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) { isi() }
}

/**
 * Chip kelangkaan. Tiap tier punya lambang berbeda (lingkaran, belah ketupat, bintang, mahkota, api)
 * sehingga terbaca tanpa bergantung pada warna; legenda dan mitos memakai latar gradien dan garis tebal.
 */
@Composable
fun ChipTier(tier: String, modifier: Modifier = Modifier) {
    val warna = tierWarna(tier)
    val k = kelangkaan(tier)
    val bentuk = RoundedCornerShape(50)
    val latar: Brush = when (k) {
        Kelangkaan.LEGENDA -> Brush.horizontalGradient(
            listOf(warna.copy(alpha = 0.30f), Color(0xFFFFE27A).copy(alpha = 0.34f), warna.copy(alpha = 0.30f)),
        )
        Kelangkaan.MITOS -> Brush.horizontalGradient(
            listOf(
                Color(0xFFFF4D6D).copy(alpha = 0.28f),
                Color(0xFFE63DFF).copy(alpha = 0.24f),
                Color(0xFF3DD9FF).copy(alpha = 0.26f),
            ),
        )
        else -> SolidColor(warna.copy(alpha = 0.14f))
    }
    val tebalGaris = when (k) {
        Kelangkaan.LEGENDA, Kelangkaan.MITOS -> 1.6.dp
        Kelangkaan.EPIK, Kelangkaan.LANGKA -> 1.2.dp
        Kelangkaan.BIASA -> 1.dp
    }
    Row(
        modifier = modifier
            .background(latar, bentuk)
            .border(tebalGaris, warna.copy(alpha = if (k == Kelangkaan.BIASA) 0.5f else 0.75f), bentuk)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        LambangTier(tier, 12.dp)
        Text(
            text = t(tierLabel(tier)),
            color = warna,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun Bintang(jumlah: Int, modifier: Modifier = Modifier, ukuran: Int = 20) {
    val gelap = LocalWarna.current.gelap
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { i ->
            Gambar(
                when {
                    i < jumlah -> Art.bintang_penuh
                    gelap -> Art.bintang_kosong
                    else -> Art.bintang_kosong_terang
                },
                t(if (i < jumlah) "Bintang terisi" else "Bintang kosong"),
                Modifier.size((ukuran * 1.3f).dp),
            )
        }
    }
}

@Composable
fun KodeBlok(kode: String, modifier: Modifier = Modifier, judul: String = "PYTHON") {
    val warna = LocalWarna.current
    Column(
        modifier
            .fillMaxWidth()
            .background(warna.kodeLatar, RoundedCornerShape(14.dp))
            .border(1.dp, warna.garis, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(judul, color = MaterialTheme.colorScheme.tertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text(kode, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = warna.kodeTeks, lineHeight = 22.sp)
    }
}

@Composable
fun Memuat(padding: androidx.compose.foundation.layout.PaddingValues, teks: String = "Menyiapkan perairan...") {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(padding)
            .height(320.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(t(teks), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun Kosong(judul: String, pesan: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(judul, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(pesan, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun LabelNilai(label: String, nilai: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(nilai, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BarProgres(nilai: Float, modifier: Modifier = Modifier, warna: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(LocalWarna.current.garis, RoundedCornerShape(50)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(nilai.coerceIn(0f, 1f))
                .height(8.dp)
                .background(warna, RoundedCornerShape(50)),
        )
    }
}

@Composable
fun IkonKecil(teks: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(28.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(50)),
        contentAlignment = Alignment.Center,
    ) {
        Text(teks, fontSize = 14.sp)
    }
}

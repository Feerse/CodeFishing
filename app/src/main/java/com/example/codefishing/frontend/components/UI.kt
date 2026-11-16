package com.example.codefishing.frontend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
    /**
     * Garis warna tipis di sisi kiri kartu. Dipakai daftar misi Mode Expert supaya tiap misi
     * punya penanda warna sendiri dan tidak lagi terbaca sebagai tujuh baris yang sama.
     */
    aksenSisi: Color? = null,
    /**
     * Bentuk sudut kartu. Daftar misi memakai bentuk berbeda untuk status yang berbeda
     * (belum selesai / selesai / sempurna) supaya status terbaca dari siluet kartu, bukan
     * hanya dari warna bintang.
     */
    bentuk: Shape = RoundedCornerShape(20.dp),
    /** Warna garis tepi; null berarti memakai warna garis tema. */
    warnaGaris: Color? = null,
    tebalGaris: Dp = 1.dp,
    isi: @Composable () -> Unit,
) {
    // Column, bukan Box: isi kartu sering terdiri dari beberapa teks/Spacer.
    // Dengan Box semua child menumpuk di posisi yang sama.
    Column(
        modifier
            .clip(bentuk)
            .background(warna)
            .then(
                if (aksenSisi == null) {
                    Modifier
                } else {
                    Modifier.drawBehind {
                        drawRect(aksenSisi, size = Size(5.dp.toPx(), size.height))
                    }
                },
            )
            .border(tebalGaris, warnaGaris ?: LocalWarna.current.garis, bentuk)
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

/**
 * Blok kode bergaya jendela editor: tiga titik jendela, label isi, lalu teks monospace.
 *
 * Bentuk ini sengaja dibuat berbeda dari kartu biasa. Di layar kuis, pemain harus bisa
 * membedakan mana instruksi soal dan mana potongan kode hanya dari bentuknya, tanpa membaca
 * ulang kalimatnya.
 */
@Composable
fun KodeBlok(kode: String, modifier: Modifier = Modifier, judul: String = "PYTHON") {
    val warna = LocalWarna.current
    val bentuk = RoundedCornerShape(14.dp)
    Column(
        modifier
            .fillMaxWidth()
            .background(warna.kodeLatar, bentuk)
            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f), bentuk)
            .clip(bentuk),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            TitikJendela(Color(0xFFFF5F57))
            TitikJendela(Color(0xFFFEBC2E))
            TitikJendela(Color(0xFF28C840))
            Spacer(Modifier.width(6.dp))
            Text(
                judul,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(warna.garis))
        Text(
            kode,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = warna.kodeTeks,
            lineHeight = 22.sp,
        )
    }
}

/** Satu titik jendela pada [KodeBlok]; murni hiasan, tidak dibacakan pembaca layar. */
@Composable
private fun TitikJendela(warna: Color) {
    Box(Modifier.size(8.dp).background(warna.copy(alpha = 0.85f), CircleShape))
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

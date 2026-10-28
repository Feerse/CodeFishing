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
import com.example.codefishing.core.art.Art
import com.example.codefishing.ui.theme.RarityCommon
import com.example.codefishing.ui.theme.RarityEpic
import com.example.codefishing.ui.theme.RarityLegend
import com.example.codefishing.ui.theme.RarityMythic
import com.example.codefishing.ui.theme.RarityRare

fun tierWarna(tier: String): Color = when (tier) {
    "langka" -> RarityRare
    "epik" -> RarityEpic
    "legenda" -> RarityLegend
    "mitos" -> RarityMythic
    else -> RarityCommon
}

fun tierLabel(tier: String): String = when (tier) {
    "langka" -> "Langka"
    "epik" -> "Epik"
    "legenda" -> "Legenda"
    "mitos" -> "Mitos"
    else -> "Biasa"
}

fun gambarIkan(gambar: String?): Int = when (gambar) {
    "ikan_teri" -> Art.ikan_teri
    "ikan_bandeng" -> Art.ikan_bandeng
    "ikan_bawal" -> Art.ikan_bawal
    "ikan_mujair" -> Art.ikan_mujair
    "ikan_nila" -> Art.ikan_nila
    "ikan_lele" -> Art.ikan_lele
    "ikan_patin" -> Art.ikan_patin
    "ikan_gurame" -> Art.ikan_gurame
    "ikan_sepat" -> Art.ikan_sepat
    "ikan_gabus" -> Art.ikan_gabus
    "ikan_kerapu" -> Art.ikan_kerapu
    "ikan_barakuda" -> Art.ikan_barakuda
    "ikan_tenggiri" -> Art.ikan_tenggiri
    "ikan_arwana" -> Art.ikan_arwana
    "ikan_hiu_merah" -> Art.ikan_hiu_merah
    else -> Art.tanda_tanya_ikan
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
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) { isi() }
}

@Composable
fun ChipTier(tier: String, modifier: Modifier = Modifier) {
    val warna = tierWarna(tier)
    Text(
        text = tierLabel(tier),
        color = warna,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(warna.copy(alpha = 0.14f), RoundedCornerShape(50))
            .border(1.dp, warna.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun Bintang(jumlah: Int, modifier: Modifier = Modifier, ukuran: Int = 20) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { i ->
            Text(
                text = if (i < jumlah) "★" else "☆",
                color = if (i < jumlah) RarityLegend else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = ukuran.sp,
            )
        }
    }
}

@Composable
fun KodeBlok(kode: String, modifier: Modifier = Modifier, judul: String = "PYTHON") {
    Column(
        modifier
            .fillMaxWidth()
            .background(Color(0xFF071E29), RoundedCornerShape(14.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(judul, color = MaterialTheme.colorScheme.tertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text(kode, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color(0xFFE8F4F6), lineHeight = 22.sp)
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
            Text(teks, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(50)),
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

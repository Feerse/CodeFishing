package com.example.codefishing.frontend.peta

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.components.Gambar
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/** Suasana laut tujuan; menentukan palet langit, laut, dan pemandangan yang melintas. */
enum class SuasanaLaut { TROPIS, ES }

/** Lama satu pelayaran (ms): setengah pertama menutup layar, setengah kedua membukanya. */
const val DURASI_BERLAYAR_MS = 2600

/**
 * Transisi berlayar antar peta. Kapal melintas di atas laut berparalaks sementara layar menutup,
 * peta diganti lewat [onGanti] saat layar tertutup penuh (tengah pelayaran), lalu layar membuka dan
 * [onSelesai] dipanggil. Ketuk untuk mempercepat tidak disediakan agar urutan tetap konsisten.
 *
 * Bila animasi sistem dimatikan (reduce motion), hanya ada pudar singkat tanpa kapal bergerak.
 *
 * @param tujuan nama peta tujuan, mis. "Lautan Antartika".
 * @param kekanan arah kapal berlayar; true saat menuju peta berikutnya, false saat kembali.
 */
@Composable
fun TransisiBerlayar(
    tujuan: String,
    suasana: SuasanaLaut,
    kekanan: Boolean,
    onGanti: () -> Unit,
    onSelesai: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val kurangiGerak = remember { !ValueAnimator.areAnimatorsEnabled() }
    val progres = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (kurangiGerak) {
            progres.animateTo(0.5f, tween(220, easing = LinearEasing))
            onGanti()
            progres.animateTo(1f, tween(220, easing = LinearEasing))
            onSelesai()
        } else {
            progres.animateTo(0.5f, tween(DURASI_BERLAYAR_MS / 2, easing = LinearEasing))
            onGanti()
            progres.animateTo(1f, tween(DURASI_BERLAYAR_MS / 2, easing = LinearEasing))
            onSelesai()
        }
    }
    val transisi = rememberInfiniteTransition(label = "berlayar")
    val ombak by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "ombak-berlayar",
    )
    val goyang by transisi.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Reverse),
        label = "goyang-kapal",
    )

    val p = progres.value
    // Tirai: membuka-menutup di tepi pelayaran, penuh di tengah.
    val tirai = if (p < 0.5f) (p / 0.16f).coerceIn(0f, 1f) else ((1f - p) / 0.2f).coerceIn(0f, 1f)
    val pal = palet(suasana)

    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer { alpha = tirai }
            // Menyerap ketukan selama pelayaran supaya peta di bawahnya tidak ikut tersentuh.
            .clickable(enabled = true, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {}
            .semantics { contentDescription = "Berlayar menuju $tujuan" },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val garis = size.height * 0.46f
            drawRect(Brush.verticalGradient(listOf(pal.langitAtas, pal.langitBawah), 0f, garis), size = size.copy(height = garis))
            gambarBenda(pal, suasana, p, garis)
            drawRect(
                Brush.verticalGradient(listOf(pal.lautAtas, pal.lautBawah), garis, size.height),
                topLeft = Offset(0f, garis),
                size = size.copy(height = size.height - garis),
            )
            gambarGelombang(pal, p, ombak, garis, kekanan)
            if (suasana == SuasanaLaut.ES) gambarSaljuLaut(ombak)
        }

        BoxKapal(p, goyang, kekanan, kurangiGerak)

        Column(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "BERLAYAR MENUJU",
                color = pal.teks.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 3.sp,
            )
            Text(
                tujuan,
                color = pal.teks,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 40.dp, end = 40.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Canvas(Modifier.size(width = 220.dp, height = 6.dp)) {
                drawRoundRect(pal.teks.copy(alpha = 0.25f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f))
                drawRoundRect(
                    pal.teks.copy(alpha = 0.95f),
                    size = size.copy(width = size.width * p),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f),
                )
            }
            Text(
                if (suasana == SuasanaLaut.ES) "Menembus perairan dingin…" else "Kembali ke perairan hangat…",
                color = pal.teks.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private data class PaletLaut(
    val langitAtas: Color,
    val langitBawah: Color,
    val lautAtas: Color,
    val lautBawah: Color,
    val gelombang: Color,
    val benda: Color,
    val bendaTerang: Color,
    val teks: Color,
)

private fun palet(s: SuasanaLaut): PaletLaut = when (s) {
    SuasanaLaut.TROPIS -> PaletLaut(
        langitAtas = Color(0xFF3FA7D6), langitBawah = Color(0xFFBFE9F5),
        lautAtas = Color(0xFF2E8DB5), lautBawah = Color(0xFF0C4A68),
        gelombang = Color(0xFFBFE9F5), benda = Color(0xFF2F8F55), bendaTerang = Color(0xFFD9A25B),
        teks = Color.White,
    )
    SuasanaLaut.ES -> PaletLaut(
        langitAtas = Color(0xFF14283A), langitBawah = Color(0xFF6E8FA3),
        lautAtas = Color(0xFF1D4F68), lautBawah = Color(0xFF07202E),
        gelombang = Color(0xFFBFE0EC), benda = Color(0xFFB5D4E2), bendaTerang = Color(0xFFF4FAFC),
        teks = Color(0xFFF4FAFC),
    )
}

/** Matahari/bulan dan benda jauh (pulau atau gunung es) yang bergeser pelan di cakrawala. */
private fun DrawScope.gambarBenda(pal: PaletLaut, s: SuasanaLaut, p: Float, garis: Float) {
    val w = size.width
    // Matahari (tropis) atau bulan pucat (es)
    val pusat = Offset(w * 0.78f, garis * 0.38f)
    if (s == SuasanaLaut.TROPIS) {
        drawCircle(Color(0xFFFFE9A8).copy(alpha = 0.35f), garis * 0.30f, pusat)
        drawCircle(Color(0xFFFFF3CC), garis * 0.15f, pusat)
    } else {
        drawCircle(Color(0xFFDCEEF5).copy(alpha = 0.22f), garis * 0.24f, pusat)
        drawCircle(Color(0xFFDCEEF5), garis * 0.11f, pusat)
    }
    // Benda jauh bergulir berlawanan dengan arah kapal (paralaks)
    for (i in 0 until 4) {
        val x = (((i * 0.33f + 0.08f) - p * 0.35f) % 1.3f + 1.3f) % 1.3f * w - 0.15f * w
        val lebar = w * (0.20f + 0.05f * (i % 2))
        val tinggi = garis * (if (s == SuasanaLaut.ES) 0.34f else 0.13f) * (0.8f + 0.25f * (i % 3))
        if (s == SuasanaLaut.ES) {
            val path = Path().apply {
                moveTo(x - lebar / 2f, garis)
                lineTo(x - lebar * 0.15f, garis - tinggi)
                lineTo(x + lebar * 0.05f, garis - tinggi * 0.55f)
                lineTo(x + lebar * 0.2f, garis - tinggi * 0.85f)
                lineTo(x + lebar / 2f, garis)
                close()
            }
            drawPath(path, pal.benda)
            drawPath(
                Path().apply {
                    moveTo(x - lebar * 0.15f, garis - tinggi)
                    lineTo(x - lebar * 0.24f, garis - tinggi * 0.72f)
                    lineTo(x - lebar * 0.05f, garis - tinggi * 0.78f)
                    close()
                },
                pal.bendaTerang,
            )
        } else {
            drawOval(pal.bendaTerang, Offset(x - lebar / 2f, garis - tinggi * 0.45f), androidx.compose.ui.geometry.Size(lebar, tinggi * 0.55f))
            drawOval(pal.benda, Offset(x - lebar * 0.34f, garis - tinggi), androidx.compose.ui.geometry.Size(lebar * 0.68f, tinggi * 0.85f))
        }
    }
}

private fun DrawScope.gambarGelombang(pal: PaletLaut, p: Float, ombak: Float, garis: Float, kekanan: Boolean) {
    val arah = if (kekanan) -1f else 1f
    val tinggiLaut = size.height - garis
    for (lapis in 0 until 4) {
        val y0 = garis + tinggiLaut * (0.10f + lapis * 0.22f)
        val amp = (4f + lapis * 3f).dp.toPx()
        val kec = 1.2f + lapis * 0.7f
        val path = Path()
        var mulai = true
        var x = 0f
        while (x <= size.width + 8f) {
            val y = y0 + amp * sin((x / size.width * (3.2f + lapis) + arah * (ombak * kec + p * 3f * kec)) * 2f * PI.toFloat())
            if (mulai) { path.moveTo(x, y); mulai = false } else path.lineTo(x, y)
            x += 8f
        }
        drawPath(path, pal.gelombang.copy(alpha = 0.20f + lapis * 0.05f), style = Stroke(width = (2f + lapis).dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun DrawScope.gambarSaljuLaut(ombak: Float) {
    repeat(26) { i ->
        val x = ((i * 37 + 11) % 100) / 100f * size.width
        val y = ((i * 29 / 100f + ombak * (0.6f + (i % 4) * 0.2f)) % 1f) * size.height
        drawCircle(
            Color.White.copy(alpha = 0.30f + (i % 3) * 0.12f),
            (1.3f + (i % 3) * 0.7f).dp.toPx(),
            Offset(x + sin((ombak * 2f * PI.toFloat() + i)) * 6.dp.toPx(), y),
        )
    }
}

@Composable
private fun BoxKapal(
    p: Float,
    goyang: Float,
    kekanan: Boolean,
    kurangiGerak: Boolean,
) {
    val density = LocalDensity.current
    val ukuran = 160.dp
    // Kapal melintas dari luar layar ke luar layar; berada di tengah saat p = 0.5.
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
        val lebarPx = with(density) { maxWidth.toPx() }
        val tinggiPx = with(density) { maxHeight.toPx() }
        val ukPx = with(density) { ukuran.toPx() }
        val t = if (kurangiGerak) 0.5f else p
        val x = if (kekanan) -ukPx + (lebarPx + ukPx) * t else lebarPx - (lebarPx + ukPx) * t
        val y = tinggiPx * 0.46f + tinggiPx * 0.045f + (if (kurangiGerak) 0f else goyang * 5.dp.value * density.density)
        Gambar(
            Art.kapal_peta,
            null,
            Modifier
                .offset { IntOffset(x.roundToInt(), (y - ukPx * 0.62f).roundToInt()) }
                .size(ukuran)
                .graphicsLayer {
                    scaleX = if (kekanan) 1f else -1f
                    rotationZ = if (kurangiGerak) 0f else goyang * 3.2f
                },
        )
        // Jejak buih di belakang kapal
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val belakang = if (kekanan) -1f else 1f
            val dasarY = y + ukPx * 0.30f
            for (i in 0 until 7) {
                val jarak = (0.22f * ukPx) * (i + 1)
                val pudar = 1f - i / 7f
                drawOval(
                    Color.White.copy(alpha = 0.42f * pudar),
                    Offset(x + ukPx * 0.5f + belakang * jarak - ukPx * (0.14f - i * 0.012f), dasarY - 3.dp.toPx()),
                    androidx.compose.ui.geometry.Size(ukPx * (0.28f - i * 0.022f), 7.dp.toPx() * (1f + 0.1f * i)),
                )
            }
        }
    }
}

package com.example.codefishing.frontend.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.art.Art
import com.example.codefishing.ui.theme.LocalWarna
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/** Satu gelembung pada animasi pembuka. Semua nilai berupa fraksi ukuran layar. */
private data class GelembungPembuka(
    val x: Float,
    val mulai: Float,
    val ukuran: Float,
    val fase: Float,
)

private val GELEMBUNG = listOf(
    GelembungPembuka(0.38f, 0.00f, 0.095f, 0.0f),
    GelembungPembuka(0.47f, 0.08f, 0.065f, 1.3f),
    GelembungPembuka(0.56f, 0.16f, 0.115f, 2.1f),
    GelembungPembuka(0.62f, 0.24f, 0.070f, 0.7f),
    GelembungPembuka(0.34f, 0.30f, 0.075f, 2.8f),
    GelembungPembuka(0.52f, 0.38f, 0.055f, 1.8f),
    GelembungPembuka(0.44f, 0.46f, 0.090f, 3.4f),
    GelembungPembuka(0.58f, 0.54f, 0.060f, 0.4f),
)

private val TUNDA_RIAK = listOf(0.00f, 0.16f, 0.32f)

/**
 * Animasi pembuka di layar boot: logo muncul, riak mengembang, dan gelembung
 * naik. Berjalan penuh selama ±1,9 detik dan bisa dilewati dengan satu ketukan.
 * Hanya memakai aset yang sudah ada: logo, gelembung, dan pola ombak.
 *
 * [onSelesai] dipanggil sekali saat animasi selesai atau dilewati, sehingga
 * layar utama baru dibuka setelah pembuka benar-benar terlihat.
 */
@Composable
fun Pembuka(modifier: Modifier = Modifier, onSelesai: () -> Unit = {}) {
    val warna = LocalWarna.current
    val progres = remember { Animatable(0f) }
    var dilewati by remember { mutableStateOf(false) }

    LaunchedEffect(dilewati) {
        if (!dilewati) {
            progres.animateTo(1f, tween(durationMillis = 1900, easing = LinearEasing))
            onSelesai()
        }
    }

    val gambarGelembung = painterResource(Art.gelembung)

    Box(
        modifier
            .fillMaxSize()
            .clickable(
                onClickLabel = "Lewati animasi pembuka",
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                dilewati = true
                onSelesai()
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Art.ombak_pola),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.10f,
            colorFilter = ColorFilter.tint(warna.ombak),
        )

        Canvas(Modifier.fillMaxSize()) {
            // Nilai animasi dibaca di dalam draw scope supaya setiap frame
            // hanya menggambar ulang kanvas, tanpa recompose seluruh layar.
            val p = if (dilewati) 1f else progres.value
            val masuk = (p / 0.32f).coerceIn(0f, 1f)
            val pusat = Offset(size.width / 2f, size.height / 2f)
            val sisi = min(size.width, size.height)

            // Cahaya lembut di belakang logo.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(warna.ombak.copy(alpha = 0.22f * masuk), Color.Transparent),
                    center = pusat,
                    radius = sisi * 0.42f,
                ),
                radius = sisi * 0.42f,
                center = pusat,
            )

            // Tiga riak yang mengembang bergantian.
            TUNDA_RIAK.forEach { tunda ->
                val r = ((p - tunda) / 0.72f).coerceIn(0f, 1f)
                if (r > 0f && r < 1f) {
                    drawCircle(
                        color = warna.ombak.copy(alpha = (1f - r) * 0.38f),
                        radius = sisi * (0.20f + 0.30f * r),
                        center = pusat,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }

            // Gelembung naik sambil bergoyang halus.
            GELEMBUNG.forEach { g ->
                val naik = ((p - g.mulai) / 0.62f).coerceIn(0f, 1f)
                if (naik > 0f && naik < 1f) {
                    val alfa = sin(naik * PI).toFloat() * 0.55f
                    val ukuran = sisi * g.ukuran * (0.75f + 0.35f * naik)
                    val x = size.width * g.x + sin(naik * 5f + g.fase) * sisi * 0.012f
                    val y = size.height * (0.78f - 0.60f * naik)
                    translate(left = x - ukuran / 2f, top = y - ukuran / 2f) {
                        with(gambarGelembung) {
                            draw(
                                size = Size(ukuran, ukuran),
                                alpha = alfa,
                                colorFilter = ColorFilter.tint(warna.ombak),
                            )
                        }
                    }
                }
            }
        }

        Image(
            painter = painterResource(Art.logo_codefishing),
            contentDescription = "CodeFishing",
            modifier = Modifier
                .fillMaxSize(0.42f)
                .graphicsLayer {
                    // Sama seperti kanvas, alpha dan skala diperbarui di layer
                    // agar animasi tidak memicu recompose.
                    val p = if (dilewati) 1f else progres.value
                    val masuk = (p / 0.32f).coerceIn(0f, 1f)
                    alpha = masuk
                    val skala = 0.88f + 0.12f * FastOutSlowInEasing.transform(masuk)
                    scaleX = skala
                    scaleY = skala
                },
            contentScale = ContentScale.Fit,
        )
    }
}

package com.example.codefishing.frontend.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.art.Art
import com.example.codefishing.ui.theme.LocalWarna
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.roundToInt
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
 * Layar pembuka sekaligus layar muat: logo muncul, riak mengembang, dan gelembung naik sambil
 * [PemuatAset] mendekode seluruh katalog gambar di thread latar.
 *
 * Animasi masuk bisa dipercepat dengan satu ketukan, tetapi [onSelesai] baru dipanggil setelah
 * animasi selesai DAN [progresMuat] mencapai 1. Dengan begitu pemain tidak pernah masuk ke layar
 * utama saat aset masih didekode (penyebab gambar sempat kosong atau muncul terlambat).
 */
@Composable
fun Pembuka(
    progresMuat: Float,
    status: String,
    modifier: Modifier = Modifier,
    onSelesai: () -> Unit = {},
) {
    val warna = LocalWarna.current
    val progres = remember { Animatable(0f) }
    var dilewati by remember { mutableStateOf(false) }
    var animasiSelesai by remember { mutableStateOf(false) }
    var selesaiDilaporkan by remember { mutableStateOf(false) }

    LaunchedEffect(dilewati) {
        if (dilewati) {
            progres.snapTo(1f)
        } else {
            progres.animateTo(1f, tween(durationMillis = 1500, easing = LinearEasing))
        }
        animasiSelesai = true
    }

    // Layar utama baru dibuka bila animasi masuk sudah selesai dan pemuatan aset tuntas.
    LaunchedEffect(animasiSelesai, progresMuat) {
        if (!selesaiDilaporkan && animasiSelesai && progresMuat >= 1f) {
            delay(240) // beri jeda singkat agar bilah 100% sempat terlihat
            selesaiDilaporkan = true
            onSelesai()
        }
    }

    val muatHalus by animateFloatAsState(
        targetValue = progresMuat.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 260, easing = LinearOutSlowInEasing),
        label = "progres_muat",
    )
    val persen = (muatHalus * 100f).roundToInt()

    val gambarGelembung = rememberPainterGambar(Art.gelembung)

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .clickable(
                onClickLabel = "Lewati animasi pembuka",
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                // Hanya mempercepat animasi masuk; pemuatan aset tetap ditunggu.
                dilewati = true
            },
        contentAlignment = Alignment.Center,
    ) {
        // Logo dihitung dari sisi terpendek layar supaya selalu persegi, center,
        // dan tidak terpotong di HP kecil, tablet, maupun perangkat lipat.
        // Batas 280 dp mencegah logo membengkak di layar sangat besar.
        val ukuranLogo = (min(maxWidth.value, maxHeight.value) * 0.42f)
            .coerceAtMost(280f)
            .dp
        Image(
            painter = rememberPainterGambar(Art.ombak_pola),
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
            painter = rememberPainterGambar(Art.logo_codefishing),
            contentDescription = "CodeFishing",
            modifier = Modifier
                .size(ukuranLogo)
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

        // Indikator pemuatan aset. Persentase memakai nilai yang sudah dihaluskan agar bilah tidak
        // melompat saat ratusan aset selesai didekode beruntun.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 40.dp, end = 40.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            BarProgres(
                nilai = muatHalus,
                modifier = Modifier.width(220.dp),
                warna = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "$persen%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

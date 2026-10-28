package com.example.codefishing.frontend.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Fase animasi nelayan pada layar Kuis Mode Belajar.
 * - [Diam]  : soal baru, pelampung mengapung tenang.
 * - [Lempar]: pemain memilih opsi, kail dilempar (belum dikunci).
 * - [Dapat] : jawaban benar dikunci, ikan terangkat.
 * - [Lepas] : jawaban salah dikunci, tali putus dan ikan kabur.
 */
enum class FasePancing { Diam, Lempar, Dapat, Lepas }

private val BINTANG = listOf(
    0.06f to 0.14f, 0.14f to 0.31f, 0.24f to 0.10f, 0.33f to 0.25f,
    0.41f to 0.07f, 0.52f to 0.20f, 0.58f to 0.34f, 0.67f to 0.12f,
    0.95f to 0.09f, 0.99f to 0.27f,
)

/**
 * Adegan memancing vector di atas soal kuis. Digambar penuh dengan [Canvas]
 * supaya tajam di semua kerapatan layar dan tidak menambah berkas aset.
 */
@Composable
fun MemancingScene(
    fase: FasePancing,
    modifier: Modifier = Modifier,
    varianBoss: Boolean = false,
) {
    val context = LocalContext.current
    val kurangiGerak = remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f) == 0f
    }

    val transisi = rememberInfiniteTransition(label = "memancing")
    val ombak by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing)),
        label = "ombak",
    )
    val bob by transisi.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1900, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val gelembung by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "gelembung",
    )
    val renang by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing)),
        label = "renang",
    )
    val kilat by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
        label = "kilat",
    )

    // Progres aksi 0..1 untuk satu reaksi (lempar / dapat / lepas).
    val aksi = remember { Animatable(0f) }
    LaunchedEffect(fase, kurangiGerak) {
        aksi.snapTo(0f)
        if (fase != FasePancing.Diam) {
            if (kurangiGerak) {
                aksi.snapTo(1f)
            } else {
                aksi.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = if (fase == FasePancing.Dapat) 1100 else 900,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
        }
    }

    val gerak = if (kurangiGerak) 0f else 1f
    val teks = rememberTextMeasurer()
    // Diukur sekali per fase, bukan setiap frame animasi.
    val labelHasil = remember(fase) {
        when (fase) {
            FasePancing.Diam, FasePancing.Lempar -> null
            FasePancing.Dapat -> teks.measure(
                text = "+25 XP",
                style = TextStyle(color = Color(0xFF8BF0C7), fontSize = 14.sp, fontWeight = FontWeight.Bold),
            )
            FasePancing.Lepas -> teks.measure(
                text = "Umpan −1",
                style = TextStyle(color = Color(0xFFFFB3B3), fontSize = 14.sp, fontWeight = FontWeight.Bold),
            )
        }
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .semantics { contentDescription = deskripsiFase(fase) },
    ) {
        gambarAdegan(
            fase = fase,
            boss = varianBoss,
            aksi = aksi.value,
            ombak = ombak * gerak,
            bob = bob * gerak,
            gelembung = gelembung * gerak,
            renang = renang * gerak,
            kilat = kilat * gerak,
            labelHasil = labelHasil,
        )
    }
}

private fun deskripsiFase(fase: FasePancing): String = when (fase) {
    FasePancing.Diam -> "Nelayan menunggu ikan menggigit umpan"
    FasePancing.Lempar -> "Kail dilempar ke laut"
    FasePancing.Dapat -> "Ikan berhasil diangkat ke perahu"
    FasePancing.Lepas -> "Tali pancing putus dan ikan kabur"
}

private fun DrawScope.gambarAdegan(
    fase: FasePancing,
    boss: Boolean,
    aksi: Float,
    ombak: Float,
    bob: Float,
    gelembung: Float,
    renang: Float,
    kilat: Float,
    labelHasil: TextLayoutResult?,
) {
    val w = size.width
    val h = size.height
    val hz = h * 0.54f
    val satuan = 1.dp.toPx()

    val langitAtas = if (boss) Color(0xFF2A1B2A) else Color(0xFF0A2735)
    val langitBawah = if (boss) Color(0xFF40232F) else Color(0xFF1C566B)
    val airAtas = if (boss) Color(0xFF19323E) else Color(0xFF0F455C)
    val airBawah = Color(0xFF061A24)
    val busa = Color(0xFF9DD7E4)
    val hijau = Color(0xFF5FD3A0)
    val merah = Color(0xFFFF6B81)
    val faseOmbak = ombak * 2f * PI.toFloat()

    // ---------- Langit ----------
    drawRect(
        brush = Brush.verticalGradient(listOf(langitAtas, langitBawah), startY = 0f, endY = hz),
        size = Size(w, hz),
    )

    if (boss) {
        val awan = Color(0xFF1B1220)
        drawOval(awan, topLeft = Offset(-w * 0.10f, -hz * 0.60f), size = Size(w * 0.58f, hz * 0.80f))
        drawOval(awan, topLeft = Offset(w * 0.34f, -hz * 0.70f), size = Size(w * 0.62f, hz * 0.88f))
        drawOval(awan, topLeft = Offset(w * 0.62f, -hz * 0.45f), size = Size(w * 0.52f, hz * 0.64f))
    } else {
        BINTANG.forEach { (fx, fy) ->
            drawCircle(
                Color(0xFFEAF7FF).copy(alpha = 0.45f),
                radius = 0.9f * satuan,
                center = Offset(w * fx, h * fy),
            )
        }
        val mx = w * 0.82f
        val my = h * 0.16f
        val mr = h * 0.072f
        drawCircle(Color(0xFFFFE7B0).copy(alpha = 0.10f), mr * 2.6f, Offset(mx, my))
        drawCircle(Color(0xFFFFE7B0).copy(alpha = 0.18f), mr * 1.7f, Offset(mx, my))
        drawCircle(Color(0xFFFFF3D6), mr, Offset(mx, my))
    }

    // Pulau kecil di ufuk kiri.
    val pulau = Path().apply {
        moveTo(0f, hz + 2f)
        lineTo(w * 0.02f, hz - h * 0.055f)
        lineTo(w * 0.045f, hz - h * 0.018f)
        lineTo(w * 0.07f, hz - h * 0.080f)
        lineTo(w * 0.10f, hz - h * 0.026f)
        lineTo(w * 0.13f, hz - h * 0.048f)
        lineTo(w * 0.15f, hz + 2f)
        close()
    }
    drawPath(pulau, Color(0xFF08202B))

    // ---------- Air ----------
    val amp = h * 0.014f
    val langkah = w / 28f
    val permukaan = Path()
    val garisAir = Path()
    for (i in 0..28) {
        val px = i * langkah
        val py = hz + sin(i / 28f * 6f * PI.toFloat() + faseOmbak) * amp
        if (i == 0) {
            permukaan.moveTo(px, py)
            garisAir.moveTo(px, py)
        } else {
            permukaan.lineTo(px, py)
            garisAir.lineTo(px, py)
        }
    }
    permukaan.lineTo(w, h)
    permukaan.lineTo(0f, h)
    permukaan.close()
    drawPath(permukaan, Brush.verticalGradient(listOf(airAtas, airBawah), startY = hz, endY = h))
    drawPath(garisAir, busa.copy(alpha = 0.30f), style = Stroke(1.5f * satuan, cap = StrokeCap.Round))

    // Pantulan bulan di air.
    if (!boss) {
        for (i in 0 until 6) {
            val yy = hz + h * 0.03f + i * h * 0.055f
            val lebar = w * (0.055f - i * 0.006f) * (1f + sin(faseOmbak + i) * 0.15f)
            drawLine(
                Color(0xFFFFE7B0).copy(alpha = 0.16f - i * 0.02f),
                Offset(w * 0.82f - lebar, yy),
                Offset(w * 0.82f + lebar, yy),
                strokeWidth = 1.6f * satuan,
                cap = StrokeCap.Round,
            )
        }
    }

    // ---------- Bawah air ----------
    for (i in 0..2) {
        val akarX = w * (0.72f + i * 0.05f)
        val tinggi = h * (0.20f + i * 0.03f)
        val goyang = sin(faseOmbak * 1.4f + i) * w * 0.012f
        val rumput = Path().apply {
            moveTo(akarX, h)
            quadraticTo(akarX + goyang, h - tinggi * 0.6f, akarX + goyang * 0.4f, h - tinggi)
        }
        drawPath(
            rumput,
            Color(0xFF1B6B5A).copy(alpha = 0.75f),
            style = Stroke(w * 0.012f, cap = StrokeCap.Round),
        )
    }

    for (i in 0..3) {
        val t = frac(gelembung + i * 0.27f)
        val bx = w * (0.66f + i * 0.035f) + sin(t * 6f + i) * w * 0.008f
        val by = lerp(h * 0.97f, hz + h * 0.04f, t)
        drawCircle(
            Color(0xFFBFE9F3).copy(alpha = (1f - t) * 0.5f),
            radius = (1.2f + i * 0.4f) * satuan,
            center = Offset(bx, by),
            style = Stroke(1f * satuan),
        )
    }

    siluetIkan(
        x = lerp(-w * 0.08f, w * 1.08f, frac(renang)),
        y = hz + h * 0.20f + sin(faseOmbak) * h * 0.012f,
        panjang = w * 0.10f,
        arahKanan = true,
        warna = Color(0xFF2E7F96),
        alpha = 0.85f,
    )
    siluetIkan(
        x = lerp(w * 1.08f, -w * 0.08f, frac(renang + 0.45f)),
        y = hz + h * 0.34f + cos(faseOmbak) * h * 0.010f,
        panjang = w * 0.075f,
        arahKanan = false,
        warna = Color(0xFF256A80),
        alpha = 0.70f,
    )

    if (boss) {
        val geser = sin(renang * 2f * PI.toFloat()) * w * 0.10f
        siluetIkan(
            x = w * 0.50f + geser,
            y = h * 0.88f,
            panjang = w * 0.30f,
            arahKanan = true,
            warna = Color(0xFF4A2038),
            alpha = 0.55f,
        )
    }

    // ---------- Perahu dan nelayan ----------
    val ayun = sin(faseOmbak) * amp * 0.9f
    val kapalX = w * 0.19f
    val kapalY = hz + ayun
    val setengah = w * 0.135f
    val tinggiHull = h * 0.075f

    val hull = Path().apply {
        moveTo(kapalX - setengah, kapalY)
        lineTo(kapalX + setengah, kapalY)
        lineTo(kapalX + setengah * 0.72f, kapalY + tinggiHull)
        lineTo(kapalX - setengah * 0.80f, kapalY + tinggiHull)
        close()
    }
    drawPath(hull, Color(0xFF2B1A12))
    drawPath(hull, Color(0xFF6B4430), style = Stroke(1.5f * satuan))
    drawLine(
        Color(0xFF8A5738),
        Offset(kapalX - setengah * 0.92f, kapalY),
        Offset(kapalX + setengah * 0.92f, kapalY),
        strokeWidth = 2f * satuan,
        cap = StrokeCap.Round,
    )

    val badanLebar = w * 0.036f
    val badanTinggi = h * 0.125f
    val fx = kapalX - setengah * 0.05f
    val puncakBadan = kapalY - badanTinggi
    val kepalaR = w * 0.025f
    drawRoundRect(
        color = Color(0xFF10222B),
        topLeft = Offset(fx - badanLebar / 2f, puncakBadan),
        size = Size(badanLebar, badanTinggi),
        cornerRadius = CornerRadius(badanLebar * 0.45f),
    )
    drawCircle(Color(0xFF10222B), kepalaR, Offset(fx, puncakBadan - kepalaR * 0.9f))
    drawRoundRect(
        color = Color(0xFF0B1A21),
        topLeft = Offset(fx - kepalaR * 1.35f, puncakBadan - kepalaR * 2.05f),
        size = Size(kepalaR * 2.7f, kepalaR * 0.60f),
        cornerRadius = CornerRadius(kepalaR * 0.3f),
    )

    val tangan = Offset(fx + badanLebar * 0.55f, puncakBadan + badanTinggi * 0.28f)
    val sudut = when (fase) {
        FasePancing.Diam -> bob * 0.03f
        FasePancing.Lempar ->
            if (aksi < 0.35f) lerp(-0.65f, 0.18f, aksi / 0.35f)
            else lerp(0.18f, 0f, (aksi - 0.35f) / 0.65f)
        FasePancing.Dapat -> -0.55f * sin(aksi * PI.toFloat())
        FasePancing.Lepas -> 0.30f * sin(aksi * PI.toFloat())
    }
    val ujungJoran = tangan + putar(Offset(w * 0.14f, -h * 0.24f), sudut)
    drawLine(Color(0xFFD9A66B), tangan, ujungJoran, strokeWidth = 2.2f * satuan, cap = StrokeCap.Round)
    drawCircle(Color(0xFF8A5738), radius = 2.4f * satuan, center = tangan, style = Stroke(1.2f * satuan))

    // ---------- Tali dan pelampung ----------
    val target = Offset(w * 0.62f, hz + h * 0.03f)
    val pancing = when (fase) {
        FasePancing.Diam -> Offset(target.x, target.y + bob * 1.5f * satuan)
        FasePancing.Lempar -> {
            // 0..18%: kail ditarik dari air ke ujung joran, lalu dilempar melengkung.
            val tarik = (aksi / 0.18f).coerceIn(0f, 1f)
            val lempar = ((aksi - 0.18f) / 0.52f).coerceIn(0f, 1f)
            val e = lempar * lempar * (3f - 2f * lempar)
            val x0 = lerp(target.x, ujungJoran.x, tarik)
            val y0 = lerp(target.y, ujungJoran.y, tarik)
            Offset(
                lerp(x0, target.x, e),
                lerp(y0, target.y, e) - sin(e * PI.toFloat()) * h * 0.26f,
            )
        }
        FasePancing.Dapat -> Offset(
            target.x,
            target.y + sin((aksi / 0.35f).coerceAtMost(1f) * (PI.toFloat() / 2f)) * h * 0.05f,
        )
        FasePancing.Lepas -> Offset(
            target.x + sin(aksi * PI.toFloat() * 2f) * w * 0.012f,
            target.y - sin(aksi * PI.toFloat()) * h * 0.05f,
        )
    }

    val kendur = when (fase) {
        FasePancing.Diam -> h * 0.06f
        FasePancing.Lempar -> h * 0.02f
        FasePancing.Dapat -> h * 0.012f
        FasePancing.Lepas -> 0f
    }
    val kontrol = Offset((ujungJoran.x + pancing.x) / 2f, (ujungJoran.y + pancing.y) / 2f + kendur)
    val tali = Path().apply {
        moveTo(ujungJoran.x, ujungJoran.y)
        quadraticTo(kontrol.x, kontrol.y, pancing.x, pancing.y)
    }

    val terputus = fase == FasePancing.Lepas && aksi > 0.38f
    if (terputus) {
        val ukur = PathMeasure()
        ukur.setPath(tali, false)
        val potong = Path()
        ukur.getSegment(0f, ukur.length * 0.58f, potong, true)
        drawPath(
            potong,
            Color(0xFFCFE3E9).copy(alpha = 0.85f),
            style = Stroke(1.4f * satuan, cap = StrokeCap.Round),
        )
        val ujungPutus = titikKuadrat(ujungJoran, kontrol, pancing, 0.58f)
        val ayunTali = sin(aksi * PI.toFloat() * 3f) * w * 0.02f
        val terurai = Path().apply {
            moveTo(ujungPutus.x, ujungPutus.y)
            quadraticTo(
                ujungPutus.x + ayunTali,
                ujungPutus.y + h * 0.06f,
                ujungPutus.x + ayunTali * 1.4f,
                ujungPutus.y + h * 0.12f,
            )
        }
        drawPath(
            terurai,
            Color(0xFFCFE3E9).copy(alpha = 0.55f),
            style = Stroke(
                1.2f * satuan,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f * satuan, 3f * satuan)),
            ),
        )
    } else {
        drawPath(
            tali,
            Color(0xFFCFE3E9).copy(alpha = 0.75f),
            style = Stroke(1.4f * satuan, cap = StrokeCap.Round),
        )
    }

    val jariJari = w * 0.015f

    // Riak air mengikuti fase.
    when (fase) {
        FasePancing.Diam -> riak(pancing, jariJari * (2.2f + bob * 0.3f), busa, 0.22f, satuan)
        FasePancing.Lempar -> {
            if (aksi > 0.70f) {
                val t = ((aksi - 0.70f) / 0.30f).coerceIn(0f, 1f)
                riak(pancing, jariJari * (2f + t * 6f), busa, 0.5f * (1f - t), satuan)
            }
        }
        FasePancing.Dapat -> {
            val t = (aksi / 0.6f).coerceIn(0f, 1f)
            riak(pancing, jariJari * (2f + t * 9f), hijau, 0.55f * (1f - t), satuan)
        }
        FasePancing.Lepas -> {
            val t = (aksi / 0.6f).coerceIn(0f, 1f)
            riak(pancing, jariJari * (2f + t * 8f), merah, 0.50f * (1f - t), satuan)
        }
    }

    if (fase == FasePancing.Dapat) {
        // Cipratan air.
        val t = ((aksi - 0.18f) / 0.52f).coerceIn(0f, 1f)
        if (t > 0f && t < 1f) {
            for (i in 0 until 7) {
                val arah = -PI.toFloat() * 0.85f + i * (PI.toFloat() * 0.28f)
                val jarak = jariJari * (3f + t * 10f) * (0.7f + (i % 3) * 0.2f)
                drawCircle(
                    busa.copy(alpha = (1f - t) * 0.85f),
                    radius = (1.6f - t).coerceAtLeast(0.4f) * satuan,
                    center = Offset(
                        pancing.x + cos(arah) * jarak,
                        pancing.y + sin(arah) * jarak + t * t * h * 0.10f,
                    ),
                )
            }
        }
        // Ikan terangkat dari air.
        val tj = ((aksi - 0.16f) / 0.56f).coerceIn(0f, 1f)
        if (tj > 0f && tj < 1f) {
            val jx = lerp(pancing.x, pancing.x - w * 0.14f, tj)
            val jy = lerp(pancing.y + h * 0.04f, pancing.y - h * 0.20f, tj) -
                sin(tj * PI.toFloat()) * h * 0.05f
            rotate(-28f + tj * 70f, pivot = Offset(jx, jy)) {
                siluetIkan(jx, jy, w * 0.11f, true, Color(0xFF8BF0C7), 1f)
            }
        }
    }

    if (fase == FasePancing.Lepas) {
        val tl = ((aksi - 0.42f) / 0.58f).coerceIn(0f, 1f)
        if (tl > 0f) {
            siluetIkan(
                x = lerp(pancing.x, w * 1.08f, tl),
                y = pancing.y + h * 0.07f - tl * h * 0.03f,
                panjang = w * 0.10f,
                arahKanan = true,
                warna = merah,
                alpha = (1f - tl * 0.6f).coerceAtLeast(0f),
            )
        }
    }

    gambarPelampung(pancing, jariJari, satuan)

    // Teks hasil melayang di atas pelampung.
    val tTeks = ((aksi - 0.55f) / 0.45f).coerceIn(0f, 1f)
    if (labelHasil != null && tTeks > 0f) {
        drawText(
            textLayoutResult = labelHasil,
            topLeft = Offset(
                pancing.x - labelHasil.size.width / 2f,
                pancing.y - h * 0.30f - tTeks * h * 0.10f,
            ),
            alpha = (1f - tTeks).coerceIn(0f, 1f),
        )
    }

    // Kilat badai untuk stage boss.
    if (boss) {
        val denyut = maxOf(
            (1f - kilat / 0.06f).coerceIn(0f, 1f),
            (1f - abs(kilat - 0.14f) / 0.05f).coerceIn(0f, 1f),
        )
        if (denyut > 0.02f) {
            drawRect(Color(0xFFFFE7B0).copy(alpha = denyut * 0.10f), size = Size(w, hz))
            val petir = Path().apply {
                moveTo(w * 0.30f, 0f)
                lineTo(w * 0.26f, hz * 0.45f)
                lineTo(w * 0.31f, hz * 0.45f)
                lineTo(w * 0.25f, hz * 0.92f)
                lineTo(w * 0.33f, hz * 0.38f)
                lineTo(w * 0.28f, hz * 0.38f)
                lineTo(w * 0.34f, 0f)
                close()
            }
            drawPath(petir, Color(0xFFFFF3C4).copy(alpha = denyut))
        }
    }
}

private fun DrawScope.riak(center: Offset, radius: Float, warna: Color, alpha: Float, satuan: Float) {
    if (radius <= 0f || alpha <= 0f) return
    drawCircle(
        color = warna.copy(alpha = alpha),
        radius = radius,
        center = center,
        style = Stroke(1.2f * satuan),
    )
}

private fun DrawScope.gambarPelampung(pos: Offset, r: Float, satuan: Float) {
    drawLine(
        color = Color(0xFFF4F8F9),
        start = Offset(pos.x, pos.y - r * 1.3f),
        end = Offset(pos.x, pos.y - r * 2.4f),
        strokeWidth = 1.4f * satuan,
        cap = StrokeCap.Round,
    )
    drawCircle(Color(0xFFF4F8F9), radius = r, center = Offset(pos.x, pos.y - r * 0.35f))
    drawArc(
        color = Color(0xFFF26A2E),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(pos.x - r, pos.y - r * 0.35f - r),
        size = Size(r * 2f, r * 2f),
    )
}

private fun DrawScope.siluetIkan(
    x: Float,
    y: Float,
    panjang: Float,
    arahKanan: Boolean,
    warna: Color,
    alpha: Float,
) {
    if (alpha <= 0f) return
    val arah = if (arahKanan) 1f else -1f
    val hh = panjang * 0.22f
    val ekor = panjang * 0.25f
    val badan = Path().apply {
        moveTo(x + arah * panjang * 0.5f, y)
        quadraticTo(x + arah * panjang * 0.10f, y - hh, x - arah * ekor, y)
        quadraticTo(x + arah * panjang * 0.10f, y + hh, x + arah * panjang * 0.5f, y)
        close()
    }
    val sirip = Path().apply {
        moveTo(x - arah * ekor, y)
        lineTo(x - arah * panjang * 0.5f, y - hh * 0.9f)
        lineTo(x - arah * panjang * 0.5f, y + hh * 0.9f)
        close()
    }
    drawPath(sirip, warna.copy(alpha = alpha * 0.85f))
    drawPath(badan, warna.copy(alpha = alpha))
    drawCircle(
        Color(0xFF04141C).copy(alpha = alpha),
        radius = (panjang * 0.035f).coerceAtLeast(0.8f),
        center = Offset(x + arah * panjang * 0.30f, y - hh * 0.28f),
    )
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private fun frac(v: Float): Float = v - floor(v)

private fun putar(v: Offset, sudut: Float): Offset {
    val c = cos(sudut)
    val s = sin(sudut)
    return Offset(v.x * c - v.y * s, v.x * s + v.y * c)
}

private fun titikKuadrat(a: Offset, b: Offset, c: Offset, t: Float): Offset {
    val u = 1f - t
    return Offset(
        u * u * a.x + 2f * u * t * b.x + t * t * c.x,
        u * u * a.y + 2f * u * t * b.y + t * t * c.y,
    )
}

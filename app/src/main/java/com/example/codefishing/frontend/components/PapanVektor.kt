package com.example.codefishing.frontend.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Efek vektor khusus papan Mode Expert (grid dari atas). */

internal fun DrawScope.gambarKilauPapan(warna: Color, fase: Float) {
    repeat(4) { i ->
        val y = size.height * (0.18f + i * 0.22f) + sin((fase * 2f * PI.toFloat() + i).toDouble()).toFloat() * 4.dp.toPx()
        val mulai = size.width * (0.04f + 0.12f * i)
        val akhir = size.width * (0.72f + 0.06f * (i % 2))
        drawLine(
            color = warna.copy(alpha = 0.10f),
            start = Offset(mulai, y),
            end = Offset(akhir, y),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = warna.copy(alpha = 0.06f),
            start = Offset(mulai + 8.dp.toPx(), y + 4.dp.toPx()),
            end = Offset(akhir - 12.dp.toPx(), y + 4.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

internal fun DrawScope.gambarJejakKapal(
    pusat: Offset,
    arahX: Float,
    arahY: Float,
    intensitas: Float,
    warna: Color,
) {
    if (intensitas <= 0.01f) return
    val panjang = 4.5f
    repeat(3) { i ->
        val jarak = (i + 1) * panjang * 2.6f
        val x = pusat.x - arahX * jarak
        val y = pusat.y - arahY * jarak
        val lebar = (5f - i) * 2.6f.dp.toPx()
        val alpha = (0.34f - i * 0.08f) * intensitas
        drawOval(
            color = warna.copy(alpha = alpha.coerceAtLeast(0f)),
            topLeft = Offset(x - lebar / 2f, y - lebar / 3f),
            size = Size(lebar, lebar * 0.66f),
            style = Stroke(width = 1.2.dp.toPx()),
        )
    }
}

internal fun DrawScope.gambarSonar(
    pusat: Offset,
    progres: Float,
    warna: Color,
    radiusMaks: Float,
) {
    if (progres <= 0f || progres >= 1f) return
    repeat(2) { i ->
        val p = ((progres + i * 0.2f) % 1f).coerceIn(0f, 1f)
        if (p <= 0.01f || p >= 0.99f) return@repeat
        drawCircle(
            color = warna.copy(alpha = (1f - p) * 0.60f / (i + 1)),
            radius = radiusMaks * p,
            center = pusat,
            style = Stroke(width = (1.6f - i * 0.4f).dp.toPx()),
        )
    }
}

internal fun DrawScope.gambarKonfeti(
    warna: Color,
    progres: Float,
    jumlah: Int = 22,
) {
    if (progres <= 0f || progres >= 1f) return
    repeat(jumlah) { i ->
        val sudut = (i / jumlah.toFloat()) * PI.toFloat() * 2f
        val jarak = (0.35f + (i * 17 % 41) / 100f) * size.minDimension * progres
        val x = size.width * 0.5f + cos(sudut.toDouble()).toFloat() * jarak
        val y = size.height * 0.45f + sin(sudut.toDouble()).toFloat() * jarak
        val ukuran = (2.2f + (i % 3) * 1.2f).dp.toPx()
        rotate(progres * 220f + i * 13f, Offset(x, y)) {
            drawRect(
                color = warna.copy(alpha = (1f - progres) * 0.9f),
                topLeft = Offset(x - ukuran, y - ukuran * 0.5f),
                size = Size(ukuran * 2f, ukuran),
            )
        }
    }
}
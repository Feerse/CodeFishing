package com.example.codefishing.frontend.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Kumpulan gambar vektor untuk adegan memancing. Semua bentuk digambar sebagai path
 * sehingga setara SVG, tajam di semua kerapatan layar, dan bisa dianimasikan per frame.
 */

internal fun DrawScope.gambarOmbak(
    warna: Color,
    fase: Float,
    dasar: Float,
    amplitudo: Float,
    jumlahGaris: Int = 3,
) {
    if (size.width <= 0f || size.height <= 0f) return
    repeat(jumlahGaris) { baris ->
        val path = Path()
        val offsetY = dasar + baris * amplitudo * 0.9f
        val langkah = size.width / 42f
        var x = 0f
        var pertama = true
        while (x <= size.width + langkah) {
            val sudut = (x / size.width) * PI.toFloat() * 2f + fase * PI.toFloat() * 2f + baris * 1.7f
            val y = offsetY + sin(sudut.toDouble()).toFloat() * amplitudo * (1f - baris * 0.16f)
            if (pertama) {
                path.moveTo(x, y)
                pertama = false
            } else {
                path.lineTo(x, y)
            }
            x += langkah
        }
        drawPath(
            path = path,
            color = warna.copy(alpha = (0.34f - baris * 0.07f).coerceAtLeast(0.10f)),
            style = Stroke(width = (1.3f + baris * 0.4f).dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

internal fun DrawScope.gambarKilauAir(
    warna: Color,
    fase: Float,
    dasar: Float,
) {
    repeat(5) { i ->
        val x = size.width * (0.12f + 0.19f * i)
        val goyang = sin((fase * 2f * PI.toFloat() + i * 1.3f).toDouble()).toFloat()
        val y = dasar + i % 3 * 6.dp.toPx()
        val lebar = (18f + i * 7f).dp.toPx()
        drawLine(
            color = warna.copy(alpha = 0.22f),
            start = Offset(x - lebar / 2f, y + goyang * 2f),
            end = Offset(x + lebar / 2f, y + goyang * 2f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

internal fun DrawScope.gambarJoran(
    ujung: Offset,
    tekuk: Float,
    warnaKayu: Color,
    warnaPegangan: Color,
) {
    val pangkal = Offset(size.width * 1.04f, -size.height * 0.06f)
    val kendali = Offset(
        pangkal.x - size.width * 0.14f,
        pangkal.y + size.height * 0.16f - tekuk * size.height * 0.06f,
    )
    val path = Path().apply {
        moveTo(pangkal.x, pangkal.y)
        quadraticTo(kendali.x, kendali.y, ujung.x, ujung.y)
    }
    drawPath(path, warnaKayu, style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round))
    drawPath(path, warnaKayu.copy(alpha = 0.55f), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
    drawCircle(warnaPegangan, radius = 4.2.dp.toPx(), center = ujung)
    drawCircle(warnaPegangan.copy(alpha = 0.65f), radius = 2.2.dp.toPx(), center = ujung)
}

internal fun DrawScope.gambarTali(
    mulai: Offset,
    selesai: Offset,
    kendur: Float,
    warna: Color,
    lebar: Float = 1.4f,
) {
    val dx = selesai.x - mulai.x
    val dy = selesai.y - mulai.y
    val kendali = Offset(
        mulai.x + dx * 0.52f,
        mulai.y + dy * 0.55f + kendur,
    )
    val path = Path().apply {
        moveTo(mulai.x, mulai.y)
        quadraticTo(kendali.x, kendali.y, selesai.x, selesai.y)
    }
    drawPath(path, warna.copy(alpha = 0.35f), style = Stroke(width = lebar.dp.toPx() * 2.2f, cap = StrokeCap.Round))
    drawPath(path, warna, style = Stroke(width = lebar.dp.toPx(), cap = StrokeCap.Round))
}

internal fun DrawScope.gambarTaliPutus(
    mulai: Offset,
    selesai: Offset,
    warna: Color,
    arah: Float,
) {
    val tengah = Offset((mulai.x + selesai.x) / 2f, (mulai.y + selesai.y) / 2f)
    drawLine(
        color = warna.copy(alpha = 0.9f),
        start = mulai,
        end = Offset(tengah.x - arah * 10.dp.toPx(), tengah.y - arah * 8.dp.toPx()),
        strokeWidth = 1.4.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = warna.copy(alpha = 0.7f),
        start = Offset(tengah.x + arah * 12.dp.toPx(), tengah.y + arah * 7.dp.toPx()),
        end = selesai,
        strokeWidth = 1.2.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

internal fun DrawScope.gambarPelampung(
    pusat: Offset,
    skala: Float,
    rotasi: Float,
    warnaBadan: Color,
    warnaPita: Color,
) {
    val lebar = 14.dp.toPx() * skala
    val tinggi = 20.dp.toPx() * skala
    drawOval(
        color = Color.Black.copy(alpha = 0.18f),
        topLeft = Offset(pusat.x - lebar / 2f, pusat.y - tinggi / 2f + 4.dp.toPx()),
        size = Size(lebar, tinggi * 0.55f),
    )
    rotate(rotasi, pusat) {
        drawOval(
            color = warnaBadan,
            topLeft = Offset(pusat.x - lebar / 2f, pusat.y - tinggi / 2f),
            size = Size(lebar, tinggi),
        )
        drawRect(
            color = warnaPita,
            topLeft = Offset(pusat.x - lebar / 2f, pusat.y - tinggi * 0.12f),
            size = Size(lebar, tinggi * 0.24f),
        )
        drawLine(
            color = warnaPita,
            start = Offset(pusat.x, pusat.y - tinggi / 2f),
            end = Offset(pusat.x, pusat.y - tinggi * 0.75f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

internal fun DrawScope.gambarRiak(
    pusat: Offset,
    progres: Float,
    warna: Color,
    jumlah: Int = 3,
) {
    if (progres <= 0f || progres >= 1f) return
    repeat(jumlah) { i ->
        val p = (progres + i * 0.18f) % 1f
        if (p <= 0f || p >= 1f) return@repeat
        val lebar = (16f + p * 88f).dp.toPx()
        val tinggi = lebar * 0.34f
        drawOval(
            color = warna.copy(alpha = (1f - p) * 0.62f),
            topLeft = Offset(pusat.x - lebar / 2f, pusat.y - tinggi / 2f),
            size = Size(lebar, tinggi),
            style = Stroke(width = 1.4.dp.toPx()),
        )
    }
}

internal fun DrawScope.gambarPercikan(
    pusat: Offset,
    progres: Float,
    warna: Color,
    jumlah: Int = 14,
) {
    if (progres <= 0f || progres >= 1f) return
    repeat(jumlah) { i ->
        val sudut = -PI.toFloat() * 0.92f + (i / jumlah.toFloat()) * PI.toFloat() * 0.84f
        val jarak = (18f + (i * 13 % 40)) * progres * 1.8f
        val gravitasi = 42f * progres * progres
        val x = pusat.x + cos(sudut.toDouble()).toFloat() * jarak
        val y = pusat.y + sin(sudut.toDouble()).toFloat() * jarak + gravitasi
        val radius = (1.2f + (i % 3) * 0.8f).dp.toPx()
        drawCircle(
            color = warna.copy(alpha = (1f - progres) * 0.85f),
            radius = radius,
            center = Offset(x, y),
        )
    }
}

internal fun DrawScope.gambarKilau(
    pusat: Offset,
    progres: Float,
    warna: Color,
    jumlah: Int = 10,
) {
    if (progres <= 0f || progres >= 1f) return
    repeat(jumlah) { i ->
        val sudut = (i / jumlah.toFloat()) * PI.toFloat() * 2f
        val jarak = (12f + (i * 17 % 26)) * (0.5f + progres)
        val x = pusat.x + cos(sudut.toDouble()).toFloat() * jarak.dp.toPx()
        val y = pusat.y + sin(sudut.toDouble()).toFloat() * jarak.dp.toPx()
        val panjang = (2.5f + (i % 3) * 1.4f).dp.toPx() * (1f - progres * 0.4f)
        val alpha = (1f - progres) * 0.95f
        drawLine(
            color = warna.copy(alpha = alpha),
            start = Offset(x - panjang, y),
            end = Offset(x + panjang, y),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = warna.copy(alpha = alpha),
            start = Offset(x, y - panjang),
            end = Offset(x, y + panjang),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

internal fun DrawScope.gambarHujan(
    warna: Color,
    fase: Float,
    jumlah: Int = 26,
) {
    repeat(jumlah) { i ->
        val x = ((i * 37 + 11) % 100) / 100f * size.width
        val y = ((i * 29 / 100f + fase * 1.4f) % 1f) * size.height
        val panjang = (8f + (i % 4) * 3f).dp.toPx()
        drawLine(
            color = warna.copy(alpha = 0.28f),
            start = Offset(x, y),
            end = Offset(x - 2.5.dp.toPx(), y + panjang),
            strokeWidth = 1.1.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

internal fun DrawScope.gambarSalju(
    warna: Color,
    fase: Float,
    jumlah: Int = 22,
) {
    repeat(jumlah) { i ->
        val x = ((i * 41 + 9) % 100) / 100f * size.width
        val y = ((i * 31 / 100f + fase) % 1f) * size.height
        val radius = (1.2f + (i % 3) * 0.7f).dp.toPx()
        drawCircle(
            color = warna.copy(alpha = 0.35f + (i % 3) * 0.12f),
            radius = radius,
            center = Offset(x + sin((fase * 2f * PI.toFloat() + i).toDouble()).toFloat() * 5.dp.toPx(), y),
        )
    }
}

internal fun DrawScope.gambarGelembung(
    warna: Color,
    fase: Float,
    dasar: Float,
    jumlah: Int = 12,
) {
    repeat(jumlah) { i ->
        val x = size.width * (0.16f + (i * 0.13f) % 0.72f)
        val naik = ((fase + i * 0.11f) % 1f)
        val y = dasar - naik * dasar * 0.8f
        val radius = (1.5f + (i % 3) * 1.1f).dp.toPx()
        drawCircle(
            color = warna.copy(alpha = 0.18f + (1f - naik) * 0.22f),
            radius = radius,
            center = Offset(x, y),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

internal fun DrawScope.gambarBayanganIkan(
    pusat: Offset,
    progres: Float,
    warna: Color,
) {
    if (progres <= 0f || progres >= 1f) return
    val lebar = (54f + progres * 32f).dp.toPx()
    val tinggi = lebar * 0.28f
    drawOval(
        color = warna.copy(alpha = (1f - progres) * 0.35f),
        topLeft = Offset(pusat.x - lebar / 2f, pusat.y - tinggi / 2f),
        size = Size(lebar, tinggi),
    )
    val ekor = Path().apply {
        moveTo(pusat.x + lebar / 2f, pusat.y)
        lineTo(pusat.x + lebar / 2f + tinggi * 0.8f, pusat.y - tinggi * 0.55f)
        lineTo(pusat.x + lebar / 2f + tinggi * 0.8f, pusat.y + tinggi * 0.55f)
        close()
    }
    drawPath(ekor, warna.copy(alpha = (1f - progres) * 0.28f))
}
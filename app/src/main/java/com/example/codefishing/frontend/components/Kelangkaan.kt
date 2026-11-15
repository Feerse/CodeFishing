package com.example.codefishing.frontend.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.data.LocalEfekVisual
import com.example.codefishing.core.data.hemat
import com.example.codefishing.ui.theme.LocalWarna
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Identitas visual tiap kelangkaan ikan.
 *
 * Setiap tier punya BENTUK bingkai, ornamen, dan perilaku efek yang berbeda, bukan hanya warna,
 * sehingga tetap terbedakan oleh pemain buta warna dan tetap terbaca di tema terang maupun gelap:
 *
 *   Biasa    polos          : garis tipis, tanpa ornamen, tanpa efek.
 *   Langka   kristal        : siku kristal + permata sudut, kilau berkedip pelan.
 *   Epik     rune           : cincin ganda (luar solid, dalam putus-putus), bintang sudut,
 *                             bintang mengorbit di tepi, cahaya bernapas.
 *   Legenda  mahkota        : bingkai emas bergradien tebal, mahkota di puncak, sinar matahari
 *                             berputar di belakang ikan, sapuan kilap, debu emas naik.
 *   Mitos    api & aurora   : bingkai gradien pelangi yang berputar, nyala api di puncak,
 *                             pusaran busur, bara api naik, halo berdenyut.
 */

enum class Kelangkaan { BIASA, LANGKA, EPIK, LEGENDA, MITOS }

fun kelangkaan(tier: String): Kelangkaan = when (tier) {
    "langka" -> Kelangkaan.LANGKA
    "epik" -> Kelangkaan.EPIK
    "legenda" -> Kelangkaan.LEGENDA
    "mitos" -> Kelangkaan.MITOS
    else -> Kelangkaan.BIASA
}

enum class BentukBingkai { POLOS, SIKU_KRISTAL, CINCIN_GANDA, EMAS_TEBAL, PELANGI_BERPUTAR }
enum class OrnamenSudut { TIDAK_ADA, PERMATA, BINTANG, API }
enum class OrnamenPuncak { TIDAK_ADA, PERMATA, BINTANG_ORBIT, MAHKOTA, NYALA }
enum class JenisPartikel { TIDAK_ADA, KILAU_BERKEDIP, BINTANG_ORBIT, DEBU_EMAS, BARA }

@Immutable
data class GayaTier(
    val bingkai: BentukBingkai,
    val sudut: OrnamenSudut,
    val puncak: OrnamenPuncak,
    val partikel: JenisPartikel,
    val jumlahPartikel: Int,
    val sinarBerputar: Boolean,
    val sapuanKilap: Boolean,
    val denyut: Boolean,
    val pusaran: Boolean,
    /** Tebal garis bingkai dalam dp. */
    val tebalDp: Float,
    /** Seberapa kuat cahaya latar, 0..1. */
    val cahaya: Float,
)

/** Gaya statis tiap tier. Murni data supaya mudah dites. */
fun gayaTier(k: Kelangkaan): GayaTier = when (k) {
    Kelangkaan.BIASA -> GayaTier(
        BentukBingkai.POLOS, OrnamenSudut.TIDAK_ADA, OrnamenPuncak.TIDAK_ADA,
        JenisPartikel.TIDAK_ADA, 0, sinarBerputar = false, sapuanKilap = false,
        denyut = false, pusaran = false, tebalDp = 1f, cahaya = 0f,
    )
    Kelangkaan.LANGKA -> GayaTier(
        BentukBingkai.SIKU_KRISTAL, OrnamenSudut.PERMATA, OrnamenPuncak.PERMATA,
        JenisPartikel.KILAU_BERKEDIP, 3, sinarBerputar = false, sapuanKilap = false,
        denyut = false, pusaran = false, tebalDp = 1.5f, cahaya = 0.30f,
    )
    Kelangkaan.EPIK -> GayaTier(
        BentukBingkai.CINCIN_GANDA, OrnamenSudut.BINTANG, OrnamenPuncak.BINTANG_ORBIT,
        JenisPartikel.BINTANG_ORBIT, 4, sinarBerputar = false, sapuanKilap = false,
        denyut = true, pusaran = false, tebalDp = 1.5f, cahaya = 0.50f,
    )
    Kelangkaan.LEGENDA -> GayaTier(
        BentukBingkai.EMAS_TEBAL, OrnamenSudut.TIDAK_ADA, OrnamenPuncak.MAHKOTA,
        JenisPartikel.DEBU_EMAS, 7, sinarBerputar = true, sapuanKilap = true,
        denyut = false, pusaran = false, tebalDp = 2.6f, cahaya = 0.75f,
    )
    Kelangkaan.MITOS -> GayaTier(
        BentukBingkai.PELANGI_BERPUTAR, OrnamenSudut.API, OrnamenPuncak.NYALA,
        JenisPartikel.BARA, 9, sinarBerputar = false, sapuanKilap = false,
        denyut = true, pusaran = true, tebalDp = 3f, cahaya = 1f,
    )
}

/** Warna tier yang cerah untuk digambar di atas adegan yang selalu gelap (tidak bergantung tema). */
fun warnaTierAdegan(tier: String): Color = when (kelangkaan(tier)) {
    Kelangkaan.BIASA -> Color(0xFFB8C7CE)
    Kelangkaan.LANGKA -> Color(0xFF5B9CFF)
    Kelangkaan.EPIK -> Color(0xFFB388FF)
    Kelangkaan.LEGENDA -> Color(0xFFFFC93C)
    Kelangkaan.MITOS -> Color(0xFFFF4D6D)
}

/**
 * Warna pendukung tiap tier. Di tema gelap memakai warna terang (cahaya di atas kartu gelap); di tema
 * terang memakai warna lebih pekat plus [kontur] gelap supaya ornamen tidak pudar di atas kartu terang.
 */
@Immutable
private data class PaletEfek(
    val utama: Color,
    val terang: Color,
    val gelap: Color,
    val spektrum: List<Color>,
    val kontur: Boolean,
)

private fun paletEfek(k: Kelangkaan, utama: Color, gelapTema: Boolean): PaletEfek {
    val kontur = !gelapTema
    return when (k) {
        Kelangkaan.BIASA -> PaletEfek(utama, utama, utama, listOf(utama), false)
        Kelangkaan.LANGKA -> if (gelapTema) {
            PaletEfek(utama, Color(0xFF9FE7FF), Color(0xFF1F4FA3), listOf(utama, Color(0xFF7FE0FF)), kontur)
        } else {
            PaletEfek(utama, Color(0xFF3D8BFF), Color(0xFF173F8F), listOf(utama, Color(0xFF3D8BFF)), kontur)
        }
        Kelangkaan.EPIK -> if (gelapTema) {
            PaletEfek(utama, Color(0xFFFFA8F2), Color(0xFF4B2A9A), listOf(utama, Color(0xFFFF8AE8), utama), kontur)
        } else {
            PaletEfek(utama, Color(0xFFC13DE0), Color(0xFF3F2189), listOf(utama, Color(0xFFC13DE0), utama), kontur)
        }
        Kelangkaan.LEGENDA -> if (gelapTema) {
            PaletEfek(
                utama, Color(0xFFFFF3B8), Color(0xFFB8860B),
                listOf(Color(0xFFFFF3B8), Color(0xFFFFC93C), Color(0xFFB8860B), Color(0xFFFFE27A)), kontur,
            )
        } else {
            PaletEfek(
                utama, Color(0xFFFFB800), Color(0xFF7A4A00),
                listOf(Color(0xFFFFD54A), Color(0xFFFFB800), Color(0xFF9A6A00), Color(0xFFFFC93C)), kontur,
            )
        }
        Kelangkaan.MITOS -> PaletEfek(
            utama,
            if (gelapTema) Color(0xFFFFD1A8) else Color(0xFFFF6A2B),
            if (gelapTema) Color(0xFF8E1330) else Color(0xFF7A0F26),
            listOf(Color(0xFFFF4D6D), Color(0xFFFF8A3D), Color(0xFFE63DFF), Color(0xFF3DD9FF), Color(0xFFFF4D6D)),
            kontur,
        )
    }
}

// ------------------------------------------------------------------------------ util gambar
private fun frac(x: Float): Float = x - floor(x)

/** Angka acak deterministik 0..1 untuk partikel ke-[i]. */
internal fun acak(i: Int, benih: Int): Float = frac(sin(i * 12.9898f + benih * 78.233f) * 43758.547f)

private fun Path.bintangEmpat(cx: Float, cy: Float, r: Float) {
    val d = r * 0.34f
    moveTo(cx, cy - r)
    lineTo(cx + d, cy - d)
    lineTo(cx + r, cy)
    lineTo(cx + d, cy + d)
    lineTo(cx, cy + r)
    lineTo(cx - d, cy + d)
    lineTo(cx - r, cy)
    lineTo(cx - d, cy - d)
    close()
}

private fun Path.belahKetupat(cx: Float, cy: Float, r: Float) {
    moveTo(cx, cy - r)
    lineTo(cx + r * 0.72f, cy)
    lineTo(cx, cy + r)
    lineTo(cx - r * 0.72f, cy)
    close()
}

private fun Path.nyala(cx: Float, dasarY: Float, lebar: Float, tinggi: Float) {
    moveTo(cx, dasarY - tinggi)
    cubicTo(cx + lebar * 0.95f, dasarY - tinggi * 0.45f, cx + lebar * 0.55f, dasarY, cx, dasarY)
    cubicTo(cx - lebar * 0.55f, dasarY, cx - lebar * 0.95f, dasarY - tinggi * 0.45f, cx, dasarY - tinggi)
    close()
}

private fun Path.mahkota(cx: Float, dasarY: Float, lebar: Float, tinggi: Float) {
    val l = lebar / 2f
    moveTo(cx - l, dasarY)
    lineTo(cx - l, dasarY - tinggi * 0.55f)
    lineTo(cx - l * 0.5f, dasarY - tinggi * 0.28f)
    lineTo(cx, dasarY - tinggi)
    lineTo(cx + l * 0.5f, dasarY - tinggi * 0.28f)
    lineTo(cx + l, dasarY - tinggi * 0.55f)
    lineTo(cx + l, dasarY)
    close()
}

/** Titik pada keliling persegi bulat sepanjang [t] (0..1), mengitari searah jarum jam. */
private fun titikKeliling(t: Float, w: Float, h: Float, inset: Float): Offset {
    val x0 = inset
    val y0 = inset
    val lebar = w - 2 * inset
    val tinggi = h - 2 * inset
    val total = 2 * (lebar + tinggi)
    var s = frac(t) * total
    if (s < lebar) return Offset(x0 + s, y0)
    s -= lebar
    if (s < tinggi) return Offset(x0 + lebar, y0 + s)
    s -= tinggi
    if (s < lebar) return Offset(x0 + lebar - s, y0 + tinggi)
    s -= lebar
    return Offset(x0, y0 + tinggi - s)
}

private fun denyut(waktu: Float, aktif: Boolean): Float =
    if (aktif) 0.5f + 0.5f * sin(waktu * 2f * PI.toFloat() * 2f) else 0.5f

// ------------------------------------------------------------------- lapisan di BELAKANG isi
private fun DrawScope.efekBelakang(
    k: Kelangkaan,
    g: GayaTier,
    p: PaletEfek,
    waktu: Float,
    bulat: Boolean,
    gelap: Boolean,
) {
    if (k == Kelangkaan.BIASA) return
    val w = size.width
    val h = size.height
    val pusat = Offset(w / 2f, h / 2f)
    val jari = min(w, h) / 2f
    val dyn = denyut(waktu, g.denyut)
    val aLatar = if (gelap) 1f else 0.8f

    // Cahaya latar berbentuk radial
    val kuat = (g.cahaya * (0.55f + 0.45f * dyn)) * aLatar
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(p.utama.copy(alpha = 0.34f * kuat), p.utama.copy(alpha = 0f)),
            center = pusat,
            radius = max(w, h) * (if (bulat) 0.62f else 0.78f),
        ),
        radius = max(w, h) * (if (bulat) 0.62f else 0.78f),
        center = pusat,
    )

    // Sinar matahari berputar (legenda). Hanya untuk aura ikan: pada kartu, aura ikan sudah
    // menggambarnya tepat di belakang ikan sehingga tidak digambar dua kali di tengah kartu.
    if (g.sinarBerputar && bulat) {
        rotate(waktu * 360f * 0.5f, pusat) {
            val panjang = max(w, h) * 0.72f
            for (i in 0 until 12) {
                val a = i * (2f * PI.toFloat() / 12f)
                val a2 = a + 0.13f
                val path = Path().apply {
                    moveTo(pusat.x, pusat.y)
                    lineTo(pusat.x + cos(a) * panjang, pusat.y + sin(a) * panjang)
                    lineTo(pusat.x + cos(a2) * panjang, pusat.y + sin(a2) * panjang)
                    close()
                }
                drawPath(
                    path,
                    Brush.radialGradient(
                        listOf(p.terang.copy(alpha = 0.28f * aLatar), p.terang.copy(alpha = 0f)),
                        center = pusat,
                        radius = panjang,
                    ),
                )
            }
        }
    }

    // Pusaran busur (mitos); sama seperti sinar, hanya untuk aura ikan.
    if (g.pusaran && bulat) {
        val tebal = 2.2.dp.toPx()
        for (i in 0 until 3) {
            val r = jari * (0.46f + i * 0.17f)
            rotate((if (i % 2 == 0) 1f else -1f) * waktu * 360f * (0.8f + i * 0.35f), pusat) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(p.spektrum[i % p.spektrum.size].copy(alpha = 0f), p.spektrum[i % p.spektrum.size].copy(alpha = 0.9f)),
                        center = pusat,
                    ),
                    startAngle = 0f,
                    sweepAngle = 220f,
                    useCenter = false,
                    topLeft = Offset(pusat.x - r, pusat.y - r),
                    size = Size(r * 2f, r * 2f),
                    style = Stroke(tebal, cap = StrokeCap.Round),
                )
            }
        }
    }

    // Kristal: garis faset tipis di sudut kiri atas
    if (k == Kelangkaan.LANGKA && !bulat) {
        val f = p.terang.copy(alpha = 0.28f)
        drawLine(f, Offset(0f, h * 0.32f), Offset(w * 0.32f, 0f), 1.2.dp.toPx())
        drawLine(f, Offset(0f, h * 0.18f), Offset(w * 0.18f, 0f), 1.2.dp.toPx())
        drawLine(f, Offset(w, h * 0.82f), Offset(w * 0.82f, h), 1.2.dp.toPx())
    }
}

// -------------------------------------------------------------------- lapisan di DEPAN isi
private fun DrawScope.efekDepan(
    k: Kelangkaan,
    g: GayaTier,
    p: PaletEfek,
    waktu: Float,
    radiusPx: Float,
    kotak: Boolean,
    gelap: Boolean,
) {
    if (k == Kelangkaan.BIASA) return
    val w = size.width
    val h = size.height
    val dyn = denyut(waktu, g.denyut)

    // Sapuan kilap diagonal (legenda): pita putih lewat tiap siklus
    if (g.sapuanKilap) {
        val fase = frac(waktu * 1.0f)
        if (fase < 0.45f) {
            val t = fase / 0.45f
            val x = -w * 0.4f + (w * 1.8f) * t
            val lebarPita = w * 0.22f
            rotate(18f, Offset(w / 2f, h / 2f)) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        // Putih tak terlihat di kartu terang, jadi sapuan memakai emas di tema terang.
                        listOf(
                            (if (gelap) Color.White else p.terang).copy(alpha = 0f),
                            (if (gelap) Color.White else p.terang).copy(alpha = if (gelap) 0.34f else 0.55f),
                            (if (gelap) Color.White else p.terang).copy(alpha = 0f),
                        ),
                        startX = x,
                        endX = x + lebarPita,
                    ),
                    topLeft = Offset(x, -h * 0.3f),
                    size = Size(lebarPita, h * 1.6f),
                )
            }
        }
    }

    if (kotak) gambarBingkai(k, g, p, waktu, radiusPx, dyn)
    if (kotak) gambarOrnamen(k, g, p, waktu, dyn)
    gambarPartikel(k, g, p, waktu, kotak)
}

private fun DrawScope.gambarBingkai(
    k: Kelangkaan,
    g: GayaTier,
    p: PaletEfek,
    waktu: Float,
    r: Float,
    dyn: Float,
) {
    val w = size.width
    val h = size.height
    val t = g.tebalDp.dp.toPx()
    val ins = t / 2f
    val ukuran = Size(w - t, h - t)
    val sudut = CornerRadius(r)
    when (g.bingkai) {
        BentukBingkai.POLOS -> Unit
        BentukBingkai.SIKU_KRISTAL -> {
            drawRoundRect(p.utama.copy(alpha = 0.55f), Offset(ins, ins), ukuran, sudut, style = Stroke(t))
            // Siku tebal di keempat sudut (kristal)
            val L = 16.dp.toPx()
            val tb = t * 2f
            val c = p.terang
            fun siku(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(c, Offset(x, y + dy * 0f), Offset(x + dx * L, y), tb, StrokeCap.Round)
                drawLine(c, Offset(x, y), Offset(x, y + dy * L), tb, StrokeCap.Round)
            }
            val m = r * 0.35f + ins
            siku(m, m, 1f, 1f)
            siku(w - m, m, -1f, 1f)
            siku(m, h - m, 1f, -1f)
            siku(w - m, h - m, -1f, -1f)
        }
        BentukBingkai.CINCIN_GANDA -> {
            val a = 0.7f + 0.3f * dyn
            drawRoundRect(p.utama.copy(alpha = a), Offset(ins, ins), ukuran, sudut, style = Stroke(t))
            val dalam = 5.dp.toPx()
            drawRoundRect(
                p.terang.copy(alpha = 0.75f),
                Offset(dalam, dalam),
                Size(w - 2 * dalam, h - 2 * dalam),
                CornerRadius(max(2f, r - dalam + t)),
                style = Stroke(
                    1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()), waktu * 40.dp.toPx()),
                ),
            )
        }
        BentukBingkai.EMAS_TEBAL -> {
            drawRoundRect(
                Brush.linearGradient(
                    p.spektrum,
                    start = Offset(0f, 0f),
                    end = Offset(w, h),
                ),
                Offset(ins, ins), ukuran, sudut, style = Stroke(t),
            )
            // Garis tipis terang di dalam untuk kesan emas bertatah
            val dalam = t + 2.dp.toPx()
            drawRoundRect(
                p.terang.copy(alpha = 0.55f),
                Offset(dalam, dalam),
                Size(w - 2 * dalam, h - 2 * dalam),
                CornerRadius(max(2f, r - dalam + ins)),
                style = Stroke(0.8.dp.toPx()),
            )
        }
        BentukBingkai.PELANGI_BERPUTAR -> {
            val pusat = Offset(w / 2f, h / 2f)
            rotate(waktu * 360f, pusat) {
                drawRoundRect(
                    Brush.sweepGradient(p.spektrum, center = pusat),
                    Offset(ins, ins), ukuran, sudut, style = Stroke(t),
                )
            }
            // Kilap halo luar berdenyut
            drawRoundRect(
                p.utama.copy(alpha = 0.18f + 0.22f * dyn),
                Offset(0f, 0f), Size(w, h), CornerRadius(r + ins),
                style = Stroke(t * 2.4f),
            )
        }
    }
}

private fun DrawScope.gambarOrnamen(k: Kelangkaan, g: GayaTier, p: PaletEfek, waktu: Float, dyn: Float) {
    val w = size.width
    val h = size.height
    val m = 7.dp.toPx()
    // Ornamen sudut
    when (g.sudut) {
        OrnamenSudut.TIDAK_ADA -> Unit
        OrnamenSudut.PERMATA -> {
            for ((x, y) in listOf(m to m, w - m to m, m to h - m, w - m to h - m)) {
                val path = Path().apply { belahKetupat(x, y, 4.5.dp.toPx()) }
                drawPath(path, p.terang)
                drawPath(path, p.dasarGelap(), style = Stroke(0.8.dp.toPx()))
            }
        }
        OrnamenSudut.BINTANG -> {
            for ((x, y) in listOf(m to m, w - m to m, m to h - m, w - m to h - m)) {
                val path = Path().apply { bintangEmpat(x, y, (5.5f + 1.5f * dyn).dp.toPx()) }
                drawPath(path, p.terang)
                if (p.kontur) drawPath(path, p.dasarGelap(), style = Stroke(0.9.dp.toPx()))
            }
        }
        OrnamenSudut.API -> {
            val tinggi = 13.dp.toPx()
            val lebar = 6.dp.toPx()
            for ((x, sisi) in listOf(m + 2.dp.toPx() to 0, w - m - 2.dp.toPx() to 1)) {
                val fl = (0.85f + 0.3f * sin(waktu * 2f * PI.toFloat() * 3f + sisi))
                val path = Path().apply { nyala(x, h - 2.dp.toPx(), lebar, tinggi * fl) }
                drawPath(path, Brush.verticalGradient(listOf(p.terang, p.utama), startY = h - tinggi, endY = h))
                if (p.kontur) drawPath(path, p.dasarGelap(), style = Stroke(0.9.dp.toPx()))
            }
        }
    }
    // Ornamen puncak (tengah atas)
    val cx = w / 2f
    when (g.puncak) {
        OrnamenPuncak.TIDAK_ADA -> Unit
        OrnamenPuncak.PERMATA -> {
            val path = Path().apply { belahKetupat(cx, 7.dp.toPx(), 5.5.dp.toPx()) }
            drawPath(path, p.terang)
            drawPath(path, p.dasarGelap(), style = Stroke(0.8.dp.toPx()))
        }
        OrnamenPuncak.BINTANG_ORBIT -> {
            val path = Path().apply { bintangEmpat(cx, 8.dp.toPx(), 7.dp.toPx()) }
            drawPath(path, p.terang)
            if (p.kontur) drawPath(path, p.dasarGelap(), style = Stroke(0.9.dp.toPx()))
        }
        OrnamenPuncak.MAHKOTA -> {
            val lebar = 26.dp.toPx()
            val tinggi = 15.dp.toPx()
            val dasar = tinggi + 1.dp.toPx()   // puncak mahkota tepat di dalam tepi atas kartu
            val path = Path().apply { mahkota(cx, dasar, lebar, tinggi) }
            drawPath(path, Brush.verticalGradient(listOf(p.terang, p.utama), startY = 0f, endY = dasar))
            drawPath(path, p.dasarGelap(), style = Stroke(1.dp.toPx()))
            for (dx in listOf(-lebar / 2f, 0f, lebar / 2f)) {
                val yy = if (dx == 0f) dasar - tinggi else dasar - tinggi * 0.55f
                drawCircle(Color.White, 1.8.dp.toPx(), Offset(cx + dx, yy))
            }
        }
        OrnamenPuncak.NYALA -> {
            val lebar = 11.dp.toPx()
            val tinggi = (16f + 3f * sin(waktu * 2f * PI.toFloat() * 3f)).dp.toPx()
            val path = Path().apply { nyala(cx, tinggi + 2.dp.toPx(), lebar, tinggi) }
            drawPath(path, Brush.verticalGradient(listOf(p.terang, p.utama, p.gelap), startY = 2.dp.toPx(), endY = tinggi + 2.dp.toPx()))
            if (p.kontur) drawPath(path, p.dasarGelap(), style = Stroke(1.dp.toPx()))
            val dalam = Path().apply { nyala(cx, tinggi + 1.dp.toPx(), lebar * 0.45f, tinggi * 0.55f) }
            drawPath(dalam, Color.White.copy(alpha = 0.85f))
        }
    }
}

private fun PaletEfek.dasarGelap(): Color = gelap.copy(alpha = 0.9f)

private fun DrawScope.gambarPartikel(k: Kelangkaan, g: GayaTier, p: PaletEfek, waktu: Float, kotak: Boolean) {
    val w = size.width
    val h = size.height
    when (g.partikel) {
        JenisPartikel.TIDAK_ADA -> Unit
        JenisPartikel.KILAU_BERKEDIP -> {
            for (i in 0 until g.jumlahPartikel) {
                val x = w * (0.18f + 0.64f * acak(i, 1))
                val y = h * (0.15f + 0.65f * acak(i, 2))
                val fase = frac(waktu * 1.5f + acak(i, 3))
                val a = max(0f, sin(fase * PI.toFloat())).let { it * it }
                if (a > 0.02f) {
                    val path = Path().apply { bintangEmpat(x, y, (2.5f + 3.5f * a).dp.toPx()) }
                    drawPath(path, p.terang.copy(alpha = a))
                }
            }
        }
        JenisPartikel.BINTANG_ORBIT -> {
            val inset = 3.dp.toPx()
            for (i in 0 until g.jumlahPartikel) {
                val t = waktu * 0.6f + i / g.jumlahPartikel.toFloat()
                val pos = if (kotak) titikKeliling(t, w, h, inset) else {
                    val a = t * 2f * PI.toFloat()
                    val r = min(w, h) / 2f - inset
                    Offset(w / 2f + cos(a) * r, h / 2f + sin(a) * r)
                }
                val kecil = 0.8f + 0.5f * sin((waktu * 4f + i) * PI.toFloat())
                val path = Path().apply { bintangEmpat(pos.x, pos.y, (3.6f * kecil).dp.toPx()) }
                drawPath(path, p.terang)
            }
        }
        JenisPartikel.DEBU_EMAS -> {
            for (i in 0 until g.jumlahPartikel) {
                val kecepatan = 0.5f + 0.6f * acak(i, 4)
                val naik = frac(waktu * kecepatan * 2f + acak(i, 5))
                val x = w * (0.12f + 0.76f * acak(i, 6)) + sin((naik + i) * 6f) * 3.dp.toPx()
                val y = h * (1.0f - naik)
                val a = sin(naik * PI.toFloat())
                drawCircle(p.terang.copy(alpha = 0.9f * a), (1.4f + 1.4f * acak(i, 7)).dp.toPx(), Offset(x, y))
            }
        }
        JenisPartikel.BARA -> {
            for (i in 0 until g.jumlahPartikel) {
                val kecepatan = 0.6f + 0.7f * acak(i, 8)
                val naik = frac(waktu * kecepatan * 2f + acak(i, 9))
                val x = w * (0.1f + 0.8f * acak(i, 10)) + sin((naik * 5f + i)) * 5.dp.toPx()
                val y = h * (1.0f - naik)
                val a = sin(naik * PI.toFloat())
                val warna = p.spektrum[i % (p.spektrum.size - 1)]
                val path = Path().apply { nyala(x, y, (2.2f + 1.5f * acak(i, 11)).dp.toPx(), (5f + 4f * acak(i, 12)).dp.toPx()) }
                drawPath(path, warna.copy(alpha = a))
            }
        }
    }
}

// ----------------------------------------------------------------------------- komponen publik

@Composable
private fun rememberWaktuTier(animasi: Boolean): State<Float> {
    val kurangiGerak = remember { !ValueAnimator.areAnimatorsEnabled() }
    // Mode "Hemat" mematikan kilau tier sepenuhnya: efek ini menggambar ulang bingkai terus-menerus
    // dan tidak membawa informasi apa pun.
    val hemat = LocalEfekVisual.current.hemat
    if (!animasi || kurangiGerak || hemat) return remember { mutableFloatStateOf(0.33f) }
    val transisi = rememberInfiniteTransition(label = "kelangkaan")
    val mentah: State<Float> = transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Restart),
        label = "waktu-tier",
    )
    // Dibulatkan ke 150 langkah per siklus (±29 fps): kanvas hanya digambar ulang saat nilai berubah,
    // separuh dari 60 fps, dan efek dekoratif tetap terlihat mulus.
    return remember { derivedStateOf { floor(mentah.value * LANGKAH_WAKTU) / LANGKAH_WAKTU } }
}

private const val LANGKAH_WAKTU = 150f

/**
 * Bingkai kartu sesuai kelangkaan. Bentuk bingkai, ornamen sudut/puncak, dan efek berbeda tiap tier.
 * Tier biasa dibiarkan polos (tanpa lapisan tambahan) agar daftar panjang tetap ringan.
 */
@Composable
fun BingkaiTier(
    tier: String,
    modifier: Modifier = Modifier,
    radius: Dp = 16.dp,
    animasi: Boolean = true,
    isi: @Composable BoxScope.() -> Unit,
) {
    val k = kelangkaan(tier)
    val warna = LocalWarna.current
    val utama = tierWarna(tier)
    val bentuk = RoundedCornerShape(radius)
    if (k == Kelangkaan.BIASA) {
        Box(
            modifier
                .clip(bentuk)
                .background(warna.kartuLembut, bentuk)
                .border(1.dp, utama.copy(alpha = 0.45f), bentuk),
            content = isi,
        )
        return
    }
    val g = gayaTier(k)
    val p = paletEfek(k, utama, warna.gelap)
    val waktu = rememberWaktuTier(animasi)
    Box(modifier.clip(bentuk).background(warna.kartuLembut, bentuk)) {
        Canvas(Modifier.matchParentSize()) {
            efekBelakang(k, g, p, waktu.value, bulat = false, gelap = warna.gelap)
        }
        isi()
        Canvas(Modifier.matchParentSize()) {
            efekDepan(k, g, p, waktu.value, radius.toPx(), kotak = true, gelap = warna.gelap)
        }
    }
}

/**
 * Aura di sekeliling gambar ikan (tanpa bingkai kartu). Dipakai di detail ikan, hasil tangkapan,
 * akuarium, dan peta. Ukuran aura mengikuti ukuran [modifier].
 */
@Composable
fun AuraIkan(
    tier: String,
    modifier: Modifier = Modifier,
    animasi: Boolean = true,
    isi: @Composable BoxScope.() -> Unit,
) {
    val k = kelangkaan(tier)
    if (k == Kelangkaan.BIASA) {
        Box(modifier, contentAlignment = Alignment.Center, content = isi)
        return
    }
    val warna = LocalWarna.current
    val g = gayaTier(k)
    val p = paletEfek(k, tierWarna(tier), warna.gelap)
    val waktu = rememberWaktuTier(animasi)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            efekBelakang(k, g, p, waktu.value, bulat = true, gelap = warna.gelap)
        }
        isi()
        Canvas(Modifier.matchParentSize()) {
            efekDepan(k, g, p, waktu.value, 0f, kotak = false, gelap = warna.gelap)
        }
    }
}

/**
 * Lambang kecil kelangkaan (dipakai di chip): lingkaran, belah ketupat, bintang empat, mahkota, api.
 * Bentuknya berbeda tiap tier sehingga tier terbaca tanpa bergantung pada warna.
 */
@Composable
fun LambangTier(tier: String, ukuran: Dp = 12.dp, modifier: Modifier = Modifier) {
    val k = kelangkaan(tier)
    val utama = tierWarna(tier)
    Canvas(modifier.then(Modifier.size(ukuran))) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)
        when (k) {
            Kelangkaan.BIASA -> drawCircle(utama, w * 0.34f, c, style = Stroke(w * 0.14f))
            Kelangkaan.LANGKA -> drawPath(Path().apply { belahKetupat(c.x, c.y, w * 0.46f) }, utama)
            Kelangkaan.EPIK -> drawPath(Path().apply { bintangEmpat(c.x, c.y, w * 0.5f) }, utama)
            Kelangkaan.LEGENDA -> drawPath(Path().apply { mahkota(c.x, h * 0.88f, w * 0.92f, h * 0.76f) }, utama)
            Kelangkaan.MITOS -> drawPath(Path().apply { nyala(c.x, h * 0.96f, w * 0.46f, h * 0.92f) }, utama)
        }
    }
}

/**
 * Menggambar aura tier langsung di Canvas (mis. di belakang ikan di adegan memancing).
 * [pusat] dan [jari] dalam piksel; [waktu] 0..1 berulang.
 */
fun DrawScope.gambarAuraTier(tier: String, pusat: Offset, jari: Float, waktu: Float, gelap: Boolean, utama: Color, alpha: Float = 1f) {
    val k = kelangkaan(tier)
    if (k == Kelangkaan.BIASA || alpha <= 0.01f) return
    val g = gayaTier(k)
    val p = paletEfek(k, utama, gelap)
    val dyn = denyut(waktu, g.denyut)
    val kuat = g.cahaya * (0.55f + 0.45f * dyn) * alpha
    drawCircle(
        brush = Brush.radialGradient(
            listOf(p.utama.copy(alpha = 0.55f * kuat), p.utama.copy(alpha = 0f)),
            center = pusat,
            radius = jari,
        ),
        radius = jari,
        center = pusat,
    )
    if (g.sinarBerputar) {
        rotate(waktu * 180f, pusat) {
            for (i in 0 until 12) {
                val a = i * (2f * PI.toFloat() / 12f)
                val a2 = a + 0.13f
                val path = Path().apply {
                    moveTo(pusat.x, pusat.y)
                    lineTo(pusat.x + cos(a) * jari, pusat.y + sin(a) * jari)
                    lineTo(pusat.x + cos(a2) * jari, pusat.y + sin(a2) * jari)
                    close()
                }
                drawPath(path, p.terang.copy(alpha = 0.22f * alpha))
            }
        }
    }
    if (g.pusaran) {
        for (i in 0 until 2) {
            val r = jari * (0.6f + i * 0.22f)
            rotate((if (i == 0) 1f else -1f) * waktu * 360f, pusat) {
                drawArc(
                    color = p.spektrum[i % p.spektrum.size].copy(alpha = 0.8f * alpha),
                    startAngle = 0f, sweepAngle = 200f, useCenter = false,
                    topLeft = Offset(pusat.x - r, pusat.y - r), size = Size(r * 2f, r * 2f),
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }
    }
    if (g.partikel == JenisPartikel.KILAU_BERKEDIP || g.partikel == JenisPartikel.BINTANG_ORBIT ||
        g.partikel == JenisPartikel.DEBU_EMAS || g.partikel == JenisPartikel.BARA
    ) {
        for (i in 0 until g.jumlahPartikel) {
            val a = (waktu * (if (g.partikel == JenisPartikel.BINTANG_ORBIT) 0.6f else 1f) + i / g.jumlahPartikel.toFloat()) * 2f * PI.toFloat()
            val r = jari * (0.55f + 0.4f * acak(i, 21))
            val pos = Offset(pusat.x + cos(a) * r, pusat.y + sin(a) * r * 0.7f)
            val kedip = 0.5f + 0.5f * sin((waktu * 3f + acak(i, 22)) * 2f * PI.toFloat())
            val path = Path().apply { bintangEmpat(pos.x, pos.y, (2.2f + 2f * kedip).dp.toPx()) }
            drawPath(path, p.terang.copy(alpha = (0.35f + 0.6f * kedip) * alpha))
        }
    }
}

/** Waktu 0..1 berulang untuk dipakai [gambarAuraTier] di luar komponen di atas. */
@Composable
fun rememberWaktuKelangkaan(animasi: Boolean = true): State<Float> = rememberWaktuTier(animasi)

package com.example.codefishing.frontend.components

import android.animation.ValueAnimator
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.core.art.Art
import com.example.codefishing.ui.theme.LocalWarna
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Fase animasi nelayan pada layar Kuis Mode Belajar.
 * - [Diam]  : soal baru, tali tergulung dan adegan tenang.
 * - [Lempar]: pemain memilih opsi, kail dilempar dan menunggu.
 * - [Dapat] : jawaban benar dikunci, ikan ditarik lalu melompat.
 * - [Lepas] : jawaban salah dikunci, tali menegang lalu putus.
 */
enum class FasePancing { Diam, Lempar, Dapat, Lepas }

/**
 * Adegan studi kasus untuk tiap stage (aset 03_Studi_Kasus), dicocokkan dengan cerita soalnya:
 * kapal, dermaga, perahu cadik, penyelam, lubang es, bubu. Boss memakai kapal pukat di tengah badai.
 */
@DrawableRes
fun adeganStage(nomor: Int, boss: Boolean): Int = when {
    boss -> Art.adegan_kapal_pukat_badai
    else -> when (nomor) {
        2 -> Art.adegan_dermaga
        3, 7 -> Art.adegan_perahu_cadik
        4, 9 -> Art.adegan_penyelam
        5 -> Art.adegan_lubang_es
        6, 8 -> Art.adegan_bubu
        else -> Art.adegan_kapal
    }
}

private enum class Suasana { Laut, Dermaga, Es, BawahLaut, Bubu, Badai }

private fun suasanaStage(nomor: Int, boss: Boolean): Suasana = when {
    boss -> Suasana.Badai
    nomor == 5 -> Suasana.Es
    nomor == 4 || nomor == 9 -> Suasana.BawahLaut
    nomor == 2 -> Suasana.Dermaga
    nomor == 6 || nomor == 8 -> Suasana.Bubu
    else -> Suasana.Laut
}

/**
 * Adegan memancing di atas soal kuis. Latar tetap memakai ilustrasi final `adegan_*`,
 * sedangkan aksi pancing (joran, tali, pelampung, riak, partikel, ikan) adalah lapisan
 * vektor yang dianimasikan dengan timeline supaya tidak terasa seperti gambar tempel.
 */
@Composable
fun MemancingScene(
    fase: FasePancing,
    modifier: Modifier = Modifier,
    varianBoss: Boolean = false,
    @DrawableRes adegan: Int = adeganStage(1, varianBoss),
    @DrawableRes ikan: Int = Art.ikan_teri,
    poin: Int = 25,
    combo: Int = 0,
    nomorStage: Int = 1,
) {
    val warna = LocalWarna.current
    val density = LocalDensity.current
    val px = density.density
    val kurangiGerak = remember { !ValueAnimator.areAnimatorsEnabled() }
    val suasana = remember(nomorStage, varianBoss) { suasanaStage(nomorStage, varianBoss) }

    val transisi = rememberInfiniteTransition(label = "memancing-ambient")
    val ombak by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7800, easing = LinearEasing)),
        label = "ombak",
    )
    val napas by transisi.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Reverse),
        label = "napas",
    )
    val kilat by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing)),
        label = "kilat",
    )

    // Timeline aksi: 0..1 untuk satu reaksi. Waktu bervariasi per fase.
    val aksi = remember { Animatable(0f) }
    val getar = remember { Animatable(0f) }
    LaunchedEffect(fase, kurangiGerak) {
        aksi.snapTo(0f)
        if (fase != FasePancing.Diam) {
            if (kurangiGerak) {
                aksi.snapTo(1f)
            } else {
                aksi.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = when (fase) {
                            FasePancing.Dapat -> 1500
                            FasePancing.Lepas -> 1150
                            else -> 820
                        },
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
        }
    }
    LaunchedEffect(fase, kurangiGerak) {
        if (fase == FasePancing.Lepas && !kurangiGerak) {
            delay(250)
            getar.snapTo(1f)
            getar.animateTo(0f, tween(560, easing = LinearEasing))
        } else {
            getar.snapTo(0f)
        }
    }

    val a = aksi.value
    val gerak = if (kurangiGerak) 0f else 1f
    val guncang = getar.value * gerak
    val bobPx = sin(ombak * 2f * PI.toFloat()) * 2.4f * px * gerak
    val napasPx = napas * 2.2f * px * gerak

    Box(
        modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .semantics { contentDescription = deskripsiFase(fase) },
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            val ujung = Offset(w * 0.84f, h * 0.06f)
            val target = Offset(w * 0.55f, h * 0.63f)
            val castAwal = Offset(w * 0.90f, h * 0.18f)

            val tLempar = ((a - 0.08f) / 0.55f).coerceIn(0f, 1f)
            val easedLempar = 1f - (1f - tLempar) * (1f - tLempar) * (1f - tLempar)
            val ikanT = ((a - 0.30f) / 0.55f).coerceIn(0f, 1f)
            val fishStart = target + Offset(0f, -6f * px)
            val fishEnd = Offset(w * 0.56f, h * 0.20f)
            val fishX = lerpF(fishStart.x, fishEnd.x, ikanT)
            val fishY = lerpF(fishStart.y, fishEnd.y, ikanT) -
                h * 0.20f * 4f * ikanT * (1f - ikanT) +
                if (ikanT >= 0.98f) sin(a * PI.toFloat() * 6f) * 2.4f * px else 0f
            val fishRot = -18f + 36f * ikanT + sin(a * PI.toFloat() * 7f) * 4f
            val fishScaleY = 1f + 0.10f * sin(a * PI.toFloat() * 10f).toFloat()
            val fishAlpha = (a * 4f).coerceIn(0f, 1f)

            val lepasT = ((a - 0.30f) / 0.70f).coerceIn(0f, 1f)
            val bobber = when (fase) {
                FasePancing.Diam -> Offset(w * 0.885f, h * 0.18f + bobPx)
                FasePancing.Lempar -> {
                    val x = lerpF(castAwal.x, target.x, easedLempar)
                    val y = lerpF(castAwal.y, target.y, easedLempar) -
                        h * 0.22f * 4f * easedLempar * (1f - easedLempar)
                    if (tLempar < 0.999f) Offset(x, y) else Offset(target.x, target.y + bobPx)
                }
                FasePancing.Dapat -> {
                    val seret = ((a - 0.15f) / 0.28f).coerceIn(0f, 1f)
                    Offset(
                        target.x + w * 0.04f * seret,
                        target.y + bobPx + sin(a * PI.toFloat() * 8f) * 2f * px,
                    )
                }
                FasePancing.Lepas -> Offset(
                    target.x + w * 0.30f * lepasT,
                    target.y - h * 0.50f * lepasT + h * 0.40f * lepasT * lepasT + bobPx * (1f - lepasT),
                )
            }
            val bobberAlpha = if (fase == FasePancing.Lepas) (1f - lepasT).coerceIn(0f, 1f) else 1f

            val splashT = when (fase) {
                FasePancing.Lempar -> ((a - 0.48f) / 0.30f).coerceIn(0f, 1f)
                FasePancing.Dapat -> ((a - 0.24f) / 0.30f).coerceIn(0f, 1f)
                FasePancing.Lepas -> ((a - 0.18f) / 0.25f).coerceIn(0f, 1f)
                FasePancing.Diam -> 0f
            }
            val riakT = when (fase) {
                FasePancing.Diam -> (ombak + 0.15f) % 1f
                FasePancing.Lempar -> ((a - 0.52f) / 0.48f).coerceIn(0f, 1f)
                FasePancing.Dapat -> ((a - 0.22f) / 0.35f).coerceIn(0f, 1f)
                FasePancing.Lepas -> ((a - 0.18f) / 0.35f).coerceIn(0f, 1f)
            }
            val kilauT = if (fase == FasePancing.Dapat) ((a - 0.50f) / 0.50f).coerceIn(0f, 1f) else 0f
            val bayanganT = if (fase == FasePancing.Lepas) ((a - 0.38f) / 0.62f).coerceIn(0f, 1f) else 0f

            val punch = if (fase == FasePancing.Dapat) ((a - 0.45f) / 0.35f).coerceIn(0f, 1f) * 0.026f else 0f
            val skalaKamera = 1.055f + punch + if (varianBoss) 0.012f else 0f
            val gx = sin(guncang * PI.toFloat() * 10f) * 5f * guncang * px
            val gy = sin(guncang * PI.toFloat() * 8f + 1f) * 3f * guncang * px

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = skalaKamera
                        scaleY = skalaKamera
                        translationX = gx
                        translationY = napasPx + gy * 0.5f
                    },
            ) {
                Gambar(adegan, null, Modifier.fillMaxSize(), ContentScale.Crop)

                if (varianBoss) {
                    Gambar(Art.hujan_badai, null, Modifier.fillMaxSize().alpha(0.30f), ContentScale.Crop)
                    Gambar(
                        Art.petir,
                        null,
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 22.dp)
                            .size(52.dp)
                            .alpha(if (gerak > 0f && kilat > 0.90f) 1f else 0f),
                    )
                }

                Canvas(Modifier.fillMaxSize()) {
                    val dasarAir = size.height * when (suasana) {
                        Suasana.Es -> 0.60f
                        Suasana.BawahLaut -> 0.52f
                        Suasana.Dermaga -> 0.65f
                        else -> 0.58f
                    }

                    // Ombak vektor: membuat air terlihat bergerak, bukan latar diam.
                    if (suasana != Suasana.Es) {
                        gambarOmbak(
                            warna = warna.ombak,
                            fase = ombak,
                            dasar = dasarAir,
                            amplitudo = size.height * 0.018f,
                            jumlahGaris = if (suasana == Suasana.Badai) 4 else 3,
                        )
                        gambarKilauAir(warna.ombak, ombak, dasarAir + 8.dp.toPx())
                    }
                    if (suasana == Suasana.Es) {
                        gambarOmbak(warna.teksDiAdegan, ombak, dasarAir, size.height * 0.008f, 2)
                        gambarSalju(warna.teksDiAdegan, ombak)
                    }
                    if (suasana == Suasana.BawahLaut) {
                        gambarGelembung(warna.ombak, ombak, dasar = size.height * 0.96f)
                    }
                    if (suasana == Suasana.Bubu) {
                        gambarGelembung(warna.ombak, ombak, dasar = size.height * 0.92f, jumlah = 6)
                    }

                    // Riak: diam kecil di sekitar pelampung, lalu membesar saat splash.
                    if (fase != FasePancing.Diam) {
                        gambarRiak(target, riakT, if (fase == FasePancing.Lepas) warna.bahayaTeks else warna.ombak)
                    }

                    val ujungTali = if (fase == FasePancing.Dapat && ikanT > 0.04f) {
                        Offset(fishX, fishY + 8f * px)
                    } else {
                        bobber
                    }
                    val kendur = when (fase) {
                        FasePancing.Diam -> 9.dp.toPx()
                        FasePancing.Lempar -> (1f - easedLempar) * 22.dp.toPx() + sin(ombak * PI.toFloat() * 2f) * 2.dp.toPx()
                        FasePancing.Dapat -> lerpF(2.dp.toPx(), 0f, ikanT)
                        FasePancing.Lepas -> if (a < 0.30f) 2.dp.toPx() else 0f
                    }
                    if (fase == FasePancing.Lepas && a > 0.30f) {
                        gambarTaliPutus(ujung, bobber, warna.teksDiAdegan, lepasT)
                    } else {
                        gambarTali(ujung, ujungTali, kendur, warna.teksDiAdegan)
                    }

                    gambarJoran(
                        ujung = ujung,
                        tekuk = when {
                            fase == FasePancing.Dapat -> 0.7f + ikanT * 0.5f
                            fase == FasePancing.Lepas && a < 0.35f -> 1f
                            fase == FasePancing.Lempar && a < 0.22f -> -0.8f
                            else -> 0f
                        },
                        warnaKayu = Color(0xFF4A2B1B),
                        warnaPegangan = warna.peringatanTeks,
                    )

                    if (splashT in 0.002f..0.998f) {
                        gambarPercikan(target, splashT, Color.White, jumlah = 16)
                    }
                    if (kilauT in 0.002f..0.998f) {
                        gambarKilau(Offset(fishX, fishY), kilauT, warna.suksesTeks, jumlah = 12)
                    }
                    if (bayanganT in 0.002f..0.998f) {
                        gambarBayanganIkan(target, bayanganT, warna.bahayaTeks)
                    }
                    if (varianBoss) {
                        gambarHujan(warna.teksDiAdegan, ombak)
                    }
                    if (varianBoss && !kurangiGerak && kilat > 0.92f) {
                        drawRect(Color.White.copy(alpha = 0.16f))
                    }
                }

                // Pelampung vektor, bukan ikon tempel: ada kemiringan dan skala saat bergerak.
                if (bobberAlpha > 0.01f) {
                    Canvas(Modifier.fillMaxSize().alpha(bobberAlpha)) {
                        gambarPelampung(
                            pusat = bobber,
                            skala = 1f + 0.10f * sin(a * PI.toFloat() * 10f).toFloat(),
                            rotasi = sin(ombak * PI.toFloat() * 2f + a * 6f) * 8f,
                            warnaBadan = warna.bahayaTeks,
                            warnaPita = Color.White,
                        )
                    }
                }

                // Splash memakai aset final, tetapi diberi rotasi, skala, dan partikel vektor.
                if (splashT in 0.002f..0.998f) {
                    val ukuranSplash = (68f + 58f * splashT).dp
                    Gambar(
                        Art.percikan_air,
                        null,
                        Modifier
                            .offset {
                                IntOffset(
                                    (target.x - ukuranSplash.toPx() / 2f).roundToInt(),
                                    (target.y - ukuranSplash.toPx() / 2f).roundToInt(),
                                )
                            }
                            .size(ukuranSplash)
                            .alpha((1f - splashT).coerceIn(0f, 1f))
                            .graphicsLayer { rotationZ = -10f + splashT * 20f },
                    )
                }

                if (fase == FasePancing.Lepas && a > 0.18f) {
                    val ledakT = ((a - 0.18f) / 0.42f).coerceIn(0f, 1f)
                    val ukuranLedak = (68f + 42f * ledakT).dp
                    Gambar(
                        Art.ledakan_kail_putus,
                        null,
                        Modifier
                            .offset {
                                IntOffset(
                                    (target.x - ukuranLedak.toPx() / 2f).roundToInt(),
                                    (target.y - ukuranLedak.toPx() / 2f).roundToInt(),
                                )
                            }
                            .size(ukuranLedak)
                            .alpha((1f - ledakT * 0.55f).coerceIn(0f, 1f))
                            .graphicsLayer { rotationZ = 8f + ledakT * 18f },
                    )
                    val kailT = ((a - 0.30f) / 0.70f).coerceIn(0f, 1f)
                    Gambar(
                        Art.kail_putus,
                        "Kail putus",
                        Modifier
                            .offset {
                                IntOffset(
                                    (target.x + w * 0.08f * kailT - 24.dp.toPx()).roundToInt(),
                                    (target.y - h * 0.28f * kailT - 24.dp.toPx()).roundToInt(),
                                )
                            }
                            .size(48.dp)
                            .alpha((1f - 0.45f * kailT).coerceIn(0f, 1f))
                            .graphicsLayer {
                                rotationZ = -18f + 52f * kailT
                                scaleX = 1f - 0.16f * kailT
                                scaleY = 1f - 0.16f * kailT
                            },
                    )
                }

                if (fase == FasePancing.Dapat && ikanT > 0.01f) {
                    Gambar(
                        ikan,
                        "Ikan tertangkap",
                        Modifier
                            .offset {
                                IntOffset(
                                    (fishX - 42.dp.toPx()).roundToInt(),
                                    (fishY - 42.dp.toPx()).roundToInt(),
                                )
                            }
                            .size(84.dp)
                            .graphicsLayer {
                                alpha = fishAlpha
                                rotationZ = fishRot
                                scaleY = fishScaleY
                            },
                    )
                }
            }

            // Scrim tipis supaya chip dan efek tetap terbaca di atas adegan yang ramai.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.22f),
                            0.28f to Color.Transparent,
                            0.74f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.30f),
                        ),
                    ),
            )

            if (fase == FasePancing.Dapat) {
                val muncul = ((a - 0.52f) / 0.26f).coerceIn(0f, 1f)
                Row(
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 11.dp)
                        .graphicsLayer {
                            alpha = muncul
                            val s = 0.72f + 0.28f * muncul
                            scaleX = s
                            scaleY = s
                        }
                        .background(warna.suksesLatar.copy(alpha = 0.94f), RoundedCornerShape(50))
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Gambar(Art.lencana_plus_satu, null, Modifier.size(22.dp))
                    Text(
                        "+$poin XP",
                        color = warna.suksesTeks,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (combo > 1) {
                        Text(
                            "x$combo",
                            color = warna.suksesTeks.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (fase == FasePancing.Lepas) {
                val muncul = ((a - 0.42f) / 0.28f).coerceIn(0f, 1f)
                Text(
                    "Salah",
                    color = warna.bahayaTeks,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 13.dp)
                        .graphicsLayer {
                            alpha = muncul
                            val s = 0.80f + 0.20f * muncul
                            scaleX = s
                            scaleY = s
                        }
                        .background(warna.bahayaLatar.copy(alpha = 0.94f), RoundedCornerShape(50))
                        .padding(horizontal = 11.dp, vertical = 3.dp),
                )
            }
        }
    }
}

private fun lerpF(mulai: Float, selesai: Float, t: Float): Float = mulai + (selesai - mulai) * t

private fun deskripsiFase(fase: FasePancing): String = when (fase) {
    FasePancing.Diam -> "Nelayan menunggu ikan menggigit umpan"
    FasePancing.Lempar -> "Kail dilempar ke laut dan pelampung mengapung"
    FasePancing.Dapat -> "Ikan berhasil diangkat ke perahu"
    FasePancing.Lepas -> "Tali pancing putus dan ikan kabur"
}
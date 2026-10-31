package com.example.codefishing.frontend.components

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.core.art.Art

/**
 * Fase animasi nelayan pada layar Kuis Mode Belajar.
 * - [Diam]  : soal baru, adegan tenang.
 * - [Lempar]: pemain memilih opsi, kail dilempar (belum dikunci).
 * - [Dapat] : jawaban benar dikunci, ikan terangkat.
 * - [Lepas] : jawaban salah dikunci, tali putus.
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

/**
 * Adegan memancing di atas soal kuis. Memakai ilustrasi final dari folder aset
 * (adegan_*, percikan_air, ledakan_kail_putus, kail_putus, lencana_plus_satu, dan
 * awan/hujan/petir untuk Boss), bukan lagi gambar buatan kode.
 */
@Composable
fun MemancingScene(
    fase: FasePancing,
    modifier: Modifier = Modifier,
    varianBoss: Boolean = false,
    @DrawableRes adegan: Int = adeganStage(1, varianBoss),
    @DrawableRes ikan: Int = Art.ikan_teri,
) {
    val context = LocalContext.current
    val kurangiGerak = remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f) == 0f
    }

    val transisi = rememberInfiniteTransition(label = "memancing")
    val bob by transisi.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob",
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
    val a = aksi.value

    Box(
        modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .semantics { contentDescription = deskripsiFase(fase) },
        contentAlignment = Alignment.Center,
    ) {
        // Latar adegan; sedikit diperbesar dan naik-turun pelan seperti diayun ombak.
        Gambar(
            adegan,
            null,
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = bob * gerak * 3.dp.toPx()
                    scaleX = 1.08f
                    scaleY = 1.08f
                },
            ContentScale.Crop,
        )

        if (varianBoss) {
            Gambar(Art.hujan_badai, null, Modifier.fillMaxSize().alpha(0.35f), ContentScale.Crop)
            Gambar(
                Art.petir,
                null,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 24.dp)
                    .size(56.dp)
                    .alpha(if (gerak > 0f && kilat > 0.9f) 1f else 0f),
            )
        }

        when (fase) {
            FasePancing.Diam -> Unit
            FasePancing.Lempar -> Gambar(
                Art.percikan_air,
                null,
                Modifier
                    .offset(y = 34.dp)
                    .size((48f + 44f * a).dp)
                    .alpha(1f - 0.5f * a),
            )
            FasePancing.Dapat -> {
                Gambar(
                    ikan,
                    "Ikan tertangkap",
                    Modifier
                        .offset(y = (50f - 80f * a).dp)
                        .size(96.dp)
                        .alpha((a * 3f).coerceAtMost(1f)),
                )
                Row(
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .alpha(a),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Gambar(Art.lencana_plus_satu, null, Modifier.size(28.dp))
                    Text("+25 XP", color = Color(0xFF8BF0C7), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            FasePancing.Lepas -> {
                Gambar(
                    Art.ledakan_kail_putus,
                    null,
                    Modifier
                        .offset(y = 10.dp)
                        .size((70f + 46f * a).dp)
                        .alpha(1f - 0.4f * a),
                )
                Gambar(
                    Art.kail_putus,
                    "Kail putus",
                    Modifier
                        .offset(y = (6f + 54f * a).dp)
                        .size(56.dp)
                        .alpha(1f - 0.7f * a),
                )
                Text(
                    "Umpan \u22121",
                    color = Color(0xFFFFB3B3),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 14.dp)
                        .alpha(a),
                )
            }
        }
    }
}

private fun deskripsiFase(fase: FasePancing): String = when (fase) {
    FasePancing.Diam -> "Nelayan menunggu ikan menggigit umpan"
    FasePancing.Lempar -> "Kail dilempar ke laut"
    FasePancing.Dapat -> "Ikan berhasil diangkat ke perahu"
    FasePancing.Lepas -> "Tali pancing putus dan ikan kabur"
}

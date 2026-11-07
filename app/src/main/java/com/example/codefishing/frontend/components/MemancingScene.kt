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
 * Fase animasi adegan pada layar Kuis Mode Belajar.
 * - [Diam]  : soal baru, adegan tenang dan aktor menunggu.
 * - [Lempar]: pemain memilih opsi; aksi dimulai (lempar jala/kail, tembak tombak,
 *             turunkan tali es, tarik bubu, atau tebar pukat).
 * - [Dapat] : jawaban benar dikunci, ikan ditarik lalu diangkat.
 * - [Lepas] : jawaban salah dikunci, aksi gagal dan ikan kabur.
 */
enum class FasePancing { Diam, Lempar, Dapat, Lepas }

private fun durasiAksi(fase: FasePancing, jenis: JenisAdegan): Int = when (fase) {
    FasePancing.Dapat -> 1500
    FasePancing.Lepas -> 1150
    FasePancing.Diam -> 0
    FasePancing.Lempar -> durasiLemparAdegan(jenis)
}

/**
 * Adegan kuis di atas soal. Latar memakai ilustrasi final `adegan_*`, sedangkan aksi
 * digambar sebagai lapisan vektor yang berangkat dari aktor di dalam ilustrasi:
 * joran di tangan nelayan, jala dari penjala, tombak dari penyelam, tali dari lubang es,
 * tali bubu, atau pukat dari kapal.
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
    val jenis = remember(nomorStage, varianBoss) { jenisAdeganStage(nomorStage, varianBoss) }
    val titik = remember(jenis) { titikAdegan(jenis) }

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

    val aksi = remember { Animatable(0f) }
    val getar = remember { Animatable(0f) }
    LaunchedEffect(fase, kurangiGerak, jenis) {
        aksi.snapTo(0f)
        if (fase != FasePancing.Diam) {
            if (kurangiGerak) {
                aksi.snapTo(1f)
            } else {
                aksi.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durasiAksi(fase, jenis), easing = FastOutSlowInEasing),
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
            .semantics { contentDescription = deskripsiFase(jenis, fase) },
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            // Adegan Bubu menaruh bubu di dasar ilustrasi; ratakan gambar ke bawah
            // agar bubu tetap masuk kotak saat rasio layar memotong bagian atas/bawah.
            val ratakanBawah = jenis == JenisAdegan.Bubu
            val pangkal = titikGambar(titik.pangkal.x, titik.pangkal.y, w, h, ratakanBawah)
            val ujung = titikGambar(titik.ujung.x, titik.ujung.y, w, h, ratakanBawah)
            val istirahat = titikGambar(titik.istirahat.x, titik.istirahat.y, w, h, ratakanBawah)
            val target = titikGambar(titik.target.x, titik.target.y, w, h, ratakanBawah)
            val ikanTujuan = titikGambar(titik.ikanTujuan.x, titik.ikanTujuan.y, w, h, ratakanBawah)
            val dasarAir = if (titik.dasarAir > 0f) {
                titikGambar(0f, titik.dasarAir, w, h, ratakanBawah).y
            } else {
                h * 0.62f
            }

            val tLempar = ((a - 0.08f) / 0.55f).coerceIn(0f, 1f)
            val easedLempar = 1f - (1f - tLempar) * (1f - tLempar) * (1f - tLempar)
            val tIkan = ((a - 0.30f) / 0.55f).coerceIn(0f, 1f)
            val tLepas = ((a - 0.22f) / 0.72f).coerceIn(0f, 1f)

            val busurIkan = if (diBawahAir(jenis) || jenis == JenisAdegan.LubangEs) {
                h * 0.05f
            } else {
                h * 0.20f
            }
            val fishStart = target + Offset(0f, -6f * px)
            val fishX = lerpF(fishStart.x, ikanTujuan.x, tIkan)
            val fishY = lerpF(fishStart.y, ikanTujuan.y, tIkan) -
                busurIkan * 4f * tIkan * (1f - tIkan) +
                if (tIkan >= 0.98f) sin(a * PI.toFloat() * 6f) * 2.4f * px else 0f
            val fishRot = -18f + 36f * tIkan + sin(a * PI.toFloat() * 7f) * 4f
            val fishScaleY = 1f + 0.10f * sin(a * PI.toFloat() * 10f).toFloat()
            val fishAlpha = ((a - 0.26f) / 0.22f).coerceIn(0f, 1f)

            val kaburT = ((a - 0.18f) / 0.70f).coerceIn(0f, 1f)
            val kabur = target + Offset(
                w * 0.28f * kaburT,
                h * 0.10f * kaburT + sin(a * PI.toFloat() * 4f) * 3f * px,
            )

            // --- Aksi joran untuk kapal dan dermaga --------------------------------
            val memakaiRod = memakaiJoran(jenis)
            val bobber: Offset? = when {
                !memakaiRod -> null
                // Saat tenang tali sudah terpasang dan pelampung mengapung.
                fase == FasePancing.Diam -> istirahat
                fase == FasePancing.Lempar -> {
                    // Lempar dimulai dari posisi istirahat, bukan dari ujung joran,
                    // supaya kail terlihat terangkat lalu melesat ke target.
                    val x = lerpF(istirahat.x, target.x, easedLempar)
                    val y = lerpF(istirahat.y, target.y, easedLempar) -
                        h * 0.18f * 4f * easedLempar * (1f - easedLempar)
                    if (tLempar >= 0.999f) Offset(target.x, target.y + bobPx) else Offset(x, y)
                }
                fase == FasePancing.Dapat -> Offset(
                    target.x,
                    target.y + bobPx + sin(a * PI.toFloat() * 8f) * 2f * px,
                )
                else -> Offset(
                    target.x + w * 0.28f * tLepas,
                    target.y - h * 0.45f * tLepas + h * 0.38f * tLepas * tLepas +
                        bobPx * (1f - tLepas),
                )
            }
            val bobberAlpha = when {
                !memakaiRod -> 0f
                fase == FasePancing.Lepas -> (1f - tLepas).coerceIn(0f, 1f)
                else -> 1f
            }

            // --- Aksi jala/pukat untuk perahu cadik dan boss -----------------------
            val memakaiNet = memakaiJala(jenis)
            val netT = when (fase) {
                FasePancing.Lempar -> easedLempar
                FasePancing.Dapat, FasePancing.Lepas -> 1f
                FasePancing.Diam -> 0f
            }
            val netPos: Offset? = when {
                !memakaiNet || fase == FasePancing.Diam -> null
                fase == FasePancing.Lempar -> Offset(
                    lerpF(pangkal.x, target.x, easedLempar),
                    lerpF(pangkal.y, target.y, easedLempar) -
                        h * 0.20f * 4f * easedLempar * (1f - easedLempar),
                )
                else -> target
            }
            val netAlpha = when {
                !memakaiNet || fase == FasePancing.Diam -> 0f
                fase == FasePancing.Lepas -> (1f - tLepas).coerceIn(0f, 1f)
                fase == FasePancing.Dapat -> (1f - 0.20f * tIkan).coerceIn(0f, 1f)
                else -> 1f
            }
            val netScale = 0.55f + 0.55f * netT
            val netRot = -16f + 32f * netT
            val netRes = if (jenis == JenisAdegan.Badai) Art.alat_jaring_pukat else Art.alat_jala_lempar

            // --- Aksi tombak penyelam ---------------------------------------------
            // Ikan diberi waktu berenang masuk dulu; tombak baru meluncur setelahnya.
            val tembakT = if (fase == FasePancing.Lempar) {
                ((a - 0.52f) / 0.30f).coerceIn(0f, 1f)
            } else if (fase == FasePancing.Dapat) {
                1f
            } else {
                0f
            }
            val tombakUjung = when (fase) {
                // Meleset: tombak meluncur melewati ikan lalu memudar.
                FasePancing.Lepas -> target + (target - pangkal) * (0.55f * tLepas)
                // Berhasil: tombak ikut ditarik bersama ikan ke arah penyelam,
                // jadi tidak ada tombak menggantung di air setelah ikan diangkat.
                FasePancing.Dapat -> if (tIkan > 0.04f) Offset(fishX, fishY) else target
                else -> target
            }

            // --- Aksi tali lubang es ----------------------------------------------
            val dropT = if (fase == FasePancing.Lempar) {
                ((a - 0.05f) / 0.35f).coerceIn(0f, 1f)
            } else {
                1f
            }
            val jig = if (fase == FasePancing.Lempar && dropT >= 1f && !kurangiGerak) {
                sin((a - 0.40f) * PI.toFloat() * 8f) * 3f * px
            } else {
                0f
            }
            val ujungTaliEs = when (fase) {
                FasePancing.Diam -> target
                FasePancing.Lempar -> Offset(
                    lerpF(ujung.x, target.x, dropT),
                    lerpF(ujung.y, target.y, dropT) + jig,
                )
                FasePancing.Dapat -> if (tIkan > 0.04f) Offset(fishX, fishY) else target
                FasePancing.Lepas -> target
            }

            // --- Aksi tali bubu ----------------------------------------------------
            val kendurBubu = when (fase) {
                FasePancing.Diam -> 14f * px
                FasePancing.Lempar -> lerpF(
                    14f * px,
                    2f * px,
                    (a / 0.60f).coerceIn(0f, 1f),
                )
                FasePancing.Dapat -> 1f * px
                FasePancing.Lepas -> lerpF(2f * px, 16f * px, tLepas)
            }

            val splashT = when (jenis) {
                JenisAdegan.Selam, JenisAdegan.Bubu -> 0f
                JenisAdegan.LubangEs -> when (fase) {
                    FasePancing.Lempar -> ((a - 0.36f) / 0.22f).coerceIn(0f, 1f)
                    FasePancing.Dapat -> ((a - 0.30f) / 0.30f).coerceIn(0f, 1f)
                    FasePancing.Lepas -> ((a - 0.18f) / 0.25f).coerceIn(0f, 1f)
                    FasePancing.Diam -> 0f
                }
                else -> when (fase) {
                    FasePancing.Lempar -> ((a - 0.48f) / 0.30f).coerceIn(0f, 1f)
                    FasePancing.Dapat -> ((a - 0.24f) / 0.30f).coerceIn(0f, 1f)
                    FasePancing.Lepas -> ((a - 0.18f) / 0.25f).coerceIn(0f, 1f)
                    FasePancing.Diam -> 0f
                }
            }
            val riakT = when (fase) {
                FasePancing.Lempar -> ((a - 0.52f) / 0.48f).coerceIn(0f, 1f)
                FasePancing.Dapat -> ((a - 0.22f) / 0.35f).coerceIn(0f, 1f)
                FasePancing.Lepas -> ((a - 0.18f) / 0.35f).coerceIn(0f, 1f)
                FasePancing.Diam -> 0f
            }
            val kilauT = if (fase == FasePancing.Dapat) {
                ((a - 0.50f) / 0.50f).coerceIn(0f, 1f)
            } else {
                0f
            }
            val bayanganT = if (fase == FasePancing.Lepas) {
                ((a - 0.38f) / 0.62f).coerceIn(0f, 1f)
            } else {
                0f
            }

            // Ikan tidak pernah muncul sebelum aksinya bergerak. Pada scene selam,
            // ikan berenang masuk dari kanan setelah penyelam membidik.
            val ikanMasukT = if (jenis == JenisAdegan.Selam && fase == FasePancing.Lempar) {
                ((a - 0.16f) / 0.36f).coerceIn(0f, 1f)
            } else {
                0f
            }
            val ikanMasukAwal = target + Offset(w * 0.34f, h * 0.035f)
            val bayanganIkanT = if (
                (jenis == JenisAdegan.LubangEs || jenis == JenisAdegan.Bubu) &&
                fase == FasePancing.Lempar
            ) {
                ((a - 0.52f) / 0.40f).coerceIn(0f, 1f)
            } else {
                0f
            }
            val posisiIkan = when (fase) {
                FasePancing.Dapat -> Offset(fishX, fishY)
                FasePancing.Lepas -> if (
                    jenis == JenisAdegan.Selam ||
                    jenis == JenisAdegan.Bubu ||
                    jenis == JenisAdegan.LubangEs
                ) {
                    kabur
                } else {
                    target
                }
                FasePancing.Lempar -> if (jenis == JenisAdegan.Selam) {
                    Offset(
                        lerpF(ikanMasukAwal.x, target.x, ikanMasukT),
                        lerpF(ikanMasukAwal.y, target.y, ikanMasukT) +
                            sin(a * PI.toFloat() * 6f) * 2.5f * px * gerak,
                    )
                } else {
                    target
                }
                FasePancing.Diam -> target
            }
            val alphaIkanAdegan = when (fase) {
                FasePancing.Dapat -> if (jenis == JenisAdegan.Selam) 1f else fishAlpha
                FasePancing.Lepas -> if (
                    jenis == JenisAdegan.Selam ||
                    jenis == JenisAdegan.Bubu ||
                    jenis == JenisAdegan.LubangEs
                ) {
                    (1f - tLepas).coerceIn(0f, 1f)
                } else {
                    0f
                }
                FasePancing.Lempar -> if (jenis == JenisAdegan.Selam) ikanMasukT else 0f
                FasePancing.Diam -> 0f
            }
            val rotasiIkan = when (fase) {
                FasePancing.Dapat -> fishRot
                FasePancing.Lepas -> -8f + 24f * tLepas
                FasePancing.Lempar -> if (jenis == JenisAdegan.Selam) {
                    sin(a * PI.toFloat() * 5f) * 4f * gerak
                } else {
                    0f
                }
                FasePancing.Diam -> 0f
            }
            // Ikan menghilang cepat setelah selesai ditarik ke tepi/aktor.
            val ikanMenghilangT = ((tIkan - 0.82f) / 0.16f).coerceIn(0f, 1f)
            val alphaIkanTampil = if (fase == FasePancing.Dapat) {
                alphaIkanAdegan * (1f - ikanMenghilangT)
            } else {
                alphaIkanAdegan
            }
            val skalaYIkan = if (fase == FasePancing.Dapat) {
                fishScaleY
            } else {
                1f + 0.05f * sin(a * PI.toFloat() * 10f).toFloat() * gerak
            }

            val punch = if (fase == FasePancing.Dapat) {
                ((a - 0.45f) / 0.35f).coerceIn(0f, 1f) * 0.026f
            } else {
                0f
            }
            val skalaKamera = 1.055f + punch + if (varianBoss) 0.012f else 0f
            val gx = sin(guncang * PI.toFloat() * 10f) * 5f * guncang * px
            val gy = sin(guncang * PI.toFloat() * 8f + 1f) * 3f * guncang * px
            val tembakGetar = if (
                jenis == JenisAdegan.Selam &&
                fase == FasePancing.Lempar &&
                a > 0.52f && a < 0.74f &&
                !kurangiGerak
            ) {
                sin(a * PI.toFloat() * 52f) * 1.4f * px
            } else {
                0f
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = skalaKamera
                        scaleY = skalaKamera
                        translationX = gx + tembakGetar
                        translationY = napasPx + gy * 0.5f
                    },
            ) {
                Gambar(
                    adegan,
                    null,
                    Modifier.fillMaxSize(),
                    ContentScale.Crop,
                    perataan = if (ratakanBawah) Alignment.BottomCenter else Alignment.Center,
                )

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
                    // --- Ambient sesuai jenis adegan ------------------------------
                    if (jenis == JenisAdegan.LubangEs) {
                        gambarOmbak(warna.teksDiAdegan, ombak, h * 0.70f, h * 0.008f, 2)
                        gambarSalju(warna.teksDiAdegan, ombak)
                    } else if (diBawahAir(jenis)) {
                        gambarGelembung(warna.ombak, ombak, dasar = h * 0.96f)
                    } else {
                        gambarOmbak(
                            warna = warna.ombak,
                            fase = ombak,
                            dasar = dasarAir,
                            amplitudo = h * 0.018f,
                            jumlahGaris = if (jenis == JenisAdegan.Badai) 4 else 3,
                        )
                        gambarKilauAir(warna.ombak, ombak, dasarAir + 8.dp.toPx())
                    }

                    // --- Aksi per adegan ------------------------------------------
                    when {
                        memakaiRod -> {
                            val ujungTali = if (fase == FasePancing.Dapat && tIkan > 0.04f) {
                                Offset(fishX, fishY + 8f * px)
                            } else {
                                bobber ?: ujung
                            }
                            // Joran tetap milik ilustrasi. Saat ikan tertarik, garis
                            // dibuat sedikit melengkung tanpa menggambar joran kedua.
                            val asalTali = if (fase == FasePancing.Dapat) {
                                Offset(
                                    ujung.x + (target.x - ujung.x) * 0.035f * tIkan,
                                    ujung.y + (target.y - ujung.y) * 0.035f * tIkan,
                                )
                            } else {
                                ujung
                            }
                            val kendur = when (fase) {
                                FasePancing.Diam -> 0f
                                FasePancing.Lempar -> (1f - easedLempar) * 20f * px +
                                    sin(ombak * PI.toFloat() * 2f) * 2f * px
                                FasePancing.Dapat -> lerpF(2f * px, 0f, tIkan)
                                FasePancing.Lepas -> if (a < 0.30f) 2f * px else 0f
                            }
                            if (fase == FasePancing.Lepas && a > 0.30f) {
                                gambarTaliPutus(asalTali, ujungTali, warna.teksDiAdegan, tLepas)
                            } else {
                                gambarTali(asalTali, ujungTali, kendur, warna.teksDiAdegan)
                            }
                        }

                        memakaiNet -> {
                            if (fase == FasePancing.Lempar && netPos != null) {
                                gambarTali(
                                    mulai = pangkal,
                                    selesai = netPos,
                                    kendur = 4.dp.toPx(),
                                    warna = warna.teksDiAdegan,
                                    lebar = 1.2f,
                                )
                            }
                        }

                        jenis == JenisAdegan.Selam -> {
                            if (fase != FasePancing.Diam) {
                                val alphaTombak = if (fase == FasePancing.Lepas) {
                                    (1f - tLepas).coerceIn(0f, 1f)
                                } else {
                                    1f
                                }
                                // Penyelam membidik dulu, ikan berenang masuk,
                                // lalu tombak meluncur setelah ikan mendekat.
                                if (fase == FasePancing.Lempar) {
                                    val bidikT = ((a - 0.06f) / 0.20f).coerceIn(0f, 1f)
                                    if (bidikT > 0.01f && ikanMasukT > 0.01f) {
                                        gambarGarisBidik(
                                            mulai = pangkal,
                                            selesai = posisiIkan,
                                            alpha = 0.26f * bidikT * (1f - tembakT),
                                            warna = warna.ombak,
                                        )
                                    }
                                }
                                val mundur = if (
                                    fase == FasePancing.Lempar &&
                                    a in 0.52f..0.70f
                                ) {
                                    3.5f * px * gerak
                                } else {
                                    0f
                                }
                                val mulaiTombak = Offset(pangkal.x - mundur, pangkal.y)
                                gambarTombak(
                                    mulai = mulaiTombak,
                                    selesai = tombakUjung,
                                    progres = if (fase == FasePancing.Lempar) tembakT else 1f,
                                    warna = warna.teksDiAdegan,
                                    alpha = alphaTombak,
                                )
                                if (tembakT > 0.02f || fase == FasePancing.Lepas) {
                                    gambarGelembungJejak(
                                        mulai = mulaiTombak,
                                        selesai = tombakUjung,
                                        progres = if (fase == FasePancing.Lepas) tLepas else tembakT,
                                        warna = warna.ombak,
                                    )
                                }
                            }
                        }

                        jenis == JenisAdegan.LubangEs -> {
                            if (fase == FasePancing.Lepas && a > 0.30f) {
                                gambarTaliPutus(ujung, target, warna.teksDiAdegan, tLepas)
                            } else {
                                gambarTali(
                                    mulai = ujung,
                                    selesai = ujungTaliEs,
                                    kendur = 0f,
                                    warna = warna.teksDiAdegan,
                                )
                            }
                        }

                        jenis == JenisAdegan.Bubu -> {
                            gambarTali(
                                mulai = pangkal,
                                selesai = target,
                                kendur = kendurBubu,
                                warna = warna.teksDiAdegan,
                                lebar = 1.3f,
                            )
                        }
                    }

                    if (!diBawahAir(jenis) && fase != FasePancing.Diam && riakT > 0f) {
                        gambarRiak(
                            pusat = target,
                            progres = riakT,
                            warna = if (fase == FasePancing.Lepas) warna.bahayaTeks else warna.ombak,
                        )
                    }

                    if (splashT in 0.002f..0.998f) {
                        if (jenis == JenisAdegan.LubangEs) {
                            gambarSerpihanEs(target, splashT, Color.White)
                        } else {
                            gambarPercikan(target, splashT, Color.White, jumlah = 16)
                        }
                    }
                    if (kilauT in 0.002f..0.998f) {
                        gambarKilau(Offset(fishX, fishY), kilauT, warna.suksesTeks, jumlah = 12)
                    }
                    if (bayanganIkanT > 0.02f) {
                        gambarBayanganIkan(
                            pusat = target,
                            progres = 0.20f,
                            warna = warna.bahayaTeks,
                            intensitas = bayanganIkanT,
                        )
                    }
                    if (
                        fase == FasePancing.Lepas &&
                        bayanganT in 0.002f..0.998f &&
                        !diBawahAir(jenis) &&
                        jenis != JenisAdegan.LubangEs
                    ) {
                        gambarBayanganIkan(target, bayanganT, warna.bahayaTeks)
                    }
                    if (varianBoss) {
                        gambarHujan(warna.teksDiAdegan, ombak)
                    }
                    if (varianBoss && !kurangiGerak && kilat > 0.92f) {
                        drawRect(Color.White.copy(alpha = 0.16f))
                    }
                }

                // --- Pelampung: hanya untuk joran dan tali es --------------------
                val pelampung = when {
                    memakaiRod -> bobber.takeIf { bobberAlpha > 0.01f }
                    jenis == JenisAdegan.LubangEs && (fase == FasePancing.Diam || fase == FasePancing.Lempar) -> ujungTaliEs
                    else -> null
                }
                if (pelampung != null) {
                    Canvas(Modifier.fillMaxSize()) {
                        gambarPelampung(
                            pusat = pelampung,
                            skala = if (jenis == JenisAdegan.LubangEs) 0.68f else
                                1f + 0.10f * sin(a * PI.toFloat() * 10f).toFloat() * gerak,
                            rotasi = sin(ombak * PI.toFloat() * 2f + a * 6f) * 8f * gerak,
                            warnaBadan = warna.bahayaTeks,
                            warnaPita = Color.White,
                        )
                    }
                }

                // --- Jala/pukat sebagai aset yang membuka saat dilempar ----------
                if (netPos != null && netAlpha > 0.01f) {
                    val ukuranDasar = if (jenis == JenisAdegan.Badai) 118f else 88f
                    Gambar(
                        netRes,
                        if (jenis == JenisAdegan.Badai) "Pukat" else "Jala",
                        Modifier
                            .offset {
                                IntOffset(
                                    (netPos.x - ukuranDasar.dp.toPx() / 2f).roundToInt(),
                                    (netPos.y - ukuranDasar.dp.toPx() / 2f).roundToInt(),
                                )
                            }
                            .size(ukuranDasar.dp)
                            .graphicsLayer {
                                alpha = netAlpha
                                rotationZ = netRot
                                scaleX = netScale
                                scaleY = netScale
                            },
                    )
                }

                // --- Splash permukaan --------------------------------------------
                if (splashT in 0.002f..0.998f && jenis != JenisAdegan.LubangEs) {
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

                // --- Efek gagal khusus joran: kail putus -------------------------
                if (memakaiRod && fase == FasePancing.Lepas && a > 0.18f) {
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

                // --- Ikan target -------------------------------------------------
                if (alphaIkanTampil > 0.01f) {
                    val ukuranIkan = if (diBawahAir(jenis)) 76.dp else 84.dp
                    Gambar(
                        ikan,
                        if (fase == FasePancing.Dapat) "Ikan tertangkap" else "Ikan",
                        Modifier
                            .offset {
                                IntOffset(
                                    (posisiIkan.x - ukuranIkan.toPx() / 2f).roundToInt(),
                                    (posisiIkan.y - ukuranIkan.toPx() / 2f).roundToInt(),
                                )
                            }
                            .size(ukuranIkan)
                            .graphicsLayer {
                                alpha = alphaIkanTampil
                                rotationZ = rotasiIkan
                                scaleY = skalaYIkan
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

private fun deskripsiFase(jenis: JenisAdegan, fase: FasePancing): String = when (fase) {
    FasePancing.Diam -> when (jenis) {
        JenisAdegan.Kapal -> "Nelayan di kapal menunggu ikan menggigit umpan"
        JenisAdegan.Dermaga -> "Nelayan di dermaga bersiap melempar kail"
        JenisAdegan.PerahuCadik -> "Penjala di perahu cadik bersiap melempar jala"
        JenisAdegan.Selam -> "Penyelam mengintai ikan di bawah laut"
        JenisAdegan.LubangEs -> "Nelayan es menunggu di tepi lubang"
        JenisAdegan.Bubu -> "Bubu terpasang di dasar laut"
        JenisAdegan.Badai -> "Kapal pukat bertahan di tengah badai"
    }
    FasePancing.Lempar -> when (jenis) {
        JenisAdegan.Selam -> "Penyelam menembakkan tombak ke arah ikan"
        JenisAdegan.LubangEs -> "Tali pancing turun ke lubang es"
        JenisAdegan.Bubu -> "Tali bubu ditarik dari dasar laut"
        JenisAdegan.PerahuCadik -> "Jala dilempar ke laut"
        JenisAdegan.Badai -> "Pukat ditebar dari kapal"
        else -> "Kail dilempar ke laut dan pelampung mengapung"
    }
    FasePancing.Dapat -> when (jenis) {
        JenisAdegan.Selam -> "Tombak mengenai ikan dan ikan berhasil dibawa"
        JenisAdegan.LubangEs -> "Ikan berhasil diangkat dari lubang es"
        JenisAdegan.Bubu -> "Bubu terangkat dan ikan berhasil ditangkap"
        JenisAdegan.PerahuCadik -> "Jala menutup dan ikan berhasil diangkat"
        JenisAdegan.Badai -> "Pukat penuh ikan berhasil diangkat"
        else -> "Ikan berhasil diangkat ke perahu"
    }
    FasePancing.Lepas -> when (jenis) {
        JenisAdegan.Selam -> "Tombak meleset dan ikan kabur ke kedalaman"
        JenisAdegan.LubangEs -> "Tali lepas dan ikan kabur di bawah es"
        JenisAdegan.Bubu -> "Bubu miring dan ikan berhasil kabur"
        JenisAdegan.PerahuCadik -> "Jala kosong dan ikan kabur"
        JenisAdegan.Badai -> "Pukat lepas dan ikan kabur"
        else -> "Tali pancing putus dan ikan kabur"
    }
}
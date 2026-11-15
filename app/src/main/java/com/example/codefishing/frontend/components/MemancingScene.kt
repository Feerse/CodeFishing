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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.example.codefishing.core.data.LocalEfekVisual
import com.example.codefishing.core.i18n.LocalTeks
import com.example.codefishing.core.i18n.t
import com.example.codefishing.core.data.hemat
import com.example.codefishing.ui.theme.LocalWarna
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Fase animasi adegan pada layar Kuis Mode Belajar.
 * - [Diam]  : soal baru, alat siap dan aktor menunggu.
 * - [Lempar]: pemain mengunci opsi; aksi dimulai (lempar joran, tembak tombak, turunkan
 *             tali es, tarik bubu, tebar jala, atau turunkan pukat).
 * - [Dapat] : jawaban benar, ikan ditarik/diangkat lalu alat kembali siap.
 * - [Lepas] : jawaban salah, tali putus / tombak meleset / ikan kabur.
 */
enum class FasePancing { Diam, Lempar, Dapat, Lepas }

private fun durasiAksi(fase: FasePancing, jenis: JenisAdegan): Int = when (fase) {
    FasePancing.Dapat -> 1500
    FasePancing.Lepas -> 1300
    FasePancing.Diam -> 0
    FasePancing.Lempar -> durasiLemparAdegan(jenis)
}

// Ilustrasi adegan selalu bertema malam/dalam laut (gelap) di tema gelap MAUPUN terang, jadi semua
// elemen yang digambar di atasnya memakai warna terang tetap. Dulu tali, ombak, salju, dan hujan
// memakai warna tema (teksDiAdegan) yang di tema terang berupa navy gelap dan nyaris tak terlihat.
private val TALI_ADEGAN = Color(0xFFEAF2F4)
private val OMBAK_ADEGAN = Color(0xFF7FC9E0)
private val SALJU_ADEGAN = Color(0xFFF4FAFC)
private val BAHAYA_ADEGAN = Color(0xFFFF6B81)
private val PELAMPUNG_ADEGAN = Color(0xFFE5384F)
private val KILAU_ADEGAN = Color(0xFF6EE7B7)
private val WARNA_BATANG = Color(0xFF7A4B26)
private val WARNA_GAGANG = Color(0xFF3E2A18)
private val WARNA_TOMBAK = Color(0xFFDDE6E9)

/**
 * Adegan kuis di atas soal. Latar memakai ilustrasi `adegan_*` yang SUDAH BERSIH dari alat
 * pancing. Semua alat (joran, tali, pelampung, kail + umpan, jala, bubu, tombak, pukat)
 * digambar di sini dari [hitungKeadaan], jadi hanya ada satu set alat dan geraknya selalu
 * menempel ke aktor di dalam ilustrasi.
 *
 * @param berhasil hasil jawaban yang sedang dikunci. Hanya dipakai agar tembakan tombak
 *   penyelam tampak kena atau meleset sejak awal tembakan.
 * @param tier kelangkaan ikan target; ikan langka ke atas diberi aura sesuai tiernya saat muncul.
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
    berhasil: Boolean = true,
    tier: String = "biasa",
) {
    val warna = LocalWarna.current
    val tr = LocalTeks.current
    val density = LocalDensity.current
    val px = density.density
    val kurangiGerak = remember { !ValueAnimator.areAnimatorsEnabled() }
    val hemat = LocalEfekVisual.current.hemat
    val jenis = remember(nomorStage, varianBoss) { jenisAdeganStage(nomorStage, varianBoss) }
    val bersalju = adeganBersalju(nomorStage)

    val transisi = rememberInfiniteTransition(label = "memancing-ambient")
    val ombakMentah = transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7800, easing = LinearEasing)),
        label = "ombak",
    )
    val napasState = transisi.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Reverse),
        label = "napas",
    )
    val kilatState = transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing)),
        label = "kilat",
    )
    // Jam ombak dibulatkan ke ±30 fps (234 langkah per 7,8 dtk). Nilainya dibaca saat komposisi
    // (ayunan pelampung), jadi tanpa pembulatan seluruh adegan disusun ulang 60 kali per detik.
    // Napas dan kilat hanya dibaca di fase gambar/layer sehingga tidak memicu rekomposisi.
    val ombak by remember { derivedStateOf { floor(ombakMentah.value * 234f) / 234f } }

    val aksi = remember { Animatable(0f) }
    val getar = remember { Animatable(0f) }
    // Fase yang sedang dimainkan oleh [aksi]. Mencegah satu frame "sisa" dari fase sebelumnya
    // (misalnya akhir Lempar) saat fase baru baru saja berganti.
    var faseAksi by remember { mutableStateOf(fase) }
    LaunchedEffect(fase, kurangiGerak, jenis) {
        aksi.snapTo(0f)
        faseAksi = fase
        if (fase != FasePancing.Diam) {
            if (kurangiGerak) {
                aksi.snapTo(1f)
            } else {
                aksi.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durasiAksi(fase, jenis), easing = LinearEasing),
                )
            }
        }
    }
    LaunchedEffect(fase, kurangiGerak) {
        if (fase == FasePancing.Lepas && !kurangiGerak) {
            delay(250)
            getar.snapTo(1f)
            getar.animateTo(0f, tween(560, easing = FastOutSlowInEasing))
        } else {
            getar.snapTo(0f)
        }
    }

    val a = if (faseAksi == fase) aksi.value else 0f
    val gerak = if (kurangiGerak) 0f else 1f
    val guncang = getar.value * gerak

    Box(
        modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .semantics { contentDescription = tr(deskripsiFase(jenis, fase)) },
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()
            val titik = remember(jenis, w, h) { titikPiksel(jenis, w, h) }
            val st = hitungKeadaan(jenis, fase, a, berhasil, titik, ombak, gerak)
            val dasarAir = titikAdegan(jenis).dasarAir.let { fraksi ->
                if (fraksi > 0f) titikGambar(0f, fraksi, w, h).y else h * 0.62f
            }

            val punch = if (fase == FasePancing.Dapat) {
                ((a - 0.45f) / 0.35f).coerceIn(0f, 1f) * 0.026f
            } else {
                0f
            }
            val skalaKamera = 1.055f + punch + if (varianBoss) 0.012f else 0f
            val gx = sin(guncang * PI.toFloat() * 10f) * 5f * guncang * px
            val gy = sin(guncang * PI.toFloat() * 8f + 1f) * 3f * guncang * px

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = skalaKamera
                        scaleY = skalaKamera
                        translationX = gx + st.getarKamera
                        translationY = napasState.value * 2.2f * px * gerak + gy * 0.5f
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
                            .graphicsLayer { alpha = if (gerak > 0f && !hemat && kilatState.value > 0.90f) 1f else 0f },
                    )
                }

                // --- Lapisan 1: ambient air, tali, joran, tombak, efek permukaan ------------
                Canvas(Modifier.fillMaxSize()) {
                    if (jenis == JenisAdegan.LubangEs) {
                        gambarOmbak(SALJU_ADEGAN, ombak, h * 0.70f, h * 0.008f, if (hemat) 1 else 2)
                        gambarSalju(SALJU_ADEGAN, ombak, jumlah = if (hemat) 10 else 22)
                    } else if (diBawahAir(jenis)) {
                        gambarGelembung(OMBAK_ADEGAN, ombak, dasar = h * 0.96f, jumlah = if (hemat) 6 else 12)
                        if (bersalju) gambarSalju(SALJU_ADEGAN.copy(alpha = 0.55f), ombak, jumlah = if (hemat) 8 else 22)
                    } else {
                        if (bersalju) gambarSalju(SALJU_ADEGAN, ombak, jumlah = if (hemat) 10 else 22)
                        gambarOmbak(
                            warna = OMBAK_ADEGAN,
                            fase = ombak,
                            dasar = dasarAir,
                            amplitudo = h * 0.018f,
                            jumlahGaris = if (hemat) 2 else if (jenis == JenisAdegan.Badai) 4 else 3,
                        )
                        gambarKilauAir(OMBAK_ADEGAN, ombak, dasarAir + 8.dp.toPx())
                    }

                    // Aura kelangkaan di belakang ikan target (biasa tidak punya aura).
                    val posisiAura = st.ikan
                    if (posisiAura != null && st.ikanAlpha > 0.02f && kelangkaan(tier) != Kelangkaan.BIASA) {
                        val ukuranIkanPx = (if (diBawahAir(jenis)) 76.dp else 84.dp).toPx() * st.ikanSkala
                        gambarAuraTier(
                            tier = tier,
                            pusat = posisiAura,
                            jari = ukuranIkanPx * 0.85f,
                            waktu = ombak,
                            gelap = true,
                            utama = warnaTierAdegan(tier),
                            alpha = st.ikanAlpha,
                        )
                    }

                    val warnaTali = TALI_ADEGAN
                    gambarSegTali(st.tali3, warnaTali)
                    gambarSegTali(st.tali2, warnaTali)
                    gambarSegTali(st.tali, warnaTali)

                    val pangkalBatang = st.batangPangkal
                    val ujungBatang = st.batangUjung
                    if (pangkalBatang != null && ujungBatang != null) {
                        gambarBatang(pangkalBatang, ujungBatang, st.batangLengkung, WARNA_BATANG, WARNA_GAGANG)
                    }

                    val bidikDari = st.garisBidikDari
                    val bidikKe = st.garisBidikKe
                    if (bidikDari != null && bidikKe != null) {
                        gambarGarisBidik(bidikDari, bidikKe, st.garisBidikAlpha, OMBAK_ADEGAN)
                    }
                    val ekor = st.tombakMulai
                    val kepala = st.tombakUjung
                    if (ekor != null && kepala != null) {
                        gambarTombak(ekor, kepala, 1f, WARNA_TOMBAK)
                    }
                    val jejakA = st.jejakMulai
                    val jejakB = st.jejakSelesai
                    if (jejakA != null && jejakB != null && st.jejakT > 0f) {
                        gambarGelembungJejak(jejakA, jejakB, st.jejakT, OMBAK_ADEGAN)
                    }

                    val riak = st.riak
                    if (riak != null && st.riakT > 0f) {
                        gambarRiak(
                            pusat = riak,
                            progres = st.riakT,
                            warna = if (st.riakBahaya) BAHAYA_ADEGAN else OMBAK_ADEGAN,
                        )
                    }
                    val serpihan = st.serpihan
                    if (serpihan != null && st.serpihanT in 0.002f..0.998f) {
                        gambarSerpihanEs(serpihan, st.serpihanT, Color.White)
                    }
                    val splash = st.splash
                    if (splash != null && st.splashT in 0.002f..0.998f && jenis != JenisAdegan.LubangEs) {
                        gambarPercikan(splash, st.splashT, Color.White, jumlah = if (hemat) 8 else 16)
                    }
                    if (varianBoss) {
                        gambarHujan(TALI_ADEGAN, ombak, jumlah = if (hemat) 12 else 26)
                    }
                    if (varianBoss && !kurangiGerak && !hemat && kilatState.value > 0.92f) {
                        drawRect(Color.White.copy(alpha = 0.16f))
                    }
                }

                // --- Alat berbentuk gambar: jala, pukat, bubu -------------------------------
                val alat = st.alat
                if (alat != null && st.alatAlpha > 0.01f && st.alatLebar > 1f) {
                    val res = when (jenis) {
                        JenisAdegan.Badai -> Art.alat_jaring_pukat
                        JenisAdegan.Bubu -> Art.alat_bubu
                        else -> Art.alat_jala_lempar
                    }
                    val aspek = when (jenis) {
                        JenisAdegan.Badai -> 1.10f
                        JenisAdegan.Bubu -> 0.806f
                        else -> 1f
                    }
                    val lebar = st.alatLebar
                    val tinggi = lebar * aspek
                    val nama = t(
                        when (jenis) {
                            JenisAdegan.Badai -> "Pukat"
                            JenisAdegan.Bubu -> "Bubu"
                            else -> "Jala"
                        },
                    )
                    // Kotak luar diratakan (perspektif jala di permukaan air), gambar di dalamnya
                    // berputar saat jala dilempar.
                    Box(
                        Modifier
                            .offset {
                                IntOffset(
                                    (alat.x - lebar / 2f).roundToInt(),
                                    (alat.y - tinggi / 2f).roundToInt(),
                                )
                            }
                            .size(with(density) { lebar.toDp() }, with(density) { tinggi.toDp() })
                            .graphicsLayer {
                                alpha = st.alatAlpha
                                scaleY = st.alatSkalaY
                                rotationZ = st.alatRot
                            },
                    ) {
                        Gambar(
                            res,
                            nama,
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationZ = st.alatSpin },
                        )
                    }
                }

                // --- Lapisan 2: pelampung dan kail (di atas alat dan tali) ------------------
                val pelampung = st.pelampung
                val kail = st.kail
                if ((pelampung != null && st.pelampungAlpha > 0.01f) || (kail != null && st.kailAlpha > 0.01f)) {
                    Canvas(Modifier.fillMaxSize()) {
                        if (pelampung != null && kail != null && st.pelampungAlpha > 0.01f) {
                            // Tali pendek di bawah pelampung menuju kail.
                            drawLine(
                                color = TALI_ADEGAN.copy(alpha = 0.55f * st.pelampungAlpha),
                                start = pelampung,
                                end = kail,
                                strokeWidth = 1.dp.toPx(),
                            )
                        }
                        if (kail != null) gambarKail(kail, st.kailAlpha, st.kailUmpan)
                        if (pelampung != null && st.pelampungAlpha > 0.01f) {
                            gambarPelampung(
                                pusat = pelampung,
                                skala = if (jenis == JenisAdegan.Bubu) 0.8f else 1f,
                                rotasi = st.pelampungRot,
                                warnaBadan = PELAMPUNG_ADEGAN.copy(alpha = st.pelampungAlpha),
                                warnaPita = Color.White.copy(alpha = st.pelampungAlpha),
                            )
                        }
                    }
                }

                // --- Splash permukaan --------------------------------------------------------
                val splashPos = st.splash
                if (splashPos != null && st.splashT in 0.002f..0.998f && jenis != JenisAdegan.LubangEs &&
                    jenis != JenisAdegan.Selam
                ) {
                    val ukuranSplash = (58f + 50f * st.splashT).dp
                    Gambar(
                        Art.percikan_air,
                        null,
                        Modifier
                            .offset {
                                IntOffset(
                                    (splashPos.x - ukuranSplash.toPx() / 2f).roundToInt(),
                                    (splashPos.y - ukuranSplash.toPx() / 2f).roundToInt(),
                                )
                            }
                            .size(ukuranSplash)
                            .alpha((1f - st.splashT).coerceIn(0f, 1f))
                            .graphicsLayer { rotationZ = -10f + st.splashT * 20f },
                    )
                }

                // --- Percikan saat tali putus ------------------------------------------------
                val ledakan = st.ledakan
                if (ledakan != null && st.ledakanT in 0.002f..0.998f) {
                    val ukuranLedak = (56f + 36f * st.ledakanT).dp
                    Gambar(
                        Art.ledakan_kail_putus,
                        null,
                        Modifier
                            .offset {
                                IntOffset(
                                    (ledakan.x - ukuranLedak.toPx() / 2f).roundToInt(),
                                    (ledakan.y - ukuranLedak.toPx() / 2f).roundToInt(),
                                )
                            }
                            .size(ukuranLedak)
                            .alpha((1f - st.ledakanT * 0.85f).coerceIn(0f, 1f))
                            .graphicsLayer { rotationZ = 8f + st.ledakanT * 18f },
                    )
                }

                // --- Ikan target -------------------------------------------------------------
                val posisiIkan = st.ikan
                if (posisiIkan != null && st.ikanAlpha > 0.01f) {
                    val ukuranIkan = (if (diBawahAir(jenis)) 76.dp else 84.dp) * st.ikanSkala
                    Gambar(
                        ikan,
                        t(if (fase == FasePancing.Dapat) "Ikan tertangkap" else "Ikan"),
                        Modifier
                            .offset {
                                IntOffset(
                                    (posisiIkan.x - ukuranIkan.toPx() / 2f).roundToInt(),
                                    (posisiIkan.y - ukuranIkan.toPx() / 2f).roundToInt(),
                                )
                            }
                            .size(ukuranIkan)
                            .graphicsLayer {
                                alpha = st.ikanAlpha
                                rotationZ = st.ikanRot
                                scaleX = if (st.ikanBalik) -1f else 1f
                            },
                    )
                }

                // --- Kilau saat ikan diamankan -----------------------------------------------
                val kilau = st.kilau
                if (kilau != null && st.kilauT in 0.002f..0.998f) {
                    Canvas(Modifier.fillMaxSize()) {
                        gambarKilau(kilau, st.kilauT, KILAU_ADEGAN, jumlah = 12)
                    }
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
                    t("Salah"),
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

private fun DrawScope.gambarSegTali(seg: SegTali?, warna: Color) {
    if (seg == null || seg.alpha <= 0.01f) return
    gambarTali(
        mulai = seg.a,
        selesai = seg.b,
        kendur = seg.kendur,
        warna = warna.copy(alpha = warna.alpha * seg.alpha),
        lebar = seg.lebar,
    )
}

private fun deskripsiFase(jenis: JenisAdegan, fase: FasePancing): String = when (fase) {
    FasePancing.Diam -> when (jenis) {
        JenisAdegan.Kapal -> "Nelayan di kapal memegang joran, kail berumpan siap dilempar"
        JenisAdegan.Dermaga -> "Nelayan di dermaga memegang joran, kail berumpan siap dilempar"
        JenisAdegan.PerahuCadik -> "Penjala di perahu cadik menggenggam jala, siap melempar"
        JenisAdegan.Selam -> "Penyelam memegang senapan tombak dan mengintai ikan"
        JenisAdegan.LubangEs -> "Nelayan es memegang joran pendek di tepi lubang es"
        JenisAdegan.Bubu -> "Bubu terpasang di dasar laut dengan pelampung penanda"
        JenisAdegan.Badai -> "Pukat tergantung di derek kapal yang bertahan di tengah badai"
    }
    FasePancing.Lempar -> when (jenis) {
        JenisAdegan.Selam -> "Penyelam membidik dan menembakkan tombak ke arah ikan"
        JenisAdegan.LubangEs -> "Kail berumpan diturunkan ke dalam lubang es"
        JenisAdegan.Bubu -> "Tali bubu mulai ditarik dari dasar laut"
        JenisAdegan.PerahuCadik -> "Jala dilempar berputar dan mengembang di atas air"
        JenisAdegan.Badai -> "Pukat diturunkan dari derek ke laut"
        else -> "Joran dilecutkan, kail berumpan dan pelampung mendarat di air"
    }
    FasePancing.Dapat -> when (jenis) {
        JenisAdegan.Selam -> "Tombak mengenai ikan dan ditarik kembali dengan tali"
        JenisAdegan.LubangEs -> "Ikan ditarik keluar dari lubang es"
        JenisAdegan.Bubu -> "Bubu terangkat ke dek dan ikan berhasil ditangkap"
        JenisAdegan.PerahuCadik -> "Jala menutup dan ikan berhasil diangkat ke perahu"
        JenisAdegan.Badai -> "Pukat penuh ikan dihela naik dan diayun ke dek"
        else -> "Ikan menyambar umpan lalu ditarik ke atas"
    }
    FasePancing.Lepas -> when (jenis) {
        JenisAdegan.Selam -> "Tombak meleset dan ikan kabur"
        JenisAdegan.LubangEs -> "Kail terlepas dan ikan kabur di bawah es"
        JenisAdegan.Bubu -> "Bubu terangkat dan ikan lolos"
        JenisAdegan.PerahuCadik -> "Jala ditarik kosong, ikan lolos"
        JenisAdegan.Badai -> "Pukat terangkat kosong, ikan lolos"
        else -> "Tali pancing putus dan ikan kabur membawa kail"
    }
}

package com.example.codefishing.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.core.db.StageStatusRow
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.BarProgres
import com.example.codefishing.frontend.components.ChipTier
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan
import com.example.codefishing.frontend.peta.PetaGeometri
import com.example.codefishing.frontend.peta.PetaSvg
import com.example.codefishing.frontend.peta.TitikPeta
import com.example.codefishing.pemula.ProgresPeta
import com.example.codefishing.ui.theme.LocalWarna
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private sealed interface PilihanPeta {
    data class Stage(val nomor: Int) : PilihanPeta
    data object Gerbang : PilihanPeta
}

/** Peta yang sedang ditampilkan. Antartika terbuka setelah stage 1-10 selesai. */
private enum class PetaAktif { PASIFIK, ANTARTIKA }

/**
 * 03 Peta Perairan: peta nautical vertikal yang digeser ke atas-bawah.
 * Latar peta dirender dari SVG; rute, node stage, kapal, dan info dibuat native Compose.
 */
@Composable
fun PetaScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val warna = LocalWarna.current
    val stages by c.game.observeStages().collectAsState(emptyList())
    val player by c.game.observePlayer().collectAsState(null)
    val ikan by produceState<List<IkanEntity>>(emptyList()) { value = c.game.semuaIkan() }
    val petaIkan = remember(ikan) { ikan.associateBy { it.id } }
    val context = LocalContext.current

    // Pilihan manual pemain; null = ikut peta tempat stage terakhir dikerjakan.
    var petaPilihan by rememberSaveable { mutableStateOf<PetaAktif?>(null) }
    // Stage yang diketuk: kapal ikut pindah ke node tersebut.
    var kapalPilihan by remember { mutableStateOf<Int?>(null) }
    val selesaiPasifik = stages.count {
        it.nomor <= ProgresPeta.STAGE_PASIFIK_TERAKHIR && it.status == "selesai"
    }
    val antartikaTerbuka = ProgresPeta.antartikaTerbuka(selesaiPasifik)
    val stageAktifGlobal = stages.firstOrNull { it.status != "selesai" && it.terbuka == 1 }
        ?: stages.lastOrNull()
    val nomorTerakhir = player?.stage_terakhir_id?.let { id ->
        stages.firstOrNull { it.id == id }?.nomor
    }
    val petaAktif = petaPilihan ?: if (
        antartikaTerbuka &&
        (nomorTerakhir ?: 0) > ProgresPeta.STAGE_PASIFIK_TERAKHIR
    ) {
        PetaAktif.ANTARTIKA
    } else {
        PetaAktif.PASIFIK
    }

    fun nomorLokal(nomor: Int): Int =
        if (petaAktif == PetaAktif.ANTARTIKA) nomor - ProgresPeta.STAGE_PASIFIK_TERAKHIR else nomor

    fun diPeta(nomor: Int): Boolean =
        if (petaAktif == PetaAktif.ANTARTIKA) {
            ProgresPeta.diAntartika(nomor)
        } else {
            !ProgresPeta.diAntartika(nomor)
        }

    fun titikLokal(lokal: Int): TitikPeta? =
        if (petaAktif == PetaAktif.ANTARTIKA) {
            PetaGeometri.titikStageAntartika(lokal)
        } else {
            PetaGeometri.titikStage(lokal)
        }

    val stagePeta = stages.filter { diPeta(it.nomor) }
    val jumlahSelesaiPeta = stagePeta.count { it.status == "selesai" }
    val stageAktif = stagePeta.firstOrNull { it.status != "selesai" && it.terbuka == 1 }
        ?: stagePeta.lastOrNull()
    val nomorKapal = PetaGeometri.nomorKapal(
        (kapalPilihan?.takeIf { diPeta(it) } ?: nomorTerakhir?.takeIf { diPeta(it) })
            ?.let { nomorLokal(it) },
        stageAktif?.nomor?.let { nomorLokal(it) },
    )
    val titikKapal = titikLokal(nomorKapal) ?: PetaGeometri.stage.first()

    // Fokus kapal hanya berlaku di peta tempat node diketuk.
    LaunchedEffect(petaAktif) { kapalPilihan = null }

    val latarPeta by produceState<ImageBitmap?>(null, warna.gelap, petaAktif) {
        value = withContext(Dispatchers.IO) {
            PetaSvg.ambil(context, !warna.gelap, petaAktif == PetaAktif.ANTARTIKA)
        }
    }
    var terpilih by remember { mutableStateOf<PilihanPeta?>(null) }
    var pilihanTampil by remember { mutableStateOf<PilihanPeta?>(null) }
    val scroll = rememberScrollState()
    // Dinaikkan setiap kompas diketuk agar peta bergeser ke stage terakhir.
    var mintaGeser by remember { mutableIntStateOf(0) }

    LaunchedEffect(terpilih) {
        if (terpilih != null) pilihanTampil = terpilih
    }
    BackHandler(enabled = terpilih != null) { terpilih = null }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(warna.petaLatar),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val lebar = maxWidth
            val tinggi = lebar * (PetaGeometri.TINGGI_DUNIA / PetaGeometri.LEBAR_DUNIA)
            val density = LocalDensity.current

            suspend fun geserKe(titik: TitikPeta) {
                val tinggiPeta = with(density) { tinggi.toPx() }
                val tinggiLayar = with(density) { maxHeight.toPx() }
                val target = tinggiPeta * (titik.y / PetaGeometri.TINGGI_DUNIA) - tinggiLayar / 2f
                scroll.animateScrollTo(target.toInt().coerceIn(0, scroll.maxValue))
            }

            LaunchedEffect(stages.size, player?.stage_terakhir_id, tinggi, petaAktif) {
                if (stages.isNotEmpty()) {
                    delay(120)
                    geserKe(titikKapal)
                }
            }

            // Kompas: geser peta ke node stage yang terakhir dikerjakan.
            LaunchedEffect(mintaGeser) {
                if (mintaGeser == 0) return@LaunchedEffect
                val global = stages.firstOrNull { it.id == c.progress.kuisTerakhirStage }?.nomor
                    ?: stageAktifGlobal?.nomor
                    ?: return@LaunchedEffect
                val nomor = if (diPeta(global)) global else stageAktif?.nomor ?: return@LaunchedEffect
                titikLokal(nomorLokal(nomor))?.let { geserKe(it) }
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(tinggi)
                        .semantics {
                            contentDescription = if (petaAktif == PetaAktif.ANTARTIKA) {
                                "Peta perairan Antartika, stage 11 sampai 20"
                            } else {
                                "Peta perairan Samudra Pasifik, stage 1 sampai 10"
                            }
                        },
                ) {
                    if (latarPeta != null) {
                        Image(
                            bitmap = latarPeta!!,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds,
                        )
                    } else {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(warna.petaLatar, warna.petaLatarBawah),
                                    ),
                                ),
                        )
                    }

                    RutePeta(petaAktif, jumlahSelesaiPeta, Modifier.fillMaxSize())

                    stagePeta.forEach { stage ->
                        val titik = titikLokal(nomorLokal(stage.nomor)) ?: return@forEach
                        val ukuran = if (stage.tipe == "boss") 68.dp else 54.dp
                        NodePeta(
                            stage = stage,
                            terpilih = terpilih == PilihanPeta.Stage(stage.nomor),
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                if (stage.terbuka == 1) kapalPilihan = stage.nomor
                                terpilih = PilihanPeta.Stage(stage.nomor)
                            },
                            modifier = Modifier.tempatNode(titik, lebar, tinggi, ukuran),
                        )
                        if (stage.tipe == "boss") {
                            Box(
                                Modifier
                                    .offset(
                                        x = lebar * (titik.x / PetaGeometri.LEBAR_DUNIA) - 60.dp,
                                        y = tinggi * (titik.y / PetaGeometri.TINGGI_DUNIA) + 38.dp,
                                    )
                                    .width(120.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "BOSS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = warna.bahayaTeks,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                )
                            }
                        } else {
                            val lebarLabel = 104.dp
                            Box(
                                Modifier
                                    .offset(
                                        x = lebar * (titik.x / PetaGeometri.LEBAR_DUNIA) - lebarLabel / 2,
                                        y = tinggi * (titik.y / PetaGeometri.TINGGI_DUNIA) + ukuran / 2 + 1.dp,
                                    )
                                    .width(lebarLabel)
                                    .background(warna.petaLabelLatar, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    stage.nama,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 11.sp),
                                    color = if (stage.terbuka == 1) warna.petaTeks else warna.petaTeksRedup.copy(alpha = 0.55f),
                                )
                            }
                        }
                    }

                    if (petaAktif == PetaAktif.PASIFIK) {
                        val g = PetaGeometri.gerbang
                        NodeGerbang(
                            terbuka = antartikaTerbuka,
                            terpilih = terpilih == PilihanPeta.Gerbang,
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                terpilih = PilihanPeta.Gerbang
                            },
                            modifier = Modifier.tempatNode(g, lebar, tinggi, 58.dp),
                        )
                        Box(
                            Modifier
                                .offset(
                                    x = lebar * (g.x / PetaGeometri.LEBAR_DUNIA) - 74.dp,
                                    y = tinggi * (g.y / PetaGeometri.TINGGI_DUNIA) + 34.dp,
                                )
                                .width(148.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "ANTARTIKA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = warna.petaTeks,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.4.sp,
                                )
                                Text(
                                    if (antartikaTerbuka) "rute terbuka" else "terkunci",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = warna.petaTeksRedup.copy(alpha = 0.75f),
                                )
                            }
                        }
                    }

                    Box(
                        Modifier
                            .offset(
                                x = lebar * 0.5f - 100.dp,
                                y = tinggi * (2700f / PetaGeometri.TINGGI_DUNIA),
                            )
                            .width(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (petaAktif == PetaAktif.PASIFIK) "PERAIRAN ANTARTIKA" else "UJUNG SELATAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = warna.petaTeks.copy(alpha = 0.55f),
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp,
                        )
                    }

                    val titikMulai =
                        if (petaAktif == PetaAktif.PASIFIK) PetaGeometri.mulai else PetaGeometri.mulaiAntartika
                    titikMulai.let { awal ->
                        Gambar(
                            Art.bendera_start,
                            "Titik awal pelayaran",
                            Modifier
                                .offset(
                                    x = lebar * (awal.x / PetaGeometri.LEBAR_DUNIA) + 22.dp,
                                    y = tinggi * (awal.y / PetaGeometri.TINGGI_DUNIA) - 18.dp,
                                )
                                .size(34.dp),
                        )
                    }
                    KapalPeta(titikKapal, lebar, tinggi)
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            if (petaAktif == PetaAktif.ANTARTIKA) {
                ChipPeta(
                    judul = "Samudra Pasifik",
                    sub = "‹ Kembali",
                    onClick = {
                        c.audio.play(Sfx.Klik)
                        terpilih = null
                        petaPilihan = PetaAktif.PASIFIK
                    },
                )
            } else {
                ChipPeta("Samudra Pasifik")
            }
            ChipPeta("$jumlahSelesaiPeta/10", "stage selesai", alignEnd = true)
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClickLabel = "Geser ke soal terakhir") {
                    c.audio.play(Sfx.Klik)
                    mintaGeser++
                }
                .semantics { contentDescription = "Geser peta ke soal terakhir dikerjakan" },
            contentAlignment = Alignment.Center,
        ) {
            Gambar(
                if (warna.gelap) Art.kompas else Art.kompas_terang,
                null,
                Modifier.size(46.dp),
            )
        }

        AnimatedVisibility(
            visible = terpilih != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            when (val p = pilihanTampil) {
                is PilihanPeta.Stage -> {
                    val stage = stages.firstOrNull { it.nomor == p.nomor }
                    if (stage != null) {
                        LembarStage(
                            stage = stage,
                            ikan = stage.ikan_target_id?.let { petaIkan[it] },
                            onTutup = { terpilih = null },
                            onBerlayar = {
                                c.audio.play(Sfx.KlikUtama)
                                terpilih = null
                                nav.go(if (stage.tipe == "boss") Screen.Boss else Screen.Materi, stage.id)
                            },
                        )
                    }
                }
                PilihanPeta.Gerbang -> LembarGerbang(
                    terbuka = antartikaTerbuka,
                    onTutup = { terpilih = null },
                    onMasuk = {
                        c.audio.play(Sfx.KlikUtama)
                        terpilih = null
                        petaPilihan = PetaAktif.ANTARTIKA
                    },
                )
                null -> Unit
            }
        }
    }
}

@Composable
private fun ChipPeta(
    judul: String,
    sub: String? = null,
    alignEnd: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val warna = LocalWarna.current
    Column(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(warna.petaChipLatar)
            .border(1.dp, warna.petaTeks.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(role = Role.Button, onClick = onClick)
                        .semantics { contentDescription = "Kembali ke Samudra Pasifik" }
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Text(
            judul,
            style = MaterialTheme.typography.labelLarge,
            color = warna.petaTeks,
            fontWeight = FontWeight.Bold,
        )
        if (sub != null) {
            Text(
                sub,
                style = MaterialTheme.typography.labelSmall,
                color = warna.petaTeksRedup,
            )
        }
    }
}

@Composable
private fun RutePeta(peta: PetaAktif, jumlahSelesai: Int, modifier: Modifier = Modifier) {
    val warna = LocalWarna.current
    val warnaAktif = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val sx = size.width / PetaGeometri.LEBAR_DUNIA
        val sy = size.height / PetaGeometri.TINGGI_DUNIA
        val antartika = peta == PetaAktif.ANTARTIKA
        val semua = PetaGeometri.pathDari(
            if (antartika) PetaGeometri.ruteLengkapAntartika() else PetaGeometri.ruteLengkap(),
            sx,
            sy,
        )
        drawPath(
            path = semua,
            color = warna.petaGaris.copy(alpha = 0.55f),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 12.dp.toPx())),
            ),
        )
        val sudah = if (antartika) {
            PetaGeometri.ruteSelesaiAntartika(jumlahSelesai)
        } else {
            PetaGeometri.ruteSelesai(jumlahSelesai)
        }
        if (sudah.size > 1) {
            drawPath(
                path = PetaGeometri.pathDari(sudah, sx, sy),
                color = warnaAktif,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun NodePeta(
    stage: StageStatusRow,
    terpilih: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val warna = LocalWarna.current
    val warnaProgres = MaterialTheme.colorScheme.tertiary
    val buka = stage.terbuka == 1
    val selesai = stage.status == "selesai"
    val boss = stage.tipe == "boss"
    val ukuran = if (boss) 68.dp else 54.dp
    val progres = (stage.skor_terbaik.coerceIn(0, 100)) / 100f
    val aset = when {
        !buka -> Art.node_terkunci
        selesai -> Art.node_selesai
        boss -> Art.node_boss
        else -> Art.node_aktif
    }
    val status = when {
        !buka -> "terkunci"
        selesai -> "selesai"
        else -> "aktif"
    }

    Box(
        modifier
            .size(ukuran)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Stage ${stage.nomor}, ${stage.nama}, $status, skor ${stage.skor_terbaik} persen"
            },
        contentAlignment = Alignment.Center,
    ) {
        // Node memakai aset final: node_aktif / node_selesai / node_terkunci / node_boss.
        Gambar(aset, null, Modifier.fillMaxSize())
        if (buka && progres > 0f && !selesai) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 3.dp.toPx()
                val rr = size.minDimension / 2f - stroke / 2f
                drawArc(
                    color = warnaProgres,
                    startAngle = -90f,
                    sweepAngle = progres * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - rr, center.y - rr),
                    size = Size(rr * 2f, rr * 2f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        if (buka && !boss) {
            // Nomor stage di lencana kecil agar node tetap bisa dibaca.
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .size(20.dp)
                    .background(warna.petaLatar, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${stage.nomor}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = warna.petaNomorTeks,
                )
            }
        }
        if (terpilih) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = warna.petaTeks,
                    radius = size.minDimension / 2f - 1.dp.toPx(),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx())),
                    ),
                )
            }
        }
    }
}

@Composable
private fun NodeGerbang(
    terbuka: Boolean,
    terpilih: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val warna = LocalWarna.current
    Box(
        modifier
            .size(58.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = if (terbuka) {
                    "Gerbang Antartika, rute terbuka"
                } else {
                    "Gerbang Antartika, terkunci"
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Gambar(if (terbuka) Art.node_aktif else Art.node_terkunci, null, Modifier.fillMaxSize())
        if (terpilih) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    warna.petaTeks,
                    radius = size.minDimension / 2f - 1.dp.toPx(),
                    style = Stroke(
                        2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx())),
                    ),
                )
            }
        }
    }
}

@Composable
private fun KapalPeta(titik: TitikPeta, lebar: Dp, tinggi: Dp) {
    val transisi = rememberInfiniteTransition(label = "kapal-peta")
    val naikTurun by transisi.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing), RepeatMode.Reverse),
        label = "ombak-kapal",
    )
    // Posisi dianimasikan supaya kapal terlihat berlayar saat node lain diketuk.
    val xTarget = lebar * (titik.x / PetaGeometri.LEBAR_DUNIA) - 34.dp
    val yTarget = tinggi * (titik.y / PetaGeometri.TINGGI_DUNIA) - 82.dp
    val x by animateDpAsState(xTarget, tween(420), label = "kapal-x")
    val y by animateDpAsState(yTarget, tween(420), label = "kapal-y")
    Box(
        Modifier
            .offset {
                IntOffset(
                    x.roundToPx(),
                    (y + naikTurun.dp).roundToPx(),
                )
            }
            .size(68.dp),
        contentAlignment = Alignment.Center,
    ) {
        Gambar(Art.kapal_peta, "Kapal pemain", Modifier.size(68.dp))
    }
}

@Composable
private fun LembarStage(
    stage: StageStatusRow,
    ikan: IkanEntity?,
    onTutup: () -> Unit,
    onBerlayar: () -> Unit,
) {
    val warna = LocalWarna.current
    val buka = stage.terbuka == 1
    val selesai = stage.status == "selesai"
    Kartu(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        warna = warna.kartu.copy(alpha = 0.97f),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (stage.tipe == "boss") "STAGE ${stage.nomor} · BOSS" else "STAGE ${stage.nomor}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        letterSpacing = 1.sp,
                    )
                    Text(stage.nama, style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    "✕",
                    Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onTutup)
                        .padding(8.dp)
                        .semantics { contentDescription = "Tutup info stage" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                )
            }
            Text(
                "${stage.zona} · ${stage.konsep}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (ikan != null) {
                    Gambar(gambarIkan(ikan.gambar), ikan.nama, Modifier.size(58.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ikan.nama, style = MaterialTheme.typography.titleMedium)
                        ChipTier(ikan.tier)
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        when {
                            !buka -> "Terkunci"
                            selesai -> "Selesai"
                            stage.skor_terbaik > 0 -> "Skor terbaik"
                            else -> "Belum dimulai"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (buka) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (buka) {
                        Text(
                            "${stage.skor_terbaik}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            if (buka) {
                BarProgres(
                    stage.skor_terbaik / 100f,
                    warna = if (selesai) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    "Selesaikan Stage ${stage.nomor - 1} untuk membuka rute ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = warna.bahayaTeks,
                )
            }
            Spacer(Modifier.height(2.dp))
            TombolUtama(
                teks = when {
                    !buka -> "Terkunci"
                    selesai -> "Coba Tantang Ulang"
                    stage.tipe == "boss" -> "Hadapi Boss"
                    else -> "Berlayar"
                },
                onClick = onBerlayar,
                enabled = buka,
            )
        }
    }
}

@Composable
private fun LembarGerbang(terbuka: Boolean, onTutup: () -> Unit, onMasuk: () -> Unit) {
    val warna = LocalWarna.current
    Kartu(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        warna = warna.kartu.copy(alpha = 0.97f),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PERAIRAN BERIKUTNYA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Text("Antartika", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    "✕",
                    Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onTutup)
                        .padding(8.dp)
                        .semantics { contentDescription = "Tutup info gerbang" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Gambar(Art.gunung_es, "Gunung es", Modifier.size(62.dp))
                Text(
                    if (terbuka) {
                        "Dingin, es mengapung. Seluruh Samudra Pasifik selesai — rute ke 10 stage Antartika sudah terbuka."
                    } else {
                        "Dingin, es mengapung. Selesaikan seluruh stage 1-10 di Samudra Pasifik untuk membuka rute ini."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TombolUtama(
                teks = if (terbuka) "Berlayar ke Antartika" else "Terkunci",
                onClick = onMasuk,
                enabled = terbuka,
            )
        }
    }
}

private fun Modifier.tempatNode(titik: TitikPeta, lebar: Dp, tinggi: Dp, ukuran: Dp): Modifier =
    offset(
        x = lebar * (titik.x / PetaGeometri.LEBAR_DUNIA) - ukuran / 2f,
        y = tinggi * (titik.y / PetaGeometri.TINGGI_DUNIA) - ukuran / 2f,
    )

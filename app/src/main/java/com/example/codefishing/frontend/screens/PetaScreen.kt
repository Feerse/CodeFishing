package com.example.codefishing.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private sealed interface PilihanPeta {
    data class Stage(val nomor: Int) : PilihanPeta
    data object Gerbang : PilihanPeta
}

/**
 * 03 Peta Perairan: peta nautical vertikal yang digeser ke atas-bawah.
 * Latar peta dirender dari SVG; rute, node stage, kapal, dan info dibuat native Compose.
 */
@Composable
fun PetaScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val stages by c.game.observeStages().collectAsState(emptyList())
    val player by c.game.observePlayer().collectAsState(null)
    val ikan by produceState<List<IkanEntity>>(emptyList()) { value = c.game.semuaIkan() }
    val petaIkan = remember(ikan) { ikan.associateBy { it.id } }
    val context = LocalContext.current
    val latarPeta by produceState<ImageBitmap?>(null) {
        value = withContext(Dispatchers.IO) { PetaSvg.ambil(context) }
    }
    var terpilih by remember { mutableStateOf<PilihanPeta?>(null) }
    var pilihanTampil by remember { mutableStateOf<PilihanPeta?>(null) }
    val scroll = rememberScrollState()

    LaunchedEffect(terpilih) {
        if (terpilih != null) pilihanTampil = terpilih
    }
    BackHandler(enabled = terpilih != null) { terpilih = null }

    val jumlahSelesai = stages.count { it.status == "selesai" }
    val stageAktif = stages.firstOrNull { it.status != "selesai" && it.terbuka == 1 }
        ?: stages.lastOrNull()
    val nomorTerakhir = player?.stage_terakhir_id?.let { id ->
        stages.firstOrNull { it.id == id }?.nomor
    }
    val nomorKapal = PetaGeometri.nomorKapal(nomorTerakhir, stageAktif?.nomor)
    val titikKapal = PetaGeometri.titikStage(nomorKapal) ?: PetaGeometri.stage.first()

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFF071923)),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val lebar = maxWidth
            val tinggi = lebar * (PetaGeometri.TINGGI_DUNIA / PetaGeometri.LEBAR_DUNIA)
            val density = LocalDensity.current

            LaunchedEffect(stages.size, player?.stage_terakhir_id, tinggi) {
                if (stages.isNotEmpty()) {
                    delay(120)
                    val tinggiPeta = with(density) { tinggi.toPx() }
                    val tinggiLayar = with(density) { maxHeight.toPx() }
                    val target = (tinggiPeta * (titikKapal.y / PetaGeometri.TINGGI_DUNIA) - tinggiLayar / 2f)
                    scroll.animateScrollTo(target.toInt().coerceIn(0, scroll.maxValue))
                }
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
                        .semantics { contentDescription = "Peta perairan Samudra Pasifik, 10 stage" },
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
                                        listOf(Color(0xFF123F55), Color(0xFF0B2F42), Color(0xFF07171F)),
                                    ),
                                ),
                        )
                    }

                    RutePeta(jumlahSelesai, Modifier.fillMaxSize())

                    stages.forEach { stage ->
                        val titik = PetaGeometri.titikStage(stage.nomor) ?: return@forEach
                        val ukuran = if (stage.tipe == "boss") 68.dp else 54.dp
                        NodePeta(
                            stage = stage,
                            terpilih = terpilih == PilihanPeta.Stage(stage.nomor),
                            onClick = {
                                c.audio.play(Sfx.Klik)
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
                                    color = Color(0xFFFFB3B3),
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
                                    .background(Color(0xB3081B26), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    stage.nama,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 11.sp),
                                    color = if (stage.terbuka == 1) Color(0xFFE8F4F6) else Color(0xFF9DB5BF).copy(alpha = 0.55f),
                                )
                            }
                        }
                    }

                    val g = PetaGeometri.gerbang
                    NodeGerbang(
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
                                color = Color(0xFFD8EDF5),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                            )
                            Text(
                                "segera hadir",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF9FC4D6).copy(alpha = 0.75f),
                            )
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
                            "PERAIRAN ANTARTIKA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD8EDF5).copy(alpha = 0.55f),
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp,
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
            ChipPeta("Samudra Pasifik", "10 stage · geser & ketuk titik")
            ChipPeta("$jumlahSelesai/10", "stage selesai", alignEnd = true)
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
                PilihanPeta.Gerbang -> LembarGerbang(onTutup = { terpilih = null })
                null -> Unit
            }
        }
    }
}

@Composable
private fun ChipPeta(judul: String, sub: String, alignEnd: Boolean = false) {
    Column(
        Modifier
            .background(Color(0xCC081B26), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Text(
            judul,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            sub,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9DB5BF),
        )
    }
}

@Composable
private fun RutePeta(jumlahSelesai: Int, modifier: Modifier = Modifier) {
    val warnaAktif = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val sx = size.width / PetaGeometri.LEBAR_DUNIA
        val sy = size.height / PetaGeometri.TINGGI_DUNIA
        val semua = PetaGeometri.pathDari(PetaGeometri.ruteLengkap(), sx, sy)
        drawPath(
            path = semua,
            color = Color(0xFF8FB2B8).copy(alpha = 0.55f),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 12.dp.toPx())),
            ),
        )
        val sudah = PetaGeometri.ruteSelesai(jumlahSelesai)
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
    val buka = stage.terbuka == 1
    val selesai = stage.status == "selesai"
    val boss = stage.tipe == "boss"
    val ukuran = if (boss) 68.dp else 54.dp
    val isi = when {
        !buka -> Color(0xFF2A4656)
        selesai -> Color(0xFF1C6B58)
        boss -> Color(0xFF7B303D)
        else -> Color(0xFFF26A2E)
    }
    val ring = when {
        !buka -> Color(0xFF5F8796)
        selesai -> Color(0xFF5FD3A0)
        else -> Color(0xFFF26A2E)
    }
    val progres = (stage.skor_terbaik.coerceIn(0, 100)) / 100f
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
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val r = size.minDimension / 2f - stroke
            drawCircle(Color(0x88000000), radius = r, center = center + Offset(0f, 3.dp.toPx()))
            drawCircle(Color(0xFF071923), radius = r, center = center)
            drawCircle(Color.White.copy(alpha = 0.20f), radius = r * 0.82f, style = Stroke(stroke))
            if (buka && progres > 0f) {
                val rr = r * 0.82f
                drawArc(
                    color = ring,
                    startAngle = -90f,
                    sweepAngle = progres * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - rr, center.y - rr),
                    size = Size(rr * 2f, rr * 2f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            drawCircle(isi, radius = r * 0.62f)
            if (terpilih) {
                drawCircle(
                    color = Color.White,
                    radius = r * 0.98f,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx())),
                    ),
                )
            }
        }
        when {
            !buka -> Text("🔒", fontSize = 16.sp)
            boss -> Text("★", fontSize = 24.sp, color = Color(0xFFFFE7B0))
            else -> Text(
                "${stage.nomor}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF062B2A),
            )
        }
        if (selesai) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(17.dp)
                    .background(Color(0xFF5FD3A0), CircleShape)
                    .border(2.dp, Color(0xFF071923), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF062B2A))
            }
        }
    }
}

@Composable
private fun NodeGerbang(
    terpilih: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(58.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Gerbang Antartika, terkunci, segera hadir" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val path = Path()
            for (i in 0 until 6) {
                val sudut = Math.toRadians((60.0 * i - 90.0))
                val px = center.x + (r * 0.82f * Math.cos(sudut)).toFloat()
                val py = center.y + (r * 0.82f * Math.sin(sudut)).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawCircle(Color(0x88000000), radius = r, center = center + Offset(0f, 3.dp.toPx()))
            drawPath(path, Color(0xFF245A70))
            drawPath(path, Color(0xFF8FB2B8), style = Stroke(3.dp.toPx()))
            if (terpilih) {
                drawCircle(
                    Color.White,
                    radius = r * 0.98f,
                    style = Stroke(
                        2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx())),
                    ),
                )
            }
        }
        Text("🔒", fontSize = 16.sp)
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
    val x = lebar * (titik.x / PetaGeometri.LEBAR_DUNIA) - 34.dp
    val y = tinggi * (titik.y / PetaGeometri.TINGGI_DUNIA) - 82.dp
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
    val buka = stage.terbuka == 1
    val selesai = stage.status == "selesai"
    Kartu(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        warna = Color(0xF20C2B36),
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
                    color = Color(0xFF9DB5BF),
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
                        color = if (buka) MaterialTheme.colorScheme.tertiary else Color(0xFF9DB5BF),
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
                    warna = if (selesai) Color(0xFF5FD3A0) else MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    "Selesaikan Stage ${stage.nomor - 1} untuk membuka rute ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFB3B3),
                )
            }
            Spacer(Modifier.height(2.dp))
            TombolUtama(
                teks = when {
                    !buka -> "Terkunci"
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
private fun LembarGerbang(onTutup: () -> Unit) {
    Kartu(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        warna = Color(0xF20C2B36),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("PERAIRAN BERIKUTNYA", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9FC4D6), letterSpacing = 1.sp)
                    Text("Antartika", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    "✕",
                    Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onTutup)
                        .padding(8.dp)
                        .semantics { contentDescription = "Tutup info gerbang" },
                    color = Color(0xFF9DB5BF),
                    fontSize = 16.sp,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Gambar(Art.gunung_es, "Gunung es", Modifier.size(62.dp))
                Text(
                    "Dingin, es mengapung. Terbuka setelah seluruh Samudra Pasifik selesai. Segera hadir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TombolUtama(teks = "Segera Hadir", onClick = {}, enabled = false)
        }
    }
}

private fun Modifier.tempatNode(titik: TitikPeta, lebar: Dp, tinggi: Dp, ukuran: Dp): Modifier =
    offset(
        x = lebar * (titik.x / PetaGeometri.LEBAR_DUNIA) - ukuran / 2f,
        y = tinggi * (titik.y / PetaGeometri.TINGGI_DUNIA) - ukuran / 2f,
    )

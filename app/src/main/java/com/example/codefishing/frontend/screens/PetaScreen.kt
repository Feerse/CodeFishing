package com.example.codefishing.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
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
import androidx.compose.foundation.layout.heightIn
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
import android.animation.ValueAnimator
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import android.content.Context
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import com.example.codefishing.frontend.peta.TransisiBerlayar
import com.example.codefishing.frontend.peta.SuasanaLaut
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.core.db.StageStatusRow
import com.example.codefishing.core.i18n.LocalTeks
import com.example.codefishing.core.i18n.t
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.gambarIkanKecil
import com.example.codefishing.frontend.components.GambarKontras
import com.example.codefishing.frontend.components.AuraIkan
import com.example.codefishing.frontend.components.BarProgres
import com.example.codefishing.frontend.components.ChipTier
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan
import com.example.codefishing.frontend.peta.PetaGeometri
import com.example.codefishing.frontend.peta.PetaLatar
import com.example.codefishing.frontend.peta.TitikPeta
import com.example.codefishing.pemula.ProgresPeta
import com.example.codefishing.ui.theme.LocalWarna
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private sealed interface PilihanPeta {
    data class Stage(val nomor: Int) : PilihanPeta
    data object Gerbang : PilihanPeta
    /** Pratinjau Segitiga Bermuda di ujung peta Antartika (belum bisa dimainkan). */
    data object Bermuda : PilihanPeta
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
    // Untuk teks di dalam lambda non-@Composable (semantics, clickable).
    val tr = LocalTeks.current

    // Pilihan manual pemain; null = ikut peta stage terakhir / stage yang sedang aktif.
    var petaPilihan by rememberSaveable { mutableStateOf<PetaAktif?>(null) }
    // Stage yang diketuk: kapal ikut pindah ke node tersebut.
    var kapalPilihan by remember { mutableStateOf<Int?>(null) }
    val selesaiPasifik = stages.count {
        it.nomor <= ProgresPeta.STAGE_PASIFIK_TERAKHIR && it.status == "selesai"
    }
    val antartikaTerbuka = ProgresPeta.antartikaTerbuka(selesaiPasifik)
    val stageAktifGlobal = ProgresPeta.stageAktif(stages, { it.status }, { it.terbuka == 1 })
    val nomorTerakhir = player?.stage_terakhir_id?.let { id ->
        stages.firstOrNull { it.id == id }?.nomor
    }
    // Acuan peta awal: progres terjauh (stage aktif atau posisi terakhir). Memakai yang terjauh
    // supaya mengulang stage Pasifik tidak menarik peta kembali ke Pasifik padahal pemain sudah
    // di Antartika; setelah seluruh Pasifik selesai peta langsung pindah ke Antartika.
    val nomorAcuan = ProgresPeta.nomorAcuanPeta(stageAktifGlobal?.nomor, nomorTerakhir)
    val petaAktif = petaPilihan ?: if (
        ProgresPeta.petaAntartika(antartikaTerbuka, nomorAcuan)
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
    // Progres keseluruhan (Pasifik + Antartika) supaya angka terus bertambah sampai stage 20,
    // tidak mentok di 10 saat peta Pasifik selesai.
    val jumlahSelesaiTotal = stages.count { it.status == "selesai" }
    // Bermuda adalah lanjutan Antartika, jadi pratinjaunya selalu memakai progres Antartika
    // meskipun toolbar dibuka saat peta Pasifik sedang tampil.
    val jumlahSelesaiAntartika = stages.count {
        ProgresPeta.diAntartika(it.nomor) && it.status == "selesai"
    }
    val totalStage = stages.size.coerceAtLeast(PetaGeometri.JUMLAH_STAGE)
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

    // Peta berupa WebP hasil render sekali (PetaLatar): bila sudah ada di memori langsung tampil,
    // bila belum didekode di thread latar tanpa menahan komposisi layar.
    val latarPeta by produceState(
        PetaLatar.dapat(!warna.gelap, petaAktif == PetaAktif.ANTARTIKA),
        warna.gelap,
        petaAktif,
    ) {
        val antartika = petaAktif == PetaAktif.ANTARTIKA
        value = PetaLatar.dapat(!warna.gelap, antartika) ?: withContext(Dispatchers.IO) {
            PetaLatar.ambil(context, !warna.gelap, antartika)
        }
    }
    var terpilih by remember { mutableStateOf<PilihanPeta?>(null) }
    var pilihanTampil by remember { mutableStateOf<PilihanPeta?>(null) }

    // Peta lawan dimuat lebih dulu di latar (PetaLatar menyimpan dua peta) sehingga pelayaran antar
    // peta langsung menampilkan peta tujuan begitu layar transisi terbuka.
    LaunchedEffect(antartikaTerbuka, warna.gelap, petaAktif) {
        if (antartikaTerbuka) {
            withContext(Dispatchers.IO) {
                PetaLatar.ambil(context, !warna.gelap, petaAktif != PetaAktif.ANTARTIKA)
            }
        }
    }

    // Toolbar peta mulai tersembunyi supaya peta terlihat penuh; pemain membukanya lewat penanda
    // kecil di kiri atas. Tidak disimpan antar sesi: setiap kali peta dibuka, layar kembali bersih.
    var toolbarTampil by rememberSaveable { mutableStateOf(false) }

    // Pelayaran antar peta: petaAktif baru diganti saat layar transisi tertutup penuh.
    var berlayarKe by remember { mutableStateOf<PetaAktif?>(null) }
    var berlayarDari by remember { mutableStateOf(PetaAktif.PASIFIK) }
    fun mulaiBerlayar(tujuan: PetaAktif) {
        if (berlayarKe != null || tujuan == petaAktif) return
        terpilih = null
        berlayarDari = petaAktif
        berlayarKe = tujuan
        c.audio.play(Sfx.Cipratan)
    }
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

            suspend fun geserKe(titik: TitikPeta, halus: Boolean = true) {
                val tinggiPeta = with(density) { tinggi.toPx() }
                val tinggiLayar = with(density) { maxHeight.toPx() }
                val target = tinggiPeta * (titik.y / PetaGeometri.TINGGI_DUNIA) - tinggiLayar / 2f
                val posisi = target.toInt().coerceIn(0, scroll.maxValue)
                if (halus) scroll.animateScrollTo(posisi) else scroll.scrollTo(posisi)
            }

            LaunchedEffect(stages.size, player?.stage_terakhir_id, tinggi, petaAktif) {
                if (stages.isNotEmpty()) {
                    delay(120)
                    // Langsung pasang posisi awal (tanpa animasi gulir) supaya peta tidak terlihat
                    // bergerak dari stage awal ke stage terakhir saat dibuka.
                    geserKe(titikKapal, halus = false)
                }
            }

            // Kompas: geser peta ke kapal, yaitu node yang sedang dipakai pemain. Dulu sasarannya
            // `kuisTerakhirStage` dari SharedPreferences, sehingga kompas bisa meleset ke stage lain
            // (bahkan ke stage awal peta ini) padahal kapal ada di node lain; sekarang sasarannya
            // persis titik kapal yang tampil, jadi mengetuk kompas selalu membawa kapal ke layar.
            LaunchedEffect(mintaGeser) {
                if (mintaGeser == 0) return@LaunchedEffect
                geserKe(titikKapal)
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
                                tr("Peta perairan Antartika, stage 11 sampai 20")
                            } else {
                                tr("Peta perairan Samudra Pasifik, stage 1 sampai 10")
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
                                    t("BOSS"),
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
                                    t("ANTARTIKA"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = warna.petaTeks,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.4.sp,
                                )
                                Text(
                                    t(if (antartikaTerbuka) "rute terbuka" else "terkunci"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = warna.petaTeksRedup.copy(alpha = 0.75f),
                                )
                            }
                        }
                    }

                    if (petaAktif == PetaAktif.ANTARTIKA) {
                        val b = PetaGeometri.gerbangBermuda
                        NodeBermuda(
                            terpilih = terpilih == PilihanPeta.Bermuda,
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                terpilih = PilihanPeta.Bermuda
                            },
                            modifier = Modifier.tempatNode(b, lebar, tinggi, 58.dp),
                        )
                        Box(
                            Modifier
                                .offset(
                                    x = lebar * (b.x / PetaGeometri.LEBAR_DUNIA) - 74.dp,
                                    y = tinggi * (b.y / PetaGeometri.TINGGI_DUNIA) + 34.dp,
                                )
                                .width(148.dp)
                                .background(warna.petaLabelLatar, RoundedCornerShape(6.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    t("SEGITIGA BERMUDA"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = warna.petaTeks,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                )
                                Text(
                                    t("segera hadir · ketuk untuk pratinjau"),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = warna.petaTeksRedup,
                                    textAlign = TextAlign.Center,
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
                            t(if (petaAktif == PetaAktif.PASIFIK) "PERAIRAN ANTARTIKA" else "PERAIRAN BERIKUTNYA"),
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
                            t("Titik awal pelayaran"),
                            Modifier
                                .offset(
                                    x = lebar * (awal.x / PetaGeometri.LEBAR_DUNIA) + 22.dp,
                                    y = tinggi * (awal.y / PetaGeometri.TINGGI_DUNIA) - 18.dp,
                                )
                                .size(34.dp),
                        )
                    }
                    KapalPeta(
                        titik = titikKapal,
                        lebar = lebar,
                        tinggi = tinggi,
                        // Kapal hanya berlayar saat pemain mengetuk node. Saat peta dibuka,
                        // kapal langsung muncul di posisi terakhir tanpa animasi dari stage 1.
                        animasi = kapalPilihan != null,
                    )
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
            // Toolbar pergantian peta: judul peta yang sedang dilihat + tab Pasifik / Antartika.
            // Mengetuk peta lain memicu animasi berlayar. Antartika yang masih terkunci membuka
            // lembar gerbang (syarat membuka) tanpa berlayar.
            AreaToolbarPeta(
                tampil = toolbarTampil,
                aktif = petaAktif,
                antartikaTerbuka = antartikaTerbuka,
                onPilih = { tujuan ->
                    c.audio.play(Sfx.Klik)
                    // Toolbar ditutup lagi setelah memilih agar peta langsung terlihat penuh.
                    toolbarTampil = false
                    if (tujuan == PetaAktif.ANTARTIKA && !antartikaTerbuka) {
                        terpilih = PilihanPeta.Gerbang
                    } else {
                        mulaiBerlayar(tujuan)
                    }
                },
                onBermuda = {
                    c.audio.play(Sfx.Klik)
                    toolbarTampil = false
                    terpilih = PilihanPeta.Bermuda
                },
                onSembunyikan = {
                    c.audio.play(Sfx.Klik)
                    toolbarTampil = false
                },
                onTampilkan = {
                    c.audio.play(Sfx.Klik)
                    toolbarTampil = true
                },
            )
            ChipPeta("$jumlahSelesaiTotal/$totalStage", t("stage selesai"), alignEnd = true)
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClickLabel = t("Geser ke soal terakhir")) {
                    c.audio.play(Sfx.Klik)
                    mintaGeser++
                }
                .semantics { contentDescription = tr("Geser peta ke soal terakhir dikerjakan") },
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
                        mulaiBerlayar(PetaAktif.ANTARTIKA)
                    },
                )
                PilihanPeta.Bermuda -> LembarBermuda(
                    jumlahSelesai = jumlahSelesaiAntartika,
                    onTutup = { terpilih = null },
                )
                null -> Unit
            }
        }

        berlayarKe?.let { tujuan ->
            TransisiBerlayar(
                tujuan = tr(namaPeta(tujuan)),
                suasana = if (tujuan == PetaAktif.ANTARTIKA) SuasanaLaut.ES else SuasanaLaut.TROPIS,
                kekanan = tujuan == PetaAktif.ANTARTIKA,
                onGanti = { petaPilihan = tujuan },
                onSelesai = { berlayarKe = null },
            )
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
                        .semantics { contentDescription = listOfNotNull(judul, sub).joinToString(", ") }
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

private fun namaPeta(p: PetaAktif): String = when (p) {
    PetaAktif.PASIFIK -> "Samudra Pasifik"
    PetaAktif.ANTARTIKA -> "Lautan Antartika"
}

private fun rentangPeta(p: PetaAktif): String = when (p) {
    PetaAktif.PASIFIK -> "Stage 1–10"
    PetaAktif.ANTARTIKA -> "Stage 11–20"
}

/**
 * Toolbar peta dengan animasi sembunyi/tampil. Sengaja berupa fungsi tersendiri (bukan di dalam `Row`
 * header): di dalam `Row`, `AnimatedVisibility` ambigu antara versi biasa dan `RowScope.AnimatedVisibility`.
 */
@Composable
private fun AreaToolbarPeta(
    tampil: Boolean,
    aktif: PetaAktif,
    antartikaTerbuka: Boolean,
    onPilih: (PetaAktif) -> Unit,
    onBermuda: () -> Unit,
    onSembunyikan: () -> Unit,
    onTampilkan: () -> Unit,
) {
    Box {
        AnimatedVisibility(
            visible = tampil,
            enter = fadeIn(tween(140)) + expandVertically(tween(160), expandFrom = Alignment.Top),
            exit = fadeOut(tween(90)) + shrinkVertically(tween(120), shrinkTowards = Alignment.Top),
        ) {
            ToolbarPeta(
                aktif = aktif,
                antartikaTerbuka = antartikaTerbuka,
                onPilih = onPilih,
                onBermuda = onBermuda,
                onSembunyikan = onSembunyikan,
            )
        }
        AnimatedVisibility(
            visible = !tampil,
            enter = fadeIn(tween(140)),
            exit = fadeOut(tween(90)),
        ) {
            PenandaToolbar(aktif = aktif, onClick = onTampilkan)
        }
    }
}

/**
 * Toolbar perpindahan peta. Judul kecil "DAFTAR PETA" di atas, lalu daftar peta yang disusun ke bawah
 * (bukan ke samping), sehingga menambah peta baru hanya menambah baris dan tidak melebarkan toolbar
 * sampai menutupi peta. Daftar dibatasi tingginya dan bisa digulir bila peta sudah banyak.
 * Peta yang belum terbuka memakai ikon gembok; Bermuda ditandai "Segera hadir".
 */
@Composable
private fun ToolbarPeta(
    aktif: PetaAktif,
    antartikaTerbuka: Boolean,
    onPilih: (PetaAktif) -> Unit,
    onBermuda: () -> Unit,
    onSembunyikan: () -> Unit,
) {
    val warna = LocalWarna.current
    val tr = LocalTeks.current
    val bentuk = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .width(196.dp)
            .clip(bentuk)
            .background(warna.petaChipLatar)
            .border(1.dp, warna.petaTeks.copy(alpha = 0.12f), bentuk)
            .padding(vertical = 6.dp),
    ) {
        Row(
            Modifier.padding(start = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                t("DAFTAR PETA"),
                style = MaterialTheme.typography.labelSmall,
                color = warna.petaTeksRedup,
                letterSpacing = 1.4.sp,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = tr.format(
                            "Daftar peta. Peta aktif: {}",
                            tr[namaPeta(aktif)],
                        )
                    },
            )
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = t("Sembunyikan toolbar peta"),
                        onClick = onSembunyikan,
                    )
                    .semantics { contentDescription = tr("Sembunyikan toolbar peta") },
                contentAlignment = Alignment.Center,
            ) {
                Text("▴", color = warna.petaTeksRedup, fontSize = 15.sp)
            }
        }
        Box(
            Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(warna.petaTeks.copy(alpha = 0.10f)),
        )
        Column(
            Modifier
                .heightIn(max = 168.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            BarisPeta(
                teks = t("Samudra Pasifik"),
                keterangan = t(rentangPeta(PetaAktif.PASIFIK)),
                ikon = Art.ikon_air_tenang,
                aktif = aktif == PetaAktif.PASIFIK,
            ) { onPilih(PetaAktif.PASIFIK) }
            BarisPeta(
                teks = t("Lautan Antartika"),
                keterangan = t(if (antartikaTerbuka) rentangPeta(PetaAktif.ANTARTIKA) else "Terkunci"),
                ikon = Art.ikon_air_dingin,
                aktif = aktif == PetaAktif.ANTARTIKA,
                terkunci = !antartikaTerbuka,
            ) { onPilih(PetaAktif.ANTARTIKA) }
            BarisPeta(
                teks = t("Segitiga Bermuda"),
                keterangan = t("Segera hadir"),
                ikon = Art.gerbang_bermuda,
                aktif = false,
                terkunci = true,
                segeraHadir = true,
                onClick = onBermuda,
            )
        }
    }
}

/** Penanda kecil pengganti toolbar saat disembunyikan: ikon peta aktif + panah. Ketuk untuk menampilkan
 * toolbar lagi.
 */
@Composable
private fun PenandaToolbar(aktif: PetaAktif, onClick: () -> Unit) {
    val warna = LocalWarna.current
    val tr = LocalTeks.current
    val bentuk = RoundedCornerShape(50)
    Row(
        Modifier
            .clip(bentuk)
            .background(warna.petaChipLatar)
            .border(1.dp, warna.petaTeks.copy(alpha = 0.18f), bentuk)
            .clickable(
                role = Role.Button,
                onClickLabel = t("Tampilkan daftar peta"),
                onClick = onClick,
            )
            .semantics {
                contentDescription = tr.format(
                    "Tampilkan daftar peta. Peta aktif: {}",
                    tr[namaPeta(aktif)],
                )
            }
            .defaultMinSize(minHeight = 36.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GambarKontras(
            if (aktif == PetaAktif.ANTARTIKA) Art.ikon_air_dingin else Art.ikon_air_tenang,
            null,
            Modifier.size(16.dp),
        )
        // Nama peta aktif ditulis, bukan hanya ikon: ikon gelombang mudah tertukar dengan peta lain
        // sehingga pemain bisa mengira peta yang terbuka masih peta sebelumnya. Ikonnya memakai aset
        // yang sama dengan baris di daftar peta, jadi chip dan daftar tidak pernah beda lambang.
        Text(
            t(namaPeta(aktif)),
            color = warna.petaTeks,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
        Text("▾", color = warna.petaTeksRedup, fontSize = 13.sp)
    }
}

/**
 * Satu baris peta pada toolbar: ikon (atau gembok bila belum terbuka), nama, dan keterangan singkat.
 * Baris peta yang sedang dibuka diberi latar aksen dan tidak bisa diketuk lagi.
 */
@Composable
private fun BarisPeta(
    teks: String,
    keterangan: String,
    @DrawableRes ikon: Int,
    aktif: Boolean,
    terkunci: Boolean = false,
    segeraHadir: Boolean = false,
    onClick: () -> Unit,
) {
    val warna = LocalWarna.current
    val primer = MaterialTheme.colorScheme.primary
    val bentuk = RoundedCornerShape(10.dp)
    val tr = LocalTeks.current
    val status = when {
        segeraHadir -> tr("segera hadir, belum bisa dimainkan")
        terkunci -> tr("terkunci")
        aktif -> tr("sedang dibuka")
        else -> tr("ketuk untuk berlayar")
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(bentuk)
            .then(if (aktif) Modifier.background(primer.copy(alpha = 0.18f)) else Modifier)
            .then(
                if (aktif) {
                    Modifier.border(1.dp, primer.copy(alpha = 0.5f), bentuk)
                } else {
                    Modifier
                },
            )
            .then(
                if (aktif) {
                    Modifier
                } else {
                    Modifier
                        .clickable(
                            role = Role.Tab,
                            onClickLabel = tr.format(
                                if (terkunci) "Lihat info peta {}" else "Berlayar ke peta {}",
                                teks,
                            ),
                            onClick = onClick,
                        )
                },
            )
            .semantics {
                selected = aktif
                contentDescription = tr.format("Peta {}, {}", teks, status)
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (terkunci) {
            Gambar(
                if (warna.gelap) Art.ikon_gembok else Art.ikon_gembok_terang,
                null,
                Modifier.size(14.dp),
            )
        } else {
            GambarKontras(ikon, null, Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                teks,
                style = MaterialTheme.typography.labelMedium,
                color = if (aktif || !terkunci) warna.petaTeks else warna.petaTeksRedup,
                fontWeight = if (aktif) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                keterangan,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 11.sp),
                color = if (segeraHadir) warna.petaTeksRedup else warna.petaTeksRedup.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Node pratinjau Segitiga Bermuda di ujung rute Antartika, diselimuti kabut yang berdenyut pelan. */
@Composable
private fun NodeBermuda(
    terpilih: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val warna = LocalWarna.current
    val tr = LocalTeks.current
    val kurangiGerak = remember { !ValueAnimator.areAnimatorsEnabled() }
    val transisi = rememberInfiniteTransition(label = "bermuda")
    val kabut by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Reverse),
        label = "kabut",
    )
    Box(modifier.size(58.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.requiredSize(120.dp)) {
            val k = if (kurangiGerak) 0.5f else kabut
            for (i in 0 until 3) {
                drawCircle(
                    Color(0xFF7CF2C8).copy(alpha = (0.18f - 0.045f * i) * (0.6f + 0.8f * k)),
                    radius = size.minDimension * (0.28f + 0.09f * i + 0.03f * k),
                )
            }
        }
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .semantics {
                    contentDescription = tr("Segitiga Bermuda, segera hadir. Ketuk untuk melihat pratinjau")
                },
            contentAlignment = Alignment.Center,
        ) {
            Gambar(Art.gerbang_bermuda, null, Modifier.fillMaxSize())
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
}

/**
 * Lembar pratinjau Segitiga Bermuda. Hanya teaser: peta ini belum bisa dimainkan, jadi tidak ada
 * tombol berlayar dan tidak ada syarat yang harus dipenuhi.
 */
@Composable
private fun LembarBermuda(jumlahSelesai: Int, onTutup: () -> Unit) {
    val warna = LocalWarna.current
    val tr = LocalTeks.current
    Kartu(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        warna = warna.kartu.copy(alpha = 0.97f),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        t("PERAIRAN BERIKUTNYA"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                    )
                    Text(t("Segitiga Bermuda"), style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    "✕",
                    Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onTutup)
                        .padding(8.dp)
                        .semantics { contentDescription = tr("Tutup pratinjau Segitiga Bermuda") },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(14.dp)),
            ) {
                Gambar(
                    Art.preview_bermuda,
                    t("Pratinjau Segitiga Bermuda: laut berkabut dengan segitiga bercahaya dan kapal hantu"),
                    Modifier.fillMaxSize(),
                    ContentScale.Crop,
                )
                Text(
                    t("SEGERA HADIR"),
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(Color(0xE60B2F36), RoundedCornerShape(50))
                        .border(1.dp, Color(0xFF7CF2C8), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    color = Color(0xFF7CF2C8),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                )
            }
            Text(
                t(
                    "Perairan paling misterius: kompas berputar tanpa arah, kabut menelan kapal, dan arus sulit ditebak. " +
                        "Di sinilah pelayaranmu setelah Antartika berlanjut.",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                if (jumlahSelesai >= PetaGeometri.JUMLAH_STAGE) {
                    t("Pelayaran Antartika selesai. Nantikan pembaruan berikutnya.")
                } else {
                    t(
                        "Kemajuan Antartika: {}/{} stage. Selesaikan semuanya sebelum peta ini dibuka.",
                        jumlahSelesai,
                        PetaGeometri.JUMLAH_STAGE,
                    )
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
            TombolUtama(teks = t("Mengerti"), onClick = onTutup)
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
            color = warna.petaGaris.copy(alpha = if (warna.gelap) 0.55f else 0.80f),
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
    val tr = LocalTeks.current
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
        !buka -> tr("terkunci")
        selesai -> tr("selesai")
        else -> tr("aktif")
    }

    Box(
        modifier
            .size(ukuran)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = tr.format(
                    "Stage {}, {}, {}, skor {} persen",
                    stage.nomor,
                    stage.nama,
                    status,
                    stage.skor_terbaik,
                )
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
    val tr = LocalTeks.current
    Box(
        modifier
            .size(58.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = tr(
                    if (terbuka) "Gerbang Antartika, rute terbuka" else "Gerbang Antartika, terkunci",
                )
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
private fun KapalPeta(
    titik: TitikPeta,
    lebar: Dp,
    tinggi: Dp,
    animasi: Boolean,
) {
    val transisi = rememberInfiniteTransition(label = "kapal-peta")
    val naikTurun by transisi.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing), RepeatMode.Reverse),
        label = "ombak-kapal",
    )
    // Posisi dianimasikan hanya saat pemain mengetuk node. Pada pemuatan data pertama dan
    // perpindahan peta, posisi langsung dipasang (snap) agar kapal tidak terlihat berlayar
    // dari stage awal ke stage terakhir.
    val xTarget = lebar * (titik.x / PetaGeometri.LEBAR_DUNIA) - 34.dp
    val yTarget = tinggi * (titik.y / PetaGeometri.TINGGI_DUNIA) - 82.dp
    val spesifikasi: AnimationSpec<Dp> = if (animasi) tween(420) else snap()
    val x by animateDpAsState(xTarget, spesifikasi, label = "kapal-x")
    val y by animateDpAsState(yTarget, spesifikasi, label = "kapal-y")
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
        Gambar(Art.kapal_peta, t("Kapal pemain"), Modifier.size(68.dp))
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
    val tr = LocalTeks.current
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
                        t(if (stage.tipe == "boss") "STAGE {} · BOSS" else "STAGE {}", stage.nomor),
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
                        .semantics { contentDescription = tr("Tutup info stage") },
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
                    AuraIkan(ikan.tier, Modifier.size(68.dp)) {
                        GambarKontras(gambarIkanKecil(ikan.gambar), ikan.nama, Modifier.size(58.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(ikan.nama, style = MaterialTheme.typography.titleMedium)
                        ChipTier(ikan.tier)
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        t(
                            when {
                                !buka -> "Terkunci"
                                selesai -> "Selesai"
                                stage.skor_terbaik > 0 -> "Skor terbaik"
                                else -> "Belum dimulai"
                            },
                        ),
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
                    t("Selesaikan Stage {} untuk membuka rute ini.", stage.nomor - 1),
                    style = MaterialTheme.typography.bodySmall,
                    color = warna.bahayaTeks,
                )
            }
            Spacer(Modifier.height(2.dp))
            TombolUtama(
                teks = t(
                    when {
                        !buka -> "Terkunci"
                        selesai -> "Coba Tantang Ulang"
                        stage.tipe == "boss" -> "Hadapi Boss"
                        else -> "Berlayar"
                    },
                ),
                onClick = onBerlayar,
                enabled = buka,
            )
        }
    }
}

@Composable
private fun LembarGerbang(terbuka: Boolean, onTutup: () -> Unit, onMasuk: () -> Unit) {
    val warna = LocalWarna.current
    val tr = LocalTeks.current
    Kartu(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        warna = warna.kartu.copy(alpha = 0.97f),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("PERAIRAN BERIKUTNYA"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Text(t("Antartika"), style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    "✕",
                    Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onTutup)
                        .padding(8.dp)
                        .semantics { contentDescription = tr("Tutup info gerbang") },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GambarKontras(Art.gunung_es, t("Gunung es"), Modifier.size(62.dp))
                Text(
                    if (terbuka) {
                        t("Dingin, es mengapung. Seluruh Samudra Pasifik selesai — rute ke 10 stage Antartika sudah terbuka.")
                    } else {
                        t("Dingin, es mengapung. Selesaikan seluruh stage 1-10 di Samudra Pasifik untuk membuka rute ini.")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TombolUtama(
                teks = t(if (terbuka) "Berlayar ke Antartika" else "Terkunci"),
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

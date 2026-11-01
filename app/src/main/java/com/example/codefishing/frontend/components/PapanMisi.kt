package com.example.codefishing.frontend.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.expert.Arah
import com.example.codefishing.expert.MisiPeta
import com.example.codefishing.expert.SandboxRun
import com.example.codefishing.expert.py.Langkah
import com.example.codefishing.ui.theme.LocalWarna
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class JenisLempar { Ikan, Sampah, Kosong }

private sealed interface AksiPapan {
    val tick: Int

    data class Gerak(
        val dariX: Int,
        val dariY: Int,
        val keX: Int,
        val keY: Int,
        val arah: Arah?,
        val valid: Boolean,
        override val tick: Int,
    ) : AksiPapan

    data class Lempar(
        val x: Int,
        val y: Int,
        val jenis: JenisLempar,
        val namaIkan: String?,
        override val tick: Int,
    ) : AksiPapan

    data class Lihat(val x: Int, val y: Int, override val tick: Int) : AksiPapan
}

private sealed interface EfekPapan {
    val x: Int
    val y: Int

    data class Lempar(
        override val x: Int,
        override val y: Int,
        val jenis: JenisLempar,
        val namaIkan: String?,
    ) : EfekPapan

    data class Lihat(override val x: Int, override val y: Int) : EfekPapan
}

private fun bangunAksi(trace: List<Langkah>): List<AksiPapan> {
    var x = 0
    var y = 0
    return buildList {
        trace.forEach { langkah ->
            when (langkah.aksi) {
                "gerak" -> {
                    val valid = langkah.pesan != "Tepi laut"
                    val arah = if (valid) Arah.entries.firstOrNull { it.name == langkah.pesan } else null
                    add(AksiPapan.Gerak(x, y, langkah.x, langkah.y, arah, valid, langkah.tick))
                    if (valid) {
                        x = langkah.x
                        y = langkah.y
                    }
                }
                "lempar" -> {
                    val pesan = langkah.pesan ?: ""
                    val jenis = when {
                        pesan.startsWith("Dapat ") -> JenisLempar.Ikan
                        pesan.startsWith("Sampah") -> JenisLempar.Sampah
                        else -> JenisLempar.Kosong
                    }
                    add(
                        AksiPapan.Lempar(
                            x = langkah.x,
                            y = langkah.y,
                            jenis = jenis,
                            namaIkan = if (jenis == JenisLempar.Ikan) pesan.removePrefix("Dapat ").trim() else null,
                            tick = langkah.tick,
                        ),
                    )
                }
                "lihat" -> add(AksiPapan.Lihat(langkah.x, langkah.y, langkah.tick))
            }
        }
    }
}

private fun semuaTerangkat(aksi: List<AksiPapan>, ukuran: Int): Set<Int> =
    aksi.filterIsInstance<AksiPapan.Lempar>()
        .filter { it.jenis != JenisLempar.Kosong }
        .map { it.y * ukuran + it.x }
        .toSet()

/**
 * Papan Mode Expert dengan replay gerak kapal dan animasi tangkapan.
 * [onSelesaiAnimasi] dipanggil setelah replay selesai (atau langsung saat reduce motion),
 * supaya kartu hasil tidak menutupi animasi.
 */
@Composable
fun PapanMisi(
    misi: MisiPeta,
    hasil: SandboxRun?,
    petaIkan: Map<String, IkanEntity>,
    memuat: Boolean,
    modifier: Modifier = Modifier,
    haptikAktif: Boolean = true,
    onSelesaiAnimasi: () -> Unit = {},
) {
    val c = LocalContainer.current
    val haptik = LocalHapticFeedback.current
    val warna = LocalWarna.current
    val kurangiGerak = remember { !ValueAnimator.areAnimatorsEnabled() }
    val aksi = remember(hasil) { hasil?.let { bangunAksi(it.hasil.trace) }.orEmpty() }
    val terangkatSemua = remember(hasil, misi.grid) { semuaTerangkat(aksi, misi.grid) }

    var posX by remember(hasil) { mutableFloatStateOf(0f) }
    var posY by remember(hasil) { mutableFloatStateOf(0f) }
    var gerakDariX by remember(hasil) { mutableFloatStateOf(0f) }
    var gerakDariY by remember(hasil) { mutableFloatStateOf(0f) }
    var gerakKeX by remember(hasil) { mutableFloatStateOf(0f) }
    var gerakKeY by remember(hasil) { mutableFloatStateOf(0f) }
    var sedangGerak by remember(hasil) { mutableStateOf(false) }
    var arah by remember(hasil) { mutableStateOf(Arah.Timur) }
    var terangkat by remember(hasil) { mutableStateOf(emptySet<Int>()) }
    var efek by remember(hasil) { mutableStateOf<EfekPapan?>(null) }
    var tickKini by remember(hasil) { mutableIntStateOf(0) }
    var tangkapKini by remember(hasil) { mutableIntStateOf(0) }
    var sampahKini by remember(hasil) { mutableIntStateOf(0) }
    var selesai by remember(hasil) { mutableStateOf(hasil == null) }
    var memutar by remember(hasil) { mutableStateOf(hasil != null && !kurangiGerak) }
    var lewati by remember(hasil) { mutableStateOf(false) }

    val gerakProgres = remember { Animatable(0f) }
    val efekProgres = remember { Animatable(0f) }
    val getar = remember { Animatable(0f) }
    val konfeti = remember { Animatable(0f) }

    LaunchedEffect(hasil, kurangiGerak) {
        if (hasil == null) {
            posX = 0f
            posY = 0f
            arah = Arah.Timur
            terangkat = emptySet()
            efek = null
            sedangGerak = false
            tickKini = 0
            tangkapKini = 0
            sampahKini = 0
            selesai = true
            memutar = false
            return@LaunchedEffect
        }

        val daftar = bangunAksi(hasil.hasil.trace)
        if (kurangiGerak || daftar.isEmpty()) {
            posX = hasil.dunia.x.toFloat()
            posY = hasil.dunia.y.toFloat()
            terangkat = terangkatSemua
            efek = null
            sedangGerak = false
            tickKini = hasil.hasil.ticks
            tangkapKini = hasil.hasil.tangkapan.size
            sampahKini = hasil.hasil.sampah
            selesai = true
            memutar = false
            onSelesaiAnimasi()
            return@LaunchedEffect
        }

        posX = 0f
        posY = 0f
        arah = Arah.Timur
        terangkat = emptySet()
        efek = null
        sedangGerak = false
        tickKini = 0
        tangkapKini = 0
        sampahKini = 0
        selesai = false
        memutar = true
        lewati = false

        val durasiGerak = (7600f / daftar.size.coerceAtLeast(1))
            .coerceIn(55f, 220f)
            .toInt()
        val faktorDurasi = (daftar.size / 12f).coerceIn(1f, 3f)
        val durasiLempar = (540f / faktorDurasi).coerceAtLeast(190f).toInt()
        val durasiLihat = (360f / faktorDurasi).coerceAtLeast(110f).toInt()

        for (item in daftar) {
            if (lewati) break
            when (item) {
                is AksiPapan.Gerak -> {
                    tickKini = item.tick
                    if (!item.valid) {
                        getar.snapTo(1f)
                        getar.animateTo(0f, tween(320, easing = LinearEasing))
                        delay(120)
                    } else {
                        item.arah?.let { arah = it }
                        gerakDariX = posX
                        gerakDariY = posY
                        gerakKeX = item.keX.toFloat()
                        gerakKeY = item.keY.toFloat()
                        sedangGerak = true
                        gerakProgres.snapTo(0f)
                        gerakProgres.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durasiGerak, easing = FastOutSlowInEasing),
                        )
                        posX = item.keX.toFloat()
                        posY = item.keY.toFloat()
                        sedangGerak = false
                        delay(28)
                    }
                }
                is AksiPapan.Lempar -> {
                    tickKini = item.tick
                    c.audio.play(Sfx.Cipratan)
                    if (haptikAktif) haptik.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (item.jenis != JenisLempar.Kosong) {
                        terangkat = terangkat + (item.y * misi.grid + item.x)
                    }
                    efek = EfekPapan.Lempar(item.x, item.y, item.jenis, item.namaIkan)
                    efekProgres.snapTo(0f)
                    efekProgres.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durasiLempar, easing = FastOutSlowInEasing),
                    )
                    efek = null
                    if (item.jenis == JenisLempar.Ikan) {
                        tangkapKini++
                        c.audio.play(Sfx.IkanBaru)
                        if (haptikAktif) haptik.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else if (item.jenis == JenisLempar.Sampah) {
                        sampahKini++
                        c.audio.play(Sfx.Salah)
                        if (haptikAktif) haptik.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    delay(40)
                }
                is AksiPapan.Lihat -> {
                    tickKini = item.tick
                    efek = EfekPapan.Lihat(item.x, item.y)
                    efekProgres.snapTo(0f)
                    efekProgres.animateTo(1f, tween(durasiLihat, easing = LinearEasing))
                    efek = null
                    delay(24)
                }
            }
        }

        posX = hasil.dunia.x.toFloat()
        posY = hasil.dunia.y.toFloat()
        terangkat = terangkatSemua
        efek = null
        sedangGerak = false
        tickKini = hasil.hasil.ticks
        tangkapKini = hasil.hasil.tangkapan.size
        sampahKini = hasil.hasil.sampah
        selesai = true
        memutar = false
        onSelesaiAnimasi()
    }

    LaunchedEffect(hasil, selesai) {
        if (selesai && hasil?.evaluasi?.sukses == true && !kurangiGerak) {
            konfeti.snapTo(0f)
            konfeti.animateTo(1f, tween(1400, easing = LinearEasing))
        } else {
            konfeti.snapTo(0f)
        }
    }

    val posisiX = if (sedangGerak) lerpF(gerakDariX, gerakKeX, gerakProgres.value) else posX
    val posisiY = if (sedangGerak) lerpF(gerakDariY, gerakKeY, gerakProgres.value) else posY
    val selSekarang = if (selesai && hasil != null) {
        (0 until misi.grid * misi.grid).map { index ->
            hasil.dunia.petakDi(index % misi.grid, index / misi.grid).isi
        }
    } else {
        misi.cells
    }
    val ikanAwal = remember(misi.grid, misi.cells) {
        misi.cells.count { it.isNotEmpty() && it != "sampah" }
    }
    val sisaIkan = if (selesai && hasil != null) {
        hasil.dunia.sisaIkan
    } else {
        (ikanAwal - tangkapKini).coerceAtLeast(0)
    }

    val statusTeks = when {
        memuat -> "MEMUAT"
        hasil == null -> "SIAP"
        memutar -> "MEMUTAR"
        !hasil.hasil.berhasil -> "ERROR"
        hasil.evaluasi.sukses -> "BERHASIL"
        else -> "BELUM TERCAPAI"
    }
    val statusWarna = when {
        memuat -> warna.petaTeksRedup
        hasil == null -> warna.petaTeksRedup
        memutar -> warna.peringatanTeks
        !hasil.hasil.berhasil -> warna.bahayaTeks
        hasil.evaluasi.sukses -> warna.suksesTeks
        else -> warna.peringatanTeks
    }
    val judulPapan = when {
        hasil == null -> "Peta Misi"
        memutar -> "Simulasi"
        else -> "Posisi Akhir"
    }

    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    judulPapan,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    "${misi.grid} × ${misi.grid} petak · $tickKini tick",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusPapanStatus(statusTeks, statusWarna)
        }

        if (memuat) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(warna.kodeLatar, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Memuat peta misi...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            PapanGrid(
                ukuran = misi.grid,
                cells = selSekarang,
                posisiX = posisiX,
                posisiY = posisiY,
                arah = arah,
                terangkat = terangkat,
                efek = efek,
                efekT = efekProgres.value,
                guncang = getar.value,
                bergerak = sedangGerak,
                gerakT = if (sedangGerak) gerakProgres.value else 0f,
                konfetiT = konfeti.value,
                petaIkan = petaIkan,
                error = hasil != null && !hasil.hasil.berhasil,
            )
        }

        Text(
            when {
                memuat -> "Menyiapkan petak laut..."
                hasil == null -> "Kapal mulai di (0, 0). Baris bawah = start."
                memutar -> "Memutar simulasi program... tekan Lewati untuk langsung ke hasil."
                else -> "Kapal berakhir di (${hasil.dunia.x}, ${hasil.dunia.y}) · $sisaIkan ikan tersisa."
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Bingkai oranye = kapal · Petak berisi ikan = tangkapan · Sepatu bot = sampah",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        )

        if (memutar) {
            if (aksi.size > 200) {
                Text(
                    "Program panjang: animasi dipercepat. Tekan Lewati untuk langsung ke hasil.",
                    style = MaterialTheme.typography.labelSmall,
                    color = warna.peringatanTeks,
                )
            }
            Spacer(Modifier.height(2.dp))
            TombolSekunder(
                teks = "Lewati animasi",
                onClick = { lewati = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (hasil != null && !memutar) {
            Text(
                hasil.hasil.pesanError ?: hasil.evaluasi.alasan,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    !hasil.hasil.berhasil -> warna.bahayaTeks
                    hasil.evaluasi.sukses -> warna.suksesTeks
                    else -> warna.peringatanTeks
                },
            )
        }
    }
}

@Composable
private fun StatusPapanStatus(teks: String, warna: Color) {
    Text(
        teks,
        Modifier
            .background(warna.copy(alpha = 0.14f), RoundedCornerShape(50))
            .border(1.dp, warna.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = warna,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun PapanGrid(
    ukuran: Int,
    cells: List<String>,
    posisiX: Float,
    posisiY: Float,
    arah: Arah,
    terangkat: Set<Int>,
    efek: EfekPapan?,
    efekT: Float,
    guncang: Float,
    bergerak: Boolean,
    gerakT: Float,
    konfetiT: Float,
    petaIkan: Map<String, IkanEntity>,
    error: Boolean,
) {
    val warna = LocalWarna.current
    val density = LocalDensity.current
    val transisi = rememberInfiniteTransition(label = "papan-misi")
    val ombak by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6200, easing = LinearEasing)),
        label = "ombak-papan",
    )
    val gap = 3.dp
    val kapalX = posisiX.roundToInt().coerceIn(0, ukuran - 1)
    val kapalY = posisiY.roundToInt().coerceIn(0, ukuran - 1)

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .background(warna.kodeLatar, RoundedCornerShape(14.dp))
            .border(1.dp, if (error) warna.bahayaTeks.copy(alpha = 0.7f) else Color.Transparent, RoundedCornerShape(14.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        val sel = ((maxWidth - gap * (ukuran - 1).toFloat()) / ukuran.toFloat()).coerceIn(30.dp, 54.dp)
        val board = sel * ukuran.toFloat() + gap * (ukuran - 1).toFloat()
        val selPx = with(density) { sel.toPx() }
        val gapPx = with(density) { gap.toPx() }
        val boatSize = sel * 0.82f
        val boatPx = with(density) { boatSize.toPx() }
        val boatX = posisiX * (selPx + gapPx) + selPx / 2f
        val boatY = (ukuran - 1 - posisiY) * (selPx + gapPx) + selPx / 2f
        val bob = sin(ombak * 2f * PI.toFloat()) * 1.3f * with(density) { 1.dp.toPx() }
        val guncangPx = sin(guncang * PI.toFloat() * 10f) * with(density) { 4.dp.toPx() } * guncang
        val arahX = when (arah) {
            Arah.Timur -> 1f
            Arah.Barat -> -1f
            else -> 0f
        }
        val arahY = when (arah) {
            Arah.Selatan -> 1f
            Arah.Utara -> -1f
            else -> 0f
        }

        Box(
            Modifier
                .size(board)
                .clip(RoundedCornerShape(10.dp)),
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(gap),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for (y in ukuran - 1 downTo 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        for (x in 0 until ukuran) {
                            val index = y * ukuran + x
                            val isi = cells.getOrElse(index) { "" }
                            val adaKapal = x == kapalX && y == kapalY
                            Box(
                                Modifier.size(sel),
                                contentAlignment = Alignment.Center,
                            ) {
                                Gambar(
                                    when {
                                        adaKapal -> if (warna.gelap) Art.petak_kapal_sekarang else Art.petak_kapal_sekarang_terang
                                        isi == "sampah" -> if (warna.gelap) Art.petak_sampah else Art.petak_sampah_terang
                                        else -> if (warna.gelap) Art.petak_laut else Art.petak_laut_terang
                                    },
                                    null,
                                    Modifier.fillMaxSize(),
                                    ContentScale.FillBounds,
                                )
                                if (isi.isNotEmpty() && isi != "sampah" && index !in terangkat) {
                                    val ikan = petaIkan[isi.lowercase()]
                                    val goyang = sin(ombak * 2f * PI.toFloat() + (x + y * 3) * 0.7f)
                                    Gambar(
                                        gambarIkan(ikan?.gambar),
                                        ikan?.nama ?: isi,
                                        Modifier
                                            .size(if (adaKapal) sel * 0.40f else sel * 0.70f)
                                            .align(if (adaKapal) Alignment.TopStart else Alignment.Center)
                                            .graphicsLayer {
                                                translationY = goyang * 1.4.dp.toPx()
                                                rotationZ = goyang * 2.5f
                                            },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Kilau air tipis di atas petak.
            Canvas(Modifier.matchParentSize()) {
                gambarKilauPapan(warna.ombak, ombak)
            }

            // Efek lempar/lihat berada di koordinat papan, bukan di dalam satu petak.
            Canvas(Modifier.matchParentSize()) {
                val e = efek
                if (e != null) {
                    val pusat = pusatSel(e.x, e.y, ukuran, selPx, gapPx)
                    when (e) {
                        is EfekPapan.Lempar -> {
                            val titikAir = Offset(pusat.x, pusat.y + selPx * 0.15f)
                            gambarTali(
                                mulai = Offset(boatX, boatY),
                                selesai = Offset(titikAir.x, titikAir.y + selPx * 0.22f),
                                kendur = 0f,
                                warna = Color.White,
                                lebar = 1.1f,
                            )
                            gambarRiak(titikAir, efekT, warna.ombak, jumlah = 2)
                            gambarPercikan(titikAir, efekT, Color.White, jumlah = 14)
                        }
                        is EfekPapan.Lihat -> {
                            gambarSonar(pusat, efekT, warna.ombak, selPx * 4.5f)
                        }
                    }
                }
                if (konfetiT in 0.001f..0.999f) {
                    gambarKonfeti(warna.suksesTeks, konfetiT)
                }
            }

            Canvas(Modifier.matchParentSize()) {
                if (bergerak) {
                    gambarJejakKapal(
                        pusat = Offset(boatX, boatY),
                        arahX = arahX,
                        arahY = arahY,
                        intensitas = 1f,
                        warna = warna.ombak,
                    )
                }
            }

            Gambar(
                kapalArah(arah),
                "Kapal di (${posisiX.roundToInt()}, ${posisiY.roundToInt()})",
                Modifier
                    .offset {
                        IntOffset(
                            (boatX - boatPx / 2f + guncangPx).roundToInt(),
                            (boatY - boatPx / 2f + bob).roundToInt(),
                        )
                    }
                    .size(boatSize)
                    .graphicsLayer {
                        rotationZ = if (bergerak) sin(gerakT * PI.toFloat()) * 4f else 0f
                    },
            )

            val e = efek
            if (e is EfekPapan.Lempar) {
                val pusat = pusatSel(e.x, e.y, ukuran, selPx, gapPx)
                val p = efekT.coerceIn(0f, 1f)
                val terbangX = lerpF(pusat.x, boatX, p)
                val terbangY = lerpF(pusat.y, boatY, p) - sin(p * PI.toFloat()) * selPx * 0.85f
                if (e.jenis == JenisLempar.Ikan) {
                    val ikan = e.namaIkan?.lowercase()?.let { petaIkan[it] }
                    Gambar(
                        gambarIkan(ikan?.gambar),
                        "Ikan ${e.namaIkan ?: ""} tertangkap",
                        Modifier
                            .offset {
                                IntOffset(
                                    (terbangX - selPx * 0.31f).roundToInt(),
                                    (terbangY - selPx * 0.31f).roundToInt(),
                                )
                            }
                            .size(sel * 0.62f)
                            .graphicsLayer {
                                alpha = (1f - p * 0.30f).coerceIn(0f, 1f)
                                rotationZ = p * 220f
                                scaleX = 1f - p * 0.18f
                                scaleY = 1f - p * 0.18f
                            },
                    )
                } else if (e.jenis == JenisLempar.Sampah) {
                    Gambar(
                        Art.petak_sampah,
                        "Sampah terangkat",
                        Modifier
                            .offset {
                                IntOffset(
                                    (terbangX - selPx * 0.28f).roundToInt(),
                                    (terbangY - selPx * 0.28f).roundToInt(),
                                )
                            }
                            .size(sel * 0.56f)
                            .graphicsLayer {
                                alpha = (1f - p * 0.35f).coerceIn(0f, 1f)
                                rotationZ = p * 180f
                            },
                    )
                }
                val badgeAlpha = ((p - 0.35f) / 0.65f).coerceIn(0f, 1f)
                if (badgeAlpha > 0.01f) {
                    Text(
                        if (e.jenis == JenisLempar.Sampah) "sampah" else if (e.jenis == JenisLempar.Ikan) "+1" else "kosong",
                        color = if (e.jenis == JenisLempar.Sampah) warna.bahayaTeks else warna.suksesTeks,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (boatX - 22.dp.toPx()).roundToInt(),
                                    (boatY - selPx * 0.80f).roundToInt(),
                                )
                            }
                            .graphicsLayer { alpha = badgeAlpha }
                            .background(warna.kartu.copy(alpha = 0.90f), RoundedCornerShape(50))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
            }
        }
    }
}

private fun pusatSel(x: Int, y: Int, ukuran: Int, selPx: Float, gapPx: Float): Offset =
    Offset(
        x * (selPx + gapPx) + selPx / 2f,
        (ukuran - 1 - y) * (selPx + gapPx) + selPx / 2f,
    )

private fun kapalArah(arah: Arah): Int = when (arah) {
    Arah.Utara -> Art.kapal_atas_utara
    Arah.Timur -> Art.kapal_atas_timur
    Arah.Selatan -> Art.kapal_atas_selatan
    Arah.Barat -> Art.kapal_atas_barat
}

private fun lerpF(mulai: Float, selesai: Float, t: Float): Float = mulai + (selesai - mulai) * t
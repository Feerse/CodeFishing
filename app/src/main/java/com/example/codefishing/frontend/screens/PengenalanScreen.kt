package com.example.codefishing.frontend.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.GambarKontras
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkanKecil
import com.example.codefishing.ui.theme.LocalWarna
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class Halaman(
    val judul: String,
    val isi: String,
    /** Null berarti halaman memakai ilustrasi khusus (lihat [PratinjauAkuarium]). */
    val gambar: Int?,
    val gambarTerang: Int? = gambar,
)

private val halaman = listOf(
    Halaman("Belajar Python sambil memancing", "Semua soal dan kode memakai Python.", Art.logo_codefishing),
    Halaman("Pilih umpan kode yang benar", "Jawaban benar menangkap ikan.", Art.kaleng_umpan),
    Halaman(
        "Tulis programmu sendiri",
        "Mode Expert: kodemu menggerakkan kapal.",
        Art.ilustrasi_editor_kode,
        Art.ilustrasi_editor_kode_terang,
    ),
    // Halaman terakhir memakai pratinjau akuarium berisi ikan, bukan gambar latar kosong.
    Halaman("Kumpulkan ikan di akuarium", "Semua jalan offline, tanpa internet.", null),
)

/** Ikan yang berenang di pratinjau: nama aset, ukuran relatif tinggi panggung, dan lajur vertikal. */
private data class IkanPratinjau(val gambar: String, val ukuran: Float, val lajur: Float, val laju: Float)

private val ikanPratinjau = listOf(
    IkanPratinjau("ikan_hiu_merah", 0.34f, 0.30f, 0.52f),
    IkanPratinjau("ikan_kakap_merah", 0.23f, 0.50f, 0.68f),
    IkanPratinjau("ikan_mas", 0.17f, 0.70f, 0.86f),
)

/**
 * 14 Pengenalan. Frontend.
 * Empat halaman bergambar TANPA toolbar. Muncul sekali saat pertama dibuka, bisa dilihat lagi dari Pengaturan.
 */
@Composable
fun PengenalanScreen(onSelesai: () -> Unit) {
    var i by rememberSaveable { mutableIntStateOf(0) }
    val warna = LocalWarna.current
    val h = halaman[i]
    val terakhir = i == halaman.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(warna.latarAtas, warna.latarBawah)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val gambar = if (warna.gelap) h.gambar else h.gambarTerang
            if (gambar != null) {
                Gambar(gambar, null, Modifier.fillMaxWidth().height(260.dp))
            } else {
                PratinjauAkuarium(Modifier.fillMaxWidth().aspectRatio(4f / 3f))
            }
        }
        Text(h.judul, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            h.isi,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            halaman.indices.forEach { n ->
                Box(
                    Modifier.size(8.dp).clip(CircleShape).background(
                        if (n == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!terakhir) {
                TombolSekunder("Lewati", onSelesai, Modifier.weight(1f))
            }
            TombolUtama(if (terakhir) "Mulai" else "Lanjut", { if (terakhir) onSelesai() else i++ }, Modifier.weight(1f))
        }
    }
}

/**
 * Pratinjau akuarium: air, pasir, karang, dan rumput laut yang sama dengan layar Akuarium,
 * ditambah tiga ikan yang berenang pelan dan gelembung yang naik. Dipakai di halaman terakhir
 * pengenalan agar pemain melihat isi akuarium, bukan gambar latar kosong.
 */
@Composable
private fun PratinjauAkuarium(modifier: Modifier = Modifier) {
    val warna = LocalWarna.current
    val kepadatan = LocalDensity.current
    val transisi = rememberInfiniteTransition(label = "pratinjau_akuarium")
    val waktu by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "waktu_akuarium",
    )
    val bentuk = RoundedCornerShape(24.dp)

    Box(modifier.clip(bentuk).background(warna.akuariumBawah)) {
        Gambar(
            if (warna.gelap) Art.latar_akuarium else Art.latar_akuarium_terang,
            null,
            Modifier.fillMaxSize(),
            ContentScale.Crop,
        )

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val lebarPx = with(kepadatan) { maxWidth.toPx() }
            val tinggiPx = with(kepadatan) { maxHeight.toPx() }

            // Dasar akuarium, ukurannya mengikuti tinggi panggung agar tetap rapi di layar kecil.
            Gambar(
                Art.pasir_dasar,
                null,
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(maxHeight * 0.16f),
                ContentScale.FillBounds,
            )
            Gambar(
                Art.kelp_1,
                null,
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = maxWidth * 0.02f, bottom = maxHeight * 0.10f)
                    .size(maxHeight * 0.16f, maxHeight * 0.44f),
            )
            Gambar(
                Art.karang_oranye,
                null,
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = maxWidth * 0.14f, bottom = maxHeight * 0.08f)
                    .size(maxHeight * 0.16f),
            )
            Gambar(
                Art.batu_1,
                null,
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(end = maxWidth * 0.16f, bottom = maxHeight * 0.09f)
                    .size(maxHeight * 0.26f, maxHeight * 0.16f),
            )
            Gambar(
                Art.karang_pink,
                null,
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = maxWidth * 0.06f, bottom = maxHeight * 0.10f)
                    .size(maxHeight * 0.14f),
            )
            Gambar(
                Art.kelp_3,
                null,
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = maxWidth * 0.02f, bottom = maxHeight * 0.09f)
                    .size(maxHeight * 0.15f, maxHeight * 0.34f),
            )
            Gambar(
                Art.bintang_laut,
                null,
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = maxWidth * 0.26f, bottom = maxHeight * 0.04f)
                    .size(maxHeight * 0.11f),
            )

            // Gelembung naik pelan dari dasar.
            repeat(4) { b ->
                val fase = (waktu * 2.2f + b / 4f) % 1f
                Gambar(
                    Art.gelembung,
                    null,
                    Modifier
                        .offset {
                            IntOffset(
                                (lebarPx * (0.08f + 0.19f * b)).roundToInt(),
                                ((1f - fase) * tinggiPx * 0.74f).roundToInt(),
                            )
                        }
                        .size(maxHeight * (0.05f + (b % 2) * 0.015f))
                        .alpha(if (warna.gelap) 0.55f else 0.32f),
                    warna = if (warna.gelap) null else warna.petaGaris,
                )
            }

            ikanPratinjau.forEachIndexed { i, ikan ->
                val ukuran = maxHeight * ikan.ukuran
                val ukuranPx = with(kepadatan) { ukuran.toPx() }
                // Satu siklus penuh = berenang ke kanan lalu kembali, dengan laju berbeda per ikan.
                val fase = (waktu * ikan.laju + i * 0.31f) % 1f
                val x = ((sin(2 * PI * fase).toFloat()) + 1f) / 2f
                val arah = cos(2 * PI * fase).toFloat()
                val goyang = sin(2 * PI * (fase * 2f + i * 0.4f)).toFloat()
                // Sisip kecil di kiri-kanan agar ikan tidak terpotong sudut kartu.
                val sisip = lebarPx * 0.05f
                val rentang = (lebarPx - ukuranPx - sisip * 2f).coerceAtLeast(0f)
                Box(
                    Modifier
                        .offset {
                            IntOffset(
                                (sisip + x * rentang).roundToInt(),
                                ((ikan.lajur + goyang * 0.04f) * tinggiPx - ukuranPx / 2f).roundToInt(),
                            )
                        }
                        .size(ukuran),
                ) {
                    GambarKontras(
                        gambarIkanKecil(ikan.gambar),
                        null,
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // Aset ikan menghadap kanan; balik saat berenang ke kiri.
                                scaleX = if (arah >= 0f) 1f else -1f
                                rotationZ = goyang * 4f
                            },
                    )
                }
            }
        }

        Box(Modifier.matchParentSize().border(1.dp, warna.garis, bentuk))
    }
}

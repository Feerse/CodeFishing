package com.example.codefishing.frontend.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.MAKS_NAMA_AKUARIUM
import com.example.codefishing.core.db.KoleksiLengkapView
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan
import com.example.codefishing.ui.theme.LocalWarna
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 13 Akuarium: ikan yang sudah dimiliki berenang di atas latar kertas.
 * Ikan yang belum dimiliki tidak ditampilkan di sini.
 */
@Composable
fun AkuariumScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val warna = LocalWarna.current
    val ikan by c.game.observeAkuarium().collectAsState(emptyList())
    val namaPanggilan by c.namaAkuarium.nama.collectAsState()
    val scope = rememberCoroutineScope()
    var dipilih by remember { mutableStateOf<KoleksiLengkapView?>(null) }
    var dialogNama by remember { mutableStateOf(false) }
    var namaBaru by remember { mutableStateOf("") }
    val pilihan = dipilih?.let { lama -> ikan.firstOrNull { it.ikan_id == lama.ikan_id } }
    // Animasi serpihan makanan: naik 1 tiap "Beri Makan"; progres 0..1 saat serpihan jatuh.
    var makanKe by remember { mutableIntStateOf(0) }
    val jatuh = remember { Animatable(0f) }
    LaunchedEffect(makanKe) {
        if (makanKe > 0) {
            jatuh.snapTo(0f)
            jatuh.animateTo(1f, tween(2200, easing = LinearEasing))
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(warna.akuariumBawah),
    ) {
        Gambar(
            if (warna.gelap) Art.latar_akuarium else Art.latar_akuarium_terang,
            "Akuarium",
            Modifier.fillMaxSize(),
            ContentScale.Crop,
        )
        Box(Modifier.fillMaxSize().background(if (warna.gelap) Color(0x33000000) else Color.Transparent))

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val lebar = maxWidth
            val tinggi = maxHeight
            val transisi = rememberInfiniteTransition(label = "akuarium")

            // Dasar akuarium: pasir, rumput laut, batu, karang, bintang laut, kerang.
            Gambar(
                Art.pasir_dasar,
                null,
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(72.dp),
                ContentScale.FillBounds,
            )
            Gambar(Art.kelp_1, null, Modifier.align(Alignment.BottomStart).padding(start = 6.dp, bottom = 36.dp).size(60.dp, 150.dp))
            Gambar(Art.kelp_3, null, Modifier.align(Alignment.BottomStart).padding(start = 78.dp, bottom = 40.dp).size(66.dp, 112.dp))
            Gambar(Art.batu_1, null, Modifier.align(Alignment.BottomCenter).padding(start = 40.dp, bottom = 38.dp).size(84.dp, 56.dp))
            Gambar(Art.karang_oranye, null, Modifier.align(Alignment.BottomCenter).padding(end = 120.dp, bottom = 34.dp).size(58.dp))
            Gambar(Art.kerang, null, Modifier.align(Alignment.BottomEnd).padding(end = 96.dp, bottom = 30.dp).size(34.dp, 32.dp))
            Gambar(Art.batu_2, null, Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 36.dp).size(76.dp, 53.dp))
            Gambar(Art.karang_pink, null, Modifier.align(Alignment.BottomEnd).padding(end = 70.dp, bottom = 44.dp).size(52.dp))
            Gambar(Art.kelp_2, null, Modifier.align(Alignment.BottomEnd).padding(end = 150.dp, bottom = 38.dp).size(48.dp, 120.dp))
            Gambar(Art.bintang_laut, null, Modifier.align(Alignment.BottomStart).padding(start = 170.dp, bottom = 26.dp).size(34.dp))

            // Gelembung naik pelan dari dasar.
            val naik by transisi.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
                label = "gelembung",
            )
            repeat(6) { g ->
                val ukuranGel = (14 + (g % 3) * 4).dp
                Gambar(
                    Art.gelembung,
                    null,
                    Modifier
                        .offset {
                            val fase = (naik + g / 6f) % 1f
                            IntOffset(
                                ((lebar - ukuranGel) * (0.08f + 0.17f * g)).roundToPx(),
                                ((tinggi - ukuranGel) * (1f - fase)).roundToPx(),
                            )
                        }
                        .size(ukuranGel)
                        .alpha(if (warna.gelap) 0.7f else 0.28f),
                    warna = if (warna.gelap) null else warna.petaGaris,
                )
            }

            ikan.forEachIndexed { i, k ->
                val durasi = 2600 + (i % 5) * 350
                val naikTurun by transisi.animateFloat(
                    initialValue = -7f,
                    targetValue = 7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durasi, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "ikan-$i",
                )
                // Gerak menyusuri akuarium kiri-kanan; ikan menghadap arah geraknya.
                val keliling by transisi.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durasi * 4, easing = LinearEasing),
                    ),
                    label = "keliling-$i",
                )
                val kiri = if (i % 2 == 0) 12f + (i * 13) % 55 else 45f + (i * 17) % 45
                val atas = 18f + ((i * 19) % 58)
                val ukuran = if (k.tier == "mitos" || k.tier == "legenda") 92.dp else 68.dp
                val namaTampil = namaPanggilan[k.ikan_id] ?: k.nama
                Gambar(
                    gambarIkan(k.gambar),
                    namaTampil,
                    Modifier
                        .offset {
                            val maks = (lebar - ukuran).roundToPx()
                            val geser = (sin(2.0 * PI * keliling) * 38.dp.toPx()).toInt()
                            IntOffset(
                                (((lebar - ukuran) * (kiri / 100f)).roundToPx() + geser).coerceIn(0, maks),
                                ((tinggi - ukuran) * (atas / 100f) + naikTurun.dp).roundToPx(),
                            )
                        }
                        .size(ukuran)
                        .graphicsLayer {
                            // Aset ikan menghadap kanan; balik saat berenang ke kiri.
                            scaleX = if (cos(2.0 * PI * keliling) >= 0.0) 1f else -1f
                        }
                        .clickable { dipilih = k },
                )
            }

            // Serpihan makanan jatuh saat "Beri Makan".
            if (makanKe > 0 && jatuh.value < 1f) {
                repeat(9) { s ->
                    Gambar(
                        Art.serpihan_makanan,
                        null,
                        Modifier
                            .offset {
                                IntOffset(
                                    (lebar * (0.08f + 0.10f * s)).roundToPx(),
                                    (tinggi * 0.78f * (jatuh.value * (0.8f + 0.03f * s))).roundToPx(),
                                )
                            }
                            .size(20.dp, 18.dp)
                            .alpha(1f - 0.5f * jatuh.value),
                    )
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Akuarium",
                    style = MaterialTheme.typography.headlineMedium,
                    color = warna.teksDiAdegan,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${ikan.size} jenis ikan berenang di sini",
                    style = MaterialTheme.typography.bodySmall,
                    color = warna.teksDiAdeganRedup,
                )
            }

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (ikan.isEmpty()) {
                    Kartu(Modifier.fillMaxWidth(), warna = warna.kartu.copy(alpha = 0.95f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Akuarium masih kosong", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                            Text("Tangkap ikan pertamamu di Mode Belajar atau Sandbox.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            TombolUtama(
                                teks = "Buka Peta",
                                onClick = {
                                    c.audio.play(Sfx.Klik)
                                    nav.resetTo(Screen.Peta)
                                },
                            )
                        }
                    }
                }

                if (ikan.isNotEmpty()) {
                    PanelAksiAkuarium(
                        onBeriMakanSemua = {
                            c.audio.play(Sfx.Benar)
                            makanKe++
                            scope.launch {
                                ikan.forEach { c.game.beriMakan(it.ikan_id) }
                            }
                        },
                        onKoleksi = {
                            c.audio.play(Sfx.Klik)
                            nav.resetTo(Screen.Koleksi)
                        },
                    )
                }
            }
        }
    }

    if (pilihan != null) {
        Dialog(
            onDismissRequest = {
                dipilih = null
                dialogNama = false
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnClickOutside = true,
            ),
        ) {
            val panggilan = namaPanggilan[pilihan.ikan_id]
            val namaTampil = panggilan ?: pilihan.nama
            val interaksiKartu = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
                    .clickable(onClickLabel = "Tutup informasi ikan") {
                        dipilih = null
                        dialogNama = false
                    },
            ) {
                Kartu(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .clickable(
                            interactionSource = interaksiKartu,
                            indication = null,
                            onClick = {},
                        ),
                    warna = warna.kartu,
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Gambar(gambarIkan(pilihan.gambar), namaTampil, Modifier.size(64.dp))
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    namaTampil,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (panggilan != null) {
                                    Text(
                                        "Spesies: ${pilihan.nama}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    "Jumlah ×${pilihan.jumlah} · ${"%.1f".format(pilihan.bobot_terbesar)} kg · kenyang ${pilihan.kenyang}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        TombolSekunder(
                            teks = if (panggilan == null) "Beri Nama" else "Ganti Nama",
                            onClick = {
                                namaBaru = panggilan.orEmpty()
                                dialogNama = true
                            },
                            modifier = Modifier.height(48.dp),
                        )
                        TombolSekunder(
                            teks = "Beri Makan",
                            onClick = {
                                c.audio.play(Sfx.Benar)
                                makanKe++
                                scope.launch { c.game.beriMakan(pilihan.ikan_id) }
                            },
                            modifier = Modifier.height(48.dp),
                        )
                    }
                }
            }
        }
    }

    if (dialogNama && pilihan != null) {
        val ikanId = pilihan.ikan_id
        AlertDialog(
            onDismissRequest = { dialogNama = false },
            title = { Text("Ganti nama ikan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = namaBaru,
                        onValueChange = { namaBaru = it.take(MAKS_NAMA_AKUARIUM) },
                        singleLine = true,
                        label = { Text("Nama di akuarium") },
                        placeholder = { Text(pilihan.nama) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                c.namaAkuarium.simpan(ikanId, namaBaru)
                                dialogNama = false
                            },
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Nama asli: ${pilihan.nama}. Nama di Koleksi tidak berubah.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (namaPanggilan[ikanId] != null) {
                        TextButton(
                            onClick = {
                                c.namaAkuarium.simpan(ikanId, "")
                                dialogNama = false
                            },
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text("Kembalikan nama asli")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        c.namaAkuarium.simpan(ikanId, namaBaru)
                        dialogNama = false
                    },
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { dialogNama = false }) { Text("Batal") }
            },
        )
    }
}

/** Dua aksi utama akuarium: beri makan semua dan buka koleksi. */
@Composable
private fun PanelAksiAkuarium(
    onBeriMakanSemua: () -> Unit,
    onKoleksi: () -> Unit,
) {
    val warna = LocalWarna.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(warna.kartu.copy(alpha = 0.95f), RoundedCornerShape(22.dp))
            .border(1.dp, warna.garis.copy(alpha = 0.65f), RoundedCornerShape(22.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onBeriMakanSemua,
            modifier = Modifier.weight(1.4f).height(52.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            Gambar(
                Art.ikon_beri_makan,
                null,
                Modifier.size(21.dp),
                warna = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Beri Makan Semua",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FilledTonalButton(
            onClick = onKoleksi,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Gambar(
                Art.ikon_koleksi,
                null,
                Modifier.size(20.dp),
                warna = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Koleksi",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

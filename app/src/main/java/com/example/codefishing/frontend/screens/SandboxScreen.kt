package com.example.codefishing.frontend.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.TangkapanMasuk
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.expert.Dunia
import com.example.codefishing.expert.MisiPeta
import com.example.codefishing.expert.SandboxContent
import com.example.codefishing.expert.SandboxDraft
import com.example.codefishing.expert.SandboxEngine
import com.example.codefishing.expert.SandboxRun
import com.example.codefishing.expert.py.Langkah
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.frontend.components.Bintang
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.components.Gambar
import androidx.compose.ui.layout.ContentScale
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.KodeBlok
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.frontend.components.gambarIkan
import com.example.codefishing.pemula.FishRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 07 Mode Expert: daftar 7 misi dan editor kode Python.
 * Hasil eksekusi tampil inline di editor (tanpa pindah layar).
 * 15 angka batas tick, 3 bintang di par, 2 bintang di 1,5x par.
 */
private val kataKunciPython = Regex(
    """\b(def|return|if|elif|else|for|while|in|and|or|not|True|False|None|print|range|len|int|str|float|bool)\b""",
)
private val komentarPython = Regex("""#[^\n]*""")

private fun warnaiKode(teks: String): AnnotatedString = buildAnnotatedString {
    append(teks)
    kataKunciPython.findAll(teks).forEach {
        addStyle(SpanStyle(color = Color(0xFFFFC46B)), it.range.first, it.range.last + 1)
    }
    // Komentar diberi warna terakhir agar tetap menutupi kata kunci di dalamnya.
    komentarPython.findAll(teks).forEach {
        addStyle(SpanStyle(color = Color(0xFF7FB3C8)), it.range.first, it.range.last + 1)
    }
}

@Composable
fun SandboxScreen(nav: AppNavigator, padding: PaddingValues) {
    val context = LocalContext.current
    val misi = remember { SandboxContent.misi(context) }
    val c = LocalContainer.current
    val bintang by c.game.observeBintangMisi().collectAsState(emptyList())
    val petaBintang = bintang.associateBy { it.id }
    var aktif by remember(nav.current.misi) {
        mutableStateOf(misi.firstOrNull { it.nomor == nav.current.misi })
    }

    BackHandler(enabled = aktif != null) { aktif = null }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(Color(0xFF0B2432), Color(0xFF071923)))),
    ) {
        val m = aktif
        if (m == null) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text("Mode Expert", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Tulis Python, gerakkan kapal, tangkap ikan. Batas 1000 tick.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(misi, key = { it.kode }) { item ->
                    val b = petaBintang[item.dbId]
                    Kartu(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                c.audio.play(Sfx.Klik)
                                aktif = item
                            },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Gambar(lencanaMisi(item.nomor), "Misi ${item.nomor}", Modifier.size(52.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.judul, style = MaterialTheme.typography.titleMedium)
                                Text(item.deskripsi, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Bintang(b?.bintang ?: 0, ukuran = 14)
                                    if (item.par != null) Text("Par ${item.par} tick", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        "Semua program berjalan offline di perangkat. Tidak ada koneksi internet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            EditorMisi(
                misi = m,
                onKembali = { aktif = null },
                onLanjut = { nomor -> aktif = misi.firstOrNull { it.nomor == nomor } },
            )
        }
    }
}

@Composable
private fun EditorMisi(
    misi: MisiPeta,
    onKembali: () -> Unit,
    onLanjut: (Int) -> Unit,
) {
    val c = LocalContainer.current
    val scope = rememberCoroutineScope()
    val engine = remember { SandboxEngine() }
    val semuaIkan by produceState<List<IkanEntity>>(emptyList()) { value = c.game.semuaIkan() }
    val pengaturan by c.game.observeSettings().collectAsState(null)
    val petaIkan = semuaIkan.associateBy { it.nama.lowercase() }
    val transformasi = remember(pengaturan?.pewarnaan_sintaks) {
        if (pengaturan?.pewarnaan_sintaks == 0) {
            VisualTransformation.None
        } else {
            VisualTransformation { teks ->
                TransformedText(warnaiKode(teks.text), OffsetMapping.Identity)
            }
        }
    }

    var kode by remember(misi.nomor) { mutableStateOf(SandboxDraft.ambil(misi.nomor, misi.kodeAwal)) }
    var hasil by remember(misi.nomor) { mutableStateOf<SandboxRun?>(null) }
    var berjalan by remember(misi.nomor) { mutableStateOf(false) }
    var pesanSimpan by remember(misi.nomor) { mutableStateOf<String?>(null) }
    var xpDidapat by remember(misi.nomor) { mutableIntStateOf(0) }
    val duniaAwal = remember(misi.nomor) { Dunia(misi.grid, misi.cells) }
    val scroll = rememberScrollState()

    // Jangan tinggalkan editor saat program masih dijalankan/disimpan.
    BackHandler(enabled = berjalan) { }

    // Saat pindah misi, mulai dari atas dan tampilkan papan misi baru.
    LaunchedEffect(misi.nomor) {
        scroll.scrollTo(0)
    }

    // Setelah dijalankan, kembali ke papan agar posisi akhir kapal langsung terlihat.
    LaunchedEffect(hasil) {
        if (hasil != null) {
            delay(80)
            scroll.animateScrollTo(0)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Misi ${misi.nomor}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                Text(
                    misi.judul,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            TombolSekunder(
                teks = "Daftar",
                onClick = onKembali,
                modifier = Modifier.width(110.dp),
                enabled = !berjalan,
                contentPadding = PaddingValues(horizontal = 10.dp),
            )
        }
        Text(misi.deskripsi, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PapanMisi(
            misi = misi,
            dunia = hasil?.dunia ?: duniaAwal,
            hasil = hasil,
            petaIkan = petaIkan,
            memuat = semuaIkan.isEmpty(),
        )

        if (hasil?.evaluasi?.sukses == true) {
            Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF113D34)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (misi.nomor < 7) "Lanjut ke misi berikutnya" else "Semua misi selesai",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF8BF0C7),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (misi.nomor < 7) {
                            TombolUtama(
                                teks = "Misi Berikutnya",
                                onClick = {
                                    c.audio.play(Sfx.KlikUtama)
                                    onLanjut(misi.nomor + 1)
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        TombolSekunder(
                            teks = if (misi.nomor < 7) "Daftar" else "Kembali ke Daftar",
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                onKembali()
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF2A2A17)) {
            Text("Petunjuk", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFFD98A))
            Spacer(Modifier.height(4.dp))
            Text(misi.hint, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = kode,
            onValueChange = {
                kode = it
                SandboxDraft.simpan(misi.nomor, it)
                hasil = null
                pesanSimpan = null
                xpDidapat = 0
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, lineHeight = 20.sp),
            label = { Text("main.py") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
            visualTransformation = transformasi,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF071E29),
                unfocusedContainerColor = Color(0xFF071E29),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
            ),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TombolUtama(
                teks = if (berjalan) "Menjalankan..." else "Jalankan",
                onClick = {
                    if (berjalan || semuaIkan.isEmpty()) return@TombolUtama
                    berjalan = true
                    pesanSimpan = null
                    xpDidapat = 0
                    c.audio.play(Sfx.KlikUtama)
                    scope.launch {
                        try {
                            val run = withContext(Dispatchers.Default) { engine.jalankan(kode, misi) }
                            hasil = run
                            val status = when {
                                !run.hasil.berhasil -> "error"
                                run.evaluasi.sukses -> "sukses"
                                else -> "gagal"
                            }
                            val tangkapan = run.dunia.tangkapan
                                .groupingBy { it }
                                .eachCount()
                                .map { (nama, jumlah) ->
                                    val ikan = petaIkan[nama.lowercase()]
                                    TangkapanMasuk(
                                        nama = ikan?.nama ?: nama,
                                        bobot = FishRules.bobotAcak(ikan?.bobot_kg ?: 1.0),
                                        jumlah = jumlah,
                                    )
                                }
                            try {
                                val simpan = c.game.simpanHasilProgram(
                                    misiId = misi.dbId,
                                    kode = kode,
                                    output = run.hasil.keluaran.joinToString("\n"),
                                    error = run.hasil.pesanError,
                                    ticks = run.hasil.ticks,
                                    status = status,
                                    tangkapan = tangkapan,
                                )
                                if (simpan != null) {
                                    xpDidapat = simpan.hasil.xp + simpan.tangkapan.sumOf { it.xpDidapat }
                                } else {
                                    pesanSimpan = "Program berjalan, tetapi hasil tidak dapat dibaca kembali dari penyimpanan."
                                }
                            } catch (e: Exception) {
                                pesanSimpan = "Program berjalan, tetapi hasil gagal disimpan: ${e.message ?: "kesalahan penyimpanan"}"
                            }
                            c.audio.play(if (status == "sukses") Sfx.Benar else Sfx.Salah)
                        } catch (e: Exception) {
                            pesanSimpan = "Gagal menjalankan program: ${e.message ?: "kesalahan tidak dikenal"}"
                            c.audio.play(Sfx.Salah)
                        } finally {
                            berjalan = false
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = semuaIkan.isNotEmpty() && !berjalan,
            )
            TombolSekunder(
                teks = "Contoh",
                onClick = {
                    kode = misi.kodeAwal
                    SandboxDraft.simpan(misi.nomor, misi.kodeAwal)
                    hasil = null
                    pesanSimpan = null
                    xpDidapat = 0
                },
                modifier = Modifier.width(110.dp),
                enabled = !berjalan,
                contentPadding = PaddingValues(horizontal = 10.dp),
            )
        }

        if (pesanSimpan != null) {
            Kartu(Modifier.fillMaxWidth(), warna = Color(0xFF41232A)) {
                Text(pesanSimpan!!, style = MaterialTheme.typography.bodySmall)
            }
        }

        val r = hasil
        if (r != null) {
            Kartu(
                Modifier.fillMaxWidth(),
                warna = when {
                    !r.hasil.berhasil -> Color(0xFF41232A)
                    r.evaluasi.sukses -> Color(0xFF113D34)
                    else -> Color(0xFF3B3618)
                },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        when {
                            !r.hasil.berhasil -> "Kode error"
                            r.evaluasi.sukses -> "Misi berhasil!"
                            else -> "Target belum tercapai"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(r.hasil.pesanError ?: r.evaluasi.alasan, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Tick ${r.hasil.ticks}", style = MaterialTheme.typography.labelMedium)
                        Text("Tangkap ${r.hasil.tertangkap}", style = MaterialTheme.typography.labelMedium)
                        Text("Sampah ${r.hasil.sampah}", style = MaterialTheme.typography.labelMedium)
                        if (xpDidapat > 0) {
                            Text("+$xpDidapat XP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Bintang(r.evaluasi.bintang, ukuran = 16)
                    }
                }
            }

            if (r.hasil.keluaran.isNotEmpty()) {
                KodeBlok(r.hasil.keluaran.joinToString("\n"), judul = "OUTPUT")
            }

            if (r.hasil.trace.isNotEmpty()) {
                Text("Jejak gerak", style = MaterialTheme.typography.labelLarge)
                JejakRingkas(r.hasil.trace)
            }
        } else {
            Text(
                "Tekan Jalankan untuk melihat hasil. Kamu bisa mengubah kode sesukanya.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PapanMisi(
    misi: MisiPeta,
    dunia: Dunia,
    hasil: SandboxRun?,
    petaIkan: Map<String, IkanEntity>,
    memuat: Boolean,
) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (hasil == null) "Peta Misi" else "Posisi Akhir",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    "${misi.grid} × ${misi.grid} petak" + if (hasil == null) "" else " · ${hasil.hasil.ticks} tick",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusPapan(hasil)
        }

        if (memuat) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color(0xFF061A24), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Memuat peta misi...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            GridDunia(dunia.ukuran, dunia.x, dunia.y, dunia::petakDi, petaIkan)
        }

        Text(
            (if (hasil == null) {
                "Kapal mulai di (0, 0). "
            } else {
                "Kapal berakhir di (${dunia.x}, ${dunia.y}) · ${dunia.sisaIkan} ikan tersisa. "
            }) + "Baris bawah = start.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Bingkai oranye = kapal · Petak berisi ikan = tangkapan · Sepatu bot = sampah",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        )
        if (hasil != null) {
            Text(
                hasil.hasil.pesanError ?: hasil.evaluasi.alasan,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    !hasil.hasil.berhasil -> Color(0xFFFF8B8B)
                    hasil.evaluasi.sukses -> Color(0xFF5FD3A0)
                    else -> Color(0xFFFFD98A)
                },
            )
        }
    }
}

@Composable
private fun StatusPapan(hasil: SandboxRun?) {
    val (teks, warna) = when {
        hasil == null -> "SIAP" to Color(0xFF9DB5BF)
        !hasil.hasil.berhasil -> "ERROR" to Color(0xFFFF8B8B)
        hasil.evaluasi.sukses -> "BERHASIL" to Color(0xFF5FD3A0)
        else -> "BELUM TERCAPAI" to Color(0xFFFFD98A)
    }
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

/** Lencana bergambar untuk daftar misi (misi 7 dan seterusnya = mode bebas). */
private fun lencanaMisi(nomor: Int): Int = when (nomor) {
    1 -> Art.lencana_misi_1
    2 -> Art.lencana_misi_2
    3 -> Art.lencana_misi_3
    4 -> Art.lencana_misi_4
    5 -> Art.lencana_misi_5
    6 -> Art.lencana_misi_6
    else -> Art.lencana_misi_bebas
}

@Composable
private fun GridDunia(
    ukuran: Int,
    kapalX: Int,
    kapalY: Int,
    petak: (Int, Int) -> com.example.codefishing.expert.Petak,
    petaIkan: Map<String, IkanEntity>,
) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF061A24), RoundedCornerShape(14.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        val sel = ((maxWidth - 16.dp - 3.dp * (ukuran - 1)) / ukuran).coerceIn(30.dp, 54.dp)
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (y in ukuran - 1 downTo 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (x in 0 until ukuran) {
                        val isi = petak(x, y).isi
                        val adaKapal = x == kapalX && y == kapalY
                        Box(
                            Modifier.size(sel),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Alas petak memakai aset final: laut, sampah (sepatu bot), atau bingkai kapal.
                            Gambar(
                                when {
                                    adaKapal -> Art.petak_kapal_sekarang
                                    isi == "sampah" -> Art.petak_sampah
                                    else -> Art.petak_laut
                                },
                                null,
                                Modifier.fillMaxSize(),
                                ContentScale.FillBounds,
                            )
                            if (isi.isNotEmpty() && isi != "sampah") {
                                val ikan = petaIkan[isi.lowercase()]
                                Gambar(
                                    gambarIkan(ikan?.gambar),
                                    ikan?.nama ?: isi,
                                    Modifier
                                        .size(if (adaKapal) sel * 0.40f else sel * 0.70f)
                                        .align(if (adaKapal) Alignment.TopStart else Alignment.Center),
                                )
                            }
                            if (adaKapal) {
                                Gambar(
                                    Art.kapal_atas_timur,
                                    "Kapal",
                                    Modifier
                                        .size(sel * 0.68f)
                                        .align(Alignment.BottomEnd),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun JejakRingkas(trace: List<Langkah>) {
    val tampil = trace.takeLast(18)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        tampil.forEach { l ->
            Text(
                "baris ${l.baris} · ${l.aksi}${l.pesan?.let { " · $it" } ?: ""} · tick ${l.tick}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

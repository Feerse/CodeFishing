package com.example.codefishing.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.codefishing.expert.MisiPeta
import com.example.codefishing.expert.SandboxContent
import com.example.codefishing.expert.SandboxDraft
import com.example.codefishing.expert.SandboxEngine
import com.example.codefishing.expert.SandboxRun
import com.example.codefishing.expert.py.Langkah
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.frontend.components.Bintang
import com.example.codefishing.frontend.components.Gambar
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
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF163E4E), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("${item.nomor}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
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
            EditorMisi(m, onKembali = { aktif = null })
        }
    }
}

@Composable
private fun EditorMisi(misi: MisiPeta, onKembali: () -> Unit) {
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
    val scroll = rememberScrollState()

    // Jangan tinggalkan editor saat program masih dijalankan/disimpan.
    BackHandler(enabled = berjalan) { }

    LaunchedEffect(hasil) {
        if (hasil != null) {
            delay(80)
            scroll.animateScrollTo(scroll.maxValue)
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

            GridDunia(r.dunia.ukuran, r.dunia.x, r.dunia.y, r.dunia::petakDi, petaIkan)

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
private fun GridDunia(
    ukuran: Int,
    kapalX: Int,
    kapalY: Int,
    petak: (Int, Int) -> com.example.codefishing.expert.Petak,
    petaIkan: Map<String, IkanEntity>,
) {
    val sel = if (ukuran >= 6) 38.dp else if (ukuran == 5) 46.dp else 54.dp
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF061A24), RoundedCornerShape(14.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (y in ukuran - 1 downTo 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (x in 0 until ukuran) {
                    val isi = petak(x, y).isi
                    val adaKapal = x == kapalX && y == kapalY
                    Box(
                        Modifier
                            .size(sel)
                            .background(
                                if (isi == "sampah") Color(0xFF4A3A27)
                                else if (isi.isNotEmpty()) Color(0xFF123E4A)
                                else Color(0xFF0C2B36),
                                RoundedCornerShape(6.dp),
                            )
                            .border(
                                1.dp,
                                if (adaKapal) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(6.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isi == "sampah") {
                            Text("🗑", fontSize = 16.sp)
                        } else if (isi.isNotEmpty()) {
                            val ikan = petaIkan[isi.lowercase()]
                            Gambar(gambarIkan(ikan?.gambar), ikan?.nama ?: isi, Modifier.size(sel * 0.7f))
                        }
                        if (adaKapal) {
                            Gambar(com.example.codefishing.core.art.Art.kapal_pancing, "Kapal", Modifier.size(sel * 0.85f))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text("Baris bawah = start. Kapal ditandai bingkai kuning.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

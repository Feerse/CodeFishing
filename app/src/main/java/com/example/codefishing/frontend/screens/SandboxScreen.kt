package com.example.codefishing.frontend.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.TangkapanMasuk
import com.example.codefishing.core.data.LocalEfekVisual
import com.example.codefishing.core.data.hemat
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.core.i18n.LocalTeks
import com.example.codefishing.core.i18n.t
import com.example.codefishing.expert.EditorKode
import com.example.codefishing.expert.MisiPeta
import com.example.codefishing.expert.SandboxContent
import com.example.codefishing.expert.SandboxDraft
import com.example.codefishing.expert.SandboxEngine
import com.example.codefishing.expert.SandboxRun
import com.example.codefishing.expert.py.Langkah
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.frontend.components.BarisPintasanKode
import com.example.codefishing.frontend.components.Bintang
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.KodeBlok
import com.example.codefishing.frontend.components.PanduanExpertSheet
import com.example.codefishing.frontend.components.PapanMisi
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.pemula.FishRules
import com.example.codefishing.ui.theme.LocalWarna
import com.example.codefishing.ui.theme.WarnaCodeFishing
import com.example.codefishing.ui.theme.aksenMisi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

/**
 * 07 Mode Expert: daftar 7 misi dan editor kode Python.
 * Hasil eksekusi tampil inline di editor (tanpa pindah layar).
 * 15 angka batas tick, 3 bintang di par, 2 bintang di 1,5x par.
 */
private val kataKunciPython = Regex(
    """\b(def|return|if|elif|else|for|while|in|and|or|not|True|False|None|print|range|len|int|str|float|bool)\b""",
)
private val komentarPython = Regex("""#[^\n]*""")

private fun warnaiKode(teks: String, warna: WarnaCodeFishing): AnnotatedString = buildAnnotatedString {
    append(teks)
    kataKunciPython.findAll(teks).forEach {
        addStyle(SpanStyle(color = warna.kodeKataKunci), it.range.first, it.range.last + 1)
    }
    // Komentar diberi warna terakhir agar tetap menutupi kata kunci di dalamnya.
    komentarPython.findAll(teks).forEach {
        addStyle(SpanStyle(color = warna.kodeKomentar), it.range.first, it.range.last + 1)
    }
}

/** Tambahkan indentasi otomatis hanya saat satu newline baru diketik. */
private fun autoIndent(lama: TextFieldValue, baru: TextFieldValue): TextFieldValue {
    if (!baru.selection.collapsed) return baru
    val indent = EditorKode.indentasiUntukBarisBaru(
        lama = lama.text,
        baru = baru.text,
        kursor = baru.selection.start,
    )
    if (indent.isNullOrEmpty()) return baru
    val pos = baru.selection.start
    val teks = baru.text.substring(0, pos) + indent + baru.text.substring(pos)
    return TextFieldValue(teks, TextRange(pos + indent.length))
}

@Composable
fun SandboxScreen(nav: AppNavigator, padding: PaddingValues) {
    val context = LocalContext.current
    val misi = remember { SandboxContent.misi(context) }
    val c = LocalContainer.current
    val warna = LocalWarna.current
    val bintang by c.game.observeBintangMisi().collectAsState(emptyList())
    val petaBintang = bintang.associateBy { it.id }
    val petaBintangInt = remember(bintang) { bintang.associate { it.id to it.bintang } }
    var panduan by remember { mutableStateOf(false) }
    var aktif by remember(nav.current.misi) {
        mutableStateOf(misi.firstOrNull { it.nomor == nav.current.misi })
    }

    BackHandler(enabled = aktif != null) { aktif = null }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(warna.latarAtas, warna.latarBawah)))
            // IME tidak lagi mengecilkan window (edge-to-edge, targetSdk 37), jadi
            // viewport scroll harus dipersempit sendiri agar isi editor tidak
            // terkubur di bawah keyboard. consumeWindowInsets mencegah tinggi
            // bottom bar dihitung dua kali.
            .consumeWindowInsets(padding)
            .imePadding(),
    ) {
        val m = aktif
        if (m == null) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    KepalaExpert(jumlahMisi = misi.size)
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    Kartu(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                c.audio.play(Sfx.Klik)
                                panduan = true
                            },
                        warna = warna.kartuLembut,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Gambar(Art.ikon_kode, null, Modifier.size(40.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t("Panduan Expert"), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    t("Perintah, sintaks Python, dan petunjuk semua misi"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                t("Buka"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                itemsIndexed(misi, key = { _, item -> item.kode }) { indeks, item ->
                    val b = petaBintang[item.dbId]
                    MunculBertahap(indeks) {
                        KartuMisi(
                            item = item,
                            bintang = b?.bintang ?: 0,
                            aksen = aksenMisi(item.nomor, warna.gelap),
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                aktif = item
                            },
                        )
                    }
                }
                item {
                    Text(
                        t("Semua program berjalan offline di perangkat. Tidak ada koneksi internet."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            EditorMisi(
                misi = m,
                semuaMisi = misi,
                petaBintang = petaBintangInt,
                onKembali = { aktif = null },
                onLanjut = { nomor -> aktif = misi.firstOrNull { it.nomor == nomor } },
            )
        }
        if (m == null && panduan) {
            PanduanExpertSheet(
                semuaMisi = misi,
                misiAktif = null,
                bintang = petaBintangInt,
                onTutup = { panduan = false },
                onPilihMisi = { nomor ->
                    c.audio.play(Sfx.Klik)
                    aktif = misi.firstOrNull { it.nomor == nomor }
                    panduan = false
                },
                onSisip = null,
            )
        }
    }
}

@Composable
private fun EditorMisi(
    misi: MisiPeta,
    semuaMisi: List<MisiPeta>,
    petaBintang: Map<Int, Int>,
    onKembali: () -> Unit,
    onLanjut: (Int) -> Unit,
) {
    val c = LocalContainer.current
    val scope = rememberCoroutineScope()
    val engine = remember { SandboxEngine() }
    val warna = LocalWarna.current
    val aksen = aksenMisi(misi.nomor, warna.gelap)
    // Untuk teks di dalam lambda non-@Composable (onClick, scope.launch).
    val tr = LocalTeks.current
    val semuaIkan by produceState<List<IkanEntity>>(emptyList()) { value = c.game.semuaIkan() }
    val pengaturan by c.game.observeSettings().collectAsState(null)
    val petaIkan = semuaIkan.associateBy { it.nama.lowercase() }
    val transformasi = remember(pengaturan?.pewarnaan_sintaks, warna) {
        if (pengaturan?.pewarnaan_sintaks == 0) {
            VisualTransformation.None
        } else {
            VisualTransformation { teks ->
                TransformedText(warnaiKode(teks.text, warna), OffsetMapping.Identity)
            }
        }
    }

    var kode by remember(misi.nomor) {
        val awal = SandboxDraft.ambil(misi.nomor, misi.kodeAwal)
        mutableStateOf(TextFieldValue(awal, TextRange(awal.length)))
    }
    var hasil by remember(misi.nomor) { mutableStateOf<SandboxRun?>(null) }
    var tampilHasil by remember(misi.nomor, hasil) { mutableStateOf(hasil == null) }
    var berjalan by remember(misi.nomor) { mutableStateOf(false) }
    var pesanSimpan by remember(misi.nomor) { mutableStateOf<String?>(null) }
    var xpDidapat by remember(misi.nomor) { mutableIntStateOf(0) }
    var panduan by remember(misi.nomor) { mutableStateOf(false) }
    var panduanSintaks by remember(misi.nomor) { mutableStateOf(false) }
    var pesanKodeAwal by remember(misi.nomor) { mutableStateOf<String?>(null) }
    val scroll = rememberScrollState()
    val fokus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val pengungkapPintasan = remember { BringIntoViewRequester() }

    fun terapkanKode(nilai: TextFieldValue) {
        kode = nilai
        SandboxDraft.simpan(misi.nomor, nilai.text)
        hasil = null
        pesanSimpan = null
        xpDidapat = 0
    }

    fun sisip(potongan: String, kursorMundur: Int) {
        if (berjalan) return
        pesanKodeAwal = null
        val baru = EditorKode.sisipkan(
            teks = kode.text,
            mulai = kode.selection.start,
            akhir = kode.selection.end,
            potongan = potongan,
            kursorMundur = kursorMundur,
        )
        terapkanKode(TextFieldValue(baru.teks, TextRange(baru.kursor)))
        fokus.requestFocus()
        keyboard?.show()
        scope.launch {
            delay(350)
            pengungkapPintasan.bringIntoView()
        }
    }

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
                Text(t("Misi {}", misi.nomor), style = MaterialTheme.typography.labelLarge, color = aksen)
                Text(
                    misi.judul,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            TombolSekunder(
                teks = t("Daftar"),
                onClick = onKembali,
                modifier = Modifier.width(110.dp),
                enabled = !berjalan,
                contentPadding = PaddingValues(horizontal = 10.dp),
            )
        }
        Text(misi.deskripsi, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PapanMisi(
            misi = misi,
            hasil = hasil,
            petaIkan = petaIkan,
            memuat = semuaIkan.isEmpty(),
            haptikAktif = (pengaturan?.haptik ?: 1) != 0,
            aksen = aksen,
            onSelesaiAnimasi = { tampilHasil = true },
        )

        if (hasil?.evaluasi?.sukses == true) {
            Kartu(Modifier.fillMaxWidth(), warna = warna.suksesLatar) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        t(if (misi.nomor < 7) "Lanjut ke misi berikutnya" else "Semua misi selesai"),
                        style = MaterialTheme.typography.titleMedium,
                        color = warna.suksesTeks,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (misi.nomor < 7) {
                            TombolUtama(
                                teks = t("Misi Berikutnya"),
                                onClick = {
                                    c.audio.play(Sfx.KlikUtama)
                                    onLanjut(misi.nomor + 1)
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        TombolSekunder(
                            teks = t(if (misi.nomor < 7) "Daftar" else "Kembali ke Daftar"),
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

        Kartu(Modifier.fillMaxWidth(), warna = warna.peringatanLatar) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    t("Petunjuk"),
                    style = MaterialTheme.typography.labelLarge,
                    color = warna.peringatanTeks,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        c.audio.play(Sfx.Klik)
                        keyboard?.hide()
                        panduanSintaks = false
                        panduan = true
                    },
                    enabled = !berjalan,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) { Text(t("Panduan")) }
            }
            Spacer(Modifier.height(4.dp))
            Text(misi.hint, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = kode,
            onValueChange = { baru ->
                pesanKodeAwal = null
                terapkanKode(autoIndent(kode, baru))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .focusRequester(fokus)
                .onFocusChanged { status ->
                    if (status.isFocused) {
                        scope.launch {
                            delay(350)
                            pengungkapPintasan.bringIntoView()
                        }
                    }
                }
                .onPreviewKeyEvent { event ->
                    if (berjalan || event.type != KeyEventType.KeyDown || event.key != Key.Tab) {
                        false
                    } else {
                        if (event.isShiftPressed) {
                            val kurang = EditorKode.kurangiIndentasi(kode.text, kode.selection.start)
                            terapkanKode(TextFieldValue(kurang.teks, TextRange(kurang.kursor)))
                        } else {
                            sisip("    ", 0)
                        }
                        true
                    }
                },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, lineHeight = 20.sp),
            label = { Text("main.py") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
            visualTransformation = transformasi,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = warna.kodeLatar,
                unfocusedContainerColor = warna.kodeLatar,
                focusedTextColor = warna.kodeTeks,
                unfocusedTextColor = warna.kodeTeks,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = warna.garis,
            ),
        )

        BarisPintasanKode(
            onSisip = { tombol ->
                c.audio.play(Sfx.Klik)
                sisip(tombol.potongan, tombol.kursorMundur)
            },
            enabled = !berjalan,
            modifier = Modifier.bringIntoViewRequester(pengungkapPintasan),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TombolUtama(
                teks = t(if (berjalan) "Menjalankan..." else "Jalankan"),
                onClick = {
                    if (berjalan || semuaIkan.isEmpty()) return@TombolUtama
                    berjalan = true
                    pesanSimpan = null
                    pesanKodeAwal = null
                    xpDidapat = 0
                    c.audio.play(Sfx.KlikUtama)
                    scope.launch {
                        try {
                            val run = withContext(Dispatchers.Default) { engine.jalankan(kode.text, misi) }
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
                                    kode = kode.text,
                                    output = run.hasil.keluaran.joinToString("\n"),
                                    error = run.hasil.pesanError,
                                    ticks = run.hasil.ticks,
                                    status = status,
                                    tangkapan = tangkapan,
                                )
                                if (simpan != null) {
                                    xpDidapat = simpan.hasil.xp + simpan.tangkapan.sumOf { it.xpDidapat }
                                } else {
                                    pesanSimpan = tr("Program berjalan, tetapi hasil tidak dapat dibaca kembali dari penyimpanan.")
                                }
                            } catch (e: Exception) {
                                pesanSimpan = tr.format(
                                    "Program berjalan, tetapi hasil gagal disimpan: {}",
                                    e.message ?: "kesalahan penyimpanan",
                                )
                            }
                            c.audio.play(if (status == "sukses") Sfx.Benar else Sfx.Salah)
                        } catch (e: Exception) {
                            pesanSimpan = tr.format(
                                "Gagal menjalankan program: {}",
                                e.message ?: "kesalahan tidak dikenal",
                            )
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
                teks = t("Kode Awal"),
                onClick = {
                    val sudahAwal = kode.text == misi.kodeAwal
                    terapkanKode(TextFieldValue(misi.kodeAwal, TextRange(0)))
                    pesanKodeAwal = if (sudahAwal) {
                        tr("Kode memang sudah memakai contoh awal.")
                    } else {
                        tr("Kode awal dimuat.")
                    }
                },
                modifier = Modifier.width(110.dp),
                enabled = !berjalan,
                contentPadding = PaddingValues(horizontal = 10.dp),
            )
        }

        pesanKodeAwal?.let { pesan ->
            Text(
                pesan,
                style = MaterialTheme.typography.labelMedium,
                color = warna.suksesTeks,
            )
        }

        if (pesanSimpan != null) {
            Kartu(Modifier.fillMaxWidth(), warna = warna.bahayaLatar) {
                Text(pesanSimpan!!, style = MaterialTheme.typography.bodySmall)
            }
        }

        val r = hasil
        if (r != null && tampilHasil) {
            Kartu(
                Modifier.fillMaxWidth(),
                warna = when {
                    !r.hasil.berhasil -> warna.bahayaLatar
                    r.evaluasi.sukses -> warna.suksesLatar
                    else -> warna.peringatanLatar
                },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        t(
                            when {
                                !r.hasil.berhasil -> "Kode error"
                                r.evaluasi.sukses -> "Misi berhasil!"
                                else -> "Target belum tercapai"
                            },
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(r.hasil.pesanError ?: r.evaluasi.alasan, style = MaterialTheme.typography.bodySmall)
                    // Editor tidak punya nomor baris, jadi baris yang bermasalah ditampilkan
                    // langsung di sini. Tanpa ini pemain harus menghitung baris manual.
                    val barisMasalah = r.hasil.errorBaris
                    if (barisMasalah > 0) {
                        val potongan = EditorKode.barisKe(kode.text, barisMasalah)
                        Text(
                            if (potongan != null) {
                                t("Baris {} · {}", barisMasalah, potongan)
                            } else {
                                t("Baris {}", barisMasalah)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = warna.bahayaTeks,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(t("Tick {}", r.hasil.ticks), style = MaterialTheme.typography.labelMedium)
                        Text(t("Tangkap {}", r.hasil.tertangkap), style = MaterialTheme.typography.labelMedium)
                        Text(t("Sampah {}", r.hasil.sampah), style = MaterialTheme.typography.labelMedium)
                        if (xpDidapat > 0) {
                            Text("+$xpDidapat XP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Bintang(r.evaluasi.bintang, ukuran = 16)
                    }
                    if (!r.evaluasi.sukses) {
                        TextButton(
                            onClick = {
                                c.audio.play(Sfx.Klik)
                                keyboard?.hide()
                                panduanSintaks = true
                                panduan = true
                            },
                            contentPadding = PaddingValues(),
                            modifier = Modifier.align(Alignment.End),
                        ) { Text(t("Cari solusi di panduan")) }
                    }
                }
            }

            if (r.hasil.keluaran.isNotEmpty()) {
                KodeBlok(r.hasil.keluaran.joinToString("\n"), judul = t("OUTPUT"))
            }

            if (r.hasil.trace.isNotEmpty()) {
                Text(t("Jejak gerak"), style = MaterialTheme.typography.labelLarge)
                JejakRingkas(r.hasil.trace)
            }
        } else if (r == null) {
            Text(
                t("Tekan Jalankan untuk melihat hasil. Kamu bisa mengubah kode sesukanya."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
    }

    if (panduan) {
        PanduanExpertSheet(
            semuaMisi = semuaMisi,
            misiAktif = misi.nomor,
            bintang = petaBintang,
            onTutup = { panduan = false },
            onPilihMisi = { nomor ->
                c.audio.play(Sfx.Klik)
                panduan = false
                onLanjut(nomor)
            },
            onSisip = { potongan, kursorMundur ->
                sisip(potongan, kursorMundur)
                panduan = false
            },
            bukaSintaks = panduanSintaks,
        )
    }
}

/**
 * Banner kepala layar Mode Expert.
 *
 * Sebelumnya kepala layar hanya dua baris teks di atas gradien datar. Diukur dari tangkapan
 * layar emulator, layar Expert jadi layar paling tidak berwarna di aplikasi: saturasi rata-rata
 * 0,059 dengan 82% piksel berwarna jatuh di satu sektor hue, dibanding 0,211 di layar kuis.
 * Banner ini memberi satu bidang bergambar (tiga warna aksen misi, dua pita ombak, kapal)
 * supaya layar punya tempat mata beristirahat yang bukan teks.
 */
@Composable
private fun KepalaExpert(jumlahMisi: Int, modifier: Modifier = Modifier) {
    val warna = LocalWarna.current
    val a1 = aksenMisi(1, warna.gelap)
    val a3 = aksenMisi(3, warna.gelap)
    val a6 = aksenMisi(6, warna.gelap)
    // Hemat: ombak berhenti di fase tetap, jadi tidak ada yang terus-menerus menggambar.
    val fase = if (LocalEfekVisual.current.hemat) 0f else faseOmbakExpert()
    val bentuk = RoundedCornerShape(24.dp)
    // Tema terang perlu aksen lebih pekat supaya banner benar-benar berwarna; tema gelap justru
    // perlu lebih tipis, karena latarnya sudah pekat dan teks terang di atasnya bisa turun
    // kontras (pada 0,52 teks kecil hanya ~3,9:1).
    val alfa = if (warna.gelap) 0.34f else 0.52f

    Box(
        modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(bentuk)
            .background(
                Brush.linearGradient(
                    listOf(
                        a1.copy(alpha = alfa),
                        a3.copy(alpha = alfa * 0.85f),
                        a6.copy(alpha = alfa * 0.72f),
                    ),
                ),
            )
            .border(1.dp, warna.garis, bentuk),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            repeat(2) { i ->
                val dasar = h * (0.60f + i * 0.15f)
                val amplitudo = h * 0.035f
                val geser = fase * (if (i == 0) 1f else -0.7f) * w
                val path = Path()
                path.moveTo(0f, h)
                var x = 0f
                while (x <= w) {
                    val y = dasar + sin((x + geser) / w * 2f * PI.toFloat() + i) * amplitudo
                    path.lineTo(x, y)
                    x += w / 24f
                }
                path.lineTo(w, h)
                path.close()
                drawPath(path, (if (i == 0) a1 else a3).copy(alpha = if (i == 0) alfa * 0.40f else alfa * 0.26f))
            }
        }
        Row(
            Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    t("Mode Expert"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = warna.petaTeks,
                )
                Text(
                    t("Tulis Python, gerakkan kapal, tangkap ikan."),
                    style = MaterialTheme.typography.bodySmall,
                    color = warna.petaTeks,
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipAksen(t("{} misi", jumlahMisi), a1)
                    ChipAksen(t("Batas {} tick", 1000), a6)
                }
            }
            Gambar(Art.kapal_peta, t("Kapal pemain"), Modifier.size(84.dp))
        }
    }
}

@Composable
private fun faseOmbakExpert(): Float {
    val transisi = rememberInfiniteTransition(label = "ombakExpert")
    val fase by transisi.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "faseOmbakExpert",
    )
    return fase
}

@Composable
private fun ChipAksen(teks: String, aksen: Color) {
    val warna = LocalWarna.current
    Text(
        teks,
        Modifier
            .background(warna.kartu, RoundedCornerShape(50))
            .border(1.dp, aksen.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = aksen,
        fontWeight = FontWeight.Bold,
    )
}

/**
 * Kartu satu misi. Sisi kiri, cincin lencana, dan teks par memakai warna aksen misi itu sendiri,
 * sehingga tujuh misi bisa dibedakan dari warnanya tanpa membaca judul.
 */
@Composable
private fun KartuMisi(
    item: MisiPeta,
    bintang: Int,
    aksen: Color,
    onClick: () -> Unit,
) {
    val warna = LocalWarna.current
    Kartu(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        // Kartu ikut diwarnai tipis dengan aksen misinya: sisi kiri saja terlalu sempit untuk
        // mengubah kesan layar, sedangkan tint seluas kartu membuat tujuh misi terlihat berbeda.
        warna = lerp(warna.kartu, aksen, if (warna.gelap) 0.12f else 0.08f),
        aksenSisi = aksen,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(aksen.copy(alpha = 0.20f), CircleShape)
                    .border(1.5.dp, aksen.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Gambar(lencanaMisi(item.nomor), t("Misi {}", item.nomor), Modifier.size(44.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(item.judul, style = MaterialTheme.typography.titleMedium)
                Text(
                    item.deskripsi,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BintangPop(bintang, ukuran = 14)
                    if (item.par != null) {
                        Text(t("Par {} tick", item.par), style = MaterialTheme.typography.labelSmall, color = aksen)
                    }
                }
            }
        }
    }
}

/**
 * Bintang yang memantul sekali saat misi pertama kali punya bintang. Tanpa ini bintang baru
 * muncul diam-diam dan pemain tidak sadar misinya sudah bernilai.
 */
@Composable
private fun BintangPop(jumlah: Int, ukuran: Int) {
    val kurangiGerak = LocalEfekVisual.current.hemat
    val skala = remember { Animatable(if (kurangiGerak || jumlah == 0) 1f else 0.5f) }
    LaunchedEffect(jumlah) {
        if (jumlah > 0 && !kurangiGerak) {
            skala.snapTo(0.5f)
            skala.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 520f))
        } else {
            skala.snapTo(1f)
        }
    }
    Box(
        Modifier.graphicsLayer {
            scaleX = skala.value
            scaleY = skala.value
        },
    ) {
        Bintang(jumlah, ukuran = ukuran)
    }
}

/** Kartu masuk satu per satu dari bawah; dilewati saat efek visual disetel Hemat. */
@Composable
private fun MunculBertahap(indeks: Int, isi: @Composable () -> Unit) {
    val kurangiGerak = LocalEfekVisual.current.hemat
    val progres = remember { Animatable(if (kurangiGerak) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!kurangiGerak) {
            delay(indeks.coerceAtMost(8) * 55L)
            progres.animateTo(1f, tween(340, easing = FastOutSlowInEasing))
        }
    }
    Box(
        Modifier.graphicsLayer {
            alpha = progres.value
            translationY = (1f - progres.value) * 30f
        },
    ) { isi() }
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
private fun JejakRingkas(trace: List<Langkah>) {
    val tampil = trace.takeLast(18)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        tampil.forEach { l ->
            Text(
                t("baris {} · {}{} · tick {}", l.baris, l.aksi, l.pesan?.let { " · $it" } ?: "", l.tick),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

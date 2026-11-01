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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.data.QuizData
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.BarProgres
import com.example.codefishing.frontend.components.FasePancing
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.frontend.components.Kartu
import com.example.codefishing.frontend.components.KodeBlok
import com.example.codefishing.frontend.components.MemancingScene
import com.example.codefishing.frontend.components.adeganStage
import com.example.codefishing.frontend.components.gambarIkan
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.frontend.components.Memuat
import com.example.codefishing.ui.theme.LocalWarna
import com.example.codefishing.frontend.components.TombolSekunder
import com.example.codefishing.frontend.components.TombolUtama
import com.example.codefishing.pemula.PilihanKuis
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuisScreen(nav: AppNavigator, padding: PaddingValues, stagePaksa: Int? = null) {
    val c = LocalContainer.current
    val warna = LocalWarna.current
    val stageId = stagePaksa ?: nav.current.stage.takeIf { it > 0 } ?: 1
    val data by produceState<QuizData?>(null, stageId) { value = c.game.quiz(stageId) }
    val umpanAwal by produceState<Int?>(null, stageId) { value = c.game.muatUmpan().sisa }
    val pengaturan by c.game.observeSettings().collectAsState(null)
    val haptik = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val d = data
    if (d == null) {
        Memuat(padding)
        return
    }

    LaunchedEffect(umpanAwal) {
        if (umpanAwal != null && umpanAwal!! <= 0) {
            nav.replace(Screen.UmpanHabis, stageId)
        }
    }

    var indeks by remember(d) { mutableIntStateOf(0) }
    var benar by remember(d) { mutableIntStateOf(0) }
    var salah by remember(d) { mutableIntStateOf(0) }
    var combo by remember(d) { mutableIntStateOf(0) }
    var comboMaks by remember(d) { mutableIntStateOf(0) }
    var xp by remember(d) { mutableIntStateOf(0) }
    var terpilih by remember(d) { mutableStateOf<Int?>(null) }
    var kandidat by remember(d) { mutableStateOf<Int?>(null) }
    var fase by remember(d) { mutableStateOf(FasePancing.Diam) }
    var jawabanBenar by remember(d) { mutableStateOf(false) }
    var menyimpan by remember(d) { mutableStateOf(false) }
    var menyelesaikan by remember(d) { mutableStateOf(false) }
    // Ikan target stage: tampil di adegan saat jawaban benar.
    val ikanTarget by produceState<IkanEntity?>(null, d.stage.ikan_target_id) {
        value = d.stage.ikan_target_id?.let { c.game.ikan(it) }
    }

    // Jangan biarkan Back sistem membatalkan penyimpanan jawaban/hasil ke Room.
    BackHandler(enabled = menyimpan || menyelesaikan) { }

    val soal = d.soal[indeks]
    val selesaiSoal = terpilih != null

    /**
     * Mengunci jawaban SETELAH dikonfirmasi pemain. Hanya di sini skor, combo, XP,
     * umpan, dan riwayat Room berubah — menekan opsi saja tidak menyentuh apa pun.
     */
    fun kunciJawaban(pilihan: PilihanKuis, posisi: Int) {
        if (menyimpan || terpilih != null) return
        terpilih = posisi
        jawabanBenar = pilihan.benar
        menyimpan = true
        fase = if (pilihan.benar) FasePancing.Dapat else FasePancing.Lepas
        if (pengaturan?.haptik != 0) {
            haptik.performHapticFeedback(
                if (pilihan.benar) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove,
            )
        }
        if (pilihan.benar) {
            benar++
            combo++
            comboMaks = maxOf(comboMaks, combo)
            xp += soal.poin
            c.audio.play(Sfx.Benar)
            scope.launch {
                c.game.catatJawaban(soal.id, pilihan.id, true, combo)
                menyimpan = false
            }
        } else {
            salah++
            combo = 0
            c.audio.play(Sfx.Salah)
            scope.launch {
                c.game.catatJawaban(soal.id, pilihan.id, false, 0)
                menyimpan = false
            }
        }
    }

    /** Membatalkan pilihan yang belum dikunci: umpan dan progres tetap utuh. */
    fun batalPilih() {
        kandidat = null
        fase = FasePancing.Diam
    }

    fun selesaikan() {
        if (menyelesaikan) return
        menyelesaikan = true
        scope.launch {
            val hasil = c.game.selesaikanStage(
                stageId = d.stage.id,
                benar = benar,
                total = d.soal.size,
                comboMaks = comboMaks,
                sempurna = salah == 0,
            )
            if (hasil != null) {
                c.audio.play(if (hasil.lulus) Sfx.Benar else Sfx.Salah)
                nav.replace(Screen.HasilStage, d.stage.id)
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(warna.latarAtas, warna.latarBawah))),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(d.stage.nama, style = MaterialTheme.typography.titleLarge)
                    Text("Soal ${indeks + 1} dari ${d.soal.size} · ${d.stage.tipe.uppercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("$xp XP", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelLarge)
            }
            BarProgres((indeks + 1f) / d.soal.size)

            MemancingScene(
                fase = fase,
                varianBoss = d.stage.tipe == "boss",
                modifier = Modifier.fillMaxWidth(),
                adegan = adeganStage(d.stage.nomor, d.stage.tipe == "boss"),
                ikan = gambarIkan(ikanTarget?.gambar ?: "ikan_teri"),
            )

            if (d.stage.tipe == "boss") {
                Kartu(Modifier.fillMaxWidth(), warna = warna.bahayaLatar) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        com.example.codefishing.frontend.components.Gambar(
                            com.example.codefishing.core.art.Art.ikan_boss_gabus,
                            "Boss Gabus",
                            Modifier.size(72.dp),
                        )
                        Column {
                            Text("Ujian Boss", style = MaterialTheme.typography.titleMedium, color = warna.bahayaTeks)
                            Text("Lulus dengan minimal 4 dari 5 jawaban benar.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Kartu(Modifier.fillMaxWidth(), warna = warna.kartuLembut) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(soal.teks, style = MaterialTheme.typography.bodyLarge)
                    KodeBlok(soal.kode)
                }
            }

            soal.pilihan.forEachIndexed { i, p ->
                val huruf = ('A' + i).toString()
                val dipilih = terpilih == i
                val kandidatIni = kandidat == i
                val benarIni = p.benar
                val warnaOpsi = when {
                    !selesaiSoal && kandidatIni -> warna.kartuTinggi
                    !selesaiSoal -> warna.kartu
                    benarIni -> warna.suksesLatar
                    dipilih -> warna.bahayaLatar
                    else -> warna.kartu
                }
                val border = when {
                    !selesaiSoal && kandidatIni -> MaterialTheme.colorScheme.primary
                    !selesaiSoal -> warna.garis
                    benarIni -> warna.suksesTeks
                    dipilih -> warna.bahayaTeks
                    else -> warna.garis
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(warnaOpsi, RoundedCornerShape(14.dp))
                        .border(1.dp, border, RoundedCornerShape(14.dp))
                        .clickable(enabled = !selesaiSoal && kandidat == null) {
                            c.audio.play(Sfx.Klik)
                            if (pengaturan?.haptik != 0) {
                                haptik.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            // Belum dikunci: hanya membuka lembar konfirmasi.
                            kandidat = i
                            fase = FasePancing.Lempar
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .background(warna.garis, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(huruf, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(p.teks, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    if (selesaiSoal && benarIni) Gambar(Art.ikon_berhasil, "Jawaban benar", Modifier.size(26.dp))
                    if (selesaiSoal && dipilih && !benarIni) Gambar(Art.ikon_error, "Jawaban salah", Modifier.size(26.dp))
                }
            }

            if (selesaiSoal) {
                Kartu(
                    Modifier.fillMaxWidth(),
                    warna = if (jawabanBenar) warna.suksesLatar else warna.bahayaLatar,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (jawabanBenar) "Tepat!" else "Belum tepat",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (jawabanBenar) warna.suksesTeks else warna.bahayaTeks,
                        )
                        Text(soal.pembahasan, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (menyimpan) {
                    Text(
                        "Menyimpan jawaban...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    TombolUtama(
                        teks = if (indeks == d.soal.size - 1) "Lihat Hasil" else "Soal Berikutnya",
                        onClick = {
                            c.audio.play(Sfx.Klik)
                            if (indeks == d.soal.size - 1) {
                                selesaikan()
                            } else {
                                indeks++
                                terpilih = null
                                kandidat = null
                                fase = FasePancing.Diam
                            }
                        },
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Benar $benar · Salah $salah · Combo $combo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "Ketuk jawaban, lalu konfirmasi untuk menguncinya.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    // Lembar konfirmasi: jawaban belum tersubmit sampai pemain menekan tombol kunci.
    val pilihanKandidat = kandidat
    if (pilihanKandidat != null) {
        val opsi = soal.pilihan[pilihanKandidat]
        ModalBottomSheet(
            onDismissRequest = { batalPilih() },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) },
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Kunci jawaban ini?", style = MaterialTheme.typography.titleLarge)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(warna.kartuLembut, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            ('A' + pilihanKandidat).toString(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Text(opsi.teks, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                }
                Text(
                    "Jawaban yang sudah dikunci tidak dapat diubah. Umpan hanya berkurang 1 jika stage selesai dengan minimal 3 jawaban salah.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TombolUtama("Ya, kunci jawaban", onClick = {
                    c.audio.play(Sfx.Klik)
                    kandidat = null
                    kunciJawaban(opsi, pilihanKandidat)
                })
                TombolSekunder("Periksa lagi", onClick = {
                    c.audio.play(Sfx.Klik)
                    batalPilih()
                })
            }
        }
    }
}

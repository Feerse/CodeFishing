package com.example.codefishing.frontend.components

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import com.example.codefishing.core.art.Art
import com.example.codefishing.pemula.ProgresPeta
import kotlin.math.max

/**
 * Jenis adegan kuis. Satu adegan punya aktor dan alat sendiri, jadi joran, jala,
 * tombak, tali es, bubu, dan pukat tidak boleh saling tertukar.
 */
enum class JenisAdegan { Kapal, Dermaga, PerahuCadik, Selam, LubangEs, Bubu, Badai }

/**
 * Urutan studi kasus per peta (nomor stage global, boss tidak termasuk).
 *
 * Studi kasus tetap sama di kedua peta; peta hanya menambah detail visual (lihat [adeganStage]).
 * Memancing di lubang es adalah ciri khas Antartika, jadi hanya muncul di sana. Stage 5 Pasifik
 * yang dulu memakai lubang es sekarang memakai kapal.
 */
private val ADEGAN_PASIFIK = mapOf(
    1 to JenisAdegan.Kapal, 2 to JenisAdegan.Dermaga, 3 to JenisAdegan.PerahuCadik,
    4 to JenisAdegan.Selam, 5 to JenisAdegan.Kapal, 6 to JenisAdegan.Bubu,
    7 to JenisAdegan.PerahuCadik, 8 to JenisAdegan.Bubu, 9 to JenisAdegan.Selam,
)
private val ADEGAN_ANTARTIKA = mapOf(
    11 to JenisAdegan.Kapal, 12 to JenisAdegan.LubangEs, 13 to JenisAdegan.Dermaga,
    14 to JenisAdegan.Selam, 15 to JenisAdegan.LubangEs, 16 to JenisAdegan.Bubu,
    17 to JenisAdegan.PerahuCadik, 18 to JenisAdegan.Selam, 19 to JenisAdegan.LubangEs,
)

/** Sumber tunggal pemetaan stage ke adegan. Dipakai juga oleh KuisScreen untuk suara aksi. */
fun jenisAdeganStage(nomor: Int, boss: Boolean): JenisAdegan = when {
    boss -> JenisAdegan.Badai
    ProgresPeta.diAntartika(nomor) -> ADEGAN_ANTARTIKA[nomor] ?: JenisAdegan.Kapal
    else -> ADEGAN_PASIFIK[nomor] ?: JenisAdegan.Kapal
}

/** True bila stage berada di peta Lautan Antartika (dipakai untuk detail es dan salju). */
fun adeganBersalju(nomor: Int): Boolean = ProgresPeta.diAntartika(nomor)

/**
 * Ilustrasi latar stage. Peta Antartika memakai varian `*_antartika` dari adegan yang sama:
 * aktor, alat, dan titik jangkar identik, hanya ditambah bongkahan es, gunung es di cakrawala,
 * salju di dermaga/atap kapal, dan aurora.
 */
@DrawableRes
fun adeganStage(nomor: Int, boss: Boolean): Int {
    val dingin = adeganBersalju(nomor)
    return when (jenisAdeganStage(nomor, boss)) {
        JenisAdegan.Kapal -> if (dingin) Art.adegan_kapal_antartika else Art.adegan_kapal
        JenisAdegan.Dermaga -> if (dingin) Art.adegan_dermaga_antartika else Art.adegan_dermaga
        JenisAdegan.PerahuCadik -> if (dingin) Art.adegan_perahu_cadik_antartika else Art.adegan_perahu_cadik
        JenisAdegan.Selam -> if (dingin) Art.adegan_penyelam_antartika else Art.adegan_penyelam
        // Lubang es hanya ada di Antartika.
        JenisAdegan.LubangEs -> Art.adegan_lubang_es_antartika
        JenisAdegan.Bubu -> if (dingin) Art.adegan_bubu_antartika else Art.adegan_bubu
        JenisAdegan.Badai ->
            if (dingin) Art.adegan_kapal_pukat_badai_antartika else Art.adegan_kapal_pukat_badai
    }
}

/**
 * Titik-titik penting di dalam gambar sumber `adegan_*` (1200 x 907), semuanya berupa
 * fraksi 0..1 supaya tetap menempel ke aktor saat gambar di-crop.
 *
 * Gambar `adegan_*` kini TIDAK memuat alat pancing (joran, tali, pelampung, tombak, jala,
 * bubu, pukat). Semua alat digambar dinamis dari titik-titik ini oleh [hitungKeadaan].
 * Arti tiap titik bergantung jenis adegan:
 *
 * - Kapal/Dermaga: pangkal = genggaman tangan, ujung = ujung joran saat diam, target = tempat
 *   pelampung mendarat, tujuan = tempat ikan diamankan (dek / ember).
 * - LubangEs: pangkal = genggaman, ujung = ujung joran pendek, target = pusat lubang,
 *   tujuan = tempat ikan jatuh di atas es.
 * - Selam: pangkal = belakang senapan, ujung = moncong, target = tempat ikan dibidik,
 *   aux = titik ikan mulai berenang masuk.
 * - PerahuCadik: pangkal = tangan penjala, target = pusat jala di air, aux = sisi lambung,
 *   tujuan = titik jala diangkat ke perahu.
 * - Bubu: pangkal = tangan, ujung = rel kapal, target = posisi bubu di dasar,
 *   aux = titik permukaan (pelampung penanda), tujuan = tempat bubu diletakkan di dek.
 * - Badai: pangkal = posisi pukat tergantung, ujung = puncak derek, target = titik air tempat
 *   pukat diturunkan, tujuan = dek.
 */
internal data class TitikAdegan(
    val pangkal: Offset,
    val ujung: Offset,
    val target: Offset,
    val tujuan: Offset,
    val aux: Offset,
    /** Panjang joran dalam piksel gambar sumber (0 bila tidak ada joran). */
    val panjang: Float,
    /** Fraksi tinggi garis air. 0 berarti adegan tanpa permukaan (bawah laut / es). */
    val dasarAir: Float,
)

private fun f(x: Float, y: Float) = Offset(x / LEBAR_GAMBAR_ADEGAN, y / TINGGI_GAMBAR_ADEGAN)

internal fun titikAdegan(jenis: JenisAdegan): TitikAdegan = when (jenis) {
    // Kapal digeser 420 px ke kanan di ilustrasi agar ada air terbuka untuk lemparan.
    JenisAdegan.Kapal -> TitikAdegan(
        pangkal = f(713f, 595f), ujung = f(575f, 465f), target = f(300f, 665f),
        tujuan = f(655f, 578f), aux = f(0f, 0f), panjang = 190f, dasarAir = 0.680f,
    )
    JenisAdegan.Dermaga -> TitikAdegan(
        pangkal = f(356f, 445f), ujung = f(426f, 312f), target = f(800f, 685f),
        tujuan = f(455f, 462f), aux = f(0f, 0f), panjang = 150f, dasarAir = 0.690f,
    )
    JenisAdegan.PerahuCadik -> TitikAdegan(
        pangkal = f(312f, 458f), ujung = f(312f, 458f), target = f(640f, 690f),
        tujuan = f(360f, 515f), aux = f(455f, 648f), panjang = 0f, dasarAir = 0.680f,
    )
    JenisAdegan.Selam -> TitikAdegan(
        pangkal = f(372f, 598f), ujung = f(428f, 597f), target = f(720f, 600f),
        tujuan = f(470f, 598f), aux = f(1000f, 575f), panjang = 0f, dasarAir = 0f,
    )
    JenisAdegan.LubangEs -> TitikAdegan(
        pangkal = f(378f, 487f), ujung = f(305f, 443f), target = f(244f, 639f),
        tujuan = f(350f, 598f), aux = f(0f, 0f), panjang = 85f, dasarAir = 0f,
    )
    // Kapal bubu juga digeser 420 px ke kanan.
    JenisAdegan.Bubu -> TitikAdegan(
        pangkal = f(676f, 612f), ujung = f(622f, 632f), target = f(520f, 745f),
        tujuan = f(632f, 592f), aux = f(520f, 675f), panjang = 0f, dasarAir = 0.690f,
    )
    JenisAdegan.Badai -> TitikAdegan(
        pangkal = f(118f, 600f), ujung = f(138f, 362f), target = f(95f, 690f),
        tujuan = f(320f, 505f), aux = f(0f, 0f), panjang = 0f, dasarAir = 0.660f,
    )
}

internal const val LEBAR_GAMBAR_ADEGAN = 1200f
internal const val TINGGI_GAMBAR_ADEGAN = 907f

/** Piksel kotak per piksel gambar sumber dengan aturan ContentScale.Crop. */
internal fun skalaGambar(lebarKotak: Float, tinggiKotak: Float): Float =
    max(lebarKotak / LEBAR_GAMBAR_ADEGAN, tinggiKotak / TINGGI_GAMBAR_ADEGAN)

/**
 * Memetakan fraksi gambar ke koordinat kotak Compose dengan aturan ContentScale.Crop.
 * Dengan begitu anchor tetap berada di tubuh aktor pada semua rasio layar.
 */
internal fun titikGambar(fx: Float, fy: Float, lebarKotak: Float, tinggiKotak: Float): Offset {
    val skala = skalaGambar(lebarKotak, tinggiKotak)
    val geserX = (lebarKotak - LEBAR_GAMBAR_ADEGAN * skala) / 2f
    val geserY = (tinggiKotak - TINGGI_GAMBAR_ADEGAN * skala) / 2f
    return Offset(
        geserX + fx * LEBAR_GAMBAR_ADEGAN * skala,
        geserY + fy * TINGGI_GAMBAR_ADEGAN * skala,
    )
}

/** Titik adegan yang sudah dikonversi ke piksel kotak, siap dipakai [hitungKeadaan]. */
internal fun titikPiksel(jenis: JenisAdegan, lebar: Float, tinggi: Float): TitikPx {
    val d = titikAdegan(jenis)
    val s = skalaGambar(lebar, tinggi)
    fun p(o: Offset) = titikGambar(o.x, o.y, lebar, tinggi)
    return TitikPx(
        pangkal = p(d.pangkal), ujung = p(d.ujung), target = p(d.target),
        tujuan = p(d.tujuan), aux = p(d.aux), panjang = d.panjang * s,
        s = s, tinggiKotak = tinggi, lebarKotak = lebar,
    )
}

/**
 * Durasi aksi "Lempar" per scene, dipakai juga oleh KuisScreen agar animasi
 * aksi selesai sebelum hasil benar/salah ditampilkan.
 */
fun durasiLemparAdegan(jenis: JenisAdegan): Int = when (jenis) {
    JenisAdegan.Selam -> 1100
    JenisAdegan.LubangEs -> 1250
    JenisAdegan.Bubu -> 1250
    JenisAdegan.PerahuCadik -> 1100
    JenisAdegan.Badai -> 1150
    JenisAdegan.Kapal, JenisAdegan.Dermaga -> 1300
}

/** Apakah adegan di bawah permukaan (tanpa garis air). */
internal fun diBawahAir(jenis: JenisAdegan): Boolean =
    jenis == JenisAdegan.Selam

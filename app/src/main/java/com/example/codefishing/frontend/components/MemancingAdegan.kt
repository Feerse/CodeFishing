package com.example.codefishing.frontend.components

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import com.example.codefishing.core.art.Art
import kotlin.math.max

/**
 * Jenis adegan kuis. Satu adegan punya aktor dan aksi sendiri, jadi joran, jala,
 * tombak, tali es, dan pukat tidak boleh saling tertukar.
 */
enum class JenisAdegan { Kapal, Dermaga, PerahuCadik, Selam, LubangEs, Bubu, Badai }

/** Sumber tunggal pemetaan stage ke adegan. Dipakai juga oleh KuisScreen untuk suara aksi. */
fun jenisAdeganStage(nomor: Int, boss: Boolean): JenisAdegan = when {
    boss -> JenisAdegan.Badai
    nomor == 2 -> JenisAdegan.Dermaga
    nomor == 3 || nomor == 7 -> JenisAdegan.PerahuCadik
    nomor == 4 || nomor == 9 -> JenisAdegan.Selam
    nomor == 5 -> JenisAdegan.LubangEs
    nomor == 6 || nomor == 8 -> JenisAdegan.Bubu
    else -> JenisAdegan.Kapal
}

@DrawableRes
fun adeganStage(nomor: Int, boss: Boolean): Int = when (jenisAdeganStage(nomor, boss)) {
    JenisAdegan.Kapal -> Art.adegan_kapal
    JenisAdegan.Dermaga -> Art.adegan_dermaga
    JenisAdegan.PerahuCadik -> Art.adegan_perahu_cadik
    JenisAdegan.Selam -> Art.adegan_penyelam
    JenisAdegan.LubangEs -> Art.adegan_lubang_es
    JenisAdegan.Bubu -> Art.adegan_bubu
    JenisAdegan.Badai -> Art.adegan_kapal_pukat_badai
}

/**
 * Titik-titik penting di dalam gambar sumber `adegan_*` (1200 x 907).
 * Semua nilai berupa fraksi 0..1 supaya tetap menempel ke aktor saat gambar di-crop.
 */
internal data class TitikAdegan(
    /** Pangkal joran / tangan aktor / asal aksi. */
    val pangkal: Offset,
    /** Ujung joran di udara. */
    val ujung: Offset,
    /** Titik tempat kail/jala/tombak/ikan berada. */
    val target: Offset,
    /** Titik akhir ikan saat berhasil ditangkap. */
    val ikanTujuan: Offset,
    /** Fraksi tinggi garis air. 0 berarti adegan tanpa permukaan (bawah laut). */
    val dasarAir: Float,
)

internal fun titikAdegan(jenis: JenisAdegan): TitikAdegan = when (jenis) {
    JenisAdegan.Kapal -> TitikAdegan(
        pangkal = Offset(0.235f, 0.630f),
        // Ujung joran yang sudah ada di ilustrasi, bukan titik di udara.
        ujung = Offset(0.245f, 0.651f),
        target = Offset(0.550f, 0.670f),
        ikanTujuan = Offset(0.400f, 0.560f),
        dasarAir = 0.670f,
    )
    JenisAdegan.Dermaga -> TitikAdegan(
        pangkal = Offset(0.178f, 0.560f),
        // Ujung joran nelayan dermaga pada ilustrasi.
        ujung = Offset(0.197f, 0.512f),
        target = Offset(0.560f, 0.690f),
        ikanTujuan = Offset(0.360f, 0.610f),
        dasarAir = 0.690f,
    )
    JenisAdegan.PerahuCadik -> TitikAdegan(
        pangkal = Offset(0.256f, 0.610f),
        ujung = Offset(0.350f, 0.540f),
        target = Offset(0.580f, 0.680f),
        ikanTujuan = Offset(0.340f, 0.590f),
        dasarAir = 0.680f,
    )
    JenisAdegan.Selam -> TitikAdegan(
        pangkal = Offset(0.310f, 0.660f),
        ujung = Offset(0.310f, 0.660f),
        target = Offset(0.430f, 0.660f),
        ikanTujuan = Offset(0.340f, 0.600f),
        dasarAir = 0f,
    )
    JenisAdegan.LubangEs -> TitikAdegan(
        pangkal = Offset(0.400f, 0.380f),
        ujung = Offset(0.400f, 0.380f),
        target = Offset(0.460f, 0.610f),
        ikanTujuan = Offset(0.460f, 0.500f),
        dasarAir = 0f,
    )
    JenisAdegan.Bubu -> TitikAdegan(
        pangkal = Offset(0.420f, 0.180f),
        ujung = Offset(0.420f, 0.180f),
        target = Offset(0.363f, 0.752f),
        ikanTujuan = Offset(0.410f, 0.450f),
        dasarAir = 0f,
    )
    JenisAdegan.Badai -> TitikAdegan(
        pangkal = Offset(0.330f, 0.480f),
        ujung = Offset(0.400f, 0.430f),
        target = Offset(0.580f, 0.660f),
        ikanTujuan = Offset(0.420f, 0.520f),
        dasarAir = 0.660f,
    )
}

internal const val LEBAR_GAMBAR_ADEGAN = 1200f
internal const val TINGGI_GAMBAR_ADEGAN = 907f

/**
 * Memetakan fraksi gambar ke koordinat kotak Compose dengan aturan ContentScale.Crop.
 * Dengan begitu anchor tetap berada di tubuh aktor pada semua rasio layar.
 */
internal fun titikGambar(fx: Float, fy: Float, lebarKotak: Float, tinggiKotak: Float): Offset {
    val skala = max(lebarKotak / LEBAR_GAMBAR_ADEGAN, tinggiKotak / TINGGI_GAMBAR_ADEGAN)
    val geserX = (lebarKotak - LEBAR_GAMBAR_ADEGAN * skala) / 2f
    val geserY = (tinggiKotak - TINGGI_GAMBAR_ADEGAN * skala) / 2f
    return Offset(
        geserX + fx * LEBAR_GAMBAR_ADEGAN * skala,
        geserY + fy * TINGGI_GAMBAR_ADEGAN * skala,
    )
}

/** Durasi aksi "Lempar" per scene, dipakai juga oleh KuisScreen agar animasi
 *  aksi selesai sebelum hasil benar/salah ditampilkan. */
fun durasiLemparAdegan(jenis: JenisAdegan): Int = when (jenis) {
    JenisAdegan.Selam -> 950
    JenisAdegan.LubangEs -> 1150
    JenisAdegan.Bubu -> 1250
    JenisAdegan.PerahuCadik -> 900
    JenisAdegan.Badai -> 1050
    else -> 820
}

/** Apakah adegan memakai joran dan tali dari permukaan. */
internal fun memakaiJoran(jenis: JenisAdegan): Boolean =
    jenis == JenisAdegan.Kapal || jenis == JenisAdegan.Dermaga

/** Apakah adegan memakai jala atau pukat. */
internal fun memakaiJala(jenis: JenisAdegan): Boolean =
    jenis == JenisAdegan.PerahuCadik || jenis == JenisAdegan.Badai

/** Apakah adegan berada di bawah permukaan air. */
internal fun diBawahAir(jenis: JenisAdegan): Boolean =
    jenis == JenisAdegan.Selam || jenis == JenisAdegan.Bubu
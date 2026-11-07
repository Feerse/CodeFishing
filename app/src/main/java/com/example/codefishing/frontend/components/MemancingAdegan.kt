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
    /** Ujung joran di udara, tempat tali menggantung. */
    val ujung: Offset,
    /** Posisi kail dan pelampung saat adegan tenang (belum dilempar). */
    val istirahat: Offset,
    /** Titik tempat kail/jala/tombak/ikan berakhir setelah aksi. */
    val target: Offset,
    /** Titik akhir ikan saat berhasil ditangkap. */
    val ikanTujuan: Offset,
    /** Fraksi tinggi garis air. 0 berarti adegan tanpa permukaan (bawah laut). */
    val dasarAir: Float,
)

/**
 * Semua anchor diukur dari ilustrasi `adegan_*` yang sudah dibersihkan dari
 * properti bawaan. Jadi tali, pelampung, jala, tombak, dan pukat di bawah ini
 * adalah satu-satunya properti yang tampil di layar.
 */
internal fun titikAdegan(jenis: JenisAdegan): TitikAdegan = when (jenis) {
    JenisAdegan.Kapal -> TitikAdegan(
        pangkal = Offset(0.175f, 0.600f),
        ujung = Offset(0.126f, 0.512f),
        istirahat = Offset(0.104f, 0.768f),
        target = Offset(0.550f, 0.670f),
        ikanTujuan = Offset(0.225f, 0.590f),
        dasarAir = 0.496f,
    )
    JenisAdegan.Dermaga -> TitikAdegan(
        pangkal = Offset(0.325f, 0.420f),
        ujung = Offset(0.343f, 0.377f),
        istirahat = Offset(0.477f, 0.765f),
        target = Offset(0.633f, 0.684f),
        ikanTujuan = Offset(0.430f, 0.600f),
        dasarAir = 0.496f,
    )
    JenisAdegan.PerahuCadik -> TitikAdegan(
        // Jala berangkat dari tangan penjala, bukan dari ujung joran.
        pangkal = Offset(0.257f, 0.507f),
        ujung = Offset(0.257f, 0.507f),
        istirahat = Offset(0.257f, 0.507f),
        target = Offset(0.464f, 0.745f),
        ikanTujuan = Offset(0.290f, 0.600f),
        dasarAir = 0.496f,
    )
    JenisAdegan.Selam -> TitikAdegan(
        // Mulut tombak di depan penyelam, bukan di tengah badannya.
        pangkal = Offset(0.360f, 0.657f),
        ujung = Offset(0.360f, 0.657f),
        istirahat = Offset(0.360f, 0.657f),
        target = Offset(0.633f, 0.662f),
        ikanTujuan = Offset(0.430f, 0.620f),
        dasarAir = 0f,
    )
    JenisAdegan.LubangEs -> TitikAdegan(
        pangkal = Offset(0.292f, 0.525f),
        ujung = Offset(0.266f, 0.500f),
        // Pelampung istirahat tepat di tengah lubang es.
        istirahat = Offset(0.203f, 0.700f),
        target = Offset(0.203f, 0.700f),
        ikanTujuan = Offset(0.300f, 0.620f),
        dasarAir = 0f,
    )
    JenisAdegan.Bubu -> TitikAdegan(
        // Tali bubu menggantung dari ujung joran ke mulut bubu.
        pangkal = Offset(0.142f, 0.593f),
        ujung = Offset(0.142f, 0.593f),
        istirahat = Offset(0.109f, 0.854f),
        target = Offset(0.109f, 0.854f),
        ikanTujuan = Offset(0.205f, 0.600f),
        dasarAir = 0f,
    )
    JenisAdegan.Badai -> TitikAdegan(
        // Pukat ditebar dari ujung boom, bukan dari tengah kapal.
        pangkal = Offset(0.111f, 0.386f),
        ujung = Offset(0.111f, 0.386f),
        istirahat = Offset(0.111f, 0.386f),
        target = Offset(0.600f, 0.728f),
        ikanTujuan = Offset(0.260f, 0.600f),
        dasarAir = 0.496f,
    )
}

internal const val LEBAR_GAMBAR_ADEGAN = 1200f
internal const val TINGGI_GAMBAR_ADEGAN = 907f

/**
 * Memetakan fraksi gambar ke koordinat kotak Compose dengan aturan ContentScale.Crop.
 * Dengan begitu anchor tetap berada di tubuh aktor pada semua rasio layar.
 * [ratakanBawah] dipakai adegan yang objeknya menempel di dasar ilustrasi (bubu),
 * supaya objek itu tidak ikut terpotong saat kotak lebih pendek dari gambar.
 */
internal fun titikGambar(
    fx: Float,
    fy: Float,
    lebarKotak: Float,
    tinggiKotak: Float,
    ratakanBawah: Boolean = false,
): Offset {
    val skala = max(lebarKotak / LEBAR_GAMBAR_ADEGAN, tinggiKotak / TINGGI_GAMBAR_ADEGAN)
    val geserX = (lebarKotak - LEBAR_GAMBAR_ADEGAN * skala) / 2f
    val geserY = if (ratakanBawah) {
        tinggiKotak - TINGGI_GAMBAR_ADEGAN * skala
    } else {
        (tinggiKotak - TINGGI_GAMBAR_ADEGAN * skala) / 2f
    }
    return Offset(
        geserX + fx * LEBAR_GAMBAR_ADEGAN * skala,
        geserY + fy * TINGGI_GAMBAR_ADEGAN * skala,
    )
}

/** Durasi animasi aksi "Lempar" per adegan. Dipakai MemancingScene agar tempo
 *  lemparan tiap alat cocok dengan jarak dan jenis aksinya. */
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
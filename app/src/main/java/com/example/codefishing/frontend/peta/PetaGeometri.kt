package com.example.codefishing.frontend.peta

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

/** Titik pada ruang peta dunia 1000 x 2800. */
data class TitikPeta(val x: Float, val y: Float) {
    fun keOffset(): Offset = Offset(x, y)
}

/**
 * Geometri peta perairan vertikal.
 * Dua peta memakai dunia 1000 x 2800 yang sama:
 * Samudra Pasifik (stage 1-10) dan Antartika (stage 11-20).
 */
object PetaGeometri {
    const val LEBAR_DUNIA = 1000f
    const val TINGGI_DUNIA = 2800f

    /** Jumlah stage per peta (Pasifik dan Antartika masing-masing 10). */
    const val JUMLAH_STAGE = 10

    val mulai = TitikPeta(96f, 154f)
    val gerbang = TitikPeta(575f, 2365f)

    /** Titik masuk peta Antartika, tepat di bawah lapisan es utara. */
    val mulaiAntartika = TitikPeta(470f, 226f)

    /** Ujung rute Antartika, di lapangan es selatan. */
    val ujungAntartika = TitikPeta(700f, 2390f)

    /** Posisi tetap 10 stage, berliku dari atas ke bawah. Sinkron dengan glow di generate_peta_svg.js. */
    val stage: List<TitikPeta> = listOf(
        TitikPeta(220f, 340f),
        TitikPeta(430f, 540f),
        TitikPeta(720f, 730f),
        TitikPeta(820f, 940f),
        TitikPeta(600f, 1120f),
        TitikPeta(360f, 1310f),
        TitikPeta(230f, 1490f),
        TitikPeta(450f, 1670f),
        TitikPeta(700f, 1860f),
        TitikPeta(820f, 2080f),
    )

    /** Posisi 10 stage Antartika (lokal 1-10 = stage global 11-20). */
    val stageAntartika: List<TitikPeta> = listOf(
        TitikPeta(560f, 320f),
        TitikPeta(300f, 520f),
        TitikPeta(180f, 760f),
        TitikPeta(430f, 940f),
        TitikPeta(700f, 1100f),
        TitikPeta(840f, 1300f),
        TitikPeta(620f, 1490f),
        TitikPeta(380f, 1690f),
        TitikPeta(560f, 1900f),
        TitikPeta(700f, 2120f),
    )

    fun titikStage(nomor: Int): TitikPeta? = stage.getOrNull(nomor - 1)

    fun titikStageAntartika(nomorLokal: Int): TitikPeta? = stageAntartika.getOrNull(nomorLokal - 1)

    /** Rute penuh termasuk titik mulai dan gerbang Antartika. */
    fun ruteLengkap(): List<TitikPeta> = listOf(mulai) + stage + gerbang

    /** Rute penuh Antartika: titik masuk, 10 stage, lalu ujung selatan. */
    fun ruteLengkapAntartika(): List<TitikPeta> =
        listOf(mulaiAntartika) + stageAntartika + ujungAntartika

    /** Rute yang sudah ditempuh: mulai + stage yang sudah selesai. */
    fun ruteSelesai(jumlahSelesai: Int): List<TitikPeta> =
        listOf(mulai) + stage.take(jumlahSelesai.coerceIn(0, JUMLAH_STAGE))

    /** Rute Antartika yang sudah ditempuh. */
    fun ruteSelesaiAntartika(jumlahSelesai: Int): List<TitikPeta> =
        listOf(mulaiAntartika) + stageAntartika.take(jumlahSelesai.coerceIn(0, JUMLAH_STAGE))

    /** Posisi kapal: posisi terakhir pemain, lalu stage aktif, lalu stage 1. */
    fun nomorKapal(nomorTerakhir: Int?, nomorAktif: Int?): Int =
        nomorTerakhir?.takeIf { it in 1..JUMLAH_STAGE }
            ?: nomorAktif?.takeIf { it in 1..JUMLAH_STAGE }
            ?: 1

    /** Path halus Catmull-Rom yang diubah menjadi kurva Bezier kubik. */
    fun pathDari(titik: List<TitikPeta>, skalaX: Float = 1f, skalaY: Float = 1f): Path {
        val path = Path()
        if (titik.isEmpty()) return path
        path.moveTo(titik[0].x * skalaX, titik[0].y * skalaY)
        for (i in 0 until titik.lastIndex) {
            val p0 = titik.getOrElse(i - 1) { titik[i] }
            val p1 = titik[i]
            val p2 = titik[i + 1]
            val p3 = titik.getOrElse(i + 2) { p2 }
            val c1x = (p1.x + (p2.x - p0.x) / 6f) * skalaX
            val c1y = (p1.y + (p2.y - p0.y) / 6f) * skalaY
            val c2x = (p2.x - (p3.x - p1.x) / 6f) * skalaX
            val c2y = (p2.y - (p3.y - p1.y) / 6f) * skalaY
            path.cubicTo(c1x, c1y, c2x, c2y, p2.x * skalaX, p2.y * skalaY)
        }
        return path
    }
}

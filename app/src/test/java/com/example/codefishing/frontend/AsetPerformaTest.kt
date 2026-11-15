package com.example.codefishing.frontend

import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Menjaga hasil optimasi performa aset (lihat docs/PERFORMA.md): ikan tidak boleh kembali menjadi
 * bitmap besar, peta harus berupa WebP hasil render sekali (bukan SVG yang dirender di HP).
 * Unit test berjalan dengan direktori kerja modul `app`.
 */
class AsetPerformaTest {
    private val res = File("src/main/res/drawable-nodpi")
    private val peta = File("src/main/assets/peta")

    private fun png(prefix: String, akhiran: String? = null) = res.listFiles { f ->
        f.name.startsWith(prefix) && f.name.endsWith(".png") &&
            (akhiran == null || f.name.endsWith(akhiran))
    }.orEmpty().sortedBy { it.name }

    private fun sisiTerpanjang(f: File): Int {
        val img = ImageIO.read(f)
        return maxOf(img.width, img.height)
    }

    @Test
    fun petaBerupaWebpDanTanpaSvgDiApk() {
        val nama = listOf("peta_perairan", "peta_perairan_terang", "peta_antartika", "peta_antartika_terang")
        for (n in nama) {
            val f = File(peta, "$n.webp")
            assertTrue("$n.webp hilang", f.exists())
            assertTrue("$n.webp terlalu besar (${f.length() / 1024} KB)", f.length() < 400 * 1024)
        }
        assertTrue("SVG tidak boleh ikut APK", peta.listFiles { f -> f.name.endsWith(".svg") }.isNullOrEmpty())
    }

    @Test
    fun ikanPenuhTidakMelebihi448Piksel() {
        val ikan = png("ikan_").filter { !it.name.endsWith("_kecil.png") }
        assertTrue(ikan.size >= 100)
        for (f in ikan) assertTrue("${f.name} ${sisiTerpanjang(f)} px", sisiTerpanjang(f) <= 448)
    }

    @Test
    fun setiapIkanPunyaThumbnailKecil() {
        val penuh = png("ikan_").filter { !it.name.endsWith("_kecil.png") }.map { it.name.removeSuffix(".png") }
        for (n in penuh) {
            val kecil = File(res, "${n}_kecil.png")
            assertTrue("${n}_kecil.png hilang", kecil.exists())
            assertTrue("${kecil.name} ${sisiTerpanjang(kecil)} px", sisiTerpanjang(kecil) <= 256)
        }
    }

    @Test
    fun gridKoleksiTidakMelebihiAnggaranMemori() {
        // 58 thumbnail berwarna yang didekode bersamaan oleh grid Koleksi.
        val kecil = png("ikan_", "_kecil.png").filter { !it.name.startsWith("ikan_terkunci_") }
        var byte = 0L
        for (f in kecil) {
            val img = ImageIO.read(f)
            byte += img.width.toLong() * img.height * 4
        }
        assertTrue("jumlah thumbnail ${kecil.size}", kecil.size >= 50)
        assertTrue("grid ${byte / 1048576} MB", byte < 15L * 1024 * 1024)
    }

    @Test
    fun latarPenuhTetapResolusiAsli() {
        // Latar/adegan penuh sengaja tidak dikecilkan (tampil selebar layar).
        val adegan = png("adegan_")
        assertTrue(adegan.isNotEmpty())
        for (f in adegan) assertEquals(f.name, 1200, ImageIO.read(f).width)
        assertFalse(png("latar_").isEmpty())
    }
}

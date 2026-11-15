package com.example.codefishing.frontend

import androidx.compose.ui.geometry.Offset
import com.example.codefishing.frontend.components.FasePancing
import com.example.codefishing.frontend.components.JenisAdegan
import com.example.codefishing.frontend.components.hitungKeadaan
import com.example.codefishing.frontend.components.titikPiksel
import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Menjaga animasi adegan Mode Belajar: tidak ada alat ganda, tidak ada lompatan antar fase,
 * dan tidak ada umpan/pelampung tertinggal setelah hasil jawaban keluar.
 */
class GerakPancingTest {
    private val lebar = 720f
    private val tinggi = 420f

    private fun jarakSumber(a: Offset?, b: Offset?, skala: Float): Float {
        if (a == null || b == null) return 0f
        return hypot(a.x - b.x, a.y - b.y) / skala
    }

    @Test
    fun tidakAdaNaNDiSemuaFrame() {
        for (jenis in JenisAdegan.values()) {
            val t = titikPiksel(jenis, lebar, tinggi)
            for (berhasil in listOf(true, false)) for (fase in FasePancing.values()) for (i in 0..50) {
                val st = hitungKeadaan(jenis, fase, i / 50f, berhasil, t, 0.3f, 1f)
                for (p in listOf(st.alat, st.pelampung, st.kail, st.batangUjung, st.ikan, st.tombakUjung)) {
                    if (p != null) assertFalse("$jenis $fase $i", p.x.isNaN() || p.y.isNaN())
                }
            }
        }
    }

    @Test
    fun akhirLemparSamaDenganAwalDapatDanLepas() {
        for (jenis in JenisAdegan.values()) {
            val t = titikPiksel(jenis, lebar, tinggi)
            // Hasil jawaban sudah diketahui sejak Lempar, jadi parameter berhasil sama dalam satu kali main.
            for ((fase, berhasil) in listOf(FasePancing.Dapat to true, FasePancing.Lepas to false)) {
                val akhir = hitungKeadaan(jenis, FasePancing.Lempar, 1f, berhasil, t, 0.3f, 1f)
                val awal = hitungKeadaan(jenis, fase, 0f, berhasil, t, 0.3f, 1f)
                assertTrue("$jenis $fase alat", jarakSumber(akhir.alat, awal.alat, t.s) < 8f)
                assertTrue("$jenis $fase batang", jarakSumber(akhir.batangUjung, awal.batangUjung, t.s) < 8f)
                assertTrue("$jenis $fase kail", jarakSumber(akhir.kail, awal.kail, t.s) < 8f)
                assertTrue("$jenis $fase tombak", jarakSumber(akhir.tombakUjung, awal.tombakUjung, t.s) < 8f)
            }
        }
    }

    @Test
    fun alatKembaliSiapSetelahDapatTanpaIkanTertinggal() {
        for (jenis in listOf(
            JenisAdegan.Kapal, JenisAdegan.Dermaga, JenisAdegan.LubangEs,
            JenisAdegan.Selam, JenisAdegan.PerahuCadik, JenisAdegan.Badai,
        )) {
            val t = titikPiksel(jenis, lebar, tinggi)
            val diam = hitungKeadaan(jenis, FasePancing.Diam, 0f, true, t, 0.3f, 1f)
            val akhir = hitungKeadaan(jenis, FasePancing.Dapat, 1f, true, t, 0.3f, 1f)
            assertTrue("$jenis ikan hilang", akhir.ikanAlpha < 0.01f)
            assertTrue("$jenis alat", jarakSumber(akhir.alat, diam.alat, t.s) < 12f)
            assertTrue("$jenis pelampung", jarakSumber(akhir.pelampung, diam.pelampung, t.s) < 12f)
            assertTrue("$jenis kail", jarakSumber(akhir.kail, diam.kail, t.s) < 12f)
            assertTrue("$jenis batang", jarakSumber(akhir.batangUjung, diam.batangUjung, t.s) < 6f)
        }
    }

    @Test
    fun akhirLepasTidakMeninggalkanPelampungAtauIkan() {
        for (jenis in JenisAdegan.values()) {
            val t = titikPiksel(jenis, lebar, tinggi)
            val akhir = hitungKeadaan(jenis, FasePancing.Lepas, 1f, false, t, 0.3f, 1f)
            assertTrue("$jenis ikan", akhir.ikanAlpha < 0.01f)
        }
        // Joran: tali putus, pelampung dan kail ikut hanyut lalu hilang.
        for (jenis in listOf(JenisAdegan.Kapal, JenisAdegan.Dermaga)) {
            val t = titikPiksel(jenis, lebar, tinggi)
            val akhir = hitungKeadaan(jenis, FasePancing.Lepas, 1f, false, t, 0.3f, 1f)
            assertTrue("$jenis pelampung", akhir.pelampung == null || akhir.pelampungAlpha < 0.02f)
        }
    }

    @Test
    fun hanyaSatuSetAlatUntukSetiapJenisAdegan() {
        // Joran hanya di adegan berjoran, tombak hanya di adegan menyelam, dst.
        for (jenis in JenisAdegan.values()) {
            val t = titikPiksel(jenis, lebar, tinggi)
            val st = hitungKeadaan(jenis, FasePancing.Diam, 0f, true, t, 0.3f, 1f)
            val berjoran = jenis in listOf(JenisAdegan.Kapal, JenisAdegan.Dermaga, JenisAdegan.LubangEs)
            assertEquals("$jenis joran", berjoran, st.batangUjung != null)
            assertEquals("$jenis tombak", jenis == JenisAdegan.Selam, st.tombakUjung != null)
            val beralatGambar = jenis in listOf(JenisAdegan.PerahuCadik, JenisAdegan.Bubu, JenisAdegan.Badai)
            assertEquals("$jenis alat gambar", beralatGambar, st.alat != null)
            if (jenis == JenisAdegan.Selam) assertNull(st.pelampung) else Unit
            if (berjoran) assertNotNull(st.tali)
        }
    }
}

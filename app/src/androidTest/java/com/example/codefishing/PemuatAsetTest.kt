package com.example.codefishing

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.codefishing.frontend.components.PemuatAset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Memastikan layar pembuka benar-benar memuat seluruh katalog aset sebelum aplikasi dibuka,
 * sehingga tidak ada gambar yang muncul terlambat saat layar pertama dipakai.
 */
@RunWith(AndroidJUnit4::class)
class PemuatAsetTest {
    @Test
    fun semuaAsetTermuat() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val mulai = System.currentTimeMillis()
        // Tema terang memuat varian _terang sekaligus membuat kontur, jalur paling berat.
        PemuatAset.mulai(context, gelap = false)
        while (!PemuatAset.status.value.selesaiSemua && System.currentTimeMillis() - mulai < 180_000) {
            Thread.sleep(100)
        }
        val akhir = PemuatAset.status.value
        val laporan = "selesai=${akhir.selesai} total=${akhir.total} " +
            "teks=${akhir.teks} waktu=${System.currentTimeMillis() - mulai} ms"
        assertTrue("Pemuatan aset tidak selesai: $laporan", akhir.selesaiSemua)
        assertTrue("Katalog aset tidak termuat: $laporan", akhir.total > 300)
        assertEquals("Jumlah aset dimuat tidak cocok: $laporan", akhir.total, akhir.selesai)
    }
}

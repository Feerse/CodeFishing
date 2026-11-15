package com.example.codefishing.expert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PanduanExpertTest {
    private fun misiUji() = MisiPeta(
        kode = "uji",
        nomor = 1,
        judul = "Uji",
        deskripsi = "Uji",
        grid = 4,
        cells = listOf("nila", "sampah") + List(14) { "" },
        kodeAwal = "",
        hint = "",
        goalTipe = "free",
        goalN = 0,
        goalSpesies = null,
        par = null,
        xp = 0,
        dbId = 1,
    )

    @Test
    fun semuaContohPerintahBerjalanTanpaError() {
        PanduanExpert.perintah.forEach { item ->
            val contoh = item.contoh
            assertNotNull("${item.nama} belum punya contoh", contoh)
            val run = SandboxEngine().jalankan(contoh!!, misiUji())
            assertTrue("${item.nama}: ${run.hasil.pesanError}", run.hasil.berhasil)
        }
    }

    @Test
    fun semuaContohSintaksBerjalanTanpaError() {
        PanduanExpert.sintaks.forEach { item ->
            val contoh = item.contoh
            assertNotNull("${item.judul} belum punya contoh", contoh)
            val run = SandboxEngine().jalankan(contoh!!, misiUji())
            assertTrue("${item.judul}: ${run.hasil.pesanError}", run.hasil.berhasil)
        }
    }

    @Test
    fun perintahPentingTerdokumentasi() {
        val nama = PanduanExpert.perintah.map { it.nama }
        listOf(
            "gerak(arah)",
            "lempar()",
            "lihat()",
            "get_x() / get_y()",
            "ukuran()",
            "jumlah()",
        ).forEach { wajib ->
            assertTrue("$wajib tidak ada di panduan", wajib in nama)
        }
    }

    @Test
    fun labelTombolPintasanUnik() {
        val label = PanduanExpert.tombolSisip.map { it.label }
        assertEquals(label.size, label.toSet().size)
    }

    @Test
    fun tombolPintasanMenaruhKursorDenganBenar() {
        val gerak = PanduanExpert.tombolSisip.first { it.label == "gerak()" }
        val hasilGerak = EditorKode.sisipkan("", 0, 0, gerak.potongan, gerak.kursorMundur)
        assertEquals("gerak()", hasilGerak.teks)
        assertEquals(6, hasilGerak.kursor)

        val def = PanduanExpert.tombolSisip.first { it.label == "def" }
        val hasilDef = EditorKode.sisipkan("", 0, 0, def.potongan, def.kursorMundur)
        assertEquals("def ():", hasilDef.teks)
        assertEquals(4, hasilDef.kursor)
    }
}
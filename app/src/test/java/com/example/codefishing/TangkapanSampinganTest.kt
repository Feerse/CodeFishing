package com.example.codefishing

import com.example.codefishing.pemula.TangkapanSampingan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TangkapanSampinganTest {

    @Test
    fun peluangBeradaDiRentangWajar() {
        assertTrue(TangkapanSampingan.PELUANG > 0.0)
        assertTrue(TangkapanSampingan.PELUANG < 1.0)
    }

    @Test
    fun wilayahMenentukanLautDalam() {
        assertFalse(TangkapanSampingan.lautDalam(1))
        assertFalse(TangkapanSampingan.lautDalam(10))
        assertTrue(TangkapanSampingan.lautDalam(11))
        assertTrue(TangkapanSampingan.lautDalam(20))
    }

    @Test
    fun tierCocokDenganWilayah() {
        listOf("biasa", "langka").forEach {
            assertTrue(it, TangkapanSampingan.tierCocok(it, lautDalam = false))
            assertFalse(it, TangkapanSampingan.tierCocok(it, lautDalam = true))
        }
        listOf("epik", "legenda", "mitos").forEach {
            assertTrue(it, TangkapanSampingan.tierCocok(it, lautDalam = true))
            assertFalse(it, TangkapanSampingan.tierCocok(it, lautDalam = false))
        }
        assertFalse(TangkapanSampingan.tierCocok("tidak-ada", lautDalam = false))
        assertFalse(TangkapanSampingan.tierCocok("tidak-ada", lautDalam = true))
    }

    @Test
    fun bobotMenurunSesuaiKelangkaan() {
        val urut = listOf("biasa", "langka", "epik", "legenda", "mitos")
            .map { TangkapanSampingan.bobot(it) }
        assertEquals(urut.sortedDescending(), urut)
        assertTrue(urut.all { it > 0 })
    }

    @Test
    fun undianMemilihSesuaiBobot() {
        val kandidat = listOf("biasa" to 3, "langka" to 2, "mitos" to 1)
        val daftar = kandidat.map { it.first }
        val bobot = { x: String -> kandidat.first { it.first == x }.second }
        // Total bobot 6: 0-2 biasa, 3-4 langka, 5 mitos.
        assertEquals("biasa", TangkapanSampingan.pilih(daftar, bobot, 0))
        assertEquals("biasa", TangkapanSampingan.pilih(daftar, bobot, 2))
        assertEquals("langka", TangkapanSampingan.pilih(daftar, bobot, 3))
        assertEquals("langka", TangkapanSampingan.pilih(daftar, bobot, 4))
        assertEquals("mitos", TangkapanSampingan.pilih(daftar, bobot, 5))
        // Undian di luar rentang tetap aman.
        assertEquals("biasa", TangkapanSampingan.pilih(daftar, bobot, -5))
        assertEquals("mitos", TangkapanSampingan.pilih(daftar, bobot, 99))
    }

    @Test
    fun kandidatMenyaringHadiahKhususDanIkanTanpaAset() {
        fun cek(
            asal: Int? = null,
            tier: String = "biasa",
            syarat: String? = "Tangkapan sampingan di perairan Pasifik",
            gambar: String? = "ikan_kakap_merah",
            dalam: Boolean = false,
        ) = TangkapanSampingan.kandidat(asal, tier, syarat, gambar, dalam)

        assertTrue(cek())
        assertFalse("punya stage", cek(asal = 3))
        assertFalse("hadiah khusus", cek(syarat = TangkapanSampingan.SYARAT_KHUSUS))
        assertFalse("tanpa gambar", cek(gambar = null))
        assertFalse("tanda tanya", cek(gambar = "tanda_tanya_ikan"))
        assertFalse("tier beda wilayah", cek(tier = "mitos"))
        assertTrue(cek(tier = "mitos", dalam = true))
        assertFalse(cek(tier = "biasa", dalam = true))
    }

    @Test
    fun kandidatKosongMenghasilkanNull() {
        assertNull(TangkapanSampingan.pilih(emptyList<String>(), { 1 }, 0))
    }
}

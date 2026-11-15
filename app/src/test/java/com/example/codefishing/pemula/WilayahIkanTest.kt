package com.example.codefishing.pemula

import org.junit.Assert.assertEquals
import org.junit.Test

class WilayahIkanTest {
    @Test
    fun ikanStageMengikutiPetaStageNya() {
        // Stage 1-10 di Samudra Pasifik, 11-20 di Lautan Antartika.
        assertEquals(WilayahIkan.PASIFIK, WilayahIkanAturan.dari(1, "biasa", "Selesaikan Stage 1"))
        assertEquals(WilayahIkan.PASIFIK, WilayahIkanAturan.dari(10, "legenda", "Selesaikan Stage 10"))
        assertEquals(WilayahIkan.ANTARTIKA, WilayahIkanAturan.dari(11, "biasa", "Selesaikan Stage 11"))
        assertEquals(WilayahIkan.ANTARTIKA, WilayahIkanAturan.dari(20, "mitos", "Selesaikan Stage 20"))
    }

    @Test
    fun tangkapanSampinganMengikutiTier() {
        // Aturan yang sama dengan pengundian TangkapanSampingan: Biasa/Langka di Pasifik,
        // Epik sampai Mitos hanya di laut dalam (Antartika).
        assertEquals(WilayahIkan.PASIFIK, WilayahIkanAturan.dari(null, "biasa", "Tangkapan sampingan di perairan Pasifik"))
        assertEquals(WilayahIkan.PASIFIK, WilayahIkanAturan.dari(null, "langka", "Tangkapan langka di perairan Pasifik"))
        assertEquals(WilayahIkan.ANTARTIKA, WilayahIkanAturan.dari(null, "epik", "Tangkapan langka di laut dalam"))
        assertEquals(WilayahIkan.ANTARTIKA, WilayahIkanAturan.dari(null, "legenda", "Tangkapan sangat langka di laut dalam"))
        assertEquals(WilayahIkan.ANTARTIKA, WilayahIkanAturan.dari(null, "mitos", "Hanya muncul di laut dalam"))
    }

    @Test
    fun hadiahKhususTidakTerikatPeta() {
        // Ikan hadiah combo bertier Langka/Mitos, jadi tier saja akan salah menempatkannya.
        assertEquals(WilayahIkan.KHUSUS, WilayahIkanAturan.dari(null, "langka", "Tantangan khusus"))
        assertEquals(WilayahIkan.KHUSUS, WilayahIkanAturan.dari(null, "mitos", "Tantangan khusus"))
        assertEquals(WilayahIkan.KHUSUS, WilayahIkanAturan.dari(3, "biasa", "Tantangan khusus"))
    }
}

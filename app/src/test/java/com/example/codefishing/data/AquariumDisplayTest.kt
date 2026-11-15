package com.example.codefishing.data

import com.example.codefishing.core.data.HasilTambahAkuarium
import com.example.codefishing.core.data.KAPASITAS_AKUARIUM
import com.example.codefishing.core.data.cekTambahAkuarium
import com.example.codefishing.core.data.jumlahPerBaris
import org.junit.Assert.assertEquals
import org.junit.Test

class AquariumDisplayTest {
    @Test
    fun bolehTambahSaatAdaRuangDanStok() {
        assertEquals(
            HasilTambahAkuarium.Berhasil,
            cekTambahAkuarium(jumlahDiAkuarium = 2, stokKoleksi = 5, totalDiAkuarium = 4),
        )
    }

    @Test
    fun penuhSaatTotalMencapaiKapasitas() {
        assertEquals(
            HasilTambahAkuarium.Penuh,
            cekTambahAkuarium(
                jumlahDiAkuarium = 0,
                stokKoleksi = 5,
                totalDiAkuarium = KAPASITAS_AKUARIUM,
            ),
        )
    }

    @Test
    fun stokHabisSaatJumlahSpesiesMencapaiKoleksi() {
        assertEquals(
            HasilTambahAkuarium.StokHabis,
            cekTambahAkuarium(jumlahDiAkuarium = 3, stokKoleksi = 3, totalDiAkuarium = 5),
        )
    }

    @Test
    fun penuhDidahulukanSebelumStokHabis() {
        assertEquals(
            HasilTambahAkuarium.Penuh,
            cekTambahAkuarium(
                jumlahDiAkuarium = 5,
                stokKoleksi = 5,
                totalDiAkuarium = KAPASITAS_AKUARIUM,
            ),
        )
    }

    @Test
    fun stokNolBerartiTidakBisaDitambah() {
        assertEquals(
            HasilTambahAkuarium.StokHabis,
            cekTambahAkuarium(jumlahDiAkuarium = 0, stokKoleksi = 0, totalDiAkuarium = 0),
        )
    }

    @Test
    fun kapasitasNolMenolakTambahan() {
        assertEquals(
            HasilTambahAkuarium.Penuh,
            cekTambahAkuarium(jumlahDiAkuarium = 0, stokKoleksi = 1, totalDiAkuarium = 0, kapasitas = 0),
        )
    }

    @Test
    fun sepuluhIkanDibagiRataKeEmpatBaris() {
        assertEquals(listOf(3, 3, 2, 2), jumlahPerBaris(10))
        assertEquals(listOf(2, 2), jumlahPerBaris(4))
        assertEquals(listOf(3, 2, 2), jumlahPerBaris(7))
        assertEquals(emptyList<Int>(), jumlahPerBaris(0))
    }
}
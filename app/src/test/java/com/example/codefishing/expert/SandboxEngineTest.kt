package com.example.codefishing.expert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SandboxEngineTest {
    private fun misi(
        kode: String,
        grid: Int,
        cells: List<String>,
        goalTipe: String,
        goalN: Int = 0,
        goalSpesies: String? = null,
        par: Int? = null,
    ) = MisiPeta(
        kode = kode,
        nomor = 1,
        judul = "Tes",
        deskripsi = "Tes",
        grid = grid,
        cells = cells,
        kodeAwal = "",
        hint = "",
        goalTipe = goalTipe,
        goalN = goalN,
        goalSpesies = goalSpesies,
        par = par,
        xp = 0,
        dbId = 1,
    )

    @Test
    fun lemparIkanPertama() {
        val m = misi("m1", 3, List(9) { if (it == 0) "teri" else "" }, "catch", 1, par = 2)
        val run = SandboxEngine().jalankan("lempar()", m)
        assertTrue(run.hasil.berhasil)
        assertTrue(run.evaluasi.sukses)
        assertEquals(1, run.hasil.tertangkap)
        assertEquals(3, run.evaluasi.bintang)
    }

    @Test
    fun loopLimaIkan() {
        val cells = listOf("teri", "teri", "teri", "teri", "teri") + List(20) { "" }
        val m = misi("m2", 5, cells, "catch", 5, par = 14)
        val kode = """
            for i in range(5):
                lempar()
                if i < 4:
                    gerak(Timur)
        """.trimIndent()
        val run = SandboxEngine().jalankan(kode, m)
        assertTrue(run.hasil.berhasil)
        assertTrue(run.evaluasi.sukses)
        assertEquals(5, run.hasil.tertangkap)
    }

    @Test
    fun sapuEmpatKaliEmpat() {
        val m = misi("m3", 4, List(16) { "mujair" }, "all", par = 48)
        val kode = """
            for baris in range(4):
                for kolom in range(4):
                    lempar()
                    if kolom < 3:
                        gerak(Timur)
                if baris < 3:
                    gerak(Utara)
                    for balik in range(3):
                        gerak(Barat)
        """.trimIndent()
        val run = SandboxEngine().jalankan(kode, m)
        assertTrue(run.hasil.berhasil)
        assertTrue(run.evaluasi.sukses)
        assertEquals(16, run.hasil.tertangkap)
        assertEquals(3, run.evaluasi.bintang)
    }

    @Test
    fun sampahMembuatMisiGagal() {
        val m = misi("m4", 2, listOf("sampah", "", "", ""), "all")
        val run = SandboxEngine().jalankan("lempar()", m)
        assertTrue(run.hasil.berhasil)
        assertFalse(run.evaluasi.sukses)
        assertEquals(1, run.hasil.sampah)
    }

    @Test
    fun gerakDiTepiMengembalikanFalseDanTetapHitungTick() {
        val dunia = Dunia(3, List(9) { "" })
        assertFalse(dunia.gerak(Arah.Barat))
        assertEquals(0, dunia.x)
        assertEquals(1, dunia.ticks)
    }

    @Test
    fun berburuGurameSaja() {
        val cells = listOf(
            "", "", "teri", "sampah", "gurame", "sampah",
            "", "gurame", "", "teri", "", "",
            "sampah", "gurame", "teri", "teri", "", "teri",
            "", "", "gurame", "gurame", "", "teri",
            "teri", "gurame", "teri", "", "teri", "gurame",
            "", "teri", "teri", "gurame", "teri", "teri",
        )
        val m = misi("m5", 6, cells, "only", goalSpesies = "gurame", par = 40)
        val kode = """
            for baris in range(6):
                for kolom in range(6):
                    if lihat() == "gurame":
                        lempar()
                    if kolom < 5:
                        if baris % 2 == 0:
                            gerak(Timur)
                        else:
                            gerak(Barat)
                if baris < 5:
                    gerak(Utara)
        """.trimIndent()
        val run = SandboxEngine().jalankan(kode, m)
        assertTrue(run.hasil.berhasil)
        assertTrue(run.evaluasi.sukses)
        assertTrue(run.hasil.tangkapan.all { it == "gurame" })
    }

    @Test
    fun fungsiPanenBisaMenyapuSemuaBaris() {
        val m = misi("m6", 4, List(16) { "bandeng" }, "def", par = 36)
        val kode = """
            def panen_baris(arah):
                for kolom in range(3):
                    lempar()
                    gerak(arah)
                lempar()

            for baris in range(4):
                if baris % 2 == 0:
                    panen_baris(Timur)
                else:
                    panen_baris(Barat)
                if baris < 3:
                    gerak(Utara)
        """.trimIndent()
        val run = SandboxEngine().jalankan(kode, m)
        assertTrue(run.hasil.pesanError ?: "tanpa error", run.hasil.berhasil)
        assertTrue(run.evaluasi.sukses)
        assertTrue(run.hasil.defDigunakan)
        assertEquals(16, run.hasil.tertangkap)
        assertEquals(3, run.evaluasi.bintang)
    }

    @Test
    fun barisKosongDanKomentarDiDalamBlok() {
        val cells = listOf("teri", "teri") + List(7) { "" }
        val m = misi("m2", 3, cells, "catch", 2)
        val kode = """
            for i in range(2):
                # komentar di dalam blok
                lempar()

                gerak(Timur)
        """.trimIndent()
        val run = SandboxEngine().jalankan(kode, m)
        assertTrue(run.hasil.pesanError ?: "tanpa error", run.hasil.berhasil)
        assertEquals(2, run.hasil.tertangkap)
    }

    @Test
    fun errorSintaksDilaporkan() {
        val m = misi("m1", 3, List(9) { "" }, "catch", 1)
        val run = SandboxEngine().jalankan("print(", m)
        assertFalse(run.hasil.berhasil)
        assertTrue(run.hasil.pesanError?.contains("SyntaxError") == true)
    }
}

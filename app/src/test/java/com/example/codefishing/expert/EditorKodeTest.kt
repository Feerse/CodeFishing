package com.example.codefishing.expert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EditorKodeTest {
    @Test
    fun sisipDiKursor() {
        val hasil = EditorKode.sisipkan("lempar()", 8, 8, "\ngerak(Timur)")
        assertEquals("lempar()\ngerak(Timur)", hasil.teks)
        assertEquals(21, hasil.kursor)
    }

    @Test
    fun gantiSeleksi() {
        val hasil = EditorKode.sisipkan("gerak(Timur)", 0, 12, "gerak(Utara)")
        assertEquals("gerak(Utara)", hasil.teks)
        assertEquals(12, hasil.kursor)
    }

    @Test
    fun seleksiTerbalikTetapAman() {
        val hasil = EditorKode.sisipkan("abcdef", 5, 2, "X")
        assertEquals("abXf", hasil.teks)
        assertEquals(3, hasil.kursor)
    }

    @Test
    fun indeksDiLuarBatasDijepit() {
        val diAkhir = EditorKode.sisipkan("abc", 99, 99, "X")
        assertEquals("abcX", diAkhir.teks)
        assertEquals(4, diAkhir.kursor)

        val diAwal = EditorKode.sisipkan("abc", -5, -5, "X")
        assertEquals("Xabc", diAwal.teks)
        assertEquals(1, diAwal.kursor)
    }

    @Test
    fun kursorMundurMasukKeDalamKurung() {
        val gerak = EditorKode.sisipkan("", 0, 0, "gerak()", 1)
        assertEquals("gerak()", gerak.teks)
        assertEquals(6, gerak.kursor)

        val def = EditorKode.sisipkan("", 0, 0, "def ():", 3)
        assertEquals("def ():", def.teks)
        assertEquals(4, def.kursor)

        val forChip = EditorKode.sisipkan("", 0, 0, "for i in range():", 2)
        assertEquals("for i in range():", forChip.teks)
        assertEquals(15, forChip.kursor)
    }

    @Test
    fun barisSebelumHanyaSaatAdaNewline() {
        assertEquals("lempar()", EditorKode.barisSebelum("lempar()\n", 9))
        assertEquals("b", EditorKode.barisSebelum("a\nb\n", 4))
        assertEquals("", EditorKode.barisSebelum("", 0))
        assertEquals("", EditorKode.barisSebelum("abc", 2))
    }

    @Test
    fun indentasiMengikutiBarisSebelumnya() {
        assertEquals("", EditorKode.indentasiSetelahBaris("lempar()"))
        assertEquals("    ", EditorKode.indentasiSetelahBaris("if lihat() == \"nila\":"))
        assertEquals("        ", EditorKode.indentasiSetelahBaris("    if lihat() == \"nila\":"))
        assertEquals("    ", EditorKode.indentasiSetelahBaris("    "))
        assertEquals("\t    ", EditorKode.indentasiSetelahBaris("\tfor i in range(3):"))
    }

    @Test
    fun deteksiNewlineUntukIndentasiOtomatis() {
        assertEquals("    ", EditorKode.indentasiUntukBarisBaru("if x:", "if x:\n", 6))
        assertEquals("", EditorKode.indentasiUntukBarisBaru("lempar()", "lempar()\n", 9))
        assertNull(EditorKode.indentasiUntukBarisBaru("a", "ab", 2))
        assertNull(EditorKode.indentasiUntukBarisBaru("a", "a\nb", 3))
        assertNull(EditorKode.indentasiUntukBarisBaru("a", "a\n", 0))
    }

    @Test
    fun kurangiIndentasiBarisAktif() {
        val empat = EditorKode.kurangiIndentasi("    lempar()", 12)
        assertEquals("lempar()", empat.teks)
        assertEquals(8, empat.kursor)

        val dua = EditorKode.kurangiIndentasi("  lempar()", 10)
        assertEquals("lempar()", dua.teks)
        assertEquals(8, dua.kursor)

        val tanpa = EditorKode.kurangiIndentasi("lempar()", 4)
        assertEquals("lempar()", tanpa.teks)
        assertEquals(4, tanpa.kursor)

        val multiBaris = EditorKode.kurangiIndentasi("    a\n    b", 11)
        assertEquals("    a\nb", multiBaris.teks)
        assertEquals(7, multiBaris.kursor)
    }
}
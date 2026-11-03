package com.example.codefishing.expert

/**
 * Operasi teks murni untuk editor Sandbox. Tidak bergantung pada Compose
 * supaya aturan sisip kursor dan indentasi bisa diuji unit.
 */
data class HasilSisip(val teks: String, val kursor: Int)

object EditorKode {
    /** Ganti rentang [mulai]..[akhir] dengan [potongan], lalu taruh kursor [kursorMundur] dari ujung. */
    fun sisipkan(
        teks: String,
        mulai: Int,
        akhir: Int,
        potongan: String,
        kursorMundur: Int = 0,
    ): HasilSisip {
        val a = mulai.coerceIn(0, teks.length)
        val b = akhir.coerceIn(0, teks.length)
        val awal = minOf(a, b)
        val selesai = maxOf(a, b)
        val baru = teks.substring(0, awal) + potongan + teks.substring(selesai)
        val mundur = kursorMundur.coerceIn(0, potongan.length)
        val kursor = (awal + potongan.length - mundur).coerceIn(awal, awal + potongan.length)
        return HasilSisip(baru, kursor)
    }

    /** Baris sebelum posisi kursor, hanya bila karakter tepat sebelum kursor adalah newline. */
    fun barisSebelum(teks: String, kursor: Int): String {
        val pos = kursor.coerceIn(0, teks.length)
        if (pos == 0 || teks[pos - 1] != '\n') return ""
        val akhir = pos - 1
        val awal = teks.lastIndexOf('\n', akhir - 1) + 1
        return teks.substring(awal, akhir)
    }

    /** Indentasi untuk baris baru: ikuti baris sebelumnya, tambah 4 spasi setelah ':'. */
    fun indentasiSetelahBaris(barisLalu: String): String {
        val indent = barisLalu.takeWhile { it == ' ' || it == '\t' }
        return indent + if (barisLalu.trimEnd().endsWith(":")) "    " else ""
    }

    /**
     * Deteksi satu newline yang baru diketik lalu hitung indentasinya.
     * null berarti bukan penyisipan newline tunggal; string kosong berarti tidak perlu indentasi.
     */
    fun indentasiUntukBarisBaru(lama: String, baru: String, kursor: Int): String? {
        if (baru.length != lama.length + 1) return null
        if (baru.count { it == '\n' } != lama.count { it == '\n' } + 1) return null
        if (kursor <= 0 || kursor > baru.length || baru[kursor - 1] != '\n') return null
        return indentasiSetelahBaris(barisSebelum(baru, kursor))
    }

    /** Kurangi indentasi baris tempat kursor berada sebanyak maksimal [jumlah] spasi. */
    fun kurangiIndentasi(teks: String, kursor: Int, jumlah: Int = 4): HasilSisip {
        val pos = kursor.coerceIn(0, teks.length)
        val awalBaris = teks.lastIndexOf('\n', pos - 1) + 1
        var hapus = 0
        while (hapus < jumlah && awalBaris + hapus < pos && teks[awalBaris + hapus] == ' ') hapus++
        if (hapus == 0) return HasilSisip(teks, pos)
        val baru = teks.removeRange(awalBaris, awalBaris + hapus)
        return HasilSisip(baru, (pos - hapus).coerceAtLeast(awalBaris))
    }
}
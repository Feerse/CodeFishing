package com.example.codefishing.expert

/**
 * Draf kode editor per misi selama aplikasi berjalan.
 * Mencegah kode pemain kembali ke contoh saat berpindah layar/misi.
 */
object SandboxDraft {
    private val peta = mutableMapOf<Int, String>()

    fun ambil(nomor: Int, awal: String): String = peta[nomor] ?: awal

    fun simpan(nomor: Int, kode: String) {
        peta[nomor] = kode
    }
}

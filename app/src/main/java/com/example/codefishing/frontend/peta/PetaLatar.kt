package com.example.codefishing.frontend.peta

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Latar peta berupa gambar WebP 1000x2800 yang dirender sekali dari SVG saat pembuatan aset
 * (2_Aset_dan_Konten/tools/optimalkan_aset.py), bukan dirender AndroidSVG di HP.
 *
 * Dulu layar peta merender SVG lewat CPU tiap kali dibuka (beberapa detik di HP menengah, sambil
 * bersaing dengan komposisi layar). Sekarang cukup mendekode gambar (puluhan milidetik) dan hasilnya
 * disimpan untuk dua peta terakhir, sehingga pindah peta atau membuka ulang layar peta langsung tampil.
 */
object PetaLatar {
    // Dua entri: peta yang sedang dilihat dan peta lawannya (mis. Pasifik <-> Antartika).
    private val cache = LruCache<String, ImageBitmap>(2)

    private fun nama(terang: Boolean, antartika: Boolean): String {
        val dasar = if (antartika) "peta_antartika" else "peta_perairan"
        return "peta/$dasar${if (terang) "_terang" else ""}.webp"
    }

    /** Peta yang sudah ada di memori, atau null. Aman dipanggil di thread utama. */
    fun dapat(terang: Boolean, antartika: Boolean): ImageBitmap? = cache.get(nama(terang, antartika))

    /**
     * Memuat peta (mendekode bila belum ada di memori). Panggil dari thread latar. Pemanggilan
     * bersamaan diserialkan sehingga peta tidak didekode dua kali (mis. pemanasan + layar peta).
     */
    fun ambil(context: Context, terang: Boolean, antartika: Boolean): ImageBitmap? {
        val kunci = nama(terang, antartika)
        val ada = cache.get(kunci)
        if (ada != null) return ada
        return synchronized(this) {
            cache.get(kunci) ?: runCatching {
                val opsi = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                val bitmap = context.assets.open(kunci).use { BitmapFactory.decodeStream(it, null, opsi) }
                    ?: error("Peta tidak bisa didekode: $kunci")
                bitmap.asImageBitmap()
            }.onFailure {
                Log.e("PetaLatar", "Gagal memuat peta $kunci", it)
            }.getOrNull()?.also { cache.put(kunci, it) }
        }
    }
}

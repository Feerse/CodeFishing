package com.example.codefishing.frontend.peta

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import com.caverock.androidsvg.SVG

/** Merender latar SVG sekali per tema lalu menyimpannya di memori. */
object PetaSvg {
    private const val LEBAR_BITMAP = 1000
    private const val TINGGI_BITMAP = 2800

    @Volatile
    private var cache: ImageBitmap? = null

    @Volatile
    private var cacheKunci: Pair<Boolean, Boolean>? = null

    /** [terang] memilih peta versi siang; [antartika] memilih peta kedua (stage 11-20). */
    fun ambil(context: Context, terang: Boolean, antartika: Boolean = false): ImageBitmap? {
        val kunci = terang to antartika
        cache?.takeIf { cacheKunci == kunci }?.let { return it }
        return synchronized(this) {
            cache?.takeIf { cacheKunci == kunci } ?: runCatching {
                val dasar = if (antartika) "peta/peta_antartika" else "peta/peta_perairan"
                val nama = if (terang) "${dasar}_terang.svg" else "$dasar.svg"
                val svg = context.assets.open(nama).use { input ->
                    SVG.getFromInputStream(input)
                }
                svg.setDocumentWidth(LEBAR_BITMAP.toFloat())
                svg.setDocumentHeight(TINGGI_BITMAP.toFloat())
                val bitmap = createBitmap(LEBAR_BITMAP, TINGGI_BITMAP, Bitmap.Config.ARGB_8888)
                svg.renderToCanvas(Canvas(bitmap))
                bitmap.asImageBitmap()
            }.onFailure {
                Log.e("PetaSvg", "Gagal merender peta (terang=$terang, antartika=$antartika)", it)
            }.getOrNull()?.also {
                cache = it
                cacheKunci = kunci
            }
        }
    }
}

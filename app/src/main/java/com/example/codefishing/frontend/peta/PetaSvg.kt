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
    private var cacheTerang: Boolean? = null

    /** [terang] memilih peta versi siang; cache lama diganti agar tidak menyimpan dua bitmap besar. */
    fun ambil(context: Context, terang: Boolean): ImageBitmap? {
        cache?.takeIf { cacheTerang == terang }?.let { return it }
        return synchronized(this) {
            cache?.takeIf { cacheTerang == terang } ?: runCatching {
                val nama = if (terang) "peta/peta_perairan_terang.svg" else "peta/peta_perairan.svg"
                val svg = context.assets.open(nama).use { input ->
                    SVG.getFromInputStream(input)
                }
                svg.setDocumentWidth(LEBAR_BITMAP.toFloat())
                svg.setDocumentHeight(TINGGI_BITMAP.toFloat())
                val bitmap = createBitmap(LEBAR_BITMAP, TINGGI_BITMAP, Bitmap.Config.ARGB_8888)
                svg.renderToCanvas(Canvas(bitmap))
                bitmap.asImageBitmap()
            }.onFailure {
                Log.e("PetaSvg", "Gagal merender peta (terang=$terang)", it)
            }.getOrNull()?.also {
                cache = it
                cacheTerang = terang
            }
        }
    }
}

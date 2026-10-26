package com.example.codefishing.frontend.peta

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import com.caverock.androidsvg.SVG

/** Merender latar SVG sekali lalu menyimpannya di memori. */
object PetaSvg {
    private const val LEBAR_BITMAP = 1000
    private const val TINGGI_BITMAP = 2800

    @Volatile
    private var cache: ImageBitmap? = null

    fun ambil(context: Context): ImageBitmap? {
        cache?.let { return it }
        return synchronized(this) {
            cache ?: runCatching {
                val svg = context.assets.open("peta/peta_perairan.svg").use { input ->
                    SVG.getFromInputStream(input)
                }
                svg.setDocumentWidth(LEBAR_BITMAP.toFloat())
                svg.setDocumentHeight(TINGGI_BITMAP.toFloat())
                val bitmap = createBitmap(LEBAR_BITMAP, TINGGI_BITMAP, Bitmap.Config.ARGB_8888)
                svg.renderToCanvas(Canvas(bitmap))
                bitmap.asImageBitmap()
            }.onFailure {
                Log.e("PetaSvg", "Gagal merender peta_perairan.svg", it)
            }.getOrNull()?.also { cache = it }
        }
    }
}

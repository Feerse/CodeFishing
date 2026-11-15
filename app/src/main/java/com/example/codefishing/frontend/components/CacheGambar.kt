package com.example.codefishing.frontend.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Process
import android.util.LruCache
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Cache bitmap aset di memori.
 *
 * `painterResource` mendekode PNG di thread utama setiap kali layar dibuka dan tidak menyimpan hasilnya,
 * sehingga tiap perpindahan menu mendekode ulang puluhan gambar (penyebab utama patah-patah di HP
 * menengah). Di sini hasil dekode disimpan (LRU berbatas memori), dan aset yang pasti dipakai segera
 * dipanaskan di thread latar berprioritas rendah saat layar pembuka diputar.
 */
object CacheGambar {
    // Bitmap berada di memori native. Batas dinaikkan dari 1/6 ke 1/4 heap: layar pembuka memuat
    // seluruh katalog aset lebih dulu, dan sebagian besar aset kecil (ikon, lencana, thumbnail ikan)
    // muat sekaligus sehingga tidak perlu didekode ulang saat layar dibuka. Batas atas 128 MB tetap
    // konservatif agar HP 3-4 GB RAM tidak kehabisan memori.
    private val batasByte: Int = (Runtime.getRuntime().maxMemory() / 4)
        .coerceIn(48L * 1024 * 1024, 128L * 1024 * 1024).toInt()

    private val cache = object : LruCache<Int, ImageBitmap>(batasByte) {
        override fun sizeOf(key: Int, value: ImageBitmap): Int = value.width * value.height * 4
    }

    /** Gambar dengan kontur gelap untuk tema terang. [skala] mengembalikan ukuran ikan seperti semula. */
    class Kontur(val bitmap: ImageBitmap, val skala: Float)

    private val cacheKontur = object : LruCache<Int, Kontur>(batasByte / 2) {
        override fun sizeOf(key: Int, value: Kontur): Int = value.bitmap.width * value.bitmap.height * 4
    }

    private val latar = Executors.newSingleThreadExecutor { r ->
        Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            r.run()
        }, "cache-gambar").apply { isDaemon = true }
    }

    /** Bitmap yang sudah ada di memori, atau null. Aman dipanggil di thread utama. */
    fun dapat(id: Int): ImageBitmap? = cache.get(id)

    /**
     * Bitmap aset: dari cache bila ada, bila tidak didekode lalu disimpan. Mengembalikan null untuk
     * aset yang bukan bitmap (mis. vektor XML) agar pemanggil memakai `painterResource`.
     */
    fun ambil(context: Context, id: Int): ImageBitmap? {
        val ada = cache.get(id)
        if (ada != null) return ada
        val opsi = BitmapFactory.Options().apply {
            inScaled = false // semua aset ada di drawable-nodpi
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = runCatching { BitmapFactory.decodeResource(context.resources, id, opsi) }.getOrNull()
            ?: return null
        return bitmap.asImageBitmap().also { cache.put(id, it) }
    }

    fun daptKontur(id: Int): Kontur? = cacheKontur.get(id)

    /** Membuat (sekali) versi aset dengan kontur gelap tipis + bayangan lembut. Panggil dari thread latar. */
    fun buatKontur(context: Context, id: Int): Kontur? {
        val adaKontur = cacheKontur.get(id)
        if (adaKontur != null) return adaKontur
        val sumber = ambil(context, id)?.asAndroidBitmap() ?: return null
        val w = sumber.width
        val h = sumber.height
        val r = max(2, (max(w, h) * 0.014f).roundToInt())
        val hasil = Bitmap.createBitmap(w + 2 * r, h + 2 * r, Bitmap.Config.ARGB_8888)
        val kanvas = Canvas(hasil)
        val bayangan = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = PorterDuffColorFilter(0x330F2F3C, PorterDuff.Mode.SRC_IN)
        }
        val tinta = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = PorterDuffColorFilter(0xEB0F2F3C.toInt(), PorterDuff.Mode.SRC_IN)
        }
        kanvas.drawBitmap(sumber, r.toFloat(), r + r * 0.8f, bayangan)
        val arah = listOf(
            1f to 0f, -1f to 0f, 0f to 1f, 0f to -1f,
            0.7f to 0.7f, -0.7f to 0.7f, 0.7f to -0.7f, -0.7f to -0.7f,
        )
        for ((dx, dy) in arah) kanvas.drawBitmap(sumber, r + dx * r, r + dy * r, tinta)
        kanvas.drawBitmap(sumber, r.toFloat(), r.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
        val kontur = Kontur(hasil.asImageBitmap(), (w + 2f * r) / w)
        cacheKontur.put(id, kontur)
        return kontur
    }

    /** Memanaskan sejumlah aset di thread latar (tidak menunggu). */
    fun hangatkan(context: Context, ids: List<Int>) {
        val app = context.applicationContext
        latar.execute { ids.forEach { runCatching { ambil(app, it) } } }
    }
}

/**
 * Painter dari [CacheGambar] untuk kode yang membutuhkan `Painter` (bukan `Image` langsung), mis.
 * `Image(painter = ...)` atau `Canvas`. Mengembalikan `painterResource` untuk aset non-bitmap.
 */
@Composable
fun rememberPainterGambar(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val bitmap = remember(id) { CacheGambar.ambil(context, id) }
    return if (bitmap != null) remember(bitmap) { BitmapPainter(bitmap) } else painterResource(id)
}

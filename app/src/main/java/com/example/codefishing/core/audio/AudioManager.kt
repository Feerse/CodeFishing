package com.example.codefishing.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.annotation.RawRes
import com.example.codefishing.R
import com.example.codefishing.core.data.ProgressRepository
import java.util.concurrent.ConcurrentHashMap

/**
 * Efek suara. Berkas ada di app/src/main/res/raw dan dicatat di assets-source/audio.
 * Pemetaan peristiwa -> suara adalah USUL awal, boleh diubah tim lewat enum ini saja.
 */
enum class Sfx(@RawRes val res: Int) {
    /** Klik tombol biasa. */
    Klik(R.raw.sfx_klik_1),
    /** Buka/tutup drawer hamburger dan tombol utama (Mulai Main, Lanjutkan). */
    KlikUtama(R.raw.sfx_klik_2),
    /** Jawaban kuis benar. */
    Benar(R.raw.sfx_benar),
    /** Jawaban kuis salah (umpan berkurang). */
    Salah(R.raw.sfx_salah_1),
    /** Stage gagal / umpan habis. */
    Gagal(R.raw.sfx_salah_2),
    /** Kode Sandbox error. */
    KodeError(R.raw.sfx_error),
    /** Lempar kail di Sandbox dan animasi kuis. */
    Cipratan(R.raw.sfx_cipratan),
    /** Ikan baru didapat (toast). */
    IkanBaru(R.raw.sfx_sukses_4),
    /** Misi Sandbox selesai (1 bintang). */
    Sukses1Bintang(R.raw.sfx_sukses_2),
    /** Stage lulus. */
    StageLulus(R.raw.sfx_sukses_1),
    /** Misi 3 bintang dan Boss lulus. */
    Sukses3Bintang(R.raw.sfx_sukses_3),
}

class AudioManager(context: Context, private val progress: ProgressRepository) {
    private val appContext = context.applicationContext

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val ids = HashMap<Sfx, Int>()
    private val siap: MutableSet<Int> = ConcurrentHashMap.newKeySet()
    private var bgm: MediaPlayer? = null

    init {
        pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) siap.add(id) }
        Sfx.entries.forEach { ids[it] = pool.load(appContext, it.res, 1) }
    }

    fun play(sfx: Sfx) {
        if (!progress.sfxOn) return
        val id = ids[sfx] ?: return
        if (id in siap) pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    /**
     * Musik latar (BGM-Aquarium, loop), diputar di SEMUA layar. Aman dipanggil berulang.
     * Mengikuti saklar Musik di Pengaturan.
     */
    fun startBgm() {
        if (!progress.musicOn) return
        if (bgm == null) {
            bgm = MediaPlayer.create(appContext, R.raw.bgm_aquarium)?.apply {
                isLooping = true
                setVolume(0.4f, 0.4f)
            }
        }
        bgm?.let { if (!it.isPlaying) it.start() }
    }

    fun stopBgm() {
        bgm?.let { if (it.isPlaying) it.pause() }
    }

    /** Dipanggil saat app ke latar belakang. */
    fun onAppPause() = stopBgm()

    /** Dipanggil saat app kembali tampil. */
    fun onAppResume() = startBgm()

    /** Dipanggil saat saklar Musik di Pengaturan berubah. */
    fun onMusicSettingChanged() {
        if (progress.musicOn) startBgm() else stopBgm()
    }

    fun release() {
        bgm?.release()
        bgm = null
        pool.release()
    }
}

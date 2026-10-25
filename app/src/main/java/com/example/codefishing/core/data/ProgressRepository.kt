package com.example.codefishing.core.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Penyimpanan progres offline (SharedPreferences) untuk tahap kerangka.
 * Tahap database/ERD nanti menggantikan isi kelas ini (mis. Room), antarmuka publiknya dijaga tetap.
 */
class ProgressRepository(context: Context) {
    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("codefishing_progress", Context.MODE_PRIVATE)

    var onboardingSeen: Boolean
        get() = sp.getBoolean("onboarding_seen", false)
        set(v) = sp.edit().putBoolean("onboarding_seen", v).apply()

    var sfxOn: Boolean
        get() = sp.getBoolean("sfx_on", true)
        set(v) = sp.edit().putBoolean("sfx_on", v).apply()

    var musicOn: Boolean
        get() = sp.getBoolean("music_on", true)
        set(v) = sp.edit().putBoolean("music_on", v).apply()

    /** Jumlah umpan tersimpan. -1 berarti belum pernah diset (anggap penuh). */
    var umpan: Int
        get() = sp.getInt("umpan", -1)
        set(v) = sp.edit().putInt("umpan", v).apply()

    /** Waktu (ms epoch) mulai hitung pemulihan umpan, 0 bila umpan penuh. */
    var umpanSejak: Long
        get() = sp.getLong("umpan_sejak", 0L)
        set(v) = sp.edit().putLong("umpan_sejak", v).apply()

    /** Persen terbaik tiap stage (0 bila belum pernah). Indeks 0..9. */
    fun bestPercent(stage: Int): Int = sp.getInt("stage_best_$stage", 0)

    fun setBestPercent(stage: Int, persen: Int) {
        if (persen > bestPercent(stage)) sp.edit().putInt("stage_best_$stage", persen).apply()
    }

    /** Stage berikutnya yang boleh dibuka (0 = hanya stage 1). */
    var stageTerbuka: Int
        get() = sp.getInt("stage_terbuka", 0)
        set(v) = sp.edit().putInt("stage_terbuka", v).apply()

    /** Bintang terbaik tiap misi sandbox (0..3). */
    fun misiBintang(id: String): Int = sp.getInt("misi_bintang_$id", 0)

    fun setMisiBintang(id: String, bintang: Int) {
        if (bintang > misiBintang(id)) sp.edit().putInt("misi_bintang_$id", bintang).apply()
    }

    /** Hapus semua progres. Pengaturan suara dan status pengenalan ikut dijaga. */
    fun resetProgres() {
        val sfx = sfxOn
        val music = musicOn
        val seen = onboardingSeen
        sp.edit().clear().apply()
        sfxOn = sfx
        musicOn = music
        onboardingSeen = seen
    }
}

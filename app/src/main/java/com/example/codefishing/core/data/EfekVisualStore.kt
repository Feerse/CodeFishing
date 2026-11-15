package com.example.codefishing.core.data

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tingkat kepadatan efek visual.
 *
 * Yang berubah hanya jumlah dan jenis efek (partikel, lapisan air, goyangan papan), BUKAN
 * resolusi aset. Ketajaman gambar tetap sama di ketiga tingkat; ini penting karena menurunkan
 * resolusi justru membuat UI blur dan bukan itu tujuannya.
 */
enum class EfekVisual(val nilai: String, val label: String, val keterangan: String) {
    Otomatis(
        "otomatis",
        "Otomatis",
        "Ikuti kemampuan perangkat. Paling aman untuk semua HP.",
    ),
    Penuh(
        "penuh",
        "Penuh",
        "Semua efek aktif. Pilih bila perangkat Anda kuat.",
    ),
    Hemat(
        "hemat",
        "Hemat",
        "Efek dikurangi agar tetap mulus di perangkat kelas bawah.",
    );

    companion object {
        fun dari(nilai: String?): EfekVisual =
            entries.firstOrNull { it.nilai == nilai } ?: Otomatis

        /**
         * Perangkat dianggap kelas bawah bila sistem menandainya low-RAM. Ambang ini yang dipakai
         * Android sendiri untuk memutuskan aplikasi mana yang boleh dipangkas lebih agresif.
         */
        fun perangkatLemah(context: Context): Boolean {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return false
            return am.isLowRamDevice
        }
    }
}

/**
 * Menyimpan pilihan kepadatan efek di SharedPreferences.
 *
 * Sengaja tidak memakai tabel `pengaturan` di Room: nilai ini dibaca hampir semua komponen
 * gambar, jadi harus tersedia sebelum database selesai dibuka (sama seperti tema). Menyimpannya
 * di prefs juga menghindari migrasi skema yang berisiko menghapus progres pemain.
 */
class EfekVisualStore(context: Context) {
    private val appContext = context.applicationContext
    private val sp = appContext.getSharedPreferences("codefishing_efek", Context.MODE_PRIVATE)

    private val _pilihan = MutableStateFlow(EfekVisual.dari(sp.getString(KEY_EFEK, null)))
    val pilihan: StateFlow<EfekVisual> = _pilihan.asStateFlow()

    /**
     * Tingkat yang benar-benar dipakai. "Otomatis" diterjemahkan sekali di sini, bukan di setiap
     * komponen, supaya semua layar memakai keputusan yang sama.
     */
    fun aktif(pilihan: EfekVisual): EfekVisual = when (pilihan) {
        EfekVisual.Hemat, EfekVisual.Penuh -> pilihan
        EfekVisual.Otomatis -> if (perangkatLemah) EfekVisual.Hemat else EfekVisual.Penuh
    }

    private val perangkatLemah: Boolean by lazy { EfekVisual.perangkatLemah(appContext) }

    fun pilih(mode: EfekVisual) {
        _pilihan.value = mode
        sp.edit { putString(KEY_EFEK, mode.nilai) }
    }

    private companion object {
        const val KEY_EFEK = "efek_visual"
    }
}

/**
 * Tingkat efek yang berlaku sekarang, dibagikan ke seluruh komponen gambar.
 *
 * Disimpan sebagai CompositionLocal agar `MemancingScene`, `PapanMisi`, dan `Kelangkaan` tidak
 * perlu mengumpulkan flow masing-masing (setiap kolektor berarti satu rekomposisi per perubahan).
 */
val LocalEfekVisual = staticCompositionLocalOf { EfekVisual.Penuh }

/** Apakah efek dekoratif harus dikurangi. */
val EfekVisual.hemat: Boolean get() = this == EfekVisual.Hemat

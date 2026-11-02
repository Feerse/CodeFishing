package com.example.codefishing.core.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Batas panjang nama panggilan ikan di Akuarium. */
const val MAKS_NAMA_AKUARIUM = 24

/** Rapikan nama panggilan; null berarti kembali memakai nama asli dari katalog. */
fun normalisasiNamaAkuarium(teks: String): String? =
    teks.trim().take(MAKS_NAMA_AKUARIUM).ifBlank { null }

/**
 * Nama panggilan ikan khusus tampilan Akuarium. Disimpan di SharedPreferences
 * terpisah agar nama katalog di Koleksi tidak berubah dan nama tetap ada
 * setelah aplikasi ditutup.
 */
class AquariumNameStore(context: Context) {
    private val sp = context.applicationContext
        .getSharedPreferences("codefishing_aquarium", Context.MODE_PRIVATE)

    private val _nama = MutableStateFlow(bacaSemua())
    val nama: StateFlow<Map<Int, String>> = _nama.asStateFlow()

    fun simpan(ikanId: Int, teks: String) {
        val bersih = normalisasiNamaAkuarium(teks)
        sp.edit {
            if (bersih == null) remove(kunci(ikanId)) else putString(kunci(ikanId), bersih)
        }
        _nama.value = bacaSemua()
    }

    private fun bacaSemua(): Map<Int, String> = sp.all.mapNotNull { (kunci, nilai) ->
        if (!kunci.startsWith(PREFIX)) return@mapNotNull null
        val id = kunci.removePrefix(PREFIX).toIntOrNull() ?: return@mapNotNull null
        val teks = (nilai as? String)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        id to teks
    }.toMap()

    private fun kunci(ikanId: Int) = "$PREFIX$ikanId"

    private companion object {
        const val PREFIX = "nama_"
    }
}
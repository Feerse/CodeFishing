package com.example.codefishing.core.i18n

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Bahasa antarmuka yang tersedia. */
enum class Bahasa(val nilai: String, val label: String) {
    Indonesia("id", "Indonesia"),
    Inggris("en", "English");

    companion object {
        fun dari(nilai: String?): Bahasa = entries.firstOrNull { it.nilai == nilai } ?: Indonesia
    }
}

/**
 * Menyimpan pilihan bahasa di SharedPreferences.
 *
 * Sama seperti tema dan kepadatan efek: dibaca sebelum database siap, dan sengaja tidak
 * memakai tabel `pengaturan` supaya berganti bahasa tidak menyentuh data permainan.
 */
class BahasaStore(context: Context) {
    private val sp = context.applicationContext
        .getSharedPreferences("codefishing_bahasa", Context.MODE_PRIVATE)

    private val _pilihan = MutableStateFlow(Bahasa.dari(sp.getString(KEY_BAHASA, null)))
    val pilihan: StateFlow<Bahasa> = _pilihan.asStateFlow()

    fun pilih(bahasa: Bahasa) {
        _pilihan.value = bahasa
        sp.edit { putString(KEY_BAHASA, bahasa.nilai) }
    }

    private companion object {
        const val KEY_BAHASA = "bahasa"
    }
}

/**
 * Kamus teks antarmuka.
 *
 * Kunci kamus adalah teks Indonesianya sendiri. Bahasa Indonesia memakai kamus kosong, jadi
 * `t("Mulai Main")` mengembalikan "Mulai Main" tanpa biaya apa pun; bahasa Inggris hanya
 * menerjemahkan kunci yang ada di [KamusInggris]. Konsekuensinya, teks yang lupa diterjemahkan
 * tetap tampil dalam bahasa Indonesia, bukan kosong atau nama kunci.
 */
@Immutable
class Teks internal constructor(private val kamus: Map<String, String>) {
    /** Terjemahan [kunci], atau [kunci] itu sendiri bila belum ada padanannya. */
    operator fun get(kunci: String): String = kamus[kunci] ?: kunci

    operator fun invoke(kunci: String): String = kamus[kunci] ?: kunci

    /**
     * Seperti [get], tetapi mengisi penanda `{}` berurutan dengan [nilai].
     *
     * Dipakai untuk kalimat yang mengandung angka, mis. `"Stage {} · {}"`. Penanda `{}` dipilih
     * agar tidak bentrok dengan tanda persen yang sering muncul di teks materi.
     */
    fun format(kunci: String, vararg nilai: Any?): String {
        val pola = kamus[kunci] ?: kunci
        if (nilai.isEmpty()) return pola
        val hasil = StringBuilder(pola.length + 16)
        var terpakai = 0
        var i = 0
        while (i < pola.length) {
            if (pola[i] == '{' && i + 1 < pola.length && pola[i + 1] == '}') {
                hasil.append(nilai.getOrNull(terpakai)?.toString() ?: "{}")
                terpakai++
                i += 2
            } else {
                hasil.append(pola[i])
                i++
            }
        }
        return hasil.toString()
    }

    companion object {
        /** Bahasa Indonesia: kunci dikembalikan apa adanya. */
        val Indonesia = Teks(emptyMap())

        /** Bahasa Inggris: memakai [KamusInggris]. */
        val Inggris = Teks(KamusInggris.peta)
    }
}

/** Teks yang berlaku sekarang, dibagikan ke seluruh layar. */
val LocalTeks = staticCompositionLocalOf { Teks.Indonesia }

/**
 * Pintasan untuk komponen: `Text(t("Mulai Main"))`.
 *
 * Berupa fungsi @Composable agar tiap pemakaian hanya berlangganan ke [LocalTeks], tanpa perlu
 * menambah `val teks = ...` di setiap fungsi.
 */
@androidx.compose.runtime.Composable
fun t(kunci: String): String = LocalTeks.current[kunci]

/** Versi [t] dengan pengisi `{}`. */
@androidx.compose.runtime.Composable
fun t(kunci: String, vararg nilai: Any?): String = LocalTeks.current.format(kunci, *nilai)

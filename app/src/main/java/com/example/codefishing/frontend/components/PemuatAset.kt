package com.example.codefishing.frontend.components

import android.content.Context
import android.os.Process
import android.util.Log
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.peta.PetaLatar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Progres pemuatan aset awal untuk layar pembuka.
 *
 * [selesaiSemua] baru bernilai true setelah seluruh katalog aset, peta, dan (di tema terang) kontur
 * ikan masuk ke cache. Selama itu layar utama belum dibuka sehingga pemain tidak melihat gambar
 * yang muncul terlambat atau sempat kosong.
 */
data class StatusMuat(
    val selesai: Int = 0,
    val total: Int = 0,
    val selesaiSemua: Boolean = false,
    val teks: String = "Menyiapkan perairan...",
) {
    /** 0f..1f untuk bilah progres. */
    val progres: Float
        get() = if (total <= 0) 0f else (selesai.toFloat() / total).coerceIn(0f, 1f)
}

/**
 * Memuat seluruh aset gambar di awal aplikasi, sekali per proses.
 *
 * `painterResource`/`BitmapFactory` mendekode PNG saat pertama kali dipakai. Layar yang berisi
 * banyak gambar (peta, Koleksi, Akuarium, adegan stage) jadi sempat kosong atau "pop-in" bila
 * dekode itu terjadi ketika layar dibuka. Di sini semuanya didekode di thread khusus sebelum
 * [com.example.codefishing.frontend.App] ditampilkan, dengan progres yang bisa ditunggu pemain.
 *
 * Urutan muat disusun dari yang paling jarang terlihat ke yang paling cepat dibutuhkan, karena
 * [CacheGambar] adalah LRU: aset terakhir yang dimuat adalah aset yang bertahan di memori, yaitu
 * ikon antarmuka, thumbnail koleksi, dan aset layar pembuka/Home.
 */
object PemuatAset {
    private val _status = MutableStateFlow(StatusMuat())
    val status: StateFlow<StatusMuat> = _status.asStateFlow()

    private val sudahMulai = AtomicBoolean(false)

    // Prioritas normal (bukan background): saat layar pembuka beranimasi, thread ini harus tetap
    // mendapat CPU agar pemuatan tidak molor puluhan detik di perangkat lambat.
    private val utas = Executors.newSingleThreadExecutor { kerja ->
        Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT)
            kerja.run()
        }, "pemuat-aset").apply { isDaemon = true }
    }

    /** Mulai memuat. Pemanggilan kedua diabaikan (proses yang sama). */
    fun mulai(context: Context, gelap: Boolean) {
        if (!sudahMulai.compareAndSet(false, true)) return
        val app = context.applicationContext
        utas.execute {
            try {
                muatSemua(app, gelap)
            } catch (_: Throwable) {
                // Satu aset rusak tidak boleh menahan pemain selamanya di layar pembuka.
                _status.value = _status.value.copy(selesaiSemua = true, teks = "Siap berlayar!")
            }
        }
    }

    private fun muatSemua(context: Context, gelap: Boolean) {
        val mulai = System.currentTimeMillis()
        val semua = Art.GROUPS
            .flatMap { it.second }
            .distinctBy { it.second }
            .filter { (nama, _) -> !(gelap && nama.endsWith("_terang")) }
            .sortedBy { prioritas(it.first) }

        // Tema terang memakai kontur gelap (GambarKontras) untuk ikan kecil & ikon peta. Membuatnya
        // di layar pembuka menghindari kedipan "gambar polos lalu bergaris" saat grid dibuka.
        val kontur = if (gelap) emptyList() else asetKontur()

        val bobotPeta = 8
        val total = semua.size + 2 * bobotPeta + kontur.size
        var selesai = 0
        var persenTerakhir = -1
        var teksTerakhir = ""

        fun lapor(teks: String, paksa: Boolean = false) {
            val persen = if (total <= 0) 100 else (selesai * 100 / total)
            if (!paksa && persen == persenTerakhir && teks == teksTerakhir) return
            persenTerakhir = persen
            teksTerakhir = teks
            _status.value = StatusMuat(selesai = selesai, total = total, teks = teks)
        }

        lapor("Menyiapkan perairan...", paksa = true)

        // Tiga tahap mengikuti umur cache LRU: yang dimuat lebih akhir yang bertahan.
        //   1. ilustrasi, ikan penuh, lencana, lalu perlengkapan & antarmuka (paling besar);
        //   2. thumbnail koleksi — dimuat setelah antarmuka agar tidak tersingkir saat grid dibuka;
        //   3. kontur dibuat saat thumbnail masih panas, lalu aset layar pertama dimuat terakhir.
        val awal = semua.filter { prioritas(it.first) < 7 }
        val thumbnail = semua.filter { prioritas(it.first) in 7..8 }
        val akhir = semua.filter { prioritas(it.first) == 9 }

        awal.forEach { (nama, id) ->
            runCatching { CacheGambar.ambil(context, id) }
            selesai++
            lapor(teksFase(prioritas(nama)))
        }

        thumbnail.forEach { (nama, id) ->
            runCatching { CacheGambar.ambil(context, id) }
            selesai++
            lapor(teksFase(prioritas(nama)))
        }

        kontur.forEach { id ->
            runCatching { CacheGambar.buatKontur(context, id) }
            selesai++
            lapor("Mempertajam gambar ikan...")
        }

        akhir.forEach { (nama, id) ->
            runCatching { CacheGambar.ambil(context, id) }
            selesai++
            lapor(teksFase(prioritas(nama)))
        }

        // Peta Pasifik lalu Antartika: PetaLatar menyimpan dua peta, jadi keduanya bertahan.
        listOf(false, true).forEach { antartika ->
            lapor("Menyiapkan peta...")
            runCatching { PetaLatar.ambil(context, !gelap, antartika) }
            selesai += bobotPeta
            lapor("Menyiapkan peta...")
        }

        _status.value = StatusMuat(
            selesai = total,
            total = total,
            selesaiSemua = true,
            teks = "Siap berlayar!",
        )
        Log.i(
            "PemuatAset",
            "Selesai: ${semua.size} gambar, ${kontur.size} kontur, 2 peta " +
                "dalam ${System.currentTimeMillis() - mulai} ms (gelap=$gelap)",
        )
    }

    /**
     * 0 = ilustrasi stage (paling besar, dipakai belakangan), 1 = latar langit/kutub yang belum
     * dipakai layar mana pun, 2/3 = ikan ukuran penuh (hanya untuk layar detail), 5 = lencana,
     * 6 = perlengkapan + antarmuka, 7/8 = thumbnail ikan, 9 = aset layar pertama.
     */
    private fun prioritas(nama: String): Int {
        if (nama in ASET_LAYAR_AWAL) return 9
        val ikan = nama.startsWith("ikan_")
        val kecil = nama.endsWith("_kecil")
        val terkunci = nama.startsWith("ikan_terkunci_")
        return when {
            !ikan && nama.startsWith("adegan_") -> 0
            !ikan && (nama.startsWith("latar_langit_") || nama.startsWith("latar_es_")) -> 1
            ikan && !kecil && terkunci -> 3
            ikan && !kecil -> 2
            !ikan && nama.startsWith("lencana_") -> 5
            ikan && kecil && terkunci -> 7
            ikan && kecil -> 8
            else -> 6
        }
    }

    private fun teksFase(prioritas: Int): String = when (prioritas) {
        0 -> "Memuat ilustrasi petualangan..."
        1 -> "Menyiapkan latar laut..."
        2, 3 -> "Menyiapkan koleksi ikan..."
        5 -> "Memuat lencana pencapaian..."
        6 -> "Menyiapkan antarmuka..."
        7, 8 -> "Menyiapkan album ikan..."
        else -> "Menyiapkan halaman utama..."
    }

    /** Ikan thumbnail berwarna dan ikon peta yang tampil lewat GambarKontras di tema terang. */
    private fun asetKontur(): List<Int> {
        val ikan = Art.GROUPS
            .flatMap { it.second }
            .filter { (nama, _) ->
                nama.startsWith("ikan_") && nama.endsWith("_kecil") && !nama.startsWith("ikan_terkunci_")
            }
            .map { it.second }
        return (ikan + listOf(Art.ikon_air_tenang, Art.ikon_air_dingin, Art.ikon_air_badai, Art.gunung_es))
            .distinct()
    }

    /**
     * Aset yang harus tetap tinggal di cache setelah pemuatan: yang langsung terlihat di layar
     * pembuka, pengenalan, Home, Pilih Mode, dan Peta. LRU membuang aset yang dimuat lebih dulu,
     * jadi daftar ini dimuat paling akhir.
     */
    private val ASET_LAYAR_AWAL = setOf(
        // Layar pembuka & pengenalan
        "logo_codefishing",
        "logo_simbol",
        "logo_app_ikon",
        "ombak_pola",
        "gelembung",
        "latar_akuarium",
        "latar_akuarium_terang",
        "ilustrasi_editor_kode",
        "ilustrasi_editor_kode_terang",
        "kaleng_umpan",
        // Home
        "umpan_penuh",
        "umpan_penuh_terang",
        "umpan_kosong",
        "umpan_kosong_terang",
        "bintang_penuh",
        "bintang_kosong",
        "bintang_kosong_terang",
        "ikan_hiu_merah",
        "lencana_plus_satu",
        "kartu_lanjutkan",
        // Pilih Mode & peta
        "latar_bawah_laut",
        "latar_bawah_laut_terang",
        "preview_bermuda",
        "kapal_peta",
        "kompas",
        "kompas_terang",
        "gunung_es",
        "node_aktif",
        "node_boss",
        "node_selesai",
        "node_terkunci",
        "bendera_start",
        "mercusuar",
    )
}

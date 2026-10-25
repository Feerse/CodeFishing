package com.example.codefishing.core.content

/** Model data hasil ekspor dari prototipe HTML (lihat app/src/main/assets/content (berkas JSON)). */

data class Opsi(val teks: String, val keluaran: String)

data class Soal(
    val tanya: String,
    val kode: String,
    val opsi: List<Opsi>,
    val benar: Int,
    val adegan: String,
    val variasi: String,
    val studiKasus: String,
)

data class Stage(
    val topik: String,
    val ikan: String,
    val teks: String,
    val kode: String,
    val keluaran: String,
    val boss: Boolean,
    val soal: List<Soal>,
)

data class Tujuan(val tipe: String, val n: Int?, val spesies: String?)

data class Misi(
    val id: String,
    val nama: String,
    val ukuran: Int,
    val teksTujuan: String,
    val tujuan: Tujuan,
    val par: Int?,
    val petunjuk: String,
    val kodeAwal: String,
    /** Isi petak ukuran x ukuran, baris demi baris. String kosong = petak laut kosong. */
    val petak: List<String>,
)

enum class Rarity(val kunci: String) {
    Common("common"), Rare("rare"), Epic("epic"), Legend("legend"), Mythic("mythic");

    companion object {
        fun dari(kunci: String): Rarity = entries.firstOrNull { it.kunci == kunci } ?: Common
    }
}

data class Ikan(
    val key: String,
    val nama: String,
    val drawable: String,
    val drawableTerkunci: String,
    val rarity: Rarity,
)

data class GameConfig(
    val umpanMax: Int,
    val regenMs: Long,
    val ambangLulusPersen: Int,
)

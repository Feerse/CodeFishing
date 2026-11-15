package com.example.codefishing.pemula

/** Nilai `pemain.mode_terakhir` yang dikenal aplikasi. */
const val MODE_BELAJAR = "belajar"
const val MODE_EXPERT = "expert"

/** Tujuan tombol "Lanjutkan" di Home. */
enum class JenisLanjut {
    /** Mode Expert: kembali ke Sandbox. */
    Sandbox,

    /** Mode Belajar: buka materi (atau boss) pada [TujuanLanjut.stageId]. */
    Stage,

    /** Belum ada stage yang bisa dilanjutkan: minta pemain memilih mode. */
    PilihMode,
}

data class TujuanLanjut(
    val jenis: JenisLanjut,
    val stageId: Int? = null,
    val boss: Boolean = false,
)

/**
 * Menentukan ke mana "Lanjutkan" membawa pemain.
 *
 * Aturannya: posisi terakhir menang; bila posisi kosong (mis. baru pertama kali atau setelah
 * reset) pakai stage yang sedang aktif supaya tombolnya tidak berakhir di Pilih Mode seperti
 * "Mulai Main". Bila keduanya tidak ada, barulah Pilih Mode.
 */
fun tentukanTujuanLanjut(
    modeTerakhir: String?,
    posisiTerakhirId: Int?,
    posisiTerakhirBoss: Boolean,
    stageAktifId: Int?,
    stageAktifBoss: Boolean,
): TujuanLanjut = when {
    modeTerakhir == MODE_EXPERT -> TujuanLanjut(JenisLanjut.Sandbox)
    posisiTerakhirId != null ->
        TujuanLanjut(JenisLanjut.Stage, posisiTerakhirId, posisiTerakhirBoss)
    stageAktifId != null ->
        TujuanLanjut(JenisLanjut.Stage, stageAktifId, stageAktifBoss)
    else -> TujuanLanjut(JenisLanjut.PilihMode)
}

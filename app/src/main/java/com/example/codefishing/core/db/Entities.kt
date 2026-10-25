package com.example.codefishing.core.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Skema fisik ERD CodeFishing (16 tabel).
 * Nilai boolean disimpan sebagai INTEGER 0/1, waktu sebagai epoch milidetik.
 */
@Entity(
    tableName = "pemain",
    foreignKeys = [
        ForeignKey(
            entity = StageEntity::class,
            parentColumns = ["id"],
            childColumns = ["stage_terakhir_id"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true,
        ),
    ],
    indices = [Index("stage_terakhir_id")],
)
data class PemainEntity(
    @PrimaryKey val id: Int = 1,
    val nama: String = "Nakhoda Python",
    val xp: Int = 0,
    val level: Int = 1,
    val umpan_emas: Int = 0,
    val stage_terakhir_id: Int? = null,
    val mode_terakhir: String? = null,
    val dibuat_pada: Long = 0L,
    val terakhir_main: Long = 0L,
)

@Entity(
    tableName = "ikan",
    foreignKeys = [
        ForeignKey(
            entity = StageEntity::class,
            parentColumns = ["id"],
            childColumns = ["asal_stage_id"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true,
        ),
    ],
    indices = [Index("asal_stage_id"), Index(value = ["tier", "urutan"])],
)
data class IkanEntity(
    @PrimaryKey val id: Int,
    val nama: String,
    val nama_ilmiah: String? = null,
    val tier: String,
    val konsep_python: String,
    val deskripsi: String,
    val asal_stage_id: Int? = null,
    val syarat_unlock: String? = null,
    val nilai_xp: Int = 35,
    val bobot_kg: Double = 1.0,
    val gambar: String? = null,
    val urutan: Int,
)

@Entity(
    tableName = "stage",
    foreignKeys = [
        ForeignKey(
            entity = IkanEntity::class,
            parentColumns = ["id"],
            childColumns = ["ikan_target_id"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true,
        ),
    ],
    indices = [Index(value = ["nomor"], unique = true), Index("ikan_target_id"), Index("zona", "nomor")],
)
data class StageEntity(
    @PrimaryKey val id: Int,
    val nomor: Int,
    val nama: String,
    val zona: String,
    val konsep: String,
    val tipe: String,
    val jumlah_soal: Int = 5,
    val skor_lulus: Int = 70,
    val xp_reward: Int = 50,
    val ikan_target_id: Int? = null,
    val urutan: Int,
)

@Entity(
    tableName = "materi",
    foreignKeys = [
        ForeignKey(entity = StageEntity::class, parentColumns = ["id"], childColumns = ["stage_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["stage_id"], unique = true)],
)
data class MateriEntity(
    @PrimaryKey val id: Int,
    val stage_id: Int,
    val judul: String,
    val isi_markdown: String,
    val contoh_kode: String,
    val output_contoh: String? = null,
    val trik_nelayan: String? = null,
    val urutan: Int = 1,
)

@Entity(
    tableName = "soal",
    foreignKeys = [
        ForeignKey(entity = StageEntity::class, parentColumns = ["id"], childColumns = ["stage_asal_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("stage_asal_id", "urutan")],
)
data class SoalEntity(
    @PrimaryKey val id: Int,
    val stage_asal_id: Int,
    val tipe: String,
    val teks: String,
    val kode_template: String? = null,
    val jawaban_benar: String? = null,
    val pembahasan: String,
    val poin: Int = 25,
    val urutan: Int,
)

@Entity(
    tableName = "pilihan",
    foreignKeys = [
        ForeignKey(entity = SoalEntity::class, parentColumns = ["id"], childColumns = ["soal_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["soal_id", "urutan"], unique = true)],
)
data class PilihanEntity(
    @PrimaryKey val id: Int,
    val soal_id: Int,
    val teks: String,
    val benar: Int = 0,
    val urutan: Int,
)

@Entity(
    tableName = "komposisi_kuis",
    foreignKeys = [
        ForeignKey(entity = StageEntity::class, parentColumns = ["id"], childColumns = ["stage_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SoalEntity::class, parentColumns = ["id"], childColumns = ["soal_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["stage_id", "urutan"], unique = true), Index(value = ["stage_id", "soal_id"], unique = true)],
)
data class KomposisiKuisEntity(
    @PrimaryKey val id: Int,
    val stage_id: Int,
    val soal_id: Int,
    val urutan: Int,
)

@Entity(
    tableName = "progres_stage",
    foreignKeys = [
        ForeignKey(entity = PemainEntity::class, parentColumns = ["id"], childColumns = ["pemain_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = StageEntity::class, parentColumns = ["id"], childColumns = ["stage_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["pemain_id", "stage_id"], unique = true), Index("pemain_id", "status")],
)
data class ProgresStageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pemain_id: Int = 1,
    val stage_id: Int,
    val status: String = "terkunci",
    val skor_terbaik: Int = 0,
    val combo_tertinggi: Int = 0,
    val percobaan: Int = 0,
    val lulus_pada: Long? = null,
)

@Entity(
    tableName = "umpan",
    foreignKeys = [
        ForeignKey(entity = PemainEntity::class, parentColumns = ["id"], childColumns = ["pemain_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["pemain_id"], unique = true)],
)
data class UmpanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pemain_id: Int = 1,
    val sisa: Int = 5,
    val maks: Int = 5,
    val interval_menit: Int = 15,
    val regen_at: Long? = null,
)

@Entity(
    tableName = "koleksi_ikan",
    foreignKeys = [
        ForeignKey(entity = PemainEntity::class, parentColumns = ["id"], childColumns = ["pemain_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = IkanEntity::class, parentColumns = ["id"], childColumns = ["ikan_id"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index(value = ["pemain_id", "ikan_id"], unique = true), Index("pemain_id")],
)
data class KoleksiIkanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pemain_id: Int = 1,
    val ikan_id: Int,
    val jumlah: Int = 1,
    val bobot_terbesar: Double = 0.0,
    val level: Int = 1,
    val tier_didapat: String,
    val xp: Int = 0,
    val kenyang: Int = 100,
    val diperbarui_pada: Long,
    val pertama_didapat: Long,
    val terakhir_makan: Long? = null,
)

@Entity(
    tableName = "misi",
    indices = [Index(value = ["nomor"], unique = true)],
)
data class MisiEntity(
    @PrimaryKey val id: Int,
    val nomor: Int,
    val judul: String,
    val deskripsi: String,
    val tipe: String,
    val target_ikan: Int = 0,
    val target_baris: Int? = null,
    val par_tick: Int,
    val xp_reward: Int = 120,
    val urutan: Int,
)

@Entity(
    tableName = "hasil_program",
    foreignKeys = [
        ForeignKey(entity = PemainEntity::class, parentColumns = ["id"], childColumns = ["pemain_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = MisiEntity::class, parentColumns = ["id"], childColumns = ["misi_id"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("pemain_id", "dibuat_pada"), Index("misi_id", "bintang"), Index("pemain_id")],
)
data class HasilProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pemain_id: Int = 1,
    val misi_id: Int? = null,
    val kode: String,
    val output: String = "",
    val error: String? = null,
    val ticks: Int = 0,
    val waktu_jalan_ms: Long = 0,
    val bintang: Int = 0,
    val xp: Int = 0,
    val status: String,
    val dibuat_pada: Long,
)

@Entity(
    tableName = "hasil_tangkapan",
    foreignKeys = [
        ForeignKey(entity = HasilProgramEntity::class, parentColumns = ["id"], childColumns = ["hasil_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = IkanEntity::class, parentColumns = ["id"], childColumns = ["ikan_id"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index("hasil_id"), Index("ikan_id")],
)
data class HasilTangkapanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hasil_id: Long,
    val ikan_id: Int,
    val jumlah: Int = 1,
    val bobot_kg: Double = 0.0,
)

@Entity(
    tableName = "pengaturan",
    foreignKeys = [
        ForeignKey(entity = PemainEntity::class, parentColumns = ["id"], childColumns = ["pemain_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["pemain_id"], unique = true)],
)
data class PengaturanEntity(
    @PrimaryKey val id: Int = 1,
    val pemain_id: Int = 1,
    val efek_suara: Int = 1,
    val musik: Int = 1,
    val haptik: Int = 1,
    val pewarnaan_sintaks: Int = 1,
    val bahasa: String = "id",
)

@Entity(
    tableName = "riwayat_jawaban",
    foreignKeys = [
        ForeignKey(entity = PemainEntity::class, parentColumns = ["id"], childColumns = ["pemain_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SoalEntity::class, parentColumns = ["id"], childColumns = ["soal_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PilihanEntity::class, parentColumns = ["id"], childColumns = ["pilihan_id"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("pemain_id", "waktu"), Index("soal_id"), Index("pilihan_id")],
)
data class RiwayatJawabanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pemain_id: Int = 1,
    val soal_id: Int,
    val pilihan_id: Int? = null,
    val jawaban_teks: String? = null,
    val benar: Int,
    val combo_setelah: Int = 0,
    val waktu: Long,
)

@Entity(
    tableName = "tantangan_harian",
    foreignKeys = [
        ForeignKey(entity = PemainEntity::class, parentColumns = ["id"], childColumns = ["pemain_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = IkanEntity::class, parentColumns = ["id"], childColumns = ["target_ikan_id"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index(value = ["pemain_id", "tanggal"], unique = true), Index("target_ikan_id")],
)
data class TantanganHarianEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pemain_id: Int = 1,
    val tanggal: String,
    val target_ikan_id: Int,
    val reward_umpan_emas: Int = 1,
    val status: String = "tersedia",
    val expires_at: Long,
)

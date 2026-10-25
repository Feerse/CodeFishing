package com.example.codefishing.core.db

import androidx.room.DatabaseView

/** View ERD 5.6: seluruh katalog + status kepemilikan pemain 1. */
@DatabaseView(
    viewName = "v_koleksi_lengkap",
    value = """
        SELECT
          i.id AS ikan_id,
          i.nama AS nama,
          i.tier AS tier_katalog,
          COALESCE(k.tier_didapat, i.tier) AS tier,
          i.konsep_python AS konsep_python,
          i.gambar AS gambar,
          i.syarat_unlock AS syarat_unlock,
          COALESCE(k.jumlah, 0) AS jumlah,
          COALESCE(k.bobot_terbesar, 0) AS bobot_terbesar,
          COALESCE(k.level, 1) AS level,
          COALESCE(k.kenyang, 100) AS kenyang,
          CASE WHEN k.id IS NULL THEN 0 ELSE 1 END AS dimiliki,
          i.deskripsi AS deskripsi,
          i.nilai_xp AS nilai_xp,
          i.bobot_kg AS bobot_kg,
          i.urutan AS urutan
        FROM ikan i
        LEFT JOIN koleksi_ikan k ON k.ikan_id = i.id AND k.pemain_id = 1
    """,
)
data class KoleksiLengkapView(
    val ikan_id: Int,
    val nama: String,
    val tier_katalog: String,
    val tier: String,
    val konsep_python: String,
    val gambar: String?,
    val syarat_unlock: String?,
    val jumlah: Int,
    val bobot_terbesar: Double,
    val level: Int,
    val kenyang: Int,
    val dimiliki: Int,
    val deskripsi: String,
    val nilai_xp: Int,
    val bobot_kg: Double,
    val urutan: Int,
)

/** View ERD 5.6: ringkasan beranda. */
@DatabaseView(
    viewName = "v_statistik_pemain",
    value = """
        SELECT
          p.id AS id,
          p.nama AS nama,
          p.level AS level,
          p.xp AS xp,
          p.level * 100 AS xp_target,
          (SELECT COALESCE(SUM(skor_terbaik), 0) FROM progres_stage WHERE pemain_id = p.id) AS total_skor,
          (SELECT COUNT(*) FROM progres_stage WHERE pemain_id = p.id AND status = 'selesai') AS stage_selesai,
          (SELECT COUNT(*) FROM koleksi_ikan WHERE pemain_id = p.id) AS ikan_terkumpul,
          (SELECT COUNT(*) FROM ikan) AS total_ikan,
          (SELECT COALESCE(SUM(mx), 0) FROM (
              SELECT MAX(bintang) AS mx FROM hasil_program WHERE pemain_id = p.id GROUP BY misi_id
          )) AS total_bintang
        FROM pemain p
    """,
)
data class StatistikPemainView(
    val id: Int,
    val nama: String,
    val level: Int,
    val xp: Int,
    val xp_target: Int,
    val total_skor: Int,
    val stage_selesai: Int,
    val ikan_terkumpul: Int,
    val total_ikan: Int,
    val total_bintang: Int,
)

/** Baris hasil query status 10 stage (DFD 7.2). */
data class StageStatusRow(
    val id: Int,
    val nomor: Int,
    val nama: String,
    val zona: String,
    val konsep: String,
    val tipe: String,
    val jumlah_soal: Int,
    val skor_lulus: Int,
    val xp_reward: Int,
    val ikan_target_id: Int?,
    val urutan: Int,
    val status: String,
    val skor_terbaik: Int,
    val terbuka: Int,
)

data class MisiBintangRow(
    val id: Int,
    val judul: String,
    val par_tick: Int,
    val bintang: Int,
    val selesai: Int,
)

data class HasilRingkasRow(
    val id: Long,
    val misi_id: Int?,
    val judul_misi: String?,
    val ticks: Int,
    val bintang: Int,
    val xp: Int,
    val status: String,
    val dibuat_pada: Long,
)

data class AkurasiRow(
    val total: Int,
    val benar: Int,
)

package com.example.codefishing.core.db

import android.content.Context
import android.content.SharedPreferences
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject

/**
 * Memuat konten kanonik DFD dari assets/seed ke Room dalam satu transaksi.
 * Seed hanya dijalankan saat database kosong atau versi seed berubah.
 */
class SeedLoader(
    private val context: Context,
    private val db: CodeFishingDatabase,
    private val prefs: SharedPreferences,
) {
    suspend fun seedIfNeeded() {
        val sudahAda = db.contentDao().jumlahStage() > 0
        val versi = prefs.getInt(KEY_SEED_VERSION, 0)
        if (sudahAda && versi >= SEED_VERSION) return
        if (sudahAda && versi < SEED_VERSION) {
            // Konten diperbarui dengan upsert; progres pemain tidak dihapus.
        }
        db.withTransaction {
            val stages = bacaArray("stage.json").map { o ->
                StageEntity(
                    id = o.getInt("id"),
                    nomor = o.getInt("nomor"),
                    nama = o.getString("nama"),
                    zona = o.getString("zona"),
                    konsep = o.getString("konsep"),
                    tipe = o.getString("tipe"),
                    jumlah_soal = o.optInt("jumlah_soal", 5),
                    skor_lulus = o.optInt("skor_lulus", 70),
                    xp_reward = o.optInt("xp_reward", 50),
                    ikan_target_id = null,
                    urutan = o.getInt("urutan"),
                )
            }
            db.contentDao().insertStages(stages)

            val ikan = bacaArray("ikan.json").map { o ->
                IkanEntity(
                    id = o.getInt("id"),
                    nama = o.getString("nama"),
                    nama_ilmiah = o.optStringOrNull("nama_ilmiah"),
                    tier = o.getString("tier"),
                    konsep_python = o.getString("konsep_python"),
                    deskripsi = o.getString("deskripsi"),
                    asal_stage_id = o.optIntOrNull("asal_stage_id"),
                    syarat_unlock = o.optStringOrNull("syarat_unlock"),
                    nilai_xp = o.optInt("nilai_xp", 35),
                    bobot_kg = o.optDouble("bobot_kg", 1.0),
                    gambar = o.optStringOrNull("gambar"),
                    urutan = o.getInt("urutan"),
                )
            }
            db.contentDao().insertIkan(ikan)
            bacaArray("stage.json").forEach { o ->
                db.contentDao().setTarget(o.getInt("id"), o.optIntOrNull("ikan_target_id"))
            }

            db.contentDao().insertMateri(bacaArray("materi.json").map { o ->
                MateriEntity(
                    id = o.getInt("id"),
                    stage_id = o.getInt("stage_id"),
                    judul = o.getString("judul"),
                    isi_markdown = o.getString("isi_markdown"),
                    contoh_kode = o.getString("contoh_kode"),
                    output_contoh = o.optStringOrNull("output_contoh"),
                    trik_nelayan = o.optStringOrNull("trik_nelayan"),
                    urutan = o.optInt("urutan", 1),
                )
            })

            val soalRows = bacaArray("soal.json")
            db.contentDao().insertSoal(soalRows.map { o ->
                SoalEntity(
                    id = o.getInt("id"),
                    stage_asal_id = o.getInt("stage_asal_id"),
                    tipe = o.optString("tipe", "token"),
                    teks = o.getString("teks"),
                    kode_template = o.optStringOrNull("kode_template"),
                    jawaban_benar = o.optStringOrNull("jawaban_benar"),
                    pembahasan = o.getString("pembahasan"),
                    poin = o.optInt("poin", 25),
                    urutan = o.getInt("urutan"),
                )
            })
            db.contentDao().insertPilihan(soalRows.flatMap { o ->
                val arr = o.getJSONArray("pilihan")
                (0 until arr.length()).map { i ->
                    val p = arr.getJSONObject(i)
                    PilihanEntity(
                        id = p.getInt("id"),
                        soal_id = p.getInt("soal_id"),
                        teks = p.getString("teks"),
                        benar = p.optInt("benar", 0),
                        urutan = p.optInt("urutan", i + 1),
                    )
                }
            })
            db.contentDao().insertKomposisi(bacaArray("komposisi_kuis.json").map { o ->
                KomposisiKuisEntity(
                    id = o.getInt("id"),
                    stage_id = o.getInt("stage_id"),
                    soal_id = o.getInt("soal_id"),
                    urutan = o.getInt("urutan"),
                )
            })
            db.contentDao().insertMisi(bacaArray("misi.json").map { o ->
                MisiEntity(
                    id = o.getInt("id"),
                    nomor = o.getInt("nomor"),
                    judul = o.getString("judul"),
                    deskripsi = o.getString("deskripsi"),
                    tipe = o.getString("tipe"),
                    target_ikan = o.optInt("target_ikan", 0),
                    target_baris = o.optIntOrNull("target_baris"),
                    par_tick = o.getInt("par_tick"),
                    xp_reward = o.optInt("xp_reward", 120),
                    urutan = o.getInt("urutan"),
                )
            })

            val now = System.currentTimeMillis()
            if (db.playerDao().get() == null) {
                db.playerDao().upsert(
                    PemainEntity(
                        id = 1,
                        nama = "Nakhoda Python",
                        xp = 0,
                        level = 1,
                        umpan_emas = 0,
                        stage_terakhir_id = null,
                        mode_terakhir = null,
                        dibuat_pada = now,
                        terakhir_main = now,
                    ),
                )
            }
            if (db.baitDao().get() == null) {
                db.baitDao().simpan(UmpanEntity(pemain_id = 1, sisa = 5, maks = 5, interval_menit = 15))
            }
            if (db.settingsDao().get() == null) {
                db.settingsDao().simpan(PengaturanEntity(pemain_id = 1))
            }
            stages.forEachIndexed { index, s ->
                if (db.stageDao().progres(s.id) == null) {
                    db.stageDao().simpanProgres(
                        ProgresStageEntity(
                            pemain_id = 1,
                            stage_id = s.id,
                            status = if (index == 0) "aktif" else "terkunci",
                        ),
                    )
                }
            }
        }
        prefs.edit().putInt(KEY_SEED_VERSION, SEED_VERSION).apply()
    }

    private fun bacaArray(nama: String): List<JSONObject> {
        val text = context.assets.open("seed/$nama").bufferedReader().use { it.readText() }
        val arr = JSONArray(text)
        return (0 until arr.length()).map { arr.getJSONObject(it) }
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key) || !has(key)) null else optString(key).ifBlank { null }

    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (isNull(key) || !has(key)) null else optInt(key)

    companion object {
        const val SEED_VERSION = 3
        private const val KEY_SEED_VERSION = "seed_version"
    }
}

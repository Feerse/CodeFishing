package com.example.codefishing.core.data

import org.json.JSONArray
import org.json.JSONObject

/** Snapshot sesi kuis agar pemain bisa melanjutkan tepat di soal yang terakhir dimainkan. */
data class SesiKuis(
    val stageId: Int,
    val soalIds: List<Int>,
    val indeks: Int,
    val benar: Int,
    val salah: Int,
    val combo: Int,
    val comboMaks: Int,
    val xp: Int,
    /** Indeks opsi yang sudah dikunci, -1 bila soal belum dijawab. */
    val terpilih: Int,
    val jawabanBenar: Boolean,
)

/** Serialisasi sesi kuis ke SharedPreferences tanpa menambah tabel/versi Room. */
fun SesiKuis.keJson(): String = JSONObject().apply {
    put("stage_id", stageId)
    put("soal_ids", JSONArray(soalIds))
    put("indeks", indeks)
    put("benar", benar)
    put("salah", salah)
    put("combo", combo)
    put("combo_maks", comboMaks)
    put("xp", xp)
    put("terpilih", terpilih)
    put("jawaban_benar", jawabanBenar)
}.toString()

/** Membaca sesi kuis; null bila belum ada atau data tersimpan rusak. */
fun sesiKuisDariJson(teks: String): SesiKuis? = runCatching {
    val json = JSONObject(teks)
    val ids = json.getJSONArray("soal_ids")
    SesiKuis(
        stageId = json.getInt("stage_id"),
        soalIds = List(ids.length()) { i -> ids.getInt(i) },
        indeks = json.getInt("indeks"),
        benar = json.getInt("benar"),
        salah = json.getInt("salah"),
        combo = json.getInt("combo"),
        comboMaks = json.getInt("combo_maks"),
        xp = json.getInt("xp"),
        terpilih = json.getInt("terpilih"),
        jawabanBenar = json.getBoolean("jawaban_benar"),
    )
}.getOrNull()
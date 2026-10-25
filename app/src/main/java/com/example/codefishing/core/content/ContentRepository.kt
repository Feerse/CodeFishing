package com.example.codefishing.core.content

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Membaca konten game dari assets/content (berkas JSON) (offline). Dimuat sekali lalu disimpan. */
class ContentRepository(context: Context) {
    private val appContext = context.applicationContext

    val stages: List<Stage> by lazy { parseStages(baca("content/stages.json")) }
    val misi: List<Misi> by lazy { parseMisi(baca("content/misi.json")) }
    val ikan: List<Ikan> by lazy { parseIkan(baca("content/ikan_config.json")) }
    val config: GameConfig by lazy { parseConfig(baca("content/ikan_config.json")) }

    private fun baca(path: String): String =
        appContext.assets.open(path).bufferedReader().use { it.readText() }

    companion object {
        fun parseStages(json: String): List<Stage> =
            JSONObject(json).getJSONArray("stages").map { s ->
                Stage(
                    topik = s.getString("topic"),
                    ikan = s.getString("fish"),
                    teks = s.getString("text"),
                    kode = s.getString("code"),
                    keluaran = s.getString("out"),
                    boss = s.optBoolean("boss", false),
                    soal = s.getJSONArray("qs").map { q ->
                        Soal(
                            tanya = q.getString("q"),
                            kode = q.getString("code"),
                            opsi = q.getJSONArray("o").map { Opsi(it.getString("t"), it.getString("out")) },
                            benar = q.getInt("ok"),
                            adegan = q.optString("sc"),
                            variasi = q.optString("v"),
                            studiKasus = q.optString("vn"),
                        )
                    },
                )
            }

        fun parseMisi(json: String): List<Misi> =
            JSONObject(json).getJSONArray("misi").map { m ->
                val g = m.getJSONObject("goal")
                Misi(
                    id = m.getString("id"),
                    nama = m.getString("n"),
                    ukuran = m.getInt("N"),
                    teksTujuan = m.getString("gt"),
                    tujuan = Tujuan(
                        tipe = g.getString("t"),
                        n = if (g.has("n")) g.getInt("n") else null,
                        spesies = if (g.has("sp")) g.getString("sp") else null,
                    ),
                    par = if (m.isNull("par")) null else m.getInt("par"),
                    petunjuk = m.getString("hint"),
                    kodeAwal = m.getString("code"),
                    petak = m.getJSONArray("cells").let { a -> List(a.length()) { a.getString(it) } },
                )
            }

        fun parseIkan(json: String): List<Ikan> =
            JSONObject(json).getJSONArray("ikan").map { f ->
                Ikan(
                    key = f.getString("key"),
                    nama = f.getString("nama"),
                    drawable = f.getString("drawable"),
                    drawableTerkunci = f.getString("terkunci"),
                    rarity = Rarity.dari(f.getString("rarity")),
                )
            }

        fun parseConfig(json: String): GameConfig {
            val c = JSONObject(json).getJSONObject("config")
            return GameConfig(
                umpanMax = c.getInt("umpanMax"),
                regenMs = c.getLong("regenMs"),
                ambangLulusPersen = c.getInt("ambangLulusPersen"),
            )
        }

        private fun <T> JSONArray.map(f: (JSONObject) -> T): List<T> =
            List(length()) { f(getJSONObject(it)) }
    }
}

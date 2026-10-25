package com.example.codefishing.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SeedValidationTest {
    private val dir = listOf(
        File("src/main/assets/seed"),
        File("app/src/main/assets/seed"),
        File("../app/src/main/assets/seed"),
    ).first { it.exists() }

    private fun baca(nama: String): JSONArray = JSONArray(File(dir, nama).readText())

    private fun List<JSONObject>.ids(): Set<Int> = map { it.getInt("id") }.toSet()

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    @Test
    fun jumlahKontenSesuaiDfd() {
        assertEquals(10, baca("stage.json").length())
        assertEquals(9, baca("materi.json").length())
        assertEquals(45, baca("soal.json").length())
        assertEquals(48, baca("ikan.json").length())
        assertEquals(7, baca("misi.json").length())
    }

    @Test
    fun setiapStagePunyaLimaSoalUnik() {
        val soal = baca("soal.json").objects()
        assertTrue(soal.ids().size == soal.size)
        val komposisi = baca("komposisi_kuis.json").objects()
        val perStage = komposisi.groupBy { it.getInt("stage_id") }
        (1..10).forEach { stage ->
            assertEquals("Stage $stage harus 5 soal", 5, perStage[stage]?.size)
            assertEquals(5, perStage[stage]!!.map { it.getInt("soal_id") }.toSet().size)
            assertTrue(perStage[stage]!!.all { it.getInt("soal_id") in soal.ids() })
        }
    }

    @Test
    fun setiapSoalPunyaEmpatPilihanDanSatuJawabanBenar() {
        baca("soal.json").objects().forEach { s ->
            val pilihan = s.getJSONArray("pilihan").objects()
            assertEquals("Soal ${s.getInt("id")}", 4, pilihan.size)
            assertEquals("Soal ${s.getInt("id")} benar", 1, pilihan.count { it.getInt("benar") == 1 })
            assertTrue(s.getString("teks").isNotBlank())
            assertTrue(s.getString("pembahasan").isNotBlank())
        }
    }

    @Test
    fun katalogIkanPunyaLimaBelasIlustrasiDanTigaPuluhTigaPlaceholder() {
        val ikan = baca("ikan.json").objects()
        assertEquals(48, ikan.ids().size)
        assertEquals(48, ikan.map { it.getString("nama") }.toSet().size)
        val ilustrasi = ikan.count { !it.getString("gambar").contains("tanda_tanya") }
        val placeholder = ikan.count { it.getString("gambar") == "tanda_tanya_ikan" }
        assertEquals(15, ilustrasi)
        assertEquals(33, placeholder)
        assertTrue(ikan.filter { it.getString("gambar") == "tanda_tanya_ikan" }
            .all { it.getString("syarat_unlock").contains("Segera hadir") })
    }

    @Test
    fun targetStageMenunjukIkanYangAda() {
        val ikanIds = baca("ikan.json").objects().ids()
        baca("stage.json").objects().forEach { s ->
            val target = s.optInt("ikan_target_id", -1)
            assertTrue("Stage ${s.getInt("id")} target $target", target in ikanIds)
        }
    }

    @Test
    fun misiMempunyaiGridDanParTickPositif() {
        baca("misi.json").objects().forEach { m ->
            assertEquals(
                "Grid ${m.getString("kode")}",
                m.getInt("grid") * m.getInt("grid"),
                m.getJSONArray("cells").length(),
            )
            assertTrue(m.getInt("par_tick") > 0)
            assertTrue(m.getString("kode_awal").isNotBlank())
        }
    }
}

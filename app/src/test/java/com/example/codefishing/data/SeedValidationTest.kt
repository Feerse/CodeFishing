package com.example.codefishing.data

import com.example.codefishing.core.art.Art
import com.example.codefishing.pemula.TangkapanSampingan
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals(20, baca("stage.json").length())
        assertEquals(18, baca("materi.json").length())
        assertEquals(110, baca("soal.json").length())
        assertEquals(58, baca("ikan.json").length())
        assertEquals(7, baca("misi.json").length())
    }

    @Test
    fun setiapStagePunyaSoalSesuaiTipe() {
        val soal = baca("soal.json").objects()
        assertTrue(soal.ids().size == soal.size)
        val komposisi = baca("komposisi_kuis.json").objects()
        val perStage = komposisi.groupBy { it.getInt("stage_id") }
        // Stage 1-9: 5 soal (Pasifik)
        (1..9).forEach { stage ->
            assertEquals("Stage $stage harus 5 soal", 5, perStage[stage]?.size)
            assertEquals(5, perStage[stage]!!.map { it.getInt("soal_id") }.toSet().size)
            assertTrue(perStage[stage]!!.all { it.getInt("soal_id") in soal.ids() })
        }
        // Boss stage 10: 10 soal
        assertEquals("Stage 10 (boss) harus 10 soal", 10, perStage[10]?.size)
        assertEquals(10, perStage[10]!!.map { it.getInt("soal_id") }.toSet().size)
        assertTrue(perStage[10]!!.all { it.getInt("soal_id") in soal.ids() })
        // Stage 11-19: 5 soal (Antartika)
        (11..19).forEach { stage ->
            assertEquals("Stage $stage harus 5 soal", 5, perStage[stage]?.size)
            assertEquals(5, perStage[stage]!!.map { it.getInt("soal_id") }.toSet().size)
            assertTrue(perStage[stage]!!.all { it.getInt("soal_id") in soal.ids() })
        }
        // Boss stage 20: 10 soal
        assertEquals("Stage 20 (boss) harus 10 soal", 10, perStage[20]?.size)
        assertEquals(10, perStage[20]!!.map { it.getInt("soal_id") }.toSet().size)
        assertTrue(perStage[20]!!.all { it.getInt("soal_id") in soal.ids() })
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
    fun katalogIkanPunyaIlustrasiLengkap() {
        val ikan = baca("ikan.json").objects()
        assertEquals(58, ikan.ids().size)
        assertEquals(58, ikan.map { it.getString("nama") }.toSet().size)
        assertEquals(0, ikan.count { it.getString("gambar") == "tanda_tanya_ikan" })
        assertEquals(0, ikan.count { it.getString("syarat_unlock").contains("Segera hadir") })

        // Seluruh 58 ikan sudah punya ilustrasi berwarna dan siluet terkunci.
        val bergambar = ikan.filter { Art.byName(it.getString("gambar")) != null }
        assertEquals(58, bergambar.size)
        bergambar.forEach { i ->
            val kunci = "ikan_terkunci_" + i.getString("gambar").removePrefix("ikan_")
            assertNotNull("Siluet terkunci hilang: $kunci", Art.byName(kunci))
        }

        val belumBergambar = ikan.filter { Art.byName(it.getString("gambar")) == null }
            .map { it.getString("nama") }
        assertEquals(emptyList<String>(), belumBergambar)
    }

    @Test
    fun ikanSampinganCocokDenganWilayahnya() {
        val semua = baca("ikan.json").objects()
        val sampingan = semua.filter {
            it.isNull("asal_stage_id") && it.getString("syarat_unlock") != TangkapanSampingan.SYARAT_KHUSUS
        }
        assertEquals(33, sampingan.size)
        val pasifik = sampingan.filter { it.getString("tier") in setOf("biasa", "langka") }
        val lautDalam = sampingan.filter { it.getString("tier") in setOf("epik", "legenda", "mitos") }
        assertEquals(21, pasifik.size)
        assertEquals(12, lautDalam.size)
        assertTrue(pasifik.all { it.getString("syarat_unlock").contains("Pasifik") })
        assertTrue(lautDalam.all { it.getString("syarat_unlock").contains("laut dalam") })
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

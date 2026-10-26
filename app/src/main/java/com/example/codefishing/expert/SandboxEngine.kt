package com.example.codefishing.expert

import android.content.Context
import com.example.codefishing.expert.py.Interpreter
import com.example.codefishing.expert.py.Lexer
import com.example.codefishing.expert.py.Parser
import com.example.codefishing.expert.py.PyError
import org.json.JSONArray

data class MisiPeta(
    val kode: String,
    val nomor: Int,
    val judul: String,
    val deskripsi: String,
    val grid: Int,
    val cells: List<String>,
    val kodeAwal: String,
    val hint: String,
    val goalTipe: String,
    val goalN: Int,
    val goalSpesies: String?,
    val par: Int?,
    val xp: Int,
    val dbId: Int,
)

object SandboxContent {
    @Volatile
    private var cache: List<MisiPeta>? = null

    fun misi(context: Context): List<MisiPeta> {
        cache?.let { return it }
        val teks = context.assets.open("seed/misi.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(teks)
        val hasil = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val goal = o.getJSONObject("goal")
            val cells = o.getJSONArray("cells")
            MisiPeta(
                kode = o.getString("kode"),
                nomor = o.getInt("nomor"),
                judul = o.getString("judul"),
                deskripsi = o.getString("deskripsi"),
                grid = o.getInt("grid"),
                cells = (0 until cells.length()).map { cells.getString(it) },
                kodeAwal = o.getString("kode_awal"),
                hint = o.getString("hint"),
                goalTipe = goal.getString("t"),
                goalN = goal.optInt("n", 0),
                goalSpesies = goal.optString("sp", "").ifBlank { null },
                par = Scoring.parEfektif(o.optInt("par_tick", 0)),
                xp = o.optInt("xp_reward", 0),
                dbId = o.getInt("id"),
            )
        }
        cache = hasil
        return hasil
    }

    fun cari(context: Context, kode: String): MisiPeta? = misi(context).firstOrNull { it.kode == kode }
}

data class EvaluasiMisi(
    val sukses: Boolean,
    val bintang: Int,
    val alasan: String,
)

class SandboxEngine {
    fun jalankan(kode: String, misi: MisiPeta): SandboxRun {
        val dunia = Dunia(misi.grid, misi.cells)
        val hasil = jalankan(kode, dunia)
        val evaluasi = evaluasi(misi, hasil, dunia)
        return SandboxRun(hasil, evaluasi, dunia)
    }

    fun jalankan(kode: String, dunia: Dunia): HasilJalan {
        return try {
            val token = Lexer(kode).tokenisasi()
            val program = Parser(token).parse()
            val hasil = Interpreter(dunia).jalankan(program)
            HasilJalan(
                berhasil = hasil.error == null,
                ticks = dunia.ticks,
                tertangkap = dunia.tertangkap,
                keluaran = hasil.output,
                pesanError = hasil.error?.let { "${it.jenis}: ${it.message}" },
                trace = hasil.trace,
                tangkapan = dunia.tangkapan.toList(),
                sampah = dunia.sampah,
                defDigunakan = hasil.defDigunakan,
                langkah = hasil.langkah,
                errorBaris = hasil.error?.line ?: 0,
            )
        } catch (e: PyError) {
            HasilJalan(
                berhasil = false,
                ticks = dunia.ticks,
                tertangkap = dunia.tertangkap,
                keluaran = emptyList(),
                pesanError = "${e.jenis}: ${e.message}",
                errorBaris = e.line,
            )
        } catch (e: Exception) {
            HasilJalan(
                berhasil = false,
                ticks = dunia.ticks,
                tertangkap = dunia.tertangkap,
                keluaran = emptyList(),
                pesanError = "Kesalahan: ${e.message ?: "tidak dikenal"}",
            )
        }
    }

    private fun evaluasi(misi: MisiPeta, hasil: HasilJalan, dunia: Dunia): EvaluasiMisi {
        if (!hasil.berhasil) {
            return EvaluasiMisi(false, 0, hasil.pesanError ?: "Kode belum berhasil dijalankan.")
        }
        if (misi.kode == "m4" && hasil.sampah > 0) {
            return EvaluasiMisi(false, 0, "Kail mengangkat sampah. Hindari petak sampah.")
        }
        if (misi.kode == "m5") {
            val target = misi.goalSpesies ?: "gurame"
            if (hasil.sampah > 0) {
                return EvaluasiMisi(false, 0, "Kail mengangkat sampah. Hindari petak sampah.")
            }
            val salah = hasil.tangkapan.count { it != target }
            if (salah > 0) return EvaluasiMisi(false, 0, "Ada ikan lain yang ikut tertangkap. Fokus pada $target.")
            if (dunia.sisaSpesies(target) > 0) {
                return EvaluasiMisi(false, 0, "Masih ada $target yang belum tertangkap.")
            }
        }
        val sukses = when (misi.goalTipe) {
            "catch" -> hasil.tangkapan.size >= misi.goalN
            "all" -> dunia.sisaIkan == 0
            "only" -> {
                val target = misi.goalSpesies ?: ""
                hasil.tangkapan.isNotEmpty() && hasil.tangkapan.all { it == target } &&
                    dunia.sisaSpesies(target) == 0
            }
            "def" -> dunia.sisaIkan == 0 && hasil.defDigunakan
            "free" -> hasil.tangkapan.isNotEmpty()
            else -> false
        }
        val alasan = when {
            sukses -> "Misi selesai."
            misi.goalTipe == "def" && dunia.sisaIkan == 0 && !hasil.defDigunakan ->
                "Semua ikan tertangkap, tetapi program belum memakai def."
            misi.goalTipe == "catch" -> "Baru ${hasil.tangkapan.size} dari ${misi.goalN} ikan."
            misi.goalTipe == "all" -> "Masih ada ${dunia.sisaIkan} ikan di petak."
            else -> "Target misi belum terpenuhi."
        }
        return EvaluasiMisi(sukses, if (sukses) Scoring.bintang(hasil.ticks, misi.par) else 0, alasan)
    }
}

data class SandboxRun(
    val hasil: HasilJalan,
    val evaluasi: EvaluasiMisi,
    val dunia: Dunia,
)

package com.example.codefishing.expert.py

import com.example.codefishing.expert.Arah
import com.example.codefishing.expert.Dunia
import com.example.codefishing.expert.HasilLempar

data class Langkah(
    val tick: Int,
    val x: Int,
    val y: Int,
    val aksi: String,
    val baris: Int,
    val pesan: String? = null,
)

data class HasilEksekusi(
    val output: List<String>,
    val error: PyError?,
    val trace: List<Langkah>,
    val defDigunakan: Boolean,
    val langkah: Int,
)

private class ReturnSignal(val nilai: Any?) : RuntimeException()
private class BreakSignal : RuntimeException()
private class ContinueSignal : RuntimeException()

private class Lingkungan(val induk: Lingkungan? = null) {
    private val isi = mutableMapOf<String, Any?>()
    fun ambil(nama: String): Any? {
        if (isi.containsKey(nama)) return isi[nama]
        return induk?.ambil(nama) ?: throw NoSuchElementException(nama)
    }
    fun punya(nama: String): Boolean = isi.containsKey(nama) || (induk?.punya(nama) ?: false)
    fun set(nama: String, nilai: Any?) { isi[nama] = nilai }
}

private data class FungsiPy(val nama: String, val parameter: List<String>, val badan: List<Stmt>, val lingkungan: Lingkungan)

/** Interpreter subset Python yang deterministik dan berjalan offline. */
class Interpreter(private val dunia: Dunia) {
    private val global = Lingkungan()
    private val output = mutableListOf<String>()
    private val trace = mutableListOf<Langkah>()
    private var defDigunakan = false
    private var langkah = 0
    private var kedalaman = 0

    init {
        global.set("Timur", "Timur")
        global.set("Utara", "Utara")
        global.set("Selatan", "Selatan")
        global.set("Barat", "Barat")
    }

    fun jalankan(program: List<Stmt>): HasilEksekusi {
        var error: PyError? = null
        try {
            for (s in program) eksekusi(s, global)
        } catch (e: PyError) {
            error = e
        } catch (e: ReturnSignal) {
            error = PyError("SyntaxError", "return hanya boleh di dalam fungsi.", 1)
        } catch (e: Exception) {
            error = PyError("RuntimeError", e.message ?: "Kesalahan tidak dikenal.", 1)
        }
        return HasilEksekusi(output.toList(), error, trace.toList(), defDigunakan, langkah)
    }

    private fun eksekusi(s: Stmt, env: Lingkungan) {
        if (++langkah > MAX_LANGKAH) throw PyError("RuntimeError", "Program terlalu panjang atau loop tidak berhenti.", s.baris)
        when (s) {
            is Stmt.Ekspresi -> nilai(s.expr, env)
            is Stmt.Assign -> env.set(s.nama, nilai(s.nilai, env))
            is Stmt.If -> {
                if (benar(nilai(s.kondisi, env))) {
                    blok(s.lalu, env)
                } else {
                    var jalan = false
                    for ((k, b) in s.elifs) {
                        if (benar(nilai(k, env))) { blok(b, env); jalan = true; break }
                    }
                    if (!jalan) blok(s.selain, env)
                }
            }
            is Stmt.While -> {
                var putaran = 0
                while (benar(nilai(s.kondisi, env))) {
                    if (++putaran > MAX_PUTARAN) throw PyError("RuntimeError", "Loop while terlalu lama; pastikan kondisi bisa berhenti.", s.baris)
                    try { blok(s.badan, env) } catch (_: ContinueSignal) {
                    } catch (_: BreakSignal) { break }
                }
            }
            is Stmt.For -> {
                val iterable = nilai(s.iterable, env)
                val daftar = when (iterable) {
                    is List<*> -> iterable
                    is String -> iterable.map { it.toString() }
                    else -> throw PyError("TypeError", "for membutuhkan range() atau daftar.", s.baris)
                }
                for (item in daftar) {
                    env.set(s.variabel, item)
                    try { blok(s.badan, env) } catch (_: ContinueSignal) {
                    } catch (_: BreakSignal) { break }
                }
            }
            is Stmt.Def -> env.set(s.nama, FungsiPy(s.nama, s.parameter, s.badan, env))
            is Stmt.Return -> throw ReturnSignal(s.nilai?.let { nilai(it, env) })
            is Stmt.Break -> throw BreakSignal()
            is Stmt.Continue -> throw ContinueSignal()
            is Stmt.Pass -> Unit
        }
    }

    private fun blok(daftar: List<Stmt>, env: Lingkungan) {
        for (s in daftar) eksekusi(s, env)
    }

    private fun nilai(e: Expr, env: Lingkungan): Any? = when (e) {
        is Expr.Angka -> if (e.bulat) e.nilai.toInt() else e.nilai
        is Expr.Teks -> e.nilai
        is Expr.Bool -> e.nilai
        is Expr.Kosong -> null
        is Expr.Variabel -> try { env.ambil(e.nama) } catch (_: NoSuchElementException) {
            throw PyError("NameError", "Nama '${e.nama}' belum didefinisikan. Periksa ejaan atau buat variabelnya dulu.", e.baris)
        }
        is Expr.Unary -> when (e.op) {
            "-" -> -angka(nilai(e.kanan, env), e.baris)
            "not" -> !benar(nilai(e.kanan, env))
            else -> throw PyError("RuntimeError", "Operator unary tidak dikenal.", e.baris)
        }
        is Expr.Binary -> biner(e, env)
        is Expr.Panggil -> panggil(e, env)
    }

    private fun biner(e: Expr.Binary, env: Lingkungan): Any? {
        if (e.op == "and") {
            val kiri = nilai(e.kiri, env)
            return benar(kiri) && benar(nilai(e.kanan, env))
        }
        if (e.op == "or") {
            val kiri = nilai(e.kiri, env)
            return benar(kiri) || benar(nilai(e.kanan, env))
        }
        val a = nilai(e.kiri, env)
        val b = nilai(e.kanan, env)
        return when (e.op) {
            "==" -> sama(a, b)
            "!=" -> !sama(a, b)
            "<" -> banding(a, b, e.baris) < 0
            ">" -> banding(a, b, e.baris) > 0
            "<=" -> banding(a, b, e.baris) <= 0
            ">=" -> banding(a, b, e.baris) >= 0
            "+" -> when {
                a is String && b is String -> a + b
                a is List<*> && b is List<*> -> a + b
                else -> angka(a, e.baris) + angka(b, e.baris)
            }
            "-" -> angka(a, e.baris) - angka(b, e.baris)
            "*" -> when {
                a is String && b is Int -> a.repeat(b.coerceAtLeast(0))
                else -> angka(a, e.baris) * angka(b, e.baris)
            }
            "/" -> {
                val p = angka(b, e.baris)
                if (p == 0.0) throw PyError("ZeroDivisionError", "Tidak bisa membagi dengan nol.", e.baris)
                angka(a, e.baris) / p
            }
            "//" -> {
                val p = angka(b, e.baris)
                if (p == 0.0) throw PyError("ZeroDivisionError", "Tidak bisa membagi dengan nol.", e.baris)
                kotlin.math.floor(angka(a, e.baris) / p)
            }
            "%" -> {
                val p = angka(b, e.baris)
                if (p == 0.0) throw PyError("ZeroDivisionError", "Tidak bisa modulo dengan nol.", e.baris)
                angka(a, e.baris) % p
            }
            "**" -> Math.pow(angka(a, e.baris), angka(b, e.baris))
            else -> throw PyError("RuntimeError", "Operator '${e.op}' tidak dikenal.", e.baris)
        }
    }

    private fun panggil(e: Expr.Panggil, env: Lingkungan): Any? {
        val arg = e.args.map { nilai(it, env) }
        when (e.nama) {
            "print" -> {
                output.add(arg.joinToString(" ") { teks(it) })
                return null
            }
            "range" -> {
                val (mulai, akhir, langkah) = when (arg.size) {
                    1 -> Triple(0, int(arg[0], e.baris), 1)
                    2 -> Triple(int(arg[0], e.baris), int(arg[1], e.baris), 1)
                    3 -> Triple(int(arg[0], e.baris), int(arg[1], e.baris), int(arg[2], e.baris))
                    else -> throw PyError("TypeError", "range() menerima 1 sampai 3 argumen.", e.baris)
                }
                if (langkah == 0) throw PyError("ValueError", "Langkah range() tidak boleh 0.", e.baris)
                val hasil = mutableListOf<Int>()
                var i = mulai
                while ((langkah > 0 && i < akhir) || (langkah < 0 && i > akhir)) {
                    hasil.add(i)
                    if (hasil.size > 10_000) throw PyError("RuntimeError", "range() terlalu besar.", e.baris)
                    i += langkah
                }
                return hasil
            }
            "len" -> return when (val v = arg.firstOrNull()) {
                is String -> v.length
                is List<*> -> v.size
                else -> throw PyError("TypeError", "len() membutuhkan teks atau daftar.", e.baris)
            }
            "str" -> return teks(arg.firstOrNull())
            "int" -> return int(arg.firstOrNull(), e.baris)
            "float" -> return angka(arg.firstOrNull(), e.baris)
            "bool" -> return benar(arg.firstOrNull())
            "gerak" -> {
                val arah = arah(arg.firstOrNull(), e.baris)
                val berhasil = dunia.gerak(arah)
                trace.add(Langkah(dunia.ticks, dunia.x, dunia.y, "gerak", e.baris, if (berhasil) arah.name else "Tepi laut"))
                batasTick(e.baris)
                return berhasil
            }
            "lempar" -> {
                val hasil = dunia.lempar()
                trace.add(Langkah(dunia.ticks, dunia.x, dunia.y, "lempar", e.baris, pesanLempar(hasil)))
                batasTick(e.baris)
                return hasil.isi.ifEmpty { null }
            }
            "lihat" -> {
                val isi = dunia.lihat()
                trace.add(Langkah(dunia.ticks, dunia.x, dunia.y, "lihat", e.baris, isi))
                return isi
            }
            "get_x" -> return dunia.x
            "get_y" -> return dunia.y
            "ukuran" -> return dunia.ukuran
            "jumlah" -> return dunia.sisaIkan
        }
        val fungsi = try { env.ambil(e.nama) } catch (_: NoSuchElementException) {
            val saran = saranNama(e.nama)
            throw PyError("NameError", "Fungsi '${e.nama}' tidak dikenal.${if (saran != null) " Mungkin maksudmu '$saran'?" else ""}", e.baris)
        }
        if (fungsi !is FungsiPy) throw PyError("TypeError", "'${e.nama}' bukan fungsi.", e.baris)
        if (arg.size != fungsi.parameter.size) {
            throw PyError("TypeError", "Fungsi ${fungsi.nama} butuh ${fungsi.parameter.size} argumen, diberi ${arg.size}.", e.baris)
        }
        if (++kedalaman > 100) throw PyError("RecursionError", "Fungsi terlalu dalam memanggil dirinya sendiri.", e.baris)
        defDigunakan = true
        val lokal = Lingkungan(fungsi.lingkungan)
        fungsi.parameter.forEachIndexed { i, p -> lokal.set(p, arg[i]) }
        return try {
            blok(fungsi.badan, lokal)
            null
        } catch (r: ReturnSignal) {
            r.nilai
        } finally {
            kedalaman--
        }
    }

    private fun batasTick(baris: Int) {
        if (dunia.ticks > MAX_TICK) throw PyError("BatasTick", "Batas $MAX_TICK tick tercapai. Persingkat programmu.", baris)
    }

    private fun arah(v: Any?, baris: Int): Arah {
        val s = v?.toString() ?: throw PyError("TypeError", "gerak() membutuhkan arah.", baris)
        return Arah.entries.firstOrNull { it.name == s } ?: run {
            val saran = Arah.entries.minByOrNull { levenshtein(it.name.lowercase(), s.lowercase()) }
            throw PyError("ValueError", "Arah '$s' tidak dikenal.${if (saran != null) " Mungkin maksudmu '${saran.name}'?" else ""}", baris)
        }
    }

    private fun pesanLempar(h: HasilLempar): String = when {
        h.sampah -> "Sampah terangkat"
        h.isi.isEmpty() -> "Tidak ada ikan"
        else -> "Dapat ${h.isi}"
    }

    private fun int(v: Any?, baris: Int): Int = when (v) {
        is Int -> v
        is Double -> v.toInt()
        is Boolean -> if (v) 1 else 0
        is String -> v.toIntOrNull() ?: throw PyError("ValueError", "'$v' bukan bilangan bulat.", baris)
        else -> throw PyError("TypeError", "Dibutuhkan bilangan bulat.", baris)
    }

    private fun angka(v: Any?, baris: Int): Double = when (v) {
        is Int -> v.toDouble()
        is Double -> v
        is Boolean -> if (v) 1.0 else 0.0
        is String -> v.toDoubleOrNull() ?: throw PyError("ValueError", "'$v' bukan angka.", baris)
        else -> throw PyError("TypeError", "Dibutuhkan angka.", baris)
    }

    private fun banding(a: Any?, b: Any?, baris: Int): Int = when {
        a is String && b is String -> a.compareTo(b)
        a is Boolean && b is Boolean -> a.compareTo(b)
        else -> angka(a, baris).compareTo(angka(b, baris))
    }

    private fun sama(a: Any?, b: Any?): Boolean {
        if (a is Number && b is Number) return a.toDouble() == b.toDouble()
        return a == b
    }

    private fun benar(v: Any?): Boolean = when (v) {
        null -> false
        is Boolean -> v
        is Int -> v != 0
        is Double -> v != 0.0
        is String -> v.isNotEmpty()
        is List<*> -> v.isNotEmpty()
        else -> true
    }

    private fun teks(v: Any?): String = when (v) {
        null -> "None"
        is Boolean -> if (v) "True" else "False"
        is Double -> if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
        is List<*> -> v.joinToString(", ", "[", "]") { teks(it) }
        else -> v.toString()
    }

    private fun saranNama(nama: String): String? {
        val kandidat = listOf("print", "range", "len", "str", "int", "float", "bool", "gerak", "lempar", "lihat", "get_x", "get_y", "ukuran", "jumlah")
        return kandidat.minByOrNull { levenshtein(it.lowercase(), nama.lowercase()) }
            ?.takeIf { levenshtein(it.lowercase(), nama.lowercase()) <= 2 }
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length) {
            dp[i][j] = minOf(
                dp[i - 1][j] + 1,
                dp[i][j - 1] + 1,
                dp[i - 1][j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1,
            )
        }
        return dp[a.length][b.length]
    }

    companion object {
        const val MAX_TICK = 1000
        private const val MAX_LANGKAH = 10_000
        private const val MAX_PUTARAN = 50_000
    }
}

package com.example.codefishing.expert.py

class Parser(private val token: List<Token>) {
    private var pos = 0

    fun parse(): List<Stmt> {
        val hasil = mutableListOf<Stmt>()
        while (!cek(TokenType.EOF)) {
            if (cek(TokenType.NEWLINE)) { maju(); continue }
            if (cek(TokenType.DEDENT)) { maju(); continue }
            hasil.add(statement())
        }
        return hasil
    }

    private fun statement(): Stmt {
        val t = lihat()
        if (t.type == TokenType.KEYWORD) {
            return when (t.text) {
                "if" -> ifStmt()
                "while" -> whileStmt()
                "for" -> forStmt()
                "def" -> defStmt()
                "return" -> returnStmt()
                "break" -> { maju(); Stmt.Break(t.line) }
                "continue" -> { maju(); Stmt.Continue(t.line) }
                "pass" -> { maju(); Stmt.Pass(t.line) }
                else -> throw PyError("SyntaxError", "Kata kunci '${t.text}' tidak bisa memulai baris.", t.line, t.col)
            }
        }
        if (t.type == TokenType.IDENT && lihat(1).type == TokenType.ASSIGN) {
            maju()
            maju()
            val nilai = ekspresi()
            akhirBaris()
            return Stmt.Assign(t.text, nilai, t.line)
        }
        val expr = ekspresi()
        akhirBaris()
        return Stmt.Ekspresi(expr, t.line)
    }

    private fun ifStmt(): Stmt {
        val awal = harus(TokenType.KEYWORD, "if")
        val kondisi = ekspresi()
        harus(TokenType.COLON, ":")
        val lalu = suite()
        val elifs = mutableListOf<Pair<Expr, List<Stmt>>>()
        var selain = emptyList<Stmt>()
        while (cek(TokenType.KEYWORD, "elif")) {
            maju()
            val k = ekspresi()
            harus(TokenType.COLON, ":")
            elifs.add(k to suite())
        }
        if (cek(TokenType.KEYWORD, "else")) {
            maju()
            harus(TokenType.COLON, ":")
            selain = suite()
        }
        return Stmt.If(kondisi, lalu, elifs, selain, awal.line)
    }

    private fun whileStmt(): Stmt {
        val awal = harus(TokenType.KEYWORD, "while")
        val kondisi = ekspresi()
        harus(TokenType.COLON, ":")
        return Stmt.While(kondisi, suite(), awal.line)
    }

    private fun forStmt(): Stmt {
        val awal = harus(TokenType.KEYWORD, "for")
        val nama = harus(TokenType.IDENT, "nama variabel")
        harus(TokenType.KEYWORD, "in")
        val iterable = ekspresi()
        harus(TokenType.COLON, ":")
        return Stmt.For(nama.text, iterable, suite(), awal.line)
    }

    private fun defStmt(): Stmt {
        val awal = harus(TokenType.KEYWORD, "def")
        val nama = harus(TokenType.IDENT, "nama fungsi")
        harus(TokenType.LPAREN, "(")
        val params = mutableListOf<String>()
        if (!cek(TokenType.RPAREN)) {
            do {
                params.add(harus(TokenType.IDENT, "nama parameter").text)
                if (cek(TokenType.COMMA)) maju() else break
            } while (true)
        }
        harus(TokenType.RPAREN, ")")
        harus(TokenType.COLON, ":")
        return Stmt.Def(nama.text, params, suite(), awal.line)
    }

    private fun returnStmt(): Stmt {
        val awal = harus(TokenType.KEYWORD, "return")
        val nilai = if (cek(TokenType.NEWLINE) || cek(TokenType.EOF)) {
            null
        } else {
            ekspresi()
        }
        akhirBaris()
        return Stmt.Return(nilai, awal.line)
    }

    private fun suite(): List<Stmt> {
        harus(TokenType.NEWLINE, "baris baru setelah ':'")
        if (!cek(TokenType.INDENT)) {
            throw PyError("IndentationError", "Blok setelah ':' harus dijorokkan.", lihat().line)
        }
        maju()
        val isi = mutableListOf<Stmt>()
        while (!cek(TokenType.DEDENT) && !cek(TokenType.EOF)) {
            if (cek(TokenType.NEWLINE)) { maju(); continue }
            isi.add(statement())
        }
        if (cek(TokenType.DEDENT)) maju()
        if (isi.isEmpty()) throw PyError("SyntaxError", "Blok kode kosong.", lihat().line)
        return isi
    }

    // ---------- Ekspresi ----------

    private fun ekspresi(): Expr = atau()

    private fun atau(): Expr {
        var kiri = dan()
        while (cek(TokenType.KEYWORD, "or")) {
            val t = maju()
            kiri = Expr.Binary(kiri, "or", dan(), t.line)
        }
        return kiri
    }

    private fun dan(): Expr {
        var kiri = bukan()
        while (cek(TokenType.KEYWORD, "and")) {
            val t = maju()
            kiri = Expr.Binary(kiri, "and", bukan(), t.line)
        }
        return kiri
    }

    private fun bukan(): Expr {
        if (cek(TokenType.KEYWORD, "not")) {
            val t = maju()
            return Expr.Unary("not", bukan(), t.line)
        }
        return perbandingan()
    }

    private fun perbandingan(): Expr {
        var kiri = suku()
        while (true) {
            val op = when {
                cek(TokenType.EQ) -> "=="
                cek(TokenType.NE) -> "!="
                cek(TokenType.LT) -> "<"
                cek(TokenType.LE) -> "<="
                cek(TokenType.GT) -> ">"
                cek(TokenType.GE) -> ">="
                else -> break
            }
            val t = maju()
            kiri = Expr.Binary(kiri, op, suku(), t.line)
        }
        return kiri
    }

    private fun suku(): Expr {
        var kiri = faktor()
        while (cek(TokenType.PLUS) || cek(TokenType.MINUS)) {
            val t = maju()
            kiri = Expr.Binary(kiri, t.text, faktor(), t.line)
        }
        return kiri
    }

    private fun faktor(): Expr {
        var kiri = unary()
        while (cek(TokenType.STAR) || cek(TokenType.SLASH) || cek(TokenType.DSLASH) ||
            cek(TokenType.PERCENT) || cek(TokenType.POW)
        ) {
            val t = maju()
            kiri = Expr.Binary(kiri, t.text, unary(), t.line)
        }
        return kiri
    }

    private fun unary(): Expr {
        if (cek(TokenType.MINUS)) {
            val t = maju()
            return Expr.Unary("-", unary(), t.line)
        }
        return primary()
    }

    private fun primary(): Expr {
        val t = lihat()
        return when (t.type) {
            TokenType.ANGKA -> {
                maju()
                val bulat = !t.text.contains('.')
                Expr.Angka(t.text.toDouble(), bulat, t.line)
            }
            TokenType.TEKS -> { maju(); Expr.Teks(t.text, t.line) }
            TokenType.KEYWORD -> when (t.text) {
                "True" -> { maju(); Expr.Bool(true, t.line) }
                "False" -> { maju(); Expr.Bool(false, t.line) }
                "None" -> { maju(); Expr.Kosong(t.line) }
                else -> throw PyError("SyntaxError", "Ekspresi tidak lengkap di '${t.text}'.", t.line, t.col)
            }
            TokenType.IDENT -> {
                maju()
                if (cek(TokenType.LPAREN)) {
                    maju()
                    val args = mutableListOf<Expr>()
                    if (!cek(TokenType.RPAREN)) {
                        do {
                            args.add(ekspresi())
                            if (cek(TokenType.COMMA)) maju() else break
                        } while (true)
                    }
                    harus(TokenType.RPAREN, ")")
                    Expr.Panggil(t.text, args, t.line)
                } else {
                    Expr.Variabel(t.text, t.line)
                }
            }
            TokenType.LPAREN -> {
                maju()
                val e = ekspresi()
                harus(TokenType.RPAREN, ")")
                e
            }
            else -> throw PyError("SyntaxError", "Tidak mengharapkan '${t.text}'.", t.line, t.col)
        }
    }

    // ---------- Utilitas ----------

    private fun akhirBaris() {
        if (cek(TokenType.NEWLINE)) {
            maju()
        } else if (!cek(TokenType.EOF) && !cek(TokenType.DEDENT)) {
            throw PyError("SyntaxError", "Sisa '${lihat().text}' tidak bisa dibaca.", lihat().line, lihat().col)
        }
    }

    private fun lihat(offset: Int = 0): Token = token[(pos + offset).coerceAtMost(token.lastIndex)]

    private fun cek(type: TokenType): Boolean = lihat().type == type

    private fun cek(type: TokenType, teks: String): Boolean = lihat().type == type && lihat().text == teks

    private fun maju(): Token = token[pos.also { if (pos < token.lastIndex) pos++ }]

    private fun harus(type: TokenType, nama: String): Token {
        if (!cek(type)) {
            throw PyError("SyntaxError", "Dibutuhkan $nama, tetapi menemukan '${lihat().text}'.", lihat().line, lihat().col)
        }
        return maju()
    }
}

package com.example.codefishing.expert.py

enum class TokenType {
    IDENT, ANGKA, TEKS, KEYWORD, NEWLINE, INDENT, DEDENT,
    ASSIGN, EQ, NE, LT, LE, GT, GE,
    PLUS, MINUS, STAR, SLASH, DSLASH, PERCENT, POW,
    LPAREN, RPAREN, COMMA, COLON, EOF,
}

data class Token(val type: TokenType, val text: String, val line: Int, val col: Int)

class PyError(val jenis: String, message: String, val line: Int, val col: Int = 0) :
    RuntimeException(message)

class Lexer(private val sumber: String) {
    private var i = 0
    private var line = 1
    private var col = 1
    private var awalBaris = true
    private val indent = ArrayDeque<Int>().apply { addLast(0) }
    private val token = mutableListOf<Token>()

    fun tokenisasi(): List<Token> {
        val s = sumber.replace("\r\n", "\n").replace('\r', '\n')
        i = 0
        line = 1
        col = 1
        awalBaris = true
        indent.clear()
        indent.addLast(0)
        token.clear()
        while (i < s.length) {
            if (awalBaris) {
                if (tanganiIndentasi(s)) continue
            }
            val c = s[i]
            when {
                c == '#' -> lewatiKomentar(s)
                c == '\n' -> {
                    if (token.lastOrNull()?.type != TokenType.NEWLINE) {
                        token.add(Token(TokenType.NEWLINE, "\\n", line, col))
                    }
                    i++; line++; col = 1; awalBaris = true
                }
                c == ' ' || c == '\t' -> { i++; col++ }
                c == '"' || c == '\'' -> bacaTeks(s, c)
                c.isDigit() -> bacaAngka(s)
                c.isLetter() || c == '_' -> bacaIdent(s)
                else -> bacaOperator(s)
            }
        }
        if (token.lastOrNull()?.type != TokenType.NEWLINE && token.isNotEmpty()) {
            token.add(Token(TokenType.NEWLINE, "\\n", line, col))
        }
        while (indent.size > 1) {
            indent.removeLast()
            token.add(Token(TokenType.DEDENT, "", line, col))
        }
        token.add(Token(TokenType.EOF, "", line, col))
        return token
    }

    /** true jika baris berisi kode dan siap diproses. */
    private fun tanganiIndentasi(s: String): Boolean {
        var jumlah = 0
        var j = i
        while (j < s.length && (s[j] == ' ' || s[j] == '\t')) {
            jumlah += if (s[j] == '\t') 4 else 1
            j++
        }
        if (j >= s.length || s[j] == '\n' || s[j] == '#') {
            // Baris kosong/komentar: lewati sampai newline tanpa mengubah indentasi.
            i = j
            col = jumlah + 1
            while (i < s.length && s[i] != '\n') { i++; col++ }
            if (i < s.length && s[i] == '\n') { i++; line++; col = 1 }
            awalBaris = true
            return true
        }
        val sekarang = indent.last()
        if (jumlah > sekarang) {
            indent.addLast(jumlah)
            token.add(Token(TokenType.INDENT, "", line, 1))
        } else if (jumlah < sekarang) {
            while (indent.size > 1 && jumlah < indent.last()) {
                indent.removeLast()
                token.add(Token(TokenType.DEDENT, "", line, 1))
            }
            if (jumlah != indent.last()) throw PyError("IndentationError", "Jorokkan blok dengan jumlah spasi yang konsisten.", line)
        }
        i = j
        col = jumlah + 1
        awalBaris = false
        return false
    }

    private fun lewatiKomentar(s: String) {
        while (i < s.length && s[i] != '\n') { i++; col++ }
    }

    private fun bacaTeks(s: String, kutip: Char) {
        val mulaiLine = line
        val mulaiCol = col
        i++; col++
        val sb = StringBuilder()
        var tutup = false
        while (i < s.length) {
            val c = s[i]
            if (c == '\\') {
                i++; col++
                if (i >= s.length) break
                sb.append(
                    when (s[i]) {
                        'n' -> '\n'
                        't' -> '\t'
                        '\\' -> '\\'
                        '"' -> '"'
                        '\'' -> '\''
                        else -> s[i]
                    },
                )
                i++; col++
            } else if (c == kutip) {
                i++; col++
                tutup = true
                break
            } else if (c == '\n') {
                break
            } else {
                sb.append(c); i++; col++
            }
        }
        if (!tutup) throw PyError("SyntaxError", "Tanda kutip teks belum ditutup.", mulaiLine, mulaiCol)
        token.add(Token(TokenType.TEKS, sb.toString(), mulaiLine, mulaiCol))
    }

    private fun bacaAngka(s: String) {
        val mulai = i
        val mulaiCol = col
        while (i < s.length && s[i].isDigit()) { i++; col++ }
        if (i < s.length && s[i] == '.' && i + 1 < s.length && s[i + 1].isDigit()) {
            i++; col++
            while (i < s.length && s[i].isDigit()) { i++; col++ }
        }
        token.add(Token(TokenType.ANGKA, s.substring(mulai, i), line, mulaiCol))
    }

    private val kataKunci = setOf(
        "if", "elif", "else", "while", "for", "in", "def", "return",
        "and", "or", "not", "True", "False", "None", "break", "continue", "pass",
    )

    private fun bacaIdent(s: String) {
        val mulai = i
        val mulaiCol = col
        while (i < s.length && (s[i].isLetterOrDigit() || s[i] == '_')) { i++; col++ }
        val teks = s.substring(mulai, i)
        token.add(Token(if (teks in kataKunci) TokenType.KEYWORD else TokenType.IDENT, teks, line, mulaiCol))
    }

    private fun bacaOperator(s: String) {
        val mulaiCol = col
        fun dua(teks: String): Boolean {
            if (s.startsWith(teks, i)) {
                token.add(Token(opToken(teks), teks, line, mulaiCol))
                i += teks.length; col += teks.length
                return true
            }
            return false
        }
        if (dua("//") || dua("**") || dua("==") || dua("!=") || dua("<=") || dua(">=")) return
        val c = s[i]
        val type = when (c) {
            '=' -> TokenType.ASSIGN
            '<' -> TokenType.LT
            '>' -> TokenType.GT
            '+' -> TokenType.PLUS
            '-' -> TokenType.MINUS
            '*' -> TokenType.STAR
            '/' -> TokenType.SLASH
            '%' -> TokenType.PERCENT
            '(' -> TokenType.LPAREN
            ')' -> TokenType.RPAREN
            ',' -> TokenType.COMMA
            ':' -> TokenType.COLON
            else -> throw PyError("SyntaxError", "Karakter '${c}' tidak dikenal.", line, mulaiCol)
        }
        token.add(Token(type, c.toString(), line, mulaiCol))
        i++; col++
    }

    private fun opToken(teks: String): TokenType = when (teks) {
        "//" -> TokenType.DSLASH
        "**" -> TokenType.POW
        "==" -> TokenType.EQ
        "!=" -> TokenType.NE
        "<=" -> TokenType.LE
        ">=" -> TokenType.GE
        else -> TokenType.EOF
    }
}

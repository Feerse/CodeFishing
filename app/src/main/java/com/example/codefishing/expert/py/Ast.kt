package com.example.codefishing.expert.py

sealed class Expr(val baris: Int) {
    data class Angka(val nilai: Double, val bulat: Boolean, val barisExpr: Int) : Expr(barisExpr)
    data class Teks(val nilai: String, val barisExpr: Int) : Expr(barisExpr)
    data class Bool(val nilai: Boolean, val barisExpr: Int) : Expr(barisExpr)
    data class Kosong(val barisExpr: Int) : Expr(barisExpr)
    data class Variabel(val nama: String, val barisExpr: Int) : Expr(barisExpr)
    data class Unary(val op: String, val kanan: Expr, val barisExpr: Int) : Expr(barisExpr)
    data class Binary(val kiri: Expr, val op: String, val kanan: Expr, val barisExpr: Int) : Expr(barisExpr)
    data class Panggil(val nama: String, val args: List<Expr>, val barisExpr: Int) : Expr(barisExpr)
}

sealed class Stmt(val baris: Int) {
    data class Ekspresi(val expr: Expr, val barisStmt: Int) : Stmt(barisStmt)
    data class Assign(val nama: String, val nilai: Expr, val barisStmt: Int) : Stmt(barisStmt)
    data class If(
        val kondisi: Expr,
        val lalu: List<Stmt>,
        val elifs: List<Pair<Expr, List<Stmt>>>,
        val selain: List<Stmt>,
        val barisStmt: Int,
    ) : Stmt(barisStmt)

    data class While(val kondisi: Expr, val badan: List<Stmt>, val barisStmt: Int) : Stmt(barisStmt)
    data class For(val variabel: String, val iterable: Expr, val badan: List<Stmt>, val barisStmt: Int) : Stmt(barisStmt)
    data class Def(val nama: String, val parameter: List<String>, val badan: List<Stmt>, val barisStmt: Int) : Stmt(barisStmt)
    data class Return(val nilai: Expr?, val barisStmt: Int) : Stmt(barisStmt)
    data class Break(val barisStmt: Int) : Stmt(barisStmt)
    data class Continue(val barisStmt: Int) : Stmt(barisStmt)
    data class Pass(val barisStmt: Int) : Stmt(barisStmt)
}

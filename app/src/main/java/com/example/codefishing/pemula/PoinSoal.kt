package com.example.codefishing.pemula

/**
 * XP untuk satu jawaban benar, diambil dari `poin` soal.
 *
 * Dengan begitu label "+XP" yang tampil di layar sama dengan XP yang benar-benar
 * disimpan: soal stage Pasifik dan boss bernilai 25, soal reguler Antartika 20.
 */
fun xpJawabanBenar(poin: Int): Int = poin.coerceAtLeast(0)

package com.example.codefishing.core.data

/** Batas panjang nama panggilan ikan di Akuarium. */
const val MAKS_NAMA_AKUARIUM = 24

/** Rapikan nama panggilan; null berarti kembali memakai nama asli dari katalog. */
fun normalisasiNamaAkuarium(teks: String): String? =
    teks.trim().take(MAKS_NAMA_AKUARIUM).ifBlank { null }
package com.example.codefishing.core.data

/** Batas panjang nama panggilan ikan di Akuarium. */
const val MAKS_NAMA_AKUARIUM = 24

/** Rapikan nama panggilan; null berarti kembali memakai nama asli dari katalog. */
fun normalisasiNamaAkuarium(teks: String): String? =
    teks.trim().take(MAKS_NAMA_AKUARIUM).ifBlank { null }

/**
 * Nama yang ditampilkan di akuarium. Nama panggilan menggantikan nama spesies,
 * bukan ditampilkan sebagai nama kedua.
 */
fun namaTampilAkuarium(namaKustom: String?, namaSpesies: String): String =
    namaKustom?.takeIf { it.isNotBlank() } ?: namaSpesies
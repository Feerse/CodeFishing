package com.example.codefishing.core.data

import com.example.codefishing.core.db.KoleksiLengkapView

/** Kapasitas akuarium: maksimal 10 ekor ikan. */
const val KAPASITAS_AKUARIUM = 10

/** Satu ekor ikan yang dipajang di akuarium. [slotId] adalah identitas barisnya di database. */
data class IkanAkuarium(
    val slotId: Long,
    val ikan: KoleksiLengkapView,
    val nama: String?,
)

/** Hasil percobaan menambah ikan dari koleksi ke akuarium. */
enum class HasilTambahAkuarium {
    Berhasil,
    Penuh,
    StokHabis,
    TidakDimiliki,
}

/**
 * Cek apakah masih boleh menambah satu ekor dari spesies ini.
 * Kapasitas penuh didahulukan sebelum stok spesies habis.
 */
fun cekTambahAkuarium(
    jumlahDiAkuarium: Int,
    stokKoleksi: Int,
    totalDiAkuarium: Int,
    kapasitas: Int = KAPASITAS_AKUARIUM,
): HasilTambahAkuarium = when {
    totalDiAkuarium >= kapasitas -> HasilTambahAkuarium.Penuh
    jumlahDiAkuarium >= stokKoleksi.coerceAtLeast(0) -> HasilTambahAkuarium.StokHabis
    else -> HasilTambahAkuarium.Berhasil
}

/**
 * Bagi [total] ikan ke beberapa baris merata, maksimal [maksPerBaris] per baris.
 * Contoh 10 ikan, 3 per baris -> 3, 3, 2, 2.
 */
fun jumlahPerBaris(total: Int, maksPerBaris: Int = 3): List<Int> {
    if (total <= 0 || maksPerBaris <= 0) return emptyList()
    val baris = (total + maksPerBaris - 1) / maksPerBaris
    val dasar = total / baris
    val sisa = total % baris
    return List(baris) { r -> dasar + if (r < sisa) 1 else 0 }
}
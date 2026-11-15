package com.example.codefishing.pemula

/**
 * Menggeser posisi pilihan jawaban secara deterministik berdasarkan id soal.
 *
 * Tujuannya agar kunci jawaban tidak selalu menumpuk di posisi A/B, tanpa
 * merusak sesi kuis yang sudah tersimpan: sesi menyimpan indeks `terpilih`,
 * jadi urutan pilihan harus sama setiap kali soal yang sama dibuka.
 */
fun acakPilihan(pilihan: List<PilihanKuis>, soalId: Int): List<PilihanKuis> {
    val n = pilihan.size
    if (n <= 1) return pilihan
    val geser = ((soalId % n) + n) % n
    if (geser == 0) return pilihan
    return pilihan.drop(geser) + pilihan.take(geser)
}

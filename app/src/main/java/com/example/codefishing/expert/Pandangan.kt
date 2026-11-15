package com.example.codefishing.expert

import com.example.codefishing.expert.py.Langkah

/**
 * Petak mana yang isinya boleh diperlihatkan ke pemain.
 *
 * Papan misi sengaja **tidak** membuka isi petak sebelum program dijalankan. Kalau semua isi
 * terlihat dari awal, rute optimum bisa dihafal lalu ditulis apa adanya (`gerak(Timur); lempar()`
 * berulang) tanpa satu pun `lihat()`, sehingga misi berhenti menguji penalaran dan `lihat()` yang
 * gratis itu tidak pernah dipakai.
 *
 * Isi petak hanya terbuka untuk petak yang pernah **dilihat** (`lihat()`) atau **dilempar**
 * (`lempar()`) oleh program yang sedang diputar. `gerak()` tidak membuka apa pun.
 *
 * Pemakaian di layar: sebelum dijalankan trace masih kosong, jadi seluruh petak tertutup; saat
 * replay berjalan, petak terbuka satu per satu mengikuti tick.
 */
object Pandangan {
    /**
     * Indeks petak (y * [ukuran] + x) yang isinya sudah diketahui.
     *
     * @param trace jejak langkah hasil eksekusi program.
     * @param ukuran sisi papan (grid n x n).
     * @param sampaiTick hanya langkah dengan tick <= nilai ini yang dihitung; dipakai agar petak
     *   terbuka mengikuti jalannya animasi, bukan sekaligus di awal replay.
     */
    fun petakTerbuka(
        trace: List<Langkah>,
        ukuran: Int,
        sampaiTick: Int = Int.MAX_VALUE,
    ): Set<Int> {
        if (ukuran <= 0 || trace.isEmpty()) return emptySet()
        val terbuka = HashSet<Int>()
        for (langkah in trace) {
            if (langkah.tick > sampaiTick) continue
            if (langkah.aksi != "lihat" && langkah.aksi != "lempar") continue
            if (langkah.x !in 0 until ukuran || langkah.y !in 0 until ukuran) continue
            terbuka.add(langkah.y * ukuran + langkah.x)
        }
        return terbuka
    }
}

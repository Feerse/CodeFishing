package com.example.codefishing.pemula

/**
 * Wilayah tempat seekor ikan bisa didapat.
 *
 * Koleksi berisi 58 ikan yang datang dari tiga jalur berbeda, dan sebelumnya ketiganya tercampur
 * dalam satu grid yang hanya bisa disaring menurut kelangkaan. Pemain jadi tidak tahu ikan mana
 * yang harus dicari di peta mana. Pengelompokan ini menjawab pertanyaan itu:
 *
 * - [PASIFIK] — stage 1-10 (Samudra Pasifik) dan tangkapan sampingan Biasa/Langka yang ikut
 *   tertangkap saat menyelesaikan stage Pasifik.
 * - [ANTARTIKA] — stage 11-20 (Lautan Antartika) dan tangkapan sampingan Epik sampai Mitos yang
 *   hanya muncul di laut dalam.
 * - [KHUSUS] — hadiah combo/penyelesaian, bukan tangkapan liar, jadi tidak terikat peta mana pun.
 *
 * Label sengaja berbahasa Indonesia karena dipakai sebagai kunci kamus (`t(label)`).
 *
 * [labelSingkat] dipakai chip saring: nama lengkap tidak muat empat sekaligus dalam satu baris
 * selebar layar 360 dp, dan chip yang harus digulir membuat ketukan di tepinya meleset.
 */
enum class WilayahIkan(val label: String, val labelSingkat: String) {
    PASIFIK("Samudra Pasifik", "Pasifik"),
    ANTARTIKA("Lautan Antartika", "Antartika"),
    KHUSUS("Hadiah khusus", "Khusus"),
}

object WilayahIkanAturan {
    /**
     * Tentukan wilayah seekor ikan.
     *
     * @param nomorStage nomor stage asal ikan (bukan id), atau null untuk ikan tangkapan
     *   sampingan dan hadiah khusus.
     * @param tier tier katalog ikan, dipakai hanya bila [nomorStage] null.
     * @param syaratUnlock teks syarat dari katalog; hadiah khusus dikenali dari teksnya karena
     *   ikan-ikan itu bertier Langka sampai Mitos sehingga tidak bisa dibedakan dari tier saja.
     */
    fun dari(nomorStage: Int?, tier: String, syaratUnlock: String?): WilayahIkan = when {
        syaratUnlock == TangkapanSampingan.SYARAT_KHUSUS -> WilayahIkan.KHUSUS
        nomorStage != null ->
            if (ProgresPeta.diAntartika(nomorStage)) WilayahIkan.ANTARTIKA else WilayahIkan.PASIFIK
        // Sisa ikan tanpa stage adalah tangkapan sampingan; wilayahnya ditentukan tier, aturan yang
        // sama dengan pengundian di TangkapanSampingan supaya layar dan mesin tidak berbeda.
        TangkapanSampingan.tierCocok(tier, lautDalam = true) -> WilayahIkan.ANTARTIKA
        else -> WilayahIkan.PASIFIK
    }
}

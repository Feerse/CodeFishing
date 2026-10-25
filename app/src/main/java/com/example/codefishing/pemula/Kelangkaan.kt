package com.example.codefishing.pemula

/**
 * Aturan kelangkaan ikan (dari prototipe HTML).
 *  - Common..Epic : ikan Stage 1-9. Naik grade bila stage diselesaikan tanpa salah.
 *  - Rare         : jawaban benar beruntun 5 (Kerapu), 10 (Barakuda), 15 (Tenggiri).
 *  - Legend       : Arwana, seluruh stage lulus.
 *  - Mythic       : Hiu Merah, seluruh stage lulus tanpa salah.
 * Tim Logika Mode Pemula (aturan), Frontend (tampilan Koleksi dan Akuarium).
 */
object Kelangkaan {
    val STREAK_RARE = listOf(5 to "kerapu", 10 to "barakuda", 15 to "tenggiri")
}

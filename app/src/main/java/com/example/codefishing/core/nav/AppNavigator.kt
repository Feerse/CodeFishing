package com.example.codefishing.core.nav

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Navigator berbasis state (tanpa library tambahan).
 * Tidak ada tombol Kembali di toolbar (revisi dosen). Tombol Back sistem Android
 * tetap memanggil [back] supaya perilaku Android normal.
 */
class AppNavigator(start: Screen) {
    private var stack by mutableStateOf(listOf(NavEntry(start)))

    val current: NavEntry get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    /** Pindah ke layar baru dan taruh di atas tumpukan. */
    fun go(screen: Screen, stage: Int = current.stage, misi: Int = current.misi) {
        stack = stack + NavEntry(screen, stage, misi)
    }

    fun goHasil(screen: Screen, hasil: Long, stage: Int = current.stage, misi: Int = current.misi) {
        stack = stack + NavEntry(screen, stage, misi, hasil)
    }

    /** Ganti layar teratas (mis. dari Kuis ke Hasil Stage, supaya Back tidak kembali ke kuis). */
    fun replace(screen: Screen, stage: Int = current.stage, misi: Int = current.misi) {
        stack = stack.dropLast(1) + NavEntry(screen, stage, misi, current.hasil)
    }

    fun replaceHasil(screen: Screen, hasil: Long, stage: Int = current.stage, misi: Int = current.misi) {
        stack = stack.dropLast(1) + NavEntry(screen, stage, misi, hasil)
    }

    /** Tujuan dari drawer/bottom nav: kosongkan tumpukan, layar tujuan jadi akar. */
    fun resetTo(screen: Screen, stage: Int = 0, misi: Int = 0) {
        stack = listOf(NavEntry(screen, stage, misi))
    }

    /** @return true bila ada layar sebelumnya; false bila sudah di akar (biarkan sistem menutup app). */
    fun back(): Boolean {
        if (stack.size <= 1) return false
        stack = stack.dropLast(1)
        return true
    }
}

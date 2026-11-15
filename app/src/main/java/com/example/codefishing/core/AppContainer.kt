package com.example.codefishing.core

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.codefishing.core.audio.AudioManager
import com.example.codefishing.core.content.ContentRepository
import com.example.codefishing.core.data.GameRepository
import com.example.codefishing.core.data.ProgressRepository
import com.example.codefishing.core.data.ThemeStore
import com.example.codefishing.core.db.CodeFishingDatabase
import com.example.codefishing.core.db.SeedLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/** Pegangan ke semua layanan app. Dibuat sekali di MainActivity, dibagikan lewat [LocalContainer]. */
class AppContainer(
    context: Context,
    val theme: ThemeStore = ThemeStore(context),
) {
    private val appContext = context.applicationContext
    val db = CodeFishingDatabase.get(appContext)
    val game = GameRepository(appContext, db)
    /** Sementara dipertahankan untuk audio dan layar lama; data game memakai [game]. */
    val progress = ProgressRepository(appContext)
    val content = ContentRepository(appContext)
    val audio = AudioManager(appContext, progress)

    private val _siap = MutableStateFlow(false)
    val siap = _siap.asStateFlow()

    suspend fun initSeed() {
        val prefs = appContext.getSharedPreferences("codefishing_db", Context.MODE_PRIVATE)
        SeedLoader(appContext, db, prefs).seedIfNeeded()
        // Hangatkan observer Room selama animasi pembuka berjalan. Tanpa ini,
        // Home sempat tampil sekejap dengan angka default (0 XP, level 1)
        // karena kolektor pertama masih menyiapkan InvalidationTracker.
        game.observePlayer().first()
        game.observeStatistik().first()
        game.observeStages().first()
        _siap.value = true
    }
}

val LocalContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer belum disediakan")
}

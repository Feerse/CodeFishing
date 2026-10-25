package com.example.codefishing.core

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.codefishing.core.audio.AudioManager
import com.example.codefishing.core.content.ContentRepository
import com.example.codefishing.core.data.GameRepository
import com.example.codefishing.core.data.ProgressRepository
import com.example.codefishing.core.db.CodeFishingDatabase
import com.example.codefishing.core.db.SeedLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/** Pegangan ke semua layanan app. Dibuat sekali di MainActivity, dibagikan lewat [LocalContainer]. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val db = CodeFishingDatabase.get(appContext)
    val game = GameRepository(appContext, db)
    /** Sementara dipertahankan untuk audio dan layar lama; data game memakai [game]. */
    val progress = ProgressRepository(appContext)
    val content = ContentRepository(appContext)
    val audio = AudioManager(appContext, progress)

    init {
        val prefs = appContext.getSharedPreferences("codefishing_db", Context.MODE_PRIVATE)
        runBlocking(Dispatchers.IO) {
            SeedLoader(appContext, db, prefs).seedIfNeeded()
        }
    }
}

val LocalContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer belum disediakan")
}

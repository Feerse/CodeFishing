package com.example.codefishing.core

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.codefishing.core.audio.AudioManager
import com.example.codefishing.core.content.ContentRepository
import com.example.codefishing.core.data.ProgressRepository

/** Pegangan ke semua layanan app. Dibuat sekali di MainActivity, dibagikan lewat [LocalContainer]. */
class AppContainer(context: Context) {
    val progress = ProgressRepository(context)
    val content = ContentRepository(context)
    val audio = AudioManager(context, progress)
}

val LocalContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer belum disediakan")
}

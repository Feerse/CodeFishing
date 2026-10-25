package com.example.codefishing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.example.codefishing.core.AppContainer
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.frontend.App
import com.example.codefishing.ui.theme.CodeFishingTheme

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        container = AppContainer(applicationContext)
        setContent {
            CompositionLocalProvider(LocalContainer provides container) {
                CodeFishingTheme {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        App()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        container.audio.onAppPause()
    }

    override fun onResume() {
        super.onResume()
        container.audio.onAppResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) container.audio.release()
    }
}

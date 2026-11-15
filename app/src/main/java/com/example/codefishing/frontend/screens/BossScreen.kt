package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator

/** Layar kuis stage boss (10 soal, ambang lulus 8/10 atau 80%). */
@Composable
fun BossScreen(nav: AppNavigator, padding: PaddingValues) {
    // Stage boss mengikuti tujuan navigasi (10 atau 20), bukan stage tetap.
    KuisScreen(nav, padding)
}

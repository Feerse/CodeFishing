package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.codefishing.core.nav.AppNavigator

/** 12 Stage Boss: kuis 5 soal dengan ambang lulus 4/5 (80%). */
@Composable
fun BossScreen(nav: AppNavigator, padding: PaddingValues) {
    KuisScreen(nav, padding, stagePaksa = 10)
}

package com.example.codefishing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO(Frontend): ganti dengan Bricolage Grotesque (judul), Inter (isi), JetBrains Mono (kode)
// dengan menaruh berkas font di app/src/main/res/font lalu membuat FontFamily(Font(R.font.xxx)).
val HeadlineFont: FontFamily = FontFamily.SansSerif
val BodyFont: FontFamily = FontFamily.SansSerif
val CodeFont: FontFamily = FontFamily.Monospace

val Typography = Typography(
    headlineMedium = TextStyle(fontFamily = HeadlineFont, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = HeadlineFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = HeadlineFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = BodyFont, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = BodyFont, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = CodeFont, fontSize = 11.sp, letterSpacing = 0.8.sp),
)

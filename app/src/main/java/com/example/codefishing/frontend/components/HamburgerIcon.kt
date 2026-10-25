package com.example.codefishing.frontend.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/** Ikon tiga garis (menu hamburger) digambar sendiri, tanpa library ikon tambahan. */
@Composable
fun HamburgerIcon(
    modifier: Modifier = Modifier.size(24.dp),
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    Canvas(modifier) {
        val stroke = 2.dp.toPx()
        listOf(0.28f, 0.5f, 0.72f).forEach { f ->
            drawLine(
                color = color,
                start = Offset(size.width * 0.15f, size.height * f),
                end = Offset(size.width * 0.85f, size.height * f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

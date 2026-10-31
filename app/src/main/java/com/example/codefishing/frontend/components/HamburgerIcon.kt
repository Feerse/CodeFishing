package com.example.codefishing.frontend.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.art.Art

/** Ikon menu hamburger memakai aset final ikon_hamburger (lingkaran biru dengan tiga garis). */
@Composable
fun HamburgerIcon(modifier: Modifier = Modifier) {
    Gambar(Art.ikon_hamburger, null, modifier.size(32.dp))
}

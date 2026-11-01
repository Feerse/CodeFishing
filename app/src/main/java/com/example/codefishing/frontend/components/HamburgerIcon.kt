package com.example.codefishing.frontend.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.art.Art
import com.example.codefishing.ui.theme.LocalWarna

/** Ikon menu hamburger memakai aset final ikon_hamburger (lingkaran biru dengan tiga garis). */
@Composable
fun HamburgerIcon(modifier: Modifier = Modifier) {
    val gelap = LocalWarna.current.gelap
    Gambar(if (gelap) Art.ikon_hamburger else Art.ikon_hamburger_terang, null, modifier.size(32.dp))
}

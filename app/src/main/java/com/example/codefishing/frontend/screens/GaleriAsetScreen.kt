package com.example.codefishing.frontend.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.art.Art
import com.example.codefishing.frontend.components.Gambar

/**
 * Galeri aset (hanya build debug). Tim Aset dan Konten memakainya untuk memeriksa semua PNG di katalog [Art]
 * setelah menjalankan tools/sync_assets.py.
 */
@Composable
fun GaleriAsetScreen(padding: PaddingValues) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Art.GROUPS.forEach { (folder, daftar) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "$folder (${daftar.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            items(daftar) { (nama, id) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Gambar(id, nama, Modifier.fillMaxWidth().height(72.dp))
                    Text(nama, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                }
            }
        }
    }
}

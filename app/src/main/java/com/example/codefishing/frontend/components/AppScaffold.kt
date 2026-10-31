package com.example.codefishing.frontend.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.nav.DrawerItem
import kotlinx.coroutines.launch

/**
 * TIM: Frontend Compose. Toolbar seragam di semua layar (revisi dosen):
 * ikon hamburger di kiri, judul di tengah, slot kanan opsional (mis. indikator umpan).
 * TIDAK ADA tombol Kembali di toolbar. Drawer berisi 5 tujuan ([DrawerItem]).
 */
@Composable
fun AppScaffold(
    judul: String,
    aktif: DrawerItem?,
    onDrawerItem: (DrawerItem) -> Unit,
    modifier: Modifier = Modifier,
    kanan: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    isi: @Composable (PaddingValues) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val audio = LocalContainer.current.audio

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "CodeFishing",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                )
                DrawerItem.entries.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Gambar(item.ikon, null, Modifier.size(28.dp)) },
                        selected = item == aktif,
                        onClick = {
                            audio.play(Sfx.KlikUtama)
                            scope.launch { drawerState.close() }
                            onDrawerItem(item)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    IconButton(
                        onClick = {
                            audio.play(Sfx.KlikUtama)
                            scope.launch { drawerState.open() }
                        },
                        modifier = Modifier.semantics { contentDescription = "Buka menu" },
                    ) { HamburgerIcon() }
                    Text(
                        text = judul,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    kanan()
                    Spacer(Modifier.width(12.dp))
                }
            },
            bottomBar = bottomBar,
        ) { padding -> isi(padding) }
    }
}

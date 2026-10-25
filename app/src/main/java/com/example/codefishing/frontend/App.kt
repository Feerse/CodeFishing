package com.example.codefishing.frontend

import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.BottomItem
import com.example.codefishing.core.nav.DrawerItem
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.AppScaffold
import com.example.codefishing.frontend.components.IndikatorUmpan
import com.example.codefishing.frontend.screens.AkuariumScreen
import com.example.codefishing.frontend.screens.BossScreen
import com.example.codefishing.frontend.screens.GaleriAsetScreen
import com.example.codefishing.frontend.screens.HasilProgramScreen
import com.example.codefishing.frontend.screens.HasilStageScreen
import com.example.codefishing.frontend.screens.HomeScreen
import com.example.codefishing.frontend.screens.KoleksiScreen
import com.example.codefishing.frontend.screens.KuisScreen
import com.example.codefishing.frontend.screens.MateriScreen
import com.example.codefishing.frontend.screens.PengaturanScreen
import com.example.codefishing.frontend.screens.PengenalanScreen
import com.example.codefishing.frontend.screens.PetaScreen
import com.example.codefishing.frontend.screens.PilihModeScreen
import com.example.codefishing.frontend.screens.SandboxScreen
import com.example.codefishing.frontend.screens.UmpanHabisScreen

/** Layar Mode Belajar yang menampilkan indikator umpan di kanan toolbar. */
private val layarUmpan = setOf(
    Screen.Peta, Screen.Materi, Screen.Kuis, Screen.HasilStage, Screen.UmpanHabis, Screen.Boss,
)

/** Layar yang menampilkan bottom navigation 4 tab. */
private val layarBottom = setOf(
    Screen.Home, Screen.PilihMode, Screen.Peta, Screen.Materi, Screen.HasilStage,
    Screen.Sandbox, Screen.HasilProgram, Screen.Koleksi, Screen.Akuarium, Screen.Pengaturan,
)

/** Layar mana yang menyorot item drawer yang mana. */
private fun aktifDrawer(s: Screen): DrawerItem? = when (s) {
    Screen.Home, Screen.PilihMode -> DrawerItem.Home
    Screen.Peta, Screen.Materi, Screen.Kuis, Screen.HasilStage, Screen.UmpanHabis, Screen.Boss -> DrawerItem.ModeBelajar
    Screen.Sandbox, Screen.HasilProgram -> DrawerItem.ModeExpert
    Screen.Akuarium -> DrawerItem.Akuarium
    Screen.Pengaturan, Screen.GaleriAset -> DrawerItem.Pengaturan
    Screen.Koleksi, Screen.Pengenalan -> null
}

private fun aktifBottom(s: Screen): BottomItem? = when (s) {
    Screen.Home, Screen.PilihMode -> BottomItem.Home
    Screen.Peta, Screen.Materi, Screen.Kuis, Screen.HasilStage, Screen.UmpanHabis, Screen.Boss -> BottomItem.Peta
    Screen.Sandbox, Screen.HasilProgram -> BottomItem.Expert
    Screen.Koleksi, Screen.Akuarium -> BottomItem.Koleksi
    Screen.Pengaturan, Screen.GaleriAset, Screen.Pengenalan -> null
}

/** Akar tampilan: memilih layar dari navigator, memasang toolbar + bottom navigation seragam. */
@Composable
fun App() {
    val c = LocalContainer.current
    val nav = remember {
        AppNavigator(if (c.progress.onboardingSeen) Screen.Home else Screen.Pengenalan)
    }

    BackHandler(enabled = nav.canGoBack) { nav.back() }

    val saatIni = nav.current
    if (saatIni.screen == Screen.Pengenalan) {
        PengenalanScreen(onSelesai = {
            c.progress.onboardingSeen = true
            if (nav.canGoBack) nav.back() else nav.resetTo(Screen.Home)
        })
        return
    }

    val umpan by c.game.observeUmpan().collectAsState(null)
    LaunchedEffect(saatIni.screen) {
        c.game.muatUmpan()
    }

    AppScaffold(
        judul = saatIni.screen.judul,
        aktif = aktifDrawer(saatIni.screen),
        onDrawerItem = { nav.resetTo(it.target) },
        kanan = { if (saatIni.screen in layarUmpan) IndikatorUmpan(umpan?.sisa ?: 0) },
        bottomBar = {
            if (saatIni.screen in layarBottom) {
                val aktif = aktifBottom(saatIni.screen)
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    BottomItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = aktif == item,
                            onClick = { nav.resetTo(item.target) },
                            icon = { Text(item.simbol) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        when (saatIni.screen) {
            Screen.Home -> HomeScreen(nav, padding)
            Screen.PilihMode -> PilihModeScreen(nav, padding)
            Screen.Peta -> PetaScreen(nav, padding)
            Screen.Materi -> MateriScreen(nav, padding)
            Screen.Kuis -> KuisScreen(nav, padding)
            Screen.HasilStage -> HasilStageScreen(nav, padding)
            Screen.Sandbox -> SandboxScreen(nav, padding)
            Screen.HasilProgram -> HasilProgramScreen(nav, padding)
            Screen.Koleksi -> KoleksiScreen(nav, padding)
            Screen.Pengaturan -> PengaturanScreen(nav, padding)
            Screen.UmpanHabis -> UmpanHabisScreen(nav, padding)
            Screen.Boss -> BossScreen(nav, padding)
            Screen.Akuarium -> AkuariumScreen(nav, padding)
            Screen.GaleriAset -> GaleriAsetScreen(padding)
            Screen.Pengenalan -> Unit
        }
    }
}

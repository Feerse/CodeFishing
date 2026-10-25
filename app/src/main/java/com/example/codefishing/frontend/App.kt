package com.example.codefishing.frontend

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.DrawerItem
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.pemula.BaitStore
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

/** Layar mana yang menyorot item drawer yang mana. */
private fun aktifDrawer(s: Screen): DrawerItem? = when (s) {
    Screen.Home, Screen.PilihMode -> DrawerItem.Home
    Screen.Peta, Screen.Materi, Screen.Kuis, Screen.HasilStage, Screen.UmpanHabis, Screen.Boss -> DrawerItem.ModeBelajar
    Screen.Sandbox, Screen.HasilProgram -> DrawerItem.ModeExpert
    Screen.Akuarium -> DrawerItem.Akuarium
    Screen.Koleksi -> DrawerItem.Koleksi
    Screen.Pengaturan, Screen.GaleriAset -> DrawerItem.Pengaturan
    Screen.Pengenalan -> null
}

/** Akar tampilan: memilih layar dari navigator, memasang toolbar hamburger seragam. */
@Composable
fun App() {
    val c = LocalContainer.current
    val nav = remember {
        AppNavigator(if (c.progress.onboardingSeen) Screen.Home else Screen.Pengenalan)
    }
    val bait = remember { BaitStore(c.progress, c.content.config) }

    BackHandler(enabled = nav.canGoBack) { nav.back() }

    val saatIni = nav.current
    if (saatIni.screen == Screen.Pengenalan) {
        // Pengenalan: tanpa toolbar, muncul sekali (atau lewat Pengaturan).
        PengenalanScreen(onSelesai = {
            c.progress.onboardingSeen = true
            if (nav.canGoBack) nav.back() else nav.resetTo(Screen.Home)
        })
        return
    }

    AppScaffold(
        judul = saatIni.screen.judul,
        aktif = aktifDrawer(saatIni.screen),
        onDrawerItem = { nav.resetTo(it.target) },
        kanan = { if (saatIni.screen in layarUmpan) IndikatorUmpan(bait.jumlah()) },
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
            Screen.Pengenalan -> Unit // sudah ditangani di atas
        }
    }
}

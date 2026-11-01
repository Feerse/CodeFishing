package com.example.codefishing.core.nav

import androidx.annotation.DrawableRes
import com.example.codefishing.core.art.Art

/** 14 layar sesuai prototipe HTML (nomor = id di prototipe) + 1 layar khusus debug. */
enum class Screen(val id: Int, val judul: String) {
    Home(1, "CodeFishing"),
    PilihMode(2, "Pilih Mode"),
    Peta(3, "Peta Perairan"),
    Materi(4, "Materi"),
    Kuis(5, "Kuis"),
    HasilStage(6, "Hasil Stage"),
    Sandbox(7, "Mode Expert"),
    HasilProgram(8, "Hasil Program"),
    Koleksi(9, "Koleksi"),
    Pengaturan(10, "Pengaturan"),
    UmpanHabis(11, "Umpan Habis"),
    Boss(12, "Stage Boss"),
    Akuarium(13, "Akuarium"),
    Pengenalan(14, "Pengenalan"),

    /** Hanya muncul di build debug (Pengaturan > Galeri Aset). */
    GaleriAset(99, "Galeri Aset"),
}

/** Satu entri di tumpukan navigasi. stage/misi/hasil dipakai layar terkait. */
data class NavEntry(
    val screen: Screen,
    val stage: Int = 0,
    val misi: Int = 0,
    val hasil: Long = 0,
)

/** Lima tujuan drawer hamburger sesuai DFD K9. Koleksi ada di bottom nav. */
enum class DrawerItem(
    val label: String,
    val target: Screen,
    @DrawableRes val ikon: Int,
    @DrawableRes val ikonTerang: Int,
) {
    Home("Home", Screen.Home, Art.ikon_home, Art.ikon_home_terang),
    ModeBelajar("Mode Belajar", Screen.Peta, Art.ikon_mode_belajar, Art.ikon_mode_belajar_terang),
    ModeExpert("Mode Expert", Screen.Sandbox, Art.ikon_mode_expert, Art.ikon_mode_expert_terang),
    Akuarium("Akuarium", Screen.Akuarium, Art.ikon_akuarium, Art.ikon_akuarium_terang),
    Pengaturan("Pengaturan", Screen.Pengaturan, Art.ikon_pengaturan, Art.ikon_pengaturan_terang),
}

/** Empat tab bottom navigation sesuai DFD K9. */
enum class BottomItem(
    val label: String,
    @DrawableRes val ikon: Int,
    @DrawableRes val ikonTerang: Int,
    val target: Screen,
) {
    Home("Home", Art.ikon_home, Art.ikon_home_terang, Screen.Home),
    Peta("Peta", Art.ikon_mode_belajar, Art.ikon_mode_belajar_terang, Screen.Peta),
    Expert("Expert", Art.ikon_mode_expert, Art.ikon_mode_expert_terang, Screen.Sandbox),
    Koleksi("Koleksi", Art.ikon_koleksi, Art.ikon_koleksi_terang, Screen.Koleksi),
}

package com.example.codefishing

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.example.codefishing.core.AppContainer
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.data.LocalEfekVisual
import com.example.codefishing.core.data.ThemeStore
import com.example.codefishing.frontend.App
import com.example.codefishing.frontend.components.PemuatAset
import com.example.codefishing.frontend.components.Pembuka
import com.example.codefishing.ui.theme.CodeFishingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        // Baca tema sebelum super.onCreate agar window/splash langsung memakai
        // latar yang tepat, tanpa menunggu database game selesai dimuat.
        val theme = ThemeStore(applicationContext)
        val sistemGelap = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val gelap = theme.mode.value.gelap(sistemGelap)
        setTheme(
            if (gelap) R.style.Theme_CodeFishing_Gelap
            else R.style.Theme_CodeFishing_Terang,
        )

        super.onCreate(savedInstanceState)
        // Warna window langsung disamakan agar tidak ada kilatan gelap saat
        // AppContainer menyiapkan database di awal.
        window.setBackgroundDrawable(
            ContextCompat.getColor(
                this,
                if (gelap) R.color.neutral_deep else R.color.neutral_light,
            ).toDrawable(),
        )
        enableEdgeToEdge()
        container = AppContainer(applicationContext, theme)
        // Muat seluruh katalog aset di thread latar selama layar pembuka diputar. Gerbang di bawah
        // memastikan layar utama baru dibuka setelah semuanya siap, sehingga tidak ada gambar yang
        // muncul terlambat atau kosong saat pemain mulai memakai aplikasi.
        PemuatAset.mulai(applicationContext, gelap)
        setContent {
            CompositionLocalProvider(LocalContainer provides container) {
                val mode by container.theme.mode.collectAsState()
                val siap by container.siap.collectAsState()
                val muat by PemuatAset.status.collectAsState()
                val pilihanEfek by container.efekVisual.pilihan.collectAsState()
                // "Otomatis" sudah diterjemahkan ke Penuh/Hemat di store, jadi komponen gambar
                // cukup membaca satu nilai pasti tanpa tahu jenis perangkatnya.
                val efekAktif = remember(pilihanEfek) { container.efekVisual.aktif(pilihanEfek) }
                // Pembuka ditampilkan sekali tiap aplikasi dibuka dan tidak
                // dipotong oleh cepatnya proses seed. Layar utama baru muncul
                // setelah animasi selesai, data siap, DAN semua aset termuat.
                var pembukaSelesai by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        container.initSeed()
                    }
                }

                CompositionLocalProvider(LocalEfekVisual provides efekAktif) {
                    CodeFishingTheme(mode) {
                        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            if (siap && muat.selesaiSemua && pembukaSelesai) {
                                App()
                            } else {
                                // Bila aset sudah selesai tetapi data permainan belum, tahan bilah di
                                // 98% agar tidak terlihat "selesai" sebelum aplikasi benar-benar siap.
                                val progres = if (siap) muat.progres else muat.progres.coerceAtMost(0.98f)
                                val teks = when {
                                    siap -> muat.teks
                                    muat.selesaiSemua -> "Menyiapkan data permainan..."
                                    else -> muat.teks
                                }
                                Pembuka(
                                    progresMuat = progres,
                                    status = teks,
                                    onSelesai = { pembukaSelesai = true },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        container.audio.onAppPause()
    }

    override fun onResume() {
        super.onResume()
        container.audio.onAppResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) container.audio.release()
    }
}

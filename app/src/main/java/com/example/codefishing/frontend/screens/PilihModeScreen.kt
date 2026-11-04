package com.example.codefishing.frontend.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codefishing.core.LocalContainer
import com.example.codefishing.core.art.Art
import com.example.codefishing.core.audio.Sfx
import com.example.codefishing.core.nav.AppNavigator
import com.example.codefishing.core.nav.Screen
import com.example.codefishing.frontend.components.Gambar
import com.example.codefishing.ui.theme.LocalWarna
import kotlinx.coroutines.launch

/**
 * 02 Pilih Mode. Frontend.
 * Dua kartu bergambar: Belajar (Pemula) dan Sandbox (Expert), tanpa kalimat penjelasan panjang.
 */
@Composable
fun PilihModeScreen(nav: AppNavigator, padding: PaddingValues) {
    val c = LocalContainer.current
    val warna = LocalWarna.current
    val scope = rememberCoroutineScope()
    var tampil by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { tampil = true }

    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(warna.latarAtas, warna.latarBawah))),
    ) {
        // Pola ombak samar di bawah, supaya bidang kosong tidak terasa datar.
        Gambar(
            Art.ombak_pola,
            null,
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(240.dp)
                .alpha(0.16f),
            ContentScale.Crop,
            warna = warna.ombak,
        )

        BoxWithConstraints(Modifier.fillMaxSize()) {
            // Dua kartu mengisi sisa tinggi layar; minimum 232dp agar tetap terbaca di layar pendek.
            val tinggiKartu = ((maxHeight - 128.dp) / 2).coerceAtLeast(232.dp)

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(Modifier.padding(bottom = 2.dp)) {
                    Text(
                        "PILIH PERAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        letterSpacing = 2.sp,
                    )
                    Text(
                        "Mau memancing bagaimana?",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Masuk(tampil, 0) {
                    KartuMode(
                        judul = "Belajar",
                        tag = "Pemula",
                        fakta = "2 peta · 20 stage · kuis pilihan ganda",
                        ikon = if (warna.gelap) Art.ikon_mode_belajar else Art.ikon_mode_belajar_terang,
                        aksen = MaterialTheme.colorScheme.primary,
                        onAksen = MaterialTheme.colorScheme.onPrimary,
                        tinggi = tinggiKartu,
                        onClickLabel = "Pilih mode Belajar",
                        onClick = {
                            c.audio.play(Sfx.Klik)
                            scope.launch {
                                c.game.simpanPosisi(null, "belajar")
                                nav.go(Screen.Peta)
                            }
                        },
                        latar = {
                            Gambar(
                                if (warna.gelap) Art.latar_bawah_laut else Art.latar_bawah_laut_terang,
                                null,
                                Modifier.fillMaxSize(),
                                ContentScale.Crop,
                            )
                        },
                    )
                }

                Masuk(tampil, 90) {
                    KartuMode(
                        judul = "Sandbox",
                        tag = "Expert",
                        fakta = "6 misi · mode bebas",
                        ikon = if (warna.gelap) Art.ikon_mode_expert else Art.ikon_mode_expert_terang,
                        aksen = MaterialTheme.colorScheme.tertiary,
                        onAksen = MaterialTheme.colorScheme.onTertiary,
                        tinggi = tinggiKartu,
                        onClickLabel = "Pilih mode Sandbox",
                        onClick = {
                            c.audio.play(Sfx.Klik)
                            scope.launch {
                                c.game.simpanPosisi(null, "expert")
                                nav.go(Screen.Sandbox)
                            }
                        },
                        latar = { PanelSandbox(warna.gelap, MaterialTheme.colorScheme.tertiary) },
                    )
                }

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

/** Latar kartu Sandbox: panel kode dengan ilustrasi editor, bukan lagi adegan kapal. */
@Composable
private fun BoxScope.PanelSandbox(gelap: Boolean, aksen: Color) {
    val warna = LocalWarna.current
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            warna.kartuTinggi,
                            warna.kartu,
                            if (gelap) warna.kodeLatar else warna.kartuLembut,
                        ),
                    ),
                ),
        )
        // Cahaya aksen di balik ilustrasi agar panel tidak terasa gelap dan datar.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(aksen.copy(alpha = 0.18f), Color.Transparent),
                    ),
                ),
        )
        // Tanda air ikon kode.
        Gambar(
            Art.ikon_kode,
            null,
            Modifier
                .align(Alignment.Center)
                .size(170.dp)
                .alpha(0.06f),
            warna = aksen,
        )
        // Pola gelombang tipis di dasar panel.
        Gambar(
            Art.ombak_pola,
            null,
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(110.dp)
                .alpha(0.10f),
            ContentScale.Crop,
            warna = aksen,
        )
        // Varian ilustrasi mengikuti tema: gelap untuk tema gelap, terang untuk tema terang.
        Gambar(
            if (gelap) Art.ilustrasi_editor_kode else Art.ilustrasi_editor_kode_terang,
            null,
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .height(124.dp),
            ContentScale.Fit,
        )
    }
}

@Composable
private fun KartuMode(
    judul: String,
    tag: String,
    fakta: String,
    ikon: Int,
    aksen: Color,
    onAksen: Color,
    tinggi: Dp,
    onClickLabel: String,
    onClick: () -> Unit,
    latar: @Composable BoxScope.() -> Unit,
) {
    val warna = LocalWarna.current
    val interaksi = remember { MutableInteractionSource() }
    val ditekan by interaksi.collectIsPressedAsState()
    val skala by animateFloatAsState(
        targetValue = if (ditekan) 0.972f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "skala-kartu",
    )
    val bentuk = RoundedCornerShape(24.dp)

    Box(
        Modifier
            .fillMaxWidth()
            .height(tinggi)
            .graphicsLayer {
                scaleX = skala
                scaleY = skala
            }
            .clip(bentuk)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interaksi,
                indication = LocalIndication.current,
                role = Role.Button,
                onClickLabel = onClickLabel,
                onClick = onClick,
            ),
    ) {
        latar()

        // Scrim agar judul dan fakta tetap terbaca di atas gambar.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.42f to Color.Transparent,
                        0.7f to MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        0.86f to MaterialTheme.colorScheme.surface,
                        1f to MaterialTheme.colorScheme.surface,
                    ),
                ),
        )

        Row(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                        .border(1.dp, aksen.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Gambar(ikon, null, Modifier.size(16.dp))
                    Text(
                        tag.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        // Di tema gelap aksen oranye digeser sedikit ke arah putih agar kontras teks kecil tetap cukup.
                        color = if (warna.gelap) {
                            Color.White.copy(alpha = 0.25f).compositeOver(aksen)
                        } else {
                            aksen
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    judul,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    fakta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(aksen)
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Play",
                    color = onAksen,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Garis tepi digambar terakhir agar tidak tertutup gambar latar.
        Box(
            Modifier
                .matchParentSize()
                .border(1.dp, warna.garis, bentuk),
        )
    }
}

/** Animasi masuk berjenjang: pudar + naik sedikit. */
@Composable
private fun Masuk(aktif: Boolean, delayMs: Int, isi: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = aktif,
        enter = fadeIn(
            animationSpec = tween(380, delayMillis = delayMs, easing = FastOutSlowInEasing),
        ) + slideInVertically(
            animationSpec = tween(380, delayMillis = delayMs, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 6 },
        ),
    ) {
        isi()
    }
}

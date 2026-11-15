package com.example.codefishing.frontend.components

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/*
 * Mesin gerak adegan Mode Belajar.
 *
 * Seluruh alat (joran, tali, pelampung, kail, umpan, jala, bubu, tombak, pukat) digambar
 * dinamis di atas latar `adegan_*` yang sudah BERSIH dari alat. Dulu alat ikut tergambar
 * permanen di PNG sehingga muncul kail ganda. Satu-satunya sumber alat sekarang adalah
 * fungsi [hitungKeadaan] di file ini.
 *
 * Kontrak kontinuitas (supaya tidak ada lompatan/umpan tertinggal):
 *   akhir Lempar  == awal Dapat == awal Lepas
 *   akhir Dapat   == keadaan Diam (alat kembali siap, ikan sudah diamankan)
 *   akhir Lepas   == alat gagal (tali putus / ikan kabur) dan tidak ada sisa pelampung
 */

internal data class TitikPx(
    val pangkal: Offset,
    val ujung: Offset,
    val target: Offset,
    val tujuan: Offset,
    val aux: Offset,
    /** Panjang batang (px layar). 0 jika adegan tanpa joran. */
    val panjang: Float,
    /** Piksel layar per piksel gambar sumber. Dipakai untuk semua ukuran. */
    val s: Float,
    val tinggiKotak: Float,
    val lebarKotak: Float,
)

internal data class SegTali(
    val a: Offset,
    val b: Offset,
    val kendur: Float,
    val alpha: Float = 1f,
    val lebar: Float = 1.4f,
)

internal data class Keadaan(
    val batangPangkal: Offset? = null,
    val batangUjung: Offset? = null,
    val batangLengkung: Float = 0f,
    val tali: SegTali? = null,
    val tali2: SegTali? = null,
    val tali3: SegTali? = null,
    val pelampung: Offset? = null,
    val pelampungAlpha: Float = 1f,
    val pelampungRot: Float = 0f,
    val kail: Offset? = null,
    val kailAlpha: Float = 1f,
    val kailUmpan: Boolean = true,
    val alat: Offset? = null,
    val alatLebar: Float = 0f,
    val alatSkalaY: Float = 1f,
    val alatRot: Float = 0f,
    val alatSpin: Float = 0f,
    val alatAlpha: Float = 1f,
    val ikan: Offset? = null,
    val ikanAlpha: Float = 0f,
    val ikanRot: Float = 0f,
    val ikanSkala: Float = 1f,
    val ikanBalik: Boolean = false,
    val tombakMulai: Offset? = null,
    val tombakUjung: Offset? = null,
    val garisBidikAlpha: Float = 0f,
    val garisBidikDari: Offset? = null,
    val garisBidikKe: Offset? = null,
    val jejakMulai: Offset? = null,
    val jejakSelesai: Offset? = null,
    val jejakT: Float = 0f,
    val splash: Offset? = null,
    val splashT: Float = 0f,
    val riak: Offset? = null,
    val riakT: Float = 0f,
    val riakBahaya: Boolean = false,
    val ledakan: Offset? = null,
    val ledakanT: Float = 0f,
    val kilau: Offset? = null,
    val kilauT: Float = 0f,
    val serpihan: Offset? = null,
    val serpihanT: Float = 0f,
    val getarKamera: Float = 0f,
)

private const val DUA_PI = (2.0 * PI).toFloat()
private const val PI_F = PI.toFloat()

internal fun seg(a: Float, dari: Float, ke: Float): Float =
    ((a - dari) / (ke - dari)).coerceIn(0f, 1f)

internal fun halus(t: Float): Float = t * t * (3f - 2f * t)
internal fun keluar(t: Float): Float = 1f - (1f - t) * (1f - t) * (1f - t)
internal fun campur(a: Float, b: Float, t: Float): Float = a + (b - a) * t
internal fun campurO(a: Offset, b: Offset, t: Float): Offset =
    Offset(campur(a.x, b.x, t), campur(a.y, b.y, t))

private fun busur(a: Offset, b: Offset, t: Float, tinggi: Float): Offset =
    Offset(campur(a.x, b.x, t), campur(a.y, b.y, t) - tinggi * 4f * t * (1f - t))

private fun bezier(p0: Offset, c: Offset, p2: Offset, t: Float): Offset {
    val u = 1f - t
    return Offset(
        u * u * p0.x + 2f * u * t * c.x + t * t * p2.x,
        u * u * p0.y + 2f * u * t * c.y + t * t * p2.y,
    )
}

private fun sudut(v: Offset): Float = atan2(v.y, v.x)
private fun ujungBatang(g: Offset, th: Float, len: Float): Offset =
    Offset(g.x + cos(th) * len, g.y + sin(th) * len)

private fun jarak(a: Offset, b: Offset): Float = hypot(b.x - a.x, b.y - a.y)
private fun tanda(x: Float): Float = if (x >= 0f) 1f else -1f

/** Arah tegak lurus batang (searah putaran sudut). */
private fun tegak(th: Float): Offset = Offset(-sin(th), cos(th))

private fun arahLengkung(th: Float, tip: Offset, ke: Offset): Float {
    val p = tegak(th)
    return tanda(p.x * (ke.x - tip.x) + p.y * (ke.y - tip.y))
}

/** Titik sumber pelampung/kail yang bergoyang pelan menurut ombak. */
private fun ayun(ombak: Float, gerak: Float, skala: Float): Float =
    sin(ombak * DUA_PI) * skala * gerak

internal fun hitungKeadaan(
    jenis: JenisAdegan,
    fase: FasePancing,
    a: Float,
    berhasil: Boolean,
    t: TitikPx,
    ombak: Float,
    gerak: Float,
): Keadaan = when (jenis) {
    JenisAdegan.Kapal, JenisAdegan.Dermaga -> keadaanJoran(fase, a, t, ombak, gerak)
    JenisAdegan.LubangEs -> keadaanEs(fase, a, t, ombak, gerak)
    JenisAdegan.Selam -> keadaanSelam(fase, a, berhasil, t, ombak, gerak)
    JenisAdegan.PerahuCadik -> keadaanJala(fase, a, t, ombak, gerak)
    JenisAdegan.Bubu -> keadaanBubu(fase, a, t, ombak, gerak)
    JenisAdegan.Badai -> keadaanPukat(fase, a, t, ombak, gerak)
}

// ---------------------------------------------------------------------------------------
// Joran di kapal dan dermaga: lempar -> pelampung mengapung -> gigitan -> tarik / putus.
// ---------------------------------------------------------------------------------------
private fun keadaanJoran(
    fase: FasePancing,
    a: Float,
    t: TitikPx,
    ombak: Float,
    gerak: Float,
): Keadaan {
    val g = t.pangkal
    val len = t.panjang
    val s = t.s
    val dir = if (t.target.x >= g.x) 1f else -1f
    val th0 = sudut(t.ujung - g)
    val thBack = sudut(Offset(-dir * 0.35f, -1f))
    val thFwd = sudut(Offset(dir, -0.25f))
    val thLift = sudut(Offset(dir * 0.30f, -1f))
    val land = t.target
    val bob = ayun(ombak, gerak, 3f * s)
    val goyang = ayun(ombak, gerak, 0.012f)
    val tip0 = ujungBatang(g, th0 + goyang, len)
    val gantungDiam = Offset(ayun(ombak, gerak, 0.015f * len), 0.30f * len)
    val kailGantung = 52f * s
    val kailAir = 46f * s

    fun gantung(x: Float) = Offset(
        -dir * 0.10f * len * halus(seg(x, 0f, 0.28f)) + dir * 0.12f * len * halus(seg(x, 0.28f, 0.40f)),
        0.30f * len,
    )

    fun thLempar(x: Float): Float = when {
        x < 0.28f -> campur(th0, thBack, halus(seg(x, 0f, 0.28f)))
        x < 0.42f -> campur(thBack, thFwd, keluar(seg(x, 0.28f, 0.42f)))
        else -> campur(thFwd, th0, halus(seg(x, 0.55f, 1f)))
    }

    when (fase) {
        FasePancing.Diam -> {
            val p = tip0 + gantungDiam
            return Keadaan(
                batangPangkal = g,
                batangUjung = tip0,
                batangLengkung = 0.02f * len,
                tali = SegTali(tip0, p, 0.012f * jarak(tip0, p)),
                pelampung = p,
                pelampungRot = ayun(ombak, gerak, 6f),
                kail = p + Offset(0f, kailGantung),
            )
        }

        FasePancing.Lempar -> {
            val th = thLempar(a)
            val tip = ujungBatang(g, th, len)
            val rel = ujungBatang(g, thLempar(0.40f), len) + gantung(0.40f)
            val p = when {
                a < 0.40f -> tip + gantung(a)
                a < 0.66f -> busur(
                    rel, land, keluar(seg(a, 0.40f, 0.66f)),
                    0.30f * abs(land.x - rel.x) + 0.08f * t.tinggiKotak,
                )
                else -> land + Offset(0f, bob)
            }
            val whip = sin(PI_F * seg(a, 0.28f, 0.52f))
            val rot = tanda(thFwd - thBack)
            val leng = -rot * 0.20f * len * whip -
                (if (a < 0.28f) tanda(thBack - th0) * 0.05f * len * sin(PI_F * seg(a, 0f, 0.28f)) else 0f)
            val frac = when {
                a < 0.40f -> 0.01f
                a < 0.66f -> 0.07f
                else -> campur(0.07f, 0.035f, seg(a, 0.66f, 1f))
            }
            val tenggelam = seg(a, 0.66f, 0.74f)
            val kailOff = campur(kailGantung, kailAir, if (a < 0.66f) 0f else tenggelam)
            return Keadaan(
                batangPangkal = g,
                batangUjung = tip,
                batangLengkung = leng,
                tali = SegTali(tip, p, frac * jarak(tip, p)),
                pelampung = p,
                pelampungRot = ayun(ombak, gerak, 8f) + (if (a in 0.40f..0.66f) 25f * (a - 0.4f) else 0f),
                kail = p + Offset(0f, kailOff),
                kailAlpha = campur(1f, 0.45f, tenggelam),
                splash = if (a >= 0.66f) land else null,
                splashT = seg(a, 0.66f, 0.94f),
                riak = if (a >= 0.66f) land else null,
                riakT = seg(a, 0.66f, 1f),
            )
        }

        FasePancing.Dapat -> {
            val gigit = if (a < 0.22f) abs(sin(a / 0.22f * PI_F * 2.5f)) * 9f * s else 0f
            val thT = when {
                a < 0.22f -> th0
                a < 0.34f -> campur(th0, thLift, keluar(seg(a, 0.22f, 0.34f)))
                a < 0.62f -> thLift
                else -> campur(thLift, th0, halus(seg(a, 0.62f, 0.90f)))
            }
            val tip = ujungBatang(g, thT, len)
            val reel = halus(seg(a, 0.34f, 0.86f))
            val ctrl = Offset(
                campur(land.x, t.tujuan.x, 0.45f),
                min(land.y, t.tujuan.y) - 0.30f * t.tinggiKotak,
            )
            val ikanP = bezier(land, ctrl, t.tujuan, reel)
            val dibuka = a >= 0.34f
            val rigAkhir = ujungBatang(g, th0 + goyang, len) + gantungDiam
            val pelampungPos = when {
                a < 0.88f -> land + Offset(0f, bob + gigit + 16f * s * seg(a, 0.22f, 0.30f))
                else -> rigAkhir
            }
            val ujungTali = when {
                a < 0.34f -> pelampungPos
                a < 0.88f -> ikanP + Offset(0f, -8f * s)
                else -> campurO(t.tujuan + Offset(0f, -8f * s), rigAkhir, halus(seg(a, 0.88f, 1f)))
            }
            val tipAkhir = if (a >= 0.88f) ujungBatang(g, thT, len) else tip
            val pAlpha = when {
                a < 0.22f -> 1f
                a < 0.30f -> 1f - seg(a, 0.22f, 0.30f)
                a < 0.88f -> 0f
                else -> seg(a, 0.88f, 0.94f)
            }
            val leng = when {
                a < 0.22f -> 0.05f * len * sin(a / 0.22f * PI_F * 2.5f)
                a < 0.34f -> arahLengkung(thT, tip, land) * 0.22f * len * halus(seg(a, 0.22f, 0.34f))
                else -> arahLengkung(thLift, tip, t.tujuan) * campur(0.22f, 0f, halus(seg(a, 0.34f, 0.92f))) * len
            }
            val kendur = when {
                a < 0.34f -> 0.035f * jarak(tipAkhir, ujungTali)
                else -> campur(0.03f, 0f, seg(a, 0.34f, 0.5f)) * jarak(tipAkhir, ujungTali)
            }
            val kailPos = if (dibuka && a < 0.88f) ikanP + Offset(0f, -8f * s) else pelampungPos + Offset(0f, if (a >= 0.88f) kailGantung else kailAir)
            return Keadaan(
                batangPangkal = g,
                batangUjung = tipAkhir,
                batangLengkung = leng,
                tali = SegTali(tipAkhir, ujungTali, kendur),
                pelampung = pelampungPos,
                pelampungAlpha = pAlpha,
                pelampungRot = ayun(ombak, gerak, 8f),
                kail = kailPos,
                kailAlpha = if (a < 0.34f) 0.45f else 1f,
                ikan = ikanP,
                ikanAlpha = seg(a, 0.26f, 0.32f) * (1f - seg(a, 0.86f, 0.96f)),
                ikanRot = campur(-24f, 22f, reel) + sin(a * 60f) * 5f * gerak,
                splash = land,
                splashT = seg(a, 0.26f, 0.52f),
                riak = land,
                riakT = seg(a, 0.22f, 0.60f),
                kilau = t.tujuan,
                kilauT = seg(a, 0.84f, 1f),
            )
        }

        FasePancing.Lepas -> {
            val gigit = if (a < 0.22f) abs(sin(a / 0.22f * PI_F * 2.5f)) * 9f * s else 0f
            val ke = Offset(land.x - g.x, land.y - g.y)
            val n = max(hypot(ke.x, ke.y), 1f)
            val lari = Offset(ke.x / n, ke.y / n)
            val lariJauh = 0.08f * t.lebarKotak * halus(seg(a, 0.22f, 0.40f))
            val posGigit = land + Offset(lari.x * lariJauh, lari.y * lariJauh * 0.4f)
            val u = seg(a, 0.40f, 1f)
            val gelombang = exp(-4.5f * u) * cos(13f * u)
            val th = if (a < 0.22f) th0
            else if (a < 0.40f) campur(th0, thLift, keluar(seg(a, 0.22f, 0.40f)))
            else th0 + (thLift - th0) * gelombang
            val tip = ujungBatang(g, th, len)
            val leng = when {
                a < 0.22f -> 0.05f * len * sin(a / 0.22f * PI_F * 2.5f)
                a < 0.40f -> arahLengkung(thLift, tip, land) * 0.26f * len * halus(seg(a, 0.22f, 0.40f))
                else -> arahLengkung(thLift, ujungBatang(g, thLift, len), land) * 0.26f * len * gelombang
            }
            val putus = a >= 0.40f
            val hanyut = Offset(lari.x * 0.03f * t.lebarKotak * u, 0.02f * t.tinggiKotak * u)
            val bobberPos = if (!putus) {
                land + Offset(lari.x * lariJauh, lari.y * lariJauh * 0.4f + bob + gigit + 16f * s * seg(a, 0.22f, 0.34f))
            } else {
                posGigit + hanyut + Offset(0f, 16f * s + 6f * s * u)
            }
            val bAlpha = when {
                a < 0.22f -> 1f
                !putus -> campur(1f, 0.55f, seg(a, 0.22f, 0.40f))
                else -> 0.55f * (1f - seg(a, 0.40f, 0.95f))
            }
            val ujungAtas = if (!putus) bobberPos else {
                val pulang = keluar(seg(a, 0.40f, 0.62f))
                val rest = tip + Offset(0f, 0.34f * len)
                campurO(posGigit + Offset(0f, 16f * s), rest, pulang) +
                    Offset(sin(u * 20f) * exp(-5f * u) * 0.06f * len * gerak, 0f)
            }
            val kendur = if (!putus) campur(0.035f, 0f, seg(a, 0.22f, 0.40f)) * jarak(tip, ujungAtas) else (0.04f + 0.12f * sin(PI_F * seg(a, 0.40f, 0.8f))) * jarak(tip, ujungAtas)
            val ikanA = seg(a, 0.26f, 0.34f) * (1f - seg(a, 0.55f, 0.90f))
            val ikanP = posGigit + Offset(
                lari.x * 0.10f * t.lebarKotak * seg(a, 0.40f, 0.9f),
                lari.y * 0.04f * t.lebarKotak * seg(a, 0.40f, 0.9f) - 14f * s * sin(PI_F * seg(a, 0.26f, 0.5f)),
            )
            return Keadaan(
                batangPangkal = g,
                batangUjung = tip,
                batangLengkung = leng,
                tali = SegTali(tip, ujungAtas, kendur),
                tali2 = if (putus) SegTali(bobberPos + Offset(0f, -22f * s), bobberPos, 6f * s, bAlpha) else null,
                pelampung = if (a < 0.22f || bAlpha > 0.01f) bobberPos else null,
                pelampungAlpha = bAlpha,
                pelampungRot = ayun(ombak, gerak, 8f) + (if (putus) 40f * u else 0f),
                kail = bobberPos + Offset(0f, kailAir),
                kailAlpha = bAlpha * 0.8f,
                ikan = ikanP,
                ikanAlpha = ikanA,
                ikanRot = -8f + 26f * seg(a, 0.40f, 0.9f),
                ledakan = if (putus) posGigit + Offset(0f, 8f * s) else null,
                ledakanT = seg(a, 0.40f, 0.80f),
                riak = land,
                riakT = seg(a, 0.18f, 0.55f),
                riakBahaya = true,
            )
        }
    }
}

// ---------------------------------------------------------------------------------------
// Memancing di lubang es: tali diturunkan, jigging, lalu ikan ditarik keluar lubang.
// ---------------------------------------------------------------------------------------
private fun keadaanEs(
    fase: FasePancing,
    a: Float,
    t: TitikPx,
    ombak: Float,
    gerak: Float,
): Keadaan {
    val g = t.pangkal
    val len = t.panjang
    val s = t.s
    val lubang = t.target
    val th0 = sudut(t.ujung - g)
    val thOver = th0 - 0.16f
    val thLift = sudut(Offset(-0.25f, -1f))
    val dasarLubang = lubang + Offset(0f, 10f * s)
    val gantung = Offset(-4f * s, 95f * s)

    when (fase) {
        FasePancing.Diam -> {
            val tip = ujungBatang(g, th0, len)
            val h = tip + gantung + Offset(ayun(ombak, gerak, 2f * s), 0f)
            return Keadaan(
                batangPangkal = g, batangUjung = tip, batangLengkung = 0.04f * len,
                tali = SegTali(tip, h, 0.02f * jarak(tip, h)),
                kail = h,
            )
        }

        FasePancing.Lempar -> {
            val th = if (a < 0.75f) campur(th0, thOver, halus(seg(a, 0f, 0.25f)))
            else campur(thOver, th0, halus(seg(a, 0.75f, 1f)))
            val jig = if (a > 0.62f && a < 0.96f) sin((a - 0.62f) * PI_F * 9f) * 0.03f * gerak else 0f
            val tip = ujungBatang(g, th + jig, len)
            val tt = halus(seg(a, 0.20f, 0.62f))
            val h = campurO(tip + gantung, dasarLubang, tt) +
                Offset(0f, if (a > 0.62f) sin((a - 0.62f) * PI_F * 9f) * 3f * s * gerak else 0f)
            return Keadaan(
                batangPangkal = g, batangUjung = tip, batangLengkung = 0.05f * len * sin(PI_F * seg(a, 0f, 0.3f)),
                tali = SegTali(tip, h, (1f - tt) * 0.02f * jarak(tip, h) + 0.03f * tt * jarak(tip, h)),
                kail = h,
                kailAlpha = campur(1f, 0.45f, seg(a, 0.58f, 0.68f)),
                riak = if (a > 0.60f) lubang else null,
                riakT = seg(a, 0.60f, 1f),
                serpihan = if (a > 0.60f) lubang else null,
                serpihanT = seg(a, 0.60f, 0.90f),
            )
        }

        FasePancing.Dapat -> {
            val thT = when {
                a < 0.20f -> th0
                a < 0.30f -> campur(th0, thLift, keluar(seg(a, 0.20f, 0.30f)))
                a < 0.62f -> thLift
                else -> campur(thLift, th0, halus(seg(a, 0.62f, 0.90f)))
            }
            val tip = ujungBatang(g, thT, len)
            val reel = halus(seg(a, 0.30f, 0.84f))
            val ctrl = Offset(campur(lubang.x, t.tujuan.x, 0.30f), min(lubang.y, t.tujuan.y) - 0.20f * t.tinggiKotak)
            val ikanP = bezier(lubang, ctrl, t.tujuan, reel)
            val hAkhir = tip + gantung
            val h = when {
                a < 0.30f -> dasarLubang + Offset(0f, if (a < 0.20f) abs(sin(a / 0.2f * PI_F * 2.5f)) * 3f * s else 0f)
                a < 0.88f -> ikanP + Offset(0f, -6f * s)
                else -> campurO(t.tujuan + Offset(0f, -6f * s), hAkhir, halus(seg(a, 0.88f, 1f)))
            }
            val leng = when {
                a < 0.20f -> 0.06f * len * sin(a / 0.20f * PI_F * 2.5f)
                else -> arahLengkung(thLift, tip, lubang) * campur(0.24f, 0f, halus(seg(a, 0.30f, 0.92f))) * len
            }
            return Keadaan(
                batangPangkal = g, batangUjung = tip, batangLengkung = leng,
                tali = SegTali(tip, h, if (a < 0.30f) 0.03f * jarak(tip, h) else 0.01f * jarak(tip, h)),
                kail = h,
                kailAlpha = if (a < 0.30f) 0.45f else 1f,
                ikan = ikanP,
                ikanAlpha = seg(a, 0.28f, 0.34f) * (1f - seg(a, 0.86f, 0.96f)),
                ikanRot = campur(-30f, 18f, reel) + sin(a * 55f) * 6f * gerak,
                ikanSkala = campur(0.35f, 0.60f, keluar(seg(a, 0.30f, 0.60f))),
                riak = lubang, riakT = seg(a, 0.18f, 0.55f),
                serpihan = lubang, serpihanT = seg(a, 0.28f, 0.60f),
                kilau = t.tujuan, kilauT = seg(a, 0.84f, 1f),
            )
        }

        FasePancing.Lepas -> {
            val u = seg(a, 0.34f, 1f)
            val gelombang = exp(-4.5f * u) * cos(12f * u)
            val th = when {
                a < 0.20f -> th0
                a < 0.34f -> campur(th0, thLift, keluar(seg(a, 0.20f, 0.34f)))
                else -> th0 + (thLift - th0) * gelombang
            }
            val tip = ujungBatang(g, th, len)
            val hAkhir = tip + gantung
            val h = when {
                a < 0.34f -> dasarLubang + Offset(0f, if (a < 0.2f) abs(sin(a / 0.2f * PI_F * 2.5f)) * 3f * s else -4f * s)
                else -> campurO(dasarLubang, hAkhir, keluar(seg(a, 0.34f, 0.55f)))
            }
            val leng = when {
                a < 0.20f -> 0.06f * len * sin(a / 0.20f * PI_F * 2.5f)
                a < 0.34f -> arahLengkung(thLift, tip, lubang) * 0.24f * len * halus(seg(a, 0.20f, 0.34f))
                else -> arahLengkung(thLift, ujungBatang(g, thLift, len), lubang) * 0.24f * len * gelombang
            }
            val menyelam = seg(a, 0.40f, 0.85f)
            return Keadaan(
                batangPangkal = g, batangUjung = tip, batangLengkung = leng,
                tali = SegTali(tip, h, if (a < 0.34f) 0.02f * jarak(tip, h) else 0.10f * sin(PI_F * seg(a, 0.34f, 0.70f)) * jarak(tip, h) + 0.01f * jarak(tip, h)),
                kail = h,
                kailUmpan = a < 0.34f,
                kailAlpha = if (a < 0.34f) 0.6f else 1f,
                ikan = lubang + Offset(-30f * s * menyelam, -8f * s + 12f * s * menyelam),
                ikanAlpha = seg(a, 0.24f, 0.32f) * (1f - seg(a, 0.55f, 0.85f)),
                ikanRot = -10f + 30f * menyelam,
                ikanSkala = campur(0.50f, 0.30f, menyelam),
                riak = lubang, riakT = seg(a, 0.18f, 0.55f), riakBahaya = true,
                serpihan = lubang, serpihanT = seg(a, 0.24f, 0.55f),
            )
        }
    }
}

// ---------------------------------------------------------------------------------------
// Penyelam dengan speargun: membidik, menembak, tali penarik menarik tombak kembali.
// ---------------------------------------------------------------------------------------
private fun keadaanSelam(
    fase: FasePancing,
    a: Float,
    berhasil: Boolean,
    t: TitikPx,
    ombak: Float,
    gerak: Float,
): Keadaan {
    val s = t.s
    val moncong = t.ujung
    val panjangTombak = 95f * s
    val p0 = moncong + Offset(50f * s, 0f)           // ujung tombak saat terpasang di senapan
    val f = t.target
    val meleset = f + Offset(160f * s, 38f * s)
    val e = if (berhasil) f else meleset
    val aman = Offset(moncong.x - 14f * s, moncong.y + 4f * s)

    fun arah(dari: Offset, ke: Offset): Offset {
        val n = max(jarak(dari, ke), 1f)
        return Offset((ke.x - dari.x) / n, (ke.y - dari.y) / n)
    }

    fun tombak(ujungT: Offset, tujuan: Offset): Pair<Offset, Offset> {
        val d = arah(p0, tujuan)
        return Pair(ujungT - Offset(d.x * panjangTombak, d.y * panjangTombak), ujungT)
    }

    when (fase) {
        FasePancing.Diam -> {
            val (ekor, kepala) = tombak(p0, f)
            return Keadaan(
                tombakMulai = ekor, tombakUjung = kepala,
                tali = SegTali(aman, ekor, 6f * s),
            )
        }

        FasePancing.Lempar -> {
            val q = seg(a, 0.46f, 0.72f).pow(1.5f)
            val kepala = campurO(p0, e, q)
            val (ekor, _) = tombak(kepala, campurO(f, e, q))
            val renang = halus(seg(a, 0f, 0.40f))
            val ikanBebas = campurO(t.aux, f, renang) + Offset(0f, sin(a * 18f) * 3f * s * gerak)
            val menyambar = seg(a, 0.58f, 0.90f)
            val posIkan = if (berhasil) {
                if (q >= 1f) kepala else ikanBebas
            } else {
                ikanBebas + Offset(190f * s * halus(menyambar), -90f * s * halus(menyambar))
            }
            val alpha = seg(a, 0f, 0.12f) * (if (berhasil) 1f else 1f - seg(a, 0.70f, 0.95f))
            val mundur = if (a in 0.46f..0.62f) 3f * s * gerak else 0f
            return Keadaan(
                tombakMulai = ekor, tombakUjung = kepala,
                tali = SegTali(aman, ekor, campur(6f, 2f, q) * s),
                garisBidikAlpha = 0.26f * seg(a, 0.12f, 0.30f) * (1f - seg(a, 0.44f, 0.50f)),
                garisBidikDari = moncong, garisBidikKe = ikanBebas,
                jejakMulai = moncong, jejakSelesai = kepala, jejakT = if (q > 0.02f) q else 0f,
                ikan = posIkan, ikanAlpha = alpha, ikanBalik = berhasil || a < 0.62f,
                ikanRot = sin(a * 30f) * 4f * gerak,
                kilau = if (berhasil) f else null, kilauT = if (berhasil) seg(a, 0.72f, 0.98f) else 0f,
                getarKamera = mundur,
            )
        }

        FasePancing.Dapat -> {
            val u = halus(seg(a, 0.12f, 0.82f))
            val kepala = campurO(f, p0, u)
            val (ekor, _) = tombak(kepala, f)
            val gerakIkan = sin(a * 40f) * 10f * (1f - u) * gerak
            return Keadaan(
                tombakMulai = ekor, tombakUjung = kepala,
                tali = SegTali(aman, ekor, campur(2f, 6f, u) * s),
                jejakMulai = moncong, jejakSelesai = kepala, jejakT = 0f,
                ikan = kepala, ikanAlpha = 1f - seg(a, 0.80f, 0.95f), ikanBalik = true,
                ikanRot = gerakIkan,
                kilau = kepala, kilauT = seg(a, 0.78f, 1f),
            )
        }

        FasePancing.Lepas -> {
            val u = halus(seg(a, 0.15f, 0.85f))
            val kepala = campurO(meleset, p0, u)
            val (ekor, _) = tombak(kepala, campurO(meleset, f, u))
            return Keadaan(
                tombakMulai = ekor, tombakUjung = kepala,
                tali = SegTali(aman, ekor, campur(2f, 6f, u) * s),
            )
        }
    }
}

// ---------------------------------------------------------------------------------------
// Jala lempar dari perahu cadik: jala dilempar berputar, mengembang, lalu ditarik menutup.
// ---------------------------------------------------------------------------------------
private fun keadaanJala(
    fase: FasePancing,
    a: Float,
    t: TitikPx,
    ombak: Float,
    gerak: Float,
): Keadaan {
    val s = t.s
    val tangan = t.pangkal
    val genggam = tangan + Offset(14f * s, 14f * s)
    val n = t.target
    val tepi = t.aux
    val dek = t.tujuan
    val lebarBuka = 190f * s
    val lebarGulung = 50f * s
    val bob = ayun(ombak, gerak, 2.5f * s)

    fun talinya(p: Offset, kendur: Float) = SegTali(tangan, p, kendur, 0.9f, 1.3f)

    when (fase) {
        FasePancing.Diam -> return Keadaan(
            alat = genggam, alatLebar = lebarGulung, alatSkalaY = 0.95f, alatRot = -20f,
        )

        FasePancing.Lempar -> {
            val awal = genggam + Offset(-18f * s, -6f * s)
            val tb = seg(a, 0.15f, 0.58f)
            val p = when {
                a < 0.15f -> campurO(genggam, awal, halus(seg(a, 0f, 0.15f)))
                a < 0.58f -> busur(awal, n, keluar(tb), 0.33f * abs(n.x - awal.x) + 0.05f * t.tinggiKotak)
                else -> n + Offset(0f, bob)
            }
            val lebar = when {
                a < 0.15f -> campur(lebarGulung, 58f * s, halus(seg(a, 0f, 0.15f)))
                else -> campur(58f * s, lebarBuka, keluar(seg(a, 0.15f, 0.50f)))
            }
            val skalaY = campur(0.95f, 0.40f, halus(seg(a, 0.15f, 0.55f)))
            return Keadaan(
                alat = p, alatLebar = lebar, alatSkalaY = skalaY,
                alatRot = campur(-20f, 0f, seg(a, 0.15f, 0.5f)),
                alatSpin = 540f * keluar(seg(a, 0.15f, 0.58f)) * gerak,
                alatAlpha = campur(1f, 0.85f, seg(a, 0.58f, 1f)),
                tali = talinya(p, (if (a < 0.58f) 0.08f else 0.035f) * jarak(tangan, p)),
                splash = if (a >= 0.58f) n else null, splashT = seg(a, 0.58f, 0.90f),
                riak = if (a >= 0.58f) n else null, riakT = seg(a, 0.58f, 1f),
            )
        }

        FasePancing.Dapat -> {
            val haul = halus(seg(a, 0.28f, 0.80f))
            val ctrl = Offset(campur(n.x, dek.x, 0.5f), min(n.y, dek.y) - 0.12f * t.tinggiKotak)
            val naik = bezier(n, ctrl, dek, haul)
            val kembali = halus(seg(a, 0.84f, 1f))
            val p = when {
                a < 0.28f -> n + Offset(0f, bob)
                a < 0.84f -> naik
                else -> campurO(dek, genggam, kembali)
            }
            val tutup = halus(seg(a, 0.28f, 0.55f))
            val lebar = if (a < 0.84f) campur(lebarBuka, 80f * s, tutup) else campur(80f * s, lebarGulung, kembali)
            val skalaY = if (a < 0.84f) campur(0.40f, 0.90f, tutup) else campur(0.90f, 0.95f, kembali)
            val masuk = seg(a, 0.10f, 0.30f)
            val ikanPos = if (a < 0.30f) n + Offset(-70f * s * (1f - masuk), 6f * s * (1f - masuk)) else p
            return Keadaan(
                alat = p, alatLebar = lebar, alatSkalaY = skalaY,
                alatRot = campur(0f, -20f, kembali),
                alatAlpha = campur(0.85f, 1f, seg(a, 0.2f, 0.4f)),
                tali = talinya(p, campur(0.035f, 0f, seg(a, 0.28f, 0.5f)) * jarak(tangan, p)),
                ikan = ikanPos,
                ikanAlpha = seg(a, 0.08f, 0.16f) * (1f - seg(a, 0.84f, 0.94f)),
                ikanSkala = campur(0.70f, 0.45f, seg(a, 0.2f, 0.5f)),
                ikanRot = -8f + sin(a * 30f) * 5f * gerak,
                splash = n, splashT = seg(a, 0.28f, 0.50f),
                riak = n, riakT = seg(a, 0.24f, 0.60f),
                kilau = dek, kilauT = seg(a, 0.84f, 1f),
            )
        }

        FasePancing.Lepas -> {
            val haul = halus(seg(a, 0.20f, 0.88f))
            val ctrl = Offset(campur(n.x, tepi.x, 0.6f), min(n.y, tepi.y) - 0.10f * t.tinggiKotak)
            val p = if (a < 0.20f) n + Offset(0f, bob) else bezier(n, ctrl, genggam, haul)
            val tutup = halus(seg(a, 0.20f, 0.55f))
            val lebar = campur(lebarBuka, lebarGulung, halus(seg(a, 0.20f, 0.88f)))
            val skalaY = campur(0.40f, 0.95f, tutup)
            val kabur = halus(seg(a, 0.25f, 0.85f))
            return Keadaan(
                alat = p, alatLebar = lebar, alatSkalaY = skalaY,
                alatRot = campur(0f, -20f, seg(a, 0.5f, 0.88f)),
                alatAlpha = campur(0.85f, 1f, seg(a, 0.2f, 0.4f)),
                tali = talinya(p, 0.02f * jarak(tangan, p)),
                ikan = n + Offset(30f * s + 170f * s * kabur, -8f * s - 36f * s * kabur),
                ikanAlpha = seg(a, 0.14f, 0.24f) * (1f - seg(a, 0.50f, 0.85f)) * 0.9f,
                ikanRot = -6f + 20f * kabur,
                riak = n, riakT = seg(a, 0.2f, 0.6f), riakBahaya = true,
            )
        }
    }
}

// ---------------------------------------------------------------------------------------
// Bubu: tali ditarik dari atas rel kapal, bubu naik dari dasar, diangkat ke dek.
// ---------------------------------------------------------------------------------------
private fun keadaanBubu(
    fase: FasePancing,
    a: Float,
    t: TitikPx,
    ombak: Float,
    gerak: Float,
): Keadaan {
    val s = t.s
    val tangan = t.pangkal
    val rel = t.ujung
    val dasar = t.target
    val permukaan = t.aux
    val dek = t.tujuan
    val lebarBubu = 100f * s
    val tinggiBubu = lebarBubu * 0.806f
    val tengah = Offset(dasar.x + 10f * s, permukaan.y + 40f * s)
    val pecah = Offset(permukaan.x + 8f * s, permukaan.y + 6f * s)
    val goyang = ayun(ombak, gerak, 2f * s)

    fun taliTangan() = SegTali(tangan, rel, 3f * s, 0.9f, 1.3f)
    fun atasBubu(p: Offset) = p + Offset(0f, -tinggiBubu * 0.5f)

    when (fase) {
        FasePancing.Diam -> {
            val pelampung = permukaan + Offset(goyang, 0f)
            return Keadaan(
                alat = dasar, alatLebar = lebarBubu, alatAlpha = 0.92f,
                tali = SegTali(rel, pelampung, 0.10f * jarak(rel, pelampung), 0.9f, 1.3f),
                tali2 = taliTangan(),
                tali3 = SegTali(pelampung, atasBubu(dasar), 0f, 0.7f, 1.1f),
                pelampung = pelampung,
                pelampungRot = ayun(ombak, gerak, 6f),
            )
        }

        FasePancing.Lempar -> {
            val naik = halus(seg(a, 0.15f, 0.62f))
            val getar = sin(a * 60f) * 2f * s * gerak * (1f - seg(a, 0.7f, 1f))
            val p = campurO(dasar, tengah, naik) + Offset(getar, 0f)
            val hilang = seg(a, 0.20f, 0.40f)
            val pelampung = permukaan + Offset(goyang, 14f * s * hilang)
            val ujungTali = campurO(pelampung, atasBubu(p), halus(seg(a, 0.25f, 0.45f)))
            return Keadaan(
                alat = p, alatLebar = lebarBubu, alatAlpha = campur(0.92f, 0.85f, naik),
                tali = SegTali(rel, ujungTali, campur(0.10f, 0.01f, halus(seg(a, 0f, 0.25f))) * jarak(rel, ujungTali), 0.9f, 1.3f),
                tali2 = taliTangan(),
                tali3 = SegTali(pelampung, atasBubu(p), 0f, 0.7f * (1f - hilang), 1.1f),
                pelampung = pelampung, pelampungAlpha = 1f - hilang,
                pelampungRot = ayun(ombak, gerak, 6f),
            )
        }

        FasePancing.Dapat -> {
            val pEnd = halus(seg(a, 0.30f, 0.80f))
            val ctrl = Offset(campur(pecah.x, dek.x, 0.4f) - 6f * s, min(pecah.y, dek.y) - 0.18f * t.tinggiKotak)
            val p = when {
                a < 0.30f -> campurO(tengah, pecah, halus(seg(a, 0f, 0.30f)))
                else -> bezier(pecah, ctrl, dek, pEnd)
            }
            val muncul = seg(a, 0.82f, 0.94f)
            val ikanSkala = if (a < 0.82f) 0.30f else campur(0.30f, 0.80f, halus(muncul))
            val ikanP = if (a < 0.82f) p else p + Offset(-6f * s * muncul, -30f * s * halus(muncul))
            return Keadaan(
                alat = p, alatLebar = lebarBubu,
                alatRot = if (a < 0.30f) 0f else campur(0f, -12f, pEnd),
                alatAlpha = campur(0.85f, 1f, seg(a, 0.2f, 0.34f)),
                tali = SegTali(rel, atasBubu(p), campur(0.02f, 0f, seg(a, 0f, 0.3f)) * jarak(rel, atasBubu(p)), 0.9f, 1.3f),
                tali2 = taliTangan(),
                ikan = ikanP, ikanAlpha = seg(a, 0.10f, 0.22f) * (1f - seg(a, 0.92f, 1f)),
                ikanSkala = ikanSkala, ikanRot = sin(a * 30f) * 6f * gerak,
                splash = pecah, splashT = seg(a, 0.26f, 0.52f),
                riak = pecah, riakT = seg(a, 0.24f, 0.60f),
                kilau = dek, kilauT = seg(a, 0.84f, 1f),
            )
        }

        FasePancing.Lepas -> {
            val naik = halus(seg(a, 0f, 0.30f))
            val ctrl = Offset(campur(pecah.x, dek.x, 0.4f) - 6f * s, min(pecah.y, dek.y) - 0.18f * t.tinggiKotak)
            val p = when {
                a < 0.30f -> campurO(tengah, pecah, naik)
                else -> bezier(pecah, ctrl, dek, halus(seg(a, 0.38f, 0.88f)))
            }
            val kabur = halus(seg(a, 0.22f, 0.80f))
            return Keadaan(
                alat = p, alatLebar = lebarBubu,
                alatRot = campur(0f, -12f, halus(seg(a, 0.38f, 0.88f))),
                alatAlpha = campur(0.85f, 1f, seg(a, 0.2f, 0.34f)),
                tali = SegTali(rel, atasBubu(p), 0.01f * jarak(rel, atasBubu(p)), 0.9f, 1.3f),
                tali2 = taliTangan(),
                ikan = Offset(pecah.x - 150f * s * kabur, pecah.y + 20f * s - 40f * s * kabur),
                ikanAlpha = seg(a, 0.20f, 0.30f) * (1f - seg(a, 0.55f, 0.85f)),
                ikanSkala = campur(0.30f, 0.70f, seg(a, 0.2f, 0.4f)),
                ikanRot = 10f - 20f * kabur,
                riak = pecah, riakT = seg(a, 0.24f, 0.60f), riakBahaya = true,
                splash = pecah, splashT = seg(a, 0.26f, 0.52f),
            )
        }
    }
}

// ---------------------------------------------------------------------------------------
// Kapal pukat badai: pukat diturunkan dari derek buritan, dihela naik, diayun ke dek.
// ---------------------------------------------------------------------------------------
private fun keadaanPukat(
    fase: FasePancing,
    a: Float,
    t: TitikPx,
    ombak: Float,
    gerak: Float,
): Keadaan {
    val s = t.s
    val derek = t.ujung
    val gantung = t.pangkal
    val air = t.target
    val dek = t.tujuan
    val lebarGulung = 80f * s
    val lebarBuka = 150f * s
    val tinggiNet = { lebar: Float -> lebar * 1.10f }
    val goyang = ayun(ombak, gerak, 5f * s)
    val naikTitik = derek + Offset(-2f * s, 135f * s)
    val ayunDek = Offset(dek.x - 85f * s, dek.y - 35f * s)

    fun atas(p: Offset, lebar: Float) = p + Offset(0f, -tinggiNet(lebar) * 0.45f)
    fun talinya(p: Offset, lebar: Float, kendur: Float) = SegTali(derek, atas(p, lebar), kendur, 1f, 1.3f)

    when (fase) {
        FasePancing.Diam -> {
            val p = gantung + Offset(goyang, 0f)
            return Keadaan(
                alat = p, alatLebar = lebarGulung, alatRot = ayun(ombak, gerak, 3f),
                tali = talinya(p, lebarGulung, 0f),
            )
        }

        FasePancing.Lempar -> {
            val keluarX = -22f * s * halus(seg(a, 0f, 0.15f))
            val turun = halus(seg(a, 0.12f, 0.55f))
            val sinking = halus(seg(a, 0.55f, 1f))
            val awal = gantung + Offset(keluarX, 0f)
            val p = if (a < 0.55f) campurO(awal, air, turun)
            else air + Offset(-10f * s * sinking, 20f * s * sinking)
            val lebar = campur(lebarGulung, lebarBuka, halus(seg(a, 0.2f, 0.55f)))
            return Keadaan(
                alat = p, alatLebar = lebar, alatRot = campur(ayun(ombak, gerak, 3f), 0f, turun),
                alatAlpha = campur(1f, 0.78f, sinking),
                tali = talinya(p, lebar, 0.03f * jarak(derek, p)),
                splash = if (a >= 0.5f) air + Offset(0f, -18f * s) else null, splashT = seg(a, 0.50f, 0.85f),
                riak = if (a >= 0.5f) air else null, riakT = seg(a, 0.50f, 1f),
            )
        }

        FasePancing.Dapat -> {
            val dasar = air + Offset(-10f * s, 20f * s)
            val hela = halus(seg(a, 0.12f, 0.62f))
            val ayunT = halus(seg(a, 0.62f, 0.88f))
            val kembali = halus(seg(a, 0.90f, 1f))
            val ctrl = Offset(campur(naikTitik.x, ayunDek.x, 0.5f) + 30f * s, naikTitik.y + 10f * s)
            val p = when {
                a < 0.62f -> campurO(dasar, naikTitik, hela)
                a < 0.90f -> bezier(naikTitik, ctrl, ayunDek, ayunT)
                else -> campurO(ayunDek, gantung, kembali)
            }
            val lebar = when {
                a < 0.62f -> campur(lebarBuka, 190f * s, hela)
                a < 0.90f -> 190f * s
                else -> campur(190f * s, lebarGulung, kembali)
            }
            return Keadaan(
                alat = p, alatLebar = lebar, alatRot = campur(0f, 6f, ayunT) * (1f - kembali),
                alatAlpha = campur(0.78f, 1f, seg(a, 0.12f, 0.3f)),
                tali = talinya(p, lebar, campur(0.03f, 0f, seg(a, 0.12f, 0.3f)) * jarak(derek, p)),
                ikan = p + Offset(0f, 14f * s), ikanAlpha = seg(a, 0.24f, 0.36f) * (1f - seg(a, 0.90f, 0.97f)),
                ikanSkala = campur(0.40f, 0.60f, seg(a, 0.62f, 0.9f)), ikanRot = sin(a * 24f) * 6f * gerak,
                splash = dasar + Offset(0f, -20f * s), splashT = seg(a, 0.14f, 0.45f),
                riak = dasar, riakT = seg(a, 0.12f, 0.5f),
                kilau = ayunDek + Offset(0f, 20f * s), kilauT = seg(a, 0.88f, 1f),
                getarKamera = if (a in 0.12f..0.30f) sin(a * 90f) * 1.5f * s * gerak else 0f,
            )
        }

        FasePancing.Lepas -> {
            val dasar = air + Offset(-10f * s, 20f * s)
            val hela = halus(seg(a, 0.12f, 0.62f))
            val kembali = halus(seg(a, 0.66f, 1f))
            val p = if (a < 0.62f) campurO(dasar, naikTitik, hela)
            else campurO(naikTitik, gantung, kembali)
            val lebar = if (a < 0.62f) campur(lebarBuka, 170f * s, hela) else campur(170f * s, lebarGulung, kembali)
            val kabur = halus(seg(a, 0.20f, 0.80f))
            return Keadaan(
                alat = p, alatLebar = lebar, alatRot = 0f,
                alatAlpha = campur(0.78f, 1f, seg(a, 0.12f, 0.3f)),
                tali = talinya(p, lebar, 0.02f * jarak(derek, p)),
                ikan = dasar + Offset(-40f * s * kabur, 10f * s + 40f * s * kabur),
                ikanAlpha = seg(a, 0.18f, 0.28f) * (1f - seg(a, 0.55f, 0.85f)) * 0.9f,
                ikanSkala = 0.5f, ikanRot = 14f * kabur,
                riak = dasar, riakT = seg(a, 0.12f, 0.5f), riakBahaya = true,
            )
        }
    }
}

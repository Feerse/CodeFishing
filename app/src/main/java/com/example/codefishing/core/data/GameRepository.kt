package com.example.codefishing.core.data

import android.content.Context
import androidx.room.withTransaction
import com.example.codefishing.core.db.AkurasiRow
import com.example.codefishing.core.db.CodeFishingDatabase
import com.example.codefishing.core.db.HasilProgramEntity
import com.example.codefishing.core.db.HasilRingkasRow
import com.example.codefishing.core.db.HasilTangkapanEntity
import com.example.codefishing.core.db.IkanEntity
import com.example.codefishing.core.db.KoleksiIkanEntity
import com.example.codefishing.core.db.KoleksiLengkapView
import com.example.codefishing.core.db.MisiEntity
import com.example.codefishing.core.db.PemainEntity
import com.example.codefishing.core.db.PengaturanEntity
import com.example.codefishing.core.db.ProgresStageEntity
import com.example.codefishing.core.db.RiwayatJawabanEntity
import com.example.codefishing.core.db.StageEntity
import com.example.codefishing.core.db.StageStatusRow
import com.example.codefishing.core.db.StatistikPemainView
import com.example.codefishing.core.db.TantanganHarianEntity
import com.example.codefishing.core.db.UmpanEntity
import com.example.codefishing.pemula.BaitRules
import com.example.codefishing.pemula.BaitState
import com.example.codefishing.pemula.FishRules
import com.example.codefishing.pemula.LevelRules
import com.example.codefishing.pemula.PilihanKuis
import com.example.codefishing.pemula.SoalKuis
import com.example.codefishing.expert.Scoring
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

data class UmpanUi(val sisa: Int, val maks: Int, val intervalMenit: Int, val sisaMs: Long)

data class QuizData(
    val stage: StageEntity,
    val soal: List<SoalKuis>,
    val skorLulus: Int,
)

data class TangkapanData(
    val ikan: IkanEntity,
    val tier: String,
    val bobot: Double,
    val jumlah: Int,
    val baru: Boolean = false,
    val xpDidapat: Int = 0,
)

data class HasilStageData(
    val stage: StageEntity,
    val benar: Int,
    val total: Int,
    val skor: Int,
    val lulus: Boolean,
    val sempurna: Boolean,
    val xpDidapat: Int,
    val tangkapan: TangkapanData?,
    val bonus: List<TangkapanData>,
    val rekorBaru: Boolean,
    val naikTier: Boolean,
    val stageBerikut: Int?,
)

data class TantanganData(
    val row: TantanganHarianEntity,
    val ikan: IkanEntity,
)

data class TangkapanMasuk(
    val nama: String,
    val bobot: Double,
    val jumlah: Int = 1,
)

data class HasilProgramData(
    val hasil: HasilProgramEntity,
    val tangkapan: List<TangkapanData>,
)

/**
 * Fasad semua proses DFD. Menyimpan state permainan di Room dan
 * menyediakan operasi transaksi untuk layar Compose.
 */
class GameRepository(
    private val context: Context,
    private val db: CodeFishingDatabase,
) {
    /** Hasil attempt terakhir, dipakai layar Hasil Stage tanpa menyimpan state di navigasi. */
    @Volatile
    var hasilStageTerakhir: HasilStageData? = null
        private set

    private val playerDao = db.playerDao()
    private val stageDao = db.stageDao()
    private val contentDao = db.contentDao()
    private val baitDao = db.baitDao()
    private val collectionDao = db.collectionDao()
    private val resultDao = db.resultDao()
    private val settingsDao = db.settingsDao()
    private val historyDao = db.historyDao()
    private val dailyDao = db.dailyDao()

    // ---------- Profil, stage, pengaturan ----------

    fun observePlayer(): Flow<PemainEntity?> = playerDao.observe()
    fun observeStatistik(): Flow<StatistikPemainView?> = playerDao.observeStatistik()
    fun observeStages(): Flow<List<StageStatusRow>> = stageDao.observeStatus()
    fun observeStageList(): Flow<List<StageEntity>> = stageDao.observeSemua()
    fun observeSettings(): Flow<PengaturanEntity?> = settingsDao.observe()
    fun observeAkurasi(): Flow<AkurasiRow> = historyDao.observeAkurasi()

    suspend fun player(): PemainEntity = playerDao.get() ?: PemainEntity()
    suspend fun statistik(): StatistikPemainView? = playerDao.statistik()
    suspend fun stage(nomor: Int): StageEntity? = stageDao.berdasarkanNomor(nomor)
    suspend fun stageById(id: Int): StageEntity? = stageDao.berdasarkanId(id)

    suspend fun stageStatus(id: Int): StageStatusRow? = stageDao.status(id)

    suspend fun simpanPosisi(stageId: Int?, mode: String?) {
        playerDao.simpanPosisi(stageId, mode, System.currentTimeMillis())
    }

    suspend fun mulaiStage(stageId: Int) {
        val p = stageDao.progres(stageId)
        if (p == null) {
            stageDao.simpanProgres(
                ProgresStageEntity(stage_id = stageId, status = "aktif", percobaan = 0),
            )
        } else if (p.status == "terkunci") {
            stageDao.simpanProgres(p.copy(status = "aktif"))
        }
    }

    suspend fun pengaturan(): PengaturanEntity = settingsDao.get() ?: PengaturanEntity()
    suspend fun simpanPengaturan(p: PengaturanEntity) = settingsDao.simpan(p)

    suspend fun tambahXp(n: Int) {
        if (n <= 0) return
        val p = player()
        val xp = p.xp + n
        playerDao.tambahXp(n, LevelRules.levelUntukXp(xp))
    }

    suspend fun tambahUmpanEmas(n: Int) = playerDao.tambahUmpanEmas(n)

    // ---------- Umpan ----------

    fun observeUmpan() = baitDao.observe()

    suspend fun muatUmpan(now: Long = System.currentTimeMillis()): UmpanUi {
        var u = baitDao.get()
        if (u == null) {
            u = UmpanEntity(pemain_id = 1, sisa = 5, maks = 5, interval_menit = 15)
            baitDao.simpan(u)
        }
        val regenMs = u.interval_menit * 60_000L
        val state = BaitRules.pulihkan(
            BaitState(u.sisa, u.regen_at),
            now,
            u.maks,
            regenMs,
        )
        if (state.jumlah != u.sisa || state.sejakMs != u.regen_at) {
            baitDao.set(state.jumlah, state.sejakMs)
        }
        return UmpanUi(
            sisa = state.jumlah,
            maks = u.maks,
            intervalMenit = u.interval_menit,
            sisaMs = BaitRules.sisaMs(BaitState(state.jumlah, state.sejakMs), now, u.maks, regenMs),
        )
    }

    suspend fun pakaiUmpan(now: Long = System.currentTimeMillis()): UmpanUi {
        val ui = muatUmpan(now)
        val u = baitDao.get() ?: return ui
        val regenMs = u.interval_menit * 60_000L
        val state = BaitRules.pakai(BaitState(u.sisa, u.regen_at), now, u.maks, regenMs)
        baitDao.set(state.jumlah, state.sejakMs)
        return UmpanUi(
            sisa = state.jumlah,
            maks = u.maks,
            intervalMenit = u.interval_menit,
            sisaMs = BaitRules.sisaMs(state, now, u.maks, regenMs),
        )
    }

    suspend fun tukarUmpanEmas(now: Long = System.currentTimeMillis()): Boolean {
        val p = player()
        if (p.umpan_emas <= 0) return false
        val ui = muatUmpan(now)
        if (ui.sisa >= ui.maks) return false
        playerDao.pakaiUmpanEmas(1)
        val u = baitDao.get() ?: return false
        val regenMs = u.interval_menit * 60_000L
        val state = BaitRules.pulihkan(BaitState(u.sisa, u.regen_at), now, u.maks, regenMs)
        val baru = BaitState(minOf(u.maks, state.jumlah + 1), state.sejakMs ?: now)
        baitDao.set(baru.jumlah, baru.sejakMs)
        return true
    }

    // ---------- Kuis ----------

    suspend fun materi(stageId: Int) = contentDao.materi(stageId)

    suspend fun quiz(stageId: Int): QuizData? {
        val stage = stageDao.berdasarkanId(stageId) ?: return null
        val rows = contentDao.soalStage(stageId)
        if (rows.isEmpty()) return null
        val soal = rows.map { s ->
            SoalKuis(
                id = s.id,
                teks = s.teks,
                kode = s.kode_template ?: "",
                pembahasan = s.pembahasan,
                poin = s.poin,
                pilihan = contentDao.pilihan(s.id).map { p ->
                    PilihanKuis(p.id, p.teks, p.benar == 1)
                },
            )
        }
        val lulus = if (stage.tipe == "boss") 80 else stage.skor_lulus
        return QuizData(stage, soal, lulus)
    }

    suspend fun catatJawaban(
        soalId: Int,
        pilihanId: Int?,
        benar: Boolean,
        combo: Int,
        now: Long = System.currentTimeMillis(),
    ) {
        historyDao.catat(
            RiwayatJawabanEntity(
                soal_id = soalId,
                pilihan_id = pilihanId,
                benar = if (benar) 1 else 0,
                combo_setelah = combo,
                waktu = now,
            ),
        )
        if (benar) tambahXp(25)
    }

    suspend fun selesaikanStage(
        stageId: Int,
        benar: Int,
        total: Int,
        comboMaks: Int,
        sempurna: Boolean,
        now: Long = System.currentTimeMillis(),
    ): HasilStageData? {
        val stage = stageDao.berdasarkanId(stageId) ?: return null
        val skor = if (total == 0) 0 else (benar * 100) / total
        val lulus = skor >= (if (stage.tipe == "boss") 80 else stage.skor_lulus)
        val lama = stageDao.progres(stageId)
        val rekorBaru = lulus && (lama == null || skor > lama.skor_terbaik)
        val status = if (lulus || lama?.status == "selesai") "selesai" else "aktif"
        stageDao.simpanProgres(
            (lama ?: ProgresStageEntity(stage_id = stageId)).copy(
                status = status,
                skor_terbaik = maxOf(lama?.skor_terbaik ?: 0, skor),
                combo_tertinggi = maxOf(lama?.combo_tertinggi ?: 0, comboMaks),
                percobaan = (lama?.percobaan ?: 0) + 1,
                lulus_pada = if (lulus && lama?.lulus_pada == null) now else lama?.lulus_pada,
            ),
        )
        var xp = 0
        if (lulus && lama?.status != "selesai") xp += stage.xp_reward
        if (rekorBaru && lulus) xp += 10
        tambahXp(xp)

        var tangkapan: TangkapanData? = null
        var naikTier = false
        val target = stage.ikan_target_id?.let { contentDao.ikan(it) }
        if (lulus && target != null) {
            val tier = if (stage.tipe == "boss" && sempurna) {
                val tierNaik = FishRules.naikTier(target.tier)
                val lamaTier = collectionDao.koleksi(target.id)?.tier_didapat
                naikTier = lamaTier == null || FishRules.lebihTinggi(tierNaik, lamaTier)
                tierNaik
            } else target.tier
            tangkapan = beriIkan(target, tier, now)
        }

        val bonus = mutableListOf<TangkapanData>()
        FishRules.bonusCombo(comboMaks).forEach { namaBonus ->
            contentDao.ikanByNama(namaBonus)?.let { b ->
                if (collectionDao.koleksi(b.id) == null) bonus.add(beriIkan(b, b.tier, now))
            }
        }

        val selesai = stageDao.jumlahStageSelesai()
        if (lulus && selesai >= 10) {
            contentDao.ikanByNama("Arwana")?.let { b ->
                if (collectionDao.koleksi(b.id) == null) bonus.add(beriIkan(b, b.tier, now))
            }
        }
        if (lulus && stageDao.jumlahStageSempurna() >= 10) {
            contentDao.ikanByNama("Hiu Merah")?.let { b ->
                if (collectionDao.koleksi(b.id) == null) bonus.add(beriIkan(b, b.tier, now))
            }
        }

        val stageLanjut = if (lulus && stage.nomor < 10) {
            stageDao.berdasarkanNomor(stage.nomor + 1)?.id
        } else {
            null
        }
        simpanPosisi(stageLanjut ?: stageId, "belajar")
        val data = HasilStageData(
            stage = stage,
            benar = benar,
            total = total,
            skor = skor,
            lulus = lulus,
            sempurna = sempurna,
            xpDidapat = xp,
            tangkapan = tangkapan,
            bonus = bonus,
            rekorBaru = rekorBaru,
            naikTier = naikTier,
            stageBerikut = if (lulus) (stage.nomor + 1).takeIf { it <= 10 } else null,
        )
        hasilStageTerakhir = data
        return data
    }

    // ---------- Koleksi dan akuarium ----------

    fun observeKoleksi(tier: String? = null): Flow<List<KoleksiLengkapView>> =
        if (tier == null) collectionDao.observeSemua() else collectionDao.observeTier(tier)

    fun observeAkuarium(): Flow<List<KoleksiLengkapView>> = collectionDao.observeDimiliki()

    suspend fun ikan(id: Int): IkanEntity? = contentDao.ikan(id)
    suspend fun semuaIkan(): List<IkanEntity> = contentDao.semuaIkan()

    suspend fun beriIkan(
        ikan: IkanEntity,
        tier: String,
        now: Long = System.currentTimeMillis(),
        bobotPaksa: Double? = null,
        jumlah: Int = 1,
        xpPerEkor: Boolean = false,
    ): TangkapanData {
        val n = jumlah.coerceAtLeast(1)
        val bobot = bobotPaksa ?: FishRules.bobotAcak(ikan.bobot_kg, Random)
        val lama = collectionDao.koleksi(ikan.id)
        val baru = lama == null
        val tierBaru = if (lama != null && FishRules.lebihTinggi(lama.tier_didapat, tier)) lama.tier_didapat else tier
        val xpDidapat = when {
            xpPerEkor -> ikan.nilai_xp * n
            baru -> ikan.nilai_xp
            else -> 0
        }
        if (baru) {
            collectionDao.simpan(
                KoleksiIkanEntity(
                    ikan_id = ikan.id,
                    jumlah = n,
                    bobot_terbesar = bobot,
                    tier_didapat = tierBaru,
                    xp = xpDidapat,
                    kenyang = 100,
                    diperbarui_pada = now,
                    pertama_didapat = now,
                ),
            )
        } else {
            collectionDao.simpan(
                lama.copy(
                    jumlah = lama.jumlah + n,
                    bobot_terbesar = maxOf(lama.bobot_terbesar, bobot),
                    tier_didapat = tierBaru,
                    xp = if (xpPerEkor) lama.xp + xpDidapat else lama.xp,
                    diperbarui_pada = now,
                ),
            )
        }
        tambahXp(xpDidapat)
        tandaiHarianSelesai(ikan.id, now)
        return TangkapanData(
            ikan = ikan,
            tier = tierBaru,
            bobot = bobot,
            jumlah = (lama?.jumlah ?: 0) + n,
            baru = baru,
            xpDidapat = xpDidapat,
        )
    }

    suspend fun beriMakan(ikanId: Int, now: Long = System.currentTimeMillis()) {
        val k = collectionDao.koleksi(ikanId) ?: return
        val baru = minOf(100, k.kenyang + 25)
        collectionDao.beriMakan(ikanId, baru, now)
    }

    // ---------- Tantangan harian ----------

    suspend fun pastikanHarian(now: Long = System.currentTimeMillis()): TantanganData? {
        val hariIni = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val tanggal = hariIni.toString()
        val ada = dailyDao.get(tanggal)
        if (ada != null) {
            val ikan = contentDao.ikan(ada.target_ikan_id) ?: return null
            return TantanganData(ada, ikan)
        }
        val selesai = stageDao.stageSelesai()
        val semua = contentDao.semuaIkan().filter {
            it.syarat_unlock?.startsWith("Segera hadir") != true
        }
        val calon = semua.filter { it.asal_stage_id != null && it.asal_stage_id in selesai }
            .ifEmpty { semua.filter { it.asal_stage_id == 1 } }
            .ifEmpty { semua.take(1) }
        if (calon.isEmpty()) return null
        val target = calon[Random.nextInt(calon.size)]
        val expires = hariIni.plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val row = TantanganHarianEntity(
            pemain_id = 1,
            tanggal = tanggal,
            target_ikan_id = target.id,
            reward_umpan_emas = 1,
            status = "tersedia",
            expires_at = expires,
        )
        val id = dailyDao.simpan(row)
        return TantanganData(row.copy(id = id), target)
    }

    private suspend fun tandaiHarianSelesai(ikanId: Int, now: Long) {
        val tanggal = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        val t = dailyDao.get(tanggal) ?: return
        if (t.target_ikan_id == ikanId && t.status == "tersedia") {
            dailyDao.setStatus(t.id, "selesai")
        }
    }

    suspend fun klaimHarian(now: Long = System.currentTimeMillis()): Boolean {
        val t = pastikanHarian(now) ?: return false
        if (t.row.status != "selesai") return false
        dailyDao.setStatus(t.row.id, "diklaim")
        tambahUmpanEmas(t.row.reward_umpan_emas)
        return true
    }

    // ---------- Sandbox ----------

    suspend fun misi(): List<MisiEntity> = contentDao.semuaMisi()
    suspend fun misiByNomor(nomor: Int): MisiEntity? = contentDao.misi(nomor)
    fun observeMisi(): Flow<List<MisiEntity>> = contentDao.observeMisi()
    fun observeBintangMisi() = resultDao.observeBintang()
    fun observeRiwayat(): Flow<List<HasilRingkasRow>> = resultDao.observeRiwayat()

    suspend fun simpanHasilProgram(
        misiId: Int?,
        kode: String,
        output: String,
        error: String?,
        ticks: Int,
        status: String,
        tangkapan: List<TangkapanMasuk>,
        now: Long = System.currentTimeMillis(),
    ): HasilProgramData? {
        val misi = misiId?.let { id -> contentDao.semuaMisi().firstOrNull { it.id == id } }
        val sukses = status == "sukses"
        val bintang = if (sukses) Scoring.bintang(ticks, Scoring.parEfektif(misi?.par_tick)) else 0
        val sebelum = misiId?.let { resultDao.bintangTerbaik(it) } ?: 0
        val xp = if (sukses && sebelum == 0) (misi?.xp_reward ?: 0) else 0
        val hasilId = resultDao.simpanHasil(
            HasilProgramEntity(
                pemain_id = 1,
                misi_id = misiId,
                kode = kode,
                output = output,
                error = error,
                ticks = ticks,
                waktu_jalan_ms = 0,
                bintang = bintang,
                xp = xp,
                status = status,
                dibuat_pada = now,
            ),
        )
        val rows = mutableListOf<HasilTangkapanEntity>()
        val data = mutableListOf<TangkapanData>()
        for (t in tangkapan) {
            val ikan = contentDao.ikanByNama(t.nama) ?: continue
            rows.add(HasilTangkapanEntity(hasil_id = hasilId, ikan_id = ikan.id, jumlah = t.jumlah, bobot_kg = t.bobot))
            data.add(beriIkan(ikan, ikan.tier, now, t.bobot, jumlah = t.jumlah, xpPerEkor = true))
        }
        if (rows.isNotEmpty()) resultDao.simpanTangkapan(rows)
        if (xp > 0) tambahXp(xp)
        simpanPosisi(null, "expert")
        val hasil = resultDao.hasil(hasilId) ?: return null
        return HasilProgramData(hasil, data)
    }

    suspend fun hasilProgram(id: Long): HasilProgramData? {
        val h = resultDao.hasil(id) ?: return null
        val rows = resultDao.tangkapan(id)
        val tangkapan = rows.mapNotNull { row ->
            contentDao.ikan(row.ikan_id)?.let {
                val tier = collectionDao.koleksi(it.id)?.tier_didapat ?: it.tier
                TangkapanData(
                    ikan = it,
                    tier = tier,
                    bobot = row.bobot_kg,
                    jumlah = row.jumlah,
                    xpDidapat = it.nilai_xp * row.jumlah,
                )
            }
        }
        return HasilProgramData(h, tangkapan)
    }

    suspend fun bintangTerbaik(misiId: Int): Int = resultDao.bintangTerbaik(misiId) ?: 0

    // ---------- Reset ----------

    suspend fun resetProgres() {
        db.withTransaction {
            stageDao.resetProgres()
            collectionDao.reset()
            resultDao.reset()
            dailyDao.reset()
            historyDao.reset()
            playerDao.resetProfil()
            baitDao.isiPenuh()
            val stages = stageDao.semua()
            stages.forEachIndexed { i, s ->
                stageDao.simpanProgres(
                    ProgresStageEntity(
                        stage_id = s.id,
                        status = if (i == 0) "aktif" else "terkunci",
                    ),
                )
            }
        }
    }

    suspend fun seedIfNeeded(loader: com.example.codefishing.core.db.SeedLoader) {
        loader.seedIfNeeded()
    }
}

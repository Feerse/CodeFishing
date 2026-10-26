package com.example.codefishing.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM pemain WHERE id = 1")
    fun observe(): Flow<PemainEntity?>

    @Query("SELECT * FROM pemain WHERE id = 1")
    suspend fun get(): PemainEntity?

    @Query("SELECT * FROM v_statistik_pemain WHERE id = 1")
    fun observeStatistik(): Flow<StatistikPemainView?>

    @Query("SELECT * FROM v_statistik_pemain WHERE id = 1")
    suspend fun statistik(): StatistikPemainView?

    @Upsert
    suspend fun upsert(p: PemainEntity)

    @Query("UPDATE pemain SET xp = xp + :xp, level = :level WHERE id = 1")
    suspend fun tambahXp(xp: Int, level: Int)

    @Query("UPDATE pemain SET umpan_emas = umpan_emas + :n WHERE id = 1")
    suspend fun tambahUmpanEmas(n: Int)

    @Query("UPDATE pemain SET umpan_emas = MAX(0, umpan_emas - :n) WHERE id = 1")
    suspend fun pakaiUmpanEmas(n: Int)

    @Query("UPDATE pemain SET stage_terakhir_id = :stageId, mode_terakhir = :mode, terakhir_main = :now WHERE id = 1")
    suspend fun simpanPosisi(stageId: Int?, mode: String?, now: Long)

    @Query("UPDATE pemain SET xp = 0, level = 1, umpan_emas = 0, stage_terakhir_id = NULL, mode_terakhir = NULL WHERE id = 1")
    suspend fun resetProfil()
}

@Dao
interface StageDao {
    @Query("SELECT * FROM stage ORDER BY nomor")
    fun observeSemua(): Flow<List<StageEntity>>

    @Query("SELECT * FROM stage ORDER BY nomor")
    suspend fun semua(): List<StageEntity>

    @Query("SELECT * FROM stage WHERE nomor = :nomor")
    suspend fun berdasarkanNomor(nomor: Int): StageEntity?

    @Query("SELECT * FROM stage WHERE id = :id")
    suspend fun berdasarkanId(id: Int): StageEntity?

    @Query(
        """
        SELECT st.id, st.nomor, st.nama, st.zona, st.konsep, st.tipe, st.jumlah_soal,
               st.skor_lulus, st.xp_reward, st.ikan_target_id, st.urutan,
               COALESCE(p.status, 'terkunci') AS status,
               COALESCE(p.skor_terbaik, 0) AS skor_terbaik,
               CASE
                 WHEN st.nomor = 1 THEN 1
                 WHEN p.status = 'selesai' THEN 1
                 WHEN EXISTS (
                   SELECT 1 FROM stage prev
                   JOIN progres_stage pp ON pp.stage_id = prev.id AND pp.pemain_id = 1
                   WHERE prev.nomor = st.nomor - 1 AND pp.status = 'selesai'
                 ) THEN 1
                 ELSE 0
               END AS terbuka
        FROM stage st
        LEFT JOIN progres_stage p ON p.stage_id = st.id AND p.pemain_id = 1
        ORDER BY st.nomor
        """
    )
    fun observeStatus(): Flow<List<StageStatusRow>>

    @Query(
        """
        SELECT st.id, st.nomor, st.nama, st.zona, st.konsep, st.tipe, st.jumlah_soal,
               st.skor_lulus, st.xp_reward, st.ikan_target_id, st.urutan,
               COALESCE(p.status, 'terkunci') AS status,
               COALESCE(p.skor_terbaik, 0) AS skor_terbaik,
               CASE
                 WHEN st.nomor = 1 THEN 1
                 WHEN p.status = 'selesai' THEN 1
                 WHEN EXISTS (
                   SELECT 1 FROM stage prev
                   JOIN progres_stage pp ON pp.stage_id = prev.id AND pp.pemain_id = 1
                   WHERE prev.nomor = st.nomor - 1 AND pp.status = 'selesai'
                 ) THEN 1
                 ELSE 0
               END AS terbuka
        FROM stage st
        LEFT JOIN progres_stage p ON p.stage_id = st.id AND p.pemain_id = 1
        WHERE st.id = :id
        """
    )
    suspend fun status(id: Int): StageStatusRow?

    @Query("SELECT * FROM progres_stage WHERE pemain_id = 1 AND stage_id = :stageId")
    suspend fun progres(stageId: Int): ProgresStageEntity?

    @Upsert
    suspend fun simpanProgres(p: ProgresStageEntity)

    @Query("SELECT COUNT(*) FROM progres_stage WHERE pemain_id = 1 AND status = 'selesai'")
    suspend fun jumlahStageSelesai(): Int

    @Query("SELECT COUNT(*) FROM progres_stage WHERE pemain_id = 1 AND status = 'selesai' AND skor_terbaik = 100")
    suspend fun jumlahStageSempurna(): Int

    @Query("SELECT stage_id FROM progres_stage WHERE pemain_id = 1 AND status = 'selesai' ORDER BY stage_id")
    suspend fun stageSelesai(): List<Int>

    @Query("UPDATE progres_stage SET status = 'terkunci', skor_terbaik = 0, combo_tertinggi = 0, percobaan = 0, lulus_pada = NULL WHERE pemain_id = 1")
    suspend fun resetProgres()
}

@Dao
interface ContentDao {
    @Upsert
    suspend fun insertStages(rows: List<StageEntity>)

    @Upsert
    suspend fun insertIkan(rows: List<IkanEntity>)

    @Upsert
    suspend fun insertMateri(rows: List<MateriEntity>)

    @Upsert
    suspend fun insertSoal(rows: List<SoalEntity>)

    @Upsert
    suspend fun insertPilihan(rows: List<PilihanEntity>)

    @Upsert
    suspend fun insertKomposisi(rows: List<KomposisiKuisEntity>)

    @Upsert
    suspend fun insertMisi(rows: List<MisiEntity>)

    @Query("UPDATE stage SET ikan_target_id = :ikanId WHERE id = :stageId")
    suspend fun setTarget(stageId: Int, ikanId: Int?)

    @Query("SELECT COUNT(*) FROM stage")
    suspend fun jumlahStage(): Int

    @Query("SELECT COUNT(*) FROM ikan")
    suspend fun jumlahIkan(): Int

    @Query("SELECT COUNT(*) FROM soal")
    suspend fun jumlahSoal(): Int

    @Query("SELECT * FROM materi WHERE stage_id = :stageId")
    suspend fun materi(stageId: Int): MateriEntity?

    @Query("SELECT * FROM stage WHERE id = :id")
    suspend fun stage(id: Int): StageEntity?

    @Query("SELECT * FROM ikan ORDER BY urutan")
    fun observeIkan(): Flow<List<IkanEntity>>

    @Query("SELECT * FROM ikan ORDER BY urutan")
    suspend fun semuaIkan(): List<IkanEntity>

    @Query("SELECT * FROM ikan WHERE id = :id")
    suspend fun ikan(id: Int): IkanEntity?

    @Query("SELECT * FROM ikan WHERE nama = :nama COLLATE NOCASE LIMIT 1")
    suspend fun ikanByNama(nama: String): IkanEntity?

    @Query("SELECT * FROM misi ORDER BY urutan")
    fun observeMisi(): Flow<List<MisiEntity>>

    @Query("SELECT * FROM misi WHERE nomor = :nomor")
    suspend fun misi(nomor: Int): MisiEntity?

    @Query("SELECT * FROM misi ORDER BY urutan")
    suspend fun semuaMisi(): List<MisiEntity>

    @Query(
        """
        SELECT soal.* FROM soal
        JOIN komposisi_kuis k ON k.soal_id = soal.id
        WHERE k.stage_id = :stageId
        ORDER BY k.urutan
        """
    )
    suspend fun soalStage(stageId: Int): List<SoalEntity>

    @Query("SELECT * FROM pilihan WHERE soal_id = :soalId ORDER BY urutan")
    suspend fun pilihan(soalId: Int): List<PilihanEntity>

    @Query("SELECT * FROM pilihan WHERE soal_id = :soalId AND benar = 1 LIMIT 1")
    suspend fun pilihanBenar(soalId: Int): PilihanEntity?

    @Query("SELECT * FROM soal WHERE id = :id")
    suspend fun soal(id: Int): SoalEntity?

    @Query("SELECT * FROM pilihan WHERE id = :id")
    suspend fun pilihanById(id: Int): PilihanEntity?
}

@Dao
interface BaitDao {
    @Query("SELECT * FROM umpan WHERE pemain_id = 1")
    fun observe(): Flow<UmpanEntity?>

    @Query("SELECT * FROM umpan WHERE pemain_id = 1")
    suspend fun get(): UmpanEntity?

    @Upsert
    suspend fun simpan(u: UmpanEntity)

    @Query("UPDATE umpan SET sisa = :sisa, regen_at = :regenAt WHERE pemain_id = 1")
    suspend fun set(sisa: Int, regenAt: Long?)

    @Query("UPDATE umpan SET sisa = MAX(0, sisa - 1) WHERE pemain_id = 1")
    suspend fun kurangi()

    @Query("UPDATE umpan SET sisa = sisa + 1, regen_at = NULL WHERE pemain_id = 1")
    suspend fun tambahSatu()

    @Query("UPDATE umpan SET sisa = maks, regen_at = NULL WHERE pemain_id = 1")
    suspend fun isiPenuh()
}

@Dao
interface CollectionDao {
    @Query("SELECT * FROM v_koleksi_lengkap ORDER BY urutan")
    fun observeSemua(): Flow<List<KoleksiLengkapView>>

    @Query(
        """
        SELECT * FROM v_koleksi_lengkap
        WHERE (:tier IS NULL OR tier = :tier)
        ORDER BY CASE WHEN dimiliki = 1 THEN 0 ELSE 1 END, urutan
        """
    )
    fun observeTier(tier: String?): Flow<List<KoleksiLengkapView>>

    @Query("SELECT * FROM v_koleksi_lengkap WHERE ikan_id = :ikanId")
    suspend fun satu(ikanId: Int): KoleksiLengkapView?

    @Query("SELECT * FROM v_koleksi_lengkap WHERE dimiliki = 1 ORDER BY urutan")
    fun observeDimiliki(): Flow<List<KoleksiLengkapView>>

    @Query("SELECT * FROM koleksi_ikan WHERE pemain_id = 1 AND ikan_id = :ikanId")
    suspend fun koleksi(ikanId: Int): KoleksiIkanEntity?

    @Query("SELECT * FROM koleksi_ikan WHERE pemain_id = 1 AND ikan_id = :ikanId")
    fun observeKoleksi(ikanId: Int): Flow<KoleksiIkanEntity?>

    @Upsert
    suspend fun simpan(k: KoleksiIkanEntity)

    @Query("UPDATE koleksi_ikan SET kenyang = :kenyang, terakhir_makan = :now, diperbarui_pada = :now WHERE pemain_id = 1 AND ikan_id = :ikanId")
    suspend fun beriMakan(ikanId: Int, kenyang: Int, now: Long)

    @Query("SELECT COUNT(*) FROM koleksi_ikan WHERE pemain_id = 1")
    suspend fun jumlahDimiliki(): Int

    @Query("DELETE FROM koleksi_ikan WHERE pemain_id = 1")
    suspend fun reset()
}

@Dao
interface ResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanHasil(h: HasilProgramEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanTangkapan(t: List<HasilTangkapanEntity>)

    @Query(
        """
        SELECT m.id, m.judul, m.par_tick,
               COALESCE(MAX(h.bintang), 0) AS bintang,
               CASE WHEN MAX(h.bintang) > 0 THEN 1 ELSE 0 END AS selesai
        FROM misi m
        LEFT JOIN hasil_program h ON h.misi_id = m.id AND h.pemain_id = 1
        GROUP BY m.id
        ORDER BY m.urutan
        """
    )
    fun observeBintang(): Flow<List<MisiBintangRow>>

    @Query("SELECT * FROM hasil_program WHERE id = :id")
    suspend fun hasil(id: Long): HasilProgramEntity?

    @Query("SELECT * FROM hasil_tangkapan WHERE hasil_id = :hasilId")
    suspend fun tangkapan(hasilId: Long): List<HasilTangkapanEntity>

    @Query(
        """
        SELECT h.id, h.misi_id, m.judul AS judul_misi, h.ticks, h.bintang, h.xp,
               h.status, h.dibuat_pada
        FROM hasil_program h LEFT JOIN misi m ON m.id = h.misi_id
        WHERE h.pemain_id = 1 ORDER BY h.dibuat_pada DESC LIMIT 20
        """
    )
    fun observeRiwayat(): Flow<List<HasilRingkasRow>>

    @Query("SELECT MAX(bintang) FROM hasil_program WHERE pemain_id = 1 AND misi_id = :misiId")
    suspend fun bintangTerbaik(misiId: Int): Int?

    @Query("DELETE FROM hasil_program WHERE pemain_id = 1")
    suspend fun reset()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM pengaturan WHERE pemain_id = 1")
    fun observe(): Flow<PengaturanEntity?>

    @Query("SELECT * FROM pengaturan WHERE pemain_id = 1")
    suspend fun get(): PengaturanEntity?

    @Upsert
    suspend fun simpan(p: PengaturanEntity)
}

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun catat(r: RiwayatJawabanEntity)

    @Query("SELECT COUNT(*) AS total, COALESCE(SUM(benar), 0) AS benar FROM riwayat_jawaban WHERE pemain_id = 1")
    fun observeAkurasi(): Flow<AkurasiRow>

    @Query("DELETE FROM riwayat_jawaban WHERE pemain_id = 1")
    suspend fun reset()
}

@Dao
interface DailyDao {
    @Query("SELECT * FROM tantangan_harian WHERE pemain_id = 1 AND tanggal = :tanggal")
    suspend fun get(tanggal: String): TantanganHarianEntity?

    @Query("SELECT * FROM tantangan_harian WHERE pemain_id = 1 AND tanggal = :tanggal")
    fun observe(tanggal: String): Flow<TantanganHarianEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpan(t: TantanganHarianEntity): Long

    @Query("UPDATE tantangan_harian SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: String)

    @Query("SELECT * FROM tantangan_harian WHERE pemain_id = 1 ORDER BY tanggal DESC LIMIT 1")
    suspend fun terakhir(): TantanganHarianEntity?

    @Query("DELETE FROM tantangan_harian WHERE pemain_id = 1")
    suspend fun reset()
}

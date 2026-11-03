package com.example.codefishing.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PemainEntity::class,
        StageEntity::class,
        IkanEntity::class,
        MateriEntity::class,
        SoalEntity::class,
        PilihanEntity::class,
        KomposisiKuisEntity::class,
        ProgresStageEntity::class,
        UmpanEntity::class,
        KoleksiIkanEntity::class,
        IkanAkuariumEntity::class,
        MisiEntity::class,
        HasilProgramEntity::class,
        HasilTangkapanEntity::class,
        PengaturanEntity::class,
        RiwayatJawabanEntity::class,
        TantanganHarianEntity::class,
    ],
    views = [KoleksiLengkapView::class, StatistikPemainView::class],
    version = 2,
    exportSchema = true,
)
abstract class CodeFishingDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun stageDao(): StageDao
    abstract fun contentDao(): ContentDao
    abstract fun baitDao(): BaitDao
    abstract fun collectionDao(): CollectionDao
    abstract fun akuariumDao(): AkuariumDao
    abstract fun resultDao(): ResultDao
    abstract fun settingsDao(): SettingsDao
    abstract fun historyDao(): HistoryDao
    abstract fun dailyDao(): DailyDao

    companion object {
        @Volatile
        private var INSTANCE: CodeFishingDatabase? = null

        /** Versi 2: tabel akuarium per ekor, terpisah dari koleksi per spesies. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `ikan_akuarium` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `pemain_id` INTEGER NOT NULL,
                        `ikan_id` INTEGER NOT NULL,
                        `nama` TEXT,
                        `ditambahkan_pada` INTEGER NOT NULL,
                        FOREIGN KEY(`pemain_id`) REFERENCES `pemain`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`ikan_id`) REFERENCES `ikan`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ikan_akuarium_pemain_id` ON `ikan_akuarium` (`pemain_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ikan_akuarium_ikan_id` ON `ikan_akuarium` (`ikan_id`)")
            }
        }

        fun get(context: Context): CodeFishingDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CodeFishingDatabase::class.java,
                    "codefishing.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
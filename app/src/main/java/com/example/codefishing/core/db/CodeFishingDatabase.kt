package com.example.codefishing.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
        MisiEntity::class,
        HasilProgramEntity::class,
        HasilTangkapanEntity::class,
        PengaturanEntity::class,
        RiwayatJawabanEntity::class,
        TantanganHarianEntity::class,
    ],
    views = [KoleksiLengkapView::class, StatistikPemainView::class],
    version = 1,
    exportSchema = true,
)
abstract class CodeFishingDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun stageDao(): StageDao
    abstract fun contentDao(): ContentDao
    abstract fun baitDao(): BaitDao
    abstract fun collectionDao(): CollectionDao
    abstract fun resultDao(): ResultDao
    abstract fun settingsDao(): SettingsDao
    abstract fun historyDao(): HistoryDao
    abstract fun dailyDao(): DailyDao

    companion object {
        @Volatile
        private var INSTANCE: CodeFishingDatabase? = null

        fun get(context: Context): CodeFishingDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CodeFishingDatabase::class.java,
                    "codefishing.db",
                ).build().also { INSTANCE = it }
            }
    }
}

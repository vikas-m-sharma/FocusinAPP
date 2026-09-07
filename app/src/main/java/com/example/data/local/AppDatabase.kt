package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.DailyStatsDao
import com.example.data.local.dao.FocusSessionDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.dao.TimetableDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.dao.VoiceRecordingDao
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.local.entity.FocusSessionRecordEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TimetableSessionEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.local.entity.VoiceRecordingEntity

@Database(
    entities = [
        SubjectEntity::class,
        TimetableSessionEntity::class,
        FocusSessionRecordEntity::class,
        DailyStatsEntity::class,
        VoiceRecordingEntity::class,
        AchievementEntity::class,
        UserSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun timetableDao(): TimetableDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun voiceRecordingDao(): VoiceRecordingDao
    abstract fun achievementDao(): AchievementDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focusin_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

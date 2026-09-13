package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.DailyStatsDao
import com.example.data.local.dao.FocusSessionDao
import com.example.data.local.dao.LearningDao
import com.example.data.local.dao.NcertDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.dao.TimetableDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.dao.VoiceRecordingDao
import com.example.data.local.dao.MistakeDao
import com.example.data.local.dao.ImportedTestDao
import com.example.data.local.entity.AchievementEntity
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.ChapterProgressEntity
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.local.entity.FocusSessionRecordEntity
import com.example.data.local.entity.LearningResourceEntity
import com.example.data.local.entity.MistakeEntity
import com.example.data.local.entity.NcertProgressEntity
import com.example.data.local.entity.QuestionAttemptEntity
import com.example.data.local.entity.QuestionAttemptRecordEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.QuizAttemptRecordEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TimetableSessionEntity
import com.example.data.local.entity.TopicEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.local.entity.VoiceRecordingEntity
import com.example.data.local.entity.ImportedTestEntity
import com.example.data.local.entity.ImportedQuestionEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SubjectEntity::class,
        TimetableSessionEntity::class,
        FocusSessionRecordEntity::class,
        DailyStatsEntity::class,
        VoiceRecordingEntity::class,
        AchievementEntity::class,
        UserSettingsEntity::class,
        ChapterProgressEntity::class,
        QuestionAttemptRecordEntity::class,
        QuizAttemptRecordEntity::class,
        ChapterEntity::class,
        TopicEntity::class,
        QuestionEntity::class,
        QuestionAttemptEntity::class,
        QuizAttemptEntity::class,
        LearningResourceEntity::class,
        MistakeEntity::class,
        ImportedTestEntity::class,
        ImportedQuestionEntity::class,
        NcertProgressEntity::class
    ],
    version = 15,
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
    abstract fun learningDao(): LearningDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun importedTestDao(): ImportedTestDao
    abstract fun ncertDao(): NcertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `ncert_progress` (
                        `chapterId` TEXT NOT NULL PRIMARY KEY,
                        `bookId` TEXT NOT NULL,
                        `subjectId` TEXT NOT NULL,
                        `classNumber` INTEGER NOT NULL,
                        `lastPageRead` INTEGER NOT NULL DEFAULT 1,
                        `totalPages` INTEGER NOT NULL DEFAULT 1,
                        `progressPercentage` INTEGER NOT NULL DEFAULT 0,
                        `isBookmarked` INTEGER NOT NULL DEFAULT 0,
                        `isCompleted` INTEGER NOT NULL DEFAULT 0,
                        `lastOpenedAt` INTEGER NOT NULL DEFAULT 0,
                        `startedAt` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to questions table
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `sourceExam` TEXT NOT NULL DEFAULT 'NEET_UG'")
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `examYear` INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `paperSession` TEXT NOT NULL DEFAULT 'MAIN'")
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `syllabusStatus` TEXT NOT NULL DEFAULT 'CURRENT'")

                // Add Room indices on questions
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_chapterId` ON `questions` (`chapterId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_subjectId` ON `questions` (`subjectId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_examYear` ON `questions` (`examYear`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_sourceExam` ON `questions` (`sourceExam`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_difficulty` ON `questions` (`difficulty`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_isOfficialPYQ` ON `questions` (`isOfficialPYQ`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_chapterId_examYear` ON `questions` (`chapterId`, `examYear`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_chapterId_sourceExam` ON `questions` (`chapterId`, `sourceExam`)")

                // Add sourceExam and examYear to mistake_entries table
                db.execSQL("ALTER TABLE `mistake_entries` ADD COLUMN `sourceExam` TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE `mistake_entries` ADD COLUMN `examYear` INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `sourceVerificationStatus` TEXT NOT NULL DEFAULT 'UNVERIFIED'")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_sourceVerificationStatus` ON `questions` (`sourceVerificationStatus`)")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `historicalPaperId` TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `originalQuestionNumber` INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `sourceReference` TEXT DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_historicalPaperId` ON `questions` (`historicalPaperId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focusin_database"
                )
                    .addMigrations(MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

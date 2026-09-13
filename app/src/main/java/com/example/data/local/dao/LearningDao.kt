package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.ChapterProgressEntity
import com.example.data.local.entity.LearningResourceEntity
import com.example.data.local.entity.QuestionAttemptEntity
import com.example.data.local.entity.QuestionAttemptRecordEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.QuizAttemptRecordEntity
import com.example.data.local.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {

    // Chapter Progress & Chapters
    @Query("SELECT * FROM chapter_progress")
    fun getAllChapterProgress(): Flow<List<ChapterProgressEntity>>

    @Query("SELECT * FROM chapter_progress WHERE chapterId = :chapterId")
    suspend fun getChapterProgressById(chapterId: String): ChapterProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChapterProgress(progress: ChapterProgressEntity)

    @Query("SELECT * FROM chapters")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    fun getChapterFlow(chapterId: String): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getChaptersBySubject(subjectId: String): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    // Topics
    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getTopicsForChapter(chapterId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    suspend fun getTopicsDirect(chapterId: String): List<TopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Query("UPDATE topics SET status = :status, lastUpdated = :timestamp WHERE id = :topicId")
    suspend fun updateTopicStatus(topicId: String, status: String, timestamp: Long = System.currentTimeMillis())

    // Resources
    @Query("SELECT * FROM learning_resources WHERE chapterId = :chapterId")
    fun getResourcesForChapter(chapterId: String): Flow<List<LearningResourceEntity>>

    @Query("SELECT * FROM learning_resources WHERE isBookmarked = 1")
    fun getFeaturedResources(): Flow<List<LearningResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<LearningResourceEntity>)

    // Questions
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId ORDER BY examYear DESC, id ASC")
    fun getQuestionsForChapter(chapterId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId ORDER BY examYear DESC, id ASC")
    suspend fun getQuestionsForChapterSync(chapterId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId AND sourceExam = :sourceExam ORDER BY examYear DESC, id ASC")
    fun getQuestionsForChapterAndExam(chapterId: String, sourceExam: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId AND examYear = :year ORDER BY id ASC")
    fun getQuestionsForChapterAndYear(chapterId: String, year: Int): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId AND examYear BETWEEN :startYear AND :endYear ORDER BY examYear DESC, id ASC")
    fun getQuestionsForChapterAndYears(chapterId: String, startYear: Int, endYear: Int): Flow<List<QuestionEntity>>

    @Query("""
        SELECT * FROM questions 
        WHERE chapterId = :chapterId
          AND (:sourceExam IS NULL OR sourceExam = :sourceExam)
          AND (:startYear IS NULL OR examYear >= :startYear)
          AND (:endYear IS NULL OR examYear <= :endYear)
          AND (:difficulty IS NULL OR difficulty = :difficulty)
          AND (:isOfficialOnly = 0 OR isOfficialPYQ = 1)
        ORDER BY examYear DESC, id ASC
    """)
    fun getQuestionsForChapterWithFilters(
        chapterId: String,
        sourceExam: String? = null,
        startYear: Int? = null,
        endYear: Int? = null,
        difficulty: String? = null,
        isOfficialOnly: Boolean = false
    ): Flow<List<QuestionEntity>>

    @Query("""
        SELECT * FROM questions 
        WHERE chapterId = :chapterId
          AND (:sourceExam IS NULL OR sourceExam = :sourceExam)
          AND (:startYear IS NULL OR examYear >= :startYear)
          AND (:endYear IS NULL OR examYear <= :endYear)
          AND (:difficulty IS NULL OR difficulty = :difficulty)
          AND (:isOfficialOnly = 0 OR isOfficialPYQ = 1)
        ORDER BY examYear DESC, id ASC
    """)
    suspend fun getQuestionsForChapterWithFiltersSync(
        chapterId: String,
        sourceExam: String? = null,
        startYear: Int? = null,
        endYear: Int? = null,
        difficulty: String? = null,
        isOfficialOnly: Boolean = false
    ): List<QuestionEntity>

    @Query("""
        SELECT * FROM questions 
        WHERE chapterId = :chapterId 
          AND id NOT IN (SELECT DISTINCT questionId FROM question_attempt_records WHERE chapterId = :chapterId)
        ORDER BY examYear DESC, id ASC
    """)
    fun getUnansweredQuestionsForChapter(chapterId: String): Flow<List<QuestionEntity>>

    @Query("""
        SELECT * FROM questions 
        WHERE chapterId = :chapterId 
          AND id IN (SELECT DISTINCT questionId FROM mistake_entries WHERE isResolved = 0)
        ORDER BY examYear DESC, id ASC
    """)
    fun getMistakeQuestionsForChapter(chapterId: String): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions WHERE chapterId = :chapterId")
    fun getQuestionCountForChapter(chapterId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE chapterId = :chapterId")
    suspend fun getQuestionCountForChapterSync(chapterId: String): Int

    @Query("SELECT COUNT(*) FROM questions")
    fun getTotalQuestionCount(): Flow<Int>

    @Query("SELECT DISTINCT examYear FROM questions WHERE examYear IS NOT NULL ORDER BY examYear ASC")
    fun getDistinctExamYears(): Flow<List<Int>>

    @Query("SELECT COUNT(*) FROM questions WHERE isOfficialPYQ = 1 AND sourceVerificationStatus = 'VERIFIED'")
    fun getTotalOfficialPyqCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE sourceVerificationStatus = 'UNVERIFIED'")
    fun getTotalUnverifiedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE sourceVerificationStatus = 'SAMPLE'")
    fun getTotalSampleCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE sourceVerificationStatus = 'GENERATED'")
    fun getTotalGeneratedCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT COALESCE(historicalPaperId, sourceExam || '_' || examYear)) FROM questions WHERE examYear IS NOT NULL")
    fun getTotalImportedPapersCount(): Flow<Int>

    @Query("""
        SELECT 
            examYear,
            COUNT(*) as totalCount,
            SUM(CASE WHEN sourceVerificationStatus = 'VERIFIED' THEN 1 ELSE 0 END) as verifiedCount,
            SUM(CASE WHEN sourceVerificationStatus = 'UNVERIFIED' THEN 1 ELSE 0 END) as unverifiedCount,
            SUM(CASE WHEN sourceVerificationStatus = 'SAMPLE' THEN 1 ELSE 0 END) as sampleCount,
            COUNT(DISTINCT historicalPaperId) as paperCount,
            MAX(sourceExam) as detectedExam,
            SUM(CASE WHEN subjectId = 'PHYSICS' THEN 1 ELSE 0 END) as physicsCount,
            SUM(CASE WHEN subjectId = 'CHEMISTRY' THEN 1 ELSE 0 END) as chemistryCount,
            SUM(CASE WHEN subjectId = 'BIOLOGY' THEN 1 ELSE 0 END) as biologyCount
        FROM questions 
        WHERE examYear IS NOT NULL 
        GROUP BY examYear 
        ORDER BY examYear ASC
    """)
    fun getGlobalYearStats(): Flow<List<GlobalYearStats>>

    @Query("SELECT examYear, COUNT(*) as count FROM questions WHERE chapterId = :chapterId AND examYear IS NOT NULL GROUP BY examYear ORDER BY examYear ASC")
    fun getQuestionCountByYear(chapterId: String): Flow<List<YearCount>>

    @Query("SELECT examYear, COUNT(*) as count FROM questions WHERE chapterId = :chapterId AND examYear IS NOT NULL AND sourceVerificationStatus = 'VERIFIED' GROUP BY examYear ORDER BY examYear ASC")
    fun getVerifiedQuestionCountByYear(chapterId: String): Flow<List<YearCount>>

    @Query("SELECT examYear, COUNT(*) as count FROM questions WHERE chapterId = :chapterId AND examYear IS NOT NULL AND sourceVerificationStatus = 'UNVERIFIED' GROUP BY examYear ORDER BY examYear ASC")
    fun getUnverifiedQuestionCountByYear(chapterId: String): Flow<List<YearCount>>

    @Query("SELECT examYear, COUNT(*) as count FROM questions WHERE chapterId = :chapterId AND examYear IS NOT NULL GROUP BY examYear ORDER BY examYear ASC")
    suspend fun getQuestionCountByYearSync(chapterId: String): List<YearCount>

    @Query("SELECT sourceExam, COUNT(*) as count FROM questions WHERE chapterId = :chapterId GROUP BY sourceExam")
    fun getQuestionCountByExam(chapterId: String): Flow<List<ExamCount>>

    @Query("SELECT topicName, COUNT(*) as count FROM questions WHERE chapterId = :chapterId GROUP BY topicName ORDER BY count DESC")
    fun getQuestionCountByTopic(chapterId: String): Flow<List<TopicCount>>

    @Query("SELECT id FROM questions")
    suspend fun getAllQuestionIds(): List<String>

    @Query("SELECT questionText FROM questions")
    suspend fun getAllQuestionTexts(): List<String>

    @Query("SELECT sourceExam || '-' || COALESCE(examYear, 0) || '-' || paperSession || '-' || COALESCE(originalQuestionNumber, 0) FROM questions WHERE originalQuestionNumber IS NOT NULL")
    suspend fun getExistingNaturalKeys(): List<String>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE examYear = :year ORDER BY id ASC")
    fun getQuestionsForYear(year: Int): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE examYear = :year ORDER BY id ASC")
    suspend fun getQuestionsForYearSync(year: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE sourceExam = :sourceExam AND examYear = :year ORDER BY id ASC")
    fun getQuestionsForExamAndYear(sourceExam: String, year: Int): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE sourceExam = :sourceExam AND examYear = :year ORDER BY id ASC")
    suspend fun getQuestionsForExamAndYearSync(sourceExam: String, year: Int): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1")
    fun bookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("UPDATE questions SET isBookmarked = :isBookmarked WHERE id = :questionId")
    suspend fun toggleQuestionBookmark(questionId: String, isBookmarked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    // Question Attempts
    @Query("SELECT * FROM question_attempt_records ORDER BY timestamp DESC")
    fun getAllQuestionAttempts(): Flow<List<QuestionAttemptRecordEntity>>

    @Query("SELECT * FROM question_attempt_records WHERE subjectName = :subjectName")
    fun getQuestionAttemptsForSubject(subjectName: String): Flow<List<QuestionAttemptRecordEntity>>

    @Query("SELECT * FROM question_attempt_records WHERE chapterName = :chapterName")
    fun getQuestionAttemptsForChapter(chapterName: String): Flow<List<QuestionAttemptRecordEntity>>

    @Query("SELECT * FROM question_attempts WHERE chapterId = :chapterId")
    fun getAttemptsForChapter(chapterId: String): Flow<List<QuestionAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionAttempt(attempt: QuestionAttemptRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordQuestionAttempt(attempt: QuestionAttemptEntity): Long

    // Quiz Attempts
    @Query("SELECT * FROM quiz_attempt_records ORDER BY timestamp DESC")
    fun getAllQuizAttempts(): Flow<List<QuizAttemptRecordEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    suspend fun getQuizAttemptEntityById(id: Long): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_attempt_records WHERE id = :id")
    suspend fun getQuizAttemptById(id: Long): QuizAttemptRecordEntity?

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    fun getQuizAttemptFlowById(id: Long): Flow<QuizAttemptEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttemptRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordQuizAttempt(attempt: QuizAttemptEntity): Long

    @Query("DELETE FROM chapter_progress")
    suspend fun deleteAllChapterProgress()

    @Query("DELETE FROM question_attempt_records")
    suspend fun deleteAllQuestionAttempts()

    @Query("DELETE FROM quiz_attempt_records")
    suspend fun deleteAllQuizAttempts()
}

data class YearCount(
    val examYear: Int?,
    val count: Int
)

data class ExamCount(
    val sourceExam: String,
    val count: Int
)

data class TopicCount(
    val topicName: String,
    val count: Int
)

data class GlobalYearStats(
    val examYear: Int,
    val totalCount: Int,
    val verifiedCount: Int,
    val unverifiedCount: Int,
    val sampleCount: Int,
    val paperCount: Int = 0,
    val detectedExam: String? = null,
    val physicsCount: Int,
    val chemistryCount: Int,
    val biologyCount: Int
)


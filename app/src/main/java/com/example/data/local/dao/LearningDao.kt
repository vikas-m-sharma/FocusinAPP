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

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId")
    fun getQuestionsForChapter(chapterId: String): Flow<List<QuestionEntity>>

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
    suspend fun getQuizAttemptById(id: Long): QuizAttemptEntity?

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    fun getQuizAttemptFlowById(id: Long): Flow<QuizAttemptEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttemptRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordQuizAttempt(attempt: QuizAttemptEntity): Long
}

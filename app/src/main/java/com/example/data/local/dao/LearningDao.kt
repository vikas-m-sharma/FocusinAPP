package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ChapterProgressEntity
import com.example.data.local.entity.QuestionAttemptRecordEntity
import com.example.data.local.entity.QuizAttemptRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {

    // Chapter Progress
    @Query("SELECT * FROM chapter_progress")
    fun getAllChapterProgress(): Flow<List<ChapterProgressEntity>>

    @Query("SELECT * FROM chapter_progress WHERE chapterId = :chapterId")
    suspend fun getChapterProgressById(chapterId: String): ChapterProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChapterProgress(progress: ChapterProgressEntity)

    // Question Attempts
    @Query("SELECT * FROM question_attempts ORDER BY timestamp DESC")
    fun getAllQuestionAttempts(): Flow<List<QuestionAttemptRecordEntity>>

    @Query("SELECT * FROM question_attempts WHERE subjectName = :subjectName")
    fun getQuestionAttemptsForSubject(subjectName: String): Flow<List<QuestionAttemptRecordEntity>>

    @Query("SELECT * FROM question_attempts WHERE chapterName = :chapterName")
    fun getQuestionAttemptsForChapter(chapterName: String): Flow<List<QuestionAttemptRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionAttempt(attempt: QuestionAttemptRecordEntity): Long

    // Quiz Attempts
    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllQuizAttempts(): Flow<List<QuizAttemptRecordEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    suspend fun getQuizAttemptById(id: Long): QuizAttemptRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttemptRecordEntity): Long

    @Query("DELETE FROM chapter_progress")
    suspend fun deleteAllChapterProgress()

    @Query("DELETE FROM question_attempts")
    suspend fun deleteAllQuestionAttempts()

    @Query("DELETE FROM quiz_attempts")
    suspend fun deleteAllQuizAttempts()
}

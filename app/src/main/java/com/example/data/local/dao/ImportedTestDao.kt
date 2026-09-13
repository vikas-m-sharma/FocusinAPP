package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ImportedQuestionEntity
import com.example.data.local.entity.ImportedTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportedTestDao {

    @Query("SELECT * FROM imported_tests ORDER BY createdAt DESC")
    fun getAllImportedTests(): Flow<List<ImportedTestEntity>>

    @Query("SELECT * FROM imported_tests WHERE testId = :testId")
    suspend fun getImportedTestById(testId: String): ImportedTestEntity?

    @Query("SELECT * FROM imported_questions WHERE testId = :testId ORDER BY questionNumber ASC")
    fun getQuestionsForTest(testId: String): Flow<List<ImportedQuestionEntity>>

    @Query("SELECT * FROM imported_questions WHERE testId = :testId ORDER BY questionNumber ASC")
    suspend fun getQuestionsForTestSync(testId: String): List<ImportedQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportedTest(test: ImportedTestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportedQuestions(questions: List<ImportedQuestionEntity>)

    @Update
    suspend fun updateImportedTest(test: ImportedTestEntity)

    @Query("""
        UPDATE imported_tests 
        SET status = :status, 
            score = :score, 
            accuracy = :accuracy, 
            correctCount = :correctCount, 
            wrongCount = :wrongCount, 
            unattemptedCount = :unattemptedCount, 
            timeTakenSeconds = :timeTakenSeconds,
            lastAttemptedAt = :lastAttemptedAt
        WHERE testId = :testId
    """)
    suspend fun recordTestResult(
        testId: String,
        status: String,
        score: Int,
        accuracy: Int,
        correctCount: Int,
        wrongCount: Int,
        unattemptedCount: Int,
        timeTakenSeconds: Long,
        lastAttemptedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE imported_tests
        SET currentQuestionIndex = :currentQuestionIndex,
            remainingSeconds = :remainingSeconds,
            status = 'IN_PROGRESS'
        WHERE testId = :testId
    """)
    suspend fun updateTestProgress(
        testId: String,
        currentQuestionIndex: Int,
        remainingSeconds: Long
    )

    @Query("""
        UPDATE imported_questions
        SET userAnswer = :userAnswer,
            userAnswerIndex = :userAnswerIndex,
            isAttempted = :isAttempted,
            isMarkedForReview = :isMarkedForReview,
            answerState = :answerState,
            timeSpentSeconds = :timeSpentSeconds,
            isCorrect = :isCorrect
        WHERE id = :questionId
    """)
    suspend fun updateUserAnswerState(
        questionId: String,
        userAnswer: String?,
        userAnswerIndex: Int?,
        isAttempted: Boolean,
        isMarkedForReview: Boolean,
        answerState: String,
        timeSpentSeconds: Int,
        isCorrect: Boolean?
    )

    @Query("UPDATE imported_questions SET isMarkedForReview = :isMarked WHERE id = :questionId")
    suspend fun updateQuestionMarkedForReview(questionId: String, isMarked: Boolean)

    @Query("""
        UPDATE imported_questions
        SET userAnswer = NULL,
            userAnswerIndex = NULL,
            isAttempted = 0,
            isMarkedForReview = 0,
            answerState = 'UNATTEMPTED',
            timeSpentSeconds = 0,
            isCorrect = NULL
        WHERE testId = :testId
    """)
    suspend fun resetTestAnswers(testId: String)

    @Query("DELETE FROM imported_tests WHERE testId = :testId")
    suspend fun deleteImportedTest(testId: String)

    @Query("DELETE FROM imported_questions WHERE testId = :testId")
    suspend fun deleteQuestionsForTest(testId: String)
}

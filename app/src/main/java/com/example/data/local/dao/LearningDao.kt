package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.LearningResourceEntity
import com.example.data.local.entity.QuestionAttemptEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {

    // Chapters
    @Query("SELECT * FROM chapters WHERE examId = :examId ORDER BY orderIndex ASC")
    fun getChaptersByExam(examId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE examId = :examId AND subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getChaptersBySubject(examId: String, subjectId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    fun getChapterById(chapterId: String): Flow<ChapterEntity?>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterDirect(chapterId: String): ChapterEntity?

    @Query("SELECT * FROM chapters ORDER BY lastAccessedTimestamp DESC LIMIT 1")
    fun getLastActiveChapter(): Flow<ChapterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    // Topics
    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getTopicsForChapter(chapterId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    suspend fun getTopicsDirect(chapterId: String): List<TopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Query("UPDATE topics SET status = :status, lastUpdated = :timestamp WHERE id = :topicId")
    suspend fun updateTopicStatus(topicId: String, status: String, timestamp: Long = System.currentTimeMillis())

    // Questions
    @Query("SELECT * FROM questions WHERE chapterId = :chapterId")
    fun getQuestionsForChapter(chapterId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId")
    suspend fun getQuestionsForChapterDirect(chapterId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId")
    suspend fun getQuestionsForSubjectDirect(subjectId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE isOfficialPYQ = 1 AND pyqYear = :year ORDER BY id ASC")
    fun getQuestionsForPYQ(year: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isOfficialPYQ = 1 AND pyqYear = :year")
    suspend fun getQuestionsForPYQDirect(year: String): List<QuestionEntity>

    @Query("SELECT DISTINCT pyqYear FROM questions WHERE isOfficialPYQ = 1 AND pyqYear IS NOT NULL")
    fun getAvailablePYQYears(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions ORDER BY id ASC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1 ORDER BY id ASC")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("UPDATE questions SET isBookmarked = :isBookmarked WHERE id = :questionId")
    suspend fun updateQuestionBookmark(questionId: String, isBookmarked: Boolean)

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId")
    fun getQuestionsForSubject(subjectId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId AND topicName = :topicName")
    fun getQuestionsForTopic(chapterId: String, topicName: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId AND topicName = :topicName")
    suspend fun getQuestionsForTopicDirect(chapterId: String, topicName: String): List<QuestionEntity>

    // Attempts
    @Insert
    suspend fun recordQuestionAttempt(attempt: QuestionAttemptEntity): Long

    @Query("SELECT * FROM question_attempts WHERE chapterId = :chapterId")
    fun getAttemptsForChapter(chapterId: String): Flow<List<QuestionAttemptEntity>>

    @Query("SELECT * FROM question_attempts WHERE subjectId = :subjectId")
    fun getAttemptsForSubject(subjectId: String): Flow<List<QuestionAttemptEntity>>

    @Query("SELECT * FROM question_attempts ORDER BY timestamp DESC")
    fun getAllQuestionAttempts(): Flow<List<QuestionAttemptEntity>>

    @Query("SELECT * FROM question_attempts ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentQuestionAttempts(limit: Int): Flow<List<QuestionAttemptEntity>>

    @Query("SELECT COUNT(*) FROM question_attempts")
    fun getTotalAttemptedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM question_attempts WHERE isCorrect = 1")
    fun getTotalCorrectCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM question_attempts WHERE subjectId = :subjectId")
    fun getAttemptsCountForSubject(subjectId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM question_attempts WHERE subjectId = :subjectId AND isCorrect = 1")
    fun getCorrectCountForSubject(subjectId: String): Flow<Int>

    // Quizzes & Tests
    @Insert
    suspend fun recordQuizAttempt(attempt: QuizAttemptEntity): Long

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllQuizAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE mode = 'MOCK_TEST' ORDER BY timestamp DESC")
    fun getMockTestAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE mode = 'MOCK_TEST'")
    fun getTotalMockTestsCount(): Flow<Int>

    @Query("SELECT * FROM quiz_attempts WHERE chapterId = :chapterId ORDER BY timestamp DESC")
    fun getQuizAttemptsForChapter(chapterId: String): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC LIMIT 5")
    fun getRecentQuizAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    fun getQuizAttemptById(id: Long): Flow<QuizAttemptEntity?>

    @Query("SELECT * FROM quiz_attempts WHERE id = :id")
    suspend fun getQuizAttemptByIdDirect(id: Long): QuizAttemptEntity?

    // Topic single queries
    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun getTopicById(topicId: String): Flow<TopicEntity?>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicDirect(topicId: String): TopicEntity?

    @Query("SELECT * FROM topics ORDER BY lastUpdated DESC LIMIT 1")
    fun getLastActiveTopic(): Flow<TopicEntity?>

    // Learning Resources
    @Query("SELECT * FROM learning_resources WHERE chapterId = :chapterId")
    fun getResourcesForChapter(chapterId: String): Flow<List<LearningResourceEntity>>

    @Query("SELECT * FROM learning_resources WHERE topicId = :topicId")
    fun getResourcesForTopic(topicId: String): Flow<List<LearningResourceEntity>>

    @Query("SELECT * FROM learning_resources WHERE examId = :examId LIMIT 10")
    fun getFeaturedResources(examId: String): Flow<List<LearningResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<LearningResourceEntity>)

    @Query("UPDATE learning_resources SET isCompleted = :completed WHERE id = :resourceId")
    suspend fun updateResourceCompletion(resourceId: String, completed: Boolean)

    @Query("UPDATE learning_resources SET isBookmarked = :bookmarked WHERE id = :resourceId")
    suspend fun updateResourceBookmark(resourceId: String, bookmarked: Boolean)
}

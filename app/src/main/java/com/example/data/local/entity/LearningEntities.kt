package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Structured Roadmap & Study Resource Entities.
 * Supports NEET (MVP) and scalable for JEE, Boards, etc.
 */

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey
    val id: String, // e.g., "neet_phy_current_electricity"
    val examId: String = "NEET",
    val subjectId: String, // "PHYSICS", "CHEMISTRY", "BIOLOGY"
    val name: String,
    val orderIndex: Int,
    val totalTopics: Int = 12,
    val completedTopics: Int = 0,
    val totalQuestions: Int = 120,
    val attemptedQuestions: Int = 0,
    val correctAttempts: Int = 0,
    val isBookmarked: Boolean = false,
    val lastAccessedTimestamp: Long = 0L
) {
    val completionPercentage: Int
        get() = if (totalTopics > 0) ((completedTopics.toFloat() / totalTopics) * 100).toInt().coerceIn(0, 100) else 0

    val accuracyPercentage: Int
        get() = if (attemptedQuestions > 0) ((correctAttempts.toFloat() / attemptedQuestions) * 100).toInt().coerceIn(0, 100) else 0

    val completedTopicsCount get() = completedTopics
    val totalTopicsCount get() = totalTopics
    val totalQuestionsCount get() = totalQuestions
    val progressPercent get() = completionPercentage
}

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey
    val id: String, // e.g. "neet_phy_curr_01"
    val chapterId: String,
    val name: String,
    val orderIndex: Int,
    val status: String = "NOT_STARTED", // "NOT_STARTED", "LEARNING", "COMPLETED", "NEEDS_REVISION"
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val examId: String = "NEET",
    val subjectId: String, // "PHYSICS", "CHEMISTRY", "BIOLOGY"
    val chapterId: String,
    val topicName: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", "D"
    val explanation: String,
    val difficulty: String = "MEDIUM", // "EASY", "MEDIUM", "HARD"
    val pyqYear: String? = null, // e.g., "NEET 2024", "NEET 2023", null for standard practice
    val isOfficialPYQ: Boolean = false,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "question_attempts")
data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: String = "",
    val chapterId: String = "",
    val subjectId: String = "",
    val topicName: String = "",
    val selectedOption: String = "",
    val isCorrect: Boolean = false,
    val timeTakenSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val timeSpentSeconds get() = timeTakenSeconds
}

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: String = "NEET",
    val subjectId: String = "",
    val chapterId: String = "",
    val chapterName: String = "",
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val timeTakenSeconds: Int = 0,
    val mode: String = "PRACTICE", // "PRACTICE", "MOCK_TEST", "AI_QUIZ", "PYQ"
    val strongTopicsJson: String = "[]",
    val weakTopicsJson: String = "[]",
    val timestamp: Long = System.currentTimeMillis()
) {
    val accuracy: Int
        get() = if (totalQuestions > 0) ((correctAnswers.toFloat() / totalQuestions) * 100).toInt() else 0

    val correctCount get() = correctAnswers
    val attemptedAt get() = timestamp
    val scorePercentage get() = accuracy
}

@Entity(tableName = "learning_resources")
data class LearningResourceEntity(
    @PrimaryKey
    val id: String,
    val chapterId: String,
    val topicId: String? = null,
    val examId: String = "NEET",
    val subjectId: String = "PHYSICS",
    val title: String,
    val channel: String,
    val durationText: String,
    val resourceType: String, // "Complete Chapter", "Concept", "Revision", "One Shot", "Numerical Practice"
    val recommendedReason: String, // "Concept building", "Formula revision", "Problem solving", etc.
    val searchQuery: String, // Legitimate YouTube search query
    val isCompleted: Boolean = false,
    val isBookmarked: Boolean = false
)

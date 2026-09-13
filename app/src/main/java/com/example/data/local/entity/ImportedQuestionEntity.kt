package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.NeetQuestion

/**
 * Standard user-answer states in the test engine.
 */
enum class QuestionAnswerState {
    UNATTEMPTED,
    ANSWERED,
    MARKED_FOR_REVIEW,
    ANSWERED_AND_MARKED_FOR_REVIEW,
    SKIPPED
}

/**
 * Room Entity representing a single MCQ from a user-uploaded test paper.
 * Stores question content, options, and active user-answer states (selected option,
 * marked for review, time spent, correctness) ensuring full compatibility with
 * the existing NEET test engine and OMR sheet simulator.
 */
@Entity(tableName = "imported_questions")
data class ImportedQuestion(
    @PrimaryKey
    val id: String, // e.g. "${testId}_q${questionNumber}"
    val testId: String,
    val questionNumber: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val subject: String = "Physics", // "Physics", "Chemistry", "Biology", "Botany", "Zoology"
    val chapter: String = "",
    val topic: String = "",
    val correctAnswer: String = "A", // "A", "B", "C", "D"
    val correctOptionIndex: Int = 0, // 0..3 (A=0, B=1, C=2, D=3)
    val answerSource: String = "AI_SOLVED", // "EXTRACTED_FROM_DOCUMENT", "AI_SOLVED", "AI_VERIFIED", "USER_REVIEWED"
    val confidence: Float = 0.92f,
    val explanation: String = "",

    // =========================================================================
    // USER-ANSWER STATES (Integrated with interactive test & OMR engine)
    // =========================================================================
    val userAnswer: String? = null, // "A", "B", "C", "D" or null
    val userAnswerIndex: Int? = null, // 0..3 or null
    val isAttempted: Boolean = false,
    val isMarkedForReview: Boolean = false,
    val answerState: String = "UNATTEMPTED", // "UNATTEMPTED", "ANSWERED", "MARKED_FOR_REVIEW", "ANSWERED_AND_MARKED_FOR_REVIEW"
    val timeSpentSeconds: Int = 0,
    val isCorrect: Boolean? = null // null when unattempted, true/false when evaluated
) {
    /**
     * Helper to return all 4 options as a list for UI rendering and compatibility.
     */
    fun getOptionsList(): List<String> = listOf(optionA, optionB, optionC, optionD)

    /**
     * Helper to convert this imported entity into standard NeetQuestion model
     * used across the app's question bank and test engines.
     */
    fun toNeetQuestion(): NeetQuestion = NeetQuestion(
        id = id,
        subjectName = subject,
        chapterName = chapter.ifBlank { "NEET Curriculum" },
        topicName = topic.ifBlank { "Unit Practice" },
        questionText = questionText,
        options = listOf(optionA, optionB, optionC, optionD),
        correctOptionIndex = correctOptionIndex.coerceIn(0, 3),
        explanation = explanation.ifBlank { "Solution for Question $questionNumber" },
        difficulty = "MEDIUM"
    )

    /**
     * Checks whether user answer matches correct answer
     */
    fun checkCorrectness(): Boolean? {
        if (userAnswer == null) return null
        return userAnswer.equals(correctAnswer, ignoreCase = true)
    }
}

/**
 * Typealias ensuring 100% backward compatibility with existing usages.
 */
typealias ImportedQuestionEntity = ImportedQuestion


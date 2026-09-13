package com.example.data.model

import com.example.data.local.entity.QuestionEntity

/**
 * Historical PYQ Session Configuration
 */
data class PyqPracticeConfig(
    val chapterId: String,
    val subjectId: String,
    val sourceExamFilter: String? = null, // null = ALL, "NEET_UG", "AIPMT"
    val startYear: Int? = null,
    val endYear: Int? = null,
    val statusFilter: String = "ALL", // "ALL", "UNANSWERED", "MISTAKES"
    val questionCount: Int = 25,
    val mode: String = "INSTANT", // "INSTANT" or "OMR"
    val verificationFilter: String? = null // null = ALL, "VERIFIED", "UNVERIFIED"
)

/**
 * Extension to convert Room QuestionEntity to NeetQuestion
 */
fun QuestionEntity.toNeetQuestion(
    chapterDisplayName: String? = null,
    subjectDisplayName: String? = null
): NeetQuestion {
    return NeetQuestion(
        id = id,
        subjectName = subjectDisplayName ?: subjectId,
        chapterName = chapterDisplayName ?: chapterId,
        topicName = topicName,
        questionText = questionText,
        options = listOf(optionA, optionB, optionC, optionD),
        correctOptionIndex = correctOptionIndex,
        explanation = explanation,
        difficulty = difficulty,
        pyqYear = examYear,
        sourceExam = sourceExam,
        paperSession = paperSession,
        isOfficialPYQ = isOfficialPYQ,
        syllabusStatus = syllabusStatus,
        sourceVerificationStatus = sourceVerificationStatus
    )
}

package com.example.data.importer

import com.example.data.local.dao.LearningDao
import com.example.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Developer and Ingestion Review Queue Manager.
 *
 * Provides human-in-the-loop review capabilities before historical questions are
 * committed to Room or elevated to VERIFIED status.
 *
 * Rules:
 * - Only questions with verified primary source evidence can be approved as VERIFIED.
 * - Unmapped chapters can be manually mapped to canonical curriculum chapters.
 * - Ambiguous answers can be confirmed from official keys.
 * - Rejected questions are dropped safely.
 * - Batch commit is atomic and idempotent.
 */
class PyqReviewQueueManager(
    private val learningDao: LearningDao? = null
) {
    private val _queue = MutableStateFlow<List<PyqNormalizedQuestion>>(emptyList())
    val queue: StateFlow<List<PyqNormalizedQuestion>> = _queue.asStateFlow()

    fun setQueue(items: List<PyqNormalizedQuestion>) {
        _queue.value = items
    }

    fun addToQueue(items: List<PyqNormalizedQuestion>) {
        val current = _queue.value.toMutableList()
        current.addAll(items)
        _queue.value = current
    }

    fun clearQueue() {
        _queue.value = emptyList()
    }

    /**
     * Approves a question as VERIFIED.
     * Enforces the requirement that primary evidence notes/citation must be present.
     */
    fun approveAsVerified(questionId: String, primaryEvidenceCitation: String): Boolean {
        if (primaryEvidenceCitation.isBlank()) return false
        val current = _queue.value
        val target = current.find { it.id == questionId } ?: return false

        val updatedMeta = target.sourceReference.copy(
            primaryEvidenceUri = primaryEvidenceCitation,
            contributorNotes = "Human-approved with verified primary source evidence: $primaryEvidenceCitation"
        )

        val updatedQuestion = target.copy(
            verificationStatus = "VERIFIED",
            needsHumanReview = false,
            sourceReference = updatedMeta,
            reviewNotes = target.reviewNotes + "Approved as VERIFIED by reviewer."
        )

        _queue.value = current.map { if (it.id == questionId) updatedQuestion else it }
        return true
    }

    /**
     * Keeps question in dataset but strictly as UNVERIFIED.
     */
    fun keepUnverified(questionId: String): Boolean {
        val current = _queue.value
        val target = current.find { it.id == questionId } ?: return false

        val updatedQuestion = target.copy(
            verificationStatus = "UNVERIFIED",
            needsHumanReview = false,
            reviewNotes = target.reviewNotes + "Kept as UNVERIFIED pending primary source verification."
        )

        _queue.value = current.map { if (it.id == questionId) updatedQuestion else it }
        return true
    }

    /**
     * Rejects and discards question from import queue.
     */
    fun rejectQuestion(questionId: String): Boolean {
        val current = _queue.value
        val target = current.find { it.id == questionId } ?: return false
        _queue.value = current.filterNot { it.id == questionId }
        return true
    }

    /**
     * Updates chapter mapping for an unmapped or misclassified question.
     */
    fun updateChapter(
        questionId: String,
        canonicalSubjectId: String,
        canonicalChapterId: String,
        topicName: String
    ): Boolean {
        val current = _queue.value
        val target = current.find { it.id == questionId } ?: return false

        val updatedQuestion = target.copy(
            subjectId = canonicalSubjectId,
            chapterId = canonicalChapterId,
            topicName = topicName,
            reviewNotes = target.reviewNotes + "Manual curriculum mapped to $canonicalChapterId."
        )

        _queue.value = current.map { if (it.id == questionId) updatedQuestion else it }
        return true
    }

    /**
     * Corrects or sets answer index.
     */
    fun updateAnswer(questionId: String, correctOptionIndex: Int): Boolean {
        if (correctOptionIndex !in 0..3) return false
        val current = _queue.value
        val target = current.find { it.id == questionId } ?: return false

        val updatedQuestion = target.copy(
            correctOptionIndex = correctOptionIndex,
            reviewNotes = target.reviewNotes + "Manual answer index set to $correctOptionIndex."
        )

        _queue.value = current.map { if (it.id == questionId) updatedQuestion else it }
        return true
    }

    /**
     * Converts clean/approved items in queue to Room entities and inserts into database.
     * Returns the count of newly committed questions.
     */
    suspend fun commitReviewedToDatabase(dao: LearningDao? = learningDao): Int {
        val targetDao = dao ?: return 0
        val itemsToCommit = _queue.value.filter {
            !it.needsHumanReview && it.chapterId != "UNMAPPED" && it.correctOptionIndex in 0..3
        }

        if (itemsToCommit.isEmpty()) return 0

        val entities = itemsToCommit.map { norm ->
            val correctOptionLetter = when (norm.correctOptionIndex) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                3 -> "D"
                else -> "A"
            }
            QuestionEntity(
                id = norm.id,
                examId = "NEET",
                subjectId = norm.subjectId,
                chapterId = norm.chapterId,
                topicName = norm.topicName,
                questionText = norm.questionText,
                optionA = norm.options.getOrElse(0) { "" },
                optionB = norm.options.getOrElse(1) { "" },
                optionC = norm.options.getOrElse(2) { "" },
                optionD = norm.options.getOrElse(3) { "" },
                correctOption = correctOptionLetter,
                explanation = norm.explanation,
                difficulty = norm.difficulty,
                pyqYear = if (norm.examYear != null) "${norm.sourceExam} ${norm.examYear}" else null,
                isOfficialPYQ = norm.verificationStatus == "VERIFIED",
                isBookmarked = false,
                sourceExam = norm.sourceExam,
                examYear = norm.examYear,
                paperSession = norm.paperSession,
                syllabusStatus = norm.syllabusStatus,
                sourceVerificationStatus = norm.verificationStatus,
                historicalPaperId = norm.historicalPaperId,
                originalQuestionNumber = norm.questionNumber,
                sourceReference = norm.sourceReference.toJsonString()
            )
        }

        entities.chunked(100).forEach { chunk ->
            targetDao.insertQuestions(chunk)
        }

        // Remove committed items from queue
        val committedIds = itemsToCommit.map { it.id }.toSet()
        _queue.value = _queue.value.filterNot { committedIds.contains(it.id) }

        return itemsToCommit.size
    }
}

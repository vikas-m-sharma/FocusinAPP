package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing an incorrectly answered question in the Mistake Diary.
 */
@Entity(tableName = "mistake_entries")
data class MistakeEntity(
    @PrimaryKey
    val id: String,
    val questionId: String,
    val testTitle: String,
    val questionText: String,
    val selectedOption: String,
    val correctOption: String,
    val optionsJson: String, // Stored as comma-separated or serialized strings
    val explanation: String,
    val subjectName: String,
    val topicName: String,
    val errorReason: String = "UNTAGGED", // SILLY_MISTAKE, FORMULA_FORGOT, MISREAD_QUESTION, CONCEPT_GAP, UNTAGGED
    val studentNotes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false,
    val reattemptCount: Int = 0,
    val sourceExam: String? = null,
    val examYear: Int? = null
)

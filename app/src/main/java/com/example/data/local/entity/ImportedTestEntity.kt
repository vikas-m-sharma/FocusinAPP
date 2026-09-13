package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a user-uploaded test paper (from PDF or Photos).
 * Compatible with the existing test engine structure, OMR simulation, and performance tracking.
 */
@Entity(tableName = "imported_tests")
data class ImportTest(
    @PrimaryKey
    val testId: String,
    val title: String,
    val sourceType: String = "PDF", // "PDF" or "PHOTOS"
    val exam: String = "NEET (UG)",
    val durationMinutes: Int = 200,
    val totalQuestions: Int = 180,
    val positiveMarks: Float = 4.0f,
    val negativeMarks: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "NOT_ATTEMPTED", // "NOT_ATTEMPTED", "IN_PROGRESS", "COMPLETED"
    val score: Int = 0,
    val maxScore: Int = 720,
    val accuracy: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val unattemptedCount: Int = 0,
    val timeTakenSeconds: Long = 0L,
    val localFilePath: String? = null,
    val lastAttemptedAt: Long? = null,
    val currentQuestionIndex: Int = 0,
    val remainingSeconds: Long = 200L * 60L
)

/**
 * Typealias ensuring 100% backward compatibility with existing usages.
 */
typealias ImportedTestEntity = ImportTest


package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempt_records")
data class QuizAttemptRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val examId: String = "NEET",
    val subjectId: String = "PHYSICS",
    val subjectName: String = "Physics",
    val chapterId: String = "",
    val chapterName: String = "",
    val totalQuestions: Int = 10,
    val correctCount: Int = 8,
    val correctAnswers: Int = 8,
    val scorePercentage: Int = 80,
    val accuracy: Int = 80,
    val mode: String = "PRACTICE",
    val timeTakenSeconds: Int = 300,
    val strongTopicsJson: String = "[]",
    val weakTopicsJson: String = "[]",
    val timestamp: Long = System.currentTimeMillis(),
    val attemptedAt: Long = System.currentTimeMillis()
)

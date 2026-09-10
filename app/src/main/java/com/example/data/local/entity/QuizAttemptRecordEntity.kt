package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_attempts")
data class QuizAttemptRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val examId: String = "NEET",
    val subjectName: String,
    val chapterName: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercentage: Int,
    val strongTopicsJson: String = "[]",
    val weakTopicsJson: String = "[]",
    val timestamp: Long = System.currentTimeMillis()
)

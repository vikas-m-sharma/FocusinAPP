package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_attempts")
data class QuestionAttemptRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: String = "NEET",
    val subjectName: String,
    val chapterName: String,
    val topicName: String = "",
    val questionText: String,
    val selectedOptionIndex: Int,
    val correctOptionIndex: Int,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int = 30,
    val timestamp: Long = System.currentTimeMillis(),
    val quizType: String = "PRACTICE" // "PRACTICE", "PYQ", "AI_QUIZ"
)

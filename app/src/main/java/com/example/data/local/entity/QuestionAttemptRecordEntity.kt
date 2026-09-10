package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_attempt_records")
data class QuestionAttemptRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: String = "",
    val examId: String = "NEET",
    val subjectId: String = "PHYSICS",
    val subjectName: String = "Physics",
    val chapterId: String = "",
    val chapterName: String = "Current Electricity",
    val topicName: String = "",
    val questionText: String = "",
    val selectedOptionIndex: Int = 0,
    val selectedOption: String = "A",
    val correctOptionIndex: Int = 0,
    val isCorrect: Boolean = true,
    val timeSpentSeconds: Int = 30,
    val timeTakenSeconds: Int = 30,
    val timestamp: Long = System.currentTimeMillis(),
    val attemptedAt: Long = System.currentTimeMillis(),
    val quizType: String = "PRACTICE"
)

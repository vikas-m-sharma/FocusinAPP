package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_progress")
data class ChapterProgressEntity(
    @PrimaryKey
    val chapterId: String, // e.g. "neet_physics_current_electricity"
    val examId: String = "NEET",
    val subjectName: String, // "Physics", "Chemistry", "Biology"
    val chapterName: String,
    val completedTopicsJson: String = "[]", // e.g. ["Ohm's Law", "Resistance"]
    val totalTopicsCount: Int = 10,
    val lastStudiedTimestamp: Long = System.currentTimeMillis()
)

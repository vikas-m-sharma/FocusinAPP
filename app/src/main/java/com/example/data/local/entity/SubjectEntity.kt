package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#38BDF8",
    val iconName: String = "School",
    val totalPlannedMinutes: Int = 0,
    val actualFocusedMinutes: Int = 0,
    val sessionsCompleted: Int = 0,
    val targetWeeklyHours: Float = 10f
)

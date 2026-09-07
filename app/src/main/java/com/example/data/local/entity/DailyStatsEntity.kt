package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val dateString: String, // "yyyy-MM-dd"
    val totalPlannedMinutes: Int = 0,
    val totalFocusedMinutes: Int = 0,
    val sessionsCompleted: Int = 0,
    val sessionsTotal: Int = 0,
    val focusScore: Int = 0,
    val distractionCount: Int = 0,
    val goalMinutes: Int = 360, // default 6h
    val dayOfWeek: Int = 1
) {
    val dailyFocusScore: Int get() = focusScore
}

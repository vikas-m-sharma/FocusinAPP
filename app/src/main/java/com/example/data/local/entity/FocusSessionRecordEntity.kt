package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_session_records")
data class FocusSessionRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val taskName: String,
    val plannedDurationMinutes: Int,
    val actualDurationSeconds: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isCompleted: Boolean,
    val earlyEndReason: String? = null,
    val distractionCount: Int = 0,
    val mode: String = "COUNTDOWN", // "COUNTDOWN" or "STOPWATCH"
    val dateString: String // "yyyy-MM-dd"
) {
    val actualDurationMinutes: Int get() = (actualDurationSeconds / 60).toInt()
    val durationMinutes: Int get() = actualDurationMinutes
    val focusScore: Int get() = 85
    val startTimestamp: Long get() = startTimeMillis
}

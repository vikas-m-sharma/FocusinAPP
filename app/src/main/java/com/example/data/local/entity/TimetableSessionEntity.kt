package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_sessions")
data class TimetableSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val subjectId: Long,
    val subjectName: String,
    val taskName: String,
    val startTime: String, // "09:00"
    val endTime: String,   // "11:00"
    val durationMinutes: Int,
    val colorHex: String = "#38BDF8",
    val note: String = "",
    val alarmEnabled: Boolean = true,
    val focusModeEnabled: Boolean = true,
    val voiceNoteId: Long? = null,
    val soundName: String = "Default Chime",
    val soundUri: String? = null,
    val recurrenceType: String = "WEEKLY", // "ONCE", "WEEKLY", "WEEKDAYS", "DAILY", "CUSTOM"
    val customDaysJson: String = "[]",     // e.g. "[1, 3, 5]" for Mon, Wed, Fri
    val isEnabled: Boolean = true,
    val blockedAppsJson: String = "[\"Instagram\", \"TikTok\", \"YouTube\", \"Twitter\", \"Facebook\", \"Snapchat\", \"Netflix\", \"Reddit\"]",
    val isCompleted: Boolean = false
) {
    val recurrence: String get() = recurrenceType
}

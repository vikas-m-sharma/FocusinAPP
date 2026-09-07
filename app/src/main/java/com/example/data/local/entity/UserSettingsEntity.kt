package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val isOnboardingComplete: Boolean = true,
    val userName: String = "Scholar",
    val userEmail: String? = null,
    val userGoals: String = "Study, Programming", // comma separated
    val dailyGoalMinutes: Int = 360, // 6 hours
    val weeklyGoalMinutes: Int = 2400, // 40 hours
    val preferredSchedule: String = "Morning", // Morning, Afternoon, Evening, Custom
    val currentStreak: Int = 0,
    val lastActiveDate: String = "",
    val focusProtectionLevel: String = "ENHANCED", // STANDARD, ENHANCED, STRICT
    val allowedAppsJson: String = "[\"Calculator\", \"Google Drive\", \"Chrome\", \"ChatGPT\", \"Notion\", \"Dictionary\"]",
    val blockedAppsJson: String = "[\"Instagram\", \"TikTok\", \"YouTube\", \"Twitter\", \"Facebook\", \"Snapchat\", \"Netflix\", \"Reddit\"]",
    val defaultAlarmSound: String = "Digital Chime",
    val defaultVoiceNoteId: Long? = null,
    val breakFocusMinutes: Int = 50,
    val breakRestMinutes: Int = 10,
    val notificationsEnabled: Boolean = true,
    val sessionRemindersEnabled: Boolean = true,
    val eveningSummaryEnabled: Boolean = true,
    val isGoogleSignedIn: Boolean = false,
    val cloudSyncEnabled: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)

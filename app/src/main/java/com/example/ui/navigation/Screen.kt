package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Exactly Three Primary Bottom Navigation Tabs
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Schedule : Screen("schedule", "Schedule", Icons.Default.DateRange)
    object Performance : Screen("performance", "Performance", Icons.Default.BarChart)

    // Aliases for compatibility
    val Timetable get() = Schedule
    val Analytics get() = Performance

    // Secondary Flow Screens
    object Focus : Screen("focus", "Focus")
    object CompletionReward : Screen("completion_reward", "Session Complete")
    object Settings : Screen("settings", "Settings")
    object Subjects : Screen("subjects", "Subjects")
    object VoiceStudio : Screen("voice_studio", "Voice Studio")
    object Onboarding : Screen("onboarding", "Welcome")

    companion object {
        val Timetable = Schedule
        val Analytics = Performance
        val VoiceNotes = VoiceStudio
    }
}

// Exactly 3 bottom navigation items
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Schedule,
    Screen.Performance
)

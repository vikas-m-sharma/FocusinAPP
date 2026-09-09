package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Exactly Five Primary Bottom Navigation Tabs
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Schedule : Screen("schedule", "Schedule", Icons.Default.DateRange)
    object Prepare : Screen("prepare", "Prepare", Icons.Default.School)
    object QuestionBank : Screen("question_bank", "QBank", Icons.Default.Assignment)
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

    // Chapter & Practice secondary destinations
    object SubjectChapters : Screen("subject_chapters/{subjectId}", "Subject Chapters") {
        fun createRoute(subjectId: String) = "subject_chapters/$subjectId"
    }
    object ChapterDetail : Screen("chapter_detail/{chapterId}", "Chapter Detail") {
        fun createRoute(chapterId: String) = "chapter_detail/$chapterId"
    }
    object ChapterPractice : Screen("chapter_practice/{chapterId}/{mode}", "Chapter Practice") {
        fun createRoute(chapterId: String, mode: String = "PRACTICE") = "chapter_practice/$chapterId/$mode"
    }
    object PyqPractice : Screen("pyq_practice/{year}", "PYQ Paper") {
        fun createRoute(year: String) = "pyq_practice/$year"
    }
    object AiQuizGenerator : Screen("ai_quiz_generator/{chapterId}", "AI Quiz") {
        fun createRoute(chapterId: String = "ALL") = "ai_quiz_generator/$chapterId"
    }
    object ChapterOptions : Screen("chapter_options/{chapterId}", "Chapter Options") {
        fun createRoute(chapterId: String) = "chapter_options/$chapterId"
    }
    object PreviousYearPapers : Screen("previous_year_papers", "Previous Year Papers")
    object MockTests : Screen("mock_tests", "Mock Tests")
    object MyPractice : Screen("my_practice", "My Practice")
    object Bookmarks : Screen("bookmarks", "Bookmarks")
    object WeakTopics : Screen("weak_topics", "Weak Topics")

    // Performance & Analytics Secondary Flow Screens
    object SubjectPerformance : Screen("subject_performance/{subjectId}", "Subject Performance") {
        fun createRoute(subjectId: String = "PHYSICS") = "subject_performance/$subjectId"
    }
    object FocusAnalytics : Screen("focus_analytics", "Focus Analytics")
    object LearningAnalytics : Screen("learning_analytics", "Learning Analytics")
    object RecentActivity : Screen("recent_activity", "Recent Activity")
    object TestResults : Screen("test_results/{quizId}", "Test Results") {
        fun createRoute(quizId: Long = 0L) = "test_results/$quizId"
    }
    object ProgressComparison : Screen("progress_comparison", "Your Progress")
    object GoalsAndInsights : Screen("goals_and_insights", "Goals & Insights")
    object AiPerformanceCoach : Screen("ai_performance_coach", "AI Performance Coach")

    companion object {
        val Timetable = Schedule
        val Analytics = Performance
        val VoiceNotes = VoiceStudio
    }
}

// Exactly 5 bottom navigation items: Home, Schedule, Prepare, Question Bank, Performance
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Schedule,
    Screen.Prepare,
    Screen.QuestionBank,
    Screen.Performance
)

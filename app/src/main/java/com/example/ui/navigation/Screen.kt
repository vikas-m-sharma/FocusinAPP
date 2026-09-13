package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Primary Bottom Navigation Destinations
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Schedule : Screen("schedule", "Schedule", Icons.Default.DateRange)
    object QuestionBank : Screen("question_bank", "QBank", Icons.Default.Quiz)
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
    object Login : Screen("login", "Sign In")
    object Onboarding : Screen("onboarding", "Welcome")
    object SubjectChapters : Screen("subject_chapters/{subjectId}", "Subject Chapters") {
        fun createRoute(subjectId: String) = "subject_chapters/$subjectId"
    }
    object ChapterDetail : Screen("chapter_detail/{chapterId}", "Chapter") {
        fun createRoute(chapterId: String) = "chapter_detail/$chapterId"
    }
    object ChapterOptions : Screen("chapter_options/{chapterId}", "Chapter Options") {
        fun createRoute(chapterId: String) = "chapter_options/$chapterId"
    }
    object ChapterPractice : Screen("chapter_practice/{chapterId}/{mode}", "Chapter Practice") {
        fun createRoute(chapterId: String, mode: String = "topic") = "chapter_practice/$chapterId/$mode"
    }
    object PreviousYearPapers : Screen("previous_year_papers", "Previous Year Papers")
    object AiQuizGenerator : Screen("ai_quiz_generator/{chapterId}", "AI Quiz Generator") {
        fun createRoute(chapterId: String = "neet_phy_current_electricity") = "ai_quiz_generator/$chapterId"
    }
    object MockTest : Screen("mock_test/{testTitle}", "Mock Test") {
        fun createRoute(testTitle: String = "NEET Full Syllabus Test 1") = "mock_test/$testTitle"
    }
    object MyPractice : Screen("my_practice", "My Practice")
    object Bookmarks : Screen("bookmarks", "Bookmarks")
    object WeakTopics : Screen("weak_topics", "Weak Topics")
    object TestResults : Screen("test_results/{quizId}", "Test Results") {
        fun createRoute(quizId: Long) = "test_results/$quizId"
    }
    object FocusAnalytics : Screen("focus_analytics", "Focus Analytics")
    object LearningAnalytics : Screen("learning_analytics", "Learning Analytics")
    object SubjectPerformance : Screen("subject_performance/{subjectName}", "Subject Performance") {
        fun createRoute(subjectName: String = "Physics") = "subject_performance/$subjectName"
    }
    object RecentActivity : Screen("recent_activity", "Recent Activity")
    object ProgressComparison : Screen("progress_comparison", "Progress Comparison")
    object GoalsAndInsights : Screen("goals_and_insights", "Goals & Insights")
    object AiPerformanceCoach : Screen("ai_performance_coach", "AI Performance Coach")
    object MistakeDiary : Screen("mistake_diary", "Mistake Diary")
    object ImportTest : Screen("import_test", "Import Test")
    object SolveImportedTest : Screen("solve_imported_test/{testId}", "Solve Imported Test") {
        fun createRoute(testId: String) = "solve_imported_test/$testId"
    }
    object PdfReader : Screen("pdf_reader/{year}", "PDF Reader") {
        fun createRoute(year: Int) = "pdf_reader/$year"
    }
    object QuestionPaperViewer : Screen("question_paper_viewer/{year}", "Question Paper Viewer") {
        fun createRoute(year: Int = 2024) = "question_paper_viewer/$year"
    }

    // Prepare Hub & NCERT Library Routes
    object Prepare : Screen("prepare", "Prepare")
    object NcertLibrary : Screen("ncert_library", "NCERT Library")
    object NcertClass : Screen("ncert_class/{classNum}", "NCERT Class") {
        fun createRoute(classNum: Int) = "ncert_class/$classNum"
    }
    object NcertSubject : Screen("ncert_subject/{classNum}/{subjectId}", "NCERT Subject") {
        fun createRoute(classNum: Int, subjectId: String) = "ncert_subject/$classNum/$subjectId"
    }
    object NcertBook : Screen("ncert_book/{bookId}", "NCERT Book") {
        fun createRoute(bookId: String) = "ncert_book/$bookId"
    }
    object NcertChapterDetail : Screen("ncert_chapter_detail/{chapterId}", "NCERT Chapter") {
        fun createRoute(chapterId: String) = "ncert_chapter_detail/$chapterId"
    }
    object NcertReader : Screen("ncert_reader/{chapterId}", "NCERT Reader") {
        fun createRoute(chapterId: String) = "ncert_reader/$chapterId"
    }
    object NcertSearch : Screen("ncert_search", "Search NCERT")

    companion object {
        val Timetable = Schedule
        val Analytics = Performance
        val VoiceNotes = VoiceStudio
    }
}

// 4 primary bottom navigation items
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Schedule,
    Screen.QuestionBank,
    Screen.Performance
)

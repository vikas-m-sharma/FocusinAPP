package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.TimetableRingingDialog
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavItems
import com.example.ui.screens.CompletionRewardScreen
import com.example.ui.screens.FocusSessionScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PerformanceScreen
import com.example.ui.screens.PrepareScreen
import com.example.ui.screens.QuestionBankScreen
import com.example.ui.screens.ChapterDetailScreen
import com.example.ui.screens.ChapterPracticeScreen
import com.example.ui.screens.AiQuizGeneratorScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.ChapterOptionsScreen
import com.example.ui.screens.MockTestScreen
import com.example.ui.screens.MyPracticeScreen
import com.example.ui.screens.PreviousYearPapersScreen
import com.example.ui.screens.SubjectChaptersScreen
import com.example.ui.screens.WeakTopicsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.VoiceNotesScreen
import com.example.ui.screens.FocusAnalyticsScreen
import com.example.ui.screens.LearningAnalyticsScreen
import com.example.ui.screens.SubjectPerformanceScreen
import com.example.ui.screens.RecentActivityScreen
import com.example.ui.screens.TestResultsScreen
import com.example.ui.screens.AiPerformanceCoachScreen
import com.example.ui.screens.ProgressComparisonScreen
import com.example.ui.screens.GoalsAndInsightsScreen
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.FocusinTheme
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle distraction warning
        if (intent?.getBooleanExtra("EXTRA_DISTRACTION_WARNING", false) == true) {
            Toast.makeText(this, "Distraction blocked. Focus session is active!", Toast.LENGTH_LONG).show()
        }

        setContent {
            FocusinTheme {
                val viewModel: FocusinViewModel = viewModel()
                val context = LocalContext.current

                // Request Notification Permission on Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {}

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                FocusinApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FocusinApp(viewModel: FocusinViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userSettings by viewModel.userSettings.collectAsState()

    val isRinging by viewModel.isAlarmRinging.collectAsState()
    val ringingSession by viewModel.currentRingingSession.collectAsState()

    // Global Timetable Ringing Screen / Alert
    if (isRinging && ringingSession != null) {
        TimetableRingingDialog(
            session = ringingSession!!,
            onStartFocusAndLock = {
                viewModel.startFocusFromRinging(ringingSession!!)
                navController.navigate(Screen.Focus.route)
            },
            onDismissRinging = {
                viewModel.stopAlarmRinging()
            }
        )
    }

    // Determine if bottom navigation should be visible (strictly 5 primary tabs)
    val showBottomNav = currentRoute in listOf(
        Screen.Home.route,
        Screen.Schedule.route,
        Screen.Prepare.route,
        Screen.QuestionBank.route,
        Screen.Performance.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Slate950,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = Slate950,
                    contentColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E293B))
                    )
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .width(20.dp)
                                                .height(2.5.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(CyanPrimary)
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                    } else {
                                        Spacer(modifier = Modifier.height(5.5.dp))
                                    }
                                    if (screen.icon != null) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanPrimary,
                                selectedTextColor = CyanPrimary,
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B),
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (userSettings?.isOnboardingComplete == false) Screen.Onboarding.route else Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = { goals, dailyHours, schedule ->
                        viewModel.completeOnboarding(goals, dailyHours, schedule)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    onSeedDemo = {
                        viewModel.seedDemoData()
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSchedule = { navController.navigate(Screen.Schedule.route) },
                    onNavigateToFocus = { navController.navigate(Screen.Focus.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onOpenCreateSession = { navController.navigate(Screen.Schedule.route) },
                    onNavigateToPrepare = { navController.navigate(Screen.Prepare.route) },
                    onNavigateToQuestionBank = { navController.navigate(Screen.QuestionBank.route) }
                )
            }

            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    viewModel = viewModel,
                    onStartSession = { session ->
                        val isProtection = session.focusModeEnabled && (session.note != "OFF")
                        viewModel.startFocusSession(
                            subjectId = session.subjectId,
                            subjectName = session.subjectName,
                            taskName = session.taskName,
                            durationMinutes = session.durationMinutes,
                            mode = "COUNTDOWN",
                            protection = isProtection
                        )
                        navController.navigate(Screen.Focus.route)
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Prepare.route) {
                PrepareScreen(
                    viewModel = viewModel,
                    onNavigateToChapter = { chapterId ->
                        navController.navigate(Screen.ChapterDetail.createRoute(chapterId))
                    },
                    onNavigateToSubject = { subjectId ->
                        navController.navigate(Screen.SubjectChapters.createRoute(subjectId))
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.SubjectChapters.route) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId") ?: "PHYSICS"
                SubjectChaptersScreen(
                    subjectId = subjectId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChapter = { chapterId ->
                        navController.navigate(Screen.ChapterDetail.createRoute(chapterId))
                    }
                )
            }

            composable(Screen.QuestionBank.route) {
                QuestionBankScreen(
                    viewModel = viewModel,
                    onNavigateToSubject = { subjectId ->
                        navController.navigate(Screen.SubjectChapters.createRoute(subjectId))
                    },
                    onNavigateToPreviousYearPapers = {
                        navController.navigate(Screen.PreviousYearPapers.route)
                    },
                    onNavigateToAiQuiz = { chapterId ->
                        navController.navigate(Screen.AiQuizGenerator.createRoute(chapterId))
                    },
                    onNavigateToMockTests = {
                        navController.navigate(Screen.MockTests.route)
                    },
                    onNavigateToMyPractice = {
                        navController.navigate(Screen.MyPractice.route)
                    },
                    onNavigateToBookmarks = {
                        navController.navigate(Screen.Bookmarks.route)
                    },
                    onNavigateToWeakTopics = {
                        navController.navigate(Screen.WeakTopics.route)
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.ChapterOptions.route) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: "neet_phy_current_electricity"
                ChapterOptionsScreen(
                    chapterId = chapterId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPractice = { chapId, mode ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapId, mode))
                    },
                    onNavigateToAiQuiz = { chapId ->
                        navController.navigate(Screen.AiQuizGenerator.createRoute(chapId))
                    }
                )
            }

            composable(Screen.PreviousYearPapers.route) {
                PreviousYearPapersScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSolvePaper = { year ->
                        navController.navigate(Screen.MockTests.route)
                    },
                    onNavigateToChapterPyq = { chapterId ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapterId, "PYQ"))
                    }
                )
            }

            composable(Screen.MockTests.route) {
                MockTestScreen(
                    testTitle = "NEET Full Mock Test",
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPractice = { chapterId, mode ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapterId, mode))
                    }
                )
            }

            composable(Screen.MyPractice.route) {
                MyPracticeScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Bookmarks.route) {
                BookmarksScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.WeakTopics.route) {
                WeakTopicsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPracticeTopic = { chapterId, mode ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapterId, mode))
                    }
                )
            }

            composable(Screen.ChapterDetail.route) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: "neet_phy_current_electricity"
                ChapterDetailScreen(
                    chapterId = chapterId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPractice = { chapId, mode ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapId, mode))
                    },
                    onNavigateToAiQuiz = { chapId ->
                        navController.navigate(Screen.AiQuizGenerator.createRoute(chapId))
                    }
                )
            }

            composable(Screen.ChapterPractice.route) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: "neet_phy_current_electricity"
                val mode = backStackEntry.arguments?.getString("mode") ?: "PRACTICE"
                ChapterPracticeScreen(
                    chapterId = chapterId,
                    mode = mode,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AiQuizGenerator.route) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: "neet_phy_current_electricity"
                AiQuizGeneratorScreen(
                    chapterId = chapterId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Performance.route) {
                PerformanceScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToFocusAnalytics = { navController.navigate(Screen.FocusAnalytics.route) },
                    onNavigateToLearningAnalytics = { navController.navigate(Screen.LearningAnalytics.route) },
                    onNavigateToSubjectPerformance = { subjectName ->
                        navController.navigate(Screen.SubjectPerformance.createRoute(subjectName))
                    },
                    onNavigateToWeakTopics = { navController.navigate(Screen.WeakTopics.route) },
                    onNavigateToRecentActivity = { navController.navigate(Screen.RecentActivity.route) },
                    onNavigateToTestResults = { quizId ->
                        navController.navigate(Screen.TestResults.createRoute(quizId))
                    },
                    onNavigateToAiCoach = { navController.navigate(Screen.AiPerformanceCoach.route) },
                    onNavigateToProgressComparison = { navController.navigate(Screen.ProgressComparison.route) },
                    onNavigateToGoalsAndInsights = { navController.navigate(Screen.GoalsAndInsights.route) },
                    onPracticeTopic = { chapterId, mode ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapterId, mode))
                    }
                )
            }

            composable(Screen.FocusAnalytics.route) {
                FocusAnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.LearningAnalytics.route) {
                LearningAnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onStartPractice = { chapterId ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapterId, "PRACTICE"))
                    }
                )
            }

            composable(Screen.SubjectPerformance.route) { backStackEntry ->
                val subjectName = backStackEntry.arguments?.getString("subjectName") ?: "Physics"
                SubjectPerformanceScreen(
                    subjectName = subjectName,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChapter = { chapterId ->
                        navController.navigate(Screen.ChapterDetail.createRoute(chapterId))
                    }
                )
            }

            composable(Screen.RecentActivity.route) {
                RecentActivityScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToQuizResult = { quizId ->
                        navController.navigate(Screen.TestResults.createRoute(quizId))
                    }
                )
            }

            composable(Screen.TestResults.route) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getString("quizId")?.toLongOrNull() ?: 1L
                TestResultsScreen(
                    quizId = quizId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPracticeWeakAreas = { chapterId ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapterId, "WEAK_TOPICS"))
                    }
                )
            }

            composable(Screen.AiPerformanceCoach.route) {
                AiPerformanceCoachScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onStartPractice = { chapterId ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapterId, "PRACTICE"))
                    }
                )
            }

            composable(Screen.ProgressComparison.route) {
                ProgressComparisonScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.GoalsAndInsights.route) {
                GoalsAndInsightsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Focus.route) {
                FocusSessionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CompletionReward.route) {
                CompletionRewardScreen(
                    subjectName = "Machine Learning",
                    taskName = "Feature Scaling & KNN",
                    focusedMinutes = 90,
                    onReturnHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) },
                    onNavigateToVoiceStudio = { navController.navigate(Screen.VoiceStudio.route) }
                )
            }

            composable(Screen.Subjects.route) {
                SubjectsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.VoiceStudio.route) {
                VoiceNotesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}


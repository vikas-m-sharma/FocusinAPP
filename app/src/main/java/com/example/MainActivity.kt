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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavItems
import com.example.ui.screens.AiPerformanceCoachScreen
import com.example.ui.screens.AiQuizGeneratorScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.ChapterDetailScreen
import com.example.ui.screens.ChapterOptionsScreen
import com.example.ui.screens.ChapterPracticeScreen
import com.example.ui.screens.CompletionRewardScreen
import com.example.ui.screens.FocusAnalyticsScreen
import com.example.ui.screens.FocusSessionScreen
import com.example.ui.screens.GoalsAndInsightsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearningAnalyticsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MockTestScreen
import com.example.ui.screens.MyPracticeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PerformanceScreen
import com.example.ui.screens.PreparationScreen
import com.example.ui.screens.PrepareScreen
import com.example.ui.screens.PreviousYearPapersScreen
import com.example.ui.screens.ProgressComparisonScreen
import com.example.ui.screens.QuestionBankScreen
import com.example.ui.screens.RecentActivityScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubjectChaptersScreen
import com.example.ui.screens.SubjectPerformanceScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TestResultsScreen
import com.example.ui.screens.VoiceNotesScreen
import com.example.ui.screens.WeakTopicsScreen
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
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    // Determine if bottom navigation should be visible (5 primary tabs)
    val showBottomNav = isUserLoggedIn && currentRoute in listOf(
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
                    containerColor = Slate900,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { iconVector ->
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Slate950,
                                selectedTextColor = CyanPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = CyanPrimary
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
            startDestination = if (!isUserLoggedIn) Screen.Login.route else Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = { goals, dailyHours, schedule ->
                        viewModel.completeOnboarding(goals, dailyHours, schedule)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    onGoogleSignIn = { name, email ->
                        viewModel.signInWithGoogle(name, email)
                        viewModel.completeOnboarding("Study, Programming", 6, "Morning")
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
                    onNavigateToPreparation = { navController.navigate(Screen.Preparation.route) },
                    onOpenCreateSession = { navController.navigate(Screen.Schedule.route) }
                )
            }

            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    viewModel = viewModel,
                    onStartSession = { session ->
                        viewModel.startFocusSession(
                            subjectId = session.subjectId,
                            subjectName = session.subjectName,
                            taskName = session.taskName,
                            durationMinutes = session.durationMinutes,
                            mode = "COUNTDOWN"
                        )
                        navController.navigate(Screen.Focus.route)
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Performance.route) {
                PerformanceScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToFocusAnalytics = { navController.navigate(Screen.FocusAnalytics.route) },
                    onNavigateToLearningAnalytics = { navController.navigate(Screen.LearningAnalytics.route) },
                    onNavigateToSubjectPerformance = { subject ->
                        navController.navigate(Screen.SubjectPerformance.createRoute(subject))
                    },
                    onNavigateToWeakTopics = { navController.navigate(Screen.WeakTopics.route) },
                    onNavigateToRecentActivity = { navController.navigate(Screen.RecentActivity.route) },
                    onNavigateToTestResults = { quizId ->
                        navController.navigate(Screen.TestResults.createRoute(quizId))
                    },
                    onNavigateToAiCoach = { navController.navigate(Screen.AiPerformanceCoach.route) },
                    onNavigateToProgressComparison = { navController.navigate(Screen.ProgressComparison.route) },
                    onNavigateToGoalsAndInsights = { navController.navigate(Screen.GoalsAndInsights.route) },
                    onPracticeTopic = { chapId, mode ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapId, mode))
                    }
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
                    onNavigateToVoiceStudio = { navController.navigate(Screen.VoiceStudio.route) },
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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

            composable(Screen.Preparation.route) {
                PrepareScreen(
                    viewModel = viewModel,
                    onNavigateToSubject = { subjectId ->
                        navController.navigate(Screen.SubjectChapters.createRoute(subjectId))
                    },
                    onNavigateToChapter = { chapterId ->
                        navController.navigate(Screen.ChapterDetail.createRoute(chapterId))
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Prepare.route) {
                PrepareScreen(
                    viewModel = viewModel,
                    onNavigateToSubject = { subjectId ->
                        navController.navigate(Screen.SubjectChapters.createRoute(subjectId))
                    },
                    onNavigateToChapter = { chapterId ->
                        navController.navigate(Screen.ChapterDetail.createRoute(chapterId))
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(
                route = Screen.SubjectChapters.route,
                arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId") ?: "PHYSICS"
                SubjectChaptersScreen(
                    subjectId = subjectId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChapter = { chapterId ->
                        navController.navigate(Screen.ChapterOptions.createRoute(chapterId))
                    }
                )
            }

            composable(
                route = Screen.ChapterDetail.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
            ) { backStackEntry ->
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

            composable(Screen.QuestionBank.route) {
                QuestionBankScreen(
                    viewModel = viewModel,
                    onNavigateToSubject = { subjectId ->
                        navController.navigate(Screen.SubjectChapters.createRoute(subjectId))
                    },
                    onNavigateToPyq = {
                        navController.navigate(Screen.PreviousYearPapers.route)
                    },
                    onNavigateToAiQuiz = { chapterId ->
                        navController.navigate(Screen.AiQuizGenerator.createRoute(chapterId))
                    },
                    onNavigateToMockTest = { testTitle ->
                        navController.navigate(Screen.MockTest.createRoute(testTitle))
                    },
                    onNavigateToMyPractice = {
                        navController.navigate(Screen.MyPractice.route)
                    },
                    onNavigateToBookmarks = {
                        navController.navigate(Screen.Bookmarks.route)
                    },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.PreviousYearPapers.route) {
                PreviousYearPapersScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSolvePaper = { testTitle ->
                        navController.navigate(Screen.MockTest.createRoute(testTitle))
                    },
                    onNavigateToChapterPyq = { chapterId ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapterId, "pyq"))
                    }
                )
            }

            composable(
                route = Screen.AiQuizGenerator.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: "neet_phy_current_electricity"
                AiQuizGeneratorScreen(
                    chapterId = chapterId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.MockTest.route,
                arguments = listOf(navArgument("testTitle") { type = NavType.StringType })
            ) { backStackEntry ->
                val testTitle = backStackEntry.arguments?.getString("testTitle") ?: "NEET Full Syllabus Test 1"
                MockTestScreen(
                    testTitle = testTitle,
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
                    onPracticeTopic = { chapId, mode ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapId, mode))
                    }
                )
            }

            composable(
                route = Screen.ChapterOptions.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
            ) { backStackEntry ->
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

            composable(
                route = Screen.ChapterPractice.route,
                arguments = listOf(
                    navArgument("chapterId") { type = NavType.StringType },
                    navArgument("mode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getString("chapterId") ?: "neet_phy_current_electricity"
                val mode = backStackEntry.arguments?.getString("mode") ?: "topic"
                ChapterPracticeScreen(
                    chapterId = chapterId,
                    mode = mode,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.TestResults.route,
                arguments = listOf(navArgument("quizId") { type = NavType.LongType })
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getLong("quizId") ?: 0L
                TestResultsScreen(
                    quizId = quizId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPracticeWeakAreas = {
                        navController.navigate(Screen.WeakTopics.route)
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
                    onStartPractice = { chapId ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapId, "practice"))
                    }
                )
            }

            composable(
                route = Screen.SubjectPerformance.route,
                arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
            ) { backStackEntry ->
                val subjectName = backStackEntry.arguments?.getString("subjectName") ?: "Physics"
                SubjectPerformanceScreen(
                    subjectName = subjectName,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChapter = { chapId ->
                        navController.navigate(Screen.ChapterDetail.createRoute(chapId))
                    },
                    onNavigateToChapterPractice = { chapId ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapId, "practice"))
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

            composable(Screen.AiPerformanceCoach.route) {
                AiPerformanceCoachScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onStartPractice = { chapId ->
                        navController.navigate(Screen.ChapterPractice.createRoute(chapId, "practice"))
                    }
                )
            }
        }
    }
}


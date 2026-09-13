package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MutedGold
import com.example.ui.theme.MutedGoldDark
import com.example.ui.theme.RoseError
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.WarningDark
import com.example.viewmodel.FocusinViewModel

@Composable
fun PerformanceScreen(
    viewModel: FocusinViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFocusAnalytics: () -> Unit = {},
    onNavigateToLearningAnalytics: () -> Unit = {},
    onNavigateToSubjectPerformance: (String) -> Unit = {},
    onNavigateToWeakTopics: () -> Unit = {},
    onNavigateToRecentActivity: () -> Unit = {},
    onNavigateToTestResults: (Long) -> Unit = {},
    onNavigateToAiCoach: () -> Unit = {},
    onNavigateToProgressComparison: () -> Unit = {},
    onNavigateToGoalsAndInsights: () -> Unit = {},
    onPracticeTopic: (String, String) -> Unit = { _, _ -> },
    onNavigateToMistakeDiary: () -> Unit = {},
    onNavigateToQBank: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {}
) {
    var viewPeriod by remember { mutableStateOf("WEEK") } // "WEEK", "MONTH", "YEAR"

    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
    val recentWeekStats by viewModel.recentWeekStats.collectAsState()
    val recentMonthStats by viewModel.recentMonthStats.collectAsState()
    val recentYearStats by viewModel.recentYearStats.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val totalQuestionsAttempted by viewModel.totalQuestionsAttempted.collectAsState()
    val totalQuestionsCorrect by viewModel.totalQuestionsCorrect.collectAsState()
    val quizAttempts by viewModel.allQuizAttempts.collectAsState()
    val allQuestionAttempts by viewModel.allQuestionAttempts.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val todaySessions by viewModel.todaySessions.collectAsState()

    val activeStats = when (viewPeriod) {
        "MONTH" -> recentMonthStats
        "YEAR" -> recentYearStats
        else -> recentWeekStats
    }

    val totalFocused = activeStats.sumOf { it.totalFocusedMinutes }

    // Check if the user has real learning activity recorded
    val hasLearningActivity = (totalQuestionsAttempted > 0 || quizAttempts.isNotEmpty() || totalFocused > 0)
    val showRealPerformance = isUserLoggedIn && hasLearningActivity

    // Real Focus values
    val displayHours = totalFocused / 60
    val displayMins = totalFocused % 60
    val targetHours = when (viewPeriod) {
        "MONTH" -> 100
        "YEAR" -> 1200
        else -> 25
    }
    val displayCompletionPercent = if (targetHours > 0 && totalFocused > 0) {
        ((totalFocused.toFloat() / (targetHours * 60)) * 100).toInt().coerceIn(0, 100)
    } else 0

    val displayScore = todayStats?.focusScore ?: if (totalFocused > 0) 80 else 0
    val displayStreak = userSettings?.currentStreak ?: 0
    val completedSessionsCount = todaySessions.count { it.isCompleted }
    val displayScheduleDone = if (todaySessions.isNotEmpty()) {
        "${((completedSessionsCount.toFloat() / todaySessions.size) * 100).toInt()}%"
    } else if (totalFocused > 0) "100%" else "0%"

    // Real Learning Performance stats
    val displayQuestions = totalQuestionsAttempted
    val calculatedAccuracy = if (totalQuestionsAttempted > 0) {
        ((totalQuestionsCorrect.toFloat() / totalQuestionsAttempted) * 100).toInt()
    } else 0
    val displayAccuracy = calculatedAccuracy
    val displayTests = quizAttempts.size

    // Real Subject-wise stats
    val physicsAttempts = allQuestionAttempts.filter { it.subjectName.equals("Physics", ignoreCase = true) }
    val physicsAcc = if (physicsAttempts.isNotEmpty()) {
        ((physicsAttempts.count { it.isCorrect }.toFloat() / physicsAttempts.size) * 100).toInt()
    } else 0

    val chemAttempts = allQuestionAttempts.filter { it.subjectName.equals("Chemistry", ignoreCase = true) }
    val chemAcc = if (chemAttempts.isNotEmpty()) {
        ((chemAttempts.count { it.isCorrect }.toFloat() / chemAttempts.size) * 100).toInt()
    } else 0

    val bioAttempts = allQuestionAttempts.filter {
        it.subjectName.contains("Bio", ignoreCase = true) ||
                it.subjectName.contains("Botany", ignoreCase = true) ||
                it.subjectName.contains("Zoology", ignoreCase = true)
    }
    val bioAcc = if (bioAttempts.isNotEmpty()) {
        ((bioAttempts.count { it.isCorrect }.toFloat() / bioAttempts.size) * 100).toInt()
    } else 0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Slate950 // Classic Deep Calm Background (#101722)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar: "Performance" + "Track progress. Stay motivated."
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Performance",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Slate100,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Track progress. Stay motivated.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate400,
                        fontSize = 12.sp
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!showRealPerformance) {
                    // ========================================================
                    // EMPTY / STARTER STATE (Only shows when no learning data exists)
                    // ========================================================
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("perf_starter_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            border = BorderStroke(1.dp, Slate800)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MutedGoldDark.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = MutedGoldDark,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "No Learning Activity Yet",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Slate100,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (!isUserLoggedIn) {
                                        "Please sign in and start practicing questions, completing mock tests, or running focus timers to unlock personalized performance analytics."
                                    } else {
                                        "Your study analytics, accuracy charts, and subject breakdown will appear here once you solve PYQs or complete a study session."
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate400,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Action 1: Practice Questions
                                Button(
                                    onClick = onNavigateToQBank,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("perf_btn_start_practice"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MutedGold,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Practice Questions (QBank)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action 2: Start Focus Timer
                                Button(
                                    onClick = onNavigateToFocus,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("perf_btn_start_focus"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SecondaryNavy,
                                        contentColor = Slate100
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = MutedGoldDark)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start Focus Session", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    // ========================================================
                    // REAL PERFORMANCE DASHBOARD (When user has learning data)
                    // ========================================================

                    // 1. Period Selector: Week | Month | Year
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PeriodPill(
                                label = "Week",
                                isSelected = viewPeriod == "WEEK",
                                onClick = { viewPeriod = "WEEK" }
                            )
                            PeriodPill(
                                label = "Month",
                                isSelected = viewPeriod == "MONTH",
                                onClick = { viewPeriod = "MONTH" }
                            )
                            PeriodPill(
                                label = "Year",
                                isSelected = viewPeriod == "YEAR",
                                onClick = { viewPeriod = "YEAR" }
                            )
                        }
                    }

                    // 2. Card 1: Focus Performance
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateToFocusAnalytics)
                                .testTag("perf_card_focus"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            border = BorderStroke(1.dp, Slate800)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "Focus Performance",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Slate100,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Focus hours and percentage row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${displayHours}h ${displayMins}m / ${targetHours}h",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = Slate100,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    )
                                    Text(
                                        text = "$displayCompletionPercent%",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MutedGoldDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Classic Progress Indicator
                                LinearProgressIndicator(
                                    progress = { (displayCompletionPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = MutedGoldDark,
                                    trackColor = Slate800
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // 3 Badges in a row: Focus Score, Day Streak, Schedule Done
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Badge 1: Focus Score
                                    StatBadgeItem(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.Shield,
                                        iconTint = EmeraldSuccess,
                                        value = "$displayScore",
                                        label = "Focus Score"
                                    )

                                    // Badge 2: Day Streak
                                    StatBadgeItem(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.Whatshot,
                                        iconTint = MutedGoldDark,
                                        value = "$displayStreak",
                                        label = "Day Streak"
                                    )

                                    // Badge 3: Schedule Done
                                    StatBadgeItem(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.PieChart,
                                        iconTint = WarningDark,
                                        value = displayScheduleDone,
                                        label = "Schedule Done"
                                    )
                                }
                            }
                        }
                    }

                    // 3. Card 2: Learning Performance
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateToLearningAnalytics)
                                .testTag("perf_card_learning"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            border = BorderStroke(1.dp, Slate800)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "Learning Performance",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Slate100,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Column 1: Questions
                                    LearningMetricItem(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.HelpOutline,
                                        iconTint = MutedGoldDark,
                                        iconBg = MutedGoldDark.copy(alpha = 0.15f),
                                        value = "$displayQuestions",
                                        label = "Questions"
                                    )

                                    // Column 2: Accuracy
                                    LearningMetricItem(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.Check,
                                        iconTint = EmeraldSuccess,
                                        iconBg = EmeraldSuccess.copy(alpha = 0.15f),
                                        value = "$displayAccuracy%",
                                        label = "Accuracy"
                                    )

                                    // Column 3: Tests Completed
                                    LearningMetricItem(
                                        modifier = Modifier.weight(1f),
                                        icon = Icons.Default.Quiz,
                                        iconTint = WarningDark,
                                        iconBg = WarningDark.copy(alpha = 0.15f),
                                        value = "$displayTests",
                                        label = "Tests Completed"
                                    )
                                }
                            }
                        }
                    }

                    // 4. Card 3: Subject Performance
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToSubjectPerformance("Physics") }
                                .testTag("perf_card_subjects"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            border = BorderStroke(1.dp, Slate800)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "Subject Performance",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Slate100,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Physics Row
                                SubjectPerformanceBarRow(
                                    name = "Physics",
                                    icon = Icons.Default.ElectricBolt,
                                    iconTint = MutedGoldDark,
                                    progress = physicsAcc / 100f,
                                    percentageText = if (physicsAttempts.isNotEmpty()) "$physicsAcc%" else "0%",
                                    barColor = MutedGoldDark,
                                    onClick = { onNavigateToSubjectPerformance("Physics") }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Chemistry Row
                                SubjectPerformanceBarRow(
                                    name = "Chemistry",
                                    icon = Icons.Default.Science,
                                    iconTint = WarningDark,
                                    progress = chemAcc / 100f,
                                    percentageText = if (chemAttempts.isNotEmpty()) "$chemAcc%" else "0%",
                                    barColor = WarningDark,
                                    onClick = { onNavigateToSubjectPerformance("Chemistry") }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Biology Row
                                SubjectPerformanceBarRow(
                                    name = "Biology",
                                    icon = Icons.Default.Spa,
                                    iconTint = EmeraldSuccess,
                                    progress = bioAcc / 100f,
                                    percentageText = if (bioAttempts.isNotEmpty()) "$bioAcc%" else "0%",
                                    barColor = EmeraldSuccess,
                                    onClick = { onNavigateToSubjectPerformance("Biology") }
                                )
                            }
                        }
                    }
                }

                // 5. Quote Footer
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\"Progress is the result of consistent effort.\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate500,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 6. Quick Access Navigation Hub (explore insights)
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "More Insights & Tools",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Slate400,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HubQuickButton(
                                modifier = Modifier.weight(1f),
                                label = "Weak Topics",
                                icon = Icons.Default.WarningAmber,
                                tint = WarningDark,
                                onClick = onNavigateToWeakTopics
                            )
                            HubQuickButton(
                                modifier = Modifier.weight(1f),
                                label = "Activity Log",
                                icon = Icons.Default.History,
                                tint = MutedGoldDark,
                                onClick = onNavigateToRecentActivity
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HubQuickButton(
                                modifier = Modifier.weight(1f),
                                label = "Comparison",
                                icon = Icons.Default.BarChart,
                                tint = MutedGold,
                                onClick = onNavigateToProgressComparison
                            )
                            HubQuickButton(
                                modifier = Modifier.weight(1f),
                                label = "Goals",
                                icon = Icons.Default.Flag,
                                tint = EmeraldSuccess,
                                onClick = onNavigateToGoalsAndInsights
                            )
                        }

                        HubQuickButton(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Mistake Diary (Galti Tracker) 🔥",
                            icon = Icons.Default.Whatshot,
                            tint = RoseError,
                            onClick = onNavigateToMistakeDiary
                        )

                        HubQuickButton(
                            modifier = Modifier.fillMaxWidth(),
                            label = "AI Performance Coach",
                            icon = Icons.Default.AutoAwesome,
                            tint = MutedGoldDark,
                            onClick = onNavigateToAiCoach
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun PeriodPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MutedGold else Slate900)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Slate400,
                fontSize = 13.sp
            )
        )
    }
}

@Composable
private fun StatBadgeItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = Slate100,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate400,
                fontSize = 11.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LearningMetricItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    value: String,
    label: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = Slate100,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate400,
                fontSize = 11.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SubjectPerformanceBarRow(
    name: String,
    icon: ImageVector,
    iconTint: Color,
    progress: Float,
    percentageText: String,
    barColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Slate100,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f)
        )
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .width(110.dp)
                .height(6.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = Slate800
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = percentageText,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Slate100,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )
    }
}

@Composable
private fun HubQuickButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.dp, Slate800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate100,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Slate500,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

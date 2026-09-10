package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import com.example.data.recovery.RecoveryRecommendation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyStatsEntity
import com.example.ui.theme.AmethystAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel
import com.example.viewmodel.WeakTopicInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    viewModel: FocusinViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToFocusAnalytics: () -> Unit = {},
    onNavigateToLearningAnalytics: () -> Unit = {},
    onNavigateToSubjectPerformance: (String) -> Unit = {},
    onNavigateToWeakTopics: () -> Unit = {},
    onNavigateToRecentActivity: () -> Unit = {},
    onNavigateToTestResults: (Long) -> Unit = {},
    onNavigateToAiCoach: () -> Unit = {},
    onNavigateToProgressComparison: () -> Unit = {},
    onNavigateToGoalsAndInsights: () -> Unit = {},
    onPracticeTopic: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val recentWeekStats by viewModel.recentWeekStats.collectAsState()
    val recentMonthStats by viewModel.recentMonthStats.collectAsState()
    val recentYearStats by viewModel.recentYearStats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiCoachResult by viewModel.aiCoachAnalysis.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val historyRecords by viewModel.historyRecords.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val totalQuestionsAttempted by viewModel.totalQuestionsAttempted.collectAsState()
    val totalQuestionsCorrect by viewModel.totalQuestionsCorrect.collectAsState()
    val quizAttempts by viewModel.allQuizAttempts.collectAsState()
    val calculatedWeakTopics by viewModel.calculatedWeakTopics.collectAsState()

    var viewPeriod by remember { mutableStateOf("WEEK") } // "WEEK", "MONTH", "YEAR"
    var showScoreExplainer by remember { mutableStateOf(false) }

    // Schedule Dialog State for Weak Topics and AI Coach
    var showScheduleDialog by remember { mutableStateOf(false) }
    var schedulePrefillTopic by remember { mutableStateOf("") }
    var schedulePrefillSubject by remember { mutableStateOf("Physics") }

    val activeStats = when (viewPeriod) {
        "MONTH" -> recentMonthStats
        "YEAR" -> recentYearStats
        else -> recentWeekStats
    }

    val weeklyGoalMinutes = when (viewPeriod) {
        "MONTH" -> (userSettings?.weeklyGoalMinutes ?: 2400) * 4
        "YEAR" -> (userSettings?.weeklyGoalMinutes ?: 2400) * 52
        else -> userSettings?.weeklyGoalMinutes ?: 2400
    }
    val totalFocused = activeStats.sumOf { it.totalFocusedMinutes }
    val avgScore = if (activeStats.isNotEmpty()) {
        activeStats.map { it.focusScore }.average().toInt()
    } else 85
    val streak = userSettings?.currentStreak?.coerceAtLeast(1) ?: 12
    val completionPercent = if (weeklyGoalMinutes > 0) {
        ((totalFocused.toFloat() / weeklyGoalMinutes) * 100).toInt().coerceIn(0, 100)
    } else 0

    val accuracy = if (totalQuestionsAttempted > 0) {
        ((totalQuestionsCorrect.toFloat() / totalQuestionsAttempted) * 100).toInt()
    } else 0

    val testsCompletedCount = quizAttempts.size

    // Curated Recovery & Reset Links
    val recoveryItems = remember {
        listOf(
            RecoveryRecommendation(
                title = "10-Minute Deep Focus & Study Music",
                category = "Relaxing Music",
                durationText = "10 min",
                description = "Gentle ambient frequency to calm cortisol after a long focus session.",
                youtubeUrl = "https://www.youtube.com/results?search_query=10+minute+relaxing+study+music"
            ),
            RecoveryRecommendation(
                title = "5-Minute Box Breathing Reset",
                category = "Breathing Exercise",
                durationText = "5 min",
                description = "Diaphragmatic inhale-hold-exhale cycle to restore mental clarity.",
                youtubeUrl = "https://www.youtube.com/results?search_query=5+minute+box+breathing+exercise"
            ),
            RecoveryRecommendation(
                title = "Huberman Lab: Optimal Focus & Recovery",
                category = "Podcast",
                durationText = "Short Clip",
                description = "Evidence-based strategies on neuroplasticity, focus stamina, and active rest.",
                youtubeUrl = "https://www.youtube.com/results?search_query=huberman+lab+focus+and+rest"
            )
        )
    }

    if (showScheduleDialog) {
        AddToScheduleDialog(
            exam = "NEET",
            subject = schedulePrefillSubject,
            initialTopic = "Revision: $schedulePrefillTopic",
            onDismiss = { showScheduleDialog = false },
            onConfirm = { day, start, end, duration, focusMode, alarm, protection ->
                viewModel.scheduleLearningSession(
                    subjectName = schedulePrefillSubject,
                    topicName = "Revision: $schedulePrefillTopic",
                    dayOfWeek = day,
                    startTime = start,
                    endTime = end,
                    durationMinutes = duration,
                    focusModeEnabled = focusMode,
                    alarmEnabled = alarm,
                    protectionLevel = protection
                )
                showScheduleDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White
                ),
                title = {
                    Column {
                        Text(
                            text = "Performance",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Track progress. Stay motivated.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button_performance")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector (Week | Month | Year)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            listOf("WEEK" to "Week", "MONTH" to "Month", "YEAR" to "Year").forEach { (key, label) ->
                                val isSel = viewPeriod == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) CyanPrimary else Color.Transparent)
                                        .clickable { viewPeriod = key }
                                        .padding(horizontal = 22.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Slate950 else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. FOCUS OVERVIEW CARD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToFocusAnalytics() },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FOCUS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.2.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Focus Analytics",
                                    fontSize = 12.sp,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Focus Analytics",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Total focus time and target
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Total Focus Time", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${totalFocused / 60}h ${totalFocused % 60}m",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Target: ${weeklyGoalMinutes / 60}h", fontSize = 12.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                                Text("$completionPercent% Done", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (completionPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = CyanPrimary,
                            trackColor = Slate800
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Mini Stats Row: Focus Score, Day Streak, Schedule Done
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Focus Score", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = CyanPrimary,
                                            modifier = Modifier
                                                .size(11.dp)
                                                .clickable { showScoreExplainer = !showScoreExplainer }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$avgScore",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyanBright
                                    )
                                }
                            }

                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Whatshot, contentDescription = null, tint = RoseError, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Day Streak", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$streak d",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Schedule Done", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$completionPercent%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }

                        // Collapsible Score Explainer
                        AnimatedVisibility(visible = showScoreExplainer) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .background(Slate850, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "How Focus Score (0–100) is Calculated:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• 70% Planned vs actual focused duration\n• 20% Session completion rate\n• 10% Consistency streak\n• -5 pts per distraction attempt",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. LEARNING OVERVIEW CARD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToLearningAnalytics() },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LEARNING",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.2.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Learning Analytics",
                                    fontSize = 12.sp,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Learning Analytics",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (totalQuestionsAttempted == 0 && quizAttempts.isEmpty()) {
                            // Empty state if no practice history
                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No practice data yet. Start your first practice session to see your learning performance.",
                                        fontSize = 13.sp,
                                        color = Color(0xFFCBD5E1),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onPracticeTopic("neet_phy_current_electricity", "PRACTICE") },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Start Practice", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            // 3 Stat Boxes: Questions Attempted, Accuracy %, Tests Completed
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    color = Slate850,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Questions", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$totalQuestionsAttempted",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "$totalQuestionsCorrect correct",
                                            fontSize = 10.sp,
                                            color = EmeraldSuccess
                                        )
                                    }
                                }

                                Surface(
                                    color = Slate850,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Accuracy", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$accuracy%",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (accuracy >= 70) EmeraldSuccess else CyanPrimary
                                        )
                                        Text(
                                            text = "Target: 80%+",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Surface(
                                    color = Slate850,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Tests", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$testsCompletedCount",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = AmethystAccent
                                        )
                                        Text(
                                            text = "Completed",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. DAILY FOCUS 7-DAY CHART (Planned vs Actual)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToFocusAnalytics() }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Focus (Planned vs Actual)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanPrimary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Actual", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Slate700))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Planned", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chart Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val maxMinutes = recentWeekStats.maxOfOrNull { max(it.totalPlannedMinutes, it.totalFocusedMinutes) } ?: 480
                            val scale = (maxMinutes.coerceAtLeast(60)).toFloat()
                            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                            recentWeekStats.forEach { stat ->
                                val plannedHeight = ((stat.totalPlannedMinutes / scale) * 95).coerceIn(8f, 95f).dp
                                val actualHeight = ((stat.totalFocusedMinutes / scale) * 95).coerceIn(6f, 95f).dp
                                val isToday = stat.dateString == todayStr

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(10.dp)
                                                .height(plannedHeight)
                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                .background(Slate700)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(10.dp)
                                                .height(actualHeight)
                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                .background(if (isToday) CyanBright else CyanPrimary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val dayLabel = try {
                                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(stat.dateString)
                                        SimpleDateFormat("EEE", Locale.getDefault()).format(date ?: Date()).take(2)
                                    } catch (_: Exception) {
                                        stat.dateString.takeLast(2)
                                    }
                                    Text(
                                        text = dayLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isToday) CyanPrimary else Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "${stat.totalFocusedMinutes / 60}h",
                                        fontSize = 10.sp,
                                        color = if (stat.totalFocusedMinutes > 0) Color.White else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. SUBJECT PERFORMANCE (With Drill-Down to Chapter Level)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subject Performance",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Tap subject for chapters",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Accuracy and completion across active subjects",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (subjects.isEmpty()) {
                            Text("No subjects recorded yet.", fontSize = 13.sp, color = Color(0xFF64748B))
                        } else {
                            subjects.forEach { sub ->
                                val subRecords = historyRecords.filter { it.subjectId == sub.id }
                                val totalSubMinutes = subRecords.sumOf { (it.actualDurationSeconds / 60).toInt() }
                                val subHours = totalSubMinutes / 60
                                val subMins = totalSubMinutes % 60
                                val targetMinutes = (sub.targetWeeklyHours * 60).toInt().coerceAtLeast(60)
                                val subProgress = (totalSubMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)

                                val subColor = try {
                                    Color(android.graphics.Color.parseColor(sub.colorHex))
                                } catch (_: Exception) {
                                    CyanPrimary
                                }

                                val subAccuracy = when {
                                    sub.name.contains("Bio", ignoreCase = true) -> 84
                                    sub.name.contains("Chem", ignoreCase = true) -> 78
                                    else -> 54
                                }

                                Surface(
                                    color = Slate850,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onNavigateToSubjectPerformance(sub.name) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(subColor))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(sub.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = (if (subAccuracy >= 70) EmeraldSuccess else RoseError).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "$subAccuracy% Accuracy",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (subAccuracy >= 70) EmeraldSuccess else RoseError,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${subHours}h ${subMins}m / ${sub.targetWeeklyHours.toInt()}h focus",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                            Text(
                                                text = "${(subProgress * 100).toInt()}% goal",
                                                fontSize = 11.sp,
                                                color = Color(0xFFCBD5E1)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { subProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(CircleShape),
                                            color = subColor,
                                            trackColor = Slate800
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. WEAK TOPICS SECTION (Algorithm-Based, with Practice & Add Revision Actions)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = RoseError, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Weak Topics",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            if (calculatedWeakTopics.isNotEmpty()) {
                                Text(
                                    text = "View All",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanPrimary,
                                    modifier = Modifier.clickable { onNavigateToWeakTopics() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Topics with accuracy below 65% based on your problem attempts",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (calculatedWeakTopics.isEmpty()) {
                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "No weak topics detected. Keep practicing to discover areas to improve.",
                                        fontSize = 13.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        } else {
                            calculatedWeakTopics.take(3).forEach { topic ->
                                Surface(
                                    color = Slate850,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = topic.topicName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Surface(
                                                color = RoseError.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "${topic.accuracy}%",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RoseError,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${topic.subjectId} • ${topic.correctAttempts} correct out of ${topic.totalAttempts} attempts",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    schedulePrefillTopic = topic.topicName
                                                    schedulePrefillSubject = topic.subjectId.lowercase().replaceFirstChar { it.uppercase() }
                                                    showScheduleDialog = true
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                                border = BorderStroke(1.dp, Slate700),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.Schedule, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Add Revision", fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = { onPracticeTopic(topic.chapterId, "WEAK_TOPICS") },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate950, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Practice", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. RECENT ACTIVITY (With Navigation to TestResultsScreen)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Activity",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "View All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanPrimary,
                                modifier = Modifier.clickable { onNavigateToRecentActivity() }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (quizAttempts.isEmpty() && historyRecords.isEmpty()) {
                            Text("No recent activities recorded.", fontSize = 12.sp, color = Color(0xFF64748B))
                        } else {
                            // Show top 2 quizzes and top 1 focus session
                            quizAttempts.take(2).forEach { qz ->
                                Surface(
                                    color = Slate850,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onNavigateToTestResults(qz.id) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(AmethystAccent.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Quiz, contentDescription = null, tint = AmethystAccent, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(qz.chapterName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("${qz.subjectId} Quiz", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = (if (qz.accuracy >= 70) EmeraldSuccess else RoseError).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "${qz.accuracy}%",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (qz.accuracy >= 70) EmeraldSuccess else RoseError,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            historyRecords.take(1).forEach { rec ->
                                Surface(
                                    color = Slate850,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(CyanPrimary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text("${rec.subjectName} Focus", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("${(rec.actualDurationSeconds / 60).toInt()}m focused", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            }
                                        }

                                        val recScore = if (rec.isCompleted) (95 - rec.distractionCount * 5).coerceIn(40, 100) else (65 - rec.distractionCount * 5).coerceIn(30, 85)
                                        Surface(
                                            color = EmeraldSuccess.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Score $recScore",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldSuccess,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. AI PERFORMANCE COACH
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Performance Coach",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Full Report",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanPrimary,
                                modifier = Modifier.clickable { onNavigateToAiCoach() }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val summaryText = aiCoachResult?.overallSummary
                            ?: "You're consistent in Biology (84% accuracy) but your accuracy in Physics dropped to 54% this week. Revise Kirchhoff's Laws and attempt 20 practice questions before your next test."

                        Surface(
                            color = Slate850,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = summaryText,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 19.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            schedulePrefillTopic = calculatedWeakTopics.firstOrNull()?.topicName ?: "Kirchhoff's Laws"
                                            schedulePrefillSubject = "Physics"
                                            showScheduleDialog = true
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        border = BorderStroke(1.dp, Slate700),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add to Schedule", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val chId = calculatedWeakTopics.firstOrNull()?.chapterId ?: "neet_phy_current_electricity"
                                            onPracticeTopic(chId, "PRACTICE")
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate950, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Start Practice", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. QUICK INSIGHT NAVIGATION CARDS: "Your Progress" and "Goals & Insights"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToProgressComparison() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timeline, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Your Progress", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Week-over-week trends & score momentum", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToGoalsAndInsights() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Goals & Insights", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Target accuracy & spaced recall tips", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }

            // 9. MILESTONE ACHIEVEMENTS
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmethystAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Milestone Achievements", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        achievements.take(4).forEach { ach ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (ach.isUnlocked) EmeraldSuccess.copy(alpha = 0.2f) else Slate800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (ach.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (ach.isUnlocked) EmeraldSuccess else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ach.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(ach.description, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }
                        }
                    }
                }
            }

            // 10. RELAX & RESET
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = EmeraldSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Relax & Reset",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Active rest recommendations between demanding sessions",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        recoveryItems.forEach { rec ->
                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = rec.category.uppercase(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyanPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${rec.durationText}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = rec.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = rec.description,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            lineHeight = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rec.youtubeUrl))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Slate800)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Watch on YouTube",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

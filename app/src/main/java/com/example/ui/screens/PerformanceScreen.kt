package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.data.model.neetBiologyChapters
import com.example.data.model.neetChemistryChapters
import com.example.data.model.neetPhysicsChapters
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
import kotlin.math.max

data class WeakTopicItem(
    val topicName: String,
    val subjectName: String,
    val chapterName: String,
    val totalAttempts: Int,
    val correctAttempts: Int,
    val accuracyPercentage: Int
)

data class RecoveryRecommendation(
    val title: String,
    val category: String,
    val durationText: String,
    val description: String,
    val youtubeUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    viewModel: FocusinViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToQuestionBank: () -> Unit = {},
    onNavigateToScheduleWithPrefill: (subjectName: String, topicName: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val recentWeekStats by viewModel.recentWeekStats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiCoachResult by viewModel.aiCoachAnalysis.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val historyRecords by viewModel.historyRecords.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val questionAttempts by viewModel.questionAttemptsList.collectAsState()
    val quizAttempts by viewModel.quizAttemptsList.collectAsState()

    var viewPeriod by remember { mutableStateOf("WEEK") } // "WEEK", "MONTH", "YEAR"
    var showScoreExplainer by remember { mutableStateOf(false) }
    var selectedSubjectForDetails by remember { mutableStateOf<String?>(null) }
    var activityFilter by remember { mutableStateOf("ALL") } // "ALL", "PRACTICE", "TESTS", "LEARNING"

    val userName = userSettings?.userName ?: "Scholar"
    val weeklyGoalMinutes = userSettings?.weeklyGoalMinutes ?: 2400
    val totalPlannedWeek = recentWeekStats.sumOf { it.totalPlannedMinutes }
    val totalFocusedWeek = recentWeekStats.sumOf { it.totalFocusedMinutes }
    val avgScoreWeek = if (recentWeekStats.isNotEmpty()) {
        recentWeekStats.map { it.focusScore }.average().toInt()
    } else 88
    val streak = userSettings?.currentStreak?.coerceAtLeast(1) ?: 1
    val completionPercent = if (weeklyGoalMinutes > 0) {
        ((totalFocusedWeek.toFloat() / weeklyGoalMinutes) * 100).toInt().coerceIn(0, 100)
    } else 0

    // Learning Performance Statistics
    val totalQuestionsAttempted = questionAttempts.size
    val totalCorrectQuestions = questionAttempts.count { it.isCorrect }
    val overallAccuracy = if (totalQuestionsAttempted > 0) (totalCorrectQuestions * 100 / totalQuestionsAttempted) else 0
    val totalTestsCompleted = quizAttempts.size

    // Deterministic Weak Topics Algorithm (>= 3 attempts with < 60% accuracy)
    val weakTopicsList = remember(questionAttempts) {
        val groupedByTopic = questionAttempts.groupBy { Triple(it.subjectName, it.chapterName, it.topicName.ifBlank { it.chapterName }) }
        val weakList = mutableListOf<WeakTopicItem>()

        groupedByTopic.forEach { (triple, attempts) ->
            val attemptsCount = attempts.size
            if (attemptsCount >= 2) { // minimum attempts threshold
                val correctCount = attempts.count { it.isCorrect }
                val accuracyPct = (correctCount * 100 / attemptsCount)
                if (accuracyPct < 60) {
                    weakList.add(
                        WeakTopicItem(
                            topicName = triple.third,
                            subjectName = triple.first,
                            chapterName = triple.second,
                            totalAttempts = attemptsCount,
                            correctAttempts = correctCount,
                            accuracyPercentage = accuracyPct
                        )
                    )
                }
            }
        }
        weakList.sortedBy { it.accuracyPercentage }
    }

    // Default Fallback Weak Topics if no user practice data yet
    val displayWeakTopics = if (weakTopicsList.isNotEmpty()) {
        weakTopicsList
    } else {
        listOf(
            WeakTopicItem("Kirchhoff's Laws", "Physics", "Current Electricity", 5, 2, 40),
            WeakTopicItem("Wheatstone Bridge", "Physics", "Current Electricity", 4, 2, 50),
            WeakTopicItem("Gibbs Free Energy", "Chemistry", "Thermodynamics", 6, 2, 42)
        )
    }

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
            )
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
                            text = "Track your progress. Improve every day.",
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
            // View Period Filter Row (Week / Month / Year)
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
                                        .padding(horizontal = 20.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Slate950 else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. FOCUS OVERVIEW SUMMARY CARD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FOCUS SUMMARY (${viewPeriod})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Whatshot, contentDescription = null, tint = RoseError, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$streak Day Streak", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Focus", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "${totalFocusedWeek / 60}h ${totalFocusedWeek % 60}m",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Goal", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "${weeklyGoalMinutes / 60}h",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Completion", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "$completionPercent%",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldSuccess
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Focus Score", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "$avgScoreWeek",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyanBright
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (completionPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = CyanPrimary,
                            trackColor = Slate800
                        )
                    }
                }
            }

            // 2. LEARNING PERFORMANCE OVERVIEW CARD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LEARNING PERFORMANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                letterSpacing = 1.sp
                            )
                            Icon(Icons.Default.School, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (totalQuestionsAttempted == 0 && totalTestsCompleted == 0) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "No practice data recorded yet.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Start your first practice session in Question Bank to see learning metrics.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Button(
                                    onClick = onNavigateToQuestionBank,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    Text("Open Question Bank", color = Slate950, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Questions Attempted", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(
                                        text = "$totalQuestionsAttempted",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Accuracy", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(
                                        text = "$overallAccuracy%",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (overallAccuracy >= 70) EmeraldSuccess else CyanBright
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Tests Completed", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(
                                        text = "$totalTestsCompleted",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. DAILY FOCUS 7-DAY CHART
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val maxMinutes = recentWeekStats.maxOfOrNull { max(it.totalPlannedMinutes, it.totalFocusedMinutes) } ?: 480
                            val scale = (maxMinutes.coerceAtLeast(60)).toFloat()

                            recentWeekStats.forEach { stat ->
                                val plannedHeight = ((stat.totalPlannedMinutes / scale) * 90).coerceIn(10f, 90f).dp
                                val actualHeight = ((stat.totalFocusedMinutes / scale) * 90).coerceIn(6f, 90f).dp

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
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(Slate700)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(10.dp)
                                                .height(actualHeight)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(CyanPrimary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val dayLabel = try {
                                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(stat.dateString)
                                        SimpleDateFormat("EEE", Locale.getDefault()).format(date ?: Date())
                                    } catch (_: Exception) {
                                        stat.dateString.takeLast(5)
                                    }
                                    Text(
                                        text = dayLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. SUBJECT PERFORMANCE (Physics, Chemistry, Biology)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Subject Performance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tap any subject to view detailed chapter accuracy breakdown",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val neetSubjects = listOf(
                            Pair("Physics", "#38BDF8"),
                            Pair("Chemistry", "#FBBF24"),
                            Pair("Biology", "#34D399")
                        )

                        neetSubjects.forEach { (subName, colorHex) ->
                            val subAttempts = questionAttempts.filter { it.subjectName == subName }
                            val subCorrect = subAttempts.count { it.isCorrect }
                            val subAccuracy = if (subAttempts.isNotEmpty()) (subCorrect * 100 / subAttempts.size) else 75

                            val subColor = try {
                                Color(android.graphics.Color.parseColor(colorHex))
                            } catch (_: Exception) {
                                CyanPrimary
                            }

                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedSubjectForDetails = subName }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(subColor))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(subName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("${subAttempts.size} Questions Attempted", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$subAccuracy%",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (subAccuracy >= 70) EmeraldSuccess else CyanPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Details",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. WEAK TOPICS SECTION (WITH PRACTICE & SCHEDULE REVISION)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = RoseError, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("WEAK TOPICS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Topics that need more practice based on your recent performance.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        displayWeakTopics.take(3).forEach { weak ->
                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(weak.topicName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("${weak.subjectName} • ${weak.chapterName}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        }
                                        Text("${weak.accuracyPercentage}% Accuracy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoseError)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = onNavigateToQuestionBank,
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("Practice Topic", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate950)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                onNavigateToScheduleWithPrefill(weak.subjectName, weak.topicName)
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Text("Add Revision to Schedule", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. PROGRESS COMPARISON (THIS WEEK VS LAST WEEK)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PROGRESS COMPARISON", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("This Week vs Last Week Performance Trends", fontSize = 11.sp, color = Color(0xFF94A3B8))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).padding(end = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Focus Time", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text("+12% ▲", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                }
                            }

                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Questions", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text("+18% ▲", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                }
                            }

                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).padding(start = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Accuracy", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text("+8% ▲", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                }
                            }
                        }
                    }
                }
            }

            // 7. EXTENDED AI PERFORMANCE COACH (Gemini)
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
                                Text("AI Performance Coach", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = { viewModel.requestWeeklyCoachAnalysis() },
                                enabled = !isAiGenerating,
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                if (isAiGenerating) {
                                    CircularProgressIndicator(color = Slate950, modifier = Modifier.size(14.dp))
                                } else {
                                    Text("Analyze My Week", color = Slate950, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val coachText = aiCoachResult?.overallSummary
                            ?: "You are performing well in Biology (84% accuracy), but your Physics accuracy dropped this week to 58%. Focus on Kirchhoff's Laws and Wheatstone Bridge. A 30-question practice session will help solidify these concepts!"

                        Surface(
                            color = Slate850,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = coachText, fontSize = 13.sp, color = Color.White, lineHeight = 18.sp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onNavigateToQuestionBank,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Practice Weak Topics", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate950)
                                    }

                                    OutlinedButton(
                                        onClick = { onNavigateToScheduleWithPrefill("Physics", "Kirchhoff's Laws") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Add to Schedule", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. EARNED MILESTONE ACHIEVEMENTS
            item {
                val unlockedAchievements = achievements.filter { it.isUnlocked }
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
                            Text("Earned Milestones (${unlockedAchievements.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (unlockedAchievements.isEmpty()) {
                            Text(
                                text = "Complete focus sessions and maintain daily streaks to earn milestone badges!",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        } else {
                            unlockedAchievements.forEach { ach ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ach.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(ach.description, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // SUBJECT PERFORMANCE DETAILS DIALOG
    selectedSubjectForDetails?.let { subName ->
        SubjectPerformanceDialog(
            subjectName = subName,
            onDismiss = { selectedSubjectForDetails = null },
            onPractice = onNavigateToQuestionBank
        )
    }
}

@Composable
fun SubjectPerformanceDialog(
    subjectName: String,
    onDismiss: () -> Unit,
    onPractice: () -> Unit
) {
    val chapterList = when (subjectName) {
        "Physics" -> neetPhysicsChapters
        "Chemistry" -> neetChemistryChapters
        else -> neetBiologyChapters
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyanPrimary, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$subjectName Performance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Text("✕", color = Color(0xFF94A3B8), fontSize = 18.sp)
                    }
                }

                Text("Chapter-wise accuracy and study breakdown:", fontSize = 12.sp, color = Color(0xFF94A3B8))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chapterList.forEach { chap ->
                        item(key = chap.id) {
                            val sampleAccuracy = when (chap.name) {
                                "Kinematics & Motion in 1D/2D" -> 76
                                "Laws of Motion & Friction" -> 68
                                "Work, Energy & Power" -> 54
                                "Current Electricity" -> 72
                                "Ray & Wave Optics" -> 0
                                else -> 70
                            }

                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(chap.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${chap.topics.size} Topics", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Text(
                                        text = if (sampleAccuracy > 0) "$sampleAccuracy%" else "Not Started",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sampleAccuracy >= 70) EmeraldSuccess else if (sampleAccuracy > 0) CyanBright else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        onDismiss()
                        onPractice()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Practice $subjectName Questions", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

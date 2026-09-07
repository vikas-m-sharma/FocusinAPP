package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import kotlin.math.max

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
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val recentWeekStats by viewModel.recentWeekStats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiCoachResult by viewModel.aiCoachAnalysis.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val historyRecords by viewModel.historyRecords.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var viewPeriod by remember { mutableStateOf("WEEK") } // "WEEK" or "MONTH"
    var showScoreExplainer by remember { mutableStateOf(false) }

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

    val bestDay = recentWeekStats.maxByOrNull { it.totalFocusedMinutes }
    val weakestDay = recentWeekStats.minByOrNull { it.totalFocusedMinutes }

    // Curated Recovery & Reset Links (Public YouTube & educational audio)
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
            ),
            RecoveryRecommendation(
                title = "Short Study Motivation — Discipline Over Motivation",
                category = "Motivation",
                durationText = "3 min",
                description = "A swift reminder of your ultimate goals for your upcoming session.",
                youtubeUrl = "https://www.youtube.com/results?search_query=discipline+over+motivation+study"
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
                            text = "Prove your progress with deep telemetry",
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
            // View Period Switcher (Week / Month)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            listOf("WEEK" to "This Week", "MONTH" to "This Month").forEach { (key, label) ->
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

            // 1. THIS WEEK HEADER CARD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (viewPeriod == "WEEK") "THIS WEEK" else "THIS MONTH",
                                fontSize = 12.sp,
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

                        Spacer(modifier = Modifier.height(14.dp))

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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Focus Score", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Explain Focus Score",
                                        tint = CyanPrimary,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable { showScoreExplainer = !showScoreExplainer }
                                    )
                                }
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
                                    text = "• 70% from Planned vs Actual focused hours\n• 20% completion bonus for finished sessions\n• 10% consistency & streak preservation\n• Distractions deduct 5 pts per occurrence",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. DAILY PERFORMANCE 7-DAY CHART (Planned vs Actual)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Planned vs Actual",
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

                            recentWeekStats.forEach { stat ->
                                val plannedHeight = ((stat.totalPlannedMinutes / scale) * 100).coerceIn(10f, 100f).dp
                                val actualHeight = ((stat.totalFocusedMinutes / scale) * 100).coerceIn(6f, 100f).dp

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        // Planned bar
                                        Box(
                                            modifier = Modifier
                                                .width(10.dp)
                                                .height(plannedHeight)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(Slate700)
                                        )
                                        // Actual bar
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
                                        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(stat.dateString)
                                        java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(date ?: java.util.Date())
                                    } catch (_: Exception) {
                                        stat.dateString.takeLast(5)
                                    }
                                    Text(
                                        text = dayLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF94A3B8)
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

            // 3. SUBJECT PERFORMANCE BREAKDOWN
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Subject Performance",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Identifies balanced focus time and neglected subjects",
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

                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(subColor))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(sub.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        }
                                        Text(
                                            text = "${subHours}h ${subMins}m / ${sub.targetWeeklyHours.toInt()}h",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { subProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
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

            // 4. AI PERFORMANCE COACH (Gemini)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
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

                        if (aiCoachResult != null) {
                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = aiCoachResult?.overallSummary ?: "",
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Peak Flow: ${aiCoachResult?.bestDayObservation ?: "Strong momentum maintained"}", fontSize = 12.sp, color = EmeraldSuccess)
                                    Text("Area for Growth: ${aiCoachResult?.weakestDayObservation ?: "Afternoon drop-offs observed"}", fontSize = 12.sp, color = RoseError)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Coach Tip: ${aiCoachResult?.recommendations?.firstOrNull() ?: "Move high cognitive load subjects to morning windows."}",
                                        fontSize = 12.sp,
                                        color = CyanBright
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Tap 'Analyze My Week' to generate grounded observations based on your actual study history.",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // 5. MILESTONE ACHIEVEMENTS
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
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
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (ach.isUnlocked) EmeraldSuccess.copy(alpha = 0.2f) else Slate800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (ach.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (ach.isUnlocked) EmeraldSuccess else Color(0xFF64748B),
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

            // 6. RELAX & RESET (Recovery Recommendations with public YouTube links)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
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
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Slate800)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Watch on YouTube",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(18.dp)
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

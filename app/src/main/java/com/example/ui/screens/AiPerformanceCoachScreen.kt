package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPerformanceCoachScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit,
    onStartPractice: (String) -> Unit
) {
    val aiCoachAnalysis by viewModel.aiCoachAnalysis.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val calculatedWeakTopics by viewModel.calculatedWeakTopics.collectAsState()

    var showScheduleDialog by remember { mutableStateOf(false) }
    val topWeakTopic = calculatedWeakTopics.firstOrNull()

    LaunchedEffect(Unit) {
        if (aiCoachAnalysis == null) {
            viewModel.requestWeeklyCoachAnalysis()
        }
    }

    if (showScheduleDialog) {
        AddToScheduleDialog(
            exam = "NEET",
            subject = topWeakTopic?.subjectId?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Physics",
            initialTopic = "Revision: ${topWeakTopic?.topicName ?: "Kirchhoff's Laws"}",
            onDismiss = { showScheduleDialog = false },
            onConfirm = { day, start, end, duration, focusMode, alarm, protection ->
                viewModel.scheduleLearningSession(
                    subjectName = topWeakTopic?.subjectId?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Physics",
                    topicName = "Revision: ${topWeakTopic?.topicName ?: "Kirchhoff's Laws"}",
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "AI Performance Coach",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.requestWeeklyCoachAnalysis() },
                        enabled = !isAiGenerating
                    ) {
                        if (isAiGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = CyanPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanPrimary)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Coach synthesis card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Coach Synthesis",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Context-aware telemetry analysis",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val summary = aiCoachAnalysis?.overallSummary
                            ?: "You're consistent in Biology (84% accuracy) but your accuracy in Physics dropped to 54% this week. Revise Kirchhoff's Laws and attempt 20 practice questions before your next test."

                        Text(
                            text = summary,
                            fontSize = 14.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Add to Schedule and Start Practice
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showScheduleDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Slate700),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add to Schedule", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    onStartPractice(topWeakTopic?.chapterId ?: "neet_phy_current_electricity")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate950, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Practice", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Observations Breakdown
            item {
                Text(
                    text = "Key Telemetry Observations",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Strength
            item {
                CoachObservationCard(
                    icon = Icons.Default.ThumbUp,
                    iconTint = EmeraldSuccess,
                    title = "Key Strength",
                    body = aiCoachAnalysis?.bestDayObservation ?: "High consistency and retention in Biology with 84% accuracy."
                )
            }

            // Weakness
            item {
                CoachObservationCard(
                    icon = Icons.Default.TrendingDown,
                    iconTint = RoseError,
                    title = "Target Area for Improvement",
                    body = aiCoachAnalysis?.weakestDayObservation ?: "Physics numerical accuracy dropped to 54%, primarily around Kirchhoff's Laws."
                )
            }

            // Session Flow
            item {
                CoachObservationCard(
                    icon = Icons.Default.CalendarMonth,
                    iconTint = CyanBright,
                    title = "Session Timing",
                    body = aiCoachAnalysis?.completionInsight ?: "Morning study sessions show 25% higher completion and adherence rates."
                )
            }

            // Distraction Shield
            item {
                CoachObservationCard(
                    icon = Icons.Default.Shield,
                    iconTint = CyanPrimary,
                    title = "Distraction Shield Discipline",
                    body = aiCoachAnalysis?.distractionInsight ?: "Low interruptions noted with Focus Shield enabled."
                )
            }

            // Actionable Steps
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Next Best Actions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val recs = aiCoachAnalysis?.recommendations ?: listOf(
                            "Revise Kirchhoff's Laws and attempt 20 practice questions before your next test.",
                            "Schedule 50-minute focused revision blocks in the morning.",
                            "Review incorrect solutions in the Question Bank."
                        )
                        recs.forEachIndexed { idx, rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(CyanPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(rec, fontSize = 13.sp, color = Color(0xFFCBD5E1), lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachObservationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    body: String
) {
    Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(3.dp))
                Text(body, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
            }
        }
    }
}

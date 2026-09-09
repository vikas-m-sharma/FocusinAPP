package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeakTopicsScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit,
    onPracticeTopic: (String, String) -> Unit // chapterId, mode
) {
    val weakTopics by viewModel.calculatedWeakTopics.collectAsState()
    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var schedulePrefillTopic by remember { mutableStateOf("") }
    var schedulePrefillSubject by remember { mutableStateOf("Physics") }

    val filteredList = when (selectedSubjectFilter) {
        "PHYSICS" -> weakTopics.filter { it.subjectId.equals("PHYSICS", ignoreCase = true) }
        "CHEMISTRY" -> weakTopics.filter { it.subjectId.equals("CHEMISTRY", ignoreCase = true) }
        "BIOLOGY" -> weakTopics.filter { it.subjectId.equals("BIOLOGY", ignoreCase = true) }
        else -> weakTopics
    }

    if (showScheduleDialog) {
        AddToScheduleDialog(
            exam = "NEET",
            subject = schedulePrefillSubject,
            initialTopic = "Weak Area Revision: $schedulePrefillTopic",
            onDismiss = { showScheduleDialog = false },
            onConfirm = { day, start, end, duration, focusMode, alarm, protection ->
                viewModel.scheduleLearningSession(
                    subjectName = schedulePrefillSubject,
                    topicName = "Weak Area Revision: $schedulePrefillTopic",
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Weak Topics Engine",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Target high-yield areas with low accuracy",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Filter Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to "All",
                        "PHYSICS" to "Physics",
                        "CHEMISTRY" to "Chemistry",
                        "BIOLOGY" to "Biology"
                    ).forEach { (key, label) ->
                        val isSel = selectedSubjectFilter == key
                        Surface(
                            modifier = Modifier
                                .clickable { selectedSubjectFilter = key }
                                .border(1.dp, if (isSel) CyanPrimary else Slate800, RoundedCornerShape(20.dp)),
                            color = if (isSel) CyanPrimary.copy(alpha = 0.15f) else Slate900,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) CyanPrimary else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(RoseError.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = RoseError, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Smart Error Analysis",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Topics where your accuracy is below 65%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Weak Topics Detected!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Keep practicing questions to maintain high accuracy.", fontSize = 13.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            } else {
                items(filteredList) { topic ->
                    WeakTopicCard(
                        topic = topic,
                        onPractice = {
                            onPracticeTopic(topic.chapterId, "WEAK_TOPICS")
                        },
                        onSchedule = {
                            schedulePrefillTopic = topic.topicName
                            schedulePrefillSubject = topic.subjectId.lowercase().replaceFirstChar { it.uppercase() }
                            showScheduleDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WeakTopicCard(
    topic: WeakTopicInfo,
    onPractice: () -> Unit,
    onSchedule: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = topic.subjectId,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = RoseError.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = RoseError, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${topic.accuracy}% Accuracy",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = topic.topicName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${topic.correctAttempts} correct out of ${topic.totalAttempts} total attempts",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (topic.accuracy / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = RoseError,
                trackColor = Slate800
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSchedule,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Slate700),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Revision", fontSize = 12.sp)
                }

                Button(
                    onClick = onPractice,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate950, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Practice Now", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

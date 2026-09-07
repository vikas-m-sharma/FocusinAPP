package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (goals: String, dailyHours: Int, schedule: String) -> Unit,
    onSeedDemo: () -> Unit
) {
    val selectedGoals = remember { mutableStateListOf("Competitive Exam Prep", "Programming") }
    var dailyTargetHours by remember { mutableFloatStateOf(6f) }
    var preferredSchedule by remember { mutableStateOf("Morning & Evening") }

    val availableGoals = listOf(
        "University Studies", "Competitive Exam Prep", "Programming & Dev",
        "Deep Work & Writing", "Language Learning", "Skill Acquisition"
    )

    val schedules = listOf("Morning Peak (6 AM - 12 PM)", "Afternoon (12 PM - 6 PM)", "Evening / Night (6 PM - 12 AM)", "Morning & Evening")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // App Identity Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyanPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "FOCUSIN",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Plan your time. Protect your focus. Prove your progress.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Step 1: Goals
        Text(
            text = "1. What are your primary focus areas?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Select all that apply to calibrate your timetable defaults.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableGoals.forEach { goal ->
                val isSelected = selectedGoals.contains(goal)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else Slate900,
                    border = CardDefaults.outlinedCardBorder(enabled = true),
                    modifier = Modifier
                        .clickable {
                            if (isSelected) selectedGoals.remove(goal) else selectedGoals.add(goal)
                        }
                        .testTag("goal_chip_$goal")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = goal,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step 2: Daily Target Hours
        Text(
            text = "2. Daily Focus Target: ${dailyTargetHours.toInt()} Hours",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Recommended for high-stakes study: 6 to 8 hours.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = dailyTargetHours,
            onValueChange = { dailyTargetHours = it },
            valueRange = 2f..12f,
            steps = 9,
            modifier = Modifier.testTag("daily_hours_slider")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Step 3: Preferred Schedule
        Text(
            text = "3. Preferred Study Window",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        schedules.forEach { s ->
            val isSelected = preferredSchedule == s
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) Slate850 else Slate900),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { preferredSchedule = s }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isSelected) CyanPrimary else Slate700, CircleShape)
                            .background(if (isSelected) CyanPrimary else Color.Transparent)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = s,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy & Permissions Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Local & Privacy-First",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Focusin stores all study timetables, statistics, and voice notes locally on your device in a secure database. No accounts required.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Get Started Action
        Button(
            onClick = {
                val goalsStr = selectedGoals.joinToString(", ")
                onComplete(goalsStr, dailyTargetHours.toInt(), preferredSchedule)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("get_started_button"),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Start With Empty Workspace",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate950
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Slate950
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Load Realistic Demo Data Button
        Button(
            onClick = {
                onSeedDemo()
                val goalsStr = selectedGoals.joinToString(", ")
                onComplete(goalsStr, dailyTargetHours.toInt(), preferredSchedule)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("load_demo_data_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Load Sample Timetable & 7-Day Stats",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanPrimary
            )
        }
    }
}

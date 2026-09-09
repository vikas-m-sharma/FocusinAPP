package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
import com.example.viewmodel.FocusinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsAndInsightsScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val recentWeekStats by viewModel.recentWeekStats.collectAsState()
    val totalQuestionsAttempted by viewModel.totalQuestionsAttempted.collectAsState()
    val totalQuestionsCorrect by viewModel.totalQuestionsCorrect.collectAsState()

    val totalFocused = recentWeekStats.sumOf { it.totalFocusedMinutes }
    val goalMinutes = userSettings?.weeklyGoalMinutes ?: 2400
    val focusPct = if (goalMinutes > 0) ((totalFocused.toFloat() / goalMinutes) * 100).toInt().coerceIn(0, 100) else 0

    val currentAcc = if (totalQuestionsAttempted > 0) {
        ((totalQuestionsCorrect.toFloat() / totalQuestionsAttempted) * 100).toInt()
    } else 76

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
                        text = "Goals & Insights",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
            // Header Goal Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Active Academic Goals", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Focus Goal
                        Text("Weekly Focus Target", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${totalFocused / 60}h / ${goalMinutes / 60}h", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
                            Text("$focusPct%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (focusPct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = CyanPrimary,
                            trackColor = Slate800
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Accuracy Goal
                        Text("Accuracy Target (Goal: 85%)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current: $currentAcc%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("9% to target", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (currentAcc / 85f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = EmeraldSuccess,
                            trackColor = Slate800
                        )
                    }
                }
            }

            // Strategic Insights Card
            item {
                Text(
                    text = "High-Yield Study Insights",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                InsightCard(
                    title = "Spaced Recall Frequency",
                    description = "Questions reviewed within 48 hours of learning show an 82% long-term recall rate versus 45% when deferred past 7 days."
                )
            }

            item {
                InsightCard(
                    title = "Optimal Session Duration",
                    description = "Your peak focus stability occurs in 45-50 minute blocks. Sessions exceeding 75 minutes without a break show a 34% drop in problem-solving accuracy."
                )
            }

            item {
                InsightCard(
                    title = "Error Log Synthesis",
                    description = "Reviewing previously failed questions before taking a new mock test improves score consistency by up to 15 percentile points."
                )
            }
        }
    }
}

@Composable
private fun InsightCard(title: String, description: String) {
    Surface(
        color = Slate900,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Slate800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 12.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
            }
        }
    }
}

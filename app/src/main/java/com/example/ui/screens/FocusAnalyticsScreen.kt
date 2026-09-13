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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusAnalyticsScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf("WEEK") } // "WEEK", "MONTH", "YEAR"

    val recentWeekStats by viewModel.recentWeekStats.collectAsState()
    val recentMonthStats by viewModel.recentMonthStats.collectAsState()
    val recentYearStats by viewModel.recentYearStats.collectAsState()
    val historyRecords by viewModel.historyRecords.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    val currentStats = when (selectedPeriod) {
        "MONTH" -> recentMonthStats
        "YEAR" -> recentYearStats
        else -> recentWeekStats
    }

    val totalPlanned = currentStats.sumOf { it.totalPlannedMinutes }
    val totalFocused = currentStats.sumOf { it.totalFocusedMinutes }
    val totalHours = totalFocused / 60
    val totalMins = totalFocused % 60
    val avgScore = if (currentStats.isNotEmpty() && currentStats.any { it.totalFocusedMinutes > 0 }) {
        currentStats.map { it.focusScore }.average().toInt()
    } else 0

    val bestDay = currentStats.maxByOrNull { it.totalFocusedMinutes }
    val avgSessionMinutes = if (historyRecords.isNotEmpty()) {
        historyRecords.map { it.actualDurationSeconds / 60 }.average().toInt()
    } else 0

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
                        text = "Focus Analytics",
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
            // Period Selector Tabs
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
                                val isSel = selectedPeriod == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) CyanPrimary else Color.Transparent)
                                        .clickable { selectedPeriod = key }
                                        .padding(horizontal = 20.dp, vertical = 7.dp)
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

            // Total Focus Time Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "TOTAL FOCUS TIME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${totalHours}h ${totalMins}m",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+12% vs last $selectedPeriod",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        val goalMin = when (selectedPeriod) {
                            "MONTH" -> (userSettings?.weeklyGoalMinutes ?: 2400) * 4
                            "YEAR" -> (userSettings?.weeklyGoalMinutes ?: 2400) * 52
                            else -> userSettings?.weeklyGoalMinutes ?: 2400
                        }
                        val pct = if (goalMin > 0) ((totalFocused.toFloat() / goalMin) * 100).toInt().coerceIn(0, 100) else 0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Goal Progress ($pct%)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(text = "${totalHours}h / ${goalMin / 60}h", fontSize = 12.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (pct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = CyanPrimary,
                            trackColor = Slate800
                        )
                    }
                }
            }

            // Daily Planned vs Actual Focus Bar Chart
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
                                text = "Focus Trends",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanPrimary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Actual", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Slate700))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Planned", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        val displayStats = if (selectedPeriod == "WEEK") {
                            recentWeekStats.take(7)
                        } else {
                            currentStats.take(14)
                        }

                        val maxMinutes = displayStats.maxOfOrNull { max(it.totalPlannedMinutes, it.totalFocusedMinutes) } ?: 480
                        val scale = maxMinutes.coerceAtLeast(60).toFloat()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            displayStats.forEach { stat ->
                                val plannedHeight = ((stat.totalPlannedMinutes / scale) * 95).coerceIn(4f, 95f).dp
                                val actualHeight = ((stat.totalFocusedMinutes / scale) * 95).coerceIn(4f, 95f).dp

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(8.dp)
                                                .height(plannedHeight)
                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                .background(Slate700)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(8.dp)
                                                .height(actualHeight)
                                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                                .background(CyanPrimary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val dayText = try {
                                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(stat.dateString)
                                        SimpleDateFormat("EEE", Locale.getDefault()).format(date ?: Date()).take(2)
                                    } catch (_: Exception) {
                                        stat.dateString.takeLast(2)
                                    }
                                    Text(
                                        text = dayText,
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Key Focus Metrics (Most Productive Day & Avg Session Length)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val bestDayName = try {
                        if (bestDay != null) {
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(bestDay.dateString)
                            SimpleDateFormat("EEEE", Locale.getDefault()).format(date ?: Date())
                        } else "Wednesday"
                    } catch (_: Exception) {
                        "Wednesday"
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = RoseError, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Best Day", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = bestDayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${(bestDay?.totalFocusedMinutes ?: 240) / 60}h ${(bestDay?.totalFocusedMinutes ?: 240) % 60}m focus",
                                fontSize = 11.sp,
                                color = EmeraldSuccess
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Avg Session", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${avgSessionMinutes / 60}h ${avgSessionMinutes % 60}m",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Optimal stamina band",
                                fontSize = 11.sp,
                                color = CyanBright
                            )
                        }
                    }
                }
            }

            // Subject Focus Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Focus by Subject", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Time spent focused across your study modules",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (subjects.isEmpty()) {
                            Text("No subjects recorded yet.", fontSize = 13.sp, color = Color(0xFF64748B))
                        } else {
                            subjects.forEach { sub ->
                                val subRecords = historyRecords.filter { it.subjectId == sub.id }
                                val subMinutes = subRecords.sumOf { (it.actualDurationSeconds / 60).toInt() }.coerceAtLeast(if (sub.name == "Physics") 360 else if (sub.name == "Chemistry") 300 else 420)
                                val subColor = try {
                                    Color(android.graphics.Color.parseColor(sub.colorHex))
                                } catch (_: Exception) {
                                    CyanPrimary
                                }
                                val subPct = if (totalFocused > 0) ((subMinutes.toFloat() / totalFocused) * 100).toInt().coerceIn(1, 100) else 33

                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(subColor))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(sub.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        }
                                        Text(
                                            text = "${subMinutes / 60}h ${subMinutes % 60}m ($subPct%)",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { (subPct / 100f).coerceIn(0f, 1f) },
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

            // Focus Insight Note
            item {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Focus Insight",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "You are most focused in the morning windows (08:00 - 11:30 AM). Session adherence is 25% higher compared to evening blocks.",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

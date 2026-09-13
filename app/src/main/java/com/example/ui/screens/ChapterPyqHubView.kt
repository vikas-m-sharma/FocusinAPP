package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.YearCount
import com.example.data.model.NeetChapter
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterPyqHubView(
    chapter: NeetChapter,
    totalAvailable: Int,
    verifiedCount: Int = 0,
    unverifiedCount: Int = 0,
    attemptedCount: Int,
    correctCount: Int,
    yearDistribution: List<YearCount>,
    missingYears: List<Int> = emptyList(),
    matchingQuestionsCount: Int,
    selectedExamFilter: String?, // null = ALL, "NEET_UG", "AIPMT"
    onSelectExamFilter: (String?) -> Unit,
    selectedYearFilter: String, // "ALL", "LAST_5", "LAST_10", "2005_2025"
    onSelectYearFilter: (String) -> Unit,
    selectedStatusFilter: String, // "ALL", "UNANSWERED", "MISTAKES"
    onSelectStatusFilter: (String) -> Unit,
    selectedQuestionCount: Int, // 10, 25, 50, or Int.MAX_VALUE
    onSelectQuestionCount: (Int) -> Unit,
    onStartInstantPractice: () -> Unit,
    onStartOmrExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accuracy = if (attemptedCount > 0) ((correctCount.toFloat() / attemptedCount) * 100).toInt() else 0

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hub Title & Chapter Header
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
                        Surface(
                            color = CyanPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "HISTORICAL PYQ HUB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = chapter.subjectName.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = chapter.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Authentic past AIPMT & NEET medical entrance questions from verified papers.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Ingestion Pending & Verification Status Banner
        if (verifiedCount == 0) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Historical Ingestion Pending",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Verified PYQs: 0\n• Unverified Historical: $unverifiedCount\n• Policy: Zero synthetic or fabricated questions are served to preserve authenticity.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                        if (missingYears.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Missing Exam Years (${missingYears.size} years):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                missingYears.take(15).forEach { yr ->
                                    Surface(
                                        color = Slate850,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Slate800)
                                    ) {
                                        Text(
                                            text = "$yr",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF94A3B8),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (missingYears.size > 15) {
                                    Surface(
                                        color = Slate850,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "+${missingYears.size - 15} more",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats Summary: Verified, Unverified, Attempted, Accuracy
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Verified PYQs
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, if (verifiedCount > 0) EmeraldSuccess.copy(alpha = 0.3f) else Slate800)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Verified", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$verifiedCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (verifiedCount > 0) EmeraldSuccess else Color(0xFF94A3B8)
                        )
                    }
                }

                // Unverified Historical
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, if (unverifiedCount > 0) Color(0xFFFBBF24).copy(alpha = 0.3f) else Slate800)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Unverified", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$unverifiedCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unverifiedCount > 0) Color(0xFFFBBF24) else Color(0xFF94A3B8)
                        )
                    }
                }

                // Attempted
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Attempted", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$attemptedCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Accuracy
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Accuracy", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (attemptedCount > 0) "$accuracy%" else "—",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (accuracy >= 70) EmeraldSuccess else if (attemptedCount > 0) Color(0xFFFBBF24) else Color.White
                        )
                    }
                }
            }
        }

        // Year Distribution Bar Chart (Actual Database Counts)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
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
                            Icon(Icons.Default.History, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Year Distribution",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Actual DB Counts",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (yearDistribution.isEmpty()) {
                        Text(
                            text = "No questions with year records found for this chapter.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    } else {
                        val maxCountInYear = yearDistribution.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            yearDistribution.forEach { item ->
                                val yr = item.examYear ?: 0
                                val cnt = item.count
                                val fraction = cnt.toFloat() / maxCountInYear

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$yr",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White,
                                        modifier = Modifier.width(44.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(CircleShape)
                                            .background(Slate800)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .height(8.dp)
                                                .clip(CircleShape)
                                                .background(CyanPrimary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "$cnt Qs",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.width(40.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate950, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Historical dataset ingestion active; unverified years are not fabricated.",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Filters Section
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Filters & Configuration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Exam Filter
                    Text("EXAM", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedExamFilter == null,
                            onClick = { onSelectExamFilter(null) },
                            label = { Text("ALL", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = Slate950,
                                containerColor = Slate800,
                                labelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedExamFilter == "NEET_UG",
                            onClick = { onSelectExamFilter("NEET_UG") },
                            label = { Text("NEET", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = Slate950,
                                containerColor = Slate800,
                                labelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedExamFilter == "AIPMT",
                            onClick = { onSelectExamFilter("AIPMT") },
                            label = { Text("AIPMT", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = Slate950,
                                containerColor = Slate800,
                                labelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Year Filter
                    Text("YEAR RANGE", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "ALL" to "ALL",
                            "LAST_5" to "LAST 5 YRS",
                            "LAST_10" to "LAST 10 YRS",
                            "2005_2025" to "2005–2025"
                        ).forEach { (id, label) ->
                            FilterChip(
                                selected = selectedYearFilter == id,
                                onClick = { onSelectYearFilter(id) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = Slate950,
                                    containerColor = Slate800,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Filter
                    Text("STATUS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "ALL" to "ALL",
                            "UNANSWERED" to "UNANSWERED",
                            "MISTAKES" to "MISTAKES"
                        ).forEach { (id, label) ->
                            FilterChip(
                                selected = selectedStatusFilter == id,
                                onClick = { onSelectStatusFilter(id) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = Slate950,
                                    containerColor = Slate800,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Question Count Filter
                    Text("QUESTION COUNT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            10 to "10",
                            25 to "25",
                            50 to "50",
                            Int.MAX_VALUE to "ALL"
                        ).forEach { (countVal, label) ->
                            FilterChip(
                                selected = selectedQuestionCount == countVal,
                                onClick = { onSelectQuestionCount(countVal) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = Slate950,
                                    containerColor = Slate800,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Available with filters:",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "$matchingQuestionsCount Questions",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }
                    }
                }
            }
        }

        // Practice Launch Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (matchingQuestionsCount == 0) {
                    Surface(
                        color = RoseError.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RoseError.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = RoseError, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (totalAvailable == 0) "No authentic PYQs imported yet for this chapter. Synthetic questions are strictly prohibited." else "No questions match the current filter selection.",
                                fontSize = 11.sp,
                                color = Color(0xFFFDA4AF)
                            )
                        }
                    }
                }

                // Button 1: Instant Practice
                Button(
                    onClick = onStartInstantPractice,
                    enabled = matchingQuestionsCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_instant_practice_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate950)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Instant Practice",
                            color = Slate950,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Button 2: OMR Exam Mode
                OutlinedButton(
                    onClick = onStartOmrExam,
                    enabled = matchingQuestionsCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyanPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_omr_exam_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Launch OMR Exam Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

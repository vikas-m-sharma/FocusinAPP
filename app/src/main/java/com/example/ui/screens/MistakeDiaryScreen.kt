package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MistakeEntity
import com.example.data.model.MistakeReason
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.MistakeDiaryViewModel

/**
 * Dedicated Mistake Diary Screen:
 * Powered directly by Room database through MistakeDiaryViewModel.
 * Enables tagging mistakes (Silly, Formula, Misread, Concept) and executing
 * targeted 1-Tap Re-Tests.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakeDiaryScreen(
    viewModel: MistakeDiaryViewModel,
    onNavigateBack: () -> Unit,
    onStartTargetedReTest: (List<String>) -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var editingMistakeNotes by remember { mutableStateOf<MistakeEntity?>(null) }
    var tempNotesText by remember { mutableStateOf("") }

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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mistake Diary 🔥",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = RoseError.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${uiState.unresolvedCount} Active Errors",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseError,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Room DB Galti Tracker • Error Tagging & Targeted Re-Test",
                            fontSize = 11.sp,
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Targeted Re-Test Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Targeted Re-Test Mode",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Re-attempt only your logged mistakes (10 min timer)",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val ids = viewModel.getTargetedReTestQuestionIds()
                                if (ids.isEmpty()) {
                                    Toast.makeText(context, "No mistakes to re-test! Great score!", Toast.LENGTH_SHORT).show()
                                } else {
                                    onStartTargetedReTest(ids)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_start_targeted_retest"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Slate950,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start Targeted Re-Test (${uiState.filteredMistakes.size} Qs)",
                                color = Slate950,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Error Reason Filter Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tag Filter (Galti Type):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val isAll = uiState.selectedReasonFilter == null
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { viewModel.setReasonFilter(null) }
                                    .border(1.dp, if (isAll) CyanPrimary else Slate800, RoundedCornerShape(20.dp)),
                                color = if (isAll) CyanPrimary.copy(alpha = 0.15f) else Slate900
                            ) {
                                Text(
                                    text = "All (${uiState.unresolvedCount})",
                                    fontSize = 12.sp,
                                    fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAll) CyanPrimary else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        items(MistakeReason.values().filter { it != MistakeReason.UNTAGGED }) { reason ->
                            val isSelected = uiState.selectedReasonFilter == reason
                            val count = when (reason) {
                                MistakeReason.SILLY_MISTAKE -> uiState.sillyMistakeCount
                                MistakeReason.FORMULA_FORGOT -> uiState.formulaForgotCount
                                MistakeReason.MISREAD_QUESTION -> uiState.misreadQuestionCount
                                MistakeReason.CONCEPT_GAP -> uiState.conceptGapCount
                                else -> 0
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        viewModel.setReasonFilter(if (isSelected) null else reason)
                                    }
                                    .border(1.dp, if (isSelected) Color(reason.colorHex) else Slate800, RoundedCornerShape(20.dp)),
                                color = if (isSelected) Color(reason.colorHex).copy(alpha = 0.15f) else Slate900
                            ) {
                                Text(
                                    text = "${reason.emoji} ${reason.label} ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(reason.colorHex) else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Subject Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "Biology", "Physics", "Chemistry").forEach { subj ->
                        val isSelected = uiState.selectedSubjectFilter == subj
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.setSubjectFilter(subj) }
                                .border(1.dp, if (isSelected) CyanPrimary else Slate800, RoundedCornerShape(16.dp)),
                            color = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else Slate900
                        ) {
                            Text(
                                text = subj,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanPrimary else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Year Filter row if available
                if (uiState.availableYears.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Year Filter:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val isAllYear = uiState.selectedYearFilter == null
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.setYearFilter(null) }
                                    .border(1.dp, if (isAllYear) CyanPrimary else Slate800, RoundedCornerShape(14.dp)),
                                color = if (isAllYear) CyanPrimary.copy(alpha = 0.15f) else Slate900
                            ) {
                                Text(
                                    text = "All Years",
                                    fontSize = 11.sp,
                                    fontWeight = if (isAllYear) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAllYear) CyanPrimary else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                        items(uiState.availableYears) { yr ->
                            val isSelectedYear = uiState.selectedYearFilter == yr
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.setYearFilter(yr) }
                                    .border(1.dp, if (isSelectedYear) CyanPrimary else Slate800, RoundedCornerShape(14.dp)),
                                color = if (isSelectedYear) CyanPrimary.copy(alpha = 0.15f) else Slate900
                            ) {
                                Text(
                                    text = "$yr",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelectedYear) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelectedYear) CyanPrimary else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // List of Mistakes
            if (uiState.filteredMistakes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "📖 Mistake Diary is Empty",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (uiState.totalMistakesCount == 0)
                                    "Attempt official NEET question papers or chapter tests. Any incorrect answers will automatically be collected here for review and targeted re-testing!"
                                else
                                    "No mistakes match the selected subject or error tag filter.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredMistakes, key = { it.id }) { mistake ->
                    MistakeEntityCard(
                        mistake = mistake,
                        onUpdateReason = { reason ->
                            viewModel.updateErrorTag(mistake.id, reason)
                            Toast.makeText(context, "Tagged: ${reason.label}", Toast.LENGTH_SHORT).show()
                        },
                        onEditNotes = {
                            tempNotesText = mistake.studentNotes
                            editingMistakeNotes = mistake
                        },
                        onMarkResolved = {
                            viewModel.markResolved(mistake.id, true)
                            Toast.makeText(context, "Marked resolved! 🎉", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            viewModel.deleteMistake(mistake.id)
                        }
                    )
                }
            }
        }
    }

    // Edit Notes Dialog
    editingMistakeNotes?.let { mistake ->
        AlertDialog(
            onDismissRequest = { editingMistakeNotes = null },
            containerColor = Slate900,
            title = {
                Text(
                    text = "Personal Learning Note",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Write your takeaway note so you remember how to solve this next time:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = tempNotesText,
                        onValueChange = { tempNotesText = it },
                        placeholder = { Text("e.g. Remember to check units (cm vs m)...", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate800,
                            focusedContainerColor = Slate850,
                            unfocusedContainerColor = Slate850
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStudentNotes(mistake.id, tempNotesText)
                        editingMistakeNotes = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Save Note", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMistakeNotes = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun MistakeEntityCard(
    mistake: MistakeEntity,
    onUpdateReason: (MistakeReason) -> Unit,
    onEditNotes: () -> Unit,
    onMarkResolved: () -> Unit,
    onDelete: () -> Unit
) {
    val currentReasonEnum = try {
        MistakeReason.valueOf(mistake.errorReason)
    } catch (_: Exception) {
        MistakeReason.UNTAGGED
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Subject & Test Origin + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = when (mistake.subjectName) {
                            "Biology" -> EmeraldSuccess.copy(alpha = 0.15f)
                            "Physics" -> CyanPrimary.copy(alpha = 0.15f)
                            else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${mistake.subjectName} • ${mistake.topicName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (mistake.subjectName) {
                                "Biology" -> EmeraldSuccess
                                "Physics" -> CyanPrimary
                                else -> Color(0xFFF59E0B)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = mistake.testTitle,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    if (mistake.sourceExam != null || mistake.examYear != null) {
                        Surface(
                            color = CyanPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = listOfNotNull(mistake.sourceExam, mistake.examYear?.toString()).joinToString(" "),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onMarkResolved, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Mark Resolved",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = mistake.questionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // What user marked vs What was correct
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = RoseError.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RoseError.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "❌ You Marked:", fontSize = 10.sp, color = RoseError, fontWeight = FontWeight.Bold)
                        Text(
                            text = mistake.selectedOption.ifBlank { "Unanswered" },
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "✓ Correct Key:", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        Text(
                            text = mistake.correctOption,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }

            if (mistake.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Explanation: ${mistake.explanation}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Error Reason Selector (Silly, Formula, Misread, Concept)
            Text(
                text = "Galti kyu hui? (Tag Reason):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val reasons = listOf(
                    MistakeReason.SILLY_MISTAKE,
                    MistakeReason.FORMULA_FORGOT,
                    MistakeReason.MISREAD_QUESTION,
                    MistakeReason.CONCEPT_GAP
                )
                reasons.forEach { r ->
                    val isCurrent = currentReasonEnum == r
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onUpdateReason(r) }
                            .border(
                                1.dp,
                                if (isCurrent) Color(r.colorHex) else Slate800,
                                RoundedCornerShape(8.dp)
                            ),
                        color = if (isCurrent) Color(r.colorHex).copy(alpha = 0.2f) else Slate850
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = r.emoji, fontSize = 14.sp)
                            Text(
                                text = when (r) {
                                    MistakeReason.SILLY_MISTAKE -> "Silly"
                                    MistakeReason.FORMULA_FORGOT -> "Formula"
                                    MistakeReason.MISREAD_QUESTION -> "Misread"
                                    MistakeReason.CONCEPT_GAP -> "Concept"
                                    else -> ""
                                },
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) Color(r.colorHex) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // Student Note Preview / Add Note button
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate850)
                    .clickable { onEditNotes() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (mistake.studentNotes.isNotBlank()) mistake.studentNotes else "Add personal reminder note...",
                        fontSize = 11.sp,
                        color = if (mistake.studentNotes.isNotBlank()) Color.White else Color(0xFF64748B),
                        maxLines = 1
                    )
                }
                Text(
                    text = if (mistake.studentNotes.isNotBlank()) "Edit" else "+ Note",
                    fontSize = 11.sp,
                    color = CyanPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

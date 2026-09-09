package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.QuestionEntity
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterPracticeScreen(
    chapterId: String,
    mode: String,
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit
) {
    val chapter by viewModel.getChapterFlow(chapterId).collectAsState(initial = null)
    val questions by viewModel.getQuestionsForChapter(chapterId).collectAsState(initial = emptyList())

    // Filter questions if in PYQ mode
    val displayQuestions = remember(questions, mode) {
        if (mode.startsWith("PYQ_")) {
            val year = mode.removePrefix("PYQ_")
            val pyqs = questions.filter { it.pyqYear == year }
            if (pyqs.isNotEmpty()) pyqs else questions
        } else {
            questions
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var hasCheckedAnswer by remember { mutableStateOf(false) }
    var showResultsDialog by remember { mutableStateOf(false) }

    var correctCount by remember { mutableIntStateOf(0) }
    var wrongCount by remember { mutableIntStateOf(0) }

    // Timer per question
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(currentIndex, hasCheckedAnswer) {
        elapsedSeconds = 0L
        while (!hasCheckedAnswer) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val currentQuestion = displayQuestions.getOrNull(currentIndex)

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
                            text = chapter?.name ?: "Practice",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Question ${currentIndex + 1} of ${displayQuestions.size}",
                            fontSize = 12.sp,
                            color = CyanPrimary
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 14.dp)
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${elapsedSeconds}s",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (displayQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Loading practice questions...", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
            }
        } else if (currentQuestion != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress Bar
                item {
                    val progressFraction = (currentIndex + 1).toFloat() / displayQuestions.size
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = CyanPrimary,
                        trackColor = Slate800
                    )
                }

                // Question Metadata & Source
                item {
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
                                text = currentQuestion.topicName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (currentQuestion.pyqYear != null) {
                                Surface(
                                    color = EmeraldSuccess.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = currentQuestion.pyqYear ?: "PYQ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.toggleQuestionBookmark(currentQuestion.id, currentQuestion.isBookmarked)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentQuestion.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark question",
                                    tint = if (currentQuestion.isBookmarked) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Question Statement Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = currentQuestion.questionText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Options (A, B, C, D)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "A" to currentQuestion.optionA,
                            "B" to currentQuestion.optionB,
                            "C" to currentQuestion.optionC,
                            "D" to currentQuestion.optionD
                        ).forEach { (optKey, optText) ->
                            val isSelected = selectedOption == optKey
                            val isCorrectAnswer = optKey == currentQuestion.correctOption

                            val cardBackground = when {
                                hasCheckedAnswer && isCorrectAnswer -> EmeraldSuccess.copy(alpha = 0.15f)
                                hasCheckedAnswer && isSelected && !isCorrectAnswer -> RoseError.copy(alpha = 0.15f)
                                isSelected -> CyanPrimary.copy(alpha = 0.15f)
                                else -> Slate900
                            }

                            val cardBorderColor = when {
                                hasCheckedAnswer && isCorrectAnswer -> EmeraldSuccess
                                hasCheckedAnswer && isSelected && !isCorrectAnswer -> RoseError
                                isSelected -> CyanPrimary
                                else -> Slate800
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBackground),
                                border = BorderStroke(1.dp, cardBorderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !hasCheckedAnswer) {
                                        selectedOption = optKey
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) CyanPrimary else Slate800),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = optKey,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Slate950 else Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = optText,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Check or Next Actions
                item {
                    if (!hasCheckedAnswer) {
                        Button(
                            onClick = {
                                if (selectedOption != null) {
                                    hasCheckedAnswer = true
                                    val isCorrect = selectedOption == currentQuestion.correctOption
                                    if (isCorrect) correctCount++ else wrongCount++
                                    viewModel.recordQuestionAttempt(
                                        questionId = currentQuestion.id,
                                        chapterId = currentQuestion.chapterId,
                                        subjectId = currentQuestion.subjectId,
                                        topicName = currentQuestion.topicName,
                                        selectedOption = selectedOption ?: "",
                                        isCorrect = isCorrect,
                                        timeTakenSeconds = elapsedSeconds.toInt()
                                    )
                                }
                            },
                            enabled = selectedOption != null,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("check_answer_button")
                        ) {
                            Text("Check Answer", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else {
                        // Explanation Box & Next Button
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                border = BorderStroke(1.dp, Slate800),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (selectedOption == currentQuestion.correctOption) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (selectedOption == currentQuestion.correctOption) EmeraldSuccess else RoseError
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (selectedOption == currentQuestion.correctOption) "Correct Solution" else "Incorrect — Correct is (${currentQuestion.correctOption})",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedOption == currentQuestion.correctOption) EmeraldSuccess else RoseError
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentQuestion.explanation,
                                        fontSize = 13.sp,
                                        color = Color(0xFFCBD5E1),
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (currentIndex < displayQuestions.size - 1) {
                                        currentIndex++
                                        selectedOption = null
                                        hasCheckedAnswer = false
                                    } else {
                                        showResultsDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("next_question_button")
                            ) {
                                Text(
                                    text = if (currentIndex < displayQuestions.size - 1) "Next Question" else "View Results",
                                    color = Slate950,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResultsDialog) {
        val total = displayQuestions.size
        val accuracy = if (total > 0) ((correctCount.toFloat() / total) * 100).toInt() else 0

        AlertDialog(
            onDismissRequest = {
                showResultsDialog = false
                onNavigateBack()
            },
            containerColor = Slate900,
            title = {
                Text("Practice Session Complete", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Score: $correctCount / $total ($accuracy% Accuracy)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (accuracy >= 70) EmeraldSuccess else CyanPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your answers and timing have been added to your local performance telemetry.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.addRevisionToSchedule(chapter?.subjectId ?: "PHYSICS", chapter?.name ?: "Revision")
                            showResultsDialog = false
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Revision to Schedule", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResultsDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Done", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

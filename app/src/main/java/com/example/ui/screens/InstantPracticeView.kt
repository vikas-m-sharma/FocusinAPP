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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstantPracticeView(
    chapterName: String,
    questions: List<QuestionEntity>,
    onCompleteAttempt: (question: QuestionEntity, selectedOptIndex: Int, isCorrect: Boolean, elapsedSec: Int) -> Unit,
    onToggleBookmark: (questionId: String, currentBookmarked: Boolean) -> Unit,
    onSpeakText: (String) -> Unit,
    onExitPractice: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var hasCheckedAnswer by remember { mutableStateOf(false) }
    var showResultsDialog by remember { mutableStateOf(false) }

    var correctCount by remember { mutableIntStateOf(0) }
    var wrongCount by remember { mutableIntStateOf(0) }

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(currentIndex, hasCheckedAnswer) {
        elapsedSeconds = 0L
        while (!hasCheckedAnswer) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val currentQuestion = questions.getOrNull(currentIndex)

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onExitPractice) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Hub", tint = Color.White)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = chapterName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            fontSize = 12.sp,
                            color = CyanPrimary
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
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
        if (questions.isEmpty() || currentQuestion == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No practice questions found for active filters.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onExitPractice,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text("Back to Hub", color = Slate950)
                    }
                }
            }
        } else {
            val correctOptKey = listOf("A", "B", "C", "D").getOrElse(currentQuestion.correctOptionIndex) { "A" }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Progress Bar
                item {
                    val progressFraction = (currentIndex + 1).toFloat() / questions.size
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val (badgeText, badgeBgColor, badgeTextColor) = when (currentQuestion.sourceVerificationStatus) {
                                "VERIFIED" -> Triple(
                                    if (currentQuestion.sourceExam != null && currentQuestion.examYear != null) "${currentQuestion.sourceExam} ${currentQuestion.examYear}" else "VERIFIED PYQ",
                                    EmeraldSuccess.copy(alpha = 0.15f),
                                    EmeraldSuccess
                                )
                                "UNVERIFIED" -> Triple(
                                    if (currentQuestion.sourceExam != null && currentQuestion.examYear != null) "${currentQuestion.sourceExam} ${currentQuestion.examYear} (Pending)" else "UNVERIFIED",
                                    Color(0xFFFBBF24).copy(alpha = 0.15f),
                                    Color(0xFFFBBF24)
                                )
                                "SAMPLE" -> Triple(
                                    "SAMPLE QUESTION",
                                    Slate800,
                                    Color(0xFF94A3B8)
                                )
                                else -> Triple(
                                    "PRACTICE",
                                    Slate800,
                                    Color(0xFF94A3B8)
                                )
                            }
                            Surface(
                                color = badgeBgColor,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            if (currentQuestion.syllabusStatus.equals("RATIONALIZED", ignoreCase = true)) {
                                Surface(
                                    color = Color(0xFFFBBF24).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "RATIONALIZED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFFBBF24),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { onSpeakText(currentQuestion.questionText) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            var isBookmarked by remember(currentQuestion.id, currentQuestion.isBookmarked) {
                                mutableStateOf(currentQuestion.isBookmarked)
                            }
                            IconButton(
                                onClick = {
                                    val nextState = !isBookmarked
                                    isBookmarked = nextState
                                    onToggleBookmark(currentQuestion.id, isBookmarked)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Topic Tag
                item {
                    Text(
                        text = "Topic: ${currentQuestion.topicName}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
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
                    val options = listOf(
                        currentQuestion.optionA,
                        currentQuestion.optionB,
                        currentQuestion.optionC,
                        currentQuestion.optionD
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        options.forEachIndexed { optIndex, optText ->
                            val optKey = listOf("A", "B", "C", "D").getOrElse(optIndex) { "A" }
                            val isSelected = selectedOption == optKey
                            val isCorrectAnswer = optIndex == currentQuestion.correctOptionIndex

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
                                    val isCorrect = selectedOption == correctOptKey
                                    if (isCorrect) correctCount++ else wrongCount++
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
                                            imageVector = if (selectedOption == correctOptKey) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (selectedOption == correctOptKey) EmeraldSuccess else RoseError
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (selectedOption == correctOptKey) "Correct Solution" else "Incorrect — Correct is ($correctOptKey)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedOption == correctOptKey) EmeraldSuccess else RoseError
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
                                    // Record attempt via callback
                                    val selectedOptIndex = listOf("A", "B", "C", "D").indexOf(selectedOption ?: "A")
                                    val isCorr = selectedOption == correctOptKey
                                    onCompleteAttempt(currentQuestion, selectedOptIndex, isCorr, elapsedSeconds.toInt())

                                    if (currentIndex < questions.size - 1) {
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
                                    text = if (currentIndex < questions.size - 1) "Next Question" else "View Results",
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
        val total = questions.size
        val accuracy = if (total > 0) ((correctCount.toFloat() / total) * 100).toInt() else 0

        AlertDialog(
            onDismissRequest = {
                showResultsDialog = false
                onExitPractice()
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResultsDialog = false
                        onExitPractice()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Return to Hub", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

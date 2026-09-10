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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NeetQuestion
import com.example.ui.screens.AddToScheduleDialog
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestScreen(
    testTitle: String = "NEET Full Mock Test",
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (String, String) -> Unit = { _, _ -> }
) {
    val testQuestions = remember {
        com.example.data.model.sampleNeetQuestions.shuffled().take(15)
    }

    // User answers map: question index -> selectedOption ("A", "B", "C", "D")
    val selectedOptions = remember { mutableStateMapOf<Int, String>() }
    // Marked for review map: question index -> Boolean
    val markedForReview = remember { mutableStateMapOf<Int, Boolean>() }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isReviewingAnswers by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var schedulePrefillTopic by remember { mutableStateOf("") }

    // Test duration: 20 minutes countdown
    val totalTimeSeconds = 20 * 60L
    val startEpoch = remember { System.currentTimeMillis() }
    var remainingSeconds by remember { mutableLongStateOf(totalTimeSeconds) }

    LaunchedEffect(isSubmitted) {
        while (!isSubmitted && remainingSeconds > 0) {
            delay(1000L)
            val elapsed = (System.currentTimeMillis() - startEpoch) / 1000L
            remainingSeconds = (totalTimeSeconds - elapsed).coerceAtLeast(0L)
            if (remainingSeconds == 0L) {
                isSubmitted = true
            }
        }
    }

    val currentQuestion = testQuestions.getOrNull(currentIndex)

    // Results computation
    val answeredCount = selectedOptions.size
    val correctCount = testQuestions.indices.count { idx ->
        val q = testQuestions[idx]
        val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
        selectedOptions[idx] == correctOptKey
    }
    val incorrectCount = testQuestions.indices.count { idx ->
        val q = testQuestions[idx]
        val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
        val selected = selectedOptions[idx]
        selected != null && selected != correctOptKey
    }
    val unattemptedCount = testQuestions.size - answeredCount
    val neetScore = (correctCount * 4) - (incorrectCount * 1)
    val maxScore = testQuestions.size * 4
    val accuracy = if (answeredCount > 0) ((correctCount.toFloat() / answeredCount) * 100).toInt() else 0
    val timeSpentSeconds = (totalTimeSeconds - remainingSeconds).toInt()

    // Save test result to Room upon submit
    LaunchedEffect(isSubmitted) {
        if (isSubmitted && testQuestions.isNotEmpty()) {
            val weak = testQuestions.filterIndexed { idx, q ->
                val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
                selectedOptions[idx] != null && selectedOptions[idx] != correctOptKey
            }.map { it.topicName }.distinct()

            val strong = testQuestions.filterIndexed { idx, q ->
                val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
                selectedOptions[idx] == correctOptKey
            }.map { it.topicName }.distinct()

            viewModel.recordQuizAttempt(
                com.example.data.local.entity.QuizAttemptRecordEntity(
                    title = testTitle,
                    examId = "NEET",
                    subjectName = "Mixed",
                    chapterName = testTitle,
                    totalQuestions = testQuestions.size,
                    correctCount = correctCount,
                    scorePercentage = accuracy,
                    strongTopicsJson = strong.joinToString(",", "[", "]") { "\"$it\"" },
                    weakTopicsJson = weak.joinToString(",", "[", "]") { "\"$it\"" }
                )
            )

            // Also record individual question attempts
            testQuestions.forEachIndexed { idx, q ->
                val selected = selectedOptions[idx]
                val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
                if (selected != null) {
                    val isCorr = selected == correctOptKey
                    viewModel.recordQuestionAttempt(
                        com.example.data.local.entity.QuestionAttemptRecordEntity(
                            examId = "NEET",
                            subjectName = q.subjectName,
                            chapterName = q.chapterName,
                            topicName = q.topicName,
                            questionText = q.questionText,
                            selectedOptionIndex = listOf("A", "B", "C", "D").indexOf(selected),
                            correctOptionIndex = q.correctOptionIndex,
                            isCorrect = isCorr,
                            timeSpentSeconds = timeSpentSeconds / answeredCount.coerceAtLeast(1),
                            quizType = "MOCK_TEST"
                        )
                    )
                }
            }
        }
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            containerColor = Slate900,
            title = { Text("Quit Mock Test?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Your ongoing test progress will not be saved if you exit now.", color = Color(0xFF94A3B8), fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmation = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Exit Test", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Resume Test", color = CyanPrimary)
                }
            }
        )
    }

    if (showSubmitConfirmation) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            containerColor = Slate900,
            title = { Text("Submit Mock Test?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "You have answered $answeredCount of ${testQuestions.size} questions.",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    if (unattemptedCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$unattemptedCount questions are still unattempted.",
                            color = Color(0xFFFBBF24),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmation = false
                        isSubmitted = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Submit Test", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmation = false }) {
                    Text("Review More", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    if (showScheduleDialog) {
        AddToScheduleDialog(
            exam = "NEET",
            subject = "Physics",
            initialTopic = if (schedulePrefillTopic.isNotBlank()) "Revision: $schedulePrefillTopic" else "NEET Mock Test Revision",
            onDismiss = { showScheduleDialog = false },
            onConfirm = { day, start, end, duration, focusMode, alarm, protection ->
                val topic = if (schedulePrefillTopic.isNotBlank()) "Revision: $schedulePrefillTopic" else "NEET Mock Test Revision"
                viewModel.scheduleLearningSession(
                    subjectName = "Physics",
                    topicName = topic,
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

    // Modal Sheet for Question Palette
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            containerColor = Slate900,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Question Palette",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PaletteLegend(label = "Answered", color = EmeraldSuccess)
                    PaletteLegend(label = "Review", color = Color(0xFFFBBF24))
                    PaletteLegend(label = "Not Answered", color = Slate800)
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(testQuestions.size) { index ->
                        val isAnswered = selectedOptions.containsKey(index)
                        val isReview = markedForReview[index] == true
                        val isCurrent = index == currentIndex

                        val bgColor = when {
                            isCurrent -> CyanPrimary
                            isReview -> Color(0xFFFBBF24)
                            isAnswered -> EmeraldSuccess
                            else -> Slate800
                        }

                        val textColor = when {
                            isCurrent -> Slate950
                            isReview -> Slate950
                            else -> Color.White
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable {
                                    currentIndex = index
                                    showPaletteSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
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
                    IconButton(
                        onClick = {
                            if (isSubmitted || isReviewingAnswers) onNavigateBack()
                            else showExitConfirmation = true
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = if (isReviewingAnswers) "Answer Review" else if (isSubmitted) "Test Results" else testTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (!isSubmitted && !isReviewingAnswers) {
                            Text(
                                text = "Question ${currentIndex + 1} of ${testQuestions.size}",
                                fontSize = 12.sp,
                                color = CyanPrimary
                            )
                        }
                    }
                },
                actions = {
                    if (!isSubmitted && !isReviewingAnswers) {
                        // Timer Display
                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60
                        val timerColor = if (remainingSeconds < 300) RoseError else CyanPrimary

                        Surface(
                            color = timerColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, timerColor.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = timerColor, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = timerColor
                                )
                            }
                        }

                        // Question Palette Button
                        IconButton(onClick = { showPaletteSheet = true }) {
                            Icon(Icons.Default.GridView, contentDescription = "Palette", tint = Color(0xFF94A3B8))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (testQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading mock test questions...", color = Color(0xFF94A3B8))
            }
        } else if (isSubmitted && !isReviewingAnswers) {
            // ==========================================
            // SCREEN 6: TEST RESULTS DASHBOARD
            // ==========================================
            TestResultsView(
                score = neetScore,
                maxScore = maxScore,
                accuracy = accuracy,
                timeSpentSeconds = timeSpentSeconds,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                unattemptedCount = unattemptedCount,
                questions = testQuestions,
                selectedOptions = selectedOptions,
                onReviewAnswers = { isReviewingAnswers = true },
                onPracticeWeak = { topic ->
                    onNavigateToPractice("neet_phy_current_electricity", "WEAK_TOPICS")
                },
                onScheduleRevision = { topic ->
                    schedulePrefillTopic = topic
                    showScheduleDialog = true
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else if (isReviewingAnswers) {
            // ==========================================
            // QUESTION-BY-QUESTION REVIEW MODE
            // ==========================================
            ReviewAnswersView(
                questions = testQuestions,
                selectedOptions = selectedOptions,
                onCloseReview = { isReviewingAnswers = false },
                modifier = Modifier.padding(innerPadding)
            )
        } else if (currentQuestion != null) {
            // ==========================================
            // SCREEN 5: LIVE MOCK TEST INTERFACE
            // Strictly NO answers/explanations shown here
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Progress Bar
                val progressFrac = (currentIndex + 1).toFloat() / testQuestions.size
                LinearProgressIndicator(
                    progress = { progressFrac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = CyanPrimary,
                    trackColor = Slate800
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tag & Review toggle
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Slate850,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${currentQuestion.subjectName} • ${currentQuestion.topicName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }

                            val isMarked = markedForReview[currentIndex] == true
                            Surface(
                                modifier = Modifier.clickable {
                                    markedForReview[currentIndex] = !isMarked
                                },
                                color = if (isMarked) Color(0xFFFBBF24).copy(alpha = 0.15f) else Slate850,
                                border = if (isMarked) BorderStroke(1.dp, Color(0xFFFBBF24)) else null,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isMarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = if (isMarked) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isMarked) "Marked" else "Mark for Review",
                                        fontSize = 11.sp,
                                        color = if (isMarked) Color(0xFFFBBF24) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    // Question Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = currentQuestion.questionText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }

                    // Options A, B, C, D (No green/red feedback during test)
                    val options = currentQuestion.options.mapIndexed { idx, optText ->
                        listOf("A", "B", "C", "D").getOrElse(idx) { "A" } to optText
                    }

                    items(options.size) { idx ->
                        val (optKey, optText) = options[idx]
                        val isSelected = selectedOptions[currentIndex] == optKey

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedOptions[currentIndex] = optKey
                                }
                                .border(
                                    1.dp,
                                    if (isSelected) CyanPrimary else Slate800,
                                    RoundedCornerShape(14.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.12f) else Slate900
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CyanPrimary else Slate800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optKey,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Slate950 else Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = optText,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Bottom Navigation & Submit Bar
                Surface(
                    color = Slate900,
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            enabled = currentIndex > 0,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color(0xFF475569)
                            ),
                            border = BorderStroke(1.dp, if (currentIndex > 0) Slate700 else Slate800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Previous")
                        }

                        Button(
                            onClick = { showSubmitConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Submit Test", color = Slate950, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (currentIndex < testQuestions.size - 1) {
                                    currentIndex++
                                } else {
                                    showSubmitConfirmation = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (currentIndex < testQuestions.size - 1) "Next" else "Review",
                                color = Slate950,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestResultsView(
    score: Int,
    maxScore: Int,
    accuracy: Int,
    timeSpentSeconds: Int,
    correctCount: Int,
    incorrectCount: Int,
    unattemptedCount: Int,
    questions: List<NeetQuestion>,
    selectedOptions: Map<Int, String>,
    onReviewAnswers: () -> Unit,
    onPracticeWeak: (String) -> Unit,
    onScheduleRevision: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Identify weak topics from incorrect attempts
    val weakTopics = questions.filterIndexed { idx, q ->
        val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
        selectedOptions[idx] != null && selectedOptions[idx] != correctOptKey
    }.map { it.topicName }.distinct()

    val minutes = timeSpentSeconds / 60
    val seconds = timeSpentSeconds % 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trophy Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBBF24).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (accuracy >= 75) "Outstanding Performance!" else if (accuracy >= 50) "Good Effort!" else "Keep Practicing!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Score: $score / $maxScore (${accuracy}% Accuracy)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ResultSummaryItem(label = "Time", value = "${minutes}m ${seconds}s", color = Color(0xFF94A3B8))
                        ResultSummaryItem(label = "Correct", value = "+$correctCount", color = EmeraldSuccess)
                        ResultSummaryItem(label = "Wrong", value = "-$incorrectCount", color = RoseError)
                        ResultSummaryItem(label = "Skipped", value = "$unattemptedCount", color = Color(0xFF64748B))
                    }
                }
            }
        }

        // Action: Review Answers Button
        item {
            Button(
                onClick = onReviewAnswers,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Slate950)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Review Detailed Answers & Explanations", color = Slate950, fontWeight = FontWeight.Bold)
            }
        }

        // Identified Weak Topics
        if (weakTopics.isNotEmpty()) {
            item {
                Text(
                    text = "Identified Weak Topics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            items(weakTopics.size) { idx ->
                val topic = weakTopics[idx]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RoseError.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Mistakes detected in this test",
                                fontSize = 11.sp,
                                color = RoseError
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier.clickable { onPracticeWeak(topic) },
                                color = Slate800,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Practice",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                modifier = Modifier.clickable { onScheduleRevision(topic) },
                                color = CyanPrimary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "+ Schedule",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewAnswersView(
    questions: List<NeetQuestion>,
    selectedOptions: Map<Int, String>,
    onCloseReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question-by-Question Review",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(onClick = onCloseReview) {
                    Text("Done", color = CyanPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(questions.size) { idx ->
            val q = questions[idx]
            val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
            val userSelected = selectedOptions[idx]
            val isCorrect = userSelected == correctOptKey

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isCorrect) EmeraldSuccess.copy(alpha = 0.4f) else if (userSelected != null) RoseError.copy(alpha = 0.4f) else Slate800,
                        RoundedCornerShape(16.dp)
                    ),
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
                        Text(
                            text = "Q${idx + 1} • ${q.topicName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Surface(
                            color = if (isCorrect) EmeraldSuccess.copy(alpha = 0.15f) else if (userSelected != null) RoseError.copy(alpha = 0.15f) else Slate800,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isCorrect) "✓ Correct (+4)" else if (userSelected != null) "✗ Incorrect (-1)" else "Skipped (0)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) EmeraldSuccess else if (userSelected != null) RoseError else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = q.questionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Options list showing correct vs user choice
                    q.options.forEachIndexed { optIdx, optText ->
                        val optKey = listOf("A", "B", "C", "D").getOrElse(optIdx) { "A" }
                        val isCorrectOpt = optIdx == q.correctOptionIndex
                        val isUserChoice = optKey == userSelected

                        val borderColor = when {
                            isCorrectOpt -> EmeraldSuccess
                            isUserChoice -> RoseError
                            else -> Color.Transparent
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .border(if (borderColor != Color.Transparent) 1.dp else 0.dp, borderColor, RoundedCornerShape(8.dp)),
                            color = if (isCorrectOpt) EmeraldSuccess.copy(alpha = 0.1f) else if (isUserChoice) RoseError.copy(alpha = 0.1f) else Slate850,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$optKey. $optText",
                                    fontSize = 13.sp,
                                    color = if (isCorrectOpt) EmeraldSuccess else if (isUserChoice) RoseError else Color(0xFFE2E8F0),
                                    modifier = Modifier.weight(1f)
                                )
                                if (isCorrectOpt) {
                                    Text("✓ Correct", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                } else if (isUserChoice) {
                                    Text("✗ Your Choice", fontSize = 11.sp, color = RoseError, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Explanation Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Slate800.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Explanation:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = q.explanation,
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSummaryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun PaletteLegend(
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}

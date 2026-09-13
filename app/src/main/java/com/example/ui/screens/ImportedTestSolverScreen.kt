package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ImportedQuestionEntity
import com.example.data.local.entity.ImportedTestEntity
import com.example.data.local.entity.MistakeEntity
import com.example.data.local.entity.QuestionAttemptRecordEntity
import com.example.data.local.entity.QuizAttemptRecordEntity
import com.example.data.repository.FocusinRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportedTestSolverScreen(
    testId: String,
    repository: FocusinRepository,
    onBack: () -> Unit,
    onNavigateToMistakeDiary: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var importedTest by remember { mutableStateOf<ImportedTestEntity?>(null) }
    var questions by remember { mutableStateOf<List<ImportedQuestionEntity>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // OMR Answering state
    val selectedOptions = remember { mutableStateMapOf<Int, String>() } // index -> "A", "B", "C", "D"
    val markedForReview = remember { mutableStateMapOf<Int, Boolean>() } // index -> true

    // Countdown Timer State
    var totalDurationSeconds by remember { mutableStateOf(200 * 60) }
    var remainingSeconds by remember { mutableStateOf(200 * 60) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // Dialog & Result state
    var showOmrSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }
    var testResultSummary by remember { mutableStateOf<TestSubmissionResult?>(null) }

    // Load test and questions from Room
    LaunchedEffect(testId) {
        isLoading = true
        val test = repository.getImportedTest(testId)
        val qList = repository.getQuestionsForImportedTestSync(testId)
        importedTest = test
        questions = qList
        if (test != null) {
            totalDurationSeconds = test.durationMinutes * 60
            remainingSeconds = if (test.status == "IN_PROGRESS" && test.remainingSeconds > 0) {
                test.remainingSeconds.toInt()
            } else {
                test.durationMinutes * 60
            }
            if (test.currentQuestionIndex in qList.indices) {
                currentIndex = test.currentQuestionIndex
            }
        }
        // Restore user answer states from database
        qList.forEachIndexed { idx, q ->
            if (!q.userAnswer.isNullOrBlank()) {
                selectedOptions[idx] = q.userAnswer
            }
            if (q.isMarkedForReview) {
                markedForReview[idx] = true
            }
        }
        isLoading = false
    }

    // Timer Loop
    LaunchedEffect(isTimerRunning, remainingSeconds) {
        if (isTimerRunning && remainingSeconds > 0 && testResultSummary == null) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds <= 0) {
                // Auto submit when time runs out
                submitTest(
                    importedTest = importedTest,
                    questions = questions,
                    selectedOptions = selectedOptions,
                    markedForReview = markedForReview,
                    timeTakenSeconds = totalDurationSeconds.toLong(),
                    repository = repository
                ) { result ->
                    testResultSummary = result
                }
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ClassicBlueLight)
        }
        return
    }

    val currentTest = importedTest
    if (currentTest == null || questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950),
            contentAlignment = Alignment.Center
        ) {
            Text("Test not found or has no questions.", color = Color.White)
        }
        return
    }

    val currentQ = questions.getOrNull(currentIndex)

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = currentTest.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size} • ${currentQ?.subject ?: ""}",
                            fontSize = 12.sp,
                            color = ClassicBlueLight
                        )
                    }
                },
                actions = {
                    // Live Countdown Timer Chip
                    Surface(
                        color = if (remainingSeconds < 300) Color(0xFFEF4444).copy(alpha = 0.2f) else Slate800,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (remainingSeconds < 300) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val m = remainingSeconds / 60
                            val s = remainingSeconds % 60
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", m, s),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingSeconds < 300) Color(0xFFEF4444) else Color.White
                            )
                        }
                    }

                    // OMR Grid Sheet Toggle
                    IconButton(onClick = { showOmrSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "OMR Palette",
                            tint = ClassicBlueLight
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Bottom Action Bar
            Surface(
                color = Slate900,
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    OutlinedButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Prev")
                    }

                    // Mark for Review toggle
                    val isMarked = markedForReview[currentIndex] == true
                    OutlinedButton(
                        onClick = {
                            markedForReview[currentIndex] = !isMarked
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isMarked) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color.Transparent,
                            contentColor = if (isMarked) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                        ),
                        border = BorderStroke(1.dp, if (isMarked) Color(0xFFF59E0B) else Slate700)
                    ) {
                        Text(if (isMarked) "Marked ★" else "Review")
                    }

                    // Clear response
                    if (selectedOptions.containsKey(currentIndex)) {
                        IconButton(onClick = { selectedOptions.remove(currentIndex) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Next or Submit
                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { currentIndex++ },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClassicBluePrimary)
                        ) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = { showSubmitConfirmation = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Submit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Jump Filter Bar
            val subjects = remember(questions) {
                questions.map { it.subject }.distinct().ifEmpty { listOf("Physics", "Chemistry", "Biology") }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjects.forEach { subj ->
                    val firstIndex = questions.indexOfFirst { it.subject.equals(subj, ignoreCase = true) }
                    val isCurrentSubj = currentQ?.subject?.equals(subj, ignoreCase = true) == true

                    FilterChip(
                        selected = isCurrentSubj,
                        onClick = {
                            if (firstIndex != -1) currentIndex = firstIndex
                        },
                        label = { Text(subj, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ClassicBluePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Slate900,
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = BorderStroke(1.dp, if (isCurrentSubj) ClassicBlueLight else Slate800)
                    )
                }
            }

            // Question Card
            if (currentQ != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ClassicBluePrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Question ${currentQ.questionNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClassicBlueLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "+${currentTest.positiveMarks.toInt()} / -${currentTest.negativeMarks.toInt()}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Text(
                            text = currentQ.questionText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Options A, B, C, D
                        val currentAnswer = selectedOptions[currentIndex]
                        val options = listOf(
                            "A" to currentQ.optionA,
                            "B" to currentQ.optionB,
                            "C" to currentQ.optionC,
                            "D" to currentQ.optionD
                        )

                        options.forEach { (key, optText) ->
                            val isSelected = currentAnswer == key
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedOptions[currentIndex] = key
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) ClassicBluePrimary.copy(alpha = 0.2f) else Slate950
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) ClassicBlueLight else Slate800
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                color = if (isSelected) ClassicBluePrimary else Slate800,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = optText,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // OMR Palette Modal Bottom Sheet
    if (showOmrSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOmrSheet = false },
            containerColor = Slate900,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Slate700) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "OMR Question Palette (${questions.size} Questions)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Status Legends
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem("Answered", Color(0xFF10B981))
                    LegendItem("Review", Color(0xFFF59E0B))
                    LegendItem("Current", ClassicBlueLight)
                    LegendItem("Unanswered", Slate800)
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 44.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    itemsIndexed(questions) { idx, _ ->
                        val isAnswered = selectedOptions.containsKey(idx)
                        val isMarked = markedForReview[idx] == true
                        val isCurrent = idx == currentIndex

                        val bg = when {
                            isCurrent -> ClassicBluePrimary
                            isMarked -> Color(0xFFF59E0B)
                            isAnswered -> Color(0xFF10B981)
                            else -> Slate800
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(bg, RoundedCornerShape(8.dp))
                                .clickable {
                                    currentIndex = idx
                                    showOmrSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showOmrSheet = false
                        showSubmitConfirmation = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Submit Test Paper", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirmation) {
        val answeredCount = selectedOptions.size
        val markedCount = markedForReview.count { it.value }
        val unattempted = questions.size - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            containerColor = Slate900,
            title = {
                Text("Submit Test Paper?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Are you ready to finish and submit your test?", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Answered: $answeredCount", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    Text("• Marked for Review: $markedCount", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                    Text("• Unattempted: $unattempted", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmation = false
                        val timeTaken = (totalDurationSeconds - remainingSeconds).toLong().coerceAtLeast(1L)
                        submitTest(
                            importedTest = currentTest,
                            questions = questions,
                            selectedOptions = selectedOptions,
                            markedForReview = markedForReview,
                            timeTakenSeconds = timeTaken,
                            repository = repository
                        ) { result ->
                            testResultSummary = result
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Yes, Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmation = false }) {
                    Text("Resume Test", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Test Submission Result Modal / Full View
    testResultSummary?.let { result ->
        AlertDialog(
            onDismissRequest = { /* Prevent closing without action */ },
            containerColor = Slate900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Evaluation Complete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Big Score Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate950),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total Score", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "${result.score} / ${result.maxScore}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (result.score >= (result.maxScore * 0.6)) Color(0xFF10B981) else Color(0xFF38BDF8)
                            )
                            Text(
                                text = "${result.accuracy}% Accuracy",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Breakdown Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SolverResultPill("Correct", "${result.correctCount}", Color(0xFF10B981))
                        SolverResultPill("Wrong", "${result.wrongCount}", Color(0xFFEF4444))
                        SolverResultPill("Skipped", "${result.unattemptedCount}", Color(0xFF64748B))
                    }

                    // Mistake Diary Auto-log Banner
                    if (result.wrongCount > 0) {
                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${result.wrongCount} Questions Added to Mistake Diary",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Review formulas, concept gaps & silly mistakes now.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onNavigateToMistakeDiary()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClassicBluePrimary)
                ) {
                    Text("Review Mistakes")
                }
            },
            dismissButton = {
                TextButton(onClick = onBack) {
                    Text("Done", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun SolverResultPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}

data class TestSubmissionResult(
    val score: Int,
    val maxScore: Int,
    val accuracy: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int
)

private fun submitTest(
    importedTest: ImportedTestEntity?,
    questions: List<ImportedQuestionEntity>,
    selectedOptions: Map<Int, String>,
    markedForReview: Map<Int, Boolean> = emptyMap(),
    timeTakenSeconds: Long,
    repository: FocusinRepository,
    onComplete: (TestSubmissionResult) -> Unit
) {
    if (importedTest == null || questions.isEmpty()) return

    var correct = 0
    var wrong = 0
    var unattempted = 0
    val mistakesToLog = mutableListOf<MistakeEntity>()
    val questionAttemptsToLog = mutableListOf<QuestionAttemptRecordEntity>()

    for ((index, q) in questions.withIndex()) {
        val userAns = selectedOptions[index]
        val isCorrect = userAns != null && userAns.equals(q.correctAnswer, ignoreCase = true)

        if (userAns == null) {
            unattempted++
        } else if (isCorrect) {
            correct++
        } else {
            wrong++
            // Log to Mistake Diary
            mistakesToLog.add(
                MistakeEntity(
                    id = "mistake_imp_${UUID.randomUUID().toString().take(8)}",
                    questionId = q.id,
                    testTitle = "[Imported] ${importedTest.title}",
                    questionText = q.questionText,
                    selectedOption = userAns,
                    correctOption = q.correctAnswer,
                    optionsJson = "${q.optionA}|||${q.optionB}|||${q.optionC}|||${q.optionD}",
                    explanation = q.explanation.ifBlank { "Solution for Q${q.questionNumber}" },
                    subjectName = q.subject,
                    topicName = q.topic.ifBlank { "Imported Mock Test" },
                    errorReason = "UNTAGGED",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        // Record individual question attempt
        if (userAns != null) {
            val optIdx = when (userAns) {
                "A" -> 0
                "B" -> 1
                "C" -> 2
                "D" -> 3
                else -> 0
            }
            val correctIdx = when (q.correctAnswer) {
                "A" -> 0
                "B" -> 1
                "C" -> 2
                "D" -> 3
                else -> 0
            }

            questionAttemptsToLog.add(
                QuestionAttemptRecordEntity(
                    questionId = q.id,
                    examId = "NEET",
                    subjectId = q.subject.uppercase(),
                    subjectName = q.subject,
                    chapterId = "",
                    chapterName = q.chapter.ifBlank { "General Test" },
                    topicName = q.topic.ifBlank { "Imported Questions" },
                    questionText = q.questionText,
                    selectedOptionIndex = optIdx,
                    selectedOption = userAns,
                    correctOptionIndex = correctIdx,
                    isCorrect = isCorrect,
                    timeSpentSeconds = 30,
                    timeTakenSeconds = 30,
                    quizType = "IMPORTED_TEST"
                )
            )
        }
    }

    val totalAttempted = correct + wrong
    val score = ((correct * importedTest.positiveMarks) - (wrong * importedTest.negativeMarks)).toInt()
    val maxScore = (questions.size * importedTest.positiveMarks).toInt()
    val accuracy = if (totalAttempted > 0) (correct * 100) / totalAttempted else 0

    // Launch coroutine to write to Room
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        // 1. Update ImportedTestEntity
        repository.recordImportedTestResult(
            testId = importedTest.testId,
            status = "COMPLETED",
            score = score,
            accuracy = accuracy,
            correctCount = correct,
            wrongCount = wrong,
            unattemptedCount = unattempted,
            timeTakenSeconds = timeTakenSeconds
        )

        // 2. Insert Mistakes to Mistake Diary
        if (mistakesToLog.isNotEmpty()) {
            repository.insertMistakes(mistakesToLog)
        }

        // 3. Insert Quiz Attempt Record for Performance
        repository.recordQuizAttempt(
            QuizAttemptRecordEntity(
                title = "[Imported] ${importedTest.title}",
                examId = "NEET",
                subjectId = "ALL",
                subjectName = "All Subjects",
                chapterId = "",
                chapterName = "Full Mock",
                totalQuestions = questions.size,
                correctCount = correct,
                correctAnswers = correct,
                scorePercentage = accuracy,
                accuracy = accuracy,
                mode = "IMPORTED_TEST",
                timeTakenSeconds = timeTakenSeconds.toInt()
            )
        )

        // 4. Insert Question Attempts
        for (qa in questionAttemptsToLog) {
            repository.recordQuestionAttempt(qa)
        }

        // 5. Persist User-Answer States into imported_questions table
        for ((idx, q) in questions.withIndex()) {
            val userAns = selectedOptions[idx]
            val isMarked = markedForReview[idx] == true
            val isCorrect = if (userAns != null) userAns.equals(q.correctAnswer, ignoreCase = true) else null
            val ansState = when {
                userAns != null && isMarked -> "ANSWERED_AND_MARKED_FOR_REVIEW"
                userAns != null -> "ANSWERED"
                isMarked -> "MARKED_FOR_REVIEW"
                else -> "UNATTEMPTED"
            }
            val ansIdx = when (userAns) {
                "A" -> 0
                "B" -> 1
                "C" -> 2
                "D" -> 3
                else -> null
            }
            repository.updateImportedQuestionAnswerState(
                questionId = q.id,
                userAnswer = userAns,
                userAnswerIndex = ansIdx,
                isAttempted = userAns != null,
                isMarkedForReview = isMarked,
                answerState = ansState,
                timeSpentSeconds = 30,
                isCorrect = isCorrect
            )
        }
    }

    onComplete(
        TestSubmissionResult(
            score = score,
            maxScore = maxScore,
            accuracy = accuracy,
            correctCount = correct,
            wrongCount = wrong,
            unattemptedCount = unattempted
        )
    )
}

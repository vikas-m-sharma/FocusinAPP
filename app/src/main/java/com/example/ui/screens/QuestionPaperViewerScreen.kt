package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NeetQuestion
import com.example.data.model.toNeetQuestion
import com.example.data.repository.NeetFullPaperRepository
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.PdfPaperManager
import com.example.viewmodel.FocusinViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

enum class PaperViewerMode {
    SPLIT,      // Half PDF, Half Interactive OMR/Question Answer Sheet
    PDF_ONLY,   // Full-screen PDF viewer with zoom controls & floating OMR FAB
    SOLVER_ONLY // Interactive 180-Question Card Mode
}

/**
 * QuestionPaperViewer Screen:
 * Allows users to view official 180-question NEET paper PDFs and solve questions simultaneously.
 * Includes state management system to track user answers, review statuses, timers,
 * and auto-save mistakes to Mistake Diary.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionPaperViewerScreen(
    year: Int = 2024,
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val paperObj = remember(year) {
        neet15YearsPdfArchive.find { it.year == year }
            ?: NeetPyqPdf(
                year = year,
                title = "NEET (UG) $year Official Paper",
                paperCode = "Code Q$year",
                questionsCount = 180,
                durationText = "3h 20m",
                pdfUrl = "https://nta.ac.in/Downloads",
                solutionUrl = "https://nta.ac.in/Downloads",
                fileSize = "4.2 MB"
            )
    }

    var isArchiveVerified by remember { mutableStateOf(true) }
    var isLoadingPaper by remember { mutableStateOf(true) }
    var showNtaOfficialModal by remember { mutableStateOf(false) }

    val database = remember { com.example.data.local.AppDatabase.getDatabase(context) }
    val dbQuestionsFlow = remember(year) { database.learningDao().getQuestionsForYear(year) }
    val dbQuestions by dbQuestionsFlow.collectAsState(initial = emptyList())

    var fullPopulatedQuestions by remember { mutableStateOf<List<NeetQuestion>>(emptyList()) }

    val questionsList = remember(fullPopulatedQuestions, dbQuestions) {
        if (fullPopulatedQuestions.isNotEmpty()) {
            fullPopulatedQuestions
        } else if (dbQuestions.isNotEmpty()) {
            dbQuestions.map { it.toNeetQuestion() }
        } else {
            emptyList()
        }
    }

    // ==========================================
    // STATE MANAGEMENT SYSTEM
    // ==========================================
    // Selected options: stable mapping questionId -> option ("A", "B", "C", "D")
    val selectedOptions = remember { mutableStateMapOf<String, String>() }
    // Marked for review: stable mapping questionId -> Boolean
    val markedForReview = remember { mutableStateMapOf<String, Boolean>() }
    // Question time spent (seconds): stable mapping questionId -> Int
    val questionTimeSpentMap = remember { mutableStateMapOf<String, Int>() }

    var viewerMode by remember { mutableStateOf(PaperViewerMode.SPLIT) }
    var selectedSubjectTab by remember { mutableStateOf("ALL") } // ALL, PHYSICS, CHEMISTRY, BOTANY, ZOOLOGY
    var currentIndex by remember { mutableIntStateOf(0) }

    var isSubmitted by remember { mutableStateOf(false) }
    var showOmrSheetModal by remember { mutableStateOf(false) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    // PDF State
    var totalPages by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPdfPage by remember { mutableStateOf(false) }

    // Zoom & Pan for PDF
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // 200 minutes NTA NEET Timer
    val totalTimeSeconds = 200 * 60L
    val startEpoch = remember { System.currentTimeMillis() }
    var remainingSeconds by remember { mutableLongStateOf(totalTimeSeconds) }

    // Per-question speed timer
    var activeQuestionTime by remember { mutableIntStateOf(0) }

    // Helper to render PDF page
    fun loadPdfPage(index: Int) {
        val file = PdfPaperManager.getPdfFileForYear(context, paperObj.year)
        if (!file.exists()) return

        isLoadingPdfPage = true
        scope.launch {
            val bmp = withContext(Dispatchers.IO) {
                PdfPaperManager.renderPdfPage(file, index, destWidth = 1080)
            }
            currentBitmap = bmp
            currentPageIndex = index
            isLoadingPdfPage = false
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        }
    }

    // Auto-populate 180 questions and initialize PDF
    LaunchedEffect(year) {
        isLoadingPaper = true
        val full180 = NeetFullPaperRepository.getOrPopulateFull180Paper(context, year)
        fullPopulatedQuestions = full180

        val file = PdfPaperManager.downloadOrGeneratePaperPdf(context, paperObj)
        totalPages = PdfPaperManager.getPageCount(file).coerceAtLeast(1)
        loadPdfPage(0)
        isArchiveVerified = true
        isLoadingPaper = false
    }

    LaunchedEffect(currentIndex, isSubmitted, questionsList) {
        val q = questionsList.getOrNull(currentIndex)
        if (q != null) {
            activeQuestionTime = questionTimeSpentMap[q.id] ?: 0
            while (!isSubmitted) {
                delay(1000L)
                activeQuestionTime++
                questionTimeSpentMap[q.id] = activeQuestionTime
            }
        }
    }

    // Overall exam countdown timer
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

    // Results computation based on authentic question count and stable IDs
    val totalQuestions = questionsList.size
    val answeredCount = selectedOptions.size
    val reviewCount = markedForReview.count { it.value }
    val correctCount = questionsList.count { q ->
        val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
        selectedOptions[q.id] == correctOptKey
    }
    val incorrectCount = questionsList.count { q ->
        val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
        val sel = selectedOptions[q.id]
        sel != null && sel != correctOptKey
    }
    val unattemptedCount = (totalQuestions - answeredCount).coerceAtLeast(0)
    val neetScore = (correctCount * 4) - (incorrectCount * 1)
    val maxScore = totalQuestions * 4
    val accuracy = if (answeredCount > 0) ((correctCount.toFloat() / answeredCount) * 100).toInt() else 0
    val timeSpentSeconds = (totalTimeSeconds - remainingSeconds).toInt()

    // Save test result and log mistakes upon submit
    LaunchedEffect(isSubmitted) {
        if (isSubmitted && questionsList.isNotEmpty()) {
            val weak = questionsList.filter { q ->
                val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
                selectedOptions[q.id] != null && selectedOptions[q.id] != correctOptKey
            }.map { it.topicName }.distinct()

            val strong = questionsList.filter { q ->
                val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
                selectedOptions[q.id] == correctOptKey
            }.map { it.topicName }.distinct()

            viewModel.recordQuizAttempt(
                com.example.data.local.entity.QuizAttemptRecordEntity(
                    title = "NEET $year Official Paper",
                    examId = "NEET",
                    subjectName = "Full Syllabus",
                    chapterName = if (totalQuestions >= 180) "NEET $year Official 180 Qs" else "NEET $year ($totalQuestions Questions)",
                    totalQuestions = totalQuestions,
                    correctCount = correctCount,
                    scorePercentage = accuracy,
                    strongTopicsJson = strong.joinToString(",", "[", "]") { "\"$it\"" },
                    weakTopicsJson = weak.joinToString(",", "[", "]") { "\"$it\"" }
                )
            )

            // Record question attempts & auto-save mistakes for genuinely incorrect answers
            questionsList.forEach { q ->
                val selected = selectedOptions[q.id]
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
                            timeSpentSeconds = (questionTimeSpentMap[q.id] ?: 10),
                            quizType = "QUESTION_PAPER_VIEWER"
                        )
                    )

                    if (!isCorr) {
                        viewModel.recordMistake(
                            questionId = q.id,
                            testTitle = "NEET $year Official Paper",
                            questionText = q.questionText,
                            selectedOption = q.options.getOrElse(listOf("A", "B", "C", "D").indexOf(selected)) { selected },
                            correctOption = q.options.getOrElse(q.correctOptionIndex) { correctOptKey },
                            options = q.options,
                            explanation = q.explanation,
                            subjectName = q.subjectName,
                            topicName = q.topicName
                        )
                    }
                }
            }
        }
    }

    // Exit confirmation dialog
    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            containerColor = Slate900,
            title = { Text("Exit Paper Viewer?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Your ongoing paper answers will not be saved if you exit now.", color = Color(0xFF94A3B8), fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmation = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Exit", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Resume", color = CyanPrimary)
                }
            }
        )
    }

    // Submit confirmation dialog
    if (showSubmitConfirmation) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            containerColor = Slate900,
            title = { Text("Submit NEET $year Paper?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Exam Summary:",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Answered:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("$answeredCount of $totalQuestions", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Unanswered:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("$unattemptedCount of $totalQuestions", color = if (unattemptedCount > 0) Color(0xFFFBBF24) else Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Marked for Review:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text("$reviewCount", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    if (unattemptedCount > 0) {
                        Surface(
                            color = Color(0xFFFBBF24).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ You still have $unattemptedCount unanswered questions.",
                                color = Color(0xFFFBBF24),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
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
                    Text("Continue Test", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Official NTA Exam Resources & Links Modal
    if (showNtaOfficialModal) {
        AlertDialog(
            onDismissRequest = { showNtaOfficialModal = false },
            containerColor = Slate900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Official NTA Paper Resources", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Access genuine examination papers, answer keys, and official portals directly from the National Testing Agency (NTA):",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 18.sp
                    )

                    // 1. NTA Official NEET Portal
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://exams.nta.ac.in/NEET/"))
                                context.startActivity(intent)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NTA NEET Official Portal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("exams.nta.ac.in/NEET • Official updates", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }

                    // 2. NTA Public Downloads Archive
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://nta.ac.in/Downloads"))
                                context.startActivity(intent)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NTA Question Papers & Keys", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("nta.ac.in/Downloads • Master papers archive", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }

                    // 3. NTA National Test Abhyas Portal
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nta.ac.in/Abhyas"))
                                context.startActivity(intent)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NTA National Test Abhyas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("nta.ac.in/Abhyas • Official mock tests", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }

                    // 4. Save PDF to Downloads
                    Surface(
                        color = Slate850,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    val localFile = PdfPaperManager.getPdfFileForYear(context, paperObj.year)
                                    if (localFile.exists()) {
                                        val saved = PdfPaperManager.savePdfToDownloads(context, localFile, "NEET_${paperObj.year}_Official_180Q_Paper.pdf")
                                        if (saved) {
                                            Toast.makeText(context, "Saved NEET ${paperObj.year} 180-Question PDF to Downloads!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "PDF saved to app storage.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Save 180-Question PDF to Device", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Exports complete official paper to device storage", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNtaOfficialModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Close", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal Bottom Sheet: Full OMR Grid Sheet
    if (showOmrSheetModal) {
        ModalBottomSheet(
            onDismissRequest = { showOmrSheetModal = false },
            containerColor = Slate900,
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("$totalQuestions-Question OMR Answering Sheet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Answered: $answeredCount | Marked for Review: $reviewCount", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    Button(
                        onClick = {
                            showOmrSheetModal = false
                            showSubmitConfirmation = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Submit Test", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Subject Counts & Tabs derived directly from paper questions
                val physicsCount = remember(questionsList) { questionsList.count { it.subjectName.uppercase().contains("PHYSIC") } }
                val chemistryCount = remember(questionsList) { questionsList.count { it.subjectName.uppercase().contains("CHEM") } }
                val botanyCount = remember(questionsList) { questionsList.count { it.subjectName.uppercase().contains("BOTANY") } }
                val zoologyCount = remember(questionsList) { questionsList.count { it.subjectName.uppercase().contains("ZOO") } }
                val biologyCount = remember(questionsList) { questionsList.count { it.subjectName.uppercase().contains("BIO") || it.subjectName.uppercase().contains("BOTANY") || it.subjectName.uppercase().contains("ZOO") } }

                val availableSubjectTabs = remember(questionsList) {
                    val tabs = mutableListOf("ALL ($totalQuestions)")
                    if (physicsCount > 0) tabs.add("PHYSICS ($physicsCount)")
                    if (chemistryCount > 0) tabs.add("CHEMISTRY ($chemistryCount)")
                    if (botanyCount > 0 && zoologyCount > 0) {
                        tabs.add("BOTANY ($botanyCount)")
                        tabs.add("ZOOLOGY ($zoologyCount)")
                    } else if (biologyCount > 0) {
                        tabs.add("BIOLOGY ($biologyCount)")
                    }
                    tabs
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableSubjectTabs.forEach { tabLabel ->
                        val rawSubject = tabLabel.substringBefore(" (").trim()
                        val isSel = selectedSubjectTab == rawSubject || (selectedSubjectTab == "ALL" && rawSubject == "ALL")
                        Surface(
                            modifier = Modifier
                                .clickable { selectedSubjectTab = rawSubject }
                                .border(1.dp, if (isSel) CyanPrimary else Slate800, RoundedCornerShape(16.dp)),
                            color = if (isSel) CyanPrimary.copy(alpha = 0.15f) else Slate850,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = tabLabel,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) CyanPrimary else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filtered Question Grid based on Subject
                val filteredIndices = remember(questionsList, selectedSubjectTab) {
                    questionsList.indices.filter { idx ->
                        if (selectedSubjectTab == "ALL") return@filter true
                        val q = questionsList.getOrNull(idx) ?: return@filter false
                        val s = q.subjectName.uppercase().trim()
                        val t = q.topicName.uppercase().trim()
                        when (selectedSubjectTab) {
                            "PHYSICS" -> s.contains("PHYSIC")
                            "CHEMISTRY" -> s.contains("CHEM")
                            "BOTANY" -> s.contains("BOTANY") || (s.contains("BIO") && (t.contains("BOTANY") || !t.contains("ZOO")))
                            "ZOOLOGY" -> s.contains("ZOO") || (s.contains("BIO") && t.contains("ZOO"))
                            "BIOLOGY" -> s.contains("BIO") || s.contains("BOTANY") || s.contains("ZOO")
                            else -> s.contains(selectedSubjectTab.uppercase()) || t.contains(selectedSubjectTab.uppercase())
                        }
                    }
                }

                if (filteredIndices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No $selectedSubjectTab questions found in this paper",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedButton(
                                onClick = { selectedSubjectTab = "ALL" },
                                border = BorderStroke(1.dp, CyanPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Show All Questions", color = CyanPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    ) {
                        items(filteredIndices) { index ->
                            val q = questionsList[index]
                            val isAnswered = selectedOptions.containsKey(q.id)
                            val isReview = markedForReview[q.id] == true
                            val isCurrent = index == currentIndex

                            val bgColor = when {
                                isCurrent -> CyanPrimary
                                isReview -> Color(0xFFFBBF24)
                                isAnswered -> EmeraldSuccess
                                else -> Slate850
                            }

                            val textColor = when {
                                isCurrent || isReview -> Slate950
                                else -> Color.White
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(
                                        width = if (isCurrent) 2.dp else if (isAnswered && isReview) 2.dp else 1.dp,
                                        color = if (isCurrent) Color.White else if (isAnswered && isReview) EmeraldSuccess else Slate800,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        currentIndex = index
                                        if (totalPages > 0 && questionsList.isNotEmpty()) {
                                            val targetPage = (index * totalPages / questionsList.size).coerceIn(0, totalPages - 1)
                                            loadPdfPage(targetPage)
                                        }
                                        showOmrSheetModal = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Q${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    if (isAnswered) {
                                        Text(
                                            text = selectedOptions[q.id] ?: "",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isCurrent || isReview) Slate950 else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate900, titleContentColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSubmitted) onNavigateBack() else showExitConfirmation = true
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = if (isSubmitted) "Evaluation Result" else "NEET $year Paper Viewer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val paperTypeBadge = if (totalQuestions >= 180) "FULL OFFICIAL PAPER" else "PARTIAL PAPER"
                        Text(
                            text = "$paperTypeBadge • $totalQuestions Questions • Q${currentIndex + 1} of $totalQuestions • $answeredCount Answered",
                            fontSize = 11.sp,
                            color = CyanPrimary
                        )
                    }
                },
                actions = {
                    if (!isSubmitted) {
                        // Timer Pill
                        val minutes = remainingSeconds / 60
                        val seconds = remainingSeconds % 60
                        val timerColor = if (remainingSeconds < 300) RoseError else CyanPrimary

                        Surface(
                            color = timerColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, timerColor.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = timerColor, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = timerColor
                                )
                            }
                        }

                        // Official NTA Resources Button
                        IconButton(onClick = { showNtaOfficialModal = true }) {
                            Icon(Icons.Default.Language, contentDescription = "Official NTA Resources", tint = CyanPrimary)
                        }

                        // Mode Selector Button (SPLIT, PDF_ONLY, SOLVER_ONLY)
                        IconButton(onClick = {
                            viewerMode = when (viewerMode) {
                                PaperViewerMode.SPLIT -> PaperViewerMode.PDF_ONLY
                                PaperViewerMode.PDF_ONLY -> PaperViewerMode.SOLVER_ONLY
                                PaperViewerMode.SOLVER_ONLY -> PaperViewerMode.SPLIT
                            }
                        }) {
                            Icon(
                                imageVector = when (viewerMode) {
                                    PaperViewerMode.SPLIT -> Icons.Default.ViewAgenda
                                    PaperViewerMode.PDF_ONLY -> Icons.Default.PictureAsPdf
                                    PaperViewerMode.SOLVER_ONLY -> Icons.Default.Edit
                                },
                                contentDescription = "Switch View Mode",
                                tint = CyanPrimary
                            )
                        }

                        // Open 180-OMR Sheet Grid
                        IconButton(onClick = { showOmrSheetModal = true }) {
                            Icon(Icons.Default.GridOn, contentDescription = "OMR Sheet", tint = Color.White)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSubmitted && viewerMode == PaperViewerMode.PDF_ONLY) {
                FloatingActionButton(
                    onClick = { showOmrSheetModal = true },
                    containerColor = CyanPrimary,
                    contentColor = Slate950
                ) {
                    Icon(Icons.Default.GridOn, contentDescription = "Open OMR Answering Sheet")
                }
            }
        }
    ) { innerPadding ->
        if (isLoadingPaper) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
                        Text(
                            text = "Loading Official NEET $year Paper...",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Preparing all 180 official examination questions (Physics, Chemistry, Botany, Zoology) & authentic exam PDF.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        } else if (isSubmitted) {
            // ==========================================
            // EVALUATION RESULT DASHBOARD
            // ==========================================
            QuestionPaperResultView(
                year = year,
                score = neetScore,
                maxScore = maxScore,
                accuracy = accuracy,
                timeSpentSeconds = timeSpentSeconds,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                unattemptedCount = unattemptedCount,
                questions = questionsList,
                selectedOptions = selectedOptions,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            // ==========================================
            // INTERACTIVE QUESTION PAPER VIEWER
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Slate950)
            ) {
                // View Mode Control Bar
                Surface(
                    color = Slate900,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ViewModeChip(title = "Split View", isSelected = viewerMode == PaperViewerMode.SPLIT) {
                                viewerMode = PaperViewerMode.SPLIT
                            }
                            ViewModeChip(title = "PDF View", isSelected = viewerMode == PaperViewerMode.PDF_ONLY) {
                                viewerMode = PaperViewerMode.PDF_ONLY
                            }
                            ViewModeChip(title = "Card Solver", isSelected = viewerMode == PaperViewerMode.SOLVER_ONLY) {
                                viewerMode = PaperViewerMode.SOLVER_ONLY
                            }
                        }

                        Button(
                            onClick = { showSubmitConfirmation = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Submit", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Main Content View depending on mode
                when (viewerMode) {
                    PaperViewerMode.SPLIT -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top 48%: PDF Viewer Window
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Slate800),
                                contentAlignment = Alignment.Center
                            ) {
                                PdfRenderBox(
                                    bitmap = currentBitmap,
                                    isLoading = isLoadingPdfPage,
                                    currentPage = currentPageIndex,
                                    totalPages = totalPages,
                                    scale = scale,
                                    offsetX = offsetX,
                                    offsetY = offsetY,
                                    onScaleChange = { s, x, y ->
                                        scale = s
                                        offsetX = x
                                        offsetY = y
                                    },
                                    onNextPage = { if (currentPageIndex < totalPages - 1) loadPdfPage(currentPageIndex + 1) },
                                    onPrevPage = { if (currentPageIndex > 0) loadPdfPage(currentPageIndex - 1) }
                                )
                            }

                            // Bottom 52%: Interactive Question Answering Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1.1f)
                                    .background(Slate950)
                            ) {
                                val currentQ = questionsList.getOrNull(currentIndex)
                                InteractiveQuestionPanel(
                                    currentIndex = currentIndex,
                                    totalQuestions = totalQuestions,
                                    question = currentQ,
                                    selectedOption = currentQ?.let { selectedOptions[it.id] },
                                    isMarkedForReview = currentQ?.let { markedForReview[it.id] == true } ?: false,
                                    timeSpentSeconds = activeQuestionTime,
                                    onOptionSelected = { opt ->
                                        currentQ?.let { selectedOptions[it.id] = opt }
                                    },
                                    onToggleReview = {
                                        currentQ?.let { markedForReview[it.id] = !(markedForReview[it.id] ?: false) }
                                    },
                                    onPrevQuestion = { if (currentIndex > 0) currentIndex-- },
                                    onNextQuestion = { if (currentIndex < totalQuestions - 1) currentIndex++ },
                                    onOpenOmrSheet = { showOmrSheetModal = true },
                                    onSubmitTest = { showSubmitConfirmation = true }
                                )
                            }
                        }
                    }

                    PaperViewerMode.PDF_ONLY -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            PdfRenderBox(
                                bitmap = currentBitmap,
                                isLoading = isLoadingPdfPage,
                                currentPage = currentPageIndex,
                                totalPages = totalPages,
                                scale = scale,
                                offsetX = offsetX,
                                offsetY = offsetY,
                                onScaleChange = { s, x, y ->
                                    scale = s
                                    offsetX = x
                                    offsetY = y
                                },
                                onNextPage = { if (currentPageIndex < totalPages - 1) loadPdfPage(currentPageIndex + 1) },
                                onPrevPage = { if (currentPageIndex > 0) loadPdfPage(currentPageIndex - 1) }
                            )
                        }
                    }

                    PaperViewerMode.SOLVER_ONLY -> {
                        val currentQ = questionsList.getOrNull(currentIndex)
                        InteractiveQuestionPanel(
                            currentIndex = currentIndex,
                            totalQuestions = totalQuestions,
                            question = currentQ,
                            selectedOption = currentQ?.let { selectedOptions[it.id] },
                            isMarkedForReview = currentQ?.let { markedForReview[it.id] == true } ?: false,
                            timeSpentSeconds = activeQuestionTime,
                            onOptionSelected = { opt ->
                                currentQ?.let { selectedOptions[it.id] = opt }
                            },
                            onToggleReview = {
                                currentQ?.let { markedForReview[it.id] = !(markedForReview[it.id] ?: false) }
                            },
                            onPrevQuestion = { if (currentIndex > 0) currentIndex-- },
                            onNextQuestion = { if (currentIndex < totalQuestions - 1) currentIndex++ },
                            onOpenOmrSheet = { showOmrSheetModal = true },
                            onSubmitTest = { showSubmitConfirmation = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewModeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .border(1.dp, if (isSelected) CyanPrimary else Slate700, RoundedCornerShape(12.dp)),
        color = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else Slate800,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CyanPrimary else Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun PdfRenderBox(
    bitmap: Bitmap?,
    isLoading: Boolean,
    currentPage: Int,
    totalPages: Int,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float, Float, Float) -> Unit,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit
) {
    if (isLoading) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(28.dp))
            Text("Rendering PDF Page ${currentPage + 1}...", color = Color(0xFF94A3B8), fontSize = 11.sp)
        }
    } else if (bitmap != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 3.5f)
                        if (newScale > 1f) {
                            val maxPanX = (size.width * (newScale - 1f)) / 2f
                            val maxPanY = (size.height * (newScale - 1f)) / 2f
                            val newX = (offsetX + pan.x).coerceIn(-maxPanX, maxPanX)
                            val newY = (offsetY + pan.y).coerceIn(-maxPanY, maxPanY)
                            onScaleChange(newScale, newX, newY)
                        } else {
                            onScaleChange(1f, 0f, 0f)
                        }
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "PDF Page ${currentPage + 1}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.FillWidth
            )
        }

        // Overlay Page controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = Slate900.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onPrevPage, enabled = currentPage > 0, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Prev Page", tint = if (currentPage > 0) CyanPrimary else Color.Gray, modifier = Modifier.size(12.dp))
                    }
                    Text("Page ${currentPage + 1} / ${totalPages.coerceAtLeast(1)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = onNextPage, enabled = currentPage < totalPages - 1, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Page", tint = if (currentPage < totalPages - 1) CyanPrimary else Color.Gray, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    } else {
        Text("No PDF Page Loaded", color = RoseError, fontSize = 12.sp)
    }
}

@Composable
fun InteractiveQuestionPanel(
    currentIndex: Int,
    totalQuestions: Int,
    question: NeetQuestion?,
    selectedOption: String?,
    isMarkedForReview: Boolean,
    timeSpentSeconds: Int,
    onOptionSelected: (String) -> Unit,
    onToggleReview: () -> Unit,
    onPrevQuestion: () -> Unit,
    onNextQuestion: () -> Unit,
    onOpenOmrSheet: () -> Unit,
    onSubmitTest: () -> Unit
) {
    if (question == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Question Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(color = Slate850, shape = RoundedCornerShape(6.dp)) {
                    Text("Q${currentIndex + 1} of $totalQuestions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Surface(color = Slate850, shape = RoundedCornerShape(6.dp)) {
                    Text("${question.subjectName} • ${question.topicName}", fontSize = 11.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Surface(
                modifier = Modifier.clickable { onToggleReview() },
                color = if (isMarkedForReview) Color(0xFFFBBF24).copy(alpha = 0.15f) else Slate850,
                border = if (isMarkedForReview) BorderStroke(1.dp, Color(0xFFFBBF24)) else null,
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = if (isMarkedForReview) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null, tint = if (isMarkedForReview) Color(0xFFFBBF24) else Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isMarkedForReview) "Review" else "Mark", fontSize = 11.sp, color = if (isMarkedForReview) Color(0xFFFBBF24) else Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Question Statement Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Slate800, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = question.questionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                }

                // Options List A, B, C, D
                val optionKeys = listOf("A", "B", "C", "D")
                items(question.options.size) { idx ->
                    val optKey = optionKeys.getOrElse(idx) { "A" }
                    val optText = question.options[idx]
                    val isSelected = selectedOption == optKey

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(optKey) }
                            .border(1.dp, if (isSelected) CyanPrimary else Slate800, RoundedCornerShape(10.dp)),
                        color = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else Slate850,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CyanPrimary else Slate800),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(optKey, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Slate950 else Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(optText, fontSize = 13.sp, color = Color.White, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Question Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrevQuestion,
                enabled = currentIndex > 0,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, if (currentIndex > 0) Slate700 else Slate850),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Previous", fontSize = 12.sp, color = if (currentIndex > 0) Color.White else Color(0xFF64748B))
            }

            OutlinedButton(
                onClick = onOpenOmrSheet,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("$totalQuestions Qs OMR Grid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (currentIndex < totalQuestions - 1) {
                Button(
                    onClick = onNextQuestion,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Next", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onSubmitTest,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Slate950, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Submit Test", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun QuestionPaperResultView(
    year: Int,
    score: Int,
    maxScore: Int,
    accuracy: Int,
    timeSpentSeconds: Int,
    correctCount: Int,
    incorrectCount: Int,
    unattemptedCount: Int,
    questions: List<NeetQuestion>,
    selectedOptions: Map<String, String>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = BorderStroke(1.dp, CyanPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NEET $year Official Paper Evaluation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("$score / $maxScore", fontSize = 36.sp, fontWeight = FontWeight.Black, color = CyanPrimary)
                    Text("NTA NEET Score (+4 Correct, -1 Incorrect)", fontSize = 12.sp, color = Color(0xFF94A3B8))

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultMetricPill(label = "Correct (+4)", value = "$correctCount", color = EmeraldSuccess)
                        ResultMetricPill(label = "Incorrect (-1)", value = "$incorrectCount", color = RoseError)
                        ResultMetricPill(label = "Unattempted", value = "$unattemptedCount", color = Color(0xFFFBBF24))
                        ResultMetricPill(label = "Accuracy", value = "$accuracy%", color = CyanPrimary)
                    }
                }
            }
        }

        item {
            Surface(
                color = EmeraldSuccess.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                    Text(
                        text = "All $incorrectCount mistakes were automatically logged into your Mistake Diary for focused revision!",
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Return to Question Bank", color = Slate950, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ResultMetricPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
    }
}

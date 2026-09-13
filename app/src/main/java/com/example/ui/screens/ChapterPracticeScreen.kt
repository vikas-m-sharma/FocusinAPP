package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.QuestionAttemptRecordEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.model.neetBiologyChapters
import com.example.data.model.neetChemistryChapters
import com.example.data.model.neetPhysicsChapters
import com.example.data.model.toNeetQuestion
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterPracticeScreen(
    chapterId: String,
    mode: String,
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMockTest: ((String) -> Unit)? = null
) {
    val chapter = remember(chapterId) {
        (neetPhysicsChapters + neetChemistryChapters + neetBiologyChapters)
            .find { it.id == chapterId } ?: neetPhysicsChapters.first()
    }

    // Real Room DB queries for chapter questions
    val allQuestions by viewModel.getQuestionsForChapter(chapterId).collectAsState(initial = emptyList())
    val yearCounts by viewModel.getQuestionCountByYear(chapterId).collectAsState(initial = emptyList())
    val attempts by viewModel.questionAttemptsList.collectAsState(initial = emptyList())

    // Calculate real stats for this chapter
    val chapterAttempts = remember(attempts, chapterId, chapter.name) {
        attempts.filter { it.chapterName.equals(chapter.name, ignoreCase = true) || it.chapterName.equals(chapterId, ignoreCase = true) }
    }
    val attemptedQuestionsIds = remember(chapterAttempts) {
        chapterAttempts.map { it.questionId }.toSet()
    }
    val correctAttemptsCount = remember(chapterAttempts) {
        chapterAttempts.count { it.isCorrect }
    }

    val verifiedCount = remember(allQuestions) {
        allQuestions.count { it.sourceVerificationStatus == "VERIFIED" }
    }
    val unverifiedCount = remember(allQuestions) {
        allQuestions.count { it.sourceVerificationStatus == "UNVERIFIED" }
    }
    val missingYears = remember(allQuestions) {
        val presentYears = allQuestions.mapNotNull { it.examYear }.toSet()
        (2005..2025).filter { !presentYears.contains(it) }
    }

    // Filter states
    var selectedExamFilter by remember { mutableStateOf<String?>(null) } // null = ALL, "NEET_UG", "AIPMT"
    var selectedYearFilter by remember { mutableStateOf("ALL") } // "ALL", "LAST_5", "LAST_10", "2005_2025"
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "UNANSWERED", "MISTAKES"
    var selectedQuestionCount by remember { mutableIntStateOf(25) }

    // Mode state: false = Chapter PYQ Hub, true = Active Instant Practice
    var isInstantPracticeActive by remember { mutableStateOf(mode.equals("instant", ignoreCase = true)) }

    // Filter questions based on student selections
    val filteredQuestions = remember(
        allQuestions,
        selectedExamFilter,
        selectedYearFilter,
        selectedStatusFilter,
        selectedQuestionCount,
        attemptedQuestionsIds,
        chapterAttempts
    ) {
        var list = allQuestions

        // 1. Exam filter
        if (selectedExamFilter != null) {
            list = list.filter { it.sourceExam.equals(selectedExamFilter, ignoreCase = true) }
        }

        // 2. Year filter
        when (selectedYearFilter) {
            "LAST_5" -> list = list.filter { it.examYear != null && it.examYear >= 2020 }
            "LAST_10" -> list = list.filter { it.examYear != null && it.examYear >= 2015 }
            "2005_2025" -> list = list.filter { it.examYear != null && it.examYear in 2005..2025 }
        }

        // 3. Status filter
        when (selectedStatusFilter) {
            "UNANSWERED" -> list = list.filter { !attemptedQuestionsIds.contains(it.id) }
            "MISTAKES" -> {
                val mistakeQuestionIds = chapterAttempts.filter { !it.isCorrect }.map { it.questionId }.toSet()
                list = list.filter { mistakeQuestionIds.contains(it.id) }
            }
        }

        // 4. Question Count filter
        if (selectedQuestionCount < Int.MAX_VALUE) {
            list.take(selectedQuestionCount)
        } else {
            list
        }
    }

    if (isInstantPracticeActive) {
        // Active Instant Practice Screen
        InstantPracticeView(
            chapterName = chapter.name,
            questions = filteredQuestions,
            onCompleteAttempt = { q, selectedOptIndex, isCorrect, elapsedSec ->
                viewModel.recordQuestionAttempt(
                    QuestionAttemptRecordEntity(
                        questionId = q.id,
                        examId = q.sourceExam ?: "NEET_UG",
                        subjectName = q.subjectId,
                        chapterName = chapter.name,
                        topicName = q.topicName,
                        questionText = q.questionText,
                        selectedOptionIndex = selectedOptIndex,
                        correctOptionIndex = q.correctOptionIndex,
                        isCorrect = isCorrect,
                        timeSpentSeconds = elapsedSec,
                        quizType = if (q.isOfficialPYQ && q.sourceVerificationStatus == "VERIFIED") "OFFICIAL_PYQ" else if (q.sourceVerificationStatus == "UNVERIFIED") "UNVERIFIED_PYQ" else "PRACTICE"
                    )
                )

                if (!isCorrect) {
                    val correctOptKey = listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "A" }
                    val selectedKey = listOf("A", "B", "C", "D").getOrElse(selectedOptIndex) { "A" }
                    val optList = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                    viewModel.recordMistake(
                        questionId = q.id,
                        testTitle = "PYQ: ${chapter.name}",
                        questionText = q.questionText,
                        selectedOption = optList.getOrElse(selectedOptIndex) { selectedKey },
                        correctOption = optList.getOrElse(q.correctOptionIndex) { correctOptKey },
                        options = optList,
                        explanation = q.explanation,
                        subjectName = q.subjectId,
                        topicName = q.topicName,
                        sourceExam = q.sourceExam,
                        examYear = q.examYear
                    )
                }
            },
            onToggleBookmark = { qId, currBookmarked ->
                viewModel.toggleQuestionBookmark(qId, currBookmarked)
            },
            onSpeakText = { text ->
                viewModel.speakText(text)
            },
            onExitPractice = {
                isInstantPracticeActive = false
            }
        )
    } else {
        // CHAPTER PYQ HUB
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
                        Text(
                            text = "Historical PYQ Hub",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                )
            }
        ) { innerPadding ->
            ChapterPyqHubView(
                chapter = chapter,
                totalAvailable = allQuestions.size,
                verifiedCount = verifiedCount,
                unverifiedCount = unverifiedCount,
                attemptedCount = chapterAttempts.size,
                correctCount = correctAttemptsCount,
                yearDistribution = yearCounts,
                missingYears = missingYears,
                matchingQuestionsCount = filteredQuestions.size,
                selectedExamFilter = selectedExamFilter,
                onSelectExamFilter = { selectedExamFilter = it },
                selectedYearFilter = selectedYearFilter,
                onSelectYearFilter = { selectedYearFilter = it },
                selectedStatusFilter = selectedStatusFilter,
                onSelectStatusFilter = { selectedStatusFilter = it },
                selectedQuestionCount = selectedQuestionCount,
                onSelectQuestionCount = { selectedQuestionCount = it },
                onStartInstantPractice = {
                    isInstantPracticeActive = true
                },
                onStartOmrExam = {
                    // Set custom questions for MockTestScreen and navigate
                    val neetQuestions = filteredQuestions.map { q ->
                        q.toNeetQuestion(
                            chapterDisplayName = chapter.name,
                            subjectDisplayName = chapter.subjectName
                        )
                    }
                    viewModel.setCustomTestQuestions(neetQuestions)
                    val examLabel = selectedExamFilter ?: "NEET/AIPMT"
                    val testTitle = "$examLabel PYQs: ${chapter.name}"
                    onNavigateToMockTest?.invoke(testTitle)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

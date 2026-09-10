package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.QuestionAttemptRecordEntity
import com.example.data.model.NeetQuestion
import com.example.data.model.neetBiologyChapters
import com.example.data.model.neetChemistryChapters
import com.example.data.model.neetPhysicsChapters
import com.example.data.model.officialPyqPapers
import com.example.data.model.sampleNeetQuestions
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    viewModel: FocusinViewModel,
    onNavigateToSettings: () -> Unit
) {
    val questionAttempts by viewModel.questionAttemptsList.collectAsState()
    val quizAttempts by viewModel.quizAttemptsList.collectAsState()

    var activeTab by remember { mutableStateOf("SUBJECTS") } // "SUBJECTS", "PYQ", "AI_QUIZ"
    var selectedSubjectName by remember { mutableStateOf<String?>(null) }
    var selectedQuestionForPractice by remember { mutableStateOf<NeetQuestion?>(null) }

    val subjectsData = listOf(
        Triple("Physics", "45 Chapters • 12,500+ Questions", neetPhysicsChapters),
        Triple("Chemistry", "48 Chapters • 14,000+ Questions", neetChemistryChapters),
        Triple("Biology", "47 Chapters • 18,000+ Questions", neetBiologyChapters)
    )

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White
                ),
                title = {
                    Column {
                        Text(
                            text = "QUESTION BANK",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Practice. Analyze. Improve.",
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
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Tabs Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("SUBJECTS" to "📚 Chapters", "PYQ" to "📝 Official PYQs", "AI_QUIZ" to "🤖 AI Quiz").forEach { (key, label) ->
                        val isSel = activeTab == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) CyanPrimary else Slate900)
                                .clickable { activeTab = key }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Slate950 else Color.White
                            )
                        }
                    }
                }
            }

            if (activeTab == "SUBJECTS") {
                if (selectedSubjectName == null) {
                    // Subject List
                    subjectsData.forEach { (subName, subSubtitle, _) ->
                        item {
                            val subAttempts = questionAttempts.filter { it.subjectName == subName }
                            val correctAttempts = subAttempts.count { it.isCorrect }
                            val accuracyPct = if (subAttempts.isNotEmpty()) (correctAttempts * 100 / subAttempts.size) else 0

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                                    .clickable { selectedSubjectName = subName },
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(CyanPrimary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Quiz,
                                                    contentDescription = null,
                                                    tint = CyanPrimary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(subName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(subSubtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Open",
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Attempted: ${subAttempts.size}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        Text("Accuracy: $accuracyPct%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Chapter List for Selected Subject
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedSubjectName = null }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back to All Subjects ($selectedSubjectName)", fontSize = 13.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    val selectedSubjectTriple = subjectsData.find { it.first == selectedSubjectName }
                    val chapterList = selectedSubjectTriple?.third ?: emptyList()

                    chapterList.forEach { chap ->
                        item {
                            val chapAttempts = questionAttempts.filter { it.chapterName == chap.name }
                            val correct = chapAttempts.count { it.isCorrect }
                            val accuracy = if (chapAttempts.isNotEmpty()) (correct * 100 / chapAttempts.size) else 0

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
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(chap.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${chap.topics.size} Topics • ${chap.totalQuestionsCount}+ Questions", fontSize = 11.sp, color = Color(0xFF94A3B8))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Accuracy: $accuracy%", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)

                                        Button(
                                            onClick = {
                                                val matchedQ = sampleNeetQuestions.find { it.subjectName == selectedSubjectName } ?: sampleNeetQuestions.first()
                                                selectedQuestionForPractice = matchedQ.copy(chapterName = chap.name)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Practice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate950)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == "PYQ") {
                // Official PYQs
                item {
                    Text("NEET UG Official Previous Year Papers", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                officialPyqPapers.forEach { paper ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(paper.examName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("180 Questions • 200 Minutes • Official Exam", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }

                                Button(
                                    onClick = {
                                        selectedQuestionForPractice = sampleNeetQuestions.first()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Practice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate950)
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == "AI_QUIZ") {
                // AI Quiz Generator
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyanPrimary, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("AI QUIZ GENERATOR", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Generate custom practice quizzes powered by Gemini AI", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.generateGoalPlanWithAi("Generate 5 practice questions for NEET Physics Current Electricity")
                                    selectedQuestionForPractice = sampleNeetQuestions.first()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("⚡ GENERATE AI QUIZ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate950)
                            }
                        }
                    }
                }

                item {
                    Text("Recent Quiz Attempts (${quizAttempts.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                quizAttempts.forEach { q ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Slate800, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(q.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${q.correctCount} / ${q.totalQuestions} Correct", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Text("${q.scorePercentage}%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = EmeraldSuccess)
                            }
                        }
                    }
                }
            }
        }
    }

    // Question Practice Dialog
    selectedQuestionForPractice?.let { q ->
        QuestionPracticeDialog(
            question = q,
            onDismiss = { selectedQuestionForPractice = null },
            onSubmitAnswer = { selectedIndex, isCorrect ->
                viewModel.recordQuestionAttempt(
                    QuestionAttemptRecordEntity(
                        subjectName = q.subjectName,
                        chapterName = q.chapterName,
                        topicName = q.topicName,
                        questionText = q.questionText,
                        selectedOptionIndex = selectedIndex,
                        correctOptionIndex = q.correctOptionIndex,
                        isCorrect = isCorrect
                    )
                )
            }
        )
    }
}

@Composable
fun QuestionPracticeDialog(
    question: NeetQuestion,
    onDismiss: () -> Unit,
    onSubmitAnswer: (Int, Boolean) -> Unit
) {
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var isSubmitted by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyanPrimary, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${question.subjectName} • ${question.chapterName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Text(question.difficulty, fontSize = 10.sp, color = Color(0xFF94A3B8))
                }

                Text(
                    text = question.questionText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    lineHeight = 20.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.forEachIndexed { index, optionText ->
                        val isSelected = selectedOptionIndex == index
                        val isCorrect = index == question.correctOptionIndex

                        val bgColor = when {
                            isSubmitted && isCorrect -> EmeraldSuccess.copy(alpha = 0.25f)
                            isSubmitted && isSelected && !isCorrect -> RoseError.copy(alpha = 0.25f)
                            isSelected -> CyanPrimary.copy(alpha = 0.2f)
                            else -> Slate800
                        }

                        val borderColor = when {
                            isSubmitted && isCorrect -> EmeraldSuccess
                            isSubmitted && isSelected && !isCorrect -> RoseError
                            isSelected -> CyanPrimary
                            else -> Slate700
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .clickable {
                                    if (!isSubmitted) selectedOptionIndex = index
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "${('A' + index)}.  $optionText",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = if (isSelected || (isSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (isSubmitted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate950)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (selectedOptionIndex == question.correctOptionIndex) "🎉 Correct Answer!" else "❌ Incorrect Answer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedOptionIndex == question.correctOptionIndex) EmeraldSuccess else RoseError
                            )
                            Text(
                                text = "Explanation: ${question.explanation}",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSubmitted) {
                        Button(
                            onClick = {
                                if (selectedOptionIndex >= 0) {
                                    isSubmitted = true
                                    onSubmitAnswer(selectedOptionIndex, selectedOptionIndex == question.correctOptionIndex)
                                }
                            },
                            enabled = selectedOptionIndex >= 0,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Submit Answer", color = Slate950, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Done", color = Slate950, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    viewModel: FocusinViewModel,
    onNavigateToSubject: (String) -> Unit = {},
    onNavigateToPreviousYearPapers: () -> Unit = {},
    onNavigateToAiQuiz: (String) -> Unit = {},
    onNavigateToMockTests: () -> Unit = {},
    onNavigateToMyPractice: () -> Unit = {},
    onNavigateToBookmarks: () -> Unit = {},
    onNavigateToWeakTopics: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val neetChapters by viewModel.neetChapters.collectAsState()
    val totalAttempted by viewModel.totalQuestionsAttempted.collectAsState()
    val totalCorrect by viewModel.totalQuestionsCorrect.collectAsState()
    val mockTestsCount by viewModel.totalMockTestsCount.collectAsState()
    val allQuestionAttempts by viewModel.allQuestionAttempts.collectAsState()
    val weakTopics by viewModel.calculatedWeakTopics.collectAsState()

    var selectedExam by remember { mutableStateOf("NEET") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val totalAccuracy = if (totalAttempted > 0) ((totalCorrect.toFloat() / totalAttempted) * 100).toInt() else 0

    // Compute subject stats
    val physicsAttempts = allQuestionAttempts.filter { it.subjectId == "PHYSICS" }
    val chemistryAttempts = allQuestionAttempts.filter { it.subjectId == "CHEMISTRY" }
    val biologyAttempts = allQuestionAttempts.filter { it.subjectId == "BIOLOGY" }

    val physicsAccuracy = if (physicsAttempts.isNotEmpty()) ((physicsAttempts.count { it.isCorrect }.toFloat() / physicsAttempts.size) * 100).toInt() else 0
    val chemistryAccuracy = if (chemistryAttempts.isNotEmpty()) ((chemistryAttempts.count { it.isCorrect }.toFloat() / chemistryAttempts.size) * 100).toInt() else 0
    val biologyAccuracy = if (biologyAttempts.isNotEmpty()) ((biologyAttempts.count { it.isCorrect }.toFloat() / biologyAttempts.size) * 100).toInt() else 0

    val physicsChaptersCount = neetChapters.count { it.subjectId == "PHYSICS" }
    val chemistryChaptersCount = neetChapters.count { it.subjectId == "CHEMISTRY" }
    val biologyChaptersCount = neetChapters.count { it.subjectId == "BIOLOGY" }

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
                            text = "Question Bank",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Practice. Analyze. Improve.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isSearchActive = !isSearchActive },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchActive) CyanPrimary else Color(0xFF94A3B8)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF94A3B8)
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
            contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar when active
            if (isSearchActive) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qbank_search_input"),
                        placeholder = { Text("Search topics, chapters, formulas...", color = Color(0xFF64748B), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        )
                    )
                }
            }

            // Exam Selector Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExamPill(
                        title = "NEET",
                        isActive = selectedExam == "NEET",
                        onClick = { selectedExam = "NEET" }
                    )
                    ExamPill(
                        title = "JEE",
                        badge = "Soon",
                        isActive = false,
                        onClick = { }
                    )
                    ExamPill(
                        title = "Boards",
                        badge = "Soon",
                        isActive = false,
                        onClick = { }
                    )
                }
            }

            // Practice Journey Card
            item {
                PracticeJourneyCard(
                    attempted = totalAttempted,
                    accuracy = totalAccuracy,
                    testsCompleted = mockTestsCount,
                    onStartPractice = { onNavigateToSubject("PHYSICS") }
                )
            }

            // Weak Topics Alert Card (if any identified)
            if (weakTopics.isNotEmpty()) {
                item {
                    val topWeak = weakTopics.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToWeakTopics() }
                            .border(1.dp, RoseError.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(RoseError.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = RoseError,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Target Weak Area",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RoseError
                                    )
                                    Text(
                                        text = "${topWeak.topicName} (${topWeak.accuracy}%)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate800,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "Practice",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Subjects Section
            item {
                Text(
                    text = "Subjects",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            // Physics Card
            item {
                SubjectQBankCard(
                    subjectName = "Physics",
                    subtitle = "$physicsChaptersCount Chapters",
                    icon = Icons.Default.Science,
                    accentColor = Color(0xFF38BDF8),
                    attemptedCount = physicsAttempts.size,
                    accuracyPercent = physicsAccuracy,
                    onClick = { onNavigateToSubject("PHYSICS") }
                )
            }

            // Chemistry Card
            item {
                SubjectQBankCard(
                    subjectName = "Chemistry",
                    subtitle = "$chemistryChaptersCount Chapters",
                    icon = Icons.Default.Science,
                    accentColor = Color(0xFFA78BFA),
                    attemptedCount = chemistryAttempts.size,
                    accuracyPercent = chemistryAccuracy,
                    onClick = { onNavigateToSubject("CHEMISTRY") }
                )
            }

            // Biology Card
            item {
                SubjectQBankCard(
                    subjectName = "Biology",
                    subtitle = "$biologyChaptersCount Chapters",
                    icon = Icons.Default.Spa,
                    accentColor = Color(0xFF34D399),
                    attemptedCount = biologyAttempts.size,
                    accuracyPercent = biologyAccuracy,
                    onClick = { onNavigateToSubject("BIOLOGY") }
                )
            }

            // 5 Main Practice Options
            item {
                Text(
                    text = "Practice Modes & Tools",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            item {
                PracticeOptionRow(
                    title = "Previous Year Papers",
                    description = "NEET 2015 – 2025 official question papers",
                    badge = "High Yield",
                    icon = Icons.Default.MenuBook,
                    iconTint = CyanPrimary,
                    onClick = onNavigateToPreviousYearPapers
                )
            }

            item {
                PracticeOptionRow(
                    title = "AI Quiz Generator",
                    description = "Custom quizzes for any topic or difficulty",
                    badge = "AI Powered",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFFA78BFA),
                    onClick = { onNavigateToAiQuiz("ALL") }
                )
            }

            item {
                PracticeOptionRow(
                    title = "Mock Tests",
                    description = "Full syllabus timed tests with detailed results",
                    badge = "Simulated",
                    icon = Icons.Default.Timer,
                    iconTint = EmeraldSuccess,
                    onClick = onNavigateToMockTests
                )
            }

            item {
                PracticeOptionRow(
                    title = "My Practice",
                    description = "View your question history and performance",
                    badge = null,
                    icon = Icons.Default.History,
                    iconTint = Color(0xFF38BDF8),
                    onClick = onNavigateToMyPractice
                )
            }

            item {
                PracticeOptionRow(
                    title = "Bookmarks",
                    description = "Saved tricky and essential questions",
                    badge = null,
                    icon = Icons.Default.Bookmark,
                    iconTint = Color(0xFFFBBF24),
                    onClick = onNavigateToBookmarks
                )
            }

            item {
                PracticeOptionRow(
                    title = "Weak Topic Engine",
                    description = "Target topics where accuracy is below 65%",
                    badge = if (weakTopics.isNotEmpty()) "${weakTopics.size} Topics" else null,
                    icon = Icons.Default.Psychology,
                    iconTint = RoseError,
                    onClick = onNavigateToWeakTopics
                )
            }
        }
    }
}

@Composable
fun ExamPill(
    title: String,
    badge: String? = null,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(enabled = badge == null, onClick = onClick)
            .border(
                1.dp,
                if (isActive) CyanPrimary else Slate800,
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = if (isActive) CyanPrimary.copy(alpha = 0.15f) else Slate900
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) CyanPrimary else Color(0xFF94A3B8)
            )
            if (badge != null) {
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeJourneyCard(
    attempted: Int,
    accuracy: Int,
    testsCompleted: Int,
    onStartPractice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(18.dp)
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
                Text(
                    text = "Your Practice Journey",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    color = CyanPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "NEET 2026",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (attempted > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    JourneyStatItem(
                        title = "Attempted",
                        value = "$attempted",
                        subValue = "Questions",
                        color = CyanPrimary
                    )
                    JourneyStatItem(
                        title = "Accuracy",
                        value = "$accuracy%",
                        subValue = if (accuracy >= 70) "Strong" else "Developing",
                        color = if (accuracy >= 70) EmeraldSuccess else Color(0xFFFBBF24)
                    )
                    JourneyStatItem(
                        title = "Tests",
                        value = "$testsCompleted",
                        subValue = "Completed",
                        color = Color(0xFFA78BFA)
                    )
                }
            } else {
                Text(
                    text = "Solve questions, track progress, and master every NCERT concept for NEET.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .clickable(onClick = onStartPractice)
                        .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    color = CyanPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Start Solving Questions →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun JourneyStatItem(
    title: String,
    value: String,
    subValue: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = subValue,
            fontSize = 10.sp,
            color = Color(0xFF64748B)
        )
    }
}

@Composable
fun SubjectQBankCard(
    subjectName: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    attemptedCount: Int,
    accuracyPercent: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subjectName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (attemptedCount > 0) {
                        Text(
                            text = "$accuracyPercent% acc",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (accuracyPercent >= 65) EmeraldSuccess else Color(0xFFFBBF24)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (attemptedCount > 0) "$subtitle • $attemptedCount attempted" else "$subtitle • Not started",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                if (attemptedCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (accuracyPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = accentColor,
                        trackColor = Slate800
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PracticeOptionRow(
    title: String,
    description: String,
    badge: String?,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (badge != null) {
                        Surface(
                            color = iconTint.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconTint,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

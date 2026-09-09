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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterOptionsScreen(
    chapterId: String,
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (String, String) -> Unit, // chapterId, mode
    onNavigateToAiQuiz: (String) -> Unit
) {
    val chapter by viewModel.getChapterFlow(chapterId).collectAsState(initial = null)
    val topics by viewModel.getTopicsForChapter(chapterId).collectAsState(initial = emptyList())
    val questions by viewModel.getQuestionsForChapter(chapterId).collectAsState(initial = emptyList())
    val attempts by viewModel.getAttemptsForChapter(chapterId).collectAsState(initial = emptyList())

    val attemptedQuestionsCount = attempts.map { it.questionId }.distinct().size
    val totalQuestionsCount = if (questions.isNotEmpty()) questions.size else 15
    val correctAttempts = attempts.count { it.isCorrect }
    val accuracy = if (attempts.isNotEmpty()) ((correctAttempts.toFloat() / attempts.size) * 100).toInt() else 0

    val completedTopics = topics.count { it.status == "COMPLETED" }
    val totalTopics = if (topics.isNotEmpty()) topics.size else (chapter?.totalTopics ?: 10)
    val progressPercent = if (totalTopics > 0) ((completedTopics.toFloat() / totalTopics) * 100).toInt() else 0

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
                            text = chapter?.name ?: "Chapter Practice",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "NEET • ${chapter?.subjectId?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Physics"}",
                            fontSize = 12.sp,
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chapter Progress Card
            item {
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
                                text = "Chapter Mastery",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$progressPercent%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (progressPercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = CyanPrimary,
                            trackColor = Slate800
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$completedTopics / $totalTopics topics completed",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "$attemptedQuestionsCount / $totalQuestionsCount questions",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // 4 Primary Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrimaryActionCard(
                        title = "Practice",
                        icon = Icons.Default.PlayArrow,
                        color = CyanPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToPractice(chapterId, "PRACTICE") }
                    )
                    PrimaryActionCard(
                        title = "PYQs",
                        icon = Icons.Default.MenuBook,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToPractice(chapterId, "PYQ") }
                    )
                    PrimaryActionCard(
                        title = "AI Quiz",
                        icon = Icons.Default.AutoAwesome,
                        color = Color(0xFFA78BFA),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToAiQuiz(chapterId) }
                    )
                    PrimaryActionCard(
                        title = "Test",
                        icon = Icons.Default.Timer,
                        color = EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToPractice(chapterId, "TIMED_TEST") }
                    )
                }
            }

            // Practice Mode Options
            item {
                Text(
                    text = "Practice Modes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                ChapterPracticeModeRow(
                    title = "Topic-wise Practice",
                    description = "Choose specific NCERT concepts to master",
                    icon = Icons.Default.FilterList,
                    iconColor = CyanPrimary,
                    onClick = { onNavigateToPractice(chapterId, "TOPIC_WISE") }
                )
            }

            item {
                ChapterPracticeModeRow(
                    title = "Mixed Practice",
                    description = "Random selection from all chapter topics",
                    icon = Icons.Default.Shuffle,
                    iconColor = Color(0xFF38BDF8),
                    onClick = { onNavigateToPractice(chapterId, "PRACTICE") }
                )
            }

            item {
                ChapterPracticeModeRow(
                    title = "Difficulty-wise (Easy • Medium • Hard)",
                    description = "Progress from foundational to advanced NEET problems",
                    icon = Icons.Default.Speed,
                    iconColor = Color(0xFFFBBF24),
                    onClick = { onNavigateToPractice(chapterId, "DIFFICULTY_WISE") }
                )
            }

            item {
                ChapterPracticeModeRow(
                    title = "Weak Topic Targeted Practice",
                    description = "Focus on concepts where previous accuracy was lowest",
                    icon = Icons.Default.Psychology,
                    iconColor = RoseError,
                    onClick = { onNavigateToPractice(chapterId, "WEAK_TOPICS") }
                )
            }

            // Chapter Info Summary
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Chapter Performance",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Questions", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("$totalQuestionsCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Attempted", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("$attemptedQuestionsCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                            }
                            Column {
                                Text("Accuracy", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    if (attempts.isNotEmpty()) "$accuracy%" else "N/A",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (accuracy >= 65) EmeraldSuccess else Color(0xFFFBBF24)
                                )
                            }
                            Column {
                                Text("PYQ Weightage", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    "High (3-4 Qs)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA78BFA)
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
private fun PrimaryActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
        color = Slate900,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ChapterPracticeModeRow(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
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
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
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

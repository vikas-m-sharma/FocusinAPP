package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.neetBiologyChapters
import com.example.data.model.neetChemistryChapters
import com.example.data.model.neetPhysicsChapters
import com.example.viewmodel.FocusinViewModel

@Composable
fun QuestionBankScreen(
    viewModel: FocusinViewModel,
    onNavigateToSubject: (String) -> Unit = {},
    onNavigateToPyq: () -> Unit = {},
    onNavigateToAiQuiz: (String) -> Unit = {},
    onNavigateToMockTest: (String) -> Unit = {},
    onNavigateToMyPractice: () -> Unit = {},
    onNavigateToBookmarks: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var selectedExam by remember { mutableStateOf("NEET") }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val questionAttempts by viewModel.questionAttemptsList.collectAsState()

    // Calculated progress from actual attempts
    val physicsAttempts = questionAttempts.filter { it.subjectName.equals("Physics", ignoreCase = true) }
    val chemistryAttempts = questionAttempts.filter { it.subjectName.equals("Chemistry", ignoreCase = true) }
    val biologyAttempts = questionAttempts.filter { it.subjectName.equals("Biology", ignoreCase = true) }

    val physicsAccuracy = if (physicsAttempts.isNotEmpty()) {
        (physicsAttempts.count { it.isCorrect } * 100 / physicsAttempts.size)
    } else 58

    val chemistryAccuracy = if (chemistryAttempts.isNotEmpty()) {
        (chemistryAttempts.count { it.isCorrect } * 100 / chemistryAttempts.size)
    } else 42

    val biologyAccuracy = if (biologyAttempts.isNotEmpty()) {
        (biologyAttempts.count { it.isCorrect } * 100 / biologyAttempts.size)
    } else 68

    // Search results list
    val allChapters = remember {
        (neetPhysicsChapters.map { "Physics" to it } +
                neetChemistryChapters.map { "Chemistry" to it } +
                neetBiologyChapters.map { "Biology" to it })
    }

    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else {
            allChapters.filter { (sub, chap) ->
                chap.name.contains(searchQuery, ignoreCase = true) ||
                        sub.contains(searchQuery, ignoreCase = true) ||
                        chap.topics.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Calm dark background matching mockups
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar: "Question Bank" + "Practice. Analyze. Improve." + Search Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Question Bank",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Practice. Analyze. Improve.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    )
                }

                IconButton(
                    onClick = { isSearchActive = !isSearchActive },
                    modifier = Modifier.testTag("qbank_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Question Bank",
                        tint = if (isSearchActive) Color(0xFF00E5FF) else Color(0xFF94A3B8)
                    )
                }
            }

            // Search input field (Animated visibility)
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search questions, chapters, PYQs...",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF131C31),
                            unfocusedContainerColor = Color(0xFF131C31),
                            focusedTextColor = Color(0xFFF8FAFC),
                            unfocusedTextColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qbank_search_textfield")
                    )
                }
            }

            // If user is searching and typed text, show search results
            if (isSearchActive && searchQuery.isNotBlank()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Matching Chapters & Topics (${searchResults.size})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    if (searchResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No questions or chapters found for \"$searchQuery\"",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(searchResults) { (sub, chap) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onNavigateToSubject(sub.uppercase())
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
                                border = BorderStroke(1.dp, Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chap.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color(0xFFF8FAFC),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "$sub • ${chap.topics.size} Topics • ${chap.totalQuestionsCount}+ Qs",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF00E5FF),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Primary Screen Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Exam Filter Pills: NEET (Active), JEE (Soon), Boards (Soon)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExamPill(
                                label = "NEET",
                                isSelected = selectedExam == "NEET",
                                onClick = { selectedExam = "NEET" }
                            )
                            ExamPill(
                                label = "JEE (Soon)",
                                isSelected = selectedExam == "JEE",
                                onClick = { selectedExam = "JEE" }
                            )
                            ExamPill(
                                label = "Boards (Soon)",
                                isSelected = selectedExam == "BOARDS",
                                onClick = { selectedExam = "BOARDS" }
                            )
                        }
                    }

                    // 2. "Your Practice Journey" Banner Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToSubject("PHYSICS") }
                                .testTag("qbank_practice_journey_banner"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🌱",
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "Your Practice Journey",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Color(0xFFF8FAFC),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Solve questions, track progress, and master every concept.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Open Journey",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // 3. 3-Column Subject Cards Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Physics Card
                            SubjectStatCard(
                                modifier = Modifier.weight(1f),
                                name = "Physics",
                                questionCountText = "12,500+ Qs",
                                progressPercent = physicsAccuracy,
                                percentColor = Color(0xFF00E5FF),
                                icon = Icons.Default.ElectricBolt,
                                iconTint = Color(0xFFA855F7),
                                iconBg = Color(0xFFA855F7).copy(alpha = 0.15f),
                                onClick = { onNavigateToSubject("PHYSICS") }
                            )

                            // Chemistry Card
                            SubjectStatCard(
                                modifier = Modifier.weight(1f),
                                name = "Chemistry",
                                questionCountText = "14,200+ Qs",
                                progressPercent = chemistryAccuracy,
                                percentColor = Color(0xFFEC4899),
                                icon = Icons.Default.Science,
                                iconTint = Color(0xFFEC4899),
                                iconBg = Color(0xFFEC4899).copy(alpha = 0.15f),
                                onClick = { onNavigateToSubject("CHEMISTRY") }
                            )

                            // Biology Card
                            SubjectStatCard(
                                modifier = Modifier.weight(1f),
                                name = "Biology",
                                questionCountText = "11,600+ Qs",
                                progressPercent = biologyAccuracy,
                                percentColor = Color(0xFF10B981),
                                icon = Icons.Default.Spa,
                                iconTint = Color(0xFF10B981),
                                iconBg = Color(0xFF10B981).copy(alpha = 0.15f),
                                onClick = { onNavigateToSubject("BIOLOGY") }
                            )
                        }
                    }

                    // 4. Navigation Menu List Cards
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // 1. Previous Year Papers
                            QBankMenuItemCard(
                                title = "Previous Year Papers",
                                subtitle = "NEET 2015 – 2025",
                                icon = Icons.Default.Assignment,
                                iconTint = Color(0xFFA855F7),
                                iconBg = Color(0xFFA855F7).copy(alpha = 0.15f),
                                testTag = "qbank_menu_pyq",
                                onClick = onNavigateToPyq
                            )

                            // 2. AI Quiz Generator
                            QBankMenuItemCard(
                                title = "AI Quiz Generator",
                                subtitle = "Custom quizzes for any topic",
                                icon = Icons.Default.AutoAwesome,
                                iconTint = Color(0xFF8B5CF6),
                                iconBg = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                testTag = "qbank_menu_ai_quiz",
                                onClick = { onNavigateToAiQuiz("neet_phy_current_electricity") }
                            )

                            // 3. Mock Tests
                            QBankMenuItemCard(
                                title = "Mock Tests",
                                subtitle = "Full syllabus tests",
                                icon = Icons.Default.Quiz,
                                iconTint = Color(0xFFEC4899),
                                iconBg = Color(0xFFEC4899).copy(alpha = 0.15f),
                                testTag = "qbank_menu_mock_tests",
                                onClick = { onNavigateToMockTest("NEET Full Syllabus Test 1") }
                            )

                            // 4. My Practice
                            QBankMenuItemCard(
                                title = "My Practice",
                                subtitle = "View your attempts",
                                icon = Icons.Default.BarChart,
                                iconTint = Color(0xFF00E5FF),
                                iconBg = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                testTag = "qbank_menu_my_practice",
                                onClick = onNavigateToMyPractice
                            )

                            // 5. Bookmarks
                            QBankMenuItemCard(
                                title = "Bookmarks",
                                subtitle = "Saved questions",
                                icon = Icons.Default.Bookmark,
                                iconTint = Color(0xFF38BDF8),
                                iconBg = Color(0xFF38BDF8).copy(alpha = 0.15f),
                                testTag = "qbank_menu_bookmarks",
                                onClick = onNavigateToBookmarks
                            )
                        }
                    }

                    // Bottom padding for navigation bar
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF00E5FF) else Color(0xFF131C31))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF0F172A) else Color(0xFF64748B),
                fontSize = 13.sp
            )
        )
    }
}

@Composable
private fun SubjectStatCard(
    modifier: Modifier = Modifier,
    name: String,
    questionCountText: String,
    progressPercent: Int,
    percentColor: Color,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color(0xFFF8FAFC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = questionCountText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$progressPercent%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = percentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun QBankMenuItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
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

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Open",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

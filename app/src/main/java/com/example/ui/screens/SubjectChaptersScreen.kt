package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.FocusinViewModel

@Composable
fun SubjectChaptersScreen(
    subjectId: String,
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChapter: (String) -> Unit
) {
    val displaySubjectName = when (subjectId.uppercase()) {
        "PHYSICS" -> "Physics"
        "CHEMISTRY" -> "Chemistry"
        "BIOLOGY" -> "Biology"
        else -> subjectId.replaceFirstChar { it.uppercase() }
    }

    val chaptersFlow = remember(subjectId) {
        viewModel.getChaptersBySubject("NEET", subjectId.uppercase())
    }
    val chapters by chaptersFlow.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val filteredChapters = remember(chapters, searchQuery) {
        if (searchQuery.isBlank()) chapters
        else chapters.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val totalTopics = remember(chapters) { chapters.sumOf { it.totalTopics } }
    val completedTopics = remember(chapters) { chapters.sumOf { it.completedTopics } }
    val overallProgressPercent = remember(totalTopics, completedTopics) {
        if (totalTopics > 0) ((completedTopics.toFloat() / totalTopics) * 100).toInt() else 0
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Deep calm dark background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFE2E8F0)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displaySubjectName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC),
                            fontSize = 22.sp
                        )
                    )
                    Text(
                        text = "NEET • ${chapters.size} Chapters",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    )
                }

                IconButton(
                    onClick = { isSearchActive = !isSearchActive },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search chapters",
                        tint = if (isSearchActive) Color(0xFF38BDF8) else Color(0xFFE2E8F0)
                    )
                }
            }

            // Search Bar (Animated visibility)
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search $displaySubjectName chapters...",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedTextColor = Color(0xFFF8FAFC),
                            unfocusedTextColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Overall Progress Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
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
                                    text = "Overall Progress",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFFF8FAFC),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "$overallProgressPercent%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { (overallProgressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF334155)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "$completedTopics of $totalTopics topics completed across ${chapters.size} chapters",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // Chapter List Header
                item {
                    Text(
                        text = "Chapters Roadmap (${filteredChapters.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // Chapters
                itemsIndexed(filteredChapters) { index, chapter ->
                    ChapterListItem(
                        index = index + 1,
                        chapter = chapter,
                        onClick = { onNavigateToChapter(chapter.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ChapterListItem(
    index: Int,
    chapter: com.example.data.local.entity.ChapterEntity,
    onClick: () -> Unit
) {
    val completionPercent = chapter.completionPercentage
    val isCompleted = completionPercent >= 100
    val isStarted = chapter.completedTopics > 0

    val indexFormatted = if (index < 10) "0$index" else "$index"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isStarted && !isCompleted) Color(0xFF1E293B) else Color(0xFF161F33)
        ),
        border = if (isStarted && !isCompleted) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index number or checkmark
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Color(0xFF10B981).copy(alpha = 0.15f)
                            isStarted -> Color(0xFF38BDF8).copy(alpha = 0.15f)
                            else -> Color(0xFF334155).copy(alpha = 0.4f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = indexFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isStarted) Color(0xFF38BDF8) else Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF1F5F9),
                        fontSize = 15.sp
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${chapter.completedTopics} / ${chapter.totalTopics} topics",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = " • ",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$completionPercent%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = if (completionPercent > 0) Color(0xFF38BDF8) else Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Open",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

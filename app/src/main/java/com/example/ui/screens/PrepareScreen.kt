package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChapterEntity
import com.example.data.local.entity.LearningResourceEntity
import com.example.util.YouTubeUtils
import com.example.viewmodel.FocusinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrepareScreen(
    viewModel: FocusinViewModel,
    onNavigateToChapter: (String) -> Unit = {},
    onNavigateToSubject: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current

    // Observe real chapters from Room database
    val chapters by viewModel.neetChapters.collectAsState(initial = emptyList())
    val lastActiveChapter by viewModel.lastActiveChapter.collectAsState(initial = null)
    val featuredResources by viewModel.featuredResources.collectAsState(initial = emptyList())

    // Exam selector state
    var selectedExam by remember { mutableStateOf("NEET") }

    // Search state
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Subject breakdown derived from Room data
    val physicsChapters = remember(chapters) { chapters.filter { it.subjectId.equals("PHYSICS", ignoreCase = true) } }
    val chemistryChapters = remember(chapters) { chapters.filter { it.subjectId.equals("CHEMISTRY", ignoreCase = true) } }
    val biologyChapters = remember(chapters) { chapters.filter { it.subjectId.equals("BIOLOGY", ignoreCase = true) } }

    val totalNeetTopics = remember(chapters) { chapters.sumOf { it.totalTopics } }
    val completedNeetTopics = remember(chapters) { chapters.sumOf { it.completedTopics } }
    val overallNeetProgress = remember(totalNeetTopics, completedNeetTopics) {
        if (totalNeetTopics > 0) ((completedNeetTopics.toFloat() / totalNeetTopics) * 100).toInt() else 0
    }

    // Active learning chapter to continue
    val continueChapter: ChapterEntity? = remember(lastActiveChapter, chapters) {
        lastActiveChapter
            ?: chapters.firstOrNull { it.completedTopics in 1 until it.totalTopics }
            ?: chapters.firstOrNull()
    }

    // Search filtering across subjects and chapters
    val searchResults = remember(chapters, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else chapters.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0F1D) // Deep midnight navy canvas
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // --- TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Prepare",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC),
                            fontSize = 26.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Build your learning roadmap.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isSearchActive = !isSearchActive },
                        modifier = Modifier.testTag("prepare_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Roadmap",
                            tint = if (isSearchActive) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("prepare_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // --- INLINE SEARCH BAR ---
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
                            Text("Search NEET subjects, chapters, topics...", color = Color(0xFF64748B), fontSize = 14.sp)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF131C31),
                            unfocusedContainerColor = Color(0xFF131C31),
                            focusedTextColor = Color(0xFFF8FAFC),
                            unfocusedTextColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // --- SEARCH RESULTS OVERLAY (IF ACTIVE) ---
            if (isSearchActive && searchQuery.isNotBlank()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Matching Chapters (${searchResults.size})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    items(searchResults) { chapter ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isSearchActive = false
                                    onNavigateToChapter(chapter.id)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31))
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
                                        text = chapter.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFFF8FAFC),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = "${chapter.subjectId} • ${chapter.completedTopics}/${chapter.totalTopics} topics • ${chapter.completionPercentage}%",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF38BDF8),
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Open",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            } else {
                // --- MAIN PREPARE SCROLL CONTENT ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Exam Selector Tabs (NEET active, JEE and Boards soon)
                    item {
                        ExamSelectorRow(
                            selectedExam = selectedExam,
                            onExamSelected = { exam ->
                                if (exam == "NEET") {
                                    selectedExam = "NEET"
                                } else {
                                    Toast.makeText(context, "$exam roadmap coming soon in next update!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    // 2. NEET Preparation Overview Card
                    item {
                        NeetOverviewCard(
                            overallProgressPercent = overallNeetProgress,
                            completedTopics = completedNeetTopics,
                            totalTopics = totalNeetTopics,
                            totalChapters = chapters.size
                        )
                    }

                    // 3. Subjects Horizontal Cards (Physics, Chemistry, Biology)
                    item {
                        Column {
                            Text(
                                text = "Subjects",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFF8FAFC),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Physics Card
                                SubjectPillCard(
                                    name = "Physics",
                                    icon = Icons.Default.ElectricBolt,
                                    accentColor = Color(0xFF38BDF8),
                                    chapterCount = physicsChapters.size,
                                    completedTopics = physicsChapters.sumOf { it.completedTopics },
                                    totalTopics = physicsChapters.sumOf { it.totalTopics },
                                    currentChapterName = physicsChapters.firstOrNull { it.completedTopics > 0 && it.completedTopics < it.totalTopics }?.name
                                        ?: physicsChapters.firstOrNull()?.name ?: "Current Electricity",
                                    onClick = { onNavigateToSubject("PHYSICS") }
                                )

                                // Chemistry Card
                                SubjectPillCard(
                                    name = "Chemistry",
                                    icon = Icons.Default.Science,
                                    accentColor = Color(0xFFF43F5E),
                                    chapterCount = chemistryChapters.size,
                                    completedTopics = chemistryChapters.sumOf { it.completedTopics },
                                    totalTopics = chemistryChapters.sumOf { it.totalTopics },
                                    currentChapterName = chemistryChapters.firstOrNull { it.completedTopics > 0 && it.completedTopics < it.totalTopics }?.name
                                        ?: chemistryChapters.firstOrNull()?.name ?: "Thermodynamics",
                                    onClick = { onNavigateToSubject("CHEMISTRY") }
                                )

                                // Biology Card
                                SubjectPillCard(
                                    name = "Biology",
                                    icon = Icons.Default.Spa,
                                    accentColor = Color(0xFF10B981),
                                    chapterCount = biologyChapters.size,
                                    completedTopics = biologyChapters.sumOf { it.completedTopics },
                                    totalTopics = biologyChapters.sumOf { it.totalTopics },
                                    currentChapterName = biologyChapters.firstOrNull { it.completedTopics > 0 && it.completedTopics < it.totalTopics }?.name
                                        ?: biologyChapters.firstOrNull()?.name ?: "Human Physiology",
                                    onClick = { onNavigateToSubject("BIOLOGY") }
                                )
                            }
                        }
                    }

                    // 4. Continue Learning Section
                    item {
                        Column {
                            Text(
                                text = "Continue Learning",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFF8FAFC),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (continueChapter != null) {
                                ContinueLearningCard(
                                    chapter = continueChapter,
                                    onContinue = { onNavigateToChapter(continueChapter.id) }
                                )
                            } else {
                                StartRoadmapEmptyCard(
                                    onSelectSubject = { onNavigateToSubject("PHYSICS") }
                                )
                            }
                        }
                    }

                    // 5. Recommended For You Section
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recommended for You",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFFF8FAFC),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp
                                    )
                                )
                                TextButton(
                                    onClick = {
                                        if (continueChapter != null) {
                                            onNavigateToChapter(continueChapter.id)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "See All",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Resource cards list
                            val displayResources = if (featuredResources.isNotEmpty()) featuredResources.take(3) else listOf(
                                LearningResourceEntity(
                                    id = "fallback_1",
                                    chapterId = "neet_phy_current_electricity",
                                    title = "Current Electricity One Shot",
                                    channel = "Physics Wallah",
                                    durationText = "3:24:15",
                                    resourceType = "One Shot",
                                    recommendedReason = "Complete NCERT line-by-line & NEET concept foundation",
                                    searchQuery = "NEET Physics Current Electricity one shot Physics Wallah"
                                ),
                                LearningResourceEntity(
                                    id = "fallback_2",
                                    chapterId = "neet_bio_human_physio",
                                    title = "Human Physiology Quick Revision",
                                    channel = "Dr. Anand Mani",
                                    durationText = "4:15:00",
                                    resourceType = "One Shot",
                                    recommendedReason = "High-yield NCERT diagrams & physiological pathways",
                                    searchQuery = "NEET Biology Human Physiology one shot NCERT Dr Anand Mani"
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                displayResources.forEach { resource ->
                                    RecommendedResourceCard(
                                        resource = resource,
                                        onWatch = {
                                            YouTubeUtils.openYouTubeSearch(context, resource.searchQuery)
                                        },
                                        onOpenChapter = {
                                            onNavigateToChapter(resource.chapterId)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamSelectorRow(
    selectedExam: String,
    onExamSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // NEET Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (selectedExam == "NEET") Color(0xFF38BDF8) else Color(0xFF1E293B)
                )
                .clickable { onExamSelected("NEET") }
                .padding(horizontal = 20.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NEET",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selectedExam == "NEET") Color(0xFF0F172A) else Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            )
        }

        // JEE Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B))
                .clickable { onExamSelected("JEE") }
                .padding(horizontal = 16.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "JEE",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Soon",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF475569),
                        fontSize = 10.sp
                    )
                )
            }
        }

        // Boards Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B))
                .clickable { onExamSelected("Boards") }
                .padding(horizontal = 16.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Boards",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Soon",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF475569),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun NeetOverviewCard(
    overallProgressPercent: Int,
    completedTopics: Int,
    totalTopics: Int,
    totalChapters: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌱",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "NEET Preparation",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFF8FAFC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Your roadmap to success.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Text(
                    text = "$overallProgressPercent%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { (overallProgressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$completedTopics of $totalTopics topics completed across $totalChapters NEET chapters",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun SubjectPillCard(
    name: String,
    icon: ImageVector,
    accentColor: Color,
    chapterCount: Int,
    completedTopics: Int,
    totalTopics: Int,
    currentChapterName: String,
    onClick: () -> Unit
) {
    val progressPercent = if (totalTopics > 0) ((completedTopics.toFloat() / totalTopics) * 100).toInt() else 0

    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
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
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color(0xFFF8FAFC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )

            Text(
                text = "$chapterCount Chapters",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentChapterName,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ContinueLearningCard(
    chapter: ChapterEntity,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f))
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
                    text = chapter.subjectId,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "${chapter.completionPercentage}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = chapter.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFFF8FAFC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${chapter.completedTopics} / ${chapter.totalTopics} topics completed",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (chapter.completionPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF38BDF8),
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8),
                    contentColor = Color(0xFF0F172A)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Continue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Continue",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StartRoadmapEmptyCard(
    onSelectSubject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "START YOUR ROADMAP",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose a subject to begin your learning journey.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSelectSubject,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Explore Physics", color = Color(0xFFF8FAFC), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RecommendedResourceCard(
    resource: LearningResourceEntity,
    onWatch: () -> Unit,
    onOpenChapter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play icon box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE11D48).copy(alpha = 0.15f))
                    .clickable(onClick = onWatch),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFFFB7185),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenChapter)
            ) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF8FAFC),
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = resource.channel,
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
                        text = resource.durationText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onWatch,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF38BDF8)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Watch",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

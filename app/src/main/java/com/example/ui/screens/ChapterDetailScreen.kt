package com.example.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LearningResourceEntity
import com.example.util.YouTubeUtils
import com.example.viewmodel.FocusinViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    chapterId: String,
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (String, String) -> Unit,
    onNavigateToAiQuiz: (String) -> Unit
) {
    val context = LocalContext.current

    val currentChapter = remember(chapterId) {
        (com.example.data.model.neetPhysicsChapters + com.example.data.model.neetChemistryChapters + com.example.data.model.neetBiologyChapters)
            .find { it.id == chapterId } ?: com.example.data.model.neetPhysicsChapters.first()
    }

    val allProgress by viewModel.allChapterProgress.collectAsState()
    val chapterProgress = remember(allProgress, chapterId) {
        allProgress.find { it.chapterId == chapterId }
    }

    val completedTopicSet = remember(chapterProgress) {
        val json = chapterProgress?.completedTopicsJson ?: "[]"
        try {
            val arr = org.json.JSONArray(json)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) set.add(arr.getString(i))
            set
        } catch (_: Exception) {
            emptySet()
        }
    }

    val topics = remember(currentChapter, completedTopicSet) {
        currentChapter.topics.map { topicName ->
            com.example.data.model.NeetTopic(
                id = topicName,
                name = topicName,
                isCompleted = completedTopicSet.contains(topicName)
            )
        }
    }

    val resources = remember(currentChapter) {
        listOf(
            LearningResourceEntity(
                id = "res_${currentChapter.id}_1",
                chapterId = currentChapter.id,
                title = "${currentChapter.name} - Complete One Shot",
                channel = "Physics Wallah / Unacademy",
                durationText = "2:45:00",
                resourceType = "One Shot",
                searchQuery = "${currentChapter.name} NEET physics one shot",
                recommendedReason = "Best conceptual clarity for NEET pattern"
            )
        )
    }

    // Tab state: "LEARN" (Primary), "NOTES", "PRACTICE", "PYQs"
    var selectedTab by remember { mutableStateOf("LEARN") }

    // Selected topic in the roadmap
    var selectedTopicId by remember { mutableStateOf<String?>(null) }

    // Resource type filter in Learn view
    var selectedResourceCategory by remember { mutableStateOf("All") }

    // Add to Schedule dialog state
    var showScheduleDialog by remember { mutableStateOf(false) }
    var schedulePrefillTopicName by remember { mutableStateOf("") }

    // Topic status update dialog state
    var topicToUpdateStatus by remember { mutableStateOf<com.example.data.model.NeetTopic?>(null) }

    val activeTopic = remember(topics, selectedTopicId) {
        if (selectedTopicId != null) {
            topics.firstOrNull { it.id == selectedTopicId }
        } else {
            topics.firstOrNull { !it.isCompleted } ?: topics.firstOrNull()
        }
    }

    val completedCount = topics.count { it.isCompleted }
    val totalCount = topics.size
    val completionPercent = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Calm dark background
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                        text = currentChapter.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC),
                            fontSize = 19.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "NEET UG • ${currentChapter.subjectName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Progress Badge
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "$completionPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Progress Bar Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$completedCount / $totalCount topics completed",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = if (completionPercent >= 100) "Mastered" else "${100 - completionPercent}% remaining",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (completionPercent >= 100) Color(0xFF10B981) else Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (completionPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }

            // Segmented Section Tabs: LEARN, NOTES, PRACTICE, PYQs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            "LEARN" to "Learn",
                            "NOTES" to "Notes",
                            "PRACTICE" to "Practice",
                            "PYQs" to "PYQs"
                        ).forEach { (tabId, label) ->
                            val isSel = selectedTab == tabId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF38BDF8) else Color.Transparent)
                                    .clickable { selectedTab = tabId }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // --- TAB CONTENT ---
            when (selectedTab) {
                "LEARN" -> {
                    LearnTabContent(
                        currentChapter = currentChapter,
                        topics = topics,
                        resources = resources,
                        activeTopic = activeTopic,
                        selectedResourceCategory = selectedResourceCategory,
                        onSelectResourceCategory = { selectedResourceCategory = it },
                        onSelectTopic = { topicId ->
                            selectedTopicId = topicId
                        },
                        onOpenStatusDialog = { topic ->
                            topicToUpdateStatus = topic
                        },
                        onWatchResource = { query ->
                            YouTubeUtils.openYouTubeSearch(context, query)
                        },
                        onAddToSchedule = { topicName ->
                            schedulePrefillTopicName = topicName
                            showScheduleDialog = true
                        },
                        onNavigateToPractice = { onNavigateToPractice(currentChapter.id, "PRACTICE") },
                        onNavigateToPyq = { onNavigateToPractice(currentChapter.id, "PYQ") },
                        onNavigateToAiQuiz = { onNavigateToAiQuiz(currentChapter.id) }
                    )
                }

                "NOTES" -> {
                    NotesTabContent(chapter = currentChapter)
                }

                "PRACTICE" -> {
                    PracticeTabContent(
                        chapter = currentChapter,
                        onPractice = { onNavigateToPractice(currentChapter.id, "PRACTICE") },
                        onAiQuiz = { onNavigateToAiQuiz(currentChapter.id) }
                    )
                }

                "PYQs" -> {
                    PyqTabContent(
                        chapter = currentChapter,
                        onSolvePyq = { year ->
                            onNavigateToPractice(currentChapter.id, "PYQ_$year")
                        }
                    )
                }
            }
        }
    }

    // --- ADD TO SCHEDULE DIALOG ---
    if (showScheduleDialog) {
        AddToScheduleDialog(
            exam = "NEET",
            subject = currentChapter.subjectName,
            initialTopic = if (schedulePrefillTopicName.isNotBlank()) "${currentChapter.name} - $schedulePrefillTopicName" else currentChapter.name,
            onDismiss = { showScheduleDialog = false },
            onConfirm = { day, start, end, duration, focusMode, alarm, protection ->
                viewModel.scheduleLearningSession(
                    subjectName = currentChapter.subjectName,
                    topicName = schedulePrefillTopicName.ifBlank { currentChapter.name },
                    dayOfWeek = day,
                    startTime = start,
                    endTime = end,
                    durationMinutes = duration,
                    focusModeEnabled = focusMode,
                    alarmEnabled = alarm,
                    protectionLevel = protection
                )
                showScheduleDialog = false
                Toast.makeText(context, "Session scheduled in your timetable!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- TOPIC STATUS UPDATE DIALOG ---
    topicToUpdateStatus?.let { topic ->
        TopicStatusDialog(
            topic = topic,
            onDismiss = { topicToUpdateStatus = null },
            onSelectStatus = { newStatus ->
                viewModel.toggleTopicCompletion(currentChapter.id, currentChapter.subjectName, currentChapter.name, topic.name)
                topicToUpdateStatus = null
                Toast.makeText(context, "Updated: ${topic.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun LearnTabContent(
    currentChapter: com.example.data.model.NeetChapter,
    topics: List<com.example.data.model.NeetTopic>,
    resources: List<LearningResourceEntity>,
    activeTopic: com.example.data.model.NeetTopic?,
    selectedResourceCategory: String,
    onSelectResourceCategory: (String) -> Unit,
    onSelectTopic: (String) -> Unit,
    onOpenStatusDialog: (com.example.data.model.NeetTopic) -> Unit,
    onWatchResource: (String) -> Unit,
    onAddToSchedule: (String) -> Unit,
    onNavigateToPractice: () -> Unit,
    onNavigateToPyq: () -> Unit,
    onNavigateToAiQuiz: () -> Unit
) {
    val categories = listOf("All", "Recommended", "One Shot", "Concept", "Revision", "Numerical Practice")

    val filteredResources = remember(resources, selectedResourceCategory) {
        if (selectedResourceCategory == "All") resources
        else resources.filter { it.resourceType.equals(selectedResourceCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. TOPIC ROADMAP SECTION (Screen 3 in mock) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Topic Roadmap (${topics.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFFF8FAFC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
                Text(
                    text = "Tap to update status",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                )
            }
        }

        // Sequential topic roadmap items
        itemsIndexed(topics, key = { _, t -> t.id }) { index, topic ->
            val isActive = activeTopic?.id == topic.id
            RoadmapTopicCard(
                index = index + 1,
                topic = topic,
                isActive = isActive,
                onClick = { onSelectTopic(topic.id) },
                onStatusClick = { onOpenStatusDialog(topic) }
            )
        }

        // Action: Continue Learning Button
        item {
            Button(
                onClick = {
                    val query = "NEET ${currentChapter.subjectName} ${currentChapter.name} ${activeTopic?.name ?: "one shot"}"
                    onWatchResource(query)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8),
                    contentColor = Color(0xFF0F172A)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeTopic != null) "Continue: ${activeTopic.name}" else "Continue Learning",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // --- 2. RECOMMENDED LEARNING RESOURCES (Screen 4 in mock) ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = "Curated Video Recommendations",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFFF8FAFC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Safe YouTube search for high-yield NEET faculties",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Filter Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedResourceCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectResourceCategory(cat) },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF38BDF8),
                                selectedLabelColor = Color(0xFF0F172A),
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Color(0xFF38BDF8),
                                borderColor = Color(0xFF334155)
                            )
                        )
                    }
                }
            }
        }

        // Resource cards
        if (filteredResources.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Search YouTube for $selectedResourceCategory videos",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onWatchResource("NEET ${currentChapter.subjectName} ${currentChapter.name} $selectedResourceCategory")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                        ) {
                            Text("Open in YouTube", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredResources, key = { it.id }) { res ->
                LearningResourceCard(
                    resource = res,
                    onWatch = { onWatchResource(res.searchQuery) },
                    onAddToSchedule = { onAddToSchedule(res.title) }
                )
            }
        }

        // --- 3. END-OF-CHAPTER PRACTICE CALLOUT ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "READY TO PRACTICE?",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Test your grasp on ${currentChapter.name}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC),
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "Solidify conceptual mastery with official NEET questions and instant AI feedback.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToPractice,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A))
                        ) {
                            Text("Practice MCQs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToPyq,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color(0xFFF8FAFC))
                        ) {
                            Text("Solve PYQs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToAiQuiz,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Quiz", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { onAddToSchedule(currentChapter.name) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add to Schedule", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RoadmapTopicCard(
    index: Int,
    topic: com.example.data.model.NeetTopic,
    isActive: Boolean,
    onClick: () -> Unit,
    onStatusClick: () -> Unit
) {
    val isCompleted = topic.isCompleted
    val isInProgress = !topic.isCompleted && isActive
    val isNeedsRevision = false

    val statusColor = when {
        isCompleted -> Color(0xFF10B981)
        isInProgress -> Color(0xFF38BDF8)
        isNeedsRevision -> Color(0xFFF59E0B)
        else -> Color(0xFF64748B)
    }

    val statusLabel = when {
        isCompleted -> "Completed"
        isInProgress -> "In Progress"
        isNeedsRevision -> "Needs Revision"
        else -> "Not Started"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive || isInProgress) Color(0xFF1E293B) else Color(0xFF161F33)
        ),
        border = if (isActive || isInProgress) {
            BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
        } else {
            BorderStroke(1.dp, Color(0xFF1E293B))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon status indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f))
                    .clickable(onClick = onStatusClick),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCompleted -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    isInProgress -> {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "In Progress",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    isNeedsRevision -> {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Needs Revision",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isActive || isInProgress) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isActive || isInProgress) Color(0xFFF8FAFC) else Color(0xFFCBD5E1),
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Quick Status Pill
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.clickable(onClick = onStatusClick)
            ) {
                Text(
                    text = "Update",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LearningResourceCard(
    resource: LearningResourceEntity,
    onWatch: () -> Unit,
    onAddToSchedule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = resource.resourceType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = resource.durationText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = resource.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC),
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = resource.channel,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            )

            if (resource.recommendedReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recommended for: ${resource.recommendedReason}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onWatch,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color(0xFF38BDF8)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch on YouTube", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                OutlinedButton(
                    onClick = onAddToSchedule,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Schedule",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesTabContent(chapter: com.example.data.model.NeetChapter) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "High-Yield Summary: ${chapter.name}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Key Formulas & Governing Equations:\n" +
                                "• Current Density: J = I / A = n·e·v_d\n" +
                                "• Ohm's Law: V = I·R, where R = ρ·L / A\n" +
                                "• Temperature Dependence: R_T = R_0(1 + α·ΔT)\n" +
                                "• Kirchhoff's Junction Rule: Σ I_in = Σ I_out (Charge Conservation)\n" +
                                "• Kirchhoff's Loop Rule: Σ ΔV = 0 (Energy Conservation)\n" +
                                "• Wheatstone Bridge Balance: P / Q = R / S\n" +
                                "• Potentiometer: V_x = k·l_x (Null deflection)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            lineHeight = 20.sp,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "NEET Trap Alerts & Common Mistakes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFF43F5E),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Wire Stretching: When wire is stretched to n times length, area becomes A/n, so resistance increases by n².\n" +
                                "2. Internal Resistance: Terminal voltage V = E - Ir during discharge, but V = E + Ir during charging.\n" +
                                "3. Short Circuits: Always trace identical potential nodes before calculating equivalent resistance.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            lineHeight = 19.sp,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeTabContent(
    chapter: com.example.data.model.NeetChapter,
    onPractice: () -> Unit,
    onAiQuiz: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Chapter Question Bank",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${chapter.totalQuestionsCount} questions curated from NEET syllabus",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onPractice,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A))
                    ) {
                        Text("Start MCQ Practice", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Adaptive Drill",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFF8FAFC),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate targeted conceptual tests based on your weak areas.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onAiQuiz,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color(0xFFF8FAFC))
                    ) {
                        Text("Generate AI Test", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PyqTabContent(
    chapter: com.example.data.model.NeetChapter,
    onSolvePyq: (String) -> Unit
) {
    val years = listOf("2024", "2023", "2022", "2021", "2020")
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "NEET Official Previous Year Papers",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        items(years) { yr ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NEET $yr Paper",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFF8FAFC),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Official ${chapter.name} Questions",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        )
                    }

                    Button(
                        onClick = { onSolvePyq(yr) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color(0xFF38BDF8)
                        )
                    ) {
                        Text("Solve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicStatusDialog(
    topic: com.example.data.model.NeetTopic,
    onDismiss: () -> Unit,
    onSelectStatus: (String) -> Unit
) {
    val statuses = listOf(
        "NOT_STARTED" to "Not Started",
        "IN_PROGRESS" to "In Progress",
        "COMPLETED" to "Completed",
        "NEEDS_REVISION" to "Needs Revision"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text(
                text = "Update Topic Status",
                color = Color(0xFFF8FAFC),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = topic.name,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                statuses.forEach { (statusCode, label) ->
                    val isCurrent = if (statusCode == "COMPLETED") topic.isCompleted else !topic.isCompleted
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStatus(statusCode) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF0F172A),
                        border = if (isCurrent) BorderStroke(1.dp, Color(0xFF38BDF8)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                color = if (isCurrent) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun AddToScheduleDialog(
    exam: String,
    subject: String,
    initialTopic: String,
    onDismiss: () -> Unit,
    onConfirm: (day: Int, start: String, end: String, duration: Int, focus: Boolean, alarm: Boolean, protection: String) -> Unit
) {
    val context = LocalContext.current
    val currentCal = Calendar.getInstance()

    var selectedDay by remember { mutableIntStateOf(currentCal.get(Calendar.DAY_OF_WEEK).let { if (it == 1) 7 else it - 1 }) }
    var taskName by remember { mutableStateOf(initialTopic) }
    var startTime by remember { mutableStateOf("18:00") }
    var endTime by remember { mutableStateOf("19:00") }
    var focusModeEnabled by remember { mutableStateOf(true) }
    var alarmEnabled by remember { mutableStateOf(true) }
    var protectionLevel by remember { mutableStateOf("STANDARD") }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text(
                text = "Add to Study Schedule",
                color = Color(0xFFF8FAFC),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Prefilled details badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Exam: $exam",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Subject: $subject",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Topic / Task Name field
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Topic / Study Task", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color(0xFFF8FAFC),
                        unfocusedTextColor = Color(0xFFF8FAFC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Day Selector
                Text(
                    text = "Day of Week",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEachIndexed { idx, dayLabel ->
                        val dayNum = idx + 1
                        val isSel = selectedDay == dayNum
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSel) Color(0xFF38BDF8) else Color(0xFF0F172A))
                                .clickable { selectedDay = dayNum },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel.take(1),
                                color = if (isSel) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Start / End Time pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start Time", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val parts = startTime.split(":")
                                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 18
                                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    TimePickerDialog(context, { _, hour, minute ->
                                        startTime = String.format("%02d:%02d", hour, minute)
                                    }, h, m, true).show()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = startTime,
                                color = Color(0xFFF8FAFC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("End Time", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val parts = endTime.split(":")
                                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 19
                                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    TimePickerDialog(context, { _, hour, minute ->
                                        endTime = String.format("%02d:%02d", hour, minute)
                                    }, h, m, true).show()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = endTime,
                                color = Color(0xFFF8FAFC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Focus Mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto Focus Protection", color = Color(0xFFF8FAFC), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Block distracting apps during session", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                    Switch(
                        checked = focusModeEnabled,
                        onCheckedChange = { focusModeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF38BDF8),
                            checkedTrackColor = Color(0xFF38BDF8).copy(alpha = 0.3f)
                        )
                    )
                }

                // Alarm toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Alarm Reminder", color = Color(0xFFF8FAFC), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Ring full-screen study chime at start", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                    Switch(
                        checked = alarmEnabled,
                        onCheckedChange = { alarmEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF10B981),
                            checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedDay, startTime, endTime, 60, focusModeEnabled, alarmEnabled, protectionLevel)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add to Schedule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

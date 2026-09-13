package com.example.ui.screens.ncert

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TipsAndUpdates
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.NcertRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

/**
 * Prepare Hub Screen.
 *
 * Houses core academic prep resources:
 * - NCERT Library (Active & Fully Functional)
 * - Revision Notes (Coming Soon placeholder)
 * - Formula Sheets (Coming Soon placeholder)
 * - Mind Maps (Coming Soon placeholder)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrepareScreen(
    ncertRepository: NcertRepository,
    onNavigateBack: () -> Unit,
    onNavigateToNcertLibrary: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onOpenChapter: (String) -> Unit
) {
    val recentProgress by ncertRepository.getRecentProgress(limit = 3).collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PREPARE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Textbooks & Learning Resources",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("prepare_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("prepare_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search NCERT",
                            tint = CyanPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Intro Banner
            item {
                PrepareHeroBanner(onExploreNcert = onNavigateToNcertLibrary)
            }

            // Quick Continue Reading section if user has recent activity
            if (recentProgress.isNotEmpty()) {
                item {
                    Text(
                        text = "CONTINUE READING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF64748B)
                    )
                }

                items(recentProgress, key = { it.chapterId }) { progress ->
                    val chapter = ncertRepository.getChapter(progress.chapterId)
                    val book = chapter?.let { ncertRepository.getBook(it.bookId) }
                    if (chapter != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                                .clickable { onOpenChapter(chapter.id) }
                                .testTag("continue_reading_${chapter.id}"),
                            colors = CardDefaults.cardColors(containerColor = Slate900)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${book?.title ?: "NCERT"} • Class ${chapter.classNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CyanPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${chapter.chapterNumberFormatted}. ${chapter.title}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LinearProgressIndicator(
                                            progress = { (progress.progressPercentage / 100f).coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = CyanPrimary,
                                            trackColor = Slate800,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Page ${progress.lastPageRead} of ${progress.totalPages} (${progress.progressPercentage}%)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CyanPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume",
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Learning Resources Header
            item {
                Text(
                    text = "LEARNING MODULES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 1. NCERT Library Card (Active & Prominent)
            item {
                PrepareResourceCard(
                    title = "NCERT Library",
                    subtitle = "Official NCERT textbooks for Classes 9, 10, 11 & 12 with offline reader & direct portal access",
                    icon = Icons.Default.MenuBook,
                    accentColor = CyanPrimary,
                    badge = "ACTIVE • 100% OFFICIAL",
                    badgeColor = EmeraldSuccess,
                    isAvailable = true,
                    onClick = onNavigateToNcertLibrary,
                    testTag = "prepare_ncert_library_card"
                )
            }

            // 2. Revision Notes Card (Placeholder)
            item {
                PrepareResourceCard(
                    title = "Revision Notes",
                    subtitle = "Concise high-yield summary sheets for rapid recall before mock tests and finals",
                    icon = Icons.Default.AutoStories,
                    accentColor = PurpleAccent,
                    badge = "COMING SOON",
                    badgeColor = Color(0xFF94A3B8),
                    isAvailable = false,
                    onClick = {},
                    testTag = "prepare_revision_notes_card"
                )
            }

            // 3. Formula Sheets Card (Placeholder)
            item {
                PrepareResourceCard(
                    title = "Formula Sheets",
                    subtitle = "Chapter-wise Physics constants, Chemistry reaction charts & Maths formula cheat-sheets",
                    icon = Icons.Default.Calculate,
                    accentColor = AmberWarning,
                    badge = "COMING SOON",
                    badgeColor = Color(0xFF94A3B8),
                    isAvailable = false,
                    onClick = {},
                    testTag = "prepare_formula_sheets_card"
                )
            }

            // 4. Mind Maps Card (Placeholder)
            item {
                PrepareResourceCard(
                    title = "Mind Maps",
                    subtitle = "Visual concept diagrams, reaction mechanisms & memory anchors for complex chapters",
                    icon = Icons.Default.Hub,
                    accentColor = CyanBright,
                    badge = "COMING SOON",
                    badgeColor = Color(0xFF94A3B8),
                    isAvailable = false,
                    onClick = {},
                    testTag = "prepare_mind_maps_card"
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PrepareHeroBanner(onExploreNcert: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.5f), Slate800)),
                RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CyanPrimary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = CyanPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "FOCUSIN ACADEMIC HUB",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanPrimary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Master Every Chapter with Official NCERT",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 26.sp
            )

            Text(
                text = "Access clean, verified NCERT textbooks for Classes 9 to 12. Read offline, bookmark key topics, and reference official curriculum standards.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyanPrimary)
                    .clickable { onExploreNcert() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("hero_explore_ncert_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Open NCERT Library",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate950
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Slate950,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PrepareResourceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badge: String,
    badgeColor: Color,
    isAvailable: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isAvailable) accentColor.copy(alpha = 0.35f) else Slate800,
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isAvailable) { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )
            }

            if (isAvailable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

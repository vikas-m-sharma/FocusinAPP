package com.example.ui.screens.ncert

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.NcertProgressEntity
import com.example.data.model.NcertChapter
import com.example.data.repository.NcertRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.NcertPdfManager

/**
 * NCERT Book Screen.
 * Displays the complete, verified numbered chapter sequence of an NCERT textbook.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NcertBookScreen(
    bookId: String,
    ncertRepository: NcertRepository,
    onNavigateBack: () -> Unit,
    onNavigateToChapterDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val context = LocalContext.current
    val book = ncertRepository.getBook(bookId)
    val chapters = ncertRepository.getChaptersForBook(bookId)
    val subject = book?.let { ncertRepository.getSubject(it.subjectId) }
    val ncertClass = book?.let { ncertRepository.getClass(it.classNumber) }

    // Read all progress from Room to show completion & reading stats
    val allProgress by ncertRepository.getRecentProgress(limit = 100).collectAsState(initial = emptyList())
    val progressMap = allProgress.associateBy { it.chapterId }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = book?.title ?: "NCERT Textbook",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${subject?.name ?: "Subject"} • ${ncertClass?.displayName ?: "Class"}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ncert_book_back_button")
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
                        modifier = Modifier.testTag("ncert_book_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Book metadata banner
            if (book != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Official Book Code: ${book.bookCode}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanPrimary
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Slate800)
                                        .clickable {
                                            NcertPdfManager.openOfficialWebReader(context, book.getOfficialPortalUrl())
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Open Portal",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = Slate800,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${chapters.size} Chapters",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    color = Slate800,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = book.edition,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHAPTERS (${chapters.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Sequential Syllabus",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Chapters List
            items(chapters, key = { it.id }) { chapter ->
                val progress = progressMap[chapter.id]

                ChapterRowItem(
                    chapter = chapter,
                    progress = progress,
                    onClick = { onNavigateToChapterDetail(chapter.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ChapterRowItem(
    chapter: NcertChapter,
    progress: NcertProgressEntity?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("ncert_chapter_item_${chapter.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Chapter Number Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (progress?.isCompleted == true) EmeraldSuccess.copy(alpha = 0.15f)
                        else CyanPrimary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (progress?.isCompleted == true) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = chapter.chapterNumberFormatted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }
            }

            // Title & Progress
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                if (chapter.hindiTitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = chapter.hindiTitle,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                if (progress != null && progress.progressPercentage > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { (progress.progressPercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(70.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (progress.isCompleted) EmeraldSuccess else CyanPrimary,
                            trackColor = Slate800
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (progress.isCompleted) "Completed" else "Page ${progress.lastPageRead} of ${progress.totalPages}",
                            fontSize = 10.sp,
                            color = if (progress.isCompleted) EmeraldSuccess else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Bookmark indicator
            if (progress?.isBookmarked == true) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Bookmarked",
                    tint = AmberWarning,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

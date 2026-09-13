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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NcertClass
import com.example.data.repository.NcertRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SubjectBlue
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.NcertPdfManager

/**
 * NCERT Library Hub.
 *
 * Displays:
 * - 4 Class options: Class 9, Class 10, Class 11, Class 12 in a clean 2x2 grid
 * - Direct Search access
 * - Continue Reading section (from Room)
 * - Saved Bookmarks section (from Room)
 * - Official NCERT attribution & verification
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NcertLibraryScreen(
    ncertRepository: NcertRepository,
    onNavigateBack: () -> Unit,
    onNavigateToClass: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    onOpenChapter: (String) -> Unit
) {
    val context = LocalContext.current
    val classes = ncertRepository.getClasses()
    val recentProgress by ncertRepository.getRecentProgress(limit = 3).collectAsState(initial = emptyList())
    val bookmarkedChapters by ncertRepository.getBookmarkedChapters().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NCERT Library",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Official NCERT",
                                tint = CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Official NCERT textbooks for focused learning",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ncert_library_back_button")
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
                        modifier = Modifier.testTag("ncert_library_search_button")
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Search Bar Tap Target
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                        .clickable { onNavigateToSearch() }
                        .testTag("ncert_search_bar_trigger"),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Search chapters, books or subjects...",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // SELECT CLASS SECTION
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT YOUR CLASS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "4 Classes Available",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // 2x2 Class Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val c9 = classes.firstOrNull { it.classNumber == 9 }
                        val c10 = classes.firstOrNull { it.classNumber == 10 }
                        if (c9 != null) {
                            Box(modifier = Modifier.weight(1f)) {
                                ClassGridCard(
                                    ncertClass = c9,
                                    subjectCount = ncertRepository.getSubjectsForClass(9).size,
                                    accentColor = SubjectBlue,
                                    onClick = { onNavigateToClass(9) }
                                )
                            }
                        }
                        if (c10 != null) {
                            Box(modifier = Modifier.weight(1f)) {
                                ClassGridCard(
                                    ncertClass = c10,
                                    subjectCount = ncertRepository.getSubjectsForClass(10).size,
                                    accentColor = EmeraldSuccess,
                                    onClick = { onNavigateToClass(10) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val c11 = classes.firstOrNull { it.classNumber == 11 }
                        val c12 = classes.firstOrNull { it.classNumber == 12 }
                        if (c11 != null) {
                            Box(modifier = Modifier.weight(1f)) {
                                ClassGridCard(
                                    ncertClass = c11,
                                    subjectCount = ncertRepository.getSubjectsForClass(11).size,
                                    accentColor = CyanPrimary,
                                    onClick = { onNavigateToClass(11) }
                                )
                            }
                        }
                        if (c12 != null) {
                            Box(modifier = Modifier.weight(1f)) {
                                ClassGridCard(
                                    ncertClass = c12,
                                    subjectCount = ncertRepository.getSubjectsForClass(12).size,
                                    accentColor = PurpleAccent,
                                    onClick = { onNavigateToClass(12) }
                                )
                            }
                        }
                    }
                }
            }

            // RECENT READINGS (From Room Database)
            if (recentProgress.isNotEmpty()) {
                item {
                    Text(
                        text = "RESUME READING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 6.dp)
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
                                .testTag("ncert_recent_${chapter.id}"),
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
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LinearProgressIndicator(
                                            progress = { (progress.progressPercentage / 100f).coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .width(90.dp)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = CyanPrimary,
                                            trackColor = Slate800,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Page ${progress.lastPageRead} of ${progress.totalPages}",
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
                                        contentDescription = "Read",
                                        tint = CyanPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SAVED BOOKMARKS SECTION
            if (bookmarkedChapters.isNotEmpty()) {
                item {
                    Text(
                        text = "SAVED BOOKMARKS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(bookmarkedChapters.take(4), key = { "bm_${it.chapterId}" }) { bm ->
                    val chapter = ncertRepository.getChapter(bm.chapterId)
                    val book = chapter?.let { ncertRepository.getBook(it.bookId) }
                    if (chapter != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, AmberWarning.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .clickable { onOpenChapter(chapter.id) }
                                .testTag("ncert_bookmark_${chapter.id}"),
                            colors = CardDefaults.cardColors(containerColor = Slate900)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Bookmark",
                                        tint = AmberWarning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${chapter.chapterNumberFormatted}. ${chapter.title}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${book?.title ?: "NCERT"} • Class ${chapter.classNumber}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // OFFICIAL NCERT GUARANTEE DISCLAIMER
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Official NCERT Portal Guarantee",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "All textbooks, chapters, and curriculum structures in this library are aligned with the official National Council of Educational Research and Training repository (ncert.nic.in).",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate800)
                                .clickable {
                                    NcertPdfManager.openOfficialWebReader(context, "https://ncert.nic.in/textbook.php")
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "ncert.nic.in/textbook.php",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanPrimary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ClassGridCard(
    ncertClass: NcertClass,
    subjectCount: Int,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        .clickable { onClick() }
            .testTag("ncert_class_card_${ncertClass.classNumber}"),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${ncertClass.classNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }

                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "$subjectCount Subjects",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = ncertClass.displayName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Textbooks & Chapters",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Explore →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

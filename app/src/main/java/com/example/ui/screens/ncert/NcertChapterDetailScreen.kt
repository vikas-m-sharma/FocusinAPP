package com.example.ui.screens.ncert

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.NcertRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.NcertPdfManager
import kotlinx.coroutines.launch

/**
 * NCERT Chapter Detail Screen.
 * Provides complete overview of the chapter, reading state, official NCERT links,
 * offline PDF download, and bookmark controls.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NcertChapterDetailScreen(
    chapterId: String,
    ncertRepository: NcertRepository,
    onNavigateBack: () -> Unit,
    onNavigateToReader: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val chapter = ncertRepository.getChapter(chapterId)
    val book = chapter?.let { ncertRepository.getBook(it.bookId) }
    val subject = chapter?.let { ncertRepository.getSubject(it.subjectId) }
    val ncertClass = chapter?.let { ncertRepository.getClass(it.classNumber) }

    val progress by ncertRepository.getProgressForChapter(chapterId).collectAsState(initial = null)

    var isDownloaded by remember { mutableStateOf(NcertPdfManager.isChapterDownloaded(context, chapterId)) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    val isBookmarked = progress?.isBookmarked == true

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chapter Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ncert_detail_back_button")
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
                        onClick = {
                            scope.launch {
                                ncertRepository.toggleBookmark(chapterId)
                            }
                        },
                        modifier = Modifier.testTag("ncert_detail_bookmark_action")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Toggle Bookmark",
                            tint = if (isBookmarked) AmberWarning else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950
                )
            )
        }
    ) { paddingValues ->
        if (chapter == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chapter not found in NCERT catalog.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chapter Title & Badge Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hierarchy Breadcrumb
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = CyanPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = ncertClass?.displayName ?: "Class ${chapter.classNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(text = "•", color = Color(0xFF64748B), fontSize = 12.sp)
                            Text(
                                text = subject?.name ?: "Subject",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(text = "•", color = Color(0xFF64748B), fontSize = 12.sp)
                            Text(
                                text = book?.title ?: "Book",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Title
                        Text(
                            text = "${chapter.chapterNumberFormatted}. ${chapter.title}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 26.sp
                        )

                        if (chapter.hindiTitle != null) {
                            Text(
                                text = chapter.hindiTitle,
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Code Tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Book Code: ${chapter.bookCode}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "~${chapter.estimatedPages} Pages",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Reading Progress Status Card
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
                            Text(
                                text = "STUDY PROGRESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF64748B)
                            )
                            if (progress?.isCompleted == true) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Completed",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }

                        val pct = progress?.progressPercentage ?: 0
                        LinearProgressIndicator(
                            progress = { (pct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (progress?.isCompleted == true) EmeraldSuccess else CyanPrimary,
                            trackColor = Slate800
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (progress != null && progress!!.lastPageRead > 0)
                                    "Last Read: Page ${progress!!.lastPageRead} of ${progress!!.totalPages}"
                                else "Not started yet",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Text(
                                text = "$pct% Done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pct >= 95) EmeraldSuccess else CyanPrimary
                            )
                        }
                    }
                }
            }

            // PRIMARY ACTIONS: [ READ ] / [ CONTINUE READING ] & [ OPEN OFFICIAL NCERT ]
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onNavigateToReader(chapter.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("ncert_detail_read_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (progress != null && progress!!.lastPageRead > 1) Icons.Default.PlayArrow else Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Slate950,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (progress != null && progress!!.lastPageRead > 1) "CONTINUE READING (P. ${progress!!.lastPageRead})" else "READ CHAPTER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate950
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Open Official NCERT Portal
                        OutlinedButton(
                            onClick = {
                                NcertPdfManager.openOfficialWebReader(context, chapter.officialChapterUrl)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("ncert_detail_open_official_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Official Web",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Bookmark Toggle
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    ncertRepository.toggleBookmark(chapterId)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("ncert_detail_bookmark_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isBookmarked) AmberWarning else Slate800
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (isBookmarked) AmberWarning else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBookmarked) "Saved" else "Bookmark",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isBookmarked) AmberWarning else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // OFFLINE DOWNLOAD CARD
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.Download,
                                    contentDescription = null,
                                    tint = if (isDownloaded) EmeraldSuccess else CyanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isDownloaded) "Saved for Offline Reading" else "Download for Offline Reading",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Official PDF cached in secure app storage",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = CyanPrimary,
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        if (isDownloading) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = CyanPrimary,
                                trackColor = Slate800
                            )
                            Text(
                                text = "Downloading official NCERT PDF... ${(downloadProgress * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!isDownloaded) {
                                Button(
                                    onClick = {
                                        isDownloading = true
                                        scope.launch {
                                            val result = NcertPdfManager.downloadChapterPdf(context, chapter) { p ->
                                                downloadProgress = p
                                            }
                                            isDownloading = false
                                            result.fold(
                                                onSuccess = {
                                                    isDownloaded = true
                                                    Toast.makeText(context, "PDF saved for offline reading!", Toast.LENGTH_SHORT).show()
                                                },
                                                onFailure = { err ->
                                                    Toast.makeText(context, "Download issue: ${err.message}. You can read online.", Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    },
                                    enabled = !isDownloading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("ncert_detail_download_pdf_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = null,
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Download Official PDF",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanPrimary
                                        )
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        val saved = NcertPdfManager.savePdfToPublicDownloads(context, chapter)
                                        if (saved) {
                                            Toast.makeText(context, "Exported to Downloads folder!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Export to Device",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        NcertPdfManager.deleteChapterPdf(context, chapterId)
                                        isDownloaded = false
                                        Toast.makeText(context, "Offline cache cleared", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                                ) {
                                    Text(
                                        text = "Clear Cache",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CHAPTER DESCRIPTION / OVERVIEW
            if (chapter.description.isNotBlank()) {
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
                            Text(
                                text = "CHAPTER OVERVIEW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = chapter.description,
                                fontSize = 13.sp,
                                color = Color.White,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            // SUBTOPICS
            if (chapter.subtopics.isNotEmpty()) {
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
                            Text(
                                text = "CORE CONCEPTS & TOPICS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF64748B)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chapter.subtopics.forEach { topic ->
                                    Surface(
                                        color = Slate800,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = topic,
                                            fontSize = 12.sp,
                                            color = Color(0xFFCBD5E1),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // OFFICIAL NCERT ATTRIBUTION GUARANTEE
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Official NCERT repository (ncert.nic.in/textbook.php)",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

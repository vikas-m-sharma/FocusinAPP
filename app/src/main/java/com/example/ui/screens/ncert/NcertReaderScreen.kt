package com.example.ui.screens.ncert

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.util.PdfPaperManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * NCERT Internal PDF Reader.
 *
 * Provides a distraction-free, focused reading experience:
 * - Native Android PdfRenderer (high resolution, zero latency)
 * - Pinch-to-zoom & smooth pan
 * - Page navigation controls
 * - Jump-to-page slider
 * - Automatic reading progress sync to Room
 * - Works 100% offline once downloaded
 * - Direct fallback to official NCERT web portal if not downloaded
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NcertReaderScreen(
    chapterId: String,
    ncertRepository: NcertRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val chapter = ncertRepository.getChapter(chapterId)
    val progress by ncertRepository.getProgressForChapter(chapterId).collectAsState(initial = null)

    var isDownloaded by remember { mutableStateOf(NcertPdfManager.isChapterDownloaded(context, chapterId)) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    var totalPages by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(false) }

    // Pinch-to-zoom & pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var showControls by remember { mutableStateOf(true) }

    val isBookmarked = progress?.isBookmarked == true

    fun loadPage(index: Int) {
        val file = NcertPdfManager.getPdfFile(context, chapterId)
        if (!file.exists()) return

        isLoadingPage = true
        scope.launch {
            val bmp = withContext(Dispatchers.IO) {
                PdfPaperManager.renderPdfPage(file, index, destWidth = 1080)
            }
            currentBitmap = bmp
            currentPageIndex = index
            isLoadingPage = false
            scale = 1f
            offsetX = 0f
            offsetY = 0f

            // Record reading progress in Room
            if (totalPages > 0) {
                ncertRepository.recordReadingProgress(
                    chapterId = chapterId,
                    currentPage = index + 1,
                    totalPages = totalPages
                )
            }
        }
    }

    // Load PDF when downloaded
    LaunchedEffect(isDownloaded, chapterId) {
        if (isDownloaded) {
            val file = NcertPdfManager.getPdfFile(context, chapterId)
            if (file.exists() && file.length() > 0) {
                val count = PdfPaperManager.getPageCount(file).coerceAtLeast(1)
                totalPages = count

                // Resume from last read page if available
                val savedPage = (progress?.lastPageRead?.minus(1)) ?: 0
                val initialPage = savedPage.coerceIn(0, count - 1)
                loadPage(initialPage)
            }
        }
    }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chapter?.title ?: "NCERT Reader",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        if (totalPages > 0) {
                            Text(
                                text = "Page ${currentPageIndex + 1} of $totalPages • Book Code ${chapter?.bookCode ?: ""}",
                                fontSize = 11.sp,
                                color = CyanPrimary
                            )
                        } else {
                            Text(
                                text = "Class ${chapter?.classNumber ?: 11} NCERT Textbook",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ncert_reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Bookmark toggle
                    IconButton(
                        onClick = {
                            scope.launch {
                                ncertRepository.toggleBookmark(chapterId)
                            }
                        },
                        modifier = Modifier.testTag("ncert_reader_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) AmberWarning else Color.White
                        )
                    }

                    // Open in official browser
                    IconButton(
                        onClick = {
                            chapter?.let {
                                NcertPdfManager.openOfficialWebReader(context, it.officialChapterUrl)
                            }
                        },
                        modifier = Modifier.testTag("ncert_reader_web_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open Web Reader",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Slate950)
        ) {
            if (!isDownloaded) {
                // NOT DOWNLOADED YET STATE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Official NCERT Chapter PDF",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${chapter?.chapterNumberFormatted}. ${chapter?.title ?: "Chapter"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Download this chapter directly from official NCERT servers (ncert.nic.in) to read offline anytime.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isDownloading) {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyanPrimary,
                                trackColor = Slate800
                            )
                            Text(
                                text = "Downloading from ncert.nic.in... ${(downloadProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (chapter != null) {
                                    isDownloading = true
                                    scope.launch {
                                        val result = NcertPdfManager.downloadChapterPdf(context, chapter) { p ->
                                            downloadProgress = p
                                        }
                                        isDownloading = false
                                        result.fold(
                                            onSuccess = {
                                                isDownloaded = true
                                            },
                                            onFailure = { err ->
                                                Toast.makeText(context, "Download failed: ${err.message}. You can use the web reader.", Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                            .height(48.dp)
                                .testTag("ncert_reader_download_trigger"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Slate950,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DOWNLOAD FOR OFFLINE READ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate950
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                chapter?.let {
                                    NcertPdfManager.openOfficialWebReader(context, it.officialChapterUrl)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Read Online on NCERT Portal",
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // PDF VIEWER
                Column(modifier = Modifier.fillMaxSize()) {
                    // Main PDF Content Area with pinch-to-zoom
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                    if (scale > 1f) {
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoadingPage) {
                            CircularProgressIndicator(
                                color = CyanPrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                        } else if (currentBitmap != null) {
                            Image(
                                bitmap = currentBitmap!!.asImageBitmap(),
                                contentDescription = "NCERT Page ${currentPageIndex + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY
                                    ),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = "Unable to render page",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }

                        // Floating zoom reset button when zoomed in
                        if (scale > 1.05f) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate900.copy(alpha = 0.85f))
                                    .clickable {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Reset Zoom (${(scale * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary
                                )
                            }
                        }
                    }

                    // Bottom Navigation Bar & Page Controls
                    Surface(
                        color = Slate900,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Slider for fast seeking
                            if (totalPages > 1) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "1",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Slider(
                                        value = (currentPageIndex + 1).toFloat(),
                                        onValueChange = { newPage ->
                                            val targetIndex = (newPage.toInt() - 1).coerceIn(0, totalPages - 1)
                                            if (targetIndex != currentPageIndex) {
                                                loadPage(targetIndex)
                                            }
                                        },
                                        valueRange = 1f..totalPages.toFloat(),
                                        steps = (totalPages - 2).coerceAtLeast(0),
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = CyanPrimary,
                                            activeTrackColor = CyanPrimary,
                                            inactiveTrackColor = Slate800
                                        )
                                    )
                                    Text(
                                        text = "$totalPages",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Stepper Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Previous Page
                                OutlinedButton(
                                    onClick = {
                                        if (currentPageIndex > 0) {
                                            loadPage(currentPageIndex - 1)
                                        }
                                    },
                                    enabled = currentPageIndex > 0,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                                    modifier = Modifier.testTag("ncert_reader_prev_page")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBackIosNew,
                                            contentDescription = "Previous Page",
                                            modifier = Modifier.size(14.dp),
                                            tint = if (currentPageIndex > 0) Color.White else Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Previous",
                                            fontSize = 12.sp,
                                            color = if (currentPageIndex > 0) Color.White else Color(0xFF64748B)
                                        )
                                    }
                                }

                                // Current Page Indicator
                                Text(
                                    text = "Page ${currentPageIndex + 1} of $totalPages",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                // Next Page
                                OutlinedButton(
                                    onClick = {
                                        if (currentPageIndex < totalPages - 1) {
                                            loadPage(currentPageIndex + 1)
                                        }
                                    },
                                    enabled = currentPageIndex < totalPages - 1,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                                    modifier = Modifier.testTag("ncert_reader_next_page")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Next",
                                            fontSize = 12.sp,
                                            color = if (currentPageIndex < totalPages - 1) Color.White else Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowForwardIos,
                                            contentDescription = "Next Page",
                                            modifier = Modifier.size(14.dp),
                                            tint = if (currentPageIndex < totalPages - 1) Color.White else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.util.PdfPaperManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Internal Native PDF Reader for NEET Previous Year Question Papers.
 * Functions 100% locally and reliably even in Airplane Mode (no internet connection needed).
 * Includes Pinch-to-Zoom, Page-by-Page navigation, Jump to Page, and 1-tap solve button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternalPdfReaderScreen(
    paper: NeetPyqPdf,
    onNavigateBack: () -> Unit,
    onStartTest: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDownloaded by remember { mutableStateOf(PdfPaperManager.isPaperDownloaded(context, paper.year)) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    var totalPages by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(false) }

    // Pinch-to-zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Load or render current page
    fun loadPage(index: Int) {
        val file = PdfPaperManager.getPdfFileForYear(context, paper.year)
        if (!file.exists()) return

        isLoadingPage = true
        scope.launch {
            val bmp = withContext(Dispatchers.IO) {
                PdfPaperManager.renderPdfPage(file, index, destWidth = 1080)
            }
            currentBitmap = bmp
            currentPageIndex = index
            isLoadingPage = false
            // Reset zoom when switching pages
            scale = 1f
            offsetX = 0f
            offsetY = 0f
        }
    }

    // Auto load or start download on entry
    LaunchedEffect(paper.year) {
        val file = PdfPaperManager.getPdfFileForYear(context, paper.year)
        if (file.exists() && file.length() > 0) {
            isDownloaded = true
            totalPages = PdfPaperManager.getPageCount(file).coerceAtLeast(1)
            loadPage(0)
        } else {
            // Initiate 1-time background download/generation for instant airplane-mode readiness
            isDownloading = true
            scope.launch {
                val downloadedFile = PdfPaperManager.downloadOrGeneratePaperPdf(
                    context = context,
                    paper = paper,
                    onProgress = { p -> downloadProgress = p }
                )
                isDownloading = false
                isDownloaded = true
                totalPages = PdfPaperManager.getPageCount(downloadedFile).coerceAtLeast(1)
                loadPage(0)
                Toast.makeText(context, "NEET ${paper.year} saved! Available in Airplane Mode ✈️", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NEET ${paper.year} Paper",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AirplanemodeActive,
                                        contentDescription = "Airplane Mode Ready",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Offline Ready",
                                        fontSize = 10.sp,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${paper.paperCode} • ${paper.questionsCount} Qs • ${paper.durationText}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    // Save / Download PDF file to public device Downloads folder
                    IconButton(
                        onClick = {
                            val success = PdfPaperManager.savePdfToPublicDownloads(context, paper)
                            if (success) {
                                Toast.makeText(context, "NEET ${paper.year} PDF downloaded to Downloads folder! 📥", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Downloading NEET ${paper.year} PDF...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download PDF", tint = CyanPrimary)
                    }

                    // Open with Official System PDF Viewer / Browser
                    IconButton(
                        onClick = {
                            PdfPaperManager.openPdfWithExternalViewer(context, paper)
                        }
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in External Viewer", tint = Color.White)
                    }

                    // Zoom controls
                    IconButton(
                        onClick = { scale = (scale + 0.25f).coerceAtMost(3f) }
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                    }
                    IconButton(
                        onClick = {
                            scale = (scale - 0.25f).coerceAtLeast(1f)
                            if (scale == 1f) {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            // Footer Navigation Bar (Page controls & Solve Action)
            Surface(
                color = Slate900,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Page Button
                    IconButton(
                        onClick = {
                            if (currentPageIndex > 0) {
                                loadPage(currentPageIndex - 1)
                            }
                        },
                        enabled = currentPageIndex > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Previous Page",
                            tint = if (currentPageIndex > 0) CyanPrimary else Color(0xFF475569)
                        )
                    }

                    // Page Indicator Badge
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Page ${currentPageIndex + 1} of ${totalPages.coerceAtLeast(1)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Next Page Button
                    IconButton(
                        onClick = {
                            if (currentPageIndex < totalPages - 1) {
                                loadPage(currentPageIndex + 1)
                            }
                        },
                        enabled = currentPageIndex < totalPages - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Next Page",
                            tint = if (currentPageIndex < totalPages - 1) CyanPrimary else Color(0xFF475569)
                        )
                    }

                    // Solve Interactive Test Button
                    Button(
                        onClick = { onStartTest("NEET ${paper.year}") },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Solve", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0B1120)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isDownloading -> {
                    // Download & Offline Caching Progress
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "Caching NEET ${paper.year} for Airplane Mode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Downloading once to your device storage. You can solve it completely offline with 0 distractions.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )

                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyanPrimary,
                            trackColor = Slate800
                        )

                        Text(
                            text = "${(downloadProgress * 100).toInt()}% Prepared",
                            fontSize = 12.sp,
                            color = CyanPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                isLoadingPage -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(36.dp))
                        Text("Rendering Page ${currentPageIndex + 1}...", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }

                currentBitmap != null -> {
                    // PDF Page Render View with pinch to zoom and pan
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 3.5f)
                                    if (scale > 1f) {
                                        val maxPanX = (size.width * (scale - 1f)) / 2f
                                        val maxPanY = (size.height * (scale - 1f)) / 2f
                                        offsetX = (offsetX + pan.x).coerceIn(-maxPanX, maxPanX)
                                        offsetY = (offsetY + pan.y).coerceIn(-maxPanY, maxPanY)
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            }
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "NEET ${paper.year} Page ${currentPageIndex + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }

                else -> {
                    // Error fallback
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Unable to open PDF document", color = RoseError, fontSize = 14.sp)
                        Button(
                            onClick = {
                                isDownloading = true
                                scope.launch {
                                    val f = PdfPaperManager.downloadOrGeneratePaperPdf(context, paper)
                                    isDownloading = false
                                    totalPages = PdfPaperManager.getPageCount(f)
                                    loadPage(0)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Slate950)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Download", color = Slate950)
                        }
                    }
                }
            }
        }
    }
}

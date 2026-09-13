package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.ExtractedQuestionDraft
import com.example.viewmodel.ImportTestUiState
import com.example.viewmodel.ImportTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTestScreen(
    viewModel: ImportTestViewModel,
    onBack: () -> Unit,
    onTestCreated: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Document & Photo Pickers
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.startExtraction(
                pdfUri = uri,
                photoUris = emptyList(),
                defaultTitle = "Imported PDF Test (${System.currentTimeMillis() % 10000})"
            )
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.startExtraction(
                pdfUri = null,
                photoUris = uris,
                defaultTitle = "Imported Photo Test (${uris.size} Pages)"
            )
        }
    }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = if (uiState.isReviewReady) "Review & Create Test" else "Import My Test",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isProcessing -> {
                    ProcessingDocumentView(uiState = uiState)
                }
                uiState.isReviewReady -> {
                    ReviewDocumentView(
                        uiState = uiState,
                        onUpdateTitle = viewModel::updateTestTitle,
                        onUpdateDuration = viewModel::updateDuration,
                        onUpdateMarking = viewModel::updateMarkingScheme,
                        onUpdateQuestion = viewModel::updateQuestion,
                        onCreateTest = {
                            viewModel.createAndSaveTest(onSuccess = onTestCreated)
                        }
                    )
                }
                else -> {
                    UploadSourceSelectionView(
                        onUploadPdf = { pdfPickerLauncher.launch("application/pdf") },
                        onUploadPhotos = { photoPickerLauncher.launch("image/*") }
                    )
                }
            }
        }
    }
}

/**
 * 1. Initial Source Selection View: PDF vs Photos
 */
@Composable
private fun UploadSourceSelectionView(
    onUploadPdf: () -> Unit,
    onUploadPhotos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = ClassicBluePrimary.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = ClassicBlueLight,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Import Your Test Paper",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bring any coaching test, institution mock, or self-test paper into FOCUSIN. We automatically extract MCQs, detect options, and prepare an interactive OMR test.",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Option 1: PDF Upload Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUploadPdf() },
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Slate800)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Upload PDF Document",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Single PDF file with questions and optional answer key",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Option 2: Photos Upload Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUploadPhotos() },
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Slate800)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Upload Photos / Scans",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Multiple photos of test question pages from gallery",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Privacy Guarantee Note
        Surface(
            color = Slate900.copy(alpha = 0.6f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Slate800)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "100% Private: Stored only on your device, never publicly shared.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * 2. Processing Pipeline View
 */
@Composable
private fun ProcessingDocumentView(uiState: ImportTestUiState) {
    val progress = uiState.progress
    val currentStep = progress?.stage ?: "READING"
    val percent = progress?.progressPercent ?: 15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.size(64.dp),
            color = ClassicBlueLight,
            trackColor = Slate800,
            strokeWidth = 5.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Processing Your Test...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = progress?.currentStepText ?: "Analyzing document layout and extracting questions...",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Five Checkpoint Pipeline Steps
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PipelineStepItem(
                    label = "Reading document pages",
                    isComplete = percent > 25,
                    isActive = currentStep == "READING"
                )
                PipelineStepItem(
                    label = "Detecting questions & numbers",
                    isComplete = percent > 45,
                    isActive = currentStep == "DETECTING_QUESTIONS"
                )
                PipelineStepItem(
                    label = "Extracting options (A, B, C, D)",
                    isComplete = percent > 65,
                    isActive = currentStep == "DETECTING_OPTIONS"
                )
                PipelineStepItem(
                    label = "Detecting answers & solving with AI",
                    isComplete = percent > 85,
                    isActive = currentStep == "DETECTING_ANSWERS"
                )
                PipelineStepItem(
                    label = "Validating questions & structure",
                    isComplete = percent >= 98,
                    isActive = currentStep == "VALIDATING"
                )
            }
        }
    }
}

@Composable
private fun PipelineStepItem(
    label: String,
    isComplete: Boolean,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(
                    color = when {
                        isComplete -> Color(0xFF10B981)
                        isActive -> ClassicBluePrimary
                        else -> Slate800
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            } else if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 13.sp,
            color = if (isComplete || isActive) Color.White else Color(0xFF64748B),
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 3. Review & Edit View Before Creating Test
 */
@Composable
private fun ReviewDocumentView(
    uiState: ImportTestUiState,
    onUpdateTitle: (String) -> Unit,
    onUpdateDuration: (Int) -> Unit,
    onUpdateMarking: (Float, Float) -> Unit,
    onUpdateQuestion: (Int, ExtractedQuestionDraft) -> Unit,
    onCreateTest: () -> Unit
) {
    val questions = uiState.questions
    val physicsCount = questions.count { it.subject.equals("Physics", ignoreCase = true) }
    val chemistryCount = questions.count { it.subject.equals("Chemistry", ignoreCase = true) }
    val biologyCount = questions.size - (physicsCount + chemistryCount)
    val lowConfidenceCount = questions.count { it.confidence < 0.85f }

    var expandedQuestionIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // "Test Ready" Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ClassicBluePrimary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Test Ready",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${questions.size} Questions Found",
                                fontSize = 13.sp,
                                color = ClassicBlueLight,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "VERIFIED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Subject Breakdown Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubjectPill("Physics", physicsCount, Color(0xFF38BDF8))
                        SubjectPill("Chemistry", chemistryCount, Color(0xFFFBBF24))
                        SubjectPill("Biology", biologyCount, Color(0xFF34D399))
                    }

                    if (lowConfidenceCount > 0) {
                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$lowConfidenceCount questions have flagged items for manual review.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Test Configuration (Title, Duration, Marking)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate800)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Test Configuration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = uiState.testTitle,
                        onValueChange = onUpdateTitle,
                        label = { Text("Test Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClassicBlueLight,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.durationMinutes.toString(),
                            onValueChange = { onUpdateDuration(it.toIntOrNull() ?: 200) },
                            label = { Text("Duration (Mins)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClassicBlueLight,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = "+${uiState.positiveMarks.toInt()} / -${uiState.negativeMarks.toInt()}",
                            onValueChange = { /* Default NEET */ },
                            readOnly = true,
                            label = { Text("Marking Scheme") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClassicBlueLight,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Extracted Questions (${questions.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tap any question to edit",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Questions List with Inline Review/Edit
        itemsIndexed(questions) { index, q ->
            val isExpanded = expandedQuestionIndex == index

            ExtractedQuestionReviewCard(
                question = q,
                isExpanded = isExpanded,
                onToggleExpand = {
                    expandedQuestionIndex = if (isExpanded) null else index
                },
                onSaveEdit = { updated ->
                    onUpdateQuestion(index, updated)
                    expandedQuestionIndex = null
                }
            )
        }

        // Final Create Test CTA Card
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onCreateTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClassicBluePrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CREATE TEST (${questions.size} Questions)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SubjectPill(subject: String, count: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subject,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$count",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ExtractedQuestionReviewCard(
    question: ExtractedQuestionDraft,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSaveEdit: (ExtractedQuestionDraft) -> Unit
) {
    var editText by remember(question.questionText) { mutableStateOf(question.questionText) }
    var optA by remember(question.optionA) { mutableStateOf(question.optionA) }
    var optB by remember(question.optionB) { mutableStateOf(question.optionB) }
    var optC by remember(question.optionC) { mutableStateOf(question.optionC) }
    var optD by remember(question.optionD) { mutableStateOf(question.optionD) }
    var correctAns by remember(question.correctAnswer) { mutableStateOf(question.correctAnswer) }
    var subj by remember(question.subject) { mutableStateOf(question.subject) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (question.confidence < 0.85f) Color(0xFFF59E0B).copy(alpha = 0.5f) else Slate800
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Q${question.questionNumber}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = question.subject,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (question.answerSource == "EXTRACTED_FROM_DOCUMENT")
                            Color(0xFF10B981).copy(alpha = 0.15f)
                        else
                            ClassicBluePrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Ans: ${question.correctAnswer} (${if (question.answerSource == "EXTRACTED_FROM_DOCUMENT") "Key" else "AI"})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (question.answerSource == "EXTRACTED_FROM_DOCUMENT") Color(0xFF10B981) else ClassicBlueLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Text(
                text = question.questionText,
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2
            )

            // Expanded Editor
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("Question Text") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClassicBlueLight,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = optA,
                            onValueChange = { optA = it },
                            label = { Text("Option A") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClassicBlueLight,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = optB,
                            onValueChange = { optB = it },
                            label = { Text("Option B") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClassicBlueLight,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = optC,
                            onValueChange = { optC = it },
                            label = { Text("Option C") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClassicBlueLight,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = optD,
                            onValueChange = { optD = it },
                            label = { Text("Option D") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClassicBlueLight,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Correct: ", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            listOf("A", "B", "C", "D").forEach { opt ->
                                FilterChip(
                                    selected = correctAns == opt,
                                    onClick = { correctAns = opt },
                                    label = { Text(opt) },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                onSaveEdit(
                                    question.copy(
                                        questionText = editText,
                                        optionA = optA,
                                        optionB = optB,
                                        optionC = optC,
                                        optionD = optD,
                                        correctAnswer = correctAns,
                                        subject = subj
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ClassicBluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

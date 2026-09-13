package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.importer.PyqConversionReport
import com.example.data.importer.PyqNormalizedQuestion
import com.example.data.importer.PyqSourceDocument
import com.example.data.importer.PyqSourceMetadata
import com.example.data.importer.PyqSourceType
import com.example.data.importer.SourceDocumentFormat
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel

/**
 * Developer-only modal for importing and converting historical source documents (JSON/CSV/TXT)
 * into canonical PYQ datasets with verification safeguards.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperDatasetImportDialog(
    viewModel: FocusinViewModel,
    onDismiss: () -> Unit,
    onOpenReviewQueue: () -> Unit
) {
    var format by remember { mutableStateOf(SourceDocumentFormat.JSON) }
    var targetExam by remember { mutableStateOf("AIPMT") }
    var targetYearInput by remember { mutableStateOf("2008") }
    var targetSession by remember { mutableStateOf("MAIN") }
    var sourceType by remember { mutableStateOf(PyqSourceType.USER_PROVIDED.name) }
    var sourceName by remember { mutableStateOf("User Provided Historical Extract") }
    var primaryEvidenceUri by remember { mutableStateOf("") }
    var sourceContent by remember { mutableStateOf("") }
    var answerKeyContent by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var conversionReport by remember { mutableStateOf<PyqConversionReport?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Developer PYQ Source Ingestion",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Converts and validates external historical source material into canonical PYQ schema. Questions without official primary evidence are strictly set to UNVERIFIED.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                }

                // Format Selector Chips
                item {
                    Text("Source Format:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SourceDocumentFormat.values().forEach { fmt ->
                            val isSelected = format == fmt
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { format = fmt },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else Slate850,
                                border = BorderStroke(1.dp, if (isSelected) CyanPrimary else Slate800)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = fmt.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyanPrimary else Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }
                }

                // Exam & Year Controls
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Exam
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Exam:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("AIPMT", "NEET_UG").forEach { exam ->
                                    val sel = targetExam == exam
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { targetExam = exam },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (sel) CyanPrimary.copy(alpha = 0.2f) else Slate850,
                                        border = BorderStroke(1.dp, if (sel) CyanPrimary else Slate800)
                                    ) {
                                        Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                            Text(exam, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (sel) CyanPrimary else Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // Year
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Year (2005-2025):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            OutlinedTextField(
                                value = targetYearInput,
                                onValueChange = { targetYearInput = it },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Source Provenance
                item {
                    Text("Source Provenance:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    OutlinedTextField(
                        value = sourceName,
                        onValueChange = { sourceName = it },
                        placeholder = { Text("e.g. Official AIPMT 2008 Paper Archive", color = Color(0xFF64748B), fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Primary Evidence Citation
                item {
                    Text("Official Evidence Citation (Required for VERIFIED):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    OutlinedTextField(
                        value = primaryEvidenceUri,
                        onValueChange = { primaryEvidenceUri = it },
                        placeholder = { Text("e.g. Official Answer Key Document ID / Scan URI", color = Color(0xFF64748B), fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Source Content Field
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Raw Source Content:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        TextButton(
                            onClick = {
                                sourceContent = when (format) {
                                    SourceDocumentFormat.JSON -> """
                                        [
                                          {
                                            "number": 1,
                                            "questionText": "An electric dipole of moment p is placed in an electric field of intensity E. The dipole acquires a position such that the axis of the dipole makes an angle θ with the direction of the field. Assuming that the potential energy of the dipole to be zero when θ = 90°, the torque and the potential energy of the dipole will respectively be:",
                                            "options": ["pE sin θ, -pE cos θ", "pE sin θ, -2pE cos θ", "pE sin θ, 2pE cos θ", "pE cos θ, -pE cos θ"],
                                            "answer": "A",
                                            "subject": "PHYSICS",
                                            "chapter": "electrostatics",
                                            "topic": "Electric Dipole in Field"
                                          }
                                        ]
                                    """.trimIndent()
                                    SourceDocumentFormat.CSV -> """
                                        Number,Question,OptionA,OptionB,OptionC,OptionD,Answer,Subject,Chapter,Topic
                                        1,"The dimension of magnetic flux is:","[ML2T-2A-1]","[ML0T-2A-2]","[M0L-2T-2A-1]","[ML2T-1A-2]","A","PHYSICS","magnetism","Magnetic Flux"
                                    """.trimIndent()
                                    SourceDocumentFormat.TXT, SourceDocumentFormat.PDF_TEXT -> """
                                        1. The unit of thermal conductivity is:
                                        (A) J m-1 K-1
                                        (B) W m-1 K-1
                                        (C) W m K-1
                                        (D) J m K-1
                                        Ans: (B)
                                    """.trimIndent()
                                }
                            }
                        ) {
                            Text("Paste Sample", color = CyanPrimary, fontSize = 10.sp)
                        }
                    }

                    OutlinedTextField(
                        value = sourceContent,
                        onValueChange = { sourceContent = it },
                        placeholder = { Text("Paste raw $format content here...", color = Color(0xFF64748B), fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Optional Separate Answer Key
                item {
                    Text("Separate Answer Key (Optional):", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    OutlinedTextField(
                        value = answerKeyContent,
                        onValueChange = { answerKeyContent = it },
                        placeholder = { Text("e.g. 1: B, 2: D, 3: A", color = Color(0xFF64748B), fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Conversion Action Button
                item {
                    Button(
                        onClick = {
                            if (sourceContent.isBlank()) {
                                statusMessage = "Please provide raw source content."
                                return@Button
                            }
                            isProcessing = true
                            statusMessage = null
                            val yearVal = targetYearInput.trim().toIntOrNull()
                            val doc = PyqSourceDocument(
                                documentId = "DEV_DOC_${System.currentTimeMillis()}",
                                format = format,
                                rawContent = sourceContent,
                                metadata = PyqSourceMetadata(
                                    sourceType = sourceType,
                                    sourceName = sourceName.ifBlank { "User Provided Extract" },
                                    primaryEvidenceUri = primaryEvidenceUri.ifBlank { null }
                                ),
                                targetExam = targetExam,
                                targetYear = yearVal,
                                targetSession = targetSession,
                                answerKeyContent = answerKeyContent.ifBlank { null }
                            )

                            viewModel.convertPyqSource(doc) { report ->
                                isProcessing = false
                                conversionReport = report
                                statusMessage = "Parsed ${report.parsedQuestions} questions. ${report.reviewQueueCount} items queued for review."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Slate950, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Running Ingestion Pipeline...", color = Slate950, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Slate950, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Conversion Pipeline", color = Slate950, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Report Preview Area
                conversionReport?.let { report ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Slate950),
                            border = BorderStroke(1.dp, if (report.isClean) EmeraldSuccess.copy(alpha = 0.5f) else Color(0xFFFBBF24).copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pipeline Report Summary", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        if (report.isClean) "CLEAN" else "FLAGGED (${report.reviewQueueCount} QUEUED)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (report.isClean) EmeraldSuccess else Color(0xFFFBBF24)
                                    )
                                }
                                Text("• Parsed: ${report.parsedQuestions} | Valid: ${report.validQuestions} | Invalid: ${report.invalidQuestions}", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                                Text("• Missing Answers: ${report.missingAnswers} | Unmapped Chapters: ${report.unmappedChapters}", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                                Text("• Verified: ${report.verifiedCount} | Unverified: ${report.unverifiedCount} | Duplicates: ${report.duplicates}", fontSize = 10.sp, color = Color(0xFFCBD5E1))

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onDismiss()
                                            onOpenReviewQueue()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Open Review Queue (${report.reviewQueueCount})", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.commitReviewedPyqs { count ->
                                                statusMessage = "Committed $count approved questions directly to Room database."
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Commit Clean to DB", fontSize = 10.sp, color = Slate950, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                statusMessage?.let { msg ->
                    item {
                        Text(
                            text = msg,
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        },
        containerColor = Slate900
    )
}

/**
 * Developer and Ingestion Human Review / Validation Queue Dialog.
 *
 * Allows individual questions to be reviewed, chapter mappings adjusted,
 * primary evidence verified, and clean items committed to Room.
 */
@Composable
fun DeveloperReviewQueueDialog(
    viewModel: FocusinViewModel,
    onDismiss: () -> Unit
) {
    val reviewQueue by viewModel.pyqReviewQueue.collectAsState()
    var commitMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PYQ Review Queue (${reviewQueue.size})",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (reviewQueue.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearReviewQueue() }) {
                        Text("Clear", color = RoseError, fontSize = 11.sp)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (reviewQueue.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Validation queue is empty.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("No pending or flagged questions require review.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Review items before database commitment. Questions can only become VERIFIED with explicit primary evidence citation.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reviewQueue, key = { it.id }) { question ->
                            ReviewQueueItemCard(
                                question = question,
                                onApproveVerified = { citation ->
                                    viewModel.approveReviewItem(question.id, citation)
                                },
                                onKeepUnverified = {
                                    viewModel.keepReviewItemUnverified(question.id)
                                },
                                onReject = {
                                    viewModel.rejectReviewItem(question.id)
                                },
                                onMapChapter = { sub, chap, top ->
                                    viewModel.updateReviewItemChapter(question.id, sub, chap, top)
                                },
                                onUpdateAnswer = { ansIdx ->
                                    viewModel.updateReviewItemAnswer(question.id, ansIdx)
                                }
                            )
                        }
                    }
                }

                commitMessage?.let {
                    Text(text = it, color = EmeraldSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (reviewQueue.any { !it.needsHumanReview && it.chapterId != "UNMAPPED" }) {
                    Button(
                        onClick = {
                            viewModel.commitReviewedPyqs { count ->
                                commitMessage = "Committed $count reviewed items into database."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("Commit Approved to DB", color = Slate950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color.White)
                }
            }
        },
        containerColor = Slate900
    )
}

@Composable
private fun ReviewQueueItemCard(
    question: PyqNormalizedQuestion,
    onApproveVerified: (String) -> Unit,
    onKeepUnverified: () -> Unit,
    onReject: () -> Unit,
    onMapChapter: (String, String, String) -> Unit,
    onUpdateAnswer: (Int) -> Unit
) {
    var evidenceCitationInput by remember { mutableStateOf("") }
    var showChapterPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Slate950),
        border = BorderStroke(1.dp, if (question.isVerified) EmeraldSuccess.copy(alpha = 0.4f) else Slate800)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${question.sourceExam} ${question.examYear ?: "HIST"} (${question.paperSession}) • Item #${question.questionNumber ?: "?"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    color = if (question.isVerified) EmeraldSuccess.copy(alpha = 0.2f) else Color(0xFFFBBF24).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = question.verificationStatus,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (question.isVerified) EmeraldSuccess else Color(0xFFFBBF24),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Warnings
            if (question.reviewNotes.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Slate900)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    question.reviewNotes.forEach { note ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(note, fontSize = 9.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }
            }

            // Question Text
            Text(
                text = question.questionText,
                fontSize = 11.sp,
                color = Color.White,
                lineHeight = 15.sp
            )

            // Options Preview
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                question.options.forEachIndexed { optIdx, optText ->
                    val isCorrect = optIdx == question.correctOptionIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCorrect) EmeraldSuccess.copy(alpha = 0.12f) else Color.Transparent)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${listOf("A", "B", "C", "D").getOrElse(optIdx) { "?" }}:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) EmeraldSuccess else Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = optText,
                            fontSize = 10.sp,
                            color = if (isCorrect) EmeraldSuccess else Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // Chapter / Subject mapping badge & quick fix
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (question.isUnmappedChapter) RoseError.copy(alpha = 0.2f) else Slate850,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, if (question.isUnmappedChapter) RoseError else Slate700)
                ) {
                    Text(
                        text = "Subject: ${question.subjectId} | Chapter: ${question.chapterId}",
                        fontSize = 9.sp,
                        color = if (question.isUnmappedChapter) RoseError else Color(0xFFCBD5E1),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                if (question.isUnmappedChapter) {
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = { showChapterPicker = true },
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Fix Chapter", fontSize = 9.sp, color = CyanPrimary)
                    }
                }
            }

            // Quick Chapter Picker Dropdown
            if (showChapterPicker) {
                DropdownMenu(
                    expanded = showChapterPicker,
                    onDismissRequest = { showChapterPicker = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Physics: Kinematics") },
                        onClick = {
                            onMapChapter("PHYSICS", "neet_phy_kinematics", "Motion in Straight Line")
                            showChapterPicker = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Physics: Current Electricity") },
                        onClick = {
                            onMapChapter("PHYSICS", "neet_phy_current_electricity", "Ohm's Law")
                            showChapterPicker = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Chemistry: Thermodynamics") },
                        onClick = {
                            onMapChapter("CHEMISTRY", "neet_chem_thermodynamics", "Enthalpy")
                            showChapterPicker = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Biology: Genetics") },
                        onClick = {
                            onMapChapter("BIOLOGY", "neet_bio_genetics", "Mendelian Inheritance")
                            showChapterPicker = false
                        }
                    )
                }
            }

            // Answer index quick selector if missing
            if (question.isMissingAnswer) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Select Answer:", fontSize = 9.sp, color = Color(0xFFFBBF24))
                    listOf("A" to 0, "B" to 1, "C" to 2, "D" to 3).forEach { (label, idx) ->
                        Surface(
                            modifier = Modifier.clickable { onUpdateAnswer(idx) },
                            shape = RoundedCornerShape(4.dp),
                            color = Slate850,
                            border = BorderStroke(0.5.dp, Slate700)
                        ) {
                            Text(label, fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }

            Divider(color = Slate850, thickness = 0.5.dp)

            // Evidence Citation Input for VERIFIED Approval
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = evidenceCitationInput,
                    onValueChange = { evidenceCitationInput = it },
                    placeholder = { Text("Official Key Citation (e.g. NTA Ans Key 2024)", color = Color(0xFF64748B), fontSize = 9.sp) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Button(
                    onClick = { onApproveVerified(evidenceCitationInput.trim()) },
                    enabled = evidenceCitationInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text("Approve VERIFIED", fontSize = 9.sp, color = Slate950, fontWeight = FontWeight.Bold)
                }
            }

            // Secondary Actions: Keep Unverified & Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onKeepUnverified,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Keep UNVERIFIED", fontSize = 9.sp)
                }

                TextButton(
                    onClick = onReject,
                    colors = ButtonDefaults.textButtonColors(contentColor = RoseError),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject", fontSize = 9.sp)
                }
            }
        }
    }
}

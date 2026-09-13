package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ImportedTestEntity
import com.example.data.repository.FocusinRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyTestsTabContent(
    repository: FocusinRepository,
    onNavigateToImportTest: () -> Unit,
    onNavigateToSolveTest: (String) -> Unit,
    onNavigateToMistakeDiary: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val importedTests by repository.allImportedTests.collectAsState(initial = emptyList())
    var testToDelete by remember { mutableStateOf<ImportedTestEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Quick Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ClassicBluePrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Private Test Archive",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = ClassicBluePrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${importedTests.size} Tests",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClassicBlueLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Solve your coaching & private test papers with full OMR simulation and mistake analysis.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onNavigateToImportTest,
                        colors = ButtonDefaults.buttonColors(containerColor = ClassicBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("IMPORT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (importedTests.isEmpty()) {
            // Empty State
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = Slate800,
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = ClassicBlueLight,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Imported Tests Yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Import any PDF coaching paper or photos of test pages. AI automatically extracts questions, verifies answers, and builds a realistic NEET test with OMR.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onNavigateToImportTest,
                            colors = ButtonDefaults.buttonColors(containerColor = ClassicBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("IMPORT YOUR FIRST TEST", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // List of Imported Tests
            items(importedTests, key = { it.testId }) { test ->
                ImportedTestCard(
                    test = test,
                    onStartTest = { onNavigateToSolveTest(test.testId) },
                    onDeleteTest = { testToDelete = test },
                    onNavigateToMistakeDiary = onNavigateToMistakeDiary
                )
            }
        }
    }

    // Delete Confirmation Dialog
    testToDelete?.let { test ->
        AlertDialog(
            onDismissRequest = { testToDelete = null },
            containerColor = Slate900,
            title = {
                Text("Delete Imported Test?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${test.title}\"? All extracted questions and attempts will be removed.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteImportedTest(test.testId)
                            testToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { testToDelete = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun ImportedTestCard(
    test: ImportedTestEntity,
    onStartTest: () -> Unit,
    onDeleteTest: () -> Unit,
    onNavigateToMistakeDiary: () -> Unit
) {
    val dateStr = remember(test.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(test.createdAt))
    }
    val isCompleted = test.status == "COMPLETED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isCompleted) Color(0xFF10B981).copy(alpha = 0.35f) else Slate800
        )
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
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (isCompleted) Color(0xFF10B981).copy(alpha = 0.15f) else Slate800,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "ATTEMPTED" else "READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color(0xFF10B981) else Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(
                    onClick = onDeleteTest,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Test",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = test.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpecBadge(Icons.Default.Quiz, "${test.totalQuestions} MCQs")
                SpecBadge(Icons.Default.Grade, "${test.maxScore} Marks")
                SpecBadge(Icons.Default.Timer, "${test.durationMinutes} Mins")
            }

            // Results Section if completed
            if (isCompleted) {
                Surface(
                    color = Slate950,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Score", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "${test.score} / ${test.maxScore}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                        Column {
                            Text("Accuracy", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "${test.accuracy}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text("Mistakes", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "${test.wrongCount}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (test.wrongCount > 0) Color(0xFFEF4444) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isCompleted) {
                    OutlinedButton(
                        onClick = onStartTest,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-Attempt", fontSize = 13.sp)
                    }

                    if (test.wrongCount > 0) {
                        Button(
                            onClick = onNavigateToMistakeDiary,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mistakes", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onStartTest,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ClassicBluePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("START TEST (OMR MODE)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}

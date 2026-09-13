package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.local.entity.MistakeEntity
import com.example.data.repository.FocusinRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MistakePracticeTabContent(
    repository: FocusinRepository,
    onNavigateToMistakeDiary: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allMistakes by repository.allMistakes.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "IMPORTED", "PHYSICS", "CHEMISTRY", "BIOLOGY", "UNRESOLVED"

    val filteredMistakes = remember(allMistakes, selectedFilter) {
        when (selectedFilter) {
            "ALL" -> allMistakes
            "IMPORTED" -> allMistakes.filter { it.testTitle.contains("[Imported]", ignoreCase = true) }
            "PHYSICS" -> allMistakes.filter { it.subjectName.equals("Physics", ignoreCase = true) }
            "CHEMISTRY" -> allMistakes.filter { it.subjectName.equals("Chemistry", ignoreCase = true) }
            "BIOLOGY" -> allMistakes.filter {
                it.subjectName.equals("Biology", ignoreCase = true) ||
                it.subjectName.equals("Botany", ignoreCase = true) ||
                it.subjectName.equals("Zoology", ignoreCase = true)
            }
            "UNRESOLVED" -> allMistakes.filter { !it.isResolved }
            else -> allMistakes
        }
    }

    val unresolvedCount = remember(allMistakes) { allMistakes.count { !it.isResolved } }
    val resolvedCount = remember(allMistakes) { allMistakes.count { it.isResolved } }
    val importedMistakesCount = remember(allMistakes) {
        allMistakes.count { it.testTitle.contains("[Imported]", ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary & Navigation Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
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
                                text = "Mistake Practice Lab",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Re-attempt questions you previously got wrong.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = onNavigateToMistakeDiary,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Full Diary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Stat Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MistakeStatBadge("Total Wrong", "${allMistakes.size}", Color.White)
                        MistakeStatBadge("Unresolved", "$unresolvedCount", Color(0xFFEF4444))
                        MistakeStatBadge("Resolved", "$resolvedCount", Color(0xFF10B981))
                        MistakeStatBadge("From Imports", "$importedMistakesCount", Color(0xFF38BDF8))
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "ALL" to "All Mistakes",
                    "UNRESOLVED" to "Unresolved ($unresolvedCount)",
                    "IMPORTED" to "From Imported Tests ($importedMistakesCount)",
                    "PHYSICS" to "Physics",
                    "CHEMISTRY" to "Chemistry",
                    "BIOLOGY" to "Biology"
                )

                items(filters) { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444),
                            selectedLabelColor = Color.White,
                            containerColor = Slate900,
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFFEF4444) else Slate800)
                    )
                }
            }
        }

        if (filteredMistakes.isEmpty()) {
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (allMistakes.isEmpty()) "No Mistakes Recorded!" else "No Mistakes in this filter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (allMistakes.isEmpty())
                                "Solve any imported test or practice paper. Any wrong questions are automatically captured here for targeted revision."
                            else
                                "Switch filter above to view other subjects or unresolved questions.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredMistakes, key = { it.id }) { mistake ->
                MistakePracticeItemCard(
                    mistake = mistake,
                    onToggleResolved = {
                        coroutineScope.launch {
                            repository.markMistakeResolved(mistake.id, !mistake.isResolved)
                        }
                    },
                    onUpdateReason = { reason ->
                        coroutineScope.launch {
                            repository.updateMistakeReason(mistake.id, reason, mistake.studentNotes)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MistakeStatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun MistakePracticeItemCard(
    mistake: MistakeEntity,
    onToggleResolved: () -> Unit,
    onUpdateReason: (String) -> Unit
) {
    val options = remember(mistake.optionsJson) {
        mistake.optionsJson.split("|||").filter { it.isNotBlank() }
    }
    var selectedOptionForRetry by remember { mutableStateOf<String?>(null) }
    var hasAnsweredRetry by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (mistake.isResolved) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = mistake.subjectName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = mistake.testTitle,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1
                    )
                }

                // Resolved status toggle
                Surface(
                    color = if (mistake.isResolved) Color(0xFF10B981).copy(alpha = 0.15f) else Slate800,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onToggleResolved() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (mistake.isResolved) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (mistake.isResolved) Color(0xFF10B981) else Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (mistake.isResolved) "Resolved" else "Mark Resolved",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mistake.isResolved) Color(0xFF10B981) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Text(
                text = mistake.questionText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            // Prior attempt info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "You Marked: ${mistake.selectedOption}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Correct: ${mistake.correctOption}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            if (mistake.explanation.isNotBlank()) {
                Surface(
                    color = Slate950,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Solution & Explanation:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClassicBlueLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mistake.explanation,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            // Error Reason Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Tag: ", fontSize = 11.sp, color = Color(0xFF94A3B8))
                listOf("SILLY_MISTAKE" to "Silly", "FORMULA_FORGOT" to "Formula", "CONCEPT_GAP" to "Concept").forEach { (code, lbl) ->
                    val isTagSelected = mistake.errorReason == code
                    Surface(
                        color = if (isTagSelected) ClassicBluePrimary else Slate800,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onUpdateReason(code) }
                    ) {
                        Text(
                            text = lbl,
                            fontSize = 10.sp,
                            color = if (isTagSelected) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

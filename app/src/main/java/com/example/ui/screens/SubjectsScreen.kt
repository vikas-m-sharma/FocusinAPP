package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SubjectEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel

@Composable
fun SubjectsScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<SubjectEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Slate950)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Subjects & Topics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Manage your focus categories and weekly target hours",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (subjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No subjects added yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your classes or skills to track time precisely.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(subjects, key = { it.id }) { subject ->
                        SubjectDetailCard(
                            subject = subject,
                            onEdit = { subjectToEdit = subject },
                            onDelete = { viewModel.deleteSubject(subject) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }
        }

        // Add FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_subject_fab"),
            containerColor = CyanPrimary,
            contentColor = Slate950
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Subject")
        }
    }

    // Add Subject Dialog
    if (showAddDialog) {
        SubjectEditDialog(
            subjectToEdit = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, colorHex, hours ->
                viewModel.addSubject(name, colorHex, "School", hours)
                showAddDialog = false
            }
        )
    }

    // Edit Subject Dialog
    if (subjectToEdit != null) {
        SubjectEditDialog(
            subjectToEdit = subjectToEdit,
            onDismiss = { subjectToEdit = null },
            onSave = { name, colorHex, hours ->
                viewModel.updateSubject(
                    subjectToEdit!!.copy(
                        name = name,
                        colorHex = colorHex,
                        targetWeeklyHours = hours
                    )
                )
                subjectToEdit = null
            }
        )
    }
}

@Composable
fun SubjectDetailCard(
    subject: SubjectEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val targetMinutes = (subject.targetWeeklyHours * 60).toInt()
    val progress = if (targetMinutes > 0) {
        (subject.actualFocusedMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
    } else 0f

    val subColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (_: Exception) {
        CyanPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(subColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = subject.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseError, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress towards weekly target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${subject.actualFocusedMinutes / 60}h ${subject.actualFocusedMinutes % 60}m completed",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Target: ${subject.targetWeeklyHours.toInt()}h/week",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = subColor,
                trackColor = Slate800
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${subject.sessionsCompleted} total focus blocks completed",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SubjectEditDialog(
    subjectToEdit: SubjectEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String, targetHours: Float) -> Unit
) {
    var name by remember { mutableStateOf(subjectToEdit?.name ?: "") }
    var targetHours by remember { mutableFloatStateOf(subjectToEdit?.targetWeeklyHours ?: 10f) }
    var selectedColor by remember { mutableStateOf(subjectToEdit?.colorHex ?: "#38BDF8") }

    val colors = listOf("#38BDF8", "#34D399", "#A78BFA", "#FBBF24", "#FB7185", "#22D3EE", "#818CF8")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (subjectToEdit != null) "Edit Subject" else "New Subject") },
        text = {
            Column {
                Text("Subject Name", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Mathematics") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Weekly Focus Target: ${targetHours.toInt()} Hours", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = targetHours,
                    onValueChange = { targetHours = it },
                    valueRange = 2f..30f,
                    steps = 27
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Subject Color", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { hex ->
                        val isSel = (hex == selectedColor)
                        val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { CyanPrimary }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { selectedColor = hex }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onSave(name, selectedColor, targetHours)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Save", color = Slate950, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

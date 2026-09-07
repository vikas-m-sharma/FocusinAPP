package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.entity.VoiceRecordingEntity
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel

@Composable
fun VoiceNotesScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val voiceNotes by viewModel.voiceRecordings.collectAsState()
    val isRecording by viewModel.isAudioRecording.collectAsState()
    val currentlyPlayingId by viewModel.currentlyPlayingId.collectAsState()

    var showNameDialog by remember { mutableStateOf(false) }
    var recordingTitle by remember { mutableStateOf("") }
    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val started = viewModel.startVoiceRecording()
            if (!started) {
                permissionDeniedMessage = "Unable to start audio recorder."
            }
        } else {
            permissionDeniedMessage = "Audio recording permission is required to save motivational voice notes."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Motivational Voice Notes",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Record personal reminders in your own voice to trigger at session start or when focus slips.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Recording Studio Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) RoseError.copy(alpha = 0.2f) else CyanPrimary.copy(alpha = 0.15f))
                        .clickable {
                            if (isRecording) {
                                showNameDialog = true
                            } else {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPerm) {
                                    viewModel.startVoiceRecording()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("record_audio_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                        tint = if (isRecording) RoseError else CyanPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isRecording) "RECORDING IN PROGRESS..." else "Tap Mic to Record",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) RoseError else CyanPrimary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (isRecording) "Tap again to save note" else "Speak your goal, target score, or reason for studying",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (permissionDeniedMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = permissionDeniedMessage!!,
                color = RoseError,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Saved Notes List
        Text(
            text = "Your Recorded Notes (${voiceNotes.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (voiceNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No voice notes recorded yet. Add one above.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(voiceNotes, key = { it.id }) { note ->
                    VoiceNoteCard(
                        recording = note,
                        isPlaying = currentlyPlayingId == note.id,
                        onPlayToggle = { viewModel.playVoiceRecording(note) },
                        onDelete = { viewModel.deleteVoiceRecording(note) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }

    // Save Name Dialog
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.stopVoiceRecording(recordingTitle)
                showNameDialog = false
                recordingTitle = ""
            },
            title = { Text("Save Voice Motivation") },
            text = {
                Column {
                    Text("Give this motivational reminder a title:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recordingTitle,
                        onValueChange = { recordingTitle = it },
                        placeholder = { Text("e.g. My Goal: Clear GATE Exam") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopVoiceRecording(recordingTitle)
                        showNameDialog = false
                        recordingTitle = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Save", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.stopVoiceRecording("Voice Memo")
                        showNameDialog = false
                        recordingTitle = ""
                    }
                ) {
                    Text("Save as Default")
                }
            }
        )
    }
}

@Composable
fun VoiceNoteCard(
    recording: VoiceRecordingEntity,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) CyanPrimary else Slate800)
                        .clickable { onPlayToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) Slate950 else CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = recording.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${recording.durationSeconds} seconds audio",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

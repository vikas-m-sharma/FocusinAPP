package com.example.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeneratedTimetablePlan
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TimetableSessionEntity
import com.example.data.local.entity.VoiceRecordingEntity
import com.example.ui.theme.AmethystAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: FocusinViewModel,
    onStartSession: (TimetableSessionEntity) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val voiceRecordings by viewModel.voiceRecordings.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiGeneratedPlan by viewModel.aiGeneratedTimetable.collectAsState()
    val aiGoalPlan by viewModel.aiGoalPlan.collectAsState()

    val daySessions = allSessions.filter { it.dayOfWeek == selectedDay }

    var showCreateDialog by remember { mutableStateOf(false) }
    var sessionToEdit by remember { mutableStateOf<TimetableSessionEntity?>(null) }
    var sessionToDuplicate by remember { mutableStateOf<TimetableSessionEntity?>(null) }
    var showAiGeneratorDialog by remember { mutableStateOf(false) }
    var showGoalPlannerDialog by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        Pair(1, "Monday"),
        Pair(2, "Tuesday"),
        Pair(3, "Wednesday"),
        Pair(4, "Thursday"),
        Pair(5, "Friday"),
        Pair(6, "Saturday"),
        Pair(7, "Sunday")
    )

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White
                ),
                title = {
                    Column {
                        Text(
                            text = "Schedule",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Plan your time. Protect your focus.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    // AI Actions
                    IconButton(
                        onClick = { showAiGeneratorDialog = true },
                        modifier = Modifier.testTag("ai_schedule_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Create Schedule with AI",
                            tint = CyanPrimary
                        )
                    }
                    // Settings shortcut
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button_schedule")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CyanPrimary,
                contentColor = Slate950,
                shape = CircleShape,
                modifier = Modifier.testTag("create_session_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Focus Session",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Day Selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(daysOfWeek) { (dayNum, dayName) ->
                    val isSelected = selectedDay == dayNum
                    val sessionCount = allSessions.count { it.dayOfWeek == dayNum }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.selectDay(dayNum) }
                            .testTag("day_selector_$dayNum"),
                        color = if (isSelected) CyanPrimary else Slate900,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayName.take(3).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Slate950 else Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$sessionCount sess",
                                fontSize = 10.sp,
                                color = if (isSelected) Slate950.copy(alpha = 0.8f) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Sessions List or Empty State
            if (daySessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Slate900),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your day is still open",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create your first focus session to protect your study time.",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("empty_create_session_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Slate950, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Session", color = Slate950, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(daySessions, key = { it.id }) { session ->
                        ScheduleSessionCard(
                            session = session,
                            onStart = { onStartSession(session) },
                            onEdit = { sessionToEdit = session },
                            onDelete = { viewModel.deleteSession(session) },
                            onDuplicate = { sessionToDuplicate = session },
                            onToggleEnabled = { viewModel.toggleSessionEnabled(session) }
                        )
                    }
                }
            }
        }
    }

    // CREATE FOCUS SESSION DIALOG
    if (showCreateDialog) {
        CreateOrEditSessionDialog(
            dayOfWeek = selectedDay,
            subjects = subjects,
            voiceRecordings = voiceRecordings,
            sessionToEdit = null,
            onDismiss = { showCreateDialog = false },
            onSave = { newSession ->
                viewModel.addSession(newSession)
                showCreateDialog = false
            },
            onCreateSubject = { name, color ->
                viewModel.addSubject(name, color, "menu_book", 10f)
            }
        )
    }

    // EDIT SESSION DIALOG
    sessionToEdit?.let { session ->
        CreateOrEditSessionDialog(
            dayOfWeek = session.dayOfWeek,
            subjects = subjects,
            voiceRecordings = voiceRecordings,
            sessionToEdit = session,
            onDismiss = { sessionToEdit = null },
            onSave = { updated ->
                viewModel.updateSession(updated)
                sessionToEdit = null
            },
            onCreateSubject = { name, color ->
                viewModel.addSubject(name, color, "menu_book", 10f)
            }
        )
    }

    // DUPLICATE SESSION DIALOG
    sessionToDuplicate?.let { session ->
        DuplicateSessionDialog(
            session = session,
            onDismiss = { sessionToDuplicate = null },
            onDuplicate = { targetDay ->
                viewModel.duplicateSession(session.id, targetDay)
                sessionToDuplicate = null
            }
        )
    }

    // AI TIMETABLE GENERATOR MODAL (REVIEW AND APPROVE)
    if (showAiGeneratorDialog) {
        AiTimetableGeneratorModal(
            isGenerating = isAiGenerating,
            generatedPlan = aiGeneratedPlan,
            onDismiss = {
                viewModel.dismissAiTimetable()
                showAiGeneratorDialog = false
            },
            onGenerate = { prompt ->
                viewModel.generateTimetableWithAi(prompt)
            },
            onApply = {
                viewModel.applyAiGeneratedTimetable()
                showAiGeneratorDialog = false
            }
        )
    }
}

@Composable
fun ScheduleSessionCard(
    session: TimetableSessionEntity,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val subjectColor = try {
        Color(android.graphics.Color.parseColor(session.colorHex))
    } catch (_: Exception) {
        CyanPrimary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${session.id}")
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
                            .background(subjectColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = session.subjectName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Start Button
                    IconButton(
                        onClick = onStart,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary)
                            .testTag("start_session_${session.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Focus Session",
                            tint = Slate950,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // More Menu
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Slate850)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Session", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = CyanPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate to another day", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Session", color = RoseError) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseError) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task / Description
            Text(
                text = session.taskName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE2E8F0),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Meta Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Range & Duration
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${session.startTime} – ${session.endTime}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanBright
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${session.durationMinutes}m)",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Badges (Protection, Alarm, Voice)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (session.focusModeEnabled) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Protection", fontSize = 10.sp, color = EmeraldSuccess)
                            }
                        }
                    }
                    if (session.alarmEnabled) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Alarm", fontSize = 10.sp, color = CyanPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrEditSessionDialog(
    dayOfWeek: Int,
    subjects: List<SubjectEntity>,
    voiceRecordings: List<VoiceRecordingEntity>,
    sessionToEdit: TimetableSessionEntity?,
    onDismiss: () -> Unit,
    onSave: (TimetableSessionEntity) -> Unit,
    onCreateSubject: (String, String) -> Unit
) {
    val context = LocalContext.current

    var selectedSubject by remember {
        mutableStateOf(
            if (sessionToEdit != null) {
                subjects.find { it.id == sessionToEdit.subjectId } ?: subjects.firstOrNull()
            } else {
                subjects.firstOrNull()
            }
        )
    }

    var taskName by remember { mutableStateOf(sessionToEdit?.taskName ?: "") }
    var startHour by remember {
        mutableIntStateOf(sessionToEdit?.startTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 6)
    }
    var startMinute by remember {
        mutableIntStateOf(sessionToEdit?.startTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0)
    }
    var endHour by remember {
        mutableIntStateOf(sessionToEdit?.endTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8)
    }
    var endMinute by remember {
        mutableIntStateOf(sessionToEdit?.endTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    var focusProtection by remember { mutableStateOf(sessionToEdit?.focusModeEnabled ?: true) }
    var alarmEnabled by remember { mutableStateOf(sessionToEdit?.alarmEnabled ?: true) }
    var recurrence by remember { mutableStateOf(sessionToEdit?.recurrenceType ?: "WEEKLY") }
    var soundName by remember { mutableStateOf(sessionToEdit?.soundName ?: "Default Chime") }
    var soundUri by remember { mutableStateOf<String?>(sessionToEdit?.soundUri) }
    var selectedVoiceNoteId by remember { mutableStateOf<Long?>(sessionToEdit?.voiceNoteId) }

    var showNewSubjectDialog by remember { mutableStateOf(false) }

    // Audio file picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            soundUri = uri.toString()
            soundName = "Custom Audio (${uri.lastPathSegment?.takeLast(14) ?: "File"})"
        }
    }

    // Duration calculation
    val startTotalMinutes = startHour * 60 + startMinute
    val endTotalMinutes = endHour * 60 + endMinute
    val calculatedDurationMinutes = if (endTotalMinutes >= startTotalMinutes) {
        endTotalMinutes - startTotalMinutes
    } else {
        (24 * 60 - startTotalMinutes) + endTotalMinutes
    }.coerceAtLeast(1)

    val durationHours = calculatedDurationMinutes / 60
    val durationRemMins = calculatedDurationMinutes % 60
    val durationDisplay = if (durationHours > 0) "${durationHours}h ${durationRemMins}m" else "${durationRemMins}m"

    val formatTime: (Int, Int) -> String = { h, m ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
        }
        java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Text(
                text = if (sessionToEdit != null) "Edit Focus Session" else "Create Focus Session",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Time Range Row with Pickers & Auto Duration
                item {
                    Text("Time & Duration", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start Time Button
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        startHour = h
                                        startMinute = m
                                    },
                                    startHour,
                                    startMinute,
                                    false
                                ).show()
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Start", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(formatTime(startHour, startMinute), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                            }
                        }

                        Text("→", fontSize = 18.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)

                        // End Time Button
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        endHour = h
                                        endMinute = m
                                    },
                                    endHour,
                                    endMinute,
                                    false
                                ).show()
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("End", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(formatTime(endHour, endMinute), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                            }
                        }

                        // Duration Badge
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Duration", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(durationDisplay, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                        }
                    }
                }

                // Subject Selection
                item {
                    Text("Subject", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    var subjectDropdownExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { subjectDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = selectedSubject?.name ?: "Select Subject",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            DropdownMenu(
                                expanded = subjectDropdownExpanded,
                                onDismissRequest = { subjectDropdownExpanded = false },
                                modifier = Modifier.background(Slate850)
                            ) {
                                subjects.forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub.name, color = Color.White) },
                                        onClick = {
                                            selectedSubject = sub
                                            subjectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showNewSubjectDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Slate800)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Subject", tint = CyanPrimary)
                        }
                    }
                }

                // Task / Description
                item {
                    Text("Task / Description", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        placeholder = { Text("e.g. Complete Algebra Chapter 3 & solve 20 questions", color = Color(0xFF64748B), fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Recurrence Selector
                item {
                    Text("Recurrence", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ONE_TIME" to "Once", "WEEKLY" to "Weekly", "WEEKDAYS" to "Mon-Fri").forEach { (key, label) ->
                            val isSel = recurrence == key
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { recurrence = key },
                                color = if (isSel) CyanPrimary else Slate800,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Slate950 else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Focus Protection Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Focus Protection", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("Block social & entertainment apps during session", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = focusProtection,
                            onCheckedChange = { focusProtection = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Slate950, checkedTrackColor = CyanPrimary)
                        )
                    }
                }

                // Alarm Toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Study Alarm", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("Play ringtone at scheduled start time", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = alarmEnabled,
                            onCheckedChange = { alarmEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Slate950, checkedTrackColor = CyanPrimary)
                        )
                    }
                }

                // Custom Sound / Audio Picker
                if (alarmEnabled) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sound", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text(soundName, fontSize = 13.sp, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Select", fontSize = 12.sp, color = CyanPrimary)
                            }
                        }
                    }
                }

                // Personal Voice Reminder
                item {
                    Text("Personal Voice Reminder", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (voiceRecordings.isEmpty()) {
                        Text("No voice memos recorded yet. Record in Voice Studio.", fontSize = 12.sp, color = Color(0xFF64748B))
                    } else {
                        var voiceDropdownExpanded by remember { mutableStateOf(false) }
                        val activeVoice = voiceRecordings.find { it.id == selectedVoiceNoteId }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { voiceDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeVoice?.title ?: "None (Optional)",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                            DropdownMenu(
                                expanded = voiceDropdownExpanded,
                                onDismissRequest = { voiceDropdownExpanded = false },
                                modifier = Modifier.background(Slate850)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None", color = Color.White) },
                                    onClick = {
                                        selectedVoiceNoteId = null
                                        voiceDropdownExpanded = false
                                    }
                                )
                                voiceRecordings.forEach { note ->
                                    DropdownMenuItem(
                                        text = { Text(note.title, color = Color.White) },
                                        onClick = {
                                            selectedVoiceNoteId = note.id
                                            voiceDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sub = selectedSubject ?: return@Button
                    val finalTask = taskName.ifBlank { "Study ${sub.name}" }
                    val startStr = String.format("%02d:%02d", startHour, startMinute)
                    val endStr = String.format("%02d:%02d", endHour, endMinute)

                    val newSession = TimetableSessionEntity(
                        id = sessionToEdit?.id ?: 0L,
                        dayOfWeek = dayOfWeek,
                        subjectId = sub.id,
                        subjectName = sub.name,
                        taskName = finalTask,
                        startTime = startStr,
                        endTime = endStr,
                        durationMinutes = calculatedDurationMinutes,
                        colorHex = sub.colorHex,
                        focusModeEnabled = focusProtection,
                        alarmEnabled = alarmEnabled,
                        voiceNoteId = selectedVoiceNoteId,
                        soundUri = soundUri,
                        soundName = soundName,
                        recurrence = recurrence,
                        isEnabled = true
                    )
                    onSave(newSession)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_session_button")
            ) {
                Text("Save Session", color = Slate950, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )

    // Quick New Subject Dialog
    if (showNewSubjectDialog) {
        var newSubName by remember { mutableStateOf("") }
        val colors = listOf("#38BDF8", "#34D399", "#A78BFA", "#F43F5E", "#FBBF24", "#FB7185")
        var selectedColor by remember { mutableStateOf(colors.first()) }

        AlertDialog(
            onDismissRequest = { showNewSubjectDialog = false },
            containerColor = Slate850,
            title = { Text("New Subject", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSubName,
                        onValueChange = { newSubName = it },
                        placeholder = { Text("e.g. Mathematics, Machine Learning", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Color", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colors.forEach { hex ->
                            val isSel = selectedColor == hex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .clickable { selectedColor = hex }
                                    .border(
                                        width = if (isSel) 2.dp else 0.dp,
                                        color = if (isSel) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubName.isNotBlank()) {
                            onCreateSubject(newSubName.trim(), selectedColor)
                            showNewSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Add", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSubjectDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun DuplicateSessionDialog(
    session: TimetableSessionEntity,
    onDismiss: () -> Unit,
    onDuplicate: (Int) -> Unit
) {
    val days = listOf(
        Pair(1, "Monday"),
        Pair(2, "Tuesday"),
        Pair(3, "Wednesday"),
        Pair(4, "Thursday"),
        Pair(5, "Friday"),
        Pair(6, "Saturday"),
        Pair(7, "Sunday")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = { Text("Duplicate to Day", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Copy \"${session.subjectName}\" session to:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                days.filter { it.first != session.dayOfWeek }.forEach { (dayNum, dayName) ->
                    OutlinedButton(
                        onClick = { onDuplicate(dayNum) },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(dayName, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun AiTimetableGeneratorModal(
    isGenerating: Boolean,
    generatedPlan: GeneratedTimetablePlan?,
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit,
    onApply: () -> Unit
) {
    var promptInput by remember {
        mutableStateOf("I have college from 9 AM to 4 PM. I want to study 5 hours per day. I need Mathematics, Physics and Programming.")
    }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (generatedPlan == null) "Create Timetable with AI" else "Review Your Plan",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (generatedPlan == null) {
                    Text(
                        text = "Describe your daily routine, subjects, and study target:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    if (isGenerating) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Gemini is creating your timetable...", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Generated ${generatedPlan.sessions.size} study sessions. Review before applying to your schedule:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(generatedPlan.sessions) { s ->
                            val dayName = daysOfWeek.getOrNull(s.dayOfWeek - 1) ?: "Day ${s.dayOfWeek}"
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("$dayName • ${s.subjectName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(s.taskName, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1)
                                    }
                                    Text("${s.startTime} - ${s.endTime}", fontSize = 12.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (generatedPlan == null) {
                Button(
                    onClick = { onGenerate(promptInput) },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Generate Plan", color = Slate950, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("Apply Plan", color = Slate950, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

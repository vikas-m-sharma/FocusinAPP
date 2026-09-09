package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeneratedTimetablePlan
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TimetableSessionEntity
import com.example.data.local.entity.VoiceRecordingEntity
import com.example.receiver.AlarmRingingManager
import com.example.receiver.RingingSessionInfo
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeekDayData(
    val dayOfWeek: Int,
    val dayName: String,
    val dayOfMonth: Int,
    val fullFormattedDate: String,
    val isToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: FocusinViewModel,
    onStartSession: (TimetableSessionEntity) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val selectedDay by viewModel.selectedDay.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val voiceRecordings by viewModel.voiceRecordings.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val activeSessionState by viewModel.activeSessionState.collectAsState()
    val historyRecords by viewModel.historyRecords.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiGeneratedPlan by viewModel.aiGeneratedTimetable.collectAsState()

    val currentDayOfWeek = remember { FocusinViewModel.getCurrentDayOfWeek() }
    val currentTimeStr = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    // Dynamic current week days (Monday - Sunday)
    val weekDays = remember(currentDayOfWeek) {
        calculateCurrentWeekDays(currentDayOfWeek)
    }

    val selectedWeekDayData = weekDays.find { it.dayOfWeek == selectedDay } ?: weekDays.first()

    // Filter sessions for selected day, sorted chronologically by start time
    val daySessions = remember(allSessions, selectedDay) {
        allSessions.filter { it.dayOfWeek == selectedDay }.sortedBy { it.startTime }
    }

    // Daily progress calculations
    val totalDaySessions = daySessions.size
    val completedDaySessions = remember(daySessions, selectedDay, currentDayOfWeek, currentTimeStr, historyRecords) {
        if (selectedDay < currentDayOfWeek) {
            totalDaySessions
        } else if (selectedDay > currentDayOfWeek) {
            0
        } else {
            // For today: count sessions that have ended or have a completed record
            daySessions.count { s ->
                s.endTime <= currentTimeStr || historyRecords.any { r -> r.subjectId == s.subjectId && r.isCompleted }
            }
        }
    }

    val progressFraction = if (totalDaySessions > 0) (completedDaySessions.toFloat() / totalDaySessions).coerceIn(0f, 1f) else 0f
    val progressPercent = (progressFraction * 100).toInt()

    // Dialog states
    var showCreateDialog by remember { mutableStateOf(false) }
    var sessionToEdit by remember { mutableStateOf<TimetableSessionEntity?>(null) }
    var sessionToDuplicate by remember { mutableStateOf<TimetableSessionEntity?>(null) }
    var sessionToDelete by remember { mutableStateOf<TimetableSessionEntity?>(null) }
    var showAiGeneratorDialog by remember { mutableStateOf(false) }
    var showManageBlockedAppsDialog by remember { mutableStateOf(false) }

    // Blocked apps from UserSettings
    val blockedAppsList = remember(userSettings?.blockedAppsJson) {
        parseBlockedAppsList(userSettings?.blockedAppsJson)
    }

    // Accessibility Service Status Check
    val isAccessibilityEnabled = remember(context) {
        checkIsAccessibilityEnabled(context)
    }

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
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Plan your time. Stay consistent.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    // Reset to Today
                    IconButton(
                        onClick = { viewModel.selectDay(currentDayOfWeek) },
                        modifier = Modifier.testTag("schedule_today_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Jump to Today",
                            tint = if (selectedDay == currentDayOfWeek) CyanPrimary else Color(0xFF94A3B8)
                        )
                    }
                    // AI Generator
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
                    // Settings Shortcut
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
                modifier = Modifier
                    .padding(end = 4.dp, bottom = 4.dp)
                    .testTag("create_session_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Study Session",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // 1. HORIZONTAL WEEK SELECTOR
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(weekDays) { day ->
                        val isSelected = selectedDay == day.dayOfWeek
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.selectDay(day.dayOfWeek) }
                                .testTag("day_selector_${day.dayOfWeek}"),
                            color = if (isSelected) CyanPrimary else Slate900,
                            border = if (isSelected) null else BorderStroke(1.dp, Slate800),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = day.dayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Slate950 else Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${day.dayOfMonth}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Slate950 else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 2. DAILY SUMMARY SECTION
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (selectedWeekDayData.isToday) "TODAY" else selectedWeekDayData.dayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CyanPrimary
                                )
                                Text(
                                    text = selectedWeekDayData.fullFormattedDate,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = if (totalDaySessions == 0) "No sessions" else "$completedDaySessions / $totalDaySessions sessions",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Progress Bar & Percentage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = EmeraldSuccess,
                                trackColor = Slate800,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "$progressPercent%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }

            // 3. VERTICAL TIMELINE OF SESSIONS
            if (daySessions.isEmpty()) {
                item {
                    EmptyDayPlannerState(
                        onCreateSession = { showCreateDialog = true }
                    )
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TIMELINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                itemsIndexed(daySessions) { index, session ->
                    val isToday = selectedWeekDayData.isToday
                    val isNow = isToday && (
                        (activeSessionState.isActive && activeSessionState.subjectId == session.subjectId) ||
                        (!activeSessionState.isActive && currentTimeStr >= session.startTime && currentTimeStr < session.endTime)
                    )
                    val isPast = isToday && (currentTimeStr >= session.endTime)
                    val isCompleted = (selectedDay < currentDayOfWeek) || isPast ||
                        historyRecords.any { it.subjectId == session.subjectId && it.isCompleted }

                    TimelineSessionItem(
                        session = session,
                        isFirst = index == 0,
                        isLast = index == daySessions.size - 1,
                        isNow = isNow,
                        isCompleted = isCompleted,
                        onStart = { onStartSession(session) },
                        onEdit = { sessionToEdit = session },
                        onDuplicate = { sessionToDuplicate = session },
                        onDelete = { sessionToDelete = session },
                        onTestRinging = {
                            AlarmRingingManager.startRinging(
                                context,
                                RingingSessionInfo(
                                    sessionId = session.id,
                                    subjectId = session.subjectId,
                                    subjectName = session.subjectName,
                                    taskName = session.taskName,
                                    startTime = session.startTime,
                                    endTime = session.endTime,
                                    durationMinutes = session.durationMinutes,
                                    soundUri = session.soundUri
                                )
                            )
                        }
                    )
                }
            }

            // 4. FOCUS PROTECTION & ACCESSIBILITY APP BLOCKER CARD
            item {
                Spacer(modifier = Modifier.height(16.dp))
                FocusProtectionControlCard(
                    currentProtectionLevel = userSettings?.focusProtectionLevel ?: "STRICT",
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    onSelectLevel = { level ->
                        userSettings?.let {
                            viewModel.updateSettings(it.copy(focusProtectionLevel = level))
                        }
                    },
                    onOpenAccessibility = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        } catch (_: Exception) {}
                    },
                    onNavigateToSettings = onNavigateToSettings
                )
            }

            // 5. BLOCKED APPS PREVIEW CARD
            item {
                BlockedAppsPreviewCard(
                    blockedApps = blockedAppsList,
                    onManageApps = { showManageBlockedAppsDialog = true }
                )
            }
        }
    }

    // --- DIALOGS ---

    // Create / Edit Session Dialog
    if (showCreateDialog || sessionToEdit != null) {
        CreateOrEditSessionDialog(
            sessionToEdit = sessionToEdit,
            dayOfWeek = selectedDay,
            subjects = subjects,
            voiceRecordings = voiceRecordings,
            onDismiss = {
                showCreateDialog = false
                sessionToEdit = null
            },
            onSave = { session ->
                if (sessionToEdit != null) {
                    viewModel.updateSession(session)
                } else {
                    viewModel.addSession(session)
                }
                showCreateDialog = false
                sessionToEdit = null
            },
            onCreateSubject = { name, desc, colorHex ->
                viewModel.addSubject(name = name, description = desc, colorHex = colorHex)
            },
            onTestRinging = { preview ->
                AlarmRingingManager.startRinging(
                    context,
                    RingingSessionInfo(
                        sessionId = preview.id,
                        subjectId = preview.subjectId,
                        subjectName = preview.subjectName,
                        taskName = preview.taskName,
                        startTime = preview.startTime,
                        endTime = preview.endTime,
                        durationMinutes = preview.durationMinutes,
                        soundUri = preview.soundUri
                    )
                )
            }
        )
    }

    // Duplicate Session Dialog
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

    // Delete Session Confirm Dialog
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            containerColor = Slate900,
            title = {
                Text(
                    text = "Delete Study Session?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${session.subjectName}: ${session.taskName}\" from your schedule?",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSession(session)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // Manage Blocked Apps Dialog
    if (showManageBlockedAppsDialog) {
        ManageBlockedAppsModal(
            currentBlockedApps = blockedAppsList,
            onDismiss = { showManageBlockedAppsDialog = false },
            onSave = { updatedList ->
                userSettings?.let {
                    val json = updatedList.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
                    viewModel.updateSettings(it.copy(blockedAppsJson = json))
                }
                showManageBlockedAppsDialog = false
            },
            onNavigateToFullSettings = {
                showManageBlockedAppsDialog = false
                onNavigateToSettings()
            }
        )
    }

    // AI Timetable Generator Modal
    if (showAiGeneratorDialog) {
        AiTimetableGeneratorModal(
            isGenerating = isAiGenerating,
            generatedPlan = aiGeneratedPlan,
            onDismiss = {
                showAiGeneratorDialog = false
                viewModel.dismissAiTimetable()
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

// --------------------------------------------------------------------------------
// TIMELINE COMPONENT
// --------------------------------------------------------------------------------

@Composable
fun TimelineSessionItem(
    session: TimetableSessionEntity,
    isFirst: Boolean,
    isLast: Boolean,
    isNow: Boolean,
    isCompleted: Boolean,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onTestRinging: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val subjectColor = remember(session.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(session.colorHex))
        } catch (_: Exception) {
            CyanPrimary
        }
    }

    val iconVector = remember(session.subjectName) {
        getSubjectIcon(session.subjectName)
    }

    val sessionProtection = remember(session.focusModeEnabled, session.note) {
        if (!session.focusModeEnabled || session.note.equals("OFF", ignoreCase = true)) {
            "Off"
        } else if (session.note.isNotBlank() && session.note in listOf("STANDARD", "ENHANCED", "STRICT")) {
            session.note.lowercase().replaceFirstChar { it.uppercase() }
        } else {
            "Strict"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Time & Vertical Timeline Track
        Column(
            modifier = Modifier.width(68.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = session.startTime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isNow) CyanPrimary else Color.White
            )
            Text(
                text = "– ${session.endTime}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Timeline dot and connecting line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> EmeraldSuccess
                                isNow -> CyanPrimary
                                else -> Slate700
                            }
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .weight(1f)
                        .background(Slate800)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Column: Session Card
        Card(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isCompleted) Slate900.copy(alpha = 0.75f) else Slate900
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = if (isNow) 1.5.dp else 1.dp,
                color = if (isNow) CyanPrimary else Slate800
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: Subject Icon, Subject Name, Status Indicator, Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Subject Icon Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(subjectColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = session.subjectName,
                            tint = subjectColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = session.subjectName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // NOW Pill
                            if (isNow) {
                                Surface(
                                    color = CyanPrimary,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "NOW",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Slate950,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = session.taskName,
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status Indicator / Completed Check
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    } else if (!isNow) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Upcoming",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // More Menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Slate850)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Start Focus", color = CyanPrimary, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyanPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    onStart()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Test Ringing Alarm", color = Color(0xFFF87171)) },
                                leadingIcon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFF87171)) },
                                onClick = {
                                    menuExpanded = false
                                    onTestRinging()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Session", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplicate Session", color = Color.White) },
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

                // Badges Row (Focus Protection & Reminder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Lock / Focus Protection Badge
                    Surface(
                        color = if (sessionProtection == "Off") Slate850 else Slate800,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (sessionProtection == "Off") Icons.Default.Lock else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (sessionProtection == "Off") Color(0xFF64748B) else EmeraldSuccess,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "App Lock: $sessionProtection",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (sessionProtection == "Off") Color(0xFF64748B) else Color(0xFFE2E8F0)
                            )
                        }
                    }

                    // Reminder Alarm Badge
                    if (session.alarmEnabled) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Slate800)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reminder On",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }

                // Action Button: If active NOW or next to study
                if (isNow) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Continue Focus",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate950
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------
// EMPTY STATE
// --------------------------------------------------------------------------------

@Composable
fun EmptyDayPlannerState(
    onCreateSession: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your day is still open.",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Create a focus session to structure your study time and shield distracting apps.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCreateSession,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Slate950,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Create Session",
                    color = Slate950,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// --------------------------------------------------------------------------------
// FOCUS PROTECTION & BLOCKED APPS BOTTOM CARDS
// --------------------------------------------------------------------------------

@Composable
fun FocusProtectionControlCard(
    currentProtectionLevel: String,
    isAccessibilityEnabled: Boolean,
    onSelectLevel: (String) -> Unit,
    onOpenAccessibility: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(EmeraldSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Focus Protection",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Protection",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "During study sessions, distracting apps will be blocked automatically.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Protection Settings",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Segmented Level Choices: STANDARD, ENHANCED, STRICT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("STANDARD", "ENHANCED", "STRICT").forEach { level ->
                    val isSelected = currentProtectionLevel.equals(level, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectLevel(level) },
                        color = if (isSelected) CyanPrimary else Slate800,
                        border = if (isSelected) null else BorderStroke(1.dp, Slate700)
                    ) {
                        Text(
                            text = level.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Slate950 else Color(0xFFE2E8F0),
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Accessibility Status Warning if not enabled
            if (!isAccessibilityEnabled) {
                Surface(
                    color = Slate850,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Focus Protection requires Accessibility permission to shield apps.",
                                fontSize = 11.5.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onOpenAccessibility,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Enable", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedAppsPreviewCard(
    blockedApps: List<String>,
    onManageApps: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onManageApps() },
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate800)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AmethystAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Blocked Apps",
                        tint = AmethystAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Blocked Apps (${blockedApps.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Apps locked during active study sessions",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Manage Apps",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Horizontal Chips of Apps
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(blockedApps) { appName ->
                    val badgeColor = getAppBadgeColor(appName)
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Slate700)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = appName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------------
// CREATE OR EDIT SESSION DIALOG
// --------------------------------------------------------------------------------

@Composable
fun CreateOrEditSessionDialog(
    sessionToEdit: TimetableSessionEntity?,
    dayOfWeek: Int,
    subjects: List<SubjectEntity>,
    voiceRecordings: List<VoiceRecordingEntity>,
    onDismiss: () -> Unit,
    onSave: (TimetableSessionEntity) -> Unit,
    onCreateSubject: (String, String, String) -> Unit,
    onTestRinging: (TimetableSessionEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedSubject by remember {
        mutableStateOf(
            if (sessionToEdit != null) subjects.find { it.id == sessionToEdit.subjectId } ?: subjects.firstOrNull()
            else subjects.firstOrNull()
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

    // Individual Focus Protection for this session: OFF, STANDARD, ENHANCED, STRICT
    var protectionOption by remember {
        mutableStateOf(
            if (sessionToEdit?.focusModeEnabled == false || sessionToEdit?.note.equals("OFF", ignoreCase = true)) {
                "OFF"
            } else if (sessionToEdit?.note != null && sessionToEdit.note in listOf("STANDARD", "ENHANCED", "STRICT")) {
                sessionToEdit.note
            } else {
                "STRICT"
            }
        )
    }

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
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
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
                            border = BorderStroke(1.dp, Slate700),
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
                            border = BorderStroke(1.dp, Slate700),
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
                                border = BorderStroke(1.dp, Slate700),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = selectedSubject?.name ?: "Select Subject",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (!selectedSubject?.description.isNullOrBlank()) {
                                        Text(
                                            text = selectedSubject!!.description,
                                            color = CyanPrimary,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = subjectDropdownExpanded,
                                onDismissRequest = { subjectDropdownExpanded = false },
                                modifier = Modifier.background(Slate850)
                            ) {
                                subjects.forEach { sub ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(sub.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                                if (sub.description.isNotBlank()) {
                                                    Text(sub.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                                }
                                            }
                                        },
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
                    Text("Topic / Task Description", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        placeholder = { Text("e.g. Current Electricity theory & solve 25 MCQs", color = Color(0xFF64748B), fontSize = 13.sp) },
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
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // INDIVIDUAL FOCUS PROTECTION TOGGLE (OFF, STANDARD, ENHANCED, STRICT)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Focus Protection", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = "During this session, selected distracting apps will be blocked.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("OFF", "STANDARD", "ENHANCED", "STRICT").forEach { option ->
                                val isSel = protectionOption == option
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { protectionOption = option },
                                    color = if (isSel) CyanPrimary else Slate800,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = option.lowercase().replaceFirstChar { it.uppercase() },
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Slate950 else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Protection Note
                        Surface(
                            color = Slate850,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (protectionOption == "OFF") Icons.Default.Lock else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (protectionOption == "OFF") Color(0xFF64748B) else EmeraldSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (protectionOption) {
                                        "STRICT" -> "🔒 Strict Protection: Selected distracting apps will be blocked during this session."
                                        "ENHANCED" -> "🛡️ Enhanced Protection: Blocks distracting apps with session reminders."
                                        "STANDARD" -> "🛡️ Standard Protection: Soft shields against phone interruptions."
                                        else -> "Protection disabled: No apps will be blocked during this session."
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
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
                            Text("Study Alarm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Play ringing alert at scheduled start time", fontSize = 11.sp, color = Color(0xFF94A3B8))
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
                                border = BorderStroke(1.dp, Slate700)
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
                        Text("No voice memos recorded yet. Record in Settings / Voice Studio.", fontSize = 11.5.sp, color = Color(0xFF64748B))
                    } else {
                        var voiceDropdownExpanded by remember { mutableStateOf(false) }
                        val activeVoice = voiceRecordings.find { it.id == selectedVoiceNoteId }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { voiceDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Slate700),
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
                        note = protectionOption,
                        focusModeEnabled = (protectionOption != "OFF"),
                        alarmEnabled = alarmEnabled,
                        voiceNoteId = selectedVoiceNoteId,
                        soundUri = soundUri,
                        soundName = soundName,
                        recurrenceType = recurrence,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val sub = selectedSubject ?: return@OutlinedButton
                        val startStr = String.format("%02d:%02d", startHour, startMinute)
                        val endStr = String.format("%02d:%02d", endHour, endMinute)
                        val preview = TimetableSessionEntity(
                            id = sessionToEdit?.id ?: 0L,
                            dayOfWeek = dayOfWeek,
                            subjectId = sub.id,
                            subjectName = sub.name,
                            taskName = taskName.ifBlank { "Study ${sub.name}" },
                            startTime = startStr,
                            endTime = endStr,
                            durationMinutes = calculatedDurationMinutes,
                            soundUri = soundUri
                        )
                        onTestRinging(preview)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Ringing", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        }
    )

    // Quick New Subject Dialog
    if (showNewSubjectDialog) {
        var newSubName by remember { mutableStateOf("") }
        var newSubDesc by remember { mutableStateOf("") }
        val colors = listOf("#38BDF8", "#34D399", "#A78BFA", "#F43F5E", "#FBBF24", "#FB7185")
        var selectedColor by remember { mutableStateOf(colors.first()) }

        AlertDialog(
            onDismissRequest = { showNewSubjectDialog = false },
            containerColor = Slate850,
            title = { Text("New Subject", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newSubName,
                        onValueChange = { newSubName = it },
                        placeholder = { Text("Subject Name (e.g. Mathematics)", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSubDesc,
                        onValueChange = { newSubDesc = it },
                        placeholder = { Text("Description (e.g. Calculus, Problem sets)", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Select Color", fontSize = 12.sp, color = Color(0xFF94A3B8))
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
                            onCreateSubject(newSubName.trim(), newSubDesc.trim(), selectedColor)
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

// --------------------------------------------------------------------------------
// MANAGE BLOCKED APPS MODAL
// --------------------------------------------------------------------------------

@Composable
fun ManageBlockedAppsModal(
    currentBlockedApps: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
    onNavigateToFullSettings: () -> Unit
) {
    val appsList = remember { currentBlockedApps.toMutableList() }
    var newAppName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage Blocked Apps",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "These apps will be locked whenever a study session with Focus Protection is running:",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                // Input to add a new app
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newAppName,
                        onValueChange = { newAppName = it },
                        placeholder = { Text("Add app (e.g. Netflix)", color = Color(0xFF64748B), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            if (newAppName.isNotBlank() && !appsList.contains(newAppName.trim())) {
                                appsList.add(newAppName.trim())
                                newAppName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Chips of current blocked apps
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(appsList.toList()) { app ->
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Slate700)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(getAppBadgeColor(app))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(app, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                IconButton(
                                    onClick = { appsList.remove(app) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove $app",
                                        tint = RoseError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onNavigateToFullSettings,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Configure in Full Settings →", color = CyanPrimary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(appsList) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Apps", color = Slate950, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

// --------------------------------------------------------------------------------
// DUPLICATE SESSION DIALOG
// --------------------------------------------------------------------------------

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
                        border = BorderStroke(1.dp, Slate700),
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

// --------------------------------------------------------------------------------
// AI TIMETABLE GENERATOR MODAL
// --------------------------------------------------------------------------------

@Composable
fun AiTimetableGeneratorModal(
    isGenerating: Boolean,
    generatedPlan: GeneratedTimetablePlan?,
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit,
    onApply: () -> Unit
) {
    var promptInput by remember {
        mutableStateOf("I want to study 5 hours per day for NEET. Include Physics, Chemistry, Biology and Mock Tests.")
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

// --------------------------------------------------------------------------------
// UTILITIES & HELPERS
// --------------------------------------------------------------------------------

fun calculateCurrentWeekDays(todayDayOfWeek: Int): List<WeekDayData> {
    val dayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    val fullDateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())

    val baseCal = Calendar.getInstance().apply {
        // Find Monday of current week
        val daysFromMonday = todayDayOfWeek - 1
        add(Calendar.DAY_OF_YEAR, -daysFromMonday)
    }

    return (1..7).map { dayNum ->
        val cal = (baseCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, dayNum - 1)
        }
        WeekDayData(
            dayOfWeek = dayNum,
            dayName = dayNames[dayNum - 1],
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
            fullFormattedDate = fullDateFormat.format(cal.time),
            isToday = dayNum == todayDayOfWeek
        )
    }
}

fun getSubjectIcon(subjectName: String): ImageVector {
    val lower = subjectName.lowercase()
    return when {
        lower.contains("bio") -> Icons.Default.Eco
        lower.contains("chem") -> Icons.Default.Science
        lower.contains("phy") -> Icons.Default.Bolt
        lower.contains("math") -> Icons.Default.Calculate
        lower.contains("mock") || lower.contains("test") -> Icons.Default.Assignment
        lower.contains("rev") || lower.contains("note") -> Icons.Default.EditNote
        lower.contains("read") || lower.contains("ncert") || lower.contains("book") -> Icons.Default.MenuBook
        else -> Icons.Default.School
    }
}

fun getAppBadgeColor(appName: String): Color {
    val lower = appName.lowercase()
    return when {
        lower.contains("insta") -> Color(0xFFE1306C)
        lower.contains("you") -> Color(0xFFFF0000)
        lower.contains("tik") -> Color(0xFF00F2FE)
        lower.contains("twit") || lower == "x" -> Color(0xFF38BDF8)
        lower.contains("face") -> Color(0xFF1877F2)
        lower.contains("chrom") -> Color(0xFF34D399)
        lower.contains("netf") -> Color(0xFFE50914)
        else -> Color(0xFFA78BFA)
    }
}

fun parseBlockedAppsList(jsonStr: String?): List<String> {
    if (jsonStr.isNullOrBlank()) {
        return listOf("Instagram", "YouTube", "TikTok", "X", "Chrome")
    }
    return try {
        val raw = jsonStr.trim().removeSurrounding("[", "]")
        if (raw.isBlank()) listOf("Instagram", "YouTube", "TikTok", "X", "Chrome")
        else raw.split(",").map { it.trim().removeSurrounding("\"") }.filter { it.isNotBlank() }
    } catch (_: Exception) {
        listOf("Instagram", "YouTube", "TikTok", "X", "Chrome")
    }
}

fun checkIsAccessibilityEnabled(context: Context): Boolean {
    return try {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        enabledServices.contains(context.packageName)
    } catch (_: Exception) {
        false
    }
}

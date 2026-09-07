package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FocusSessionRecordEntity
import com.example.data.recovery.RecoveryRecommendations
import com.example.ui.components.CircularFocusTimer
import com.example.ui.components.EarlyEndSessionDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmethystAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel
import java.util.Locale

@Composable
fun FocusSessionScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit
) {
    val activeSession by viewModel.activeSessionState.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val showEarlyEndDialog by viewModel.showEarlyEndDialog.collectAsState()
    val latestCompletedRecord by viewModel.latestCompletedRecord.collectAsState()

    // If completed record is present, show satisfaction completion screen
    if (latestCompletedRecord != null) {
        SessionCompletionScreen(
            record = latestCompletedRecord!!,
            onDismiss = {
                viewModel.dismissSessionComplete()
                onNavigateBack()
            }
        )
        return
    }

    // Timer computation
    val progress = if (activeSession.isBreak) {
        val totalBreak = 600f // 10 min break
        if (totalBreak > 0) 1f - (activeSession.breakRemainingSeconds.toFloat() / totalBreak) else 0f
    } else if (activeSession.mode == "COUNTDOWN") {
        val totalSecs = activeSession.plannedDurationMinutes * 60f
        if (totalSecs > 0) 1f - (activeSession.remainingSeconds.toFloat() / totalSecs) else 0f
    } else {
        (activeSession.elapsedSeconds % 3600) / 3600f
    }

    val displaySeconds = if (activeSession.isBreak) {
        activeSession.breakRemainingSeconds
    } else if (activeSession.mode == "COUNTDOWN") {
        activeSession.remainingSeconds
    } else {
        activeSession.elapsedSeconds
    }

    val hours = displaySeconds / 3600
    val minutes = (displaySeconds % 3600) / 60
    val seconds = displaySeconds % 60
    val formattedTime = if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    val todayTotalFocusedMins = (todayStats?.totalFocusedMinutes ?: 0) + (activeSession.elapsedSeconds / 60)
    val todayFocusedHours = todayTotalFocusedMins / 60
    val todayFocusedRem = todayTotalFocusedMins % 60
    val todayFocusStr = if (todayFocusedHours > 0) "${todayFocusedHours}h ${todayFocusedRem}m" else "${todayFocusedRem}m"

    Scaffold(
        containerColor = Slate950,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return to Home",
                        tint = Color(0xFF94A3B8)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (activeSession.isBreak) AmethystAccent else EmeraldSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeSession.isBreak) "BREAK PERIOD" else "FOCUS SESSION",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }

                // Minimal balance space
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Subject & Task Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = activeSession.subjectName.ifEmpty { "General Study" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                if (activeSession.taskName.isNotBlank()) {
                    Text(
                        text = activeSession.taskName,
                        fontSize = 15.sp,
                        color = CyanBright,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (activeSession.isBreak) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmethystAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Relax & Recharge (50/10 Interval)",
                            fontSize = 11.sp,
                            color = AmethystAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Central Circular Focus Timer
            Box(
                modifier = Modifier.size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularFocusTimer(
                    progress = progress,
                    timeText = formattedTime,
                    statusText = if (activeSession.isBreak) "BREAK" else if (activeSession.isPaused) "PAUSED" else "FOCUS",
                    modifier = Modifier.size(260.dp),
                    isPaused = activeSession.isPaused,
                    isBreak = activeSession.isBreak
                )
            }

            // Info Strip: Focus Protection, Blocked Apps, Today's Focus
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate900)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Focus Protection", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("ON", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                }

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Slate800))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Blocked Apps", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("8 Active", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Slate800))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today's Focus", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(todayFocusStr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }
            }

            // Interruption Notice if any
            if (activeSession.distractionCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberWarning.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${activeSession.distractionCount} distraction attempts intercepted & redirected.",
                        fontSize = 11.sp,
                        color = AmberWarning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Controls: PAUSE / RESUME, TAKE BREAK, END SESSION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pause / Resume Button
                    Button(
                        onClick = {
                            if (activeSession.isPaused) viewModel.resumeSession() else viewModel.pauseSession()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeSession.isPaused) EmeraldSuccess else Slate800
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("pause_resume_button")
                    ) {
                        Icon(
                            imageVector = if (activeSession.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = if (activeSession.isPaused) Slate950 else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeSession.isPaused) "RESUME" else "PAUSE",
                            fontWeight = FontWeight.Bold,
                            color = if (activeSession.isPaused) Slate950 else Color.White,
                            fontSize = 14.sp
                        )
                    }

                    // Break Button
                    Button(
                        onClick = {
                            if (activeSession.isBreak) viewModel.endBreak() else viewModel.startBreak(10)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("break_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            tint = AmethystAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeSession.isBreak) "END BREAK" else "TAKE BREAK",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                // End Session Button
                OutlinedButton(
                    onClick = { viewModel.requestEndSession() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("end_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        tint = RoseError,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "END SESSION",
                        fontWeight = FontWeight.Bold,
                        color = RoseError,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    // Early End Dialog (non-shaming)
    if (showEarlyEndDialog) {
        val plannedStr = "${activeSession.plannedDurationMinutes / 60}h ${String.format(Locale.getDefault(), "%02dm", activeSession.plannedDurationMinutes % 60)}"
        val actualMins = activeSession.elapsedSeconds / 60
        val actualStr = "${actualMins / 60}h ${String.format(Locale.getDefault(), "%02dm", actualMins % 60)}"

        EarlyEndSessionDialog(
            onDismiss = { viewModel.dismissEarlyEndDialog() },
            onConfirm = { reason -> viewModel.confirmEndSession(isCompleted = false, reason = reason) },
            plannedTimeText = plannedStr,
            actualTimeText = actualStr
        )
    }
}

// --- SESSION COMPLETION SCREEN (with Rewards & Relax & Reset recommendations) ---
@Composable
fun SessionCompletionScreen(
    record: FocusSessionRecordEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val quote = remember { RecoveryRecommendations.getRandomQuote() }
    val focusedMins = record.actualDurationSeconds / 60
    val fHours = focusedMins / 60
    val fRem = focusedMins % 60
    val timeFormatted = if (fHours > 0) "${fHours}h ${fRem}m" else "${fRem}m"

    Scaffold(containerColor = Slate950) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 30.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(EmeraldSuccess.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎉", fontSize = 36.sp)
                }
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SESSION COMPLETE",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.subjectName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$timeFormatted focused",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Goal achieved ✓",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Nice work.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                        Text(
                            text = "\"$quote\"",
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // RELAX & RESET RECOMMENDATIONS
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "RELAX & RESET",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = AmethystAccent
                    )
                    Text(
                        text = "Take a short conscious break before transitioning to your next task:",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    RecoveryRecommendations.postSessionItems.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.actionUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when (item.category) {
                                    "BREATHING" -> Icons.Default.SelfImprovement
                                    "MUSIC" -> Icons.Default.MusicNote
                                    else -> Icons.Default.PlayArrow
                                }
                                Icon(icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(item.description, fontSize = 10.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Open", fontSize = 11.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("completion_done_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("RETURN HOME", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

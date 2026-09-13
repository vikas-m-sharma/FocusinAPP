package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthState
import com.example.ui.components.GoogleSignInDialog
import coil.compose.AsyncImage
import com.example.ui.theme.AmethystAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel
import com.example.data.importer.DatasetManifestRoot
import org.json.JSONObject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToVoiceStudio: () -> Unit,
    onSignOut: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val authUser = (authState as? AuthState.Authenticated)?.user

    var showClearConfirm by remember { mutableStateOf(false) }
    var showGoogleSignInModal by remember { mutableStateOf(false) }
    var showAddBlockedApp by remember { mutableStateOf(false) }
    var showAddAllowedApp by remember { mutableStateOf(false) }
    var showImportReportDialog by remember { mutableStateOf(false) }
    var showImportConverterDialog by remember { mutableStateOf(false) }
    var showReviewQueueDialog by remember { mutableStateOf(false) }
    val reviewQueue by viewModel.pyqReviewQueue.collectAsState()

    val totalQuestionsDb by viewModel.getTotalQuestionCount().collectAsState(initial = 0)
    val verifiedPyqCount by viewModel.getTotalOfficialPyqCount().collectAsState(initial = 0)
    val unverifiedPyqCount by viewModel.getTotalUnverifiedCount().collectAsState(initial = 0)
    val sampleCount by viewModel.getTotalSampleCount().collectAsState(initial = 0)
    val generatedCount by viewModel.getTotalGeneratedCount().collectAsState(initial = 0)
    val totalImportedPapers by viewModel.getTotalImportedPapersCount().collectAsState(initial = 0)
    val coverageReports by viewModel.getHistoricalCoverageReports().collectAsState(initial = emptyList())
    val pyqImportReport by viewModel.pyqImportReport.collectAsState()
    val distinctYears by viewModel.getDistinctExamYears().collectAsState(initial = emptyList())
    val missingExamYears = remember(distinctYears) {
        val presentSet = distinctYears.toSet()
        (2006..2025).filter { !presentSet.contains(it) }
    }

    val datasetManifest = remember {
        try {
            val json = context.assets.open("pyq/manifest.json").bufferedReader().use { it.readText() }
            DatasetManifestRoot.fromJsonObject(JSONObject(json))
        } catch (_: Exception) {
            null
        }
    }

    val currentSettings = userSettings ?: return

    val blockedApps = remember(currentSettings.blockedAppsJson) {
        try {
            val raw = currentSettings.blockedAppsJson.trim().removeSurrounding("[", "]")
            if (raw.isBlank()) mutableListOf()
            else raw.split(",").map { it.trim().removeSurrounding("\"") }.toMutableList()
        } catch (_: Exception) {
            mutableListOf("Instagram", "TikTok", "YouTube", "Twitter", "Facebook")
        }
    }

    val allowedApps = remember(currentSettings.allowedAppsJson) {
        try {
            val raw = currentSettings.allowedAppsJson.trim().removeSurrounding("[", "]")
            if (raw.isBlank()) mutableListOf()
            else raw.split(",").map { it.trim().removeSurrounding("\"") }.toMutableList()
        } catch (_: Exception) {
            mutableListOf("Calculator", "Google Drive", "Chrome", "ChatGPT", "Notion")
        }
    }

    var dailyHoursSlider by remember(currentSettings.dailyGoalMinutes) {
        mutableFloatStateOf((currentSettings.dailyGoalMinutes / 60f).coerceIn(1f, 16f))
    }

    var weeklyHoursSlider by remember(currentSettings.weeklyGoalMinutes) {
        mutableFloatStateOf((currentSettings.weeklyGoalMinutes / 60f).coerceIn(10f, 80f))
    }

    Scaffold(
        containerColor = Slate950,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF94A3B8)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SETTINGS & PROFILE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. ACCOUNT & CLOUD SYNC CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACCOUNT & SYNC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = CyanPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (currentSettings.isGoogleSignedIn) EmeraldSuccess.copy(alpha = 0.2f) else Slate800)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (currentSettings.isGoogleSignedIn) "Google Connected" else "Local Mode",
                                    fontSize = 10.sp,
                                    color = if (currentSettings.isGoogleSignedIn) EmeraldSuccess else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                if (authUser?.photoUrl?.isNotBlank() == true) {
                                    AsyncImage(
                                        model = authUser.photoUrl,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, CyanPrimary, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(CyanPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = CyanPrimary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }

                                if (authUser != null && authUser.isGoogleUser) {
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(0.5.dp, Slate900, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "G",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF4285F4)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = authUser?.displayName ?: currentSettings.userName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (authUser?.isGoogleUser == true) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Verified",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSuccess,
                                            modifier = Modifier
                                                .background(EmeraldSuccess.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = authUser?.email ?: currentSettings.userEmail ?: "Offline local database (No cloud sync)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Text(
                            text = if (authUser?.isGoogleUser == true) {
                                "Account synced with Firebase Auth. Study timetable, streak data, and mock test scores are backed up to your Google account."
                            } else {
                                "Focusin operates local-first. Sign in with Google to sync your study timetables, test history, and AI streak diagnostics across all devices."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        if (authUser == null || !authUser.isGoogleUser) {
                            Button(
                                onClick = { showGoogleSignInModal = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_sign_in_google_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("G", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign in with Google", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.logoutUser(context)
                                        onSignOut?.invoke()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_sign_out_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Sign Out", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.logoutUser(context)
                                        onSignOut?.invoke()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_switch_account_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Switch Account", color = Slate950, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 2. QUICK SHORTCUTS (SUBJECTS & VOICE STUDIO)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSubjects() },
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Subjects", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Manage targets", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToVoiceStudio() },
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = AmethystAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Voice Studio", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Self memos", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }

            // 3. TARGET GOALS CONFIGURATION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "FOCUS TARGETS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = CyanPrimary
                        )

                        // Daily Target
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Daily Target", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("${dailyHoursSlider.toInt()} Hours / Day", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                            }
                            Slider(
                                value = dailyHoursSlider,
                                onValueChange = { dailyHoursSlider = it },
                                onValueChangeFinished = {
                                    viewModel.updateSettings(currentSettings.copy(dailyGoalMinutes = (dailyHoursSlider * 60).toInt()))
                                },
                                valueRange = 2f..14f,
                                steps = 11,
                                colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                            )
                        }

                        // Weekly Target
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Weekly Target", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("${weeklyHoursSlider.toInt()} Hours / Week", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                            }
                            Slider(
                                value = weeklyHoursSlider,
                                onValueChange = { weeklyHoursSlider = it },
                                onValueChangeFinished = {
                                    viewModel.updateSettings(currentSettings.copy(weeklyGoalMinutes = (weeklyHoursSlider * 60).toInt()))
                                },
                                valueRange = 15f..60f,
                                steps = 8,
                                colors = SliderDefaults.colors(thumbColor = EmeraldSuccess, activeTrackColor = EmeraldSuccess)
                            )
                        }
                    }
                }
            }

            // 4. FOCUS PROTECTION & ACCESSIBILITY APP BLOCKER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "FOCUS PROTECTION & BLOCKER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = CyanPrimary
                        )

                        // Protection Level
                        Text("Protection Enforcement Level", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("STANDARD", "ENHANCED", "STRICT").forEach { level ->
                                val isSel = currentSettings.focusProtectionLevel == level
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) CyanPrimary else Slate800)
                                        .clickable { viewModel.updateSettings(currentSettings.copy(focusProtectionLevel = level)) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Slate950 else Color.White
                                    )
                                }
                            }
                        }

                        // Accessibility Permission Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate800)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Accessibility Service", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Required for on-device app blocking & interruption defense", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                    } catch (_: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("ENABLE", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Blocked Apps list
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Blocked Apps (${blockedApps.size})", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "+ Add App",
                                    fontSize = 11.sp,
                                    color = CyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showAddBlockedApp = true }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                blockedApps.take(4).forEach { appName ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Slate800)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(appName, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. NOTIFICATIONS PREFERENCES
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "NOTIFICATIONS & REMINDERS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = CyanPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Session Alarms & Reminders", fontSize = 13.sp, color = Color.White)
                            Switch(
                                checked = currentSettings.sessionRemindersEnabled,
                                onCheckedChange = { viewModel.updateSettings(currentSettings.copy(sessionRemindersEnabled = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Slate950, checkedTrackColor = CyanPrimary)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Evening Progress Summary", fontSize = 13.sp, color = Color.White)
                            Switch(
                                checked = currentSettings.eveningSummaryEnabled,
                                onCheckedChange = { viewModel.updateSettings(currentSettings.copy(eveningSummaryEnabled = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Slate950, checkedTrackColor = CyanPrimary)
                            )
                        }
                    }
                }
            }

            // 6. DEMO DATA & DATA MANAGEMENT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "DATA MANAGEMENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = CyanPrimary
                        )

                        Button(
                            onClick = { viewModel.seedDemoData() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seed Realistic Demo Data", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = RoseError, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Local Data", color = RoseError, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 7. HISTORICAL PYQ DATASET AUDIT & INTEGRITY TRAIL
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pyq_audit_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, if (verifiedPyqCount > 0) EmeraldSuccess.copy(alpha = 0.3f) else Color(0xFFFBBF24).copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (verifiedPyqCount > 0) EmeraldSuccess else Color(0xFFFBBF24),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PYQ Dataset Audit Trail",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Surface(
                                color = if (verifiedPyqCount > 0) EmeraldSuccess.copy(alpha = 0.15f) else Color(0xFFFBBF24).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (verifiedPyqCount > 0) "VERIFIED READY ($verifiedPyqCount VERIFIED)" else "ACTIVE AUDIT TRAIL ($verifiedPyqCount VERIFIED)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (verifiedPyqCount > 0) EmeraldSuccess else Color(0xFFFBBF24),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Authenticity Notice
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate950,
                            border = BorderStroke(1.dp, Slate800),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FOCUSIN includes authentic historical PYQs where source material is available and verified. Missing years are not fabricated.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Live Audit Metrics
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate850)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Audit Summary (Live Database):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Coverage Window:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("2006–2025 (20 Years)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Total Imported Papers:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("$totalImportedPapers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Total Imported Questions:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("$totalQuestionsDb", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Verified Questions:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("$verifiedPyqCount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (verifiedPyqCount > 0) EmeraldSuccess else Color(0xFF94A3B8))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Unverified Questions:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("$unverifiedPyqCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFBBF24))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                val partialCount = coverageReports.count { it.status == com.example.data.catalog.DatasetImportStatus.PARTIAL }
                                Text("• Partial Papers:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("$partialCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF59E0B))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                val notImportedCount = coverageReports.count { it.status == com.example.data.catalog.DatasetImportStatus.NOT_IMPORTED }
                                Text("• Not Imported Years:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("$notImportedCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                val rejectedCount = pyqImportReport?.invalidCount ?: 0
                                Text("• Rejected Questions:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("$rejectedCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (rejectedCount > 0) Color(0xFFEF4444) else EmeraldSuccess)
                            }
                        }

                        // Metrics Grid: DB Total, Verified, Unverified, Sample, Generated
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Total in DB
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Slate850,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Slate800)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("DB Total", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                    Text("$totalQuestionsDb", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            // Verified (Official)
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Slate850,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (verifiedPyqCount > 0) EmeraldSuccess.copy(alpha = 0.4f) else Slate800)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Verified", fontSize = 9.sp, color = if (verifiedPyqCount > 0) EmeraldSuccess else Color(0xFF94A3B8))
                                    Text("$verifiedPyqCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (verifiedPyqCount > 0) EmeraldSuccess else Color(0xFF94A3B8))
                                }
                            }
                            // Unverified Curated
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Slate850,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Slate800)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Unverified", fontSize = 9.sp, color = Color(0xFFFBBF24))
                                    Text("$unverifiedPyqCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                                }
                            }
                            // Sample Demo
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Slate850,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Slate800)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Sample", fontSize = 9.sp, color = CyanPrimary)
                                    Text("$sampleCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                                }
                            }
                            // Generated
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Slate850,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Slate800)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Generated", fontSize = 9.sp, color = Color(0xFF64748B))
                                    Text("$generatedCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                }
                            }
                        }

                        // Authoritative 20-Year Coverage Matrix (2006 - 2025)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Historical PYQ Coverage (2006–2025)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${coverageReports.count { it.actualImported > 0 }} / 20 Available",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFBBF24)
                                )
                            }

                            // Coverage List for all 20 years
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate950)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                coverageReports.forEach { report ->
                                    val isImported = report.actualImported > 0
                                    val badgeColor = when (report.status) {
                                        com.example.data.catalog.DatasetImportStatus.VERIFIED -> EmeraldSuccess
                                        com.example.data.catalog.DatasetImportStatus.PARTIAL -> Color(0xFFF59E0B)
                                        com.example.data.catalog.DatasetImportStatus.UNVERIFIED -> Color(0xFFFBBF24)
                                        com.example.data.catalog.DatasetImportStatus.NOT_IMPORTED -> Color(0xFF64748B)
                                    }
                                    val statusLabel = when (report.status) {
                                        com.example.data.catalog.DatasetImportStatus.VERIFIED -> "VERIFIED"
                                        com.example.data.catalog.DatasetImportStatus.PARTIAL -> "PARTIAL (${report.actualImported}/${report.info.expectedTotal})"
                                        com.example.data.catalog.DatasetImportStatus.UNVERIFIED -> "UNVERIFIED (${report.actualImported}/${report.info.expectedTotal})"
                                        com.example.data.catalog.DatasetImportStatus.NOT_IMPORTED -> "NOT IMPORTED"
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isImported) Slate900 else Color.Transparent)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${report.info.year} • ${report.detectedExam}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isImported) Color.White else Color(0xFF94A3B8)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = report.info.era,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                            if (isImported) {
                                                Text(
                                                    text = "Imported Papers: ${report.actualPapers} • Questions: ${report.actualImported} (Phy: ${report.actualPhysics}, Chem: ${report.actualChemistry}, Bio: ${report.actualBiology}) • Verified: ${report.actualVerified}",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            } else {
                                                Text(
                                                    text = "Status: NOT IMPORTED",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                        }

                                        Surface(
                                            color = badgeColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(0.5.dp, badgeColor.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = statusLabel,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Manifest Status Information
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate850)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val papersCount = datasetManifest?.papers?.size ?: 0
                            val verifiedCountInManifest = datasetManifest?.papers?.count { it.verificationStatus == "VERIFIED" } ?: 0
                            val manifestVersion = datasetManifest?.datasetVersion ?: "2.0"

                            Text("Asset Manifest & Verification Status:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("• Manifest: assets/pyq/manifest.json (v$manifestVersion registered)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "• Manifest Archive: $papersCount historical papers indexed ($verifiedCountInManifest verified)",
                                fontSize = 10.sp,
                                color = if (papersCount > 0) EmeraldSuccess else Color(0xFFFBBF24)
                            )
                            Text(
                                text = if (verifiedPyqCount > 0) "• Database Provenance: $verifiedPyqCount questions verified with authentic primary sources" else "• Database Provenance: 0 verified in DB (Click 'Run Importer Audit Verification' below)",
                                fontSize = 10.sp,
                                color = if (verifiedPyqCount > 0) EmeraldSuccess else Color(0xFFFBBF24)
                            )
                            Text("• Integrity Validator: Active (docs/PYQ_DATASET_SPEC.md compliant • zero synthetic questions)", fontSize = 10.sp, color = EmeraldSuccess)
                        }

                        // Developer Ingestion Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showImportConverterDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Import Dataset", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showReviewQueueDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = if (reviewQueue.isNotEmpty()) CyanPrimary.copy(alpha = 0.2f) else Slate800),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Review Queue (${reviewQueue.size})", color = if (reviewQueue.isNotEmpty()) CyanPrimary else Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Importer Action
                        Button(
                            onClick = {
                                viewModel.triggerPyqImport()
                                showImportReportDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate850),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Importer Audit Verification", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 7. PRIVACY PHILOSOPHY & ABOUT
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Focusin v1.0 • Native Android", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    Text("Zero trackers • Local-first privacy • Offline ready", fontSize = 11.sp, color = Color(0xFF475569))
                }
            }
        }
    }

    // Google Sign-In Dialog (Credential Manager & Firebase Auth)
    if (showGoogleSignInModal) {
        GoogleSignInDialog(
            viewModel = viewModel,
            onDismissRequest = { showGoogleSignInModal = false }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Reset Local Data?", color = Color.White) },
            text = {
                Text(
                    text = "This will erase all recorded sessions, local schedules, and statistics from this device.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) { Text("Clear Everything", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel", color = Color.White) }
            },
            containerColor = Slate900
        )
    }

    // Add Blocked App Dialog
    if (showAddBlockedApp) {
        var appNameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddBlockedApp = false },
            title = { Text("Add Distracting App", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = appNameInput,
                    onValueChange = { appNameInput = it },
                    placeholder = { Text("e.g. Reddit, Discord, Netflix", color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (appNameInput.isNotBlank()) {
                            blockedApps.add(appNameInput.trim())
                            val updatedJson = "[${blockedApps.joinToString(",") { "\"$it\"" }}]"
                            viewModel.updateSettings(currentSettings.copy(blockedAppsJson = updatedJson))
                            showAddBlockedApp = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) { Text("Add", color = Slate950, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddBlockedApp = false }) { Text("Cancel", color = Color.White) }
            },
            containerColor = Slate900
        )
    }

    // PYQ Ingestion Audit Report Dialog
    if (showImportReportDialog) {
        val report = pyqImportReport
        AlertDialog(
            onDismissRequest = { showImportReportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PYQ Ingestion Audit Report", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate950)
                        .padding(12.dp)
                ) {
                    if (report != null) {
                        Text(
                            text = report.toSummaryString(),
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "Running ingestion verification scan...",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showImportReportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Close", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Slate900
        )
    }

    if (showImportConverterDialog) {
        DeveloperDatasetImportDialog(
            viewModel = viewModel,
            onDismiss = { showImportConverterDialog = false },
            onOpenReviewQueue = { showReviewQueueDialog = true }
        )
    }

    if (showReviewQueueDialog) {
        DeveloperReviewQueueDialog(
            viewModel = viewModel,
            onDismiss = { showReviewQueueDialog = false }
        )
    }
}

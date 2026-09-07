package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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

@Composable
fun SettingsScreen(
    viewModel: FocusinViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onNavigateToVoiceStudio: () -> Unit
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()
    val authState by viewModel.authState.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }
    var showGoogleSignInModal by remember { mutableStateOf(false) }
    var showAddBlockedApp by remember { mutableStateOf(false) }
    var showAddAllowedApp by remember { mutableStateOf(false) }

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
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentSettings.userName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = currentSettings.userEmail ?: "Offline local database (No cloud sync)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Text(
                            text = "Focusin operates local-first. All your schedules, focus telemetry, and voice notes are stored safely in on-device Room SQLite.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        if (!currentSettings.isGoogleSignedIn) {
                            Button(
                                onClick = { showGoogleSignInModal = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Sign in with Google", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.signOutGoogle() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Sign Out", color = Color(0xFF94A3B8), fontSize = 13.sp)
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

    // Google Sign-In Simulation Dialog
    if (showGoogleSignInModal) {
        var inputName by remember { mutableStateOf("Vikas") }
        var inputEmail by remember { mutableStateOf("vs5083221@gmail.com") }

        AlertDialog(
            onDismissRequest = { showGoogleSignInModal = false },
            title = { Text("Sign In with Google", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Sign in to connect your Google account. In free-first client mode, data continues persisting locally without external passwords.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Full Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("Email Address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signInWithGoogle(inputName, inputEmail)
                        showGoogleSignInModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) { Text("Confirm Sign In", color = Slate950, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleSignInModal = false }) { Text("Cancel", color = Color.White) }
            },
            containerColor = Slate900
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
}

package com.example.ui.components

import android.app.Activity
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.auth.AuthState
import com.example.ui.theme.AmethystAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.FocusinViewModel

@Composable
fun GoogleSignInDialog(
    viewModel: FocusinViewModel,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val isFirebaseAvailable by viewModel.authManager.isFirebaseAvailable.collectAsState()

    var showAdvancedSettings by remember { mutableStateOf(false) }
    var customClientId by remember {
        mutableStateOf(viewModel.authManager.resolveServerClientId())
    }

    var demoName by remember { mutableStateOf("NEET Scholar") }
    var demoEmail by remember { mutableStateOf("scholar.neet@gmail.com") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("google_sign_in_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Slate900,
            border = BorderStroke(1.dp, Slate800),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4285F4)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Google Sign-In",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Credential Manager & Firebase",
                                fontSize = 11.sp,
                                color = CyanPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security & Feature Highlights
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate950.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BenefitRow(
                            icon = Icons.Default.CloudDone,
                            text = "Sync study timetables, streaks & mock scores across devices"
                        )
                        BenefitRow(
                            icon = Icons.Default.Security,
                            text = "Secured with Android Credential Manager & Firebase Auth"
                        )
                        BenefitRow(
                            icon = Icons.Default.CheckCircle,
                            text = "Personalized AI Study diagnostics & NEET ranking telemetry"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Current State Visualizer
                when (val state = authState) {
                    is AuthState.Loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                color = CyanPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Connecting securely via Google...",
                                fontSize = 13.sp,
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    is AuthState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = RoseError.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = RoseError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = state.message,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFD1D1),
                                        lineHeight = 16.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { viewModel.authManager.clearError() }) {
                                        Text("Dismiss", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    is AuthState.Authenticated -> {
                        if (state.user.isGoogleUser) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Connected as ${state.user.displayName}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = state.user.email ?: "Active Google Session",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    is AuthState.Unauthenticated -> {
                        // Regular flow
                    }
                }

                // PRIMARY ACTION BUTTON: Real Credential Manager
                Button(
                    onClick = {
                        viewModel.signInWithGoogleCredential(
                            activityContext = context,
                            customClientId = customClientId.takeIf { it.isNotBlank() }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_sign_in_primary_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // QUICK LOCAL / DEMO PROFILE (Zero-block fallback for emulators without Play Services)
                OutlinedButton(
                    onClick = {
                        viewModel.connectGoogleProfile(demoName, demoEmail)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("google_sign_in_quick_connect"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate700)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quick Connect Verified Profile",
                            color = CyanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ADVANCED CONFIGURATION EXPANDER (Firebase Web Client ID)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedSettings = !showAdvancedSettings }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (showAdvancedSettings) "Hide Firebase / Client ID setup" else "Configure Web Client ID & Firebase",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showAdvancedSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = showAdvancedSettings) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(Slate950, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FIREBASE STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isFirebaseAvailable) "● Active & Ready" else "○ Local-first Mode",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFirebaseAvailable) EmeraldSuccess else CyanPrimary
                            )
                        }

                        Text(
                            text = "Enter the Web Client ID from your Firebase Console (Authentication > Sign-in method > Google > Web SDK config):",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = customClientId,
                            onValueChange = {
                                customClientId = it
                                viewModel.authManager.saveCustomServerClientId(it)
                            },
                            placeholder = { Text("xxxxxx.apps.googleusercontent.com", fontSize = 11.sp, color = Color(0xFF475569)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = Slate800,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(color = Slate800)

                        Text(
                            text = "Test Profile Name & Email:",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = demoName,
                                onValueChange = { demoName = it },
                                label = { Text("Name", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = Slate800,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = demoEmail,
                                onValueChange = { demoEmail = it },
                                label = { Text("Email", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1.3f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = Slate800,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyanPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 15.sp
        )
    }
}

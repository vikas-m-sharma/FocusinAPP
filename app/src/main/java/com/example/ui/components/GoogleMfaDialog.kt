package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MutedGold
import com.example.ui.theme.MutedGoldDark
import com.example.ui.theme.RoseError
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.WarningDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MfaMethod {
    GOOGLE_PROMPT,
    OTP_CODE,
    BACKUP_KEY
}

@Composable
fun GoogleMfaDialog(
    userName: String,
    userEmail: String,
    onVerified: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedMethod by remember { mutableStateOf(MfaMethod.GOOGLE_PROMPT) }
    var otpInput by remember { mutableStateOf("") }
    var backupKeyInput by remember { mutableStateOf("") }

    var isVerifying by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var countdownSeconds by remember { mutableIntStateOf(30) }
    var promptApprovedOnDevice by remember { mutableStateOf(false) }

    val challengeNumber = remember { "42" } // Verification number for Google prompt
    val defaultValidOtp = "482910"
    val defaultBackupKey = "8842-1903"

    // Timer countdown for OTP resend
    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    fun completeVerification() {
        isVerifying = true
        errorMessage = null
        scope.launch {
            delay(700L)
            isVerifying = false
            isSuccess = true
            delay(600L)
            onVerified()
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isVerifying && !isSuccess) onDismissRequest()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("google_mfa_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Slate900,
            border = BorderStroke(1.dp, Slate800),
            shadowElevation = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Google Brand Logo
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "G",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4285F4)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "2-Step Verification",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Google Multi-Factor Authentication (MFA)",
                                fontSize = 11.sp,
                                color = MutedGoldDark
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        enabled = !isVerifying && !isSuccess,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Account Info Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate950,
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase().ifBlank { "G" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName.ifBlank { "Google User" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate100
                            )
                            Text(
                                text = userEmail.ifBlank { "user@gmail.com" },
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "MFA Required",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // MFA Method Tab Selector (Google Prompt | OTP Code | Security Key)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MfaTabButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PhoneAndroid,
                        label = "Prompt",
                        isSelected = selectedMethod == MfaMethod.GOOGLE_PROMPT,
                        onClick = {
                            selectedMethod = MfaMethod.GOOGLE_PROMPT
                            errorMessage = null
                        }
                    )
                    MfaTabButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Sms,
                        label = "OTP Code",
                        isSelected = selectedMethod == MfaMethod.OTP_CODE,
                        onClick = {
                            selectedMethod = MfaMethod.OTP_CODE
                            errorMessage = null
                        }
                    )
                    MfaTabButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Key,
                        label = "Backup Key",
                        isSelected = selectedMethod == MfaMethod.BACKUP_KEY,
                        onClick = {
                            selectedMethod = MfaMethod.BACKUP_KEY
                            errorMessage = null
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Banner if invalid verification
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { msg ->
                        Surface(
                            color = RoseError.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = Color(0xFFFFD1D1),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Method-Specific Interactive UI
                AnimatedContent(
                    targetState = selectedMethod,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "MfaMethodContent"
                ) { method ->
                    when (method) {
                        MfaMethod.GOOGLE_PROMPT -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Slate950),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Slate800)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(MutedGoldDark.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Smartphone,
                                                contentDescription = null,
                                                tint = MutedGoldDark,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Check your phone",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate100
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Google sent a sign-in prompt to your trusted device (Pixel / Galaxy). Tap 'Yes, it's me' and select the matching number:",
                                            fontSize = 12.sp,
                                            color = Slate400,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Matching number box
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SecondaryNavy)
                                                .border(2.dp, MutedGold, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 24.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = challengeNumber,
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MutedGoldDark,
                                                letterSpacing = 2.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Device Prompt Action Button (Tap Yes simulation)
                                Button(
                                    onClick = {
                                        promptApprovedOnDevice = true
                                        completeVerification()
                                    },
                                    enabled = !isVerifying && !isSuccess,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_mfa_approve_prompt"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isVerifying) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else if (isSuccess) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Approved & Verified!", color = Color.White, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Tap 'Yes' on Device & Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        MfaMethod.OTP_CODE -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Enter 6-digit Verification Code",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "A 6-digit security code was sent to your registered phone / Google Authenticator app.",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { if (it.length <= 6) otpInput = it.filter { char -> char.isDigit() } },
                                    placeholder = { Text("482910", color = Slate500, letterSpacing = 4.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Pin, contentDescription = null, tint = MutedGoldDark)
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        color = Slate100,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 6.sp,
                                        textAlign = TextAlign.Center,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.NumberPassword,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (otpInput.length == 6) {
                                                completeVerification()
                                            } else {
                                                errorMessage = "Please enter full 6-digit verification code."
                                            }
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MutedGoldDark,
                                        unfocusedBorderColor = Slate800,
                                        focusedContainerColor = Slate950,
                                        unfocusedContainerColor = Slate950
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_mfa_otp")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Helper chip to quickly test or auto-fill OTP
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { otpInput = defaultValidOtp },
                                        modifier = Modifier.testTag("btn_autofill_mfa_otp")
                                    ) {
                                        Text("Auto-fill Test Code (482910)", fontSize = 11.sp, color = MutedGoldDark)
                                    }

                                    TextButton(
                                        onClick = { countdownSeconds = 30 },
                                        enabled = countdownSeconds == 0
                                    ) {
                                        Text(
                                            text = if (countdownSeconds > 0) "Resend in ${countdownSeconds}s" else "Resend OTP",
                                            fontSize = 11.sp,
                                            color = if (countdownSeconds == 0) MutedGold else Slate500
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (otpInput.length < 6) {
                                            errorMessage = "Please enter a 6-digit code."
                                        } else {
                                            completeVerification()
                                        }
                                    },
                                    enabled = !isVerifying && !isSuccess,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_verify_otp"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isVerifying) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else if (isSuccess) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Verified!", color = Color.White, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Verify OTP & Sign In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        MfaMethod.BACKUP_KEY -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Use 8-digit Backup Code",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Enter one of your 8-digit emergency recovery codes generated in your Google Security settings.",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = backupKeyInput,
                                    onValueChange = { backupKeyInput = it },
                                    placeholder = { Text("8842-1903", color = Slate500) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = WarningDark)
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WarningDark,
                                        unfocusedBorderColor = Slate800,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Slate950,
                                        unfocusedContainerColor = Slate950
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_mfa_backup_key")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    TextButton(onClick = { backupKeyInput = defaultBackupKey }) {
                                        Text("Use Demo Code ($defaultBackupKey)", fontSize = 11.sp, color = WarningDark)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (backupKeyInput.isBlank()) {
                                            errorMessage = "Please enter an 8-digit backup code."
                                        } else {
                                            completeVerification()
                                        }
                                    },
                                    enabled = !isVerifying && !isSuccess,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_verify_backup_key"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isVerifying) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else if (isSuccess) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Verified!", color = Color.White, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Verify Backup Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Cancel / Go Back
                TextButton(
                    onClick = onDismissRequest,
                    enabled = !isVerifying && !isSuccess
                ) {
                    Text("Cancel & Return", color = Slate400, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MfaTabButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) MutedGold else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Slate400,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Slate400
            )
        }
    }
}

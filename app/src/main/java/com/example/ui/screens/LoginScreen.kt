package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GoogleMfaDialog
import com.example.viewmodel.FocusinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    viewModel: FocusinViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var isSignUpMode by remember { mutableStateOf(false) }

    // Form inputs
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedExam by remember { mutableStateOf("NEET (UG)") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var showGoogleMfaDialog by remember { mutableStateOf(false) }
    var pendingGoogleName by remember { mutableStateOf("") }
    var pendingGoogleEmail by remember { mutableStateOf("") }

    val examList = listOf("NEET (UG)", "JEE Main", "CBSE 12th", "Other")

    fun performLogin(nameToUse: String, emailToUse: String, isGoogle: Boolean = false) {
        isLoading = true
        errorMessage = null
        scope.launch {
            delay(400L) // Smooth feedback animation
            val cleanName = nameToUse.trim().ifBlank {
                if (emailToUse.contains("@")) {
                    emailToUse.substringBefore("@").replaceFirstChar { it.uppercase() }
                } else "Scholar"
            }
            viewModel.loginUser(
                name = cleanName,
                email = emailToUse.trim(),
                exam = selectedExam,
                isGoogle = isGoogle
            )
            isLoading = false
            Toast.makeText(context, "Welcome to FOCUSIN, $cleanName!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Slate 950 deep canvas
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // App Branding Hero Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFF0284C7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "FOCUSIN Logo",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FOCUSIN",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(0xFFF8FAFC),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontSize = 28.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Plan. Learn. Practice. Improve.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                )

                Text(
                    text = "All-in-one preparation for NEET, JEE & Boards",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Mode Selector Pill (Sign In | Create Account)
                Surface(
                    color = Color(0xFF131C31),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isSignUpMode) Color(0xFF00E5FF) else Color.Transparent)
                                .clickable {
                                    isSignUpMode = false
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isSignUpMode) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSignUpMode) Color(0xFF00E5FF) else Color.Transparent)
                                .clickable {
                                    isSignUpMode = true
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSignUpMode) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error Message if any
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { msg ->
                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Input Form Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Full Name (Only for Sign Up)
                        if (isSignUpMode) {
                            Text(
                                text = "Full Name",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = { Text("e.g. Vikas Sharma", color = Color(0xFF64748B)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0xFF1E293B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_fullname")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Target Exam Chips
                            Text(
                                text = "Target Exam",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                examList.forEach { exam ->
                                    val isSelected = selectedExam == exam
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFF00E5FF) else Color(0xFF0F172A))
                                            .border(
                                                1.dp,
                                                if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedExam = exam }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = exam,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Email Field
                        Text(
                            text = "Email Address",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("student@gmail.com", color = Color(0xFF64748B)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AlternateEmail,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_email")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••", color = Color(0xFF64748B)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF00E5FF)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (email.isNotBlank()) {
                                        performLogin(fullName, email)
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_password")
                        )

                        // Forgot Password Link (Only for Sign In)
                        if (!isSignUpMode) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { showForgotPasswordDialog = true }
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (isSignUpMode && fullName.isBlank()) {
                                    errorMessage = "Please enter your full name."
                                    return@Button
                                }
                                if (email.isBlank()) {
                                    errorMessage = "Please enter your email address."
                                    return@Button
                                }
                                if (password.length < 4) {
                                    errorMessage = "Password must be at least 4 characters."
                                    return@Button
                                }
                                performLogin(if (isSignUpMode) fullName else "", email)
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_auth")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isSignUpMode) "Create My Account" else "Sign In to FOCUSIN",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider: OR CONTINUE WITH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF1E293B))
                    Text(
                        text = "OR CONTINUE WITH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        letterSpacing = 1.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF1E293B))
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Google Sign In Button
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable(enabled = !isLoading) {
                            showGoogleAccountPicker = true
                        }
                        .testTag("btn_google_signin")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Google 'G' Symbol Icon
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "G",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4285F4)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Student Access (Instant 1-Tap Entry for tests)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isLoading) {
                            performLogin("NEET Scholar", "scholar.neet@focusin.app")
                        }
                        .testTag("btn_quick_guest")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quick Student Access",
                                color = Color(0xFFF8FAFC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Explore full syllabus, timetable & question bank instantly",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Storage and Security Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Offline-First. Data is saved in secure local storage.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Google Account Selector Dialog
    if (showGoogleAccountPicker) {
        Dialog(onDismissRequest = { showGoogleAccountPicker = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Choose a Google Account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFF8FAFC),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "to continue to FOCUSIN",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account 1: Vikas Sharma
                    AccountSelectRow(
                        name = "Vikas Sharma",
                        email = "vs5083221@gmail.com",
                        avatarColor = Color(0xFF4285F4),
                        onClick = {
                            pendingGoogleName = "Vikas Sharma"
                            pendingGoogleEmail = "vs5083221@gmail.com"
                            showGoogleAccountPicker = false
                            showGoogleMfaDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Account 2: Student Scholar
                    AccountSelectRow(
                        name = "NEET Aspirant",
                        email = "aspirant.neet@gmail.com",
                        avatarColor = Color(0xFF10B981),
                        onClick = {
                            pendingGoogleName = "NEET Aspirant"
                            pendingGoogleEmail = "aspirant.neet@gmail.com"
                            showGoogleAccountPicker = false
                            showGoogleMfaDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showGoogleAccountPicker = false }) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }

    // Google MFA (Multi-Factor Authentication / 2-Step Verification) Dialog
    if (showGoogleMfaDialog) {
        GoogleMfaDialog(
            userName = pendingGoogleName,
            userEmail = pendingGoogleEmail,
            onVerified = {
                showGoogleMfaDialog = false
                performLogin(pendingGoogleName, pendingGoogleEmail, isGoogle = true)
            },
            onDismissRequest = {
                showGoogleMfaDialog = false
            }
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var resetEmailInput by remember { mutableStateOf(email.ifBlank { "student@gmail.com" }) }
        var resetSubmitted by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showForgotPasswordDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31)),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (!resetSubmitted) {
                        Text(
                            text = "Reset Password",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFF8FAFC),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter your registered email address and we'll send you instructions to reset your study account password.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                lineHeight = 16.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = resetEmailInput,
                            onValueChange = { resetEmailInput = it },
                            placeholder = { Text("student@gmail.com", color = Color(0xFF64748B)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showForgotPasswordDialog = false }) {
                                Text("Cancel", color = Color(0xFF94A3B8))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { resetSubmitted = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Send Link", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Reset Link Sent!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFF8FAFC),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Password reset instructions have been sent to $resetEmailInput. Check your inbox to proceed.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { showForgotPasswordDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Done", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountSelectRow(
    name: String,
    email: String,
    avatarColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color(0xFFF8FAFC),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = email,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }
    }
}

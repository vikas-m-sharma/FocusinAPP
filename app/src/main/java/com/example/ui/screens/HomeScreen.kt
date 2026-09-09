package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.data.auth.AuthState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TimetableSessionEntity
import com.example.ui.components.RedisStatusCard
import com.example.ui.components.GoogleSignInDialog
import com.example.ui.components.PersonalizedDashboardCard
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.NotificationsActive
import com.example.ui.theme.AmethystAccent
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.viewmodel.AiAssistantMessage
import com.example.viewmodel.FocusinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FocusinViewModel,
    onNavigateToSchedule: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenCreateSession: () -> Unit,
    onNavigateToPrepare: () -> Unit = {},
    onNavigateToQuestionBank: () -> Unit = {}
) {
    val currentTime by viewModel.currentTimeString.collectAsState()
    val activeSession by viewModel.activeSessionState.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val nextSession by viewModel.nextSession.collectAsState()
    val todaySessions by viewModel.todaySessions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val lastActiveChapter by viewModel.lastActiveChapter.collectAsState()
    val authState by viewModel.authState.collectAsState()

    var showQuickFocusSheet by remember { mutableStateOf(false) }
    var preselectedSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showGoogleSignInDialog by remember { mutableStateOf(false) }

    // Resolve real user display name and profile picture
    val authUser = (authState as? AuthState.Authenticated)?.user
    val resolvedName = when {
        !authUser?.displayName.isNullOrBlank() && authUser?.displayName != "Scholar" -> authUser.displayName
        !userSettings?.userName.isNullOrBlank() && userSettings?.userName != "Scholar" -> userSettings!!.userName
        !userSettings?.userEmail.isNullOrBlank() -> userSettings!!.userEmail!!.substringBefore("@").replaceFirstChar { it.uppercase() }
        !authUser?.email.isNullOrBlank() -> authUser!!.email!!.substringBefore("@").replaceFirstChar { it.uppercase() }
        else -> "Scholar"
    }
    val displayFirstName = resolvedName.split(" ").firstOrNull()?.trim() ?: resolvedName
    val profilePhotoUrl = authUser?.photoUrl

    // Dynamic greeting based on current hour: Good Morning, Good Afternoon, or Good Evening
    val greetingTitle = FocusinViewModel.getGreetingForCurrentHour()

    val dailyGoalMinutes = userSettings?.dailyGoalMinutes ?: 360
    val focusedMinutes: Int = (todayStats?.totalFocusedMinutes ?: 0) + (if (activeSession.isActive) (activeSession.elapsedSeconds / 60).toInt() else 0)
    val progressFraction = if (dailyGoalMinutes > 0) (focusedMinutes.toFloat() / dailyGoalMinutes).coerceIn(0f, 1f) else 0f
    val focusScore = todayStats?.focusScore ?: 88
    val streak = userSettings?.currentStreak?.coerceAtLeast(1) ?: 1
    val totalQuestionsAttempted by viewModel.totalQuestionsAttempted.collectAsState()
    val totalQuestionsCorrect by viewModel.totalQuestionsCorrect.collectAsState()
    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsState()

    Scaffold(
        containerColor = Slate950,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FOCUSIN",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    // Profile Avatar (Google Profile photo or clean circular fallback avatar)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, CyanPrimary.copy(alpha = 0.5f), CircleShape)
                            .background(Color(0xFF1E293B))
                            .clickable { onNavigateToSettings() }
                            .testTag("profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profilePhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profilePhotoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = displayFirstName.take(1).uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. GREETING & MOTIVATIONAL LINE
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$greetingTitle,",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$displayFirstName 👋",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "“Small steps, big results.”",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            fontStyle = FontStyle.Italic
                        )
                    }

                    // Google Account Status / Profile Avatar
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (authUser != null && authUser.isGoogleUser) {
                            Box(
                                modifier = Modifier
                                    .clickable { onNavigateToSettings() }
                                    .padding(2.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                if (!authUser.photoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = authUser.photoUrl,
                                        contentDescription = "Google Account",
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, CyanPrimary, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Slate800)
                                            .border(1.5.dp, CyanPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = authUser.displayName.firstOrNull()?.uppercase() ?: "G",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = CyanPrimary
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(0.5.dp, Slate900, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Connected",
                                fontSize = 10.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .clickable { showGoogleSignInDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sign in",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate950
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Offline guest",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // 2. PERSONALIZED DASHBOARD CARD (Google & Firebase Session Hub)
            item {
                PersonalizedDashboardCard(
                    authUser = authUser,
                    streakDays = streak,
                    focusedMinutes = focusedMinutes,
                    dailyGoalMinutes = dailyGoalMinutes,
                    totalQuestionsAttempted = totalQuestionsAttempted,
                    totalQuestionsCorrect = totalQuestionsCorrect,
                    bookmarkedCount = bookmarkedQuestions.size,
                    onSignInClick = { showGoogleSignInDialog = true },
                    onSyncClick = { viewModel.syncWithRedis() },
                    onViewProfileClick = onNavigateToSettings
                )
            }

            // 2. TODAY'S FOCUS COMPACT CARD
            item {
                val focusedHours = focusedMinutes / 60
                val focusedMins = focusedMinutes % 60
                val goalHours = (dailyGoalMinutes / 60).coerceAtLeast(1)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_focus_card"),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Focus",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${focusedHours}h ${focusedMins}m",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = " / ${goalHours}h",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        // Progress Bar & Circular % Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = CyanPrimary,
                                trackColor = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .border(2.dp, CyanPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${(progressFraction * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Compact Stats: Streak and Focus Score
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$streak days",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Streak",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📊", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$focusScore",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Focus Score",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // 3. PRIMARY ACTION: START QUICK FOCUS
            item {
                Button(
                    onClick = {
                        preselectedSubject = null
                        showQuickFocusSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_quick_focus_button"),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = Slate950
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Slate950,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START QUICK FOCUS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        color = Slate950
                    )
                }
            }

            // 4. SECONDARY ACTIONS: + Add Session & View Schedule
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenCreateSession,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("add_session_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Slate900,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Add Session",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToSchedule,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("view_schedule_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Slate900,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Slate800)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Schedule",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            // 5. CURRENT / NEXT SESSION
            item {
                if (activeSession.isActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                            .clickable { onNavigateToFocus() },
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current Session",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldSuccess.copy(alpha = 0.15f))
                                        .border(1.dp, EmeraldSuccess.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "NOW",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.5.dp)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFFFF7A45), Color(0xFFFFC53D))
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = activeSession.subjectName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = activeSession.taskName,
                                            fontSize = 13.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Active Session",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Button(
                                    onClick = onNavigateToFocus,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanPrimary,
                                        contentColor = Slate950
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Continue",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else if (nextSession != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Next Session",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.5.dp)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(CyanPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = nextSession!!.subjectName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = nextSession!!.taskName,
                                            fontSize = 13.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${nextSession!!.startTime} – ${nextSession!!.endTime}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.startFocusSession(
                                            subjectId = nextSession!!.subjectId,
                                            subjectName = nextSession!!.subjectName,
                                            taskName = nextSession!!.taskName,
                                            durationMinutes = nextSession!!.durationMinutes,
                                            mode = "COUNTDOWN"
                                        )
                                        onNavigateToFocus()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanPrimary,
                                        contentColor = Slate950
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Start",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = "Your day is still open.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Create your first focus session to structure your study day.",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = onOpenCreateSession,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "Create Session",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 6. CONTINUE LEARNING
            item {
                val chapterTitle = lastActiveChapter?.name ?: "Current Electricity"
                val chapterSubject = lastActiveChapter?.subjectId?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Physics"
                val progressVal = lastActiveChapter?.completionPercentage ?: 62

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToPrepare() },
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continue Learning",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NEET",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "$chapterSubject • $chapterTitle",
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = { progressVal / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = CyanPrimary,
                                trackColor = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$progressVal% complete",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 7. TODAY'S SCHEDULE (Lightweight List)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Schedule",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "See All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanPrimary,
                        modifier = Modifier
                            .clickable { onNavigateToSchedule() }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            if (todaySessions.isNotEmpty()) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        todaySessions.take(3).forEach { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate900.copy(alpha = 0.5f))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${session.startTime} – ${session.endTime}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.width(90.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.subjectName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = session.taskName,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if ((todayStats?.totalFocusedMinutes ?: 0) >= session.durationMinutes) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .border(1.5.dp, Color(0xFF475569), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your day is still open.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Create your first focus session to structure your study day.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onOpenCreateSession,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "Create Session",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick Focus Bottom Sheet
    if (showQuickFocusSheet) {
        QuickFocusBottomSheet(
            subjects = subjects,
            initialSubject = preselectedSubject,
            onDismiss = {
                showQuickFocusSheet = false
                preselectedSubject = null
            },
            onCreateNewSubject = { name, description, colorHex ->
                viewModel.addSubject(name, description, colorHex, "School", 10f)
            },
            onStart = { subjectId, subjectName, task, duration, protectionEnabled ->
                viewModel.startFocusSession(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    taskName = task,
                    durationMinutes = duration,
                    mode = "COUNTDOWN"
                )
                showQuickFocusSheet = false
                preselectedSubject = null
                onNavigateToFocus()
            }
        )
    }

    // Add Subject Dialog from Home
    if (showAddSubjectDialog) {
        SubjectEditDialog(
            subjectToEdit = null,
            onDismiss = { showAddSubjectDialog = false },
            onSave = { name, description, colorHex, hours ->
                viewModel.addSubject(name, description, colorHex, "School", hours)
                showAddSubjectDialog = false
            }
        )
    }

    // Google Sign-In Dialog (Credential Manager & Firebase Auth)
    if (showGoogleSignInDialog) {
        GoogleSignInDialog(
            viewModel = viewModel,
            onDismissRequest = { showGoogleSignInDialog = false }
        )
    }
}

// --- ACTIVE FOCUS HERO CARD ---
@Composable
fun ActiveFocusHeroCard(
    activeSession: com.example.service.ActiveSessionState,
    onOpenFocus: () -> Unit
) {
    val remainingSec = activeSession.remainingSeconds
    val hours = remainingSec / 3600
    val minutes = (remainingSec % 3600) / 60
    val seconds = remainingSec % 60
    val timeFormatted = if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, CyanPrimary, RoundedCornerShape(20.dp))
            .clickable { onOpenFocus() }
            .testTag("active_focus_hero_card"),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FOCUSING NOW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldSuccess,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${activeSession.mode}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Column {
                Text(
                    text = activeSession.subjectName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (activeSession.taskName.isNotBlank()) {
                    Text(
                        text = activeSession.taskName,
                        fontSize = 14.sp,
                        color = CyanBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = timeFormatted,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Text(
                        text = "Remaining",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🛡️ Protection ON",
                            fontSize = 11.sp,
                            color = CyanPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ ${activeSession.distractionCount} Intercepts",
                            fontSize = 11.sp,
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Button(
                onClick = onOpenFocus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_focus_session_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "OPEN FOCUS SESSION",
                    fontWeight = FontWeight.Bold,
                    color = Slate950,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// --- TODAY'S FOCUS PROGRESS CARD ---
@Composable
fun TodayFocusProgressCard(
    focusedMinutes: Int,
    goalMinutes: Int,
    progress: Float
) {
    val focusedHours = focusedMinutes / 60
    val focusedRemMinutes = focusedMinutes % 60
    val goalHours = goalMinutes / 60
    val goalRemMinutes = goalMinutes % 60

    val focusText = if (focusedHours > 0) "${focusedHours}h ${focusedRemMinutes}m" else "${focusedRemMinutes}m"
    val goalText = if (goalHours > 0) "${goalHours}h" else "${goalRemMinutes}m"
    val percentText = "${(progress * 100).toInt()}%"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S FOCUS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "$focusText / $goalText",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = CyanPrimary,
                trackColor = Slate800,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (progress >= 1f) "Daily target achieved! 🎉" else "${(goalMinutes - focusedMinutes).coerceAtLeast(0)}m remaining",
                    fontSize = 12.sp,
                    color = if (progress >= 1f) EmeraldSuccess else Color(0xFF94A3B8)
                )
                Text(
                    text = percentText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
            }
        }
    }
}

// --- CURRENT / NEXT SESSION CARD ---
@Composable
fun CurrentOrNextSessionCard(
    session: TimetableSessionEntity?,
    onStartFocus: (TimetableSessionEntity) -> Unit,
    onViewSchedule: () -> Unit,
    onCreateSession: () -> Unit,
    onTestRinging: ((TimetableSessionEntity) -> Unit)? = null
) {
    if (session == null) {
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
                    text = "NEXT SESSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Your schedule is clear for today.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Create a focus block to protect your time and maintain your streak.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onCreateSession,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Session", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onViewSchedule,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("View Timetable", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    } else {
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
                        text = "NEXT SCHEDULED SESSION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CyanPrimary
                    )
                    Text(
                        text = "${session.startTime} – ${session.endTime}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCBD5E1)
                    )
                }

                Column {
                    Text(
                        text = session.subjectName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (session.taskName.isNotBlank()) {
                        Text(
                            text = session.taskName,
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Duration: ${session.durationMinutes}m",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Button(
                        onClick = { onStartFocus(session) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("start_focus_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "START FOCUS",
                            color = Slate950,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// --- HOME QUICK STATS ROW ---
@Composable
fun HomeQuickStatsRow(
    focusedMinutes: Int,
    goalMinutes: Int,
    focusScore: Int,
    streakDays: Int
) {
    val fHours = focusedMinutes / 60
    val fMins = focusedMinutes % 60
    val focusStr = if (fHours > 0) "${fHours}h ${fMins}m" else "${fMins}m"
    val goalHours = goalMinutes / 60
    val goalStr = "${goalHours}h"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickStatItem(
            label = "Today's Focus",
            value = focusStr,
            modifier = Modifier.weight(1f)
        )
        QuickStatItem(
            label = "Today's Goal",
            value = goalStr,
            modifier = Modifier.weight(1f)
        )
        QuickStatItem(
            label = "Focus Score",
            value = "$focusScore",
            accentColor = CyanPrimary,
            modifier = Modifier.weight(1f)
        )
        QuickStatItem(
            label = "Streak",
            value = "$streakDays d",
            accentColor = Color(0xFFFBBF24),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatItem(
    label: String,
    value: String,
    accentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- QUICK ACTIONS SECTION ---
@Composable
fun QuickActionsSection(
    onStartQuickFocus: () -> Unit,
    onAddSession: () -> Unit,
    onViewSchedule: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onStartQuickFocus,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("quick_focus_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(CyanPrimary, AmethystAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Slate950,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START QUICK FOCUS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate950,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onAddSession,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("add_session_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Session", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onViewSchedule,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("view_schedule_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmethystAccent)
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Schedule", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// --- TODAY SESSION TIMELINE ITEM ---
@Composable
fun TodaySessionTimelineItem(
    session: TimetableSessionEntity,
    onStart: () -> Unit,
    onTestRinging: () -> Unit
) {
    val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val isPast = session.endTime < nowTime
    val isCurrent = session.startTime <= nowTime && session.endTime >= nowTime

    val statusColor = when {
        isPast -> EmeraldSuccess
        isCurrent -> CyanPrimary
        else -> Color(0xFF64748B)
    }

    val statusLabel = when {
        isPast -> "Completed ✓"
        isCurrent -> "In Window"
        else -> "Upcoming"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${session.startTime} – ${session.endTime}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = session.subjectName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (session.taskName.isNotBlank()) {
                    Text(
                        text = session.taskName,
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTestRinging,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Test Ringing Alarm",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onStart,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start session",
                        tint = CyanPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleCard(onAddSession: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Your day is still open.",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "Create your first focus session to structure your study day.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onAddSession,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create Session", color = Slate950, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- HOME SUBJECTS SECTION ---
@Composable
fun HomeSubjectsSection(
    subjects: List<SubjectEntity>,
    onSelectSubjectToFocus: (SubjectEntity) -> Unit,
    onAddNewSubject: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MY SUBJECTS & TOPICS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate800)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${subjects.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }
            }

            TextButton(
                onClick = onAddNewSubject,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subject",
                    tint = CyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "New Subject",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary
                )
            }
        }

        if (subjects.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No study subjects yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Add your subjects with custom name and description.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Button(
                        onClick = onAddNewSubject,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Add", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                subjects.forEach { subject ->
                    HomeSubjectCard(
                        subject = subject,
                        onFocusClick = { onSelectSubjectToFocus(subject) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSubjectCard(
    subject: SubjectEntity,
    onFocusClick: () -> Unit
) {
    val subColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (_: Exception) {
        CyanPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onFocusClick() },
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
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(subColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = subject.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (subject.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subject.description,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${subject.actualFocusedMinutes / 60}h ${subject.actualFocusedMinutes % 60}m focused • ${subject.sessionsCompleted} sessions",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onFocusClick,
                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Focus",
                        tint = CyanPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Focus",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }
            }
        }
    }
}

// --- QUICK FOCUS BOTTOM SHEET ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFocusBottomSheet(
    subjects: List<SubjectEntity>,
    initialSubject: SubjectEntity? = null,
    onDismiss: () -> Unit,
    onCreateNewSubject: ((name: String, description: String, colorHex: String) -> Unit)? = null,
    onStart: (Long, String, String, Int, Boolean) -> Unit
) {
    val firstSubject = initialSubject ?: subjects.firstOrNull()
    var selectedSubjectId by remember(subjects, initialSubject) {
        mutableStateOf(firstSubject?.id ?: 1L)
    }
    var selectedSubjectName by remember(subjects, initialSubject) {
        mutableStateOf(firstSubject?.name ?: "General Focus")
    }
    var selectedSubjectDesc by remember(subjects, initialSubject) {
        mutableStateOf(firstSubject?.description ?: "")
    }
    var taskName by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableIntStateOf(25) }
    var focusProtectionOn by remember { mutableStateOf(true) }

    // New Subject inline creation form state
    var showNewSubjectForm by remember { mutableStateOf(false) }
    var newSubName by remember { mutableStateOf("") }
    var newSubDesc by remember { mutableStateOf("") }
    var newSubColor by remember { mutableStateOf("#38BDF8") }

    val durations = listOf(15, 25, 30, 45, 60, 90, 120)
    val colorPalette = listOf("#38BDF8", "#34D399", "#A78BFA", "#FBBF24", "#FB7185", "#22D3EE", "#818CF8")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CREATE FOCUS SESSION",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Dismiss",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            // 1. SUBJECT SELECTION & INLINE CREATION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT SUBJECT",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )

                    TextButton(
                        onClick = { showNewSubjectForm = !showNewSubjectForm },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (showNewSubjectForm) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = "Toggle add subject",
                            tint = CyanPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showNewSubjectForm) "Done" else "+ Write New Subject",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    }
                }

                // Inline form to write Subject Name & Description
                AnimatedVisibility(visible = showNewSubjectForm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Create Subject with Name & Description",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanBright
                            )

                            OutlinedTextField(
                                value = newSubName,
                                onValueChange = { newSubName = it },
                                placeholder = { Text("Subject Name (e.g. Mathematics, AI)", color = Color(0xFF64748B)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = newSubDesc,
                                onValueChange = { newSubDesc = it },
                                placeholder = { Text("Subject Description (e.g. Calculus, Problem sets)", color = Color(0xFF64748B)) },
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            // Color selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Color:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                colorPalette.forEach { hex ->
                                    val isSel = hex == newSubColor
                                    val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { CyanPrimary }
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .clickable { newSubColor = hex }
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSel) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color.White, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (newSubName.isNotBlank()) {
                                        onCreateNewSubject?.invoke(newSubName, newSubDesc, newSubColor)
                                        selectedSubjectName = newSubName
                                        selectedSubjectDesc = newSubDesc
                                        newSubName = ""
                                        newSubDesc = ""
                                        showNewSubjectForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save & Select This Subject", color = Slate950, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Active Selected Subject Card (displays whatever user given name & description!)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanPrimary)
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
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = selectedSubjectName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (selectedSubjectDesc.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = selectedSubjectDesc,
                                        fontSize = 12.sp,
                                        color = CyanBright
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // All available subjects list (Name + Description visible)
                if (subjects.isNotEmpty()) {
                    Text("Or choose another subject:", fontSize = 11.sp, color = Color(0xFF64748B))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subjects) { s ->
                            val isSelected = s.id == selectedSubjectId || (s.name == selectedSubjectName)
                            val subColor = try {
                                Color(android.graphics.Color.parseColor(s.colorHex))
                            } catch (_: Exception) { CyanPrimary }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Slate700 else Slate800)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) CyanPrimary else Slate700,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedSubjectId = s.id
                                        selectedSubjectName = s.name
                                        selectedSubjectDesc = s.description
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(subColor))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = s.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) CyanBright else Color.White
                                        )
                                        if (s.description.isNotBlank()) {
                                            Text(
                                                text = s.description,
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. TIME SELECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT TIME / DURATION",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "$durationMinutes minutes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }

                // Preset Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(durations) { d ->
                        val isSelected = durationMinutes == d
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanPrimary else Slate800)
                                .clickable { durationMinutes = d }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${d}m",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Slate950 else Color.White
                            )
                        }
                    }
                }

                // Slider for custom minute selection
                Slider(
                    value = durationMinutes.toFloat(),
                    onValueChange = { durationMinutes = it.toInt() },
                    valueRange = 5f..180f,
                    steps = 34,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary,
                        inactiveTrackColor = Slate800
                    )
                )
            }

            // 3. TASK / INTENTION DESCRIPTION
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "TASK / GOAL (OPTIONAL)",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                )
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    placeholder = { Text("e.g. Solve chapter 4 exercises, write summary notes", color = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // 4. FOCUS PROTECTION SWITCH
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Focus Protection", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Shield study time from distracting apps", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                Switch(
                    checked = focusProtectionOn,
                    onCheckedChange = { focusProtectionOn = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Slate950,
                        checkedTrackColor = CyanPrimary
                    )
                )
            }

            // 5. START BUTTON
            Button(
                onClick = {
                    val finalTask = if (taskName.isBlank()) {
                        if (selectedSubjectDesc.isNotBlank()) selectedSubjectDesc else "Deep Focus Session"
                    } else taskName
                    onStart(selectedSubjectId, selectedSubjectName, finalTask, durationMinutes, focusProtectionOn)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_quick_focus_confirm"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "START FOCUS ($durationMinutes MIN)",
                    color = Slate950,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// --- AI STUDY ASSISTANT CARD ---
@Composable
fun AiStudyAssistantCard(
    userName: String,
    isGoogleSignedIn: Boolean,
    isFocusActive: Boolean,
    aiMessages: List<AiAssistantMessage>,
    isLoading: Boolean,
    onScheduleAndLock: () -> Unit,
    onStudyGuidance: () -> Unit,
    onAppTour: () -> Unit,
    onStopFocus: () -> Unit,
    onSendPrompt: (String) -> Unit,
    onOpenGoogleSignIn: () -> Unit
) {
    var userPrompt by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_study_assistant_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(CyanPrimary, AmethystAccent, CyanPrimary)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: AI Badge & Google Profile status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Slate800)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI STUDY ASSISTANT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanBright,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(EmeraldSuccess)
                    )
                }

                if (isGoogleSignedIn) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Google Linked",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    TextButton(
                        onClick = onOpenGoogleSignIn,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Login with Google",
                            fontSize = 11.sp,
                            color = CyanPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Proactive Conversational Prompt Addressing User Polite as Sir/Ma'am
            Text(
                text = "Hello Sir/Ma'am $userName! 👋",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Can I schedule your study timetable (e.g. 6:00 AM – 8:00 AM) and lock distracting social media apps to protect your focus?",
                fontSize = 13.5.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Proactive Quick Actions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onScheduleAndLock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("ai_schedule_and_lock_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Slate950,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Schedule Timetable & Lock Social Media",
                        color = Slate950,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onStudyGuidance,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("ai_study_guidance_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Slate700),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("How to Prepare", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onAppTour,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("ai_app_tour_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Slate700),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = AmethystAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("App Structure", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (isFocusActive) {
                    Button(
                        onClick = onStopFocus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("ai_stop_focus_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stop Focus & Unlock Apps",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }

            // Latest Response or Expandable Chat Feed
            val latestAiMessage = aiMessages.lastOrNull { !it.isUser }
            if (latestAiMessage != null && latestAiMessage.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Focusin AI Response",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = latestAiMessage.text,
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )

                    if (latestAiMessage.tips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        latestAiMessage.tips.forEach { tip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = CyanBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = tip,
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Natural Language Interactive Text Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userPrompt,
                    onValueChange = { userPrompt = it },
                    placeholder = {
                        Text(
                            "Ask AI: 'Schedule Math 6am-8am', 'How to study'...",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_assistant_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (userPrompt.isNotBlank() && !isLoading) {
                            val p = userPrompt
                            userPrompt = ""
                            onSendPrompt(p)
                        }
                    },
                    enabled = userPrompt.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (userPrompt.isNotBlank() && !isLoading) CyanPrimary else Slate800)
                        .testTag("ai_assistant_send_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CyanPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (userPrompt.isNotBlank()) Slate950 else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

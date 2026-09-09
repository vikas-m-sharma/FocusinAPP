package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.auth.AuthUser
import com.example.ui.theme.AmethystAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun PersonalizedDashboardCard(
    authUser: AuthUser?,
    streakDays: Int,
    focusedMinutes: Int,
    dailyGoalMinutes: Int,
    totalQuestionsAttempted: Int,
    totalQuestionsCorrect: Int,
    bookmarkedCount: Int,
    onSignInClick: () -> Unit,
    onSyncClick: () -> Unit,
    onViewProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSignedIn = authUser != null && authUser.isGoogleUser

    if (!isSignedIn) {
        // Unauthenticated Invitation Card to connect Google
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("personalized_dashboard_guest_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CyanPrimary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4285F4)
                            )
                        }
                        Column {
                            Text(
                                text = "Personalized Study Hub",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Google Account & Firebase Sync",
                                fontSize = 11.sp,
                                color = CyanPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "OFFLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Sign in with Google to unlock personalized NEET recommendations, cross-device timetable backup, and AI streak diagnostics.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("dashboard_sign_in_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Slate950
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connect Google Account",
                            color = Slate950,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    } else {
        // Authenticated Personalized Dashboard Card
        val user = authUser!!
        val accuracyPercent = if (totalQuestionsAttempted > 0) {
            (totalQuestionsCorrect * 100 / totalQuestionsAttempted)
        } else {
            0
        }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("personalized_dashboard_authenticated_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Slate800.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                // Header with Profile & Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Avatar
                        Box(contentAlignment = Alignment.BottomEnd) {
                            if (!user.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, CyanPrimary, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(CyanPrimary, AmethystAccent)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.displayName.firstOrNull()?.uppercase() ?: "S",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate950
                                    )
                                }
                            }

                            // Tiny Google badge
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Slate900, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4285F4)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified Student",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = user.email ?: "Google Account Connected",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Sync button
                    IconButton(
                        onClick = onSyncClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Slate800, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync Cloud",
                            tint = CyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cloud Status & Exam Target Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Text(
                            text = "Firebase Cloud Synced",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldSuccess
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "NEET 2026",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Personalized 4-Grid Study Performance Telemetry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = Color(0xFFF97316),
                        value = "$streakDays d",
                        label = "Streak",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        icon = Icons.Default.EmojiEvents,
                        iconTint = CyanPrimary,
                        value = if (totalQuestionsAttempted > 0) "$accuracyPercent%" else "--",
                        label = "Accuracy",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        icon = Icons.Default.CheckCircle,
                        iconTint = EmeraldSuccess,
                        value = "$totalQuestionsAttempted",
                        label = "Solved",
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        icon = Icons.Default.Bookmark,
                        iconTint = AmethystAccent,
                        value = "$bookmarkedCount",
                        label = "Saved",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate950)
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

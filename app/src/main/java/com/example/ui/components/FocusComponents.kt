package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900

@Composable
fun CircularFocusTimer(
    progress: Float,
    timeText: String,
    statusText: String = "FOCUS",
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    strokeWidth: Dp = 14.dp,
    activeColor: Color = CyanPrimary,
    backgroundColor: Color = Slate800,
    isPaused: Boolean = false,
    isBreak: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "timer_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val radius = diameter / 2f
            val center = this.center

            // Background Track
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokePx)
            )

            // Active Arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(activeColor.copy(alpha = 0.7f), activeColor, activeColor)
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                color = activeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = statusText.uppercase(),
                    color = activeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = timeText,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun FocusScoreCard(
    score: Int,
    goalMinutes: Int,
    focusedMinutes: Int,
    completedSessions: Int,
    distractions: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAILY FOCUS SCORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            score >= 85 -> "Exceptional Flow"
                            score >= 70 -> "High Productivity"
                            score >= 50 -> "Solid Progress"
                            else -> "Building Momentum"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = when {
                                score >= 80 -> EmeraldSuccess.copy(alpha = 0.15f)
                                score >= 50 -> CyanPrimary.copy(alpha = 0.15f)
                                else -> RoseError.copy(alpha = 0.15f)
                            },
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = "$score",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            score >= 80 -> EmeraldSuccess
                            score >= 50 -> CyanPrimary
                            else -> RoseError
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Metric Breakdown Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreMetricPill(
                    label = "Focused",
                    value = "${focusedMinutes / 60}h ${focusedMinutes % 60}m",
                    subtext = "Goal: ${goalMinutes / 60}h",
                    modifier = Modifier.weight(1f)
                )
                ScoreMetricPill(
                    label = "Sessions",
                    value = "$completedSessions",
                    subtext = "Completed",
                    modifier = Modifier.weight(1f)
                )
                ScoreMetricPill(
                    label = "Distractions",
                    value = "$distractions",
                    subtext = if (distractions == 0) "Zero alerts" else "Alerts",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ScoreMetricPill(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Slate850,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun StreakCard(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$streakDays DAY FOCUS STREAK",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (streakDays > 0) "Daily consistency protects your momentum." else "Complete a focus block today to begin.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EarlyEndSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit,
    plannedTimeText: String = "",
    actualTimeText: String = ""
) {
    var selectedReason by remember { mutableStateOf("Finished task early") }
    val reasons = listOf(
        "Finished task early",
        "Taking an unscheduled break",
        "Distracted / Lost focus",
        "Feeling fatigued",
        "Unexpected urgent task"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RoseError,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("End Session Early?", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "You still have time remaining. Logging your reason helps your AI Performance Coach analyze study patterns.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                reasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (selectedReason == reason),
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = reason, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                modifier = Modifier.testTag("confirm_end_session_button")
            ) {
                Text("End & Record")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_end_session_button")
            ) {
                Text("Continue Focus")
            }
        }
    )
}

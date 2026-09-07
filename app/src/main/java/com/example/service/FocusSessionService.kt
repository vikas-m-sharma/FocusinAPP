package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ActiveSessionState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val subjectId: Long = 0,
    val subjectName: String = "",
    val taskName: String = "",
    val plannedDurationMinutes: Int = 25,
    val remainingSeconds: Long = 25 * 60L,
    val elapsedSeconds: Long = 0L,
    val distractionCount: Int = 0,
    val isBreak: Boolean = false,
    val breakRemainingSeconds: Long = 0L,
    val mode: String = "COUNTDOWN", // "COUNTDOWN" or "STOPWATCH"
    val focusProtectionEnabled: Boolean = true,
    val blockedAppsCount: Int = 5,
    val isNaturallyCompleted: Boolean = false
)

class FocusSessionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var sessionStartTimeMs: Long = 0L
    private var pauseStartTimeMs: Long = 0L
    private var totalPausedMs: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Focusin::SessionWakeLock")
        wakeLock?.acquire(4 * 60 * 60 * 1000L) // 4 hours max
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val subjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, 0)
                val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: "Focus Session"
                val taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: "Deep Focus"
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                val mode = intent.getStringExtra(EXTRA_MODE) ?: "COUNTDOWN"
                val protection = intent.getBooleanExtra("EXTRA_PROTECTION", true)

                startSession(subjectId, subjectName, taskName, durationMinutes, mode, protection)
            }
            ACTION_PAUSE -> pauseSession()
            ACTION_RESUME -> resumeSession()
            ACTION_STOP -> stopSession()
            ACTION_RECORD_DISTRACTION -> recordDistraction()
        }
        return START_NOT_STICKY
    }

    private fun startSession(
        subjectId: Long,
        subjectName: String,
        taskName: String,
        durationMinutes: Int,
        mode: String,
        protection: Boolean = true
    ) {
        val totalSecs = durationMinutes * 60L
        sessionStartTimeMs = System.currentTimeMillis()
        pauseStartTimeMs = 0L
        totalPausedMs = 0L

        _sessionState.value = ActiveSessionState(
            isActive = true,
            isPaused = false,
            subjectId = subjectId,
            subjectName = subjectName,
            taskName = taskName,
            plannedDurationMinutes = durationMinutes,
            remainingSeconds = totalSecs,
            elapsedSeconds = 0L,
            distractionCount = 0,
            isBreak = false,
            mode = mode,
            focusProtectionEnabled = protection,
            blockedAppsCount = 5,
            isNaturallyCompleted = false
        )

        startForeground(NOTIFICATION_ID, buildNotification(_sessionState.value))
        startTimerLoop()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && _sessionState.value.isActive) {
                delay(1000L)
                val current = _sessionState.value
                if (!current.isPaused) {
                    val now = System.currentTimeMillis()
                    val actualActiveElapsedMs = (now - sessionStartTimeMs - totalPausedMs).coerceAtLeast(0L)
                    val activeElapsedSecs = actualActiveElapsedMs / 1000L

                    if (current.isBreak) {
                        val newBreakRem = (current.breakRemainingSeconds - 1).coerceAtLeast(0)
                        val isBreakDone = newBreakRem <= 0
                        _sessionState.value = current.copy(
                            breakRemainingSeconds = newBreakRem,
                            isBreak = !isBreakDone
                        )
                    } else {
                        val plannedSecs = current.plannedDurationMinutes * 60L
                        val newRemaining = if (current.mode == "COUNTDOWN") {
                            (plannedSecs - activeElapsedSecs).coerceAtLeast(0L)
                        } else {
                            0L
                        }
                        val isFinished = current.mode == "COUNTDOWN" && newRemaining <= 0L

                        _sessionState.value = current.copy(
                            elapsedSeconds = activeElapsedSecs,
                            remainingSeconds = newRemaining,
                            isNaturallyCompleted = isFinished
                        )

                        if (isFinished) {
                            // Session naturally finished
                            updateNotification(_sessionState.value)
                            break
                        }
                    }
                    updateNotification(_sessionState.value)
                }
            }
        }
    }

    private fun pauseSession() {
        if (!_sessionState.value.isPaused) {
            pauseStartTimeMs = System.currentTimeMillis()
            _sessionState.value = _sessionState.value.copy(isPaused = true)
            updateNotification(_sessionState.value)
        }
    }

    private fun resumeSession() {
        if (_sessionState.value.isPaused) {
            if (pauseStartTimeMs > 0L) {
                totalPausedMs += (System.currentTimeMillis() - pauseStartTimeMs)
                pauseStartTimeMs = 0L
            }
            _sessionState.value = _sessionState.value.copy(isPaused = false)
            updateNotification(_sessionState.value)
        }
    }

    private fun stopSession() {
        timerJob?.cancel()
        _sessionState.value = _sessionState.value.copy(isActive = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun recordDistraction() {
        val current = _sessionState.value
        if (current.isActive) {
            _sessionState.value = current.copy(distractionCount = current.distractionCount + 1)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_focus),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_focus_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(state: ActiveSessionState): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeText = if (state.mode == "COUNTDOWN") {
            formatTime(state.remainingSeconds)
        } else {
            formatTime(state.elapsedSeconds)
        }

        val pauseResumeAction = if (state.isPaused) {
            val resumeIntent = PendingIntent.getService(
                this,
                1,
                Intent(this, FocusSessionService::class.java).apply { action = ACTION_RESUME },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(0, "Resume", resumeIntent).build()
        } else {
            val pauseIntent = PendingIntent.getService(
                this,
                2,
                Intent(this, FocusSessionService::class.java).apply { action = ACTION_PAUSE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(0, "Pause", pauseIntent).build()
        }

        val stopIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, FocusSessionService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(0, "End", stopIntent).build()

        val title = if (state.isBreak) "Break Time" else "${state.subjectName}: ${state.taskName}"
        val subtext = if (state.isPaused) "Paused • $timeText" else "Focusing • $timeText"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(subtext)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(pauseResumeAction)
            .addAction(stopAction)
            .build()
    }

    private fun updateNotification(state: ActiveSessionState) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        val h = m / 60
        val remM = m % 60
        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, remM, s)
        } else {
            String.format("%02d:%02d", remM, s)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    companion object {
        const val CHANNEL_ID = "focusin_focus_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.focusin.action.START"
        const val ACTION_PAUSE = "com.example.focusin.action.PAUSE"
        const val ACTION_RESUME = "com.example.focusin.action.RESUME"
        const val ACTION_STOP = "com.example.focusin.action.STOP"
        const val ACTION_RECORD_DISTRACTION = "com.example.focusin.action.RECORD_DISTRACTION"

        const val EXTRA_SUBJECT_ID = "extra_subject_id"
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"
        const val EXTRA_TASK_NAME = "extra_task_name"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
        const val EXTRA_MODE = "extra_mode"

        private val _sessionState = MutableStateFlow(ActiveSessionState())
        val sessionState: StateFlow<ActiveSessionState> = _sessionState.asStateFlow()

        fun recordDistractionGlobal() {
            val current = _sessionState.value
            if (current.isActive) {
                _sessionState.value = current.copy(distractionCount = current.distractionCount + 1)
            }
        }

        fun startBreak(breakMinutes: Int) {
            val current = _sessionState.value
            if (current.isActive) {
                _sessionState.value = current.copy(
                    isBreak = true,
                    breakRemainingSeconds = breakMinutes * 60L
                )
            }
        }

        fun endBreak() {
            val current = _sessionState.value
            if (current.isActive) {
                _sessionState.value = current.copy(
                    isBreak = false,
                    breakRemainingSeconds = 0L
                )
            }
        }
    }
}

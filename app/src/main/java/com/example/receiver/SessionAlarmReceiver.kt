package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getLongExtra("EXTRA_SESSION_ID", 0)
        val subjectId = intent.getLongExtra("EXTRA_SUBJECT_ID", 0)
        val subjectName = intent.getStringExtra("EXTRA_SUBJECT_NAME") ?: "Focus Session"
        val taskName = intent.getStringExtra("EXTRA_TASK_NAME") ?: "Core Study Block"
        val startTime = intent.getStringExtra("EXTRA_START_TIME") ?: "06:00"
        val endTime = intent.getStringExtra("EXTRA_END_TIME") ?: "08:00"
        val durationMinutes = intent.getIntExtra("EXTRA_DURATION_MINUTES", 120)
        val voiceNoteId = intent.getLongExtra("EXTRA_VOICE_NOTE_ID", -1)
        val soundUriString = intent.getStringExtra("EXTRA_SOUND_URI")

        createReminderChannel(context)

        // 1. Immediately activate AlarmRingingManager to play ringing audio & vibration
        val sessionInfo = RingingSessionInfo(
            sessionId = sessionId,
            subjectId = subjectId,
            subjectName = subjectName,
            taskName = taskName,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            soundUri = soundUriString
        )
        AlarmRingingManager.startRinging(context, sessionInfo)

        // Intent to open App with Ringing Dialog / Start focus
        val startFocusIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ALARM_IS_RINGING", true)
            putExtra("EXTRA_START_SESSION_ID", sessionId)
            putExtra("EXTRA_SUBJECT_ID", subjectId)
            putExtra("EXTRA_SUBJECT_NAME", subjectName)
            putExtra("EXTRA_TASK_NAME", taskName)
            putExtra("EXTRA_START_TIME", startTime)
            putExtra("EXTRA_END_TIME", endTime)
            putExtra("EXTRA_DURATION_MINUTES", durationMinutes)
            putExtra("EXTRA_AUTO_START_FOCUS", true)
        }

        val startPendingIntent = PendingIntent.getActivity(
            context,
            sessionId.toInt(),
            startFocusIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = if (!soundUriString.isNullOrBlank()) {
            try {
                Uri.parse(soundUriString)
            } catch (_: Exception) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ Timetable Ringing — $subjectName")
            .setContentText("$startTime to $endTime • $taskName")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Your scheduled study block is ringing: $subjectName ($startTime - $endTime). Tap to open ringing alert, start focus, and shield distracting apps."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 600, 300, 600, 300, 1000))
            .setFullScreenIntent(startPendingIntent, true)
            .setAutoCancel(true)
            .setContentIntent(startPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Start Focus & Lock", startPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((2000 + sessionId).toInt(), notification)

        // Reschedule alarm for the next recurrence (next week)
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            if (alarmManager != null) {
                val nextWeekCal = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, 7)
                    val timeParts = startTime.split(":")
                    if (timeParts.size == 2) {
                        set(java.util.Calendar.HOUR_OF_DAY, timeParts[0].toIntOrNull() ?: 6)
                        set(java.util.Calendar.MINUTE, timeParts[1].toIntOrNull() ?: 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                }
                val repeatIntent = Intent(context, SessionAlarmReceiver::class.java).apply {
                    putExtra("EXTRA_SESSION_ID", sessionId)
                    putExtra("EXTRA_SUBJECT_ID", subjectId)
                    putExtra("EXTRA_SUBJECT_NAME", subjectName)
                    putExtra("EXTRA_TASK_NAME", taskName)
                    putExtra("EXTRA_START_TIME", startTime)
                    putExtra("EXTRA_END_TIME", endTime)
                    putExtra("EXTRA_DURATION_MINUTES", durationMinutes)
                    putExtra("EXTRA_VOICE_NOTE_ID", voiceNoteId)
                    putExtra("EXTRA_SOUND_URI", soundUriString)
                }
                val repeatPendingIntent = PendingIntent.getBroadcast(
                    context,
                    sessionId.toInt(),
                    repeatIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val clockInfo = android.app.AlarmManager.AlarmClockInfo(nextWeekCal.timeInMillis, repeatPendingIntent)
                    alarmManager.setAlarmClock(clockInfo, repeatPendingIntent)
                }
            }
        } catch (_: Exception) {}

        // If a personal motivational voice note is attached, play it
        if (voiceNoteId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val note = db.voiceRecordingDao().getRecordingById(voiceNoteId)
                    if (note != null) {
                        try {
                            val player = android.media.MediaPlayer()
                            player.setDataSource(note.filePath)
                            player.prepare()
                            player.start()
                        } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun createReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_reminders_desc)
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "focusin_reminders_channel"
    }
}

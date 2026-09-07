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
        val subjectName = intent.getStringExtra("EXTRA_SUBJECT_NAME") ?: "Focus Session"
        val taskName = intent.getStringExtra("EXTRA_TASK_NAME") ?: "Core Study Block"
        val voiceNoteId = intent.getLongExtra("EXTRA_VOICE_NOTE_ID", -1)
        val soundUriString = intent.getStringExtra("EXTRA_SOUND_URI")

        createReminderChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_START_SESSION_ID", sessionId)
            putExtra("EXTRA_SUBJECT_NAME", subjectName)
            putExtra("EXTRA_TASK_NAME", taskName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            sessionId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = if (!soundUriString.isNullOrBlank()) {
            try {
                Uri.parse(soundUriString)
            } catch (_: Exception) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Study Time — $subjectName")
            .setContentText("$taskName. Your planned focus block is ready.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Time to focus: $taskName. Open Focusin to start distraction-free."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((2000 + sessionId).toInt(), notification)

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

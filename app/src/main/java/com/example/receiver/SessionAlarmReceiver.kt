package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
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

        Log.d("SessionAlarmReceiver", "Session alarm triggered for $subjectName - $taskName (ID: $sessionId)")

        createReminderChannel(context)

        // Activity intent when notification clicked or launched full screen
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_START_SESSION_ID", sessionId)
            putExtra("EXTRA_SUBJECT_NAME", subjectName)
            putExtra("EXTRA_TASK_NAME", taskName)
            putExtra("EXTRA_TIMETABLE_RINGING", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (sessionId + 20000).toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri: Uri = if (!soundUriString.isNullOrBlank()) {
            try {
                Uri.parse(soundUriString)
            } catch (_: Exception) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ TIMETABLE SESSION STARTED — $subjectName")
            .setContentText("Scheduled study block started: $taskName. Open app to focus!")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Time to focus on $subjectName ($taskName)! Distraction blocking active."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 800, 400, 800, 400, 1000))
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.mipmap.ic_launcher,
                "Start Session Now",
                pendingIntent
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((2000 + sessionId).toInt(), notification)

        // Try playing alarm ringtone directly
        try {
            val ringtone = RingtoneManager.getRingtone(context, soundUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("SessionAlarmReceiver", "Error playing ringtone", e)
        }

        // If a personal motivational voice note is attached, play it
        if (voiceNoteId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val note = db.voiceRecordingDao().getRecordingById(voiceNoteId)
                    if (note != null) {
                        try {
                            val player = MediaPlayer()
                            player.setDataSource(note.filePath)
                            player.prepare()
                            player.start()
                        } catch (e: Exception) {
                            Log.e("SessionAlarmReceiver", "Error playing voice note", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SessionAlarmReceiver", "Error fetching voice note", e)
                }
            }
        }
    }

    private fun createReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_reminders_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 1000)
                setSound(soundUri, audioAttributes)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "focusin_reminders_channel"
    }
}

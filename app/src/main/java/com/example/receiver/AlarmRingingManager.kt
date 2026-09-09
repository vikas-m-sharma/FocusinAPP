package com.example.receiver

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RingingSessionInfo(
    val sessionId: Long = 0,
    val subjectId: Long = 0,
    val subjectName: String = "Mathematics",
    val taskName: String = "Core Study Block",
    val startTime: String = "06:00",
    val endTime: String = "08:00",
    val durationMinutes: Int = 120,
    val colorHex: String = "#38BDF8",
    val soundUri: String? = null
)

object AlarmRingingManager {
    private const val TAG = "AlarmRingingManager"
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private val _isRinging = MutableStateFlow(false)
    val isRinging: StateFlow<Boolean> = _isRinging.asStateFlow()

    private val _currentRingingSession = MutableStateFlow<RingingSessionInfo?>(null)
    val currentRingingSession: StateFlow<RingingSessionInfo?> = _currentRingingSession.asStateFlow()

    fun startRinging(context: Context, session: RingingSessionInfo) {
        try {
            stopRinging() // stop previous instance if any
            _currentRingingSession.value = session
            _isRinging.value = true

            val soundUri = if (!session.soundUri.isNullOrBlank()) {
                try {
                    Uri.parse(session.soundUri)
                } catch (_: Exception) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                }
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }

            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator = vib

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 600, 300, 600, 300, 1000)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vib?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
            } else {
                @Suppress("DEPRECATION")
                vib?.vibrate(longArrayOf(0, 600, 300, 600, 300, 1000), 0)
            }

            Log.d(TAG, "Started alarm ringing for ${session.subjectName} (${session.startTime} - ${session.endTime})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start alarm ringing", e)
        }
    }

    fun stopRinging() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null

            vibrator?.cancel()
            vibrator = null

            _isRinging.value = false
            _currentRingingSession.value = null
            Log.d(TAG, "Alarm ringing stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm ringing", e)
            _isRinging.value = false
            _currentRingingSession.value = null
        }
    }
}

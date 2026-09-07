package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class VoiceRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTime = 0L

    var isRecording = false
        private set
    var isPlaying = false
        private set
    var playingFilePath: String? = null
        private set

    fun startRecording(): Boolean {
        try {
            stopPlaying()
            val audioDir = File(context.filesDir, "voice_notes")
            if (!audioDir.exists()) audioDir.mkdirs()

            val file = File(audioDir, "motivation_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = file

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            return true
        } catch (e: Exception) {
            Log.e("VoiceRecorderManager", "Error starting recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            return false
        }
    }

    fun stopRecording(): Pair<File?, Int> {
        if (!isRecording) return Pair(null, 0)
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            val durationSec = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
            Pair(currentRecordingFile, durationSec.coerceAtLeast(1))
        } catch (e: Exception) {
            Log.e("VoiceRecorderManager", "Error stopping recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            Pair(null, 0)
        }
    }

    fun playAudio(filePath: String, onComplete: () -> Unit = {}) {
        try {
            stopPlaying()
            val file = File(filePath)
            if (!file.exists()) {
                Log.w("VoiceRecorderManager", "File does not exist: $filePath")
                return
            }
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    this@VoiceRecorderManager.isPlaying = false
                    this@VoiceRecorderManager.playingFilePath = null
                    onComplete()
                }
                start()
            }
            isPlaying = true
            playingFilePath = filePath
        } catch (e: IOException) {
            Log.e("VoiceRecorderManager", "Error playing audio", e)
            stopPlaying()
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("VoiceRecorderManager", "Error releasing player", e)
        } finally {
            mediaPlayer = null
            isPlaying = false
            playingFilePath = null
        }
    }

    fun deleteAudioFile(filePath: String): Boolean {
        return try {
            if (playingFilePath == filePath) {
                stopPlaying()
            }
            val file = File(filePath)
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            Log.e("VoiceRecorderManager", "Error deleting audio file", e)
            false
        }
    }

    fun release() {
        if (isRecording) {
            stopRecording()
        }
        stopPlaying()
    }
}

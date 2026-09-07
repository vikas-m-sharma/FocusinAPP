package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_recordings")
data class VoiceRecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val durationSeconds: Int,
    val createdAtMillis: Long = System.currentTimeMillis()
)

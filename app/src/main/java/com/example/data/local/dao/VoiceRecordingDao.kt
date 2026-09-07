package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.VoiceRecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceRecordingDao {
    @Query("SELECT * FROM voice_recordings ORDER BY createdAtMillis DESC")
    fun getAllRecordings(): Flow<List<VoiceRecordingEntity>>

    @Query("SELECT * FROM voice_recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): VoiceRecordingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: VoiceRecordingEntity): Long

    @Update
    suspend fun updateRecording(recording: VoiceRecordingEntity)

    @Delete
    suspend fun deleteRecording(recording: VoiceRecordingEntity)

    @Query("DELETE FROM voice_recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: Long)
}

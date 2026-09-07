package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TimetableSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_sessions ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSessions(): Flow<List<TimetableSessionEntity>>

    @Query("SELECT * FROM timetable_sessions WHERE dayOfWeek = :dayOfWeek AND isEnabled = 1 ORDER BY startTime ASC")
    fun getSessionsForDay(dayOfWeek: Int): Flow<List<TimetableSessionEntity>>

    @Query("SELECT * FROM timetable_sessions WHERE dayOfWeek = :dayOfWeek AND isEnabled = 1 ORDER BY startTime ASC")
    suspend fun getSessionsForDaySync(dayOfWeek: Int): List<TimetableSessionEntity>

    @Query("SELECT * FROM timetable_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): TimetableSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TimetableSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<TimetableSessionEntity>)

    @Update
    suspend fun updateSession(session: TimetableSessionEntity)

    @Delete
    suspend fun deleteSession(session: TimetableSessionEntity)

    @Query("DELETE FROM timetable_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM timetable_sessions")
    suspend fun deleteAllSessions()
}

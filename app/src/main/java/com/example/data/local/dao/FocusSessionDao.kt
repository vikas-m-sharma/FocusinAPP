package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FocusSessionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_session_records ORDER BY startTimeMillis DESC")
    fun getAllRecords(): Flow<List<FocusSessionRecordEntity>>

    @Query("SELECT * FROM focus_session_records ORDER BY startTimeMillis DESC")
    suspend fun getAllRecordsList(): List<FocusSessionRecordEntity>

    @Query("SELECT * FROM focus_session_records WHERE dateString = :dateString ORDER BY startTimeMillis ASC")
    fun getRecordsForDate(dateString: String): Flow<List<FocusSessionRecordEntity>>

    @Query("SELECT * FROM focus_session_records WHERE dateString = :dateString ORDER BY startTimeMillis ASC")
    suspend fun getRecordsForDateSync(dateString: String): List<FocusSessionRecordEntity>

    @Query("SELECT * FROM focus_session_records WHERE dateString IN (:dates) ORDER BY startTimeMillis ASC")
    suspend fun getRecordsForDates(dates: List<String>): List<FocusSessionRecordEntity>

    @Query("SELECT * FROM focus_session_records WHERE subjectId = :subjectId")
    fun getRecordsForSubject(subjectId: Long): Flow<List<FocusSessionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FocusSessionRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<FocusSessionRecordEntity>)

    @Query("DELETE FROM focus_session_records")
    suspend fun deleteAll()
}

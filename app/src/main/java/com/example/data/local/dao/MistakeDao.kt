package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MistakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MistakeDao {

    @Query("SELECT * FROM mistake_entries ORDER BY timestamp DESC")
    fun getAllMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistake_entries WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getUnresolvedMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistake_entries WHERE errorReason = :reason AND isResolved = 0 ORDER BY timestamp DESC")
    fun getMistakesByReason(reason: String): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistake_entries WHERE subjectName = :subjectName AND isResolved = 0 ORDER BY timestamp DESC")
    fun getMistakesBySubject(subjectName: String): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistake_entries WHERE id = :id LIMIT 1")
    suspend fun getMistakeById(id: String): MistakeEntity?

    @Query("SELECT * FROM mistake_entries WHERE questionId = :questionId LIMIT 1")
    suspend fun getMistakeByQuestionId(questionId: String): MistakeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistakes(mistakes: List<MistakeEntity>)

    @Update
    suspend fun updateMistake(mistake: MistakeEntity)

    @Query("UPDATE mistake_entries SET errorReason = :reason, studentNotes = CASE WHEN :notes IS NOT NULL THEN :notes ELSE studentNotes END WHERE id = :id")
    suspend fun updateMistakeReason(id: String, reason: String, notes: String?)

    @Query("UPDATE mistake_entries SET isResolved = :isResolved WHERE id = :id")
    suspend fun markResolved(id: String, isResolved: Boolean)

    @Query("UPDATE mistake_entries SET reattemptCount = reattemptCount + 1, isResolved = :isResolved WHERE id = :id")
    suspend fun recordReattempt(id: String, isResolved: Boolean)

    @Delete
    suspend fun deleteMistake(mistake: MistakeEntity)

    @Query("DELETE FROM mistake_entries WHERE id = :id")
    suspend fun deleteMistakeById(id: String)

    @Query("DELETE FROM mistake_entries")
    suspend fun deleteAllMistakes()
}

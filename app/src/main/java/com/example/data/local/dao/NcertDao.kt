package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.NcertProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NcertDao {

    @Query("SELECT * FROM ncert_progress WHERE chapterId = :chapterId LIMIT 1")
    fun getProgressForChapter(chapterId: String): Flow<NcertProgressEntity?>

    @Query("SELECT * FROM ncert_progress WHERE chapterId = :chapterId LIMIT 1")
    suspend fun getProgressForChapterSync(chapterId: String): NcertProgressEntity?

    @Query("SELECT * FROM ncert_progress WHERE bookId = :bookId")
    fun getProgressForBook(bookId: String): Flow<List<NcertProgressEntity>>

    @Query("SELECT * FROM ncert_progress ORDER BY lastOpenedAt DESC")
    fun getAllProgress(): Flow<List<NcertProgressEntity>>

    @Query("SELECT * FROM ncert_progress WHERE lastOpenedAt > 0 ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun getRecentProgress(limit: Int = 5): Flow<List<NcertProgressEntity>>

    @Query("SELECT * FROM ncert_progress WHERE isBookmarked = 1 ORDER BY lastOpenedAt DESC")
    fun getBookmarkedChapters(): Flow<List<NcertProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: NcertProgressEntity)

    @Query("UPDATE ncert_progress SET isBookmarked = :isBookmarked, lastOpenedAt = :timestamp WHERE chapterId = :chapterId")
    suspend fun updateBookmarkState(chapterId: String, isBookmarked: Boolean, timestamp: Long)

    @Query("UPDATE ncert_progress SET lastPageRead = :page, totalPages = :totalPages, progressPercentage = :progressPercentage, lastOpenedAt = :timestamp WHERE chapterId = :chapterId")
    suspend fun updatePageRead(
        chapterId: String,
        page: Int,
        totalPages: Int,
        progressPercentage: Int,
        timestamp: Long
    )

    @Query("UPDATE ncert_progress SET isCompleted = :isCompleted, lastOpenedAt = :timestamp WHERE chapterId = :chapterId")
    suspend fun updateCompletionState(chapterId: String, isCompleted: Boolean, timestamp: Long)
}

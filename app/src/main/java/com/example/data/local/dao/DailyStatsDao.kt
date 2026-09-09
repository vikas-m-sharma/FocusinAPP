package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats WHERE dateString = :dateString")
    fun getStatsForDate(dateString: String): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats WHERE dateString = :dateString")
    suspend fun getStatsForDateSync(dateString: String): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats ORDER BY dateString DESC LIMIT 7")
    fun getRecentWeekStats(): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats ORDER BY dateString DESC LIMIT 30")
    fun getRecentMonthStats(): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats ORDER BY dateString DESC LIMIT 365")
    fun getRecentYearStats(): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats ORDER BY dateString DESC LIMIT 30")
    suspend fun getRecentMonthStatsSync(): List<DailyStatsEntity>

    @Query("SELECT * FROM daily_stats ORDER BY dateString DESC")
    fun getAllStats(): Flow<List<DailyStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: DailyStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statsList: List<DailyStatsEntity>)

    @Query("DELETE FROM daily_stats")
    suspend fun deleteAll()
}

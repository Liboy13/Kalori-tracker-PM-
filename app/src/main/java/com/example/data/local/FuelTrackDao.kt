package com.example.data.local

import androidx.room.*
import com.example.data.model.DailyProgress
import com.example.data.model.FoodLog
import com.example.data.model.WeightRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelTrackDao {

    // --- FOOD LOGS ---
    @Query("SELECT * FROM food_logs WHERE dateString = :date ORDER BY id ASC")
    fun getFoodLogsForDate(date: String): Flow<List<FoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(log: FoodLog): Long

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLog(id: Long)

    @Query("DELETE FROM food_logs")
    suspend fun deleteAllFoodLogs()

    // --- DAILY PROGRESS ---
    @Query("SELECT * FROM daily_progress WHERE dateString = :date LIMIT 1")
    fun getDailyProgressForDate(date: String): Flow<DailyProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyProgress(progress: DailyProgress)

    // --- WEIGHT RECORDS ---
    @Query("SELECT * FROM weight_records ORDER BY timestamp ASC")
    fun getAllWeightRecords(): Flow<List<WeightRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightRecord(record: WeightRecord): Long

    @Query("DELETE FROM weight_records WHERE id = :id")
    suspend fun deleteWeightRecord(id: Long)

    @Query("DELETE FROM weight_records")
    suspend fun deleteAllWeightRecords()
}

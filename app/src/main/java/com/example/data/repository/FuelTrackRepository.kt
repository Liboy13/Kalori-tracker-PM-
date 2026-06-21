package com.example.data.repository

import com.example.data.local.FuelTrackDao
import com.example.data.model.DailyProgress
import com.example.data.model.FoodLog
import com.example.data.model.WeightRecord
import kotlinx.coroutines.flow.Flow

class FuelTrackRepository(private val dao: FuelTrackDao) {

    fun getFoodLogsForDate(date: String): Flow<List<FoodLog>> {
        return dao.getFoodLogsForDate(date)
    }

    suspend fun insertFoodLog(log: FoodLog): Long {
        return dao.insertFoodLog(log)
    }

    suspend fun deleteFoodLog(id: Long) {
        dao.deleteFoodLog(id)
    }

    suspend fun deleteAllFoodLogs() {
        dao.deleteAllFoodLogs()
    }

    fun getDailyProgressForDate(date: String): Flow<DailyProgress?> {
        return dao.getDailyProgressForDate(date)
    }

    suspend fun insertDailyProgress(progress: DailyProgress) {
        dao.insertDailyProgress(progress)
    }

    fun getAllWeightRecords(): Flow<List<WeightRecord>> {
        return dao.getAllWeightRecords()
    }

    suspend fun insertWeightRecord(record: WeightRecord): Long {
        return dao.insertWeightRecord(record)
    }

    suspend fun deleteWeightRecord(id: Long) {
        dao.deleteWeightRecord(id)
    }

    suspend fun deleteAllWeightRecords() {
        dao.deleteAllWeightRecords()
    }
}

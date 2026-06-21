package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.DailyProgress
import com.example.data.model.FoodLog
import com.example.data.model.WeightRecord

@Database(
    entities = [FoodLog::class, DailyProgress::class, WeightRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fuelTrackDao(): FuelTrackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fuel_track_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

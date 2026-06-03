package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChargingStation::class, SlotBooking::class, UserStats::class, NotificationAlert::class],
    version = 1,
    exportSchema = false
)
abstract class ChargeBeesDatabase : RoomDatabase() {
    abstract fun chargeBeesDao(): ChargeBeesDao

    companion object {
        @Volatile
        private var INSTANCE: ChargeBeesDatabase? = null

        fun getDatabase(context: Context): ChargeBeesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChargeBeesDatabase::class.java,
                    "chargebees_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

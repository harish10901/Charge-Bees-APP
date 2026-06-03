package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeBeesDao {

    @Query("SELECT * FROM charging_stations ORDER BY distance ASC")
    fun getStations(): Flow<List<ChargingStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<ChargingStation>)

    @Query("DELETE FROM charging_stations")
    suspend fun clearStations()

    @Query("SELECT * FROM slot_bookings ORDER BY bookingTimestamp DESC")
    fun getBookings(): Flow<List<SlotBooking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: SlotBooking): Long

    @Update
    suspend fun updateBooking(booking: SlotBooking)

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    @Query("SELECT * FROM notification_alerts ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(alert: NotificationAlert)

    @Query("DELETE FROM notification_alerts WHERE id = :id")
    suspend fun deleteNotification(id: Int)
}

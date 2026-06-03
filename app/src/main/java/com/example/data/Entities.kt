package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_stations")
data class ChargingStation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val chargeRate: Double, // in ₹ or $ per kWh
    val fastChargerCount: Int,
    val slowChargerCount: Int,
    val isAvailable: Boolean,
    val distance: Double // in km
)

@Entity(tableName = "slot_bookings")
data class SlotBooking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationId: Int,
    val stationName: String,
    val vehicleNumber: String,
    val chargerType: String, // CCS-2, Type-2, CHAdeMO
    val bookingDate: String,
    val bookingTime: String,
    var status: String, // CONFIRMED, CHARGING, COMPLETED, CANCELLED
    val paymentAmount: Double,
    val paymentStatus: String, // PAID, PENDING
    val bookingTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // Single user row
    val email: String = "vardhan.harsha9391@gmail.com",
    val name: String = "Harsha Vardhan",
    val phone: String = "9000040477",
    val biometricEnabled: Boolean = false,
    val currentVehicle: String = "Nexon EV Max",
    val totalPowerSecured: Double = 340.5, // kWh
    val totalSpend: Double = 5120.0,
    val bookingCount: Int = 12,
    val co2SavedKg: Double = 412.3
)

@Entity(tableName = "notification_alerts")
data class NotificationAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// Helper class for UI representation of dynamic power charts
data class PowerStatPoint(
    val intervalLabel: String,
    val kwhValue: Float,
    val timestamp: Long
)

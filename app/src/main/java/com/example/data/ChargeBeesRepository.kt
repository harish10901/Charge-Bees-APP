package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class ChargeBeesRepository(private val dao: ChargeBeesDao) {

    val stations: Flow<List<ChargingStation>> = dao.getStations()
    val bookings: Flow<List<SlotBooking>> = dao.getBookings()
    val userStats: Flow<UserStats?> = dao.getUserStats()
    val notifications: Flow<List<NotificationAlert>> = dao.getNotifications()

    suspend fun prepopulateIfEmpty() {
        // Run checks
        val currentStations = dao.getStations().first()
        if (currentStations.isEmpty()) {
            val starterStations = listOf(
                ChargingStation(
                    name = "Charge Bees Gachibowli Hub",
                    address = "Gachibowli IT Park Phase 2, Near DLF, Hyderabad",
                    latitude = 17.4435,
                    longitude = 78.3489,
                    chargeRate = 18.5, // ₹18.5 per Unit (kWh)
                    fastChargerCount = 6,
                    slowChargerCount = 4,
                    isAvailable = true,
                    distance = 1.2
                ),
                ChargingStation(
                    name = "Charge Bees Madhapur Station",
                    address = "Hitech City Metro Plaza, Madhapur, Hyderabad",
                    latitude = 17.4483,
                    longitude = 78.3741,
                    chargeRate = 19.0,
                    fastChargerCount = 8,
                    slowChargerCount = 2,
                    isAvailable = true,
                    distance = 2.4
                ),
                ChargingStation(
                    name = "Charge Bees Jubilee Hills Suite",
                    address = "Road No. 36 Metro Station, Jubilee Hills, Hyderabad",
                    latitude = 17.4284,
                    longitude = 78.4116,
                    chargeRate = 21.0,
                    fastChargerCount = 4,
                    slowChargerCount = 4,
                    isAvailable = true,
                    distance = 4.8
                ),
                ChargingStation(
                    name = "Charge Bees Shamshabad Transit",
                    address = "ORR Exit 16, Shamshabad International Airport, Hyderabad",
                    latitude = 17.2514,
                    longitude = 78.4357,
                    chargeRate = 22.5,
                    fastChargerCount = 12,
                    slowChargerCount = 6,
                    isAvailable = true,
                    distance = 18.7
                ),
                ChargingStation(
                    name = "Charge Bees Vijayawada Express Hub",
                    address = "Benz Circle, near Highway NH-16, Vijayawada",
                    latitude = 16.5050,
                    longitude = 80.6480,
                    chargeRate = 17.0,
                    fastChargerCount = 8,
                    slowChargerCount = 4,
                    isAvailable = true,
                    distance = 12.3
                ),
                ChargingStation(
                    name = "Charge Bees RK Beach Point",
                    address = "RK Beach Road, opposite Marine Aquarium, Visakhapatnam",
                    latitude = 17.7123,
                    longitude = 83.3245,
                    chargeRate = 18.0,
                    fastChargerCount = 4,
                    slowChargerCount = 2,
                    isAvailable = false,
                    distance = 32.5
                ),
                ChargingStation(
                    name = "Charge Bees ORR Outer Ring Station",
                    address = "Marathahalli Multiplex Junction, Bengaluru",
                    latitude = 12.9562,
                    longitude = 77.7011,
                    chargeRate = 19.5,
                    fastChargerCount = 10,
                    slowChargerCount = 5,
                    isAvailable = true,
                    distance = 450.0
                )
            )
            dao.insertStations(starterStations)
        }

        val currentUserStats = dao.getUserStats().firstOrNull()
        if (currentUserStats == null) {
            dao.insertUserStats(
                UserStats(
                    email = "vardhan.harsha9391@gmail.com",
                    name = "Harsha Vardhan",
                    phone = "9000040477",
                    biometricEnabled = true,
                    currentVehicle = "Tata Nexon EV Max",
                    totalPowerSecured = 480.2, // kWh
                    totalSpend = 8960.0, // ₹
                    bookingCount = 15,
                    co2SavedKg = 530.5
                )
            )
        }

        val notificationsList = dao.getNotifications().first()
        if (notificationsList.isEmpty()) {
            dao.insertNotification(
                NotificationAlert(
                    title = "Welcome to Charge Bees!",
                    message = "Earn bees-coins on your first slot booking! High-voltage fast chargers are active in Gachibowli and Shamshabad ORR.",
                    timestamp = System.currentTimeMillis() - 3600000 * 3
                )
            )
        }
    }

    suspend fun createBooking(
        stationId: Int,
        stationName: String,
        vehicleNumber: String,
        chargerType: String,
        bookingDate: String,
        bookingTime: String,
        paymentAmount: Double
    ): Long {
        val booking = SlotBooking(
            stationId = stationId,
            stationName = stationName,
            vehicleNumber = vehicleNumber,
            chargerType = chargerType,
            bookingDate = bookingDate,
            bookingTime = bookingTime,
            status = "CONFIRMED",
            paymentAmount = paymentAmount,
            paymentStatus = "PAID"
        )
        val bookingId = dao.insertBooking(booking)

        // Increment stats
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStats()
        val updatedStats = currentStats.copy(
            bookingCount = currentStats.bookingCount + 1,
            totalSpend = currentStats.totalSpend + paymentAmount
        )
        dao.insertUserStats(updatedStats)

        // Trigger in-app notification
        dao.insertNotification(
            NotificationAlert(
                title = "Booking Confirmed ⚡🐝",
                message = "Your slot is reserved at $stationName for $bookingTime on $bookingDate. Spot Code: CB-${Random.nextInt(100, 999)}.",
                timestamp = System.currentTimeMillis()
            )
        )

        return bookingId
    }

    suspend fun updateBookingStatus(bookingId: Int, status: String) {
        // Query to find and update booking
        val currentBookings = dao.getBookings().first()
        val matching = currentBookings.find { it.id == bookingId }
        if (matching != null) {
            val updated = matching.copy(status = status)
            dao.updateBooking(updated)

            if (status == "COMPLETED") {
                val addedKwh = Random.nextDouble(18.0, 45.0)
                val currentStats = dao.getUserStats().firstOrNull() ?: UserStats()
                val updatedStats = currentStats.copy(
                    totalPowerSecured = currentStats.totalPowerSecured + addedKwh,
                    co2SavedKg = currentStats.co2SavedKg + (addedKwh * 1.1) // 1.1 kg CO2 saved per kWh compared to petrol
                )
                dao.insertUserStats(updatedStats)

                dao.insertNotification(
                    NotificationAlert(
                        title = "Charging Session Completed! 🎉",
                        message = "Success! Added ${String.format("%.1f", addedKwh)} kWh to your battery. Saved ${String.format("%.1f", addedKwh * 1.1)} kg of CO2 emission indices.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun triggerLiveChargingProgress(bookingId: Int, percent: Int) {
        if (percent == 80) {
            dao.insertNotification(
                NotificationAlert(
                    title = "Battery is 80% Charged ⚡",
                    message = "Your Nexon EV has reached 80% charging state. Ready to resume high speed travel!",
                    timestamp = System.currentTimeMillis()
                )
            )
        } else if (percent == 100) {
            dao.insertNotification(
                NotificationAlert(
                    title = "Battery Fully Charged! 🐝🎉",
                    message = "Nexon EV is completely loaded (100%). Please unplug within 10 minutes to avoid idle charger fee.",
                    timestamp = System.currentTimeMillis()
                )
            )
            updateBookingStatus(bookingId, "COMPLETED")
        }
    }

    suspend fun updateBiometricChoice(enabled: Boolean) {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStats()
        dao.insertUserStats(currentStats.copy(biometricEnabled = enabled))
    }

    suspend fun deleteNotification(id: Int) {
        dao.deleteNotification(id)
    }

    suspend fun postNotification(title: String, message: String) {
        dao.insertNotification(NotificationAlert(title = title, message = message))
    }
}

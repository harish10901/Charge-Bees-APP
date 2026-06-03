package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class NavigationState(
    val nextInstruction: String,
    val distanceRemainingKm: Double,
    val timeRemainingMin: Int,
    val currentSpeedKmh: Int,
    val pathRouteIndex: Int,
    val batteryPercent: Int = 18
)

class ChargeBeesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ChargeBeesDatabase.getDatabase(application)
    private val dao = database.chargeBeesDao()
    private val repository = ChargeBeesRepository(dao)

    // Flows linked to Database (Offline Support)
    val stations: StateFlow<List<ChargingStation>> = repository.stations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookings: StateFlow<List<SlotBooking>> = repository.bookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStats?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notifications: StateFlow<List<NotificationAlert>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI State variables
    val isLoggedIn = MutableStateFlow(false)
    val isBiometricPromptShowing = MutableStateFlow(false)
    val selectedStation = MutableStateFlow<ChargingStation?>(null)
    val isDarkMode = MutableStateFlow(true) // Neon yellow/green dark theme fits bees perfectly
    val isSyncing = MutableStateFlow(false)

    // Active charging session telemetries
    val activeBookingForCharging = MutableStateFlow<SlotBooking?>(null)
    val simulatedChargingProgress = MutableStateFlow<Int?>(null)
    val liveEnergyKw = MutableStateFlow(0.0f)
    val liveTempC = MutableStateFlow(32.5f)
    val sessionEnergyKwh = MutableStateFlow(0.0f)

    // Navigation telemetry
    val navigationState = MutableStateFlow<NavigationState?>(null)

    // Notification toast indicator
    val toastMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            // Periodic background status syncing simulation (Real-time syncing)
            startPeriodicStatusSyncing()
        }
    }

    // Toggle Dark Mode
    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    // Authenticate user with password or pin
    fun login(email: String, pin: String): Boolean {
        if (email.isNotBlank() && pin == "9000") {
            isLoggedIn.value = true
            return true
        } else if (email.lowercase() == "db@chargebees.com" || email == "vardhan.harsha9391@gmail.com") {
            isLoggedIn.value = true
            return true
        } else if (email.isNotBlank() && pin.isNotBlank()) {
            // Permit any valid entries for prototyping
            isLoggedIn.value = true
            return true
        }
        return false
    }

    fun loginWithBiometrics() {
        viewModelScope.launch {
            isBiometricPromptShowing.value = true
            delay(1500) // Simulate sensor check
            isBiometricPromptShowing.value = false
            isLoggedIn.value = true
            showToast("Authenticated using Biometrics 🧬")
        }
    }

    fun logout() {
        isLoggedIn.value = false
    }

    // Simulated Stripe Payment & Booking process
    fun processSlotBooking(
        vehicleNo: String,
        chargerType: String,
        date: String,
        timeSlot: String,
        amount: Double,
        onSuccess: (bookingId: Int) -> Unit
    ) {
        viewModelScope.launch {
            isSyncing.value = true
            delay(2000) // Simulate secure payment processing via Stripe SDK Gateway
            isSyncing.value = false

            val station = selectedStation.value ?: return@launch
            val bId = repository.createBooking(
                stationId = station.id,
                stationName = station.name,
                vehicleNumber = vehicleNo.ifEmpty { "TS09EV2026" },
                chargerType = chargerType,
                bookingDate = date,
                bookingTime = timeSlot,
                paymentAmount = amount
            )

            showToast("Stripe Payment Processed & Reserved successfully!")
            onSuccess(bId.toInt())
        }
    }

    // Simulated live turn-by-turn Navigation
    fun startNavigation(bookingId: Int) {
        val destStation = stations.value.find { 
            val booking = bookings.value.find { b -> b.id == bookingId }
            it.name == booking?.stationName
        } ?: stations.value.first()

        viewModelScope.launch {
            var kmLeft = destStation.distance
            var minLeft = (destStation.distance * 1.8).toInt().coerceAtLeast(3)
            var index = 0
            val instructions = listOf(
                "Turn right toward Tech Boulevard Hub in 400m",
                "Take second exit at ORR Rotary toward Chargebees Station",
                "Merge onto Express Link, keep left for 2 kilometers",
                "Take exit 14B on left toward Charge Bees Terminal",
                "Arrived! Pull into Charging Bay Hex-7"
            )

            while (kmLeft > 0.1) {
                navigationState.value = NavigationState(
                    nextInstruction = instructions[index.coerceAtMost(instructions.size - 1)],
                    distanceRemainingKm = kmLeft,
                    timeRemainingMin = minLeft,
                    currentSpeedKmh = Random.nextInt(48, 65),
                    pathRouteIndex = index,
                    batteryPercent = (10 + (kmLeft * 0.9)).toInt().coerceIn(4, 99)
                )

                delay(3000) // Simulated update pulse (Real-time UI refresh)
                kmLeft = (kmLeft - 0.4).coerceAtLeast(0.0)
                minLeft = (minLeft - 1).coerceAtLeast(1)
                if (Random.nextBoolean() && index < instructions.size - 1) {
                    index++
                }
            }

            navigationState.value = NavigationState(
                nextInstruction = "Arrived! Connect CCS-2 gun to begin power replenishment.",
                distanceRemainingKm = 0.0,
                timeRemainingMin = 0,
                currentSpeedKmh = 0,
                pathRouteIndex = instructions.size - 1,
                batteryPercent = 12
            )
        }
    }

    fun stopNavigation() {
        navigationState.value = null
    }

    // Simulated Vehicle Charging replenisher loops (Real-time dynamic dashboard)
    fun startChargingSession(bookingId: Int) {
        val booking = bookings.value.find { it.id == bookingId } ?: return
        activeBookingForCharging.value = booking
        simulatedChargingProgress.value = 15 // Start at 15% charge
        liveEnergyKw.value = 62.4f
        liveTempC.value = 34.2f
        sessionEnergyKwh.value = 0.0f

        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, "CHARGING")
            viewModelScope.launch {
                repository.postNotification(
                    "Charging Started ⚡",
                    "Your Nexon EV charging session is active. Current rate: ${liveEnergyKw.value} kW at ${booking.stationName}."
                )
            }

            while (simulatedChargingProgress.value != null && simulatedChargingProgress.value!! < 100) {
                delay(2000) // fast speed simulation
                val curProgress = simulatedChargingProgress.value ?: break
                val newProgress = (curProgress + 5).coerceAtMost(100)
                simulatedChargingProgress.value = newProgress

                // Random energy oscillations
                liveEnergyKw.value = Random.nextFloat() * 15.0f + 55.0f
                liveTempC.value = Random.nextFloat() * 4.0f + 36.0f
                sessionEnergyKwh.value = sessionEnergyKwh.value + Random.nextFloat() * 2.5f

                repository.triggerLiveChargingProgress(bookingId, newProgress)
            }

            if (simulatedChargingProgress.value == 100) {
                // Ensure finalized stats
                delay(1000)
                stopChargingSession()
            }
        }
    }

    fun stopChargingSession() {
        val booking = activeBookingForCharging.value
        if (booking != null) {
            viewModelScope.launch {
                repository.updateBookingStatus(booking.id, "COMPLETED")
            }
        }
        activeBookingForCharging.value = null
        simulatedChargingProgress.value = null
        liveEnergyKw.value = 0.0f
        sessionEnergyKwh.value = 0.0f
    }

    // Toggle Biometrics configuration in User profile settings
    fun toggleBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateBiometricChoice(enabled)
            showToast("Biometric login settings updated!")
        }
    }

    // Delete a notification entry
    fun dismissNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // Trigger explicit manual mock sync
    fun triggerSync() {
        viewModelScope.launch {
            isSyncing.value = true
            delay(1500)
            isSyncing.value = false

            // Randomly toggle charger availabilities to simulate real-time cloud sync
            val randomizedList = stations.value.map {
                it.copy(
                    isAvailable = if (Random.nextInt(5) == 0) !it.isAvailable else it.isAvailable,
                    fastChargerCount = (it.fastChargerCount + Random.nextInt(-1, 2)).coerceAtLeast(0)
                )
            }
            dao.insertStations(randomizedList)
            showToast("Synchronized charging grid with live Charge Bees cloud database! 🍯🐝")
        }
    }

    private fun startPeriodicStatusSyncing() {
        viewModelScope.launch {
            while (true) {
                delay(30000) // Periodic update check
                // Soft refresh in-app telemetry data
                val updated = stations.value.map {
                    if (Random.nextBoolean()) {
                        it.copy(fastChargerCount = (it.fastChargerCount + Random.nextInt(-1, 2)).coerceIn(0, 15))
                    } else it
                }
                dao.insertStations(updated)
            }
        }
    }

    private fun showToast(msg: String) {
        toastMessage.value = msg
        viewModelScope.launch {
            delay(3000)
            if (toastMessage.value == msg) {
                toastMessage.value = null
            }
        }
    }

    fun clearToast() {
        toastMessage.value = null
    }
}

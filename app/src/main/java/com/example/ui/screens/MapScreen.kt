package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveMap
import com.example.ui.theme.*
import com.example.viewmodel.ChargeBeesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: ChargeBeesViewModel,
    onBookingSuccess: (bookingId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.stations.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showBookingForm by remember { mutableStateOf(false) }

    // Booking input selections
    var vehicleNumber by remember { mutableStateOf("") }
    var chargerTypeSelected by remember { mutableStateOf("CCS-2 (120kW)") }
    var bookingDate by remember { mutableStateOf("June 4, 2026") }
    var bookingTimeSlot by remember { mutableStateOf("11:00 AM - 12:00 PM") }
    var paymentMethodSelected by remember { mutableStateOf("Stripe (Card)") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) PolishBackground else Color(0xFFF4F2F7))
    ) {
        // Render reactive canvas-based navigation grid map
        InteractiveMap(
            stations = stations,
            selectedStation = selectedStation,
            onStationSelect = { station ->
                showBookingForm = false
                viewModel.selectedStation.value = station
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay top utility station browser card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) PolishSurface.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
                .border(
                    1.dp,
                    if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Map hub",
                        tint = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BEES MAP TERMINALS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
                    )
                }

                // Inline quick sync status
                Box(
                    modifier = Modifier
                        .clickable { viewModel.triggerSync() }
                        .padding(4.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Sync Map Grid",
                        tint = if (isSyncing) Color(0xFFFBBF24) else PolishPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Overlay: Bottom station inspector and Reservation slider
        AnimatedVisibility(
            visible = selectedStation != null,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
        ) {
            val station = selectedStation
            if (station != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) PolishSurface else Color.White
                    ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                            RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .background(Color(0xFF64748B), RoundedCornerShape(2.dp))
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = station.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
                                )
                                Text(
                                    text = station.address,
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Availability bubble
                            val statusBgColor = if (station.isAvailable) PolishPrimary else Color(0xFFEF4444)
                            val statusTextCol = if (station.isAvailable) (if (isDarkMode) PolishOnTertiary else Color(0xFF381E72)) else Color(0xFFEF4444)
                            Box(
                                modifier = Modifier
                                    .background(if (station.isAvailable) PolishTertiary else Color(0xFFEF4444).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .border(1.dp, if (station.isAvailable) PolishTertiary else Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (station.isAvailable) "ONLINE" else "FULL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusTextCol
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.3f)
                        )

                        if (!showBookingForm) {
                            // Standard Details Card Page
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Fast chargers info
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDarkMode) PolishContainer else Color(0xFFF1F0F5)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .border(1.dp, if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text("POWER", fontSize = 9.sp, color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${station.fastChargerCount * 60} kW (CCS2)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                // Rate info
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDarkMode) PolishContainer else Color(0xFFF1F0F5)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .border(1.dp, if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text("PRICING", fontSize = 9.sp, color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "₹${station.chargeRate}/kWh",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Interactive Navigation & Booking triggers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.selectedStation.value = null },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDarkMode) PolishSecondary else Color(0xFFE2E8F0),
                                        contentColor = if (isDarkMode) PolishPrimary else Color(0xFF475569)
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.weight(0.7f)
                                ) {
                                    Text("DISMISS", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { 
                                        if (station.isAvailable) {
                                            showBookingForm = true 
                                        }
                                    },
                                    enabled = station.isAvailable,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                                        contentColor = if (isDarkMode) PolishOnPrimary else Color.White
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("reserve_slot_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Filled.CalendarMonth,
                                            contentDescription = "slot booking",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("BOOK SLOT", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // Booking Stripe process form screen
                            Text(
                                text = "SELECT SLOTS & SECURE PAY",
                                color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Vehicle no field
                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { vehicleNumber = it },
                                label = { Text("Electric Vehicle Number") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                                    focusedLabelColor = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                                    unfocusedBorderColor = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0),
                                    focusedContainerColor = if (isDarkMode) PolishContainer else Color(0xFFF8FAFC),
                                    unfocusedContainerColor = if (isDarkMode) PolishContainer else Color(0xFFF8FAFC)
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vehicle_number_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Charger select
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("CCS-2 (120kW)", "Type-2 AC (22kW)").forEach { type ->
                                    val isSel = chargerTypeSelected.contains(type.substring(0, 4))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                1.5.dp,
                                                if (isSel) (if (isDarkMode) PolishPrimary else Color(0xFF6750A4)) else (if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0)),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .background(
                                                if (isSel) (if (isDarkMode) PolishPrimary.copy(alpha = 0.12f) else Color(0xFF6750A4).copy(alpha = 0.08f)) else Color.Transparent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { chargerTypeSelected = type }
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = type,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) (if (isDarkMode) PolishPrimary else Color(0xFF381E72)) else (if (isDarkMode) PolishTextSecondary else Color(0xFF94A3B8))
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Date picker list (Mock)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val dates = listOf("June 4, 2026", "June 5, 2026")
                                dates.forEach { dt ->
                                    val isSel = bookingDate == dt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                1.dp,
                                                if (isSel) (if (isDarkMode) PolishPrimary else Color(0xFF6750A4)) else (if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0)),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .background(
                                                if (isSel) (if (isDarkMode) PolishPrimary.copy(alpha = 0.08f) else Color(0xFF6750A4).copy(alpha = 0.04f)) else Color.Transparent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { bookingDate = dt }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dt,
                                            fontSize = 12.sp,
                                            color = if (isSel) (if (isDarkMode) PolishPrimary else Color(0xFF381E72)) else (if (isDarkMode) PolishTextSecondary else Color(0xFF94A3B8)),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Time slot select list (Mock)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val slots = listOf("11:00 AM - 12:00 PM", "2:30 PM - 3:30 PM")
                                slots.forEach { slot ->
                                    val isSel = bookingTimeSlot == slot
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                1.dp,
                                                if (isSel) (if (isDarkMode) PolishPrimary else Color(0xFF6750A4)) else (if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0)),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .background(
                                                if (isSel) (if (isDarkMode) PolishPrimary.copy(alpha = 0.08f) else Color(0xFF6750A4).copy(alpha = 0.04f)) else Color.Transparent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { bookingTimeSlot = slot }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = slot.substring(0, 10),
                                            fontSize = 11.sp,
                                            color = if (isSel) (if (isDarkMode) PolishPrimary else Color(0xFF381E72)) else (if (isDarkMode) PolishTextSecondary else Color(0xFF94A3B8)),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Payment Methods (Unified, showing Stripe branding)
                            Text(
                                text = "PAYMENT METHOD (SECURE VIA STRIPE)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("Stripe (Card)", "Google Pay", "Select UPI").forEach { pay ->
                                    val isSel = paymentMethodSelected == pay
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                1.dp,
                                                if (isSel) (if (isDarkMode) PolishPrimary else Color(0xFF6750A4)) else (if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0)),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .background(
                                                if (isSel) (if (isDarkMode) PolishPrimary.copy(alpha = 0.08f) else Color(0xFF6750A4).copy(alpha = 0.04f)) else Color.Transparent
                                            )
                                            .clickable { paymentMethodSelected = pay }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pay,
                                            fontSize = 11.sp,
                                            color = if (isSel) (if (isDarkMode) PolishPrimary else Color(0xFF381E72)) else (if (isDarkMode) PolishTextSecondary else Color(0xFF94A3B8)),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showBookingForm = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDarkMode) PolishSecondary else Color(0xFFE2E8F0),
                                        contentColor = if (isDarkMode) PolishPrimary else Color(0xFF475569)
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.weight(0.7f)
                                ) {
                                    Text("BACK")
                                }

                                Button(
                                    onClick = {
                                        keyboardController?.hide()
                                        // Standard cost rate calculation (1 hour CCS-2 fast charge averages 30 units times rate)
                                        val averageKwhUnits = if (chargerTypeSelected.contains("CCS")) 35.0 else 12.0
                                        val totalAmt = averageKwhUnits * station.chargeRate

                                        viewModel.processSlotBooking(
                                            vehicleNo = vehicleNumber,
                                            chargerType = chargerTypeSelected,
                                            date = bookingDate,
                                            timeSlot = bookingTimeSlot,
                                            amount = totalAmt,
                                            onSuccess = { bId ->
                                                viewModel.selectedStation.value = null
                                                showBookingForm = false
                                                onBookingSuccess(bId)
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                                        contentColor = if (isDarkMode) PolishOnPrimary else Color.White
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("pay_stripe_button")
                                ) {
                                    val priceFactor = if (chargerTypeSelected.contains("CCS")) 35.0 else 12.0
                                    val amountText = priceFactor * station.chargeRate
                                    Text(
                                        text = "PAY STRIPE ₹${String.format("%.1f", amountText)}",
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

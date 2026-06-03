package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SlotBooking
import com.example.data.UserStats
import com.example.ui.theme.*
import com.example.viewmodel.ChargeBeesViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: ChargeBeesViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToContact: () -> Unit,
    onStartNavigation: (bookingId: Int) -> Unit,
    onStartCharging: (bookingId: Int) -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    // Fluctuating real-time statistics simulation
    var electricCurrentDraw by remember { mutableStateOf(48.5f) }
    var co2SavingsLiveOffset by remember { mutableStateOf(0.0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            electricCurrentDraw = (40f + Random.nextFloat() * 25f)
            co2SavingsLiveOffset += (0.1f + Random.nextFloat() * 0.15f)
        }
    }

    val activeBooking = bookings.find { it.status == "CONFIRMED" || it.status == "CHARGING" }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) PolishBackground else Color(0xFFF4F2F7)
            )
    ) {
        val screenWidth = maxWidth
        val isWideScreen = screenWidth > 600.dp

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.triggerSync() },
                    containerColor = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                    contentColor = if (isDarkMode) PolishOnPrimary else Color.White,
                    modifier = Modifier.testTag("sync_fab")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "Sync Cloud Grid"
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Custom Adaptive Top Header App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "CHARGES GRID",
                            color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hello, ${userStats?.name ?: "Vardhan"}! 🐝",
                                color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier
                                .background(
                                    if (isDarkMode) PolishSecondary else Color.White,
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4)
                            )
                        }
                    }
                }

                if (isWideScreen) {
                    // DESKTOP/TABLET Layout: Side-by-Side row representation
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column: User Telemetries and Daily Charts
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GridStatsConsole(userStats, electricCurrentDraw, isDarkMode, co2SavingsLiveOffset)
                            LiveEnergyConsumptionChart(isDarkMode)
                        }

                        // Right Column: Active Charging and Actions
                        Column(
                            modifier = Modifier
                                .weight(0.9f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ActiveBookingWidget(
                                activeBooking = activeBooking,
                                isDarkMode = isDarkMode,
                                onStartNavigation = onStartNavigation,
                                onStartCharging = onStartCharging,
                                viewModel = viewModel
                            )
                            GridActionButtons(onNavigateToMap, onNavigateToHistory, onNavigateToContact, isDarkMode)
                        }
                    }
                } else {
                    // MOBILE Portrait Layout: Single scrolling column
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            GridStatsConsole(userStats, electricCurrentDraw, isDarkMode, co2SavingsLiveOffset)
                        }

                        item {
                            ActiveBookingWidget(
                                activeBooking = activeBooking,
                                isDarkMode = isDarkMode,
                                onStartNavigation = onStartNavigation,
                                onStartCharging = onStartCharging,
                                viewModel = viewModel
                            )
                        }

                        item {
                            LiveEnergyConsumptionChart(isDarkMode)
                        }

                        item {
                            GridActionButtons(onNavigateToMap, onNavigateToHistory, onNavigateToContact, isDarkMode)
                        }

                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridStatsConsole(
    stats: UserStats?,
    drawRateKw: Float,
    isDarkMode: Boolean,
    co2SavingsLiveOffset: Float
) {
    val bgCardColor = if (isDarkMode) PolishSurface else Color.White
    val textBaseColor = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
    val subTextColor = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgCardColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "CURRENT ENERGY CONSOLE",
                color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Statistics Grid row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stat 1: Total energy draw
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.ElectricBolt,
                            contentDescription = "Energy",
                            tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("POWER COMPLETED", fontSize = 11.sp, color = subTextColor)
                    }
                    Text(
                        text = "${String.format("%.1f", stats?.totalPowerSecured ?: 340.5)} kWh",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textBaseColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Stat 2: Active drawing rate
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.ElectricCar,
                            contentDescription = "Car",
                            tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LIVE SYNC DRAW", fontSize = 11.sp, color = subTextColor)
                    }
                    Text(
                        text = "${String.format("%.1f", drawRateKw)} kW",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stat 3: CO2 saved metrics tracking user engagement trends
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Park,
                            contentDescription = "Co2 Saved",
                            tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CO2 EMISSION SAVED", fontSize = 11.sp, color = subTextColor)
                    }
                    Text(
                        text = "${String.format("%.1f", (stats?.co2SavedKg ?: 412.3) + co2SavingsLiveOffset)} kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textBaseColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Stat 4: Booking Counts
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.BookOnline,
                            contentDescription = "Reservations",
                            tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TOTAL REPLACES", fontSize = 11.sp, color = subTextColor)
                    }
                    Text(
                        text = "${stats?.bookingCount ?: 12} Sessions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textBaseColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveBookingWidget(
    activeBooking: SlotBooking?,
    isDarkMode: Boolean,
    onStartNavigation: (bookingId: Int) -> Unit,
    onStartCharging: (bookingId: Int) -> Unit,
    viewModel: ChargeBeesViewModel
) {
    if (activeBooking == null) return

    val bgCardColor = if (isDarkMode) PolishSurface else Color.White
    val textBaseColor = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
    val progressFlow by viewModel.simulatedChargingProgress.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = bgCardColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                RoundedCornerShape(24.dp)
            )
            .testTag("active_booking_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = "Active slot",
                        tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE SESSION STATUS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                        letterSpacing = 0.5.sp
                    )
                }

                // Status chip
                val statusColor = if (activeBooking.status == "CHARGING") (if (isDarkMode) PolishPrimary else Color(0xFF6750A4)) else Color(0xFF3B82F6)
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, statusColor.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = activeBooking.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = activeBooking.stationName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textBaseColor
            )

            Text(
                text = "Vehicle: ${activeBooking.vehicleNumber}  •  ${activeBooking.chargerType} Supercharger",
                fontSize = 13.sp,
                color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B),
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = "Slot Time: ${activeBooking.bookingTime} (${activeBooking.bookingDate})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action triggers based on status
            if (activeBooking.status == "CONFIRMED") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start navigation button
                    Button(
                        onClick = { onStartNavigation(activeBooking.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) PolishSecondary else Color(0xFFE2E8F0),
                            contentColor = if (isDarkMode) PolishPrimary else Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.Navigation,
                                contentDescription = "Navigate",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GO NAVIGATE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Start Charging button
                    Button(
                        onClick = { onStartCharging(activeBooking.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                            contentColor = if (isDarkMode) PolishOnPrimary else Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.FlashOn,
                                contentDescription = "Charge Now",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PLUG CHARGE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (activeBooking.status == "CHARGING") {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Replenishing Energy Progress:",
                            fontSize = 13.sp,
                            color = textBaseColor
                        )
                        Text(
                            text = "${progressFlow ?: 15}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = (progressFlow ?: 15).toFloat() / 100f,
                        color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                        trackColor = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.stopChargingSession() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("STOP CHARGE & RELEASE BAYS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveEnergyConsumptionChart(isDarkMode: Boolean) {
    val bgCardColor = if (isDarkMode) PolishSurface else Color.White
    val subTextColor = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgCardColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "WEEKLY ENERGY INDEX (kWh)",
                color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Weekly canvas bar drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .drawBehind {
                        val gridHeight = size.height
                        val gridWidth = size.width

                        // Draw background reference grid lines
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val y = gridHeight * (i.toFloat() / gridLines)
                            drawLine(
                                color = if (isDarkMode) PolishSecondary.copy(alpha = 0.5f) else Color(0xFFCAC4D0).copy(alpha = 0.3f),
                                start = Offset(0f, y),
                                end = Offset(gridWidth, y),
                                strokeWidth = 1f
                            )
                        }

                        // Drawing custom modern visual rods representation representing: Mon, Tue, Wed, Thu, Fri, Sat, Sun
                        val dailyEnergyStats = floatArrayOf(24f, 42f, 18f, 65f, 32f, 85f, 55f)
                        val maxKwh = 100f
                        val rodWidth = 28f
                        val gap = (gridWidth - (dailyEnergyStats.size * rodWidth)) / (dailyEnergyStats.size + 1)

                        dailyEnergyStats.forEachIndexed { idx, value ->
                            val x = gap + idx * (rodWidth + gap)
                            val barHeight = gridHeight * (value / maxKwh)
                            val y = gridHeight - barHeight

                            // Gradient brush of honey-bees colors
                            val gradient = Brush.verticalGradient(
                                colors = listOf(if (isDarkMode) PolishPrimary else Color(0xFF6750A4), if (isDarkMode) PolishTertiary else Color(0xFFD0BCFF))
                            )

                            // Draw rounded rectangle bars
                            drawRoundRect(
                                brush = gradient,
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(rodWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )
                        }
                    }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { label ->
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = subTextColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GridActionButtons(
    onNavigateToMap: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToContact: () -> Unit,
    isDarkMode: Boolean
) {
    val bgCardColor = if (isDarkMode) PolishSurface else Color.White
    val textBaseColor = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgCardColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "COMPANY CONTROL CENTER",
                color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Horizontal button actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Maps
                Button(
                    onClick = onNavigateToMap,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                        contentColor = if (isDarkMode) PolishOnPrimary else Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "Map view",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FIND HUB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Button 2: Support
                Button(
                    onClick = onNavigateToContact,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) PolishSecondary else Color(0xFFE2E8F0),
                        contentColor = if (isDarkMode) PolishPrimary else Color(0xFF475569)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.ContactSupport,
                            contentDescription = "Contact us",
                            modifier = Modifier.size(16.dp),
                            tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CONTACT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

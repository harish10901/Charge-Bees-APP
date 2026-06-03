package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.CancelPresentation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SlotBooking
import com.example.ui.theme.*
import com.example.viewmodel.ChargeBeesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun HistoryScreen(
    viewModel: ChargeBeesViewModel,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.bookings.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var selectedInvoiceBooking by remember { mutableStateOf<SlotBooking?>(null) }
    val formatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) PolishBackground else Color(0xFFF4F2F7))
            .padding(24.dp)
    ) {
        // TOP Header
        Column(
            modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
        ) {
            Text(
                text = "TRANSACTION REGISTRY",
                color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Reservation Receipts",
                color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(
            color = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = "Empty History",
                        tint = if (isDarkMode) PolishSecondary else Color(0xFFCBD5E1),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No slot bookings found on this device.",
                        color = if (isDarkMode) PolishTextSecondary else Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookings, key = { it.id }) { booking ->
                    val colorAccent = when (booking.status) {
                        "CONFIRMED" -> if (isDarkMode) PolishPrimary else Color(0xFF6750A4)
                        "CHARGING" -> if (isDarkMode) PolishPrimary else Color(0xFF6750A4)
                        "COMPLETED" -> if (isDarkMode) PolishPrimary else Color(0xFF6750A4)
                        else -> Color(0xFF64748B)
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) PolishSurface else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedInvoiceBooking = booking }
                            .testTag("booking_history_card_${booking.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Filled.ElectricBolt,
                                        contentDescription = "Bolt",
                                        tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = booking.chargerType,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(colorAccent.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .border(1.dp, colorAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = booking.status,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = booking.stationName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
                            )

                            Text(
                                text = "Reserved: ${booking.bookingTime} (${booking.bookingDate})",
                                fontSize = 12.sp,
                                color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B),
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.3f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vehicle: ${booking.vehicleNumber}",
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B)
                                )

                                Text(
                                    text = "₹${String.format("%.1f", booking.paymentAmount)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overlay invoice dialog modal popup receipt mock
        AnimatedVisibility(
            visible = selectedInvoiceBooking != null,
            enter = fadeIn() + expandIn(),
            exit = fadeOut() + shrinkOut()
        ) {
            val rec = selectedInvoiceBooking
            if (rec != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { selectedInvoiceBooking = null },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDarkMode) PolishSurface else Color.White),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .width(320.dp)
                            .border(1.dp, if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .clickable(enabled = false) {} // Prevent click-through
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "STRIPE SECURE RECEIPT",
                                color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.AssignmentTurnedIn,
                                contentDescription = "Receipt confirmed",
                                tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                                modifier = Modifier.size(54.dp)
                            )

                            Text(
                                text = "Transaction Approved",
                                color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 10.dp)
                            )

                            Text(
                                text = "Reference Id: STR-CB-${rec.id}${Random.nextInt(1000, 9999)}",
                                color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.3f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("STATION", fontSize = 11.sp, color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B))
                                Text(rec.stationName.substringBefore(" Hub").substringBefore(" Station"), fontSize = 11.sp, color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("GUN TYPE", fontSize = 11.sp, color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B))
                                Text(rec.chargerType, fontSize = 11.sp, color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("VEHICLE", fontSize = 11.sp, color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B))
                                Text(rec.vehicleNumber, fontSize = 11.sp, color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SCHED DATE", fontSize = 11.sp, color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B))
                                Text(rec.bookingDate, fontSize = 11.sp, color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F), fontWeight = FontWeight.Bold)
                            }

                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TOTAL PAID", fontSize = 13.sp, color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F), fontWeight = FontWeight.Bold)
                                Text("₹${String.format("%.1f", rec.paymentAmount)}", fontSize = 18.sp, color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4), fontWeight = FontWeight.ExtraBold)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { selectedInvoiceBooking = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                                    contentColor = if (isDarkMode) PolishOnPrimary else Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("DISMISS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

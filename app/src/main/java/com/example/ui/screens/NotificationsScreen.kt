package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ChargeBeesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: ChargeBeesViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.notifications.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val dateFormatter = remember { SimpleDateFormat("HH:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) PolishBackground else Color(0xFFF4F2F7))
            .padding(24.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "COMMUNICATION LOGS",
                    color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "System Alerts Grid",
                    color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (alerts.isNotEmpty()) {
                IconButton(
                    onClick = { alerts.forEach { viewModel.dismissNotification(it.id) } }
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Clear all notifications",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        Divider(
            color = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "No notifications",
                        tint = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Grid is clear. No active alerts.",
                        color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B),
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
                items(alerts, key = { it.id }) { alert ->
                    val isYellow = alert.title.contains("Booking") || alert.title.contains("80%")
                    val alertAccent = if (isYellow) PolishTertiary else PolishPrimary

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
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // High voltage bullet representing message state
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .offset(y = 6.dp)
                                    .background(alertAccent, RoundedCornerShape(4.dp))
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = alert.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(alert.timestamp)),
                                        fontSize = 10.sp,
                                        color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B)
                                    )
                                }

                                Text(
                                    text = alert.message,
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) PolishTextSecondary else Color(0xFF475569),
                                    modifier = Modifier.padding(top = 4.dp),
                                    lineHeight = 16.sp
                                )
                            }

                            // Dismiss individual
                            IconButton(
                                onClick = { viewModel.dismissNotification(alert.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "dismiss log",
                                    tint = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

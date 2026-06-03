package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ChargeBeesViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NavigationScreen(
    viewModel: ChargeBeesViewModel,
    onArrived: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navState by viewModel.navigationState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Gentle rotate compass animation
    val infiniteTransition = rememberInfiniteTransition(label = "compass")
    val compassAngle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "compass_angle"
    )

    // Trigger arriving callback if distance reaches zero
    LaunchedEffect(navState?.distanceRemainingKm) {
        if (navState != null && navState!!.distanceRemainingKm <= 0.05) {
            delay(1500)
            onArrived()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBackground) // High performance dark cockpit cockpit navigation
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // HUD Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "LIVE GRID HUD",
                        color = PolishPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "ROUTE PILOT ACTIVE",
                        color = PolishTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .background(PolishSurface, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Tv,
                            contentDescription = "HUD Mode",
                            tint = PolishPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("3D MAPPING", color = PolishTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // HUD Speedometer & Battery metrics panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Speedometer card
                Card(
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, PolishPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CURRENT SPEED", fontSize = 10.sp, color = PolishTextSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${navState?.currentSpeedKmh ?: 54}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = PolishPrimary
                        )
                        Text("km/h", fontSize = 11.sp, color = PolishTextSecondary)
                    }
                }

                // Battery indicators card
                val batteryColor = if ((navState?.batteryPercent ?: 18) < 15) Color(0xFFEF4444) else PolishTertiary
                Card(
                    colors = CardDefaults.cardColors(containerColor = PolishSurface),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, batteryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("NEXON BATTERY", fontSize = 10.sp, color = PolishTextSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${navState?.batteryPercent ?: 18}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = batteryColor
                        )
                        Text(
                            text = if ((navState?.batteryPercent ?: 18) < 15) "CHARGE URGENTLY" else "DESIRED RANGE",
                            fontSize = 9.sp,
                            color = batteryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Route driving lines representation via custom canvas drawing
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, PolishSecondary, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        val cWidth = size.width
                        val cHeight = size.height

                        // Compass coordinate lines drawn
                        drawCircle(
                            color = PolishSecondary,
                            radius = cHeight * 0.45f,
                            center = Offset(cWidth / 2f, cHeight / 2f),
                            style = Stroke(width = 1.5f)
                        )

                        // Draw tactical crosshair intersection line
                        drawLine(
                            color = PolishSecondary.copy(alpha = 0.5f),
                            start = Offset(cWidth / 2f, cHeight * 0.05f),
                            end = Offset(cWidth / 2f, cHeight * 0.95f),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = PolishSecondary.copy(alpha = 0.5f),
                            start = Offset(cWidth * 0.1f, cHeight / 2f),
                            end = Offset(cWidth * 0.9f, cHeight / 2f),
                            strokeWidth = 1f
                        )

                        // Glowing target charging vectors
                        val centerOffset = Offset(cWidth / 2f, cHeight / 2f)
                        val angleRad = Math.toRadians((compassAngle - 90f).toDouble())
                        val tx = cWidth / 2f + (cHeight * 0.4f) * cos(angleRad).toFloat()
                        val ty = cHeight / 2f + (cHeight * 0.4f) * sin(angleRad).toFloat()

                        drawLine(
                            brush = Brush.linearGradient(listOf(PolishPrimary, PolishTertiary)),
                            start = centerOffset,
                            end = Offset(tx, ty),
                            strokeWidth = 5f
                        )

                        // Glowing compass needle tip
                        drawCircle(
                            color = PolishTertiary,
                            radius = 8f,
                            center = Offset(tx, ty)
                        )
                    }

                    // Floating compass icons overlay
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.CompassCalibration,
                        contentDescription = "Compass calibration",
                        tint = PolishPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                            .rotate(compassAngle)
                    )
                }
            }

            // HUD Instructions Guidance card
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
                    .border(1.dp, PolishSecondary, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(PolishPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = "Turn direction",
                            tint = PolishPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(if (navState != null) (navState!!.pathRouteIndex * 90f) else 0f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = navState?.nextInstruction ?: "Turn Right on Tech Boulevard Road in 300m",
                            color = PolishTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Remaining: ${String.format("%.1f", navState?.distanceRemainingKm ?: 1.2)} km  •  ETA: ${navState?.timeRemainingMin ?: 3} mins",
                            color = PolishPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Kill navigations button
            Button(
                onClick = { viewModel.stopNavigation() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "DISMISS DRIVE NAVIGATION",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

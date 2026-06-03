package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.ChargingStation
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun InteractiveMap(
    stations: List<ChargingStation>,
    selectedStation: ChargingStation?,
    onStationSelect: (ChargingStation) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation keyframe definitions (glowing rings on active chargers)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_rings")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_size"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Animated bee flights representing active grid load
    val flightProgress by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bee_flight"
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(stations) {
                    detectTapGestures { offset ->
                        // Coordinates normalization & projection:
                        // Convert touch coordinates back into nearest charger based on relative canvas weight
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()

                        var nearest: ChargingStation? = null
                        var shortestDistance = 80f // tap threshold radius in pixels

                        stations.forEach { station ->
                            // Project station lat/long roughly into canvas coordinates
                            // Normalize based on representative coordinates for central Gachibowli hub
                            val projX = width * 0.5f + ((station.longitude - 78.37) * 2000f).toFloat()
                            val projY = height * 0.5f - ((station.latitude - 17.44) * 2000f).toFloat()

                            // Constrain projections to fit nicely within safe padding zones
                            val posX = projX.coerceIn(50f, width - 50f)
                            val posY = projY.coerceIn(100f, height - 100f)

                            val dist = sqrt((offset.x - posX) * (offset.x - posX) + (offset.y - posY) * (offset.y - posY))
                            if (dist < shortestDistance) {
                                shortestDistance = dist
                                nearest = station
                            }
                        }

                        nearest?.let { onStationSelect(it) }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw a premium Dark Slate background with Hex Honeycomb circuit lines
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF030712))
                )
            )

            // Draw Subtle Hexagonal Grid (Charge Bees Theme)
            val hexRadius = 45f
            val horizontalSpacing = hexRadius * 1.5f
            val verticalSpacing = hexRadius * sqrt(3f)

            var row = 0
            var x = 0f
            while (x < width + hexRadius) {
                var y = 0f
                if (row % 2 == 1) {
                    y = verticalSpacing / 2f
                }
                while (y < height + hexRadius) {
                    val hexPath = Path().apply {
                        for (i in 0..6) {
                            val angleRad = Math.toRadians((i * 60).toDouble())
                            val px = x + hexRadius * cos(angleRad).toFloat()
                            val py = y + hexRadius * sin(angleRad).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                    }
                    drawPath(
                        path = hexPath,
                        color = Color(0xFF1E293B).copy(alpha = 0.35f),
                        style = Stroke(width = 1.5f)
                    )
                    y += verticalSpacing
                }
                x += horizontalSpacing
                row++
            }

            // 2. Draw Simulated Grid Connections (Circuit lines flowing)
            val gridPath = Path().apply {
                moveTo(width * 0.1f, height * 0.2f)
                lineTo(width * 0.4f, height * 0.25f)
                lineTo(width * 0.5f, height * 0.45f)
                moveTo(width * 0.9f, height * 0.8f)
                lineTo(width * 0.6f, height * 0.55f)
                lineTo(width * 0.5f, height * 0.45f)
                moveTo(width * 0.2f, height * 0.85f)
                lineTo(width * 0.5f, height * 0.75f)
                lineTo(width * 0.5f, height * 0.45f)
            }
            drawPath(
                path = gridPath,
                color = Color(0xFF10B981).copy(alpha = 0.08f),
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f)))
            )

            // 3. Draw User's Current Location (Nexon EV Pulsing Beacon)
            val userX = width * 0.3f
            val userY = height * 0.6f

            // Pulsing user glow
            drawCircle(
                color = Color(0xFF3B82F6).copy(alpha = 0.25f),
                radius = 30f + pulseSize * 0.4f,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color(0xFF3B82F6),
                radius = 8f,
                center = Offset(userX, userY)
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(userX, userY)
            )

            // 4. Render Charge Bees Station Pins
            stations.forEach { station ->
                // Coordinate conversions
                val projX = width * 0.5f + ((station.longitude - 78.37) * 2000f).toFloat()
                val projY = height * 0.5f - ((station.latitude - 17.44) * 2000f).toFloat()
                val posX = projX.coerceIn(50f, width - 50f)
                val posY = projY.coerceIn(100f, height - 100f)

                val isSelected = selectedStation?.id == station.id
                val accentColor = if (station.isAvailable) Color(0xFF10B981) else Color(0xFFEF4444) // Teal vs Red
                val secondaryColor = Color(0xFFF59E0B) // Honey Gold

                // A. Animated Pulsation rings around online stations
                if (station.isAvailable) {
                    drawCircle(
                        color = accentColor.copy(alpha = pulseAlpha),
                        radius = pulseSize,
                        center = Offset(posX, posY)
                    )
                }

                // If selected, draw connecting navigation path line from User location
                if (isSelected) {
                    val routePath = Path().apply {
                        moveTo(userX, userY)
                        quadraticTo(
                            (userX + posX) / 2f + 60f,
                            (userY + posY) / 2f - 100f,
                            posX,
                            posY
                        )
                    }
                    drawPath(
                        path = routePath,
                        color = Color(0xFFF59E0B),
                        style = Stroke(
                            width = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 30f * flightProgress)
                        )
                    )

                    // Draw a little active bee indicator flying along the route
                    val t = flightProgress
                    val pControlX = (userX + posX) / 2f + 60f
                    val pControlY = (userY + posY) / 2f - 100f
                    // Bezier formula
                    val bx = (1 - t) * (1 - t) * userX + 2 * (1 - t) * t * pControlX + t * t * posX
                    val by = (1 - t) * (1 - t) * userY + 2 * (1 - t) * t * pControlY + t * t * posY

                    drawCircle(
                        color = Color(0xFFFBBF24),
                        radius = 8f,
                        center = Offset(bx, by)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 4f,
                        center = Offset(bx, by)
                    )
                }

                // B. Draw Hexagonal Charging Node Base Pin
                val bSize = if (isSelected) 22f else 16f
                val nodePath = Path().apply {
                    for (i in 0..6) {
                        val angleRad = Math.toRadians((i * 60 + 30).toDouble())
                        val px = posX + bSize * cos(angleRad).toFloat()
                        val py = posY + bSize * sin(angleRad).toFloat()
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                }

                // Node background
                drawPath(
                    path = nodePath,
                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)
                )

                // High voltage core bullet representing speed capability (fastcharger gets lightning aura)
                val coreRadius = if (isSelected) 8f else 5f
                drawPath(
                    path = nodePath,
                    color = accentColor,
                    style = Stroke(width = if (isSelected) 4f else 2.5f)
                )

                // Fill gold center if fast chargers are present
                if (station.fastChargerCount > 4) {
                    drawCircle(
                        color = secondaryColor,
                        radius = coreRadius,
                        center = Offset(posX, posY)
                    )
                } else {
                    drawCircle(
                        color = Color.White,
                        radius = coreRadius,
                        center = Offset(posX, posY)
                    )
                }

                // If selected, render a text label helper in canvas
                if (isSelected) {
                    drawCircle(
                        color = Color(0xFFFBBF24).copy(alpha = 0.35f),
                        radius = bSize + 12f,
                        center = Offset(posX, posY),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

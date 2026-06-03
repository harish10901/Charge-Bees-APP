package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ChargeBeesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ChargeBeesViewModel,
    onLoginSuccess: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("vardhan.harsha9391@gmail.com") }
    var pinCode by remember { mutableStateOf("9000") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val userStats by viewModel.userStats.collectAsState()
    val isBiometricPromptShowing by viewModel.isBiometricPromptShowing.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    // Observe login redirection state
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }

    // Interactive honeycomb-glowing layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PolishBackground, Color(0xFF0C0717))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "bee_glow")
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(22000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "honeycomb_rot"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Interactive glowing hexagonal logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 12.dp)
            ) {
                // Background Glowing Hexagon halo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotationAngle)
                        .border(1.5.dp, PolishPrimary.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .background(PolishPrimary.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
                )
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .rotate(-rotationAngle * 0.5f)
                        .border(1.5.dp, PolishTertiary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                )

                // High-voltage electric bee representation inside
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_app_logo_1780461999301),
                    contentDescription = "Charge Bees Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Text(
                text = "CHARGE BEES",
                color = PolishTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Next-Gen EV Charging Network",
                color = PolishTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Dynamic Inputs Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = PolishSurface.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        PolishSecondary.copy(alpha = 0.7f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "SECURE ACCESS PORTAL",
                        color = PolishPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Email/Phone Address
                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = { 
                            emailOrPhone = it
                            errorMessage = null 
                        },
                        label = { Text("Email or Registered Mobile") },
                        leadingIcon = { 
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.AlternateEmail,
                                contentDescription = "Email",
                                tint = Color(0xFF64748B)
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = PolishSecondary,
                            focusedContainerColor = PolishBackground,
                            unfocusedContainerColor = PolishBackground,
                            focusedLabelColor = PolishPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN Code
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { 
                            pinCode = it
                            errorMessage = null 
                        },
                        label = { Text("4-Digit Security PIN (9000)") },
                        leadingIcon = { 
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "PIN",
                                tint = Color(0xFF64748B)
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = PolishSecondary,
                            focusedContainerColor = PolishBackground,
                            unfocusedContainerColor = PolishBackground,
                            focusedLabelColor = PolishPrimary
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_input")
                    )

                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Secure Login Button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            val success = viewModel.login(emailOrPhone, pinCode)
                            if (!success) {
                                errorMessage = "Invalid PIN or ID. Use pin 9000 to authenticate."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishPrimary,
                            contentColor = PolishOnPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button")
                    ) {
                        Text(
                            text = "AUTHENTICATE CORE",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick simulated biometrics login trigger card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.isBiometricPromptShowing.value = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = "Biometrics icon",
                            tint = PolishPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap for Biometric Login",
                            color = PolishTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guest Bypass helper
            Text(
                text = "Use ID vardhan.harsha9391@gmail.com & PIN 9000 to demo",
                color = Color(0xFF475569),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }

        // Biometrics mock security fingerprint overlay prompt sheet dialog
        AnimatedVisibility(
            visible = isBiometricPromptShowing,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishSurface),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        PolishSecondary,
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = "Fingerprint scanning sensor",
                        tint = PolishPrimary,
                        modifier = Modifier
                            .size(72.dp)
                            .clickable {
                                // Instantly login successfully
                                viewModel.login("vardhan.harsha9391@gmail.com", "9000")
                                viewModel.isBiometricPromptShowing.value = false
                            }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Biometric Verification",
                        color = PolishTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Matching face / fingerprint profile...",
                        color = PolishTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.isBiometricPromptShowing.value = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishSecondary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("CANCEL")
                    }
                }
            }
        }
    }
}

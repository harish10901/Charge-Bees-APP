package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ChargeBeesViewModel

@Composable
fun ContactScreen(
    viewModel: ChargeBeesViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    val companyEmail = "db@chargebees.com"
    val companyPhone = "9000040477"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) PolishBackground else Color(0xFFF4F2F7))
            .padding(24.dp)
    ) {
        // Top Header
        Column(
            modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
        ) {
            Text(
                text = "SUPPORT & ENQUIRIES",
                color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Charge Bees Offices",
                color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(
            color = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Information Cards Grid
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) PolishSurface else Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "COMPANY STAKEHOLDERS",
                    fontSize = 11.sp,
                    color = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 1. Phone Card row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$companyPhone")
                            }
                            context.startActivity(intent)
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (isDarkMode) PolishPrimary.copy(alpha = 0.15f) else Color(0xFFCAC4D0).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Dial Phone",
                            tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Hotline voice call",
                            fontSize = 11.sp,
                            color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B)
                        )
                        Text(
                            text = "+91 $companyPhone",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
                        )
                    }
                }

                Divider(
                    color = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 2. Email Card Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$companyEmail")
                                putExtra(Intent.EXTRA_SUBJECT, "Charge Bees Booking Inquiry")
                            }
                            context.startActivity(intent)
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (isDarkMode) PolishPrimary.copy(alpha = 0.15f) else Color(0xFFCAC4D0).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Mail",
                            tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Email dispatcher support",
                            fontSize = 11.sp,
                            color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B)
                        )
                        Text(
                            text = companyEmail,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
                        )
                    }
                }

                Divider(
                    color = if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 3. Web URL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (isDarkMode) PolishPrimary.copy(alpha = 0.15f) else Color(0xFFCAC4D0).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = "Web Portal",
                            tint = if (isDarkMode) PolishPrimary else Color(0xFF6750A4),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Corporate web address",
                            fontSize = 11.sp,
                            color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B)
                        )
                        Text(
                            text = "www.chargebees.com",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // HQ Locations Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) PolishSurface else Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isDarkMode) PolishSecondary else Color(0xFFCAC4D0).copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isDarkMode) PolishTertiary.copy(alpha = 0.15f) else Color(0xFFCAC4D0).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Business,
                        contentDescription = "Headquarters",
                        tint = if (isDarkMode) PolishTertiary else Color(0xFFD0BCFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Charge Bees corporate HQ",
                        fontSize = 11.sp,
                        color = if (isDarkMode) PolishTextSecondary else Color(0xFF64748B)
                    )
                    Text(
                        text = "Hyderabad, Visakhapatnam",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) PolishTextPrimary else Color(0xFF1C1B1F),
                        modifier = Modifier.padding(top = 2.dp),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

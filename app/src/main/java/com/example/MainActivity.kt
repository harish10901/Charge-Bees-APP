package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.ChargeBeesViewModel

enum class CurrentGridTab {
    DASHBOARD,
    MAP,
    HISTORY,
    ALERTS,
    SUPPORT
}

class MainActivity : ComponentActivity() {

    private val viewModel: ChargeBeesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Collect Dark Mode reactive parameter
            val isDarkTheme by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                val navState by viewModel.navigationState.collectAsState()
                val toastMsg by viewModel.toastMessage.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }

                // Observe notifications inside Toast flow
                LaunchedEffect(toastMsg) {
                    toastMsg?.let {
                        snackbarHostState.showSnackbar(
                            message = it,
                            actionLabel = "DISMISS",
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearToast()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDarkTheme) PolishBackground else Color(0xFFF4F2F7))
                ) {
                    if (!isLoggedIn) {
                        // Secure biometric and password entry screen
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                viewModel.triggerSync()
                            }
                        )
                    } else if (navState != null) {
                        // Full Screen Driving HUD overriding standard layouts
                        NavigationScreen(
                            viewModel = viewModel,
                            onArrived = {
                                viewModel.stopNavigation()
                                // Find active booking and start charging
                                val active = viewModel.bookings.value.find { 
                                    it.status == "CONFIRMED" || it.status == "CHARGING" 
                                }
                                if (active != null) {
                                    viewModel.startChargingSession(active.id)
                                }
                            }
                        )
                    } else {
                        // Authenticated console with standard Scaffold bottom action navigation bar
                        var currentTab by remember { mutableStateOf(CurrentGridTab.DASHBOARD) }

                        Scaffold(
                            containerColor = if (isDarkTheme) PolishBackground else Color(0xFFF4F2F7),
                            snackbarHost = {
                                SnackbarHost(hostState = snackbarHostState) { data ->
                                    Snackbar(
                                        snackbarData = data,
                                        containerColor = if (isDarkTheme) PolishSurface else Color(0xFFFFFFFBFE),
                                        contentColor = if (isDarkTheme) PolishTextPrimary else Color(0xFF1C1B1F),
                                        actionColor = PolishPrimary
                                    )
                                }
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = if (isDarkTheme) PolishSurface else Color(0xFFEBE7EF),
                                    tonalElevation = 8.dp,
                                    modifier = Modifier
                                        .testTag("bottom_nav_bar")
                                        .border(
                                            width = 1.dp,
                                            color = if (isDarkTheme) PolishBackground else Color(0xFFCAC4D0).copy(alpha = 0.3f)
                                        )
                                ) {
                                    // HOME
                                    NavigationBarItem(
                                        selected = currentTab == CurrentGridTab.DASHBOARD,
                                        onClick = { currentTab = CurrentGridTab.DASHBOARD },
                                        icon = {
                                            androidx.compose.material3.Icon(
                                                imageVector = if (currentTab == CurrentGridTab.DASHBOARD) Icons.Filled.ElectricCar else Icons.Outlined.ElectricCar,
                                                contentDescription = "Console"
                                            )
                                        },
                                        label = { Text("Grid", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = if (isDarkTheme) PolishOnTertiary else Color(0xFF1D192B),
                                            selectedTextColor = if (isDarkTheme) PolishPrimary else Color(0xFF6750A4),
                                            indicatorColor = if (isDarkTheme) PolishTertiary else Color(0xFFE8DEF8),
                                            unselectedTextColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F),
                                            unselectedIconColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F)
                                        ),
                                        modifier = Modifier.testTag("nav_tab_dashboard")
                                    )

                                    // MAPS
                                    NavigationBarItem(
                                        selected = currentTab == CurrentGridTab.MAP,
                                        onClick = { currentTab = CurrentGridTab.MAP },
                                        icon = {
                                            androidx.compose.material3.Icon(
                                                imageVector = if (currentTab == CurrentGridTab.MAP) Icons.Filled.Map else Icons.Outlined.Map,
                                                contentDescription = "Stations"
                                            )
                                        },
                                        label = { Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = if (isDarkTheme) PolishOnTertiary else Color(0xFF1D192B),
                                            selectedTextColor = if (isDarkTheme) PolishPrimary else Color(0xFF6750A4),
                                            indicatorColor = if (isDarkTheme) PolishTertiary else Color(0xFFE8DEF8),
                                            unselectedTextColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F),
                                            unselectedIconColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F)
                                        ),
                                        modifier = Modifier.testTag("nav_tab_map")
                                    )

                                    // TRANSACTIONS
                                    NavigationBarItem(
                                        selected = currentTab == CurrentGridTab.HISTORY,
                                        onClick = { currentTab = CurrentGridTab.HISTORY },
                                        icon = {
                                            androidx.compose.material3.Icon(
                                                imageVector = if (currentTab == CurrentGridTab.HISTORY) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                                                contentDescription = "Ledgers"
                                            )
                                        },
                                        label = { Text("Bills", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = if (isDarkTheme) PolishOnTertiary else Color(0xFF1D192B),
                                            selectedTextColor = if (isDarkTheme) PolishPrimary else Color(0xFF6750A4),
                                            indicatorColor = if (isDarkTheme) PolishTertiary else Color(0xFFE8DEF8),
                                            unselectedTextColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F),
                                            unselectedIconColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F)
                                        ),
                                        modifier = Modifier.testTag("nav_tab_bills")
                                    )

                                    // NOTIFICATIONS
                                    val alertsCount by viewModel.notifications.collectAsState()
                                    NavigationBarItem(
                                        selected = currentTab == CurrentGridTab.ALERTS,
                                        onClick = { currentTab = CurrentGridTab.ALERTS },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (alertsCount.isNotEmpty()) {
                                                        Badge(containerColor = Color(0xFFEF4444)) {
                                                            Text(
                                                                text = "${alertsCount.size}",
                                                                color = Color.White,
                                                                fontSize = 9.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            ) {
                                                androidx.compose.material3.Icon(
                                                    imageVector = if (currentTab == CurrentGridTab.ALERTS) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                                                    contentDescription = "Inbox Logs"
                                                )
                                            }
                                        },
                                        label = { Text("Inbox", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = if (isDarkTheme) PolishOnTertiary else Color(0xFF1D192B),
                                            selectedTextColor = if (isDarkTheme) PolishPrimary else Color(0xFF6750A4),
                                            indicatorColor = if (isDarkTheme) PolishTertiary else Color(0xFFE8DEF8),
                                            unselectedTextColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F),
                                            unselectedIconColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F)
                                        ),
                                        modifier = Modifier.testTag("nav_tab_inbox")
                                    )

                                    // CONTROLLER SUPPORT
                                    NavigationBarItem(
                                        selected = currentTab == CurrentGridTab.SUPPORT,
                                        onClick = { currentTab = CurrentGridTab.SUPPORT },
                                        icon = {
                                            androidx.compose.material3.Icon(
                                                imageVector = if (currentTab == CurrentGridTab.SUPPORT) Icons.Filled.ContactSupport else Icons.Outlined.ContactSupport,
                                                contentDescription = "Contact Desk"
                                            )
                                        },
                                        label = { Text("Office", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = if (isDarkTheme) PolishOnTertiary else Color(0xFF1D192B),
                                            selectedTextColor = if (isDarkTheme) PolishPrimary else Color(0xFF6750A4),
                                            indicatorColor = if (isDarkTheme) PolishTertiary else Color(0xFFE8DEF8),
                                            unselectedTextColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F),
                                            unselectedIconColor = if (isDarkTheme) PolishTextSecondary else Color(0xFF49454F)
                                        ),
                                        modifier = Modifier.testTag("nav_tab_contact")
                                    )
                                }
                            }
                        ) { innerGridPadding ->
                            // Custom crossfade transitions for flawless native visuals
                            Crossfade<CurrentGridTab>(
                                targetState = currentTab,
                                animationSpec = tween(250),
                                modifier = Modifier.fillMaxSize()
                            ) { tab ->
                                when (tab) {
                                    CurrentGridTab.DASHBOARD -> {
                                        DashboardScreen(
                                            viewModel = viewModel,
                                            onNavigateToMap = { currentTab = CurrentGridTab.MAP },
                                            onNavigateToHistory = { currentTab = CurrentGridTab.HISTORY },
                                            onNavigateToContact = { currentTab = CurrentGridTab.SUPPORT },
                                            onStartNavigation = { bId -> viewModel.startNavigation(bId) },
                                            onStartCharging = { bId -> viewModel.startChargingSession(bId) }
                                        )
                                    }
                                    CurrentGridTab.MAP -> {
                                        MapScreen(
                                            viewModel = viewModel,
                                            onBookingSuccess = { bId ->
                                                currentTab = CurrentGridTab.DASHBOARD
                                            },
                                            modifier = Modifier.padding(innerGridPadding)
                                        )
                                    }
                                    CurrentGridTab.HISTORY -> {
                                        HistoryScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(innerGridPadding)
                                        )
                                    }
                                    CurrentGridTab.ALERTS -> {
                                        NotificationsScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(innerGridPadding)
                                        )
                                    }
                                    CurrentGridTab.SUPPORT -> {
                                        ContactScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(innerGridPadding)
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
}

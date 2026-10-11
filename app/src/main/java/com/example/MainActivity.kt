package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FixItBottomBar
import com.example.ui.screens.*
import com.example.ui.theme.FixItTheme
import com.example.ui.theme.LocalGlassStyle
import com.example.ui.viewmodel.FixItViewModel
import com.example.util.NotificationHelper

sealed class Screen {
    object MainTab : Screen()
    object PickService : Screen()
    data class DescribeJob(val category: String) : Screen()
    object CreateJob : Screen()
    data class JobDetail(val jobId: String) : Screen()
    object Wallet : Screen()
    object WalletEscrow : Screen()
    object Catalog : Screen()
    object Subscriptions : Screen()
    object Dispute : Screen()
    object EscortMap : Screen()
    object ReportExport : Screen()
    object Notifications : Screen()
    data class JobChat(val jobId: String, val vendorName: String = "Laborer") : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(applicationContext)
        enableEdgeToEdge()

        setContent {
            val viewModel: FixItViewModel = viewModel()
            val themeMode by viewModel.appThemeMode.collectAsState()
            FixItTheme(themeMode = themeMode) {
                val context = LocalContext.current

                // Request Notification permissions for Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        NotificationHelper.showNotification(
                            context = context,
                            notificationId = 1001,
                            title = "FixIt Notifications Active",
                            message = "You will receive real-time updates for job bids, escrow locks, and warranty milestones."
                        )
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                FixItApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FixItApp(viewModel: FixItViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    val statusNotice by viewModel.statusNotice.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusNotice) {
        statusNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.setNotice(null)
        }
    }

    if (currentUser == null && userProfile == null) {
        AuthScreen(viewModel = viewModel)
        return
    }

    // Navigation state
    var currentTab by remember { mutableStateOf("home") } // "home", "jobs", "chats", "profile"
    var screenStack by remember { mutableStateOf<List<Screen>>(listOf(Screen.MainTab)) }

    val currentScreen = screenStack.lastOrNull() ?: Screen.MainTab

    fun navigateTo(screen: Screen) {
        screenStack = screenStack + screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack = screenStack.dropLast(1)
        } else if (currentTab != "home") {
            currentTab = "home"
        }
    }

    BackHandler(enabled = screenStack.size > 1 || currentTab != "home") {
        navigateBack()
    }

    val isRootTab = currentScreen is Screen.MainTab

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isRootTab) {
                FixItBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        screenStack = listOf(Screen.MainTab)
                    },
                    onCenterPlusClicked = {
                        navigateTo(Screen.CreateJob)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isRootTab) innerPadding.calculateBottomPadding() else 0.dp)
                .background(LocalGlassStyle.current.backgroundBrush)
        ) {
            when (val screen = currentScreen) {
                is Screen.MainTab -> {
                    when (currentTab) {
                        "home" -> {
                            ConsumerHomeScreen(
                                viewModel = viewModel,
                                onNavigateToServiceSelect = {
                                    navigateTo(Screen.PickService)
                                },
                                onNavigateToServiceDescribe = { category ->
                                    navigateTo(Screen.DescribeJob(category))
                                },
                                onNavigateToJobDetail = { jobId ->
                                    navigateTo(Screen.JobDetail(jobId))
                                },
                                onNavigateToWallet = {
                                    navigateTo(Screen.Wallet)
                                },
                                onNavigateToSubscriptions = {
                                    navigateTo(Screen.Subscriptions)
                                },
                                onNavigateToNotifications = {
                                    navigateTo(Screen.Notifications)
                                },
                                onNavigateToAddresses = {
                                    viewModel.setNotice("Addresses are strictly masked before escrow lock to prevent disintermediation.")
                                },
                                onNavigateToReports = {
                                    navigateTo(Screen.ReportExport)
                                },
                                onNavigateToCreateJob = {
                                    navigateTo(Screen.CreateJob)
                                }
                            )
                        }
                        "jobs" -> {
                            MyJobsScreen(
                                viewModel = viewModel,
                                onNavigateToJobDetail = { jobId ->
                                    navigateTo(Screen.JobDetail(jobId))
                                },
                                onNavigateToCreateJob = {
                                    navigateTo(Screen.CreateJob)
                                },
                                onNavigateToReports = {
                                    navigateTo(Screen.ReportExport)
                                }
                            )
                        }
                        "chats" -> {
                            AssistantChatScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    currentTab = "home"
                                }
                            )
                        }
                        "profile" -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                onNavigateToAddresses = {
                                    viewModel.setNotice("Masked GPS address coordinates safely stored.")
                                },
                                onNavigateToWallet = {
                                    navigateTo(Screen.Wallet)
                                },
                                onNavigateToSupportChat = {
                                    currentTab = "chats"
                                },
                                onNavigateToSubscriptions = {
                                    navigateTo(Screen.Subscriptions)
                                },
                                onNavigateToReports = {
                                    navigateTo(Screen.ReportExport)
                                }
                            )
                        }
                    }
                }
                is Screen.PickService -> {
                    PickServiceScreen(
                        onNavigateBack = { navigateBack() },
                        onSelectService = { selectedService ->
                            navigateTo(Screen.DescribeJob(selectedService))
                        }
                    )
                }
                is Screen.DescribeJob -> {
                    DescribeJobScreen(
                        category = screen.category,
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() },
                        onJobCreated = { newJobId ->
                            // Replace create flow with job detail
                            screenStack = listOf(Screen.MainTab, Screen.JobDetail(newJobId))
                        }
                    )
                }
                is Screen.CreateJob -> {
                    CreateJobScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() },
                        onJobCreated = { newJobId ->
                            screenStack = listOf(Screen.MainTab, Screen.JobDetail(newJobId))
                        }
                    )
                }
                is Screen.JobDetail -> {
                    JobDetailScreen(
                        jobId = screen.jobId,
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() },
                        onNavigateToDispute = {
                            navigateTo(Screen.Dispute)
                        },
                        onNavigateToEscortMap = {
                            navigateTo(Screen.EscortMap)
                        },
                        onNavigateToChat = { jId, vName ->
                            navigateTo(Screen.JobChat(jId, vName))
                        }
                    )
                }
                is Screen.JobChat -> {
                    JobChatScreen(
                        jobId = screen.jobId,
                        vendorName = screen.vendorName,
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
                is Screen.Wallet -> {
                    MyWalletScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
                is Screen.WalletEscrow -> {
                    WalletEscrowScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
                is Screen.Catalog -> {
                    ServiceCatalogScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
                is Screen.Subscriptions -> {
                    SubscriptionsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
                is Screen.Dispute -> {
                    DisputeArbitrationScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
                is Screen.EscortMap -> {
                    EscortModeMapScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
                is Screen.ReportExport -> {
                    ReportExportScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
                is Screen.Notifications -> {
                    NotificationsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navigateBack() }
                    )
                }
            }
        }
    }
}

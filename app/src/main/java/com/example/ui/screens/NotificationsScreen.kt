package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItEmerald
import com.example.ui.theme.FixItPrimaryBlue
import com.example.ui.viewmodel.FixItViewModel
import com.example.util.NotificationHelper

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: String, // escrow, bid, tracking, warranty, topup
    val read: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val notifications = remember {
        listOf(
            NotificationItem(
                id = "n1",
                title = "100% Escrow Frozen",
                message = "Labor fee of $18.00 is locked in FixIt escrow for Burst Kitchen Sink Valve. Funds are protected.",
                timeAgo = "12 mins ago",
                type = "escrow"
            ),
            NotificationItem(
                id = "n2",
                title = "Blind Bid Received",
                message = "Tariq Master Plumbing submitted a bid with 30-day leak-free warranty.",
                timeAgo = "25 mins ago",
                type = "bid"
            ),
            NotificationItem(
                id = "n3",
                title = "Vendor On My Way",
                message = "Tariq has departed for your location. Live GPS tracking is temporarily enabled.",
                timeAgo = "1 hour ago",
                type = "tracking"
            ),
            NotificationItem(
                id = "n4",
                title = "Dual-Wallet Bonus Credited",
                message = "+$5.00 compounding liquidity bonus credited to your wallet balance.",
                timeAgo = "3 hours ago",
                type = "topup"
            ),
            NotificationItem(
                id = "n5",
                title = "Warranty Milestone Active",
                message = "60-Day peer warranty protection for Distribution Board breaker has started.",
                timeAgo = "2 days ago",
                type = "warranty"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Notification Center", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Escrow, Bids, Tracking & Milestones", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F172A))
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Push Alert Trigger Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF162032),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("System Push Notifications", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("High-priority system alerts with vibration", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                NotificationHelper.showNotification(
                                    context = context,
                                    notificationId = (1000..9999).random(),
                                    title = "FixIt Escrow Protected",
                                    message = "Your labor fee is 100% locked. Zero contact info exposed until bid acceptance."
                                )
                                viewModel.setNotice("System notification dispatched to notification tray!")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("dispatch_test_notification_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = FixItPrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Live Push Notification", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            item {
                Text("Recent Activity Alerts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            items(notifications.size) { index ->
                val item = notifications[index]
                val (icon, iconBg, iconColor) = when (item.type) {
                    "escrow" -> Triple(Icons.Default.Lock, Color(0xFF1E3A8A), Color(0xFF60A5FA))
                    "bid" -> Triple(Icons.Default.Gavel, Color(0xFF78350F), FixItAmber)
                    "tracking" -> Triple(Icons.Default.Navigation, Color(0xFF064E3B), FixItEmerald)
                    "topup" -> Triple(Icons.Default.AccountBalanceWallet, Color(0xFF312E81), Color(0xFFA5B4FC))
                    else -> Triple(Icons.Default.VerifiedUser, Color(0xFF164E63), Color(0xFF67E8F9))
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF162032),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(item.timeAgo, fontSize = 11.sp, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.message, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

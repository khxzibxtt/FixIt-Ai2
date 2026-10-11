package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.ui.theme.FixItEmerald
import com.example.ui.theme.FixItPrimaryBlue
import com.example.ui.viewmodel.FixItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWalletScreen(
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    var showAddFundsSheet by remember { mutableStateOf(false) }

    val walletBal = userProfile?.walletBalance ?: 45.0
    val escrowBal = userProfile?.lockedEscrowBalance ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // BLUE HEADER (Matches Screenshot 6)
        Surface(
            color = FixItPrimaryBlue,
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "My Wallet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "TOTAL BALANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (walletBal > 1000000) String.format("%.3f", walletBal) else "${String.format("%.3f", walletBal)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OMR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sub-balances Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text("AVAILABLE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                            Text(String.format("%.3f", walletBal), fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("ESCROW", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                            }
                            Text(String.format("%.3f", escrowBal), fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SCROLLABLE BODY
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Action Buttons (Add Funds & Send Payment) (Matches Screenshot 6)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF162032),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { showAddFundsSheet = true }
                            .testTag("add_funds_button")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF60A5FA))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Add Funds", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF162032),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable {
                                viewModel.setNotice("Send Payment to local verified vendor account.")
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NorthEast, contentDescription = "Send", tint = Color(0xFF60A5FA))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Send Payment", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // HISTORY SECTION (Matches Screenshot 6)
            item {
                Text(
                    text = "History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF162032),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        WalletHistoryItem(
                            title = "Topup Bonus",
                            subtitle = "14 minutes ago • top-up bonus +5",
                            amount = "+5.000",
                            isCredit = true
                        )
                        HorizontalDivider(color = Color(0xFF22314A))
                        WalletHistoryItem(
                            title = "Topup",
                            subtitle = "14 minutes ago • wallet top-up 50 OMR",
                            amount = "+50.000",
                            isCredit = true
                        )
                        HorizontalDivider(color = Color(0xFF22314A))
                        WalletHistoryItem(
                            title = "Platform Fee",
                            subtitle = "14 minutes ago • Lifetime Premium subsc...",
                            amount = "-99.000",
                            isCredit = false
                        )
                        HorizontalDivider(color = Color(0xFF22314A))
                        WalletHistoryItem(
                            title = "Platform Fee",
                            subtitle = "14 minutes ago • Monthly Plus subscripti...",
                            amount = "-3.000",
                            isCredit = false
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    // ADD FUNDS BOTTOM SHEET WITH COMPOUNDING BONUS SCALE
    if (showAddFundsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddFundsSheet = false },
            containerColor = Color(0xFF162032)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Top-Up with Compounding Bonus", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "Deposit into your FixIt dual-wallet. Compounding bonus automatically added to improve liquidity.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                val tiers = listOf(
                    Triple(10.0, 1.0, "Pay 10 OMR → Get 11 OMR (+1 Bonus)"),
                    Triple(20.0, 3.0, "Pay 20 OMR → Get 23 OMR (+3 Bonus)"),
                    Triple(30.0, 5.0, "Pay 30 OMR → Get 35 OMR (+5 Bonus)"),
                    Triple(50.0, 5.0, "Pay 50 OMR → Get 55 OMR (+5 Bonus)")
                )

                tiers.forEach { (base, bonus, label) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FixItPrimaryBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.topUpWallet(base, bonus)
                                showAddFundsSheet = false
                            }
                            .testTag("wallet_topup_option_${base.toInt()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, tint = FixItPrimaryBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun WalletHistoryItem(
    title: String,
    subtitle: String,
    amount: String,
    isCredit: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isCredit) Color(0xFF064E3B) else Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCredit) Icons.Default.SouthWest else Icons.Default.NorthEast,
                contentDescription = null,
                tint = if (isCredit) FixItEmerald else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }

        Text(
            text = amount,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCredit) FixItEmerald else Color.White
        )
    }
}

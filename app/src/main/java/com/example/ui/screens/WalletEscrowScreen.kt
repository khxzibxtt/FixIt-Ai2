package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItBlue
import com.example.ui.theme.FixItEmerald
import com.example.ui.viewmodel.FixItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletEscrowScreen(
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()

    var showBankReceiptDialog by remember { mutableStateOf(false) }
    var selectedTopUpBase by remember { mutableStateOf(30.0) }
    var selectedTopUpBonus by remember { mutableStateOf(5.0) }
    var simulatedReceiptReference by remember { mutableStateOf("BM-9281740-MUSCAT") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dual-Wallet & Escrow Ledger", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("wallet_balance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Available Balance", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(
                        "${String.format("%.2f", userProfile?.walletBalance ?: 0.0)} OMR",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = FixItEmerald
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Locked in Escrow", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                "${String.format("%.2f", userProfile?.lockedEscrowBalance ?: 0.0)} OMR",
                                fontWeight = FontWeight.Bold,
                                color = FixItAmber
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Warranty Status", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("100% Protected", fontWeight = FontWeight.Bold, color = FixItBlue)
                        }
                    }
                }
            }

            // Top-up Section (Module 04: The consumer top-up scale)
            Text("Consumer Top-Up Scale (Compounding Bonus)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(
                "Designed to reward larger commitments with bonus liquidity. Flat +5 ceiling at the 30 to 50 tier keeps the system sustainable.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val tiers = listOf(
                Triple(10.0, 11.0, 1.0),
                Triple(20.0, 23.0, 3.0),
                Triple(30.0, 35.0, 5.0),
                Triple(40.0, 45.0, 5.0),
                Triple(50.0, 55.0, 5.0)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tiers.forEach { (base, credited, bonus) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedTopUpBase == base) FixItAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (selectedTopUpBase == base) androidx.compose.foundation.BorderStroke(1.5.dp, FixItAmber) else null,
                        modifier = Modifier.fillMaxWidth().testTag("tier_${base.toInt()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTopUpBase == base,
                                onClick = {
                                    selectedTopUpBase = base
                                    selectedTopUpBonus = bonus
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Pay ${base.toInt()} OMR", fontWeight = FontWeight.Bold)
                                Text("Get ${credited.toInt()} OMR Credited (+${bonus.toInt()} OMR Platform Bonus)", style = MaterialTheme.typography.bodySmall, color = FixItEmerald)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = {
                                    selectedTopUpBase = base
                                    selectedTopUpBonus = bonus
                                    showBankReceiptDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("select_tier_${base.toInt()}_button")
                            ) {
                                Text("Top Up", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Vendor Accumulation Ledger & Payout Windows (Module 04)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vendor Accumulation Ledger", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "• Earnings accumulate in internal wallet to eliminate repeated transaction fees.\n• Payout windows: Tuesday & Friday batch transfers.\n• Full 100% Khidmah stays locked during warranty—FixIt 20% cut is only taken once warranty clears cleanly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (activeRole == "vendor") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.setNotice("Payout of ${(userProfile?.walletBalance ?: 0.0)} OMR queued for next Tuesday batch window.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FixItBlue),
                            modifier = Modifier.fillMaxWidth().testTag("request_payout_button")
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = "Bank", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Request Payout to Bank Account", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal: Manual Admin Verification Gate (Module 04)
    if (showBankReceiptDialog) {
        AlertDialog(
            onDismissRequest = { showBankReceiptDialog = false },
            title = { Text("Manual Admin Verification Gate") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "To prevent fraudulent chargebacks, bank transfer confirmations require a verification gate before live deployment.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = simulatedReceiptReference,
                        onValueChange = { simulatedReceiptReference = it },
                        label = { Text("Bank Transfer Reference / Screenshot") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Credited: ${selectedTopUpBase + selectedTopUpBonus} OMR (includes +${selectedTopUpBonus} OMR bonus)", fontWeight = FontWeight.Bold, color = FixItEmerald)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.topUpWallet(selectedTopUpBase, selectedTopUpBonus)
                        showBankReceiptDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                    modifier = Modifier.testTag("confirm_topup_button")
                ) {
                    Text("Confirm & Credit Wallet", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBankReceiptDialog = false }) { Text("Cancel") }
            }
        )
    }
}

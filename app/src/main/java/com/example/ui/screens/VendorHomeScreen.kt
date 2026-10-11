package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItBlue
import com.example.ui.theme.FixItEmerald
import com.example.ui.viewmodel.FixItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorHomeScreen(
    viewModel: FixItViewModel,
    onNavigateToJobDetail: (String) -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToSubscriptions: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()

    val isAvailableNow = userProfile?.isAvailableNow ?: false
    val isPro = userProfile?.plan == "pro"

    // Filter jobs by vendor skills (Module 02: multi-skill tagging guardrail)
    val vendorSkills = userProfile?.skills ?: listOf("Plumbing", "Electrical", "HVAC")
    val eligibleJobs = allJobs.filter { job ->
        vendorSkills.any { skill -> job.serviceCategory.contains(skill, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Engineering, contentDescription = "Vendor", tint = FixItAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vendor Hub", fontWeight = FontWeight.Bold)
                        if (userProfile?.isVerifiedSeller == true || isPro) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Blue Tick", tint = FixItBlue, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.switchRole("consumer") }) {
                        Icon(Icons.Default.Person, contentDescription = "Switch to Consumer", tint = FixItBlue)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Vendor Credentials & Bat-Signal Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("vendor_credentials_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Handyman, contentDescription = null, tint = FixItAmber)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        userProfile?.displayName ?: "Vendor",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (isPro) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = FixItAmber.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "PRO",
                                                color = FixItAmber,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    "Cleared Skills: ${vendorSkills.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // "Available Now" Bat-Signal Toggle (Module 09)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isAvailableNow) FixItEmerald else Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Available Now (Bat-Signal)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(
                                    "Broadcast pulsing radar icon to consumers for Direct Strike-Bounties.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isAvailableNow,
                                onCheckedChange = { viewModel.toggleAvailableNow(it) },
                                modifier = Modifier.testTag("bat_signal_switch")
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Bid-Back Tokens & Wallet Ledger summary
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Bid-Back Tokens", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    if (isPro) "Unlimited (Pro)" else "${userProfile?.bidTokens ?: 5} Daily Tokens",
                                    fontWeight = FontWeight.Bold,
                                    color = FixItAmber
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Accumulated Wallet", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${String.format("%.1f", userProfile?.walletBalance ?: 0.0)} OMR",
                                        fontWeight = FontWeight.Bold,
                                        color = FixItEmerald
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TextButton(
                                        onClick = onNavigateToWallet,
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Payouts", fontSize = 11.sp, color = FixItBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Upgrade to Pro Banner if free
            if (!isPro) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToSubscriptions() }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Pro", tint = FixItAmber)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Upgrade to FixIt Pro for Vendors", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Featured pinned bids, Blue Tick badge, unlimited tokens", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = Color.White)
                        }
                    }
                }
            }

            // Skill Tag Proof Action (Module 02)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Job Matches (${eligibleJobs.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onNavigateToCatalog) {
                        Text("+ Add / Prove Skill Tag", color = FixItBlue)
                    }
                }
            }

            if (eligibleJobs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No jobs matching your cleared skills.", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(eligibleJobs) { job ->
                    JobCardItem(job = job, onClick = { onNavigateToJobDetail(job.id) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun JobCardItem(
    job: com.example.data.model.Job,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("vendor_job_item_${job.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FixItAmber.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = job.serviceCategory.ifBlank { "General Repair" },
                        color = FixItAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (job.status) {
                        "open_for_bids" -> FixItBlue.copy(alpha = 0.2f)
                        "in_progress", "price_locked" -> FixItEmerald.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = job.status.replace("_", " ").uppercase(),
                        color = when (job.status) {
                            "open_for_bids" -> FixItBlue
                            "in_progress", "price_locked" -> FixItEmerald
                            else -> Color.LightGray
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = job.title.ifBlank { "${job.serviceCategory} Service" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (job.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = job.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                val priceDisplay = if (job.biddingMode == "bounty_bargain" && job.bountyPrice != null) {
                    "Bounty: $${String.format("%.2f", job.bountyPrice)}"
                } else if (job.agreedLaborFee > 0.0) {
                    "Locked: $${String.format("%.2f", job.agreedLaborFee)}"
                } else {
                    "Floor: ~$${String.format("%.2f", job.aiFloorPrice)}"
                }

                Text(
                    text = priceDisplay,
                    fontWeight = FontWeight.Bold,
                    color = FixItEmerald,
                    fontSize = 14.sp
                )
            }
        }
    }
}

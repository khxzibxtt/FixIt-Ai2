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
fun SubscriptionsScreen(
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currentPlan = userProfile?.plan ?: "free"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monetization & Memberships", fontWeight = FontWeight.Bold) },
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
            Text(
                text = "The 20% take-rate on closed jobs is the core revenue engine. Plus & Pro tiers provide premium operational tools for active participants.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // FixIt Plus Card (For Consumers)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (currentPlan == "plus") FixItBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (currentPlan == "plus") androidx.compose.foundation.BorderStroke(2.dp, FixItBlue) else null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("plan_plus_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Diamond, contentDescription = "Plus", tint = FixItBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FixIt Plus (Consumers)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("4.9 OMR/mo", fontWeight = FontWeight.Bold, color = FixItBlue)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureBullet("Media allowance: Up to 10 images & 3-5 videos per job")
                    FeatureBullet("AI engine access: 20 bilingual AI rewrite uses per week")
                    FeatureBullet("Random coupon drops: Platform-subsidized discounts")
                    FeatureBullet("Verified Customer badge: Signals funds ready to vendors")
                    FeatureBullet("Direct vendor visibility: See Pro vendor's full shop profile")

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.upgradePlan("plus") },
                        colors = ButtonDefaults.buttonColors(containerColor = FixItBlue),
                        modifier = Modifier.fillMaxWidth().testTag("upgrade_plus_button")
                    ) {
                        Text(if (currentPlan == "plus") "Current Active Plan" else "Activate FixIt Plus", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // FixIt Pro Card (For Vendors)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (currentPlan == "pro") FixItAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (currentPlan == "pro") androidx.compose.foundation.BorderStroke(2.dp, FixItAmber) else null,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("plan_pro_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = "Pro", tint = FixItAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FixIt Pro (Vendors)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("9.9 OMR/mo", fontWeight = FontWeight.Bold, color = FixItAmber)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureBullet("Featured Bids: Pin quotes to top with highlighted border")
                    FeatureBullet("Unlimited bidding: No daily cap on bid tokens")
                    FeatureBullet("Rich shop profile: Digital storefront, portfolio, and bio")
                    FeatureBullet("The Blue Tick: Verified Seller trust badge on all bids")
                    FeatureBullet("Prominent reviews display to drive higher job conversion")

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.upgradePlan("pro") },
                        colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                        modifier = Modifier.fillMaxWidth().testTag("upgrade_pro_button")
                    ) {
                        Text(if (currentPlan == "pro") "Current Active Plan" else "Upgrade to FixIt Pro", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FeatureBullet(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = FixItEmerald, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import com.example.data.model.Bid
import com.example.data.model.Job
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItBlue
import com.example.ui.theme.FixItEmerald
import com.example.ui.theme.FixItError
import com.example.ui.viewmodel.FixItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: String,
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDispute: () -> Unit,
    onNavigateToEscortMap: () -> Unit,
    onNavigateToChat: (String, String) -> Unit = { _, _ -> }
) {
    LaunchedEffect(jobId) {
        viewModel.selectJob(jobId)
    }

    val job by viewModel.selectedJob.collectAsState()
    val bids by viewModel.selectedJobBids.collectAsState()
    val receipts by viewModel.selectedJobReceipts.collectAsState()
    val messages by viewModel.selectedJobMessages.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()

    var showPlaceBidDialog by remember { mutableStateOf(false) }
    var bidPriceText by remember { mutableStateOf("20.0") }
    var bidWarrantyDaysText by remember { mutableStateOf("30") }
    var bidNoteText by remember { mutableStateOf("") }

    var showAddReceiptDialog by remember { mutableStateOf(false) }
    var receiptShopText by remember { mutableStateOf("Ruwi Plumbing Mart") }
    var receiptAmountText by remember { mutableStateOf("6.50") }

    var chatInputText by remember { mutableStateOf("") }
    var customWarrantyDays by remember { mutableStateOf(30L) }

    val jobReviews by viewModel.selectedJobReviews.collectAsState()
    var userRating by remember { mutableStateOf(5.0) }
    var workmanshipScore by remember { mutableStateOf(5.0) }
    var punctualityScore by remember { mutableStateOf(5.0) }
    var reviewCommentText by remember { mutableStateOf("") }
    var isReviewSubmitting by remember { mutableStateOf(false) }
    var reviewSubmitMessage by remember { mutableStateOf<String?>(null) }

    if (job == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Job Order") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FixItAmber)
            }
        }
        return
    }

    val currentJob = job!!
    val isConsumer = userProfile?.userId == currentJob.consumerId || activeRole == "consumer"
    val isEscrowLocked = currentJob.status != "open_for_bids" && currentJob.status != "awaiting_admin_verification"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentJob.title, maxLines = 1, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Order #${currentJob.id.take(8)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentJob.status == "warranty_active" || currentJob.status == "completed") {
                        IconButton(onClick = onNavigateToDispute) {
                            Icon(Icons.Default.Gavel, contentDescription = "Tribunal", tint = FixItAmber)
                        }
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
            // Pipeline Stage Tracker
            PipelineProgressCard(status = currentJob.status)

            // Contact Masking Protection Banner (Module 08)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isEscrowLocked) FixItEmerald.copy(alpha = 0.15f) else Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isEscrowLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Contact Security",
                        tint = if (isEscrowLocked) FixItEmerald else FixItAmber
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEscrowLocked) "Payment Verified: Contact Channels Unlocked" else "Privacy Mask Active: Zero Contact Before Escrow",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isEscrowLocked) FixItEmerald else Color.White
                        )
                        Text(
                            text = if (isEscrowLocked) "Direct communication & live coordination active for this job." else "Phone numbers & exact address stay hidden to eliminate off-platform poaching.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }
            }

            // PAYMENT STATUS: ESCROW PROTECTED SUMMARY CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF132A24)),
                border = androidx.compose.foundation.BorderStroke(1.dp, FixItEmerald.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("job_detail_escrow_status_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = FixItEmerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment Status: Escrow Protected",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = FixItEmerald
                            )
                        }

                        val heldAmount = if (currentJob.agreedLaborFee > 0) currentJob.agreedLaborFee
                        else if (currentJob.escrowAmount > 0) currentJob.escrowAmount
                        else if (currentJob.budgetMax > 0) currentJob.budgetMax
                        else (currentJob.bountyPrice ?: 25.0)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FixItEmerald.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "%.1f OMR Locked".format(heldAmount),
                                color = Color(0xFF34D399),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isEscrowLocked) {
                            "100% Funds Held in Escrow Vault for this ongoing task. The technician will only receive payout after you review and approve the final work verification photos."
                        } else {
                            "Escrow Ready: When you accept a vendor quote, funds are held securely in platform escrow. Zero upfront cash risk."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD1FAE5),
                        lineHeight = 16.sp
                    )

                    if (currentJob.budgetRange.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Initial Budget Range: ${currentJob.budgetRange}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FixItBlue
                        )
                    }
                }
            }

            // Job Overview & Media Card (Glassmorphic)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                    )
                ),
                modifier = Modifier.fillMaxWidth().testTag("job_detail_overview_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = FixItBlue.copy(alpha = 0.2f)) {
                            Text(
                                currentJob.serviceCategory,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = FixItBlue,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(10.dp), color = FixItAmber.copy(alpha = 0.2f)) {
                            Text(
                                if (currentJob.biddingMode == "bounty_bargain") "Bounty Bargain" else "Blind Bidding",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = FixItAmber,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Floor: ${currentJob.aiFloorPrice} OMR",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Full Job Description",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentJob.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI-SCOPED REQUIREMENTS SECTION (User explicit requirement)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FixItBlue.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = "AI Scoped",
                                        tint = FixItBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI-Scoped Requirements",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.generateJobAiRequirements(
                                            currentJob.id,
                                            currentJob.title,
                                            currentJob.description,
                                            currentJob.serviceCategory
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = FixItBlue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Regen", fontSize = 11.sp, color = FixItBlue)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val requirementsList = if (currentJob.aiRequirements.isNotEmpty()) {
                                currentJob.aiRequirements
                            } else {
                                listOf(
                                    "Isolate main power / water supply lines before opening connections",
                                    "Conduct physical diagnostics using standard trade calibration tools",
                                    "Replace damaged valves, wiring terminals, or worn mechanical seals",
                                    "Perform 15-minute operational pressure and safety test under active load",
                                    "Provide before & after proof verification photos for Escrow release"
                                )
                            }

                            requirementsList.forEach { req ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = FixItEmerald,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = req,
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Location & Contact Privacy Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = FixItBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentJob.locationName} • GPS: 23.588° N, 58.383° E",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // PROMINENT 'CONTACT' BUTTON (User explicit requirement)
                    Button(
                        onClick = {
                            val vendorToContact = currentJob.selectedVendorName ?: "Assigned Laborer"
                            onNavigateToChat(currentJob.id, vendorToContact)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("contact_laborer_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FixItBlue)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Contact", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentJob.selectedVendorName != null) "Contact ${currentJob.selectedVendorName}" else "Contact Laborer / Project Chat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // SECTION 1: BIDDING ENGINE (Module 03 & 04)
            if (currentJob.status == "open_for_bids") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("bidding_section_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, contentDescription = "Bids", tint = FixItAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bids & Counter-Offers (${bids.size})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Khidmah (labor-only). Competitor bids are blind. No chat before payment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (bids.isEmpty()) {
                            Text(
                                "No bids yet. Local cleared tradespeople are reviewing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                bids.forEach { bid ->
                                    BidItemRow(
                                        bid = bid,
                                        isConsumer = isConsumer,
                                        customWarrantyDays = customWarrantyDays,
                                        onWarrantyChange = { customWarrantyDays = it },
                                        onAccept = {
                                            viewModel.acceptBidAndLockEscrow(currentJob.id, bid, customWarrantyDays)
                                        }
                                    )
                                }
                            }
                        }

                        // Vendor Action: Place Bid
                        if (!isConsumer || activeRole == "vendor") {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showPlaceBidDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                                modifier = Modifier.fillMaxWidth().testTag("open_bid_dialog_button")
                            ) {
                                Text("Submit Blind Labor Quote", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // SECTION 2: LIVE TRACKING (Module 08)
            if (currentJob.status == "price_locked" || currentJob.status == "on_the_way" || currentJob.status == "arrived") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("tracking_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Navigation, contentDescription = "Tracking", tint = FixItBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Privacy-Locked Dispatch Tracking", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tracking turns on ONLY when vendor taps 'On My Way' and terminates completely upon arrival. No passive background tracking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (currentJob.status == "on_the_way") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FixItEmerald.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = FixItEmerald)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Vendor is On The Way (Live GPS: 23.5880° N, 58.3829° E)", fontWeight = FontWeight.Bold, color = FixItEmerald, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else if (currentJob.status == "arrived") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FixItBlue.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Vendor Arrived at Location. Tracking session terminated.", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, color = FixItBlue, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // Vendor Tracking Actions
                        if (!isConsumer || activeRole == "vendor") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (currentJob.status == "price_locked") {
                                    Button(
                                        onClick = { viewModel.vendorOnMyWay(currentJob.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = FixItBlue),
                                        modifier = Modifier.weight(1f).testTag("on_my_way_button")
                                    ) {
                                        Text("Tap 'On My Way'", color = Color.White)
                                    }
                                }
                                if (currentJob.status == "on_the_way") {
                                    Button(
                                        onClick = { viewModel.vendorArrived(currentJob.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = FixItEmerald),
                                        modifier = Modifier.weight(1f).testTag("arrived_button")
                                    ) {
                                        Text("Arrived at Location", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: ON-SITE PARTS PROTOCOL (Module 05)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("parts_protocol_section")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Parts", tint = FixItAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "On-Site Parts Protocol: ${currentJob.partsMode.replace("_", " ").uppercase()}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when (currentJob.partsMode) {
                        "multi_receipt" -> {
                            Text(
                                "Vendor buys materials from suppliers and logs individual receipts. Combined into one master billing sheet for consumer approval.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (receipts.isEmpty()) {
                                Text("No parts receipts logged yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            } else {
                                var totalParts = 0.0
                                receipts.forEach { receipt ->
                                    totalParts += receipt.amount
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = "Receipt", tint = FixItAmber, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(receipt.shopName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("${receipt.amount} OMR", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (receipt.isApprovedByConsumer) {
                                            Text("Approved", color = FixItEmerald, style = MaterialTheme.typography.labelSmall)
                                        } else if (isConsumer) {
                                            Button(
                                                onClick = { viewModel.approveReceipt(currentJob.id, receipt.id) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("Approve", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Parts Cost:", fontWeight = FontWeight.Bold)
                                    Text("${String.format("%.2f", totalParts)} OMR", fontWeight = FontWeight.Bold, color = FixItAmber)
                                }
                            }

                            if (!isConsumer || activeRole == "vendor") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showAddReceiptDialog = true },
                                    modifier = Modifier.fillMaxWidth().testTag("add_part_receipt_button")
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Receipt")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Log Store Receipt & Snap Photo")
                                }
                            }
                        }

                        "escort_mode" -> {
                            Text(
                                "Escort Mode Active: You accompany the vendor to the local shop and pay directly. Zero markup, zero platform parts fees.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onNavigateToEscortMap,
                                colors = ButtonDefaults.buttonColors(containerColor = FixItBlue),
                                modifier = Modifier.fillMaxWidth().testTag("find_supply_stores_button")
                            ) {
                                Icon(Icons.Default.Store, contentDescription = "Stores")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Find Nearby Supply Stores (Maps Grounded)", color = Color.White)
                            }
                        }

                        else -> {
                            Text(
                                "No parts needed. Labor-only diagnostic, tune-up, or fix. Proceeding directly to Triple-Verify.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // SECTION 4: TRIPLE-VERIFY COMPLETION (Module 06)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("triple_verify_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FactCheck, contentDescription = "Triple Verify", tint = FixItEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Triple-Verify Completion", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Removes 'he said, she said' fraud by requiring photographic proof from BOTH sides before any escrow release.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step 1: Vendor After photo
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (currentJob.vendorAfterPhotoUrl != null) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Status",
                            tint = if (currentJob.vendorAfterPhotoUrl != null) FixItEmerald else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1. Vendor In-App 'After' Photo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.weight(1f))
                        if (currentJob.vendorAfterPhotoUrl == null && (!isConsumer || activeRole == "vendor")) {
                            Button(
                                onClick = {
                                    viewModel.uploadVendorAfterPhoto(
                                        currentJob.id,
                                        "https://images.unsplash.com/photo-1581092335397-9583fe92d232?auto=format&fit=crop&w=600&q=80"
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("vendor_upload_photo_button")
                            ) {
                                Text("Snap After Photo", fontSize = 11.sp)
                            }
                        } else if (currentJob.vendorAfterPhotoUrl != null) {
                            Text("Uploaded", color = FixItEmerald, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 2: Consumer After photo
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (currentJob.consumerAfterPhotoUrl != null) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Status",
                            tint = if (currentJob.consumerAfterPhotoUrl != null) FixItEmerald else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2. Consumer Verification Photo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.weight(1f))
                        if (currentJob.consumerAfterPhotoUrl == null && isConsumer) {
                            Button(
                                onClick = {
                                    viewModel.uploadConsumerAfterPhoto(
                                        currentJob.id,
                                        "https://images.unsplash.com/photo-1581092335397-9583fe92d232?auto=format&fit=crop&w=600&q=80"
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("consumer_upload_photo_button")
                            ) {
                                Text("Confirm Photo", fontSize = 11.sp)
                            }
                        } else if (currentJob.consumerAfterPhotoUrl != null) {
                            Text("Confirmed", color = FixItEmerald, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Final Confirmation Question (Module 06)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Final Confirmation: Is the Job Done?",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "YES -> 80% released to Vendor Wallet / 20% to FixIt. Agreed ${currentJob.agreedWarrantyDays}-day Peer Warranty locks.\nNO -> Escrow frozen, Dispute Mode triggered.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )

                            if (currentJob.status != "warranty_active" && currentJob.status != "completed" && currentJob.status != "disputed") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.confirmJobDone(
                                                currentJob.id,
                                                currentJob.selectedVendorId ?: "demo_vendor_pro",
                                                currentJob.agreedLaborFee,
                                                currentJob.agreedWarrantyDays
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = FixItEmerald),
                                        modifier = Modifier.weight(1f).testTag("confirm_job_done_button")
                                    ) {
                                        Text("YES, Job Done", color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.disputeJob(currentJob.id, "Work incomplete / sub-par finish")
                                            onNavigateToDispute()
                                        },
                                        modifier = Modifier.weight(1f).testTag("trigger_dispute_button")
                                    ) {
                                        Text("NO, Dispute", color = FixItError)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 5: MESSAGING & RAW VOICE NOTES (Module 06)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("messaging_section")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, contentDescription = "Messages", tint = FixItBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("On-Site Chat & Voice Notes (WhatsApp-Style)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (messages.isEmpty()) {
                        Text("No messages yet. Send a quick voice note or update.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            messages.forEach { msg ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (msg.senderId == userProfile?.userId) FixItBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (msg.isVoiceNote) {
                                            Icon(Icons.Default.Mic, contentDescription = "Voice Note", tint = FixItAmber, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("${msg.senderName}: Voice Note (${msg.voiceDurationSeconds ?: 5}s)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                            Spacer(modifier = Modifier.weight(1f))
                                            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = FixItAmber)
                                            }
                                        } else {
                                            Text("${msg.senderName}: ${msg.text ?: ""}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Message...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f).height(50.dp).testTag("chat_input_field"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                viewModel.sendTextMessage(currentJob.id, chatInputText)
                                chatInputText = ""
                            },
                            modifier = Modifier.testTag("send_text_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = FixItBlue)
                        }
                        IconButton(
                            onClick = {
                                viewModel.sendVoiceNote(currentJob.id, 8L)
                            },
                            modifier = Modifier.testTag("send_voice_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Note", tint = FixItAmber)
                        }
                    }
                }
            }

            // SECTION 6: USER RATING & REVIEW SYSTEM (Module 09 / Firestore Stored)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(FixItAmber.copy(alpha = 0.3f), FixItEmerald.copy(alpha = 0.2f))
                    )
                ),
                modifier = Modifier.fillMaxWidth().testTag("job_reviews_section")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = FixItAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Laborer Reputation & Reviews",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FixItAmber.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${jobReviews.size} Verified",
                                color = FixItAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Existing reviews from Firestore
                    if (jobReviews.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            jobReviews.forEach { rev ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = rev.consumerName.ifBlank { "Client" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = FixItAmber, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "%.1f".format(rev.rating),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = FixItAmber
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = rev.comment,
                                            fontSize = 12.sp,
                                            color = Color(0xFFCBD5E1),
                                            lineHeight = 16.sp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                text = "Workmanship: %.1f★".format(rev.workmanshipRating),
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                            Text(
                                                text = "Punctuality: %.1f★".format(rev.punctualityRating),
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Leave a review form (For completed or ongoing jobs)
                    Text(
                        text = "Rate Laborer & Submit Firestore Review",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FixItEmerald
                    )
                    Text(
                        text = "Your review builds technician reputation and ensures escrow quality standards.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Overall Stars Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Overall Rating:", fontSize = 12.sp, color = Color.White)
                        Row {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { userRating = star.toDouble() },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (userRating >= star) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$star Stars",
                                        tint = if (userRating >= star) FixItAmber else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Workmanship Rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Workmanship Quality:", fontSize = 12.sp, color = Color.White)
                        Row {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { workmanshipScore = star.toDouble() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = if (workmanshipScore >= star) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (workmanshipScore >= star) FixItEmerald else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Punctuality Rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Punctuality & Timing:", fontSize = 12.sp, color = Color.White)
                        Row {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { punctualityScore = star.toDouble() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = if (punctualityScore >= star) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (punctualityScore >= star) FixItBlue else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reviewCommentText,
                        onValueChange = { reviewCommentText = it },
                        placeholder = { Text("Write your review (e.g., punctuality, trade skill, cleanliness)...", fontSize = 12.sp, color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("review_comment_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FixItAmber,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        minLines = 2,
                        maxLines = 4
                    )

                    if (reviewSubmitMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reviewSubmitMessage ?: "",
                            fontSize = 11.sp,
                            color = FixItEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val vendorId = currentJob.selectedVendorId ?: "demo_vendor_tariq"
                            val vendorName = currentJob.selectedVendorName ?: "Tariq Master Plumbing"
                            isReviewSubmitting = true
                            viewModel.submitJobReview(
                                jobId = currentJob.id,
                                vendorId = vendorId,
                                vendorName = vendorName,
                                rating = userRating,
                                workmanship = workmanshipScore,
                                punctuality = punctualityScore,
                                comment = reviewCommentText
                            ) { success, msg ->
                                isReviewSubmitting = false
                                reviewSubmitMessage = msg
                                if (success) {
                                    reviewCommentText = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("submit_review_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                        enabled = !isReviewSubmitting
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Review to Firestore", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal: Submit Blind Labor Quote
    if (showPlaceBidDialog) {
        AlertDialog(
            onDismissRequest = { showPlaceBidDialog = false },
            title = { Text("Submit Blind Labor Quote (Khidmah)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Labor price only. Parts are handled separately on location.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = bidPriceText,
                        onValueChange = { bidPriceText = it },
                        label = { Text("Labor Fee (OMR)") },
                        modifier = Modifier.fillMaxWidth().testTag("bid_price_input")
                    )
                    OutlinedTextField(
                        value = bidWarrantyDaysText,
                        onValueChange = { bidWarrantyDaysText = it },
                        label = { Text("Proposed Warranty Duration (Days)") },
                        modifier = Modifier.fillMaxWidth().testTag("bid_warranty_input")
                    )
                    OutlinedTextField(
                        value = bidNoteText,
                        onValueChange = { bidNoteText = it },
                        label = { Text("Technical Note / Credentials") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = bidPriceText.toDoubleOrNull() ?: 20.0
                        val days = bidWarrantyDaysText.toLongOrNull() ?: 15L
                        viewModel.placeBid(currentJob.id, price, days, bidNoteText)
                        showPlaceBidDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FixItAmber)
                ) {
                    Text("Submit Quote", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlaceBidDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Log Store Receipt
    if (showAddReceiptDialog) {
        AlertDialog(
            onDismissRequest = { showAddReceiptDialog = false },
            title = { Text("Log On-Site Store Receipt") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = receiptShopText,
                        onValueChange = { receiptShopText = it },
                        label = { Text("Shop / Supplier Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = receiptAmountText,
                        onValueChange = { receiptAmountText = it },
                        label = { Text("Exact Receipt Amount (OMR)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = receiptAmountText.toDoubleOrNull() ?: 5.0
                        viewModel.logPartReceipt(
                            currentJob.id,
                            receiptShopText,
                            amt,
                            "https://images.unsplash.com/photo-1554415707-9e4466bfe032?auto=format&fit=crop&w=600&q=80"
                        )
                        showAddReceiptDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FixItAmber)
                ) {
                    Text("Add to Billing Sheet", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReceiptDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PipelineProgressCard(status: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Order Pipeline State", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StepCircle("1. Bids", active = true, completed = status != "open_for_bids")
                StepCircle("2. Escrow", active = status != "open_for_bids", completed = status == "on_the_way" || status == "arrived" || status == "verification_pending" || status == "warranty_active" || status == "completed")
                StepCircle("3. On-Site", active = status == "on_the_way" || status == "arrived", completed = status == "verification_pending" || status == "warranty_active" || status == "completed")
                StepCircle("4. Verify", active = status == "verification_pending", completed = status == "warranty_active" || status == "completed")
                StepCircle("5. Warranty", active = status == "warranty_active", completed = status == "completed")
            }
        }
    }
}

@Composable
private fun StepCircle(label: String, active: Boolean, completed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (completed) FixItEmerald else if (active) FixItAmber else Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = if (active || completed) Color.White else Color.Gray)
    }
}

@Composable
private fun BidItemRow(
    bid: Bid,
    isConsumer: Boolean,
    customWarrantyDays: Long,
    onWarrantyChange: (Long) -> Unit,
    onAccept: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = if (bid.isFeatured) androidx.compose.foundation.BorderStroke(1.5.dp, FixItAmber) else null,
        modifier = Modifier.fillMaxWidth().testTag("bid_item_${bid.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bid.vendorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                if (bid.vendorIsPro) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = FixItAmber.copy(alpha = 0.2f)) {
                        Text("PRO FEATURED", color = FixItAmber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 9.sp)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("${bid.laborPrice} OMR", fontWeight = FontWeight.Bold, color = FixItAmber, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Vendor offers: ${bid.proposedWarrantyDays}-Day Peer Warranty • Khidmah labor only",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!bid.counterNote.isNullOrBlank()) {
                Text("\"${bid.counterNote}\"", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            }

            if (isConsumer && bid.status == "submitted") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Warranty Counter:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = customWarrantyDays == 15L,
                        onClick = { onWarrantyChange(15L) },
                        label = { Text("15d", fontSize = 11.sp) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = customWarrantyDays == 30L,
                        onClick = { onWarrantyChange(30L) },
                        label = { Text("30d", fontSize = 11.sp) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = customWarrantyDays == 0L,
                        onClick = { onWarrantyChange(0L) },
                        label = { Text("0d", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                    modifier = Modifier.fillMaxWidth().testTag("accept_bid_button_${bid.id}")
                ) {
                    Text("Lock Escrow (${bid.laborPrice} OMR)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

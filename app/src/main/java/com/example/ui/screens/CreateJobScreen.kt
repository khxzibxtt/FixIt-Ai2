package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItBlue
import com.example.ui.theme.FixItEmerald
import com.example.ui.theme.FixItPrimaryBlue
import com.example.ui.viewmodel.FixItViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobScreen(
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit,
    onJobCreated: (String) -> Unit
) {
    val services by viewModel.services.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiRewrittenText by viewModel.aiRewriteResult.collectAsState()
    val aiPriceRange by viewModel.aiPriceFloorEstimate.collectAsState()

    val defaultCategories = listOf(
        "Plumbing", "Electrical", "AC Repair", "Carpentry", "Painting", "Cleaning", "Appliance", "Masonry"
    )

    var selectedCategory by remember { mutableStateOf("Plumbing") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var minBudgetText by remember { mutableStateOf("20") }
    var maxBudgetText by remember { mutableStateOf("45") }
    var selectedBudgetPreset by remember { mutableStateOf("25 - 50 OMR (Standard)") }
    var locationName by remember { mutableStateOf("Al Khuwair, Muscat") }
    var urgency by remember { mutableStateOf("Standard") } // "Emergency", "Standard", "Flexible"
    var biddingMode by remember { mutableStateOf("blind_bidding") } // blind_bidding, bounty_bargain
    var bountyPriceText by remember { mutableStateOf("25.0") }
    var partsMode by remember { mutableStateOf("multi_receipt") } // multi_receipt, escort_mode, no_parts
    var showCategoryDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(aiRewrittenText) {
        if (!aiRewrittenText.isNullOrBlank()) {
            description = aiRewrittenText ?: ""
            if (title.isBlank()) {
                title = "$selectedCategory Diagnostic & Repair"
            }
        }
    }

    LaunchedEffect(aiPriceRange) {
        aiPriceRange?.let { range ->
            minBudgetText = range.first.toInt().toString()
            maxBudgetText = range.second.toInt().toString()
        }
    }

    val suggestedTitles = remember(selectedCategory) {
        when (selectedCategory) {
            "Plumbing" -> listOf("Leaking pipe under kitchen sink", "Water heater pressure valve leak", "Toilet tank siphon repair")
            "Electrical" -> listOf("Distribution board breaker tripping", "Living room short circuit fix", "New 3-phase outlet installation")
            "AC Repair" -> listOf("Split AC unit blowing warm air", "AC drainage water leak into room", "Gas recharge & compressor check")
            "Carpentry" -> listOf("Front door deadbolt mechanism jammed", "Kitchen cabinet hinge replacement", "Custom wooden wardrobe shelving")
            "Painting" -> listOf("Living room interior wall repaint", "Ceiling moisture stain sealing", "Exterior gate weatherproof painting")
            "Cleaning" -> listOf("Deep villa move-in cleaning", "Sofa & upholstery steam extraction", "Water tank hygiene sterilization")
            else -> listOf("Urgent diagnostic & maintenance", "General home fix ticket", "Equipment repair & tune-up")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Post a New Job", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Escrow Protected Marketplace Ticket",
                            fontSize = 11.sp,
                            color = FixItEmerald
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ESCROW PROTECTION GUARANTEE HERO BANNER
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF132A24)),
                border = androidx.compose.foundation.BorderStroke(1.dp, FixItEmerald.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("escrow_guarantee_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FixItEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "Escrow Protected",
                            tint = FixItEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "100% Escrow Protected",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FixItEmerald
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = FixItEmerald.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    "SAFE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FixItEmerald,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            "Funds for your job budget remain safe in Escrow. Payment is released to technician only when you approve final work.",
                            fontSize = 11.sp,
                            color = Color(0xFFD1FAE5)
                        )
                    }
                }
            }

            // 1. SERVICE CATEGORY
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "1. Service Category",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Text(
                        "Selected: $selectedCategory",
                        fontSize = 12.sp,
                        color = FixItBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val allCategories = if (services.isNotEmpty()) {
                        (services.map { it.name } + defaultCategories).distinct()
                    } else {
                        defaultCategories
                    }
                    items(allCategories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) FixItPrimaryBlue else Color(0xFF1E293B),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FixItBlue) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .testTag("category_chip_$cat")
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Or Select from Dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
                ) {
                    OutlinedTextField(
                        value = "Category: $selectedCategory",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("service_category_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = FixItBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        val allCategories = if (services.isNotEmpty()) {
                            (services.map { it.name } + defaultCategories).distinct()
                        } else {
                            defaultCategories
                        }
                        allCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = Color.White) },
                                onClick = {
                                    selectedCategory = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. JOB TITLE
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "2. Job Title",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("e.g. Leaking Water Pipe Under Kitchen Sink", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = FixItBlue,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                // Quick suggestions
                Text("Quick suggestions:", fontSize = 11.sp, color = Color(0xFF64748B))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestedTitles) { sug ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.clickable { title = sug }
                        ) {
                            Text(
                                text = sug,
                                fontSize = 11.sp,
                                color = Color(0xFF93C5FD),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. DESCRIPTION (with AI Rewrite & Price Floor Check)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = FixItAmber)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3. Description & Problem Details",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "AI Polisher",
                            style = MaterialTheme.typography.labelSmall,
                            color = FixItAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Describe what needs fixing. You can type in rough language, Arabic, or English, and tap AI Polish to generate technical ticket specs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed problem description...", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("job_description_input")
                            .testTag("rough_description_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = FixItBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.triggerAiRewrite(description, selectedCategory)
                        },
                        enabled = !isAiLoading && description.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_rewrite_button")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Problem with Gemini...", color = Color.Black)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Rewrite", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Polish & Market Price Floor Check", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (aiPriceRange != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FixItEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, contentDescription = "Floor", tint = FixItEmerald, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Estimated Market Floor: ${aiPriceRange?.first?.toInt()} OMR — ${aiPriceRange?.second?.toInt()} OMR (Sabotage protection active)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FixItEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 4. BUDGET RANGE & ESCROW LOCK
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("budget_range_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = "Budget", tint = FixItEmerald)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "4. Budget Range (OMR)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                        }

                        val minVal = minBudgetText.toDoubleOrNull() ?: 20.0
                        val maxVal = maxBudgetText.toDoubleOrNull() ?: 45.0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FixItEmerald.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${minVal.toInt()} - ${maxVal.toInt()} OMR",
                                color = FixItEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Set your estimated budget range. Verified vendors will submit competitive quotes within this window.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Budget Chips
                    val budgetPresets = listOf(
                        "10 - 25 OMR (Minor Fix)" to Pair(10, 25),
                        "25 - 50 OMR (Standard)" to Pair(25, 50),
                        "50 - 100 OMR (Major Repair)" to Pair(50, 100),
                        "100 - 200 OMR (Comprehensive)" to Pair(100, 200)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(budgetPresets) { (label, range) ->
                            val isSelected = selectedBudgetPreset == label
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) FixItEmerald.copy(alpha = 0.25f) else Color(0xFF0F172A),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, FixItEmerald) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.clickable {
                                    selectedBudgetPreset = label
                                    minBudgetText = range.first.toString()
                                    maxBudgetText = range.second.toString()
                                }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) FixItEmerald else Color(0xFF94A3B8),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Min and Max Budget Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = minBudgetText,
                            onValueChange = { minBudgetText = it },
                            label = { Text("Minimum Budget (OMR)", color = Color.Gray, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("min_budget_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = FixItEmerald,
                                unfocusedBorderColor = Color(0xFF334155)
                            )
                        )

                        OutlinedTextField(
                            value = maxBudgetText,
                            onValueChange = { maxBudgetText = it },
                            label = { Text("Maximum Budget (OMR)", color = Color.Gray, fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("max_budget_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = FixItEmerald,
                                unfocusedBorderColor = Color(0xFF334155)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ESCROW BADGE EXPLANATION
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FixItEmerald.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FixItEmerald.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Escrow Lock",
                                tint = FixItEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Escrow Held Status: Your approved budget is locked in FixIt Escrow. Funds are never released without your digital completion approval.",
                                fontSize = 11.sp,
                                color = Color(0xFFD1FAE5),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 5. LOCATION & SCHEDULING
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "5. Location & Urgency",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Service Location (Area / Neighborhood in Muscat)") },
                        modifier = Modifier.fillMaxWidth().testTag("location_input"),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = FixItAmber) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = FixItBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val urgencyOptions = listOf("Emergency (2 hrs)", "Standard", "Flexible")
                        urgencyOptions.forEach { opt ->
                            val isSel = urgency == opt
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) FixItPrimaryBlue else Color(0xFF0F172A),
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, FixItBlue) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { urgency = opt }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. BIDDING MODE SELECTOR
            Text("6. Bidding Engine Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color.White)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { biddingMode = "blind_bidding" }
                    ) {
                        RadioButton(
                            selected = biddingMode == "blind_bidding",
                            onClick = { biddingMode = "blind_bidding" },
                            colors = RadioButtonDefaults.colors(selectedColor = FixItBlue)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Standard Blind Bidding", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Vendors submit hidden labor quotes within your budget. No race to the bottom.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF22314A))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { biddingMode = "bounty_bargain" }
                    ) {
                        RadioButton(
                            selected = biddingMode == "bounty_bargain",
                            onClick = { biddingMode = "bounty_bargain" },
                            colors = RadioButtonDefaults.colors(selectedColor = FixItAmber)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bounty Bargain (Speed Mode)", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Name a fixed upfront price. First cleared vendor to accept wins immediately.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        }
                    }

                    if (biddingMode == "bounty_bargain") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bountyPriceText,
                            onValueChange = { bountyPriceText = it },
                            label = { Text("Your Bounty Price (OMR)") },
                            modifier = Modifier.fillMaxWidth().testTag("bounty_price_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = FixItAmber,
                                unfocusedBorderColor = Color(0xFF334155)
                            )
                        )
                    }
                }
            }

            // 7. PARTS PROTOCOL
            Text("7. On-Site Parts Protocol", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color.White)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PartsProtocolOption(
                    selected = partsMode == "multi_receipt",
                    title = "Multi-Receipt Log",
                    desc = "Vendor purchases parts, snaps store receipts, and compiles into master sheet for your 1-tap escrow approval.",
                    onClick = { partsMode = "multi_receipt" }
                )
                PartsProtocolOption(
                    selected = partsMode == "escort_mode",
                    title = "Escort Mode (Zero Markup)",
                    desc = "You shop with vendor to local supplier and pay store directly. Zero parts cash touches platform.",
                    onClick = { partsMode = "escort_mode" }
                )
                PartsProtocolOption(
                    selected = partsMode == "no_parts",
                    title = "No Parts Required",
                    desc = "Labor-only diagnostic or repair. Proceeds straight to verification with zero parts steps.",
                    onClick = { partsMode = "no_parts" }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SUBMIT BUTTON (Saves to Firestore 'jobs' collection)
            val minB = minBudgetText.toDoubleOrNull() ?: 20.0
            val maxB = maxBudgetText.toDoubleOrNull() ?: 45.0
            val budgetRangeStr = "${minB.toInt()} - ${maxB.toInt()} OMR"

            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "$selectedCategory Diagnostic & Repair" }
                    val finalDesc = description.ifBlank { "Standard labor repair and diagnostics needed for $selectedCategory in $locationName." }
                    val bounty = if (biddingMode == "bounty_bargain") bountyPriceText.toDoubleOrNull() ?: 25.0 else null

                    viewModel.postNewJob(
                        title = finalTitle,
                        description = finalDesc,
                        category = selectedCategory,
                        biddingMode = biddingMode,
                        bountyPrice = bounty,
                        partsMode = partsMode,
                        location = locationName,
                        budgetMin = minB,
                        budgetMax = maxB,
                        budgetRange = budgetRangeStr,
                        onSuccess = { newId ->
                            onJobCreated(newId)
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_job_button"),
                colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Post Job to Network (Escrow Protected)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PartsProtocolOption(
    selected: Boolean,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) FixItBlue.copy(alpha = 0.15f) else Color(0xFF162032),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, FixItBlue) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = FixItBlue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
            }
        }
    }
}

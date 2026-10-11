package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.util.PdfReportGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportExportScreen(
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val allJobs by viewModel.allJobs.collectAsState()
    val isGenerating by viewModel.isGeneratingReport.collectAsState()
    val generatedFile by viewModel.generatedPdfFile.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var selectedDaysPreset by remember { mutableStateOf(30) } // 7, 30, 90, 365, 0 (all)
    var selectedStatusFilter by remember { mutableStateOf("All Issues") }

    val now = System.currentTimeMillis()
    val startMillis = if (selectedDaysPreset > 0) now - (selectedDaysPreset * 86400000L) else 0L

    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val startDateFormatted = if (startMillis > 0) sdf.format(Date(startMillis)) else "Inception"
    val endDateFormatted = sdf.format(Date(now))

    val matchingJobs = remember(allJobs, selectedDaysPreset, selectedStatusFilter) {
        allJobs.filter { job ->
            val jobTime = (job.createdAt?.seconds ?: 0L) * 1000L
            val isInDateRange = if (selectedDaysPreset <= 0 || jobTime == 0L) true else jobTime >= startMillis

            val matchesStatus = when (selectedStatusFilter) {
                "All Issues" -> true
                "Open / Bidding" -> job.status == "open_for_bids"
                "In Progress" -> job.status == "price_locked" || job.status == "in_progress" || job.status == "on_the_way"
                "Warranty Active" -> job.status == "warranty_active"
                "Completed" -> job.status == "completed"
                "Disputed" -> job.status == "disputed"
                else -> true
            }

            isInDateRange && matchesStatus
        }
    }

    val totalEscrowMatching = matchingJobs.sumOf { it.agreedLaborFee }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Export Audit Report", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("PDF Documentation & Resolution Logs", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Description Card
            item {
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(FixItPrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Comprehensive PDF Generator", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text("Application Metadata • Resolution Steps • Timestamps", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFF22314A))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Metadata specs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Package:", fontSize = 11.sp, color = Color.Gray)
                            Text("com.aistudio.fixit.mktpl", fontSize = 11.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("App Version:", fontSize = 11.sp, color = Color.Gray)
                            Text("1.0.0 • Android Native", fontSize = 11.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Operator Account:", fontSize = 11.sp, color = Color.Gray)
                            Text(userProfile?.email ?: "khxzialt1@gmail.com", fontSize = 11.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // DATE RANGE SELECTOR
            item {
                Text(
                    text = "1. Select Date Range",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        7 to "7 Days",
                        30 to "30 Days",
                        90 to "90 Days",
                        365 to "1 Year",
                        0 to "All Time"
                    )

                    presets.forEach { (days, label) ->
                        val isSelected = selectedDaysPreset == days
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) FixItPrimaryBlue else Color(0xFF162032),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FixItPrimaryBlue else Color(0xFF22314A)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDaysPreset = days }
                                .testTag("date_range_preset_$days")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Report Window: $startDateFormatted  →  $endDateFormatted",
                    fontSize = 12.sp,
                    color = FixItAmber,
                    fontWeight = FontWeight.Medium
                )
            }

            // STATUS FILTER
            item {
                Text(
                    text = "2. Filter by Issue Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))

                val statuses = listOf("All Issues", "Open / Bidding", "In Progress", "Warranty Active", "Completed", "Disputed")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statuses.take(3).forEach { status ->
                            val isSelected = selectedStatusFilter == status
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) FixItAmber else Color(0xFF162032),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FixItAmber else Color(0xFF22314A)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedStatusFilter = status }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statuses.drop(3).forEach { status ->
                            val isSelected = selectedStatusFilter == status
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) FixItAmber else Color(0xFF162032),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FixItAmber else Color(0xFF22314A)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedStatusFilter = status }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SUMMARY PREVIEW TILES
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF162032),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Issues Found", fontSize = 11.sp, color = Color.Gray)
                            Text("${matchingJobs.size} Tickets", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FixItPrimaryBlue)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF162032),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Escrow Sum", fontSize = 11.sp, color = Color.Gray)
                            Text("$${String.format("%.2f", totalEscrowMatching)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FixItEmerald)
                        }
                    }
                }
            }

            // PRIMARY GENERATE BUTTON
            item {
                Button(
                    onClick = {
                        viewModel.generatePdfReport(
                            context = context,
                            daysPreset = selectedDaysPreset,
                            statusFilter = selectedStatusFilter
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_pdf_report_button"),
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = FixItPrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendering Vector PDF...", color = Color.White)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Detailed PDF Report", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }
            }

            // GENERATED REPORT ACTIONS CARD
            if (generatedFile != null) {
                item {
                    val file = generatedFile as File
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FixItEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FixItEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Report Ready!", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("${file.name} • ${(file.length() / 1024)} KB", fontSize = 11.sp, color = FixItEmerald)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        PdfReportGenerator.openOrSharePdf(context, file, isShare = false)
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("open_pdf_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = FixItEmerald),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        PdfReportGenerator.openOrSharePdf(context, file, isShare = true)
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("share_pdf_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FixItEmerald),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // PREVIEW LIST HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Issues in Report Scope (${matchingJobs.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text("Resolution Audit Trail", fontSize = 11.sp, color = Color.Gray)
                }
            }

            // PREVIEW OF ISSUES & RESOLUTION STEPS
            items(matchingJobs) { job ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF162032),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[${job.serviceCategory}] ${job.title}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (job.status) {
                                    "completed", "warranty_active" -> Color(0xFF064E3B)
                                    "in_progress", "price_locked" -> Color(0xFF78350F)
                                    "open_for_bids" -> Color(0xFF1E3A8A)
                                    else -> Color.DarkGray
                                }
                            ) {
                                Text(
                                    text = job.status.replace("_", " ").uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (job.status) {
                                        "completed", "warranty_active" -> Color(0xFF34D399)
                                        "in_progress", "price_locked" -> Color(0xFFFBBF24)
                                        "open_for_bids" -> Color(0xFF93C5FD)
                                        else -> Color.LightGray
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = job.description,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        // Resolution Steps summary pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Resolution Trail:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FixItAmber)
                                Text("• AI Floor calculated at $${String.format("%.1f", job.aiFloorPrice)}", fontSize = 10.sp, color = Color.LightGray)
                                if (job.selectedVendorName != null) {
                                    Text("• 100% Escrow frozen to ${job.selectedVendorName}", fontSize = 10.sp, color = Color.LightGray)
                                }
                                if (job.agreedWarrantyDays > 0) {
                                    Text("• ${job.agreedWarrantyDays}-Day peer warranty active", fontSize = 10.sp, color = Color.LightGray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ticket #${job.id.take(8)}", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = if (job.agreedLaborFee > 0) "Fee: $${String.format("%.2f", job.agreedLaborFee)}" else "Floor: ~$${String.format("%.2f", job.aiFloorPrice)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FixItEmerald
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

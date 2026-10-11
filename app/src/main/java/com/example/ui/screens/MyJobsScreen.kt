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
import com.example.data.model.Job
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItEmerald
import com.example.ui.theme.FixItPrimaryBlue
import com.example.ui.viewmodel.FixItViewModel

@Composable
fun MyJobsScreen(
    viewModel: FixItViewModel,
    onNavigateToJobDetail: (String) -> Unit,
    onNavigateToCreateJob: () -> Unit,
    onNavigateToReports: () -> Unit = {}
) {
    val myJobs by viewModel.myConsumerJobs.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()

    var selectedTab by remember { mutableStateOf("All") } // "All", "Active", "Past", "Drafts"

    val jobsToDisplay = if (myJobs.isNotEmpty()) myJobs else allJobs

    val filteredJobs = when (selectedTab) {
        "Active" -> jobsToDisplay.filter { it.status != "completed" && it.status != "cancelled" }
        "Past" -> jobsToDisplay.filter { it.status == "completed" || it.status == "cancelled" }
        "Drafts" -> emptyList()
        else -> jobsToDisplay
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // BLUE HEADER (Matches Screenshot 2 & 16)
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
                    Text(
                        text = "My Jobs",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // PDF Report Generator Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .clickable { onNavigateToReports() }
                            .testTag("my_jobs_export_pdf_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = "Export PDF",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = "Flash",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "${jobsToDisplay.size} total requests",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pill Filter Bar (All / Active / Past / Drafts)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabPillItem("All", selected = selectedTab == "All", onClick = { selectedTab = "All" })
                        TabPillItem("Active", selected = selectedTab == "Active", onClick = { selectedTab = "Active" })
                        TabPillItem("Past", selected = selectedTab == "Past", onClick = { selectedTab = "Past" })
                        TabPillItem("Drafts", selected = selectedTab == "Drafts", onClick = { selectedTab = "Drafts" })
                    }
                }
            }
        }

        // CONTENT LIST OR EMPTY STATE
        if (filteredJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FindInPage,
                        contentDescription = "No jobs",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No jobs yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You haven't posted any jobs yet.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateToCreateJob,
                        colors = ButtonDefaults.buttonColors(containerColor = FixItPrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("post_new_job_empty_button")
                    ) {
                        Text("Post a New Job", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredJobs) { job ->
                    MyJobCard(job = job, onClick = { onNavigateToJobDetail(job.id) })
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun TabPillItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color.White else Color.Transparent,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) FixItPrimaryBlue else Color.White
            )
        }
    }
}

@Composable
private fun MyJobCard(
    job: Job,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF162032),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("my_job_item_${job.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E3A8A)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (job.status == "open_for_bids") "BIDDING" else job.status.replace("_", " ").uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }

                if (job.biddingMode == "bounty_bargain") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF78350F).copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "Fixed Price",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCD34D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                val priceText = if (job.agreedLaborFee > 0) "${job.agreedLaborFee.toInt()} OMR"
                else if (job.bountyPrice != null) "${job.bountyPrice?.toInt()} OMR"
                else ""

                if (priceText.isNotBlank()) {
                    Text(
                        text = priceText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = job.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // PAYMENT STATUS: ESCROW PROTECTED
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF102A22),
                border = androidx.compose.foundation.BorderStroke(1.dp, FixItEmerald.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("job_payment_status_${job.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Escrow Protected",
                        tint = FixItEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Payment Status:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (job.status == "completed") "Released upon Completion" else "Escrow Protected",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (job.status == "completed") Color(0xFF38BDF8) else FixItEmerald
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    val heldAmount = if (job.agreedLaborFee > 0) job.agreedLaborFee
                    else if (job.escrowAmount > 0) job.escrowAmount
                    else if (job.budgetMax > 0) job.budgetMax
                    else (job.bountyPrice ?: 25.0)
                    Text(
                        text = "%.1f OMR Held".format(heldAmount),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom timestamp + Action link
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "recently posted",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Text(
                        text = if (job.status == "open_for_bids") "View Bids →" else "Track →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FixItPrimaryBlue
                    )
                }
            }
        }
    }
}

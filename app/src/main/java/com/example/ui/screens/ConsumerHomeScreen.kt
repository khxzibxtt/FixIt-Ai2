package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Job
import com.example.ui.components.JobDashboardMapView
import com.example.ui.theme.*
import com.example.ui.viewmodel.FixItViewModel

@Composable
fun ConsumerHomeScreen(
    viewModel: FixItViewModel,
    onNavigateToServiceSelect: () -> Unit,
    onNavigateToServiceDescribe: (String) -> Unit,
    onNavigateToJobDetail: (String) -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToCreateJob: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val myJobs by viewModel.myConsumerJobs.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    val availableVendors by viewModel.availableVendors.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var dashboardViewMode by remember { mutableStateOf("list") } // "list" or "map"

    val activeJobs = remember(myJobs, allJobs) {
        val list = if (myJobs.isNotEmpty()) myJobs else allJobs
        list.filter { it.status != "completed" && it.status != "cancelled" }
    }

    val totalEscrowFunds = remember(activeJobs) {
        activeJobs.sumOf { job ->
            if (job.agreedLaborFee > 0) job.agreedLaborFee
            else if (job.escrowAmount > 0) job.escrowAmount
            else if (job.budgetMax > 0) job.budgetMax
            else (job.bountyPrice ?: 25.0)
        }
    }

    val serviceGrid = listOf(
        ServiceGridItem("Electrician", Icons.Default.Bolt, Color(0xFFFBBF24)),
        ServiceGridItem("Plumber", Icons.Default.Handyman, Color(0xFF38BDF8)),
        ServiceGridItem("Mechanic", Icons.Default.Settings, Color(0xFFF87171)),
        ServiceGridItem("AC Repair", Icons.Default.AcUnit, Color(0xFF22D3EE)),
        ServiceGridItem("Carpenter", Icons.Default.Construction, Color(0xFFFB923C)),
        ServiceGridItem("Painter", Icons.Default.FormatPaint, Color(0xFFC084FC)),
        ServiceGridItem("Cleaning", Icons.Default.AutoAwesome, Color(0xFF2DD4BF)),
        ServiceGridItem("Taxi", Icons.Default.DirectionsCar, Color(0xFFFACC15)),
        ServiceGridItem("Phone Repair", Icons.Default.Smartphone, Color(0xFFF472B6)),
        ServiceGridItem("Pest Control", Icons.Default.PestControl, Color(0xFF4ADE80)),
        ServiceGridItem("Delivery", Icons.Default.TwoWheeler, Color(0xFF60A5FA)),
        ServiceGridItem("More...", Icons.Default.WorkOutline, Color(0xFF818CF8))
    )

    val glass = LocalGlassStyle.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(glass.backgroundBrush)
    ) {
        // TOP GLASS APP HEADER
        Surface(
            color = glass.cardBackground,
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, glass.cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Header Top Row: Logo + Location + Notification Bell
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // FixIt pin badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "FixIt Pin",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "FixIt",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Location clickable selector
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.clickable { onNavigateToAddresses() }
                    ) {
                        Text(
                            text = "LOCATION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCD34D),
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Muscat, Oman",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // PDF Audit Report button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onNavigateToReports() }
                            .testTag("pdf_reports_header_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF Audit Report",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Notification Bell icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onNavigateToNotifications() }
                            .testTag("notification_bell_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar inside Blue Header
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onNavigateToServiceSelect() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Search for a service..." else searchQuery,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // SCROLLABLE CONTENT
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // HERO BANNER: FixIt Plus is Here (Matches Screenshot 1 & 14)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = FixItPrimaryBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSubscriptions() }
                        .testTag("hero_banner_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "FixIt Plus is Here",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Get 5% cashback on every job",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFCD34D),
                                modifier = Modifier.clickable { onNavigateToSubscriptions() }
                            ) {
                                Text(
                                    text = "Upgrade Now",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ACTIVE JOBS & ESCROW PROTECTED DASHBOARD
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("active_jobs_dashboard_section"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Active Jobs Dashboard",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = FixItEmerald.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(FixItEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${activeJobs.size} ONGOING",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FixItEmerald
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FixItAmber,
                            modifier = Modifier
                                .clickable { onNavigateToCreateJob() }
                                .testTag("dashboard_post_job_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Post Job",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Escrow Protection Summary Banner
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A24)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FixItEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().testTag("dashboard_escrow_summary_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FixItEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = "Escrow Protected",
                                    tint = FixItEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Escrow Protected Funds",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FixItEmerald
                                    )
                                    Text(
                                        text = "%.1f OMR Locked".format(totalEscrowFunds),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                }
                                Text(
                                    text = "Payment Status: Funds held for ongoing tasks in safety vault. Zero payout until completion verified.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFD1FAE5),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Dashboard View Mode Selector (List View vs Map View)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF162032))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("list" to "List View", "map" to "Live Map View").forEach { (mode, label) ->
                            val isSelected = dashboardViewMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) FixItBlue else Color.Transparent)
                                    .clickable { dashboardViewMode = mode }
                                    .padding(vertical = 7.dp)
                                    .testTag("dashboard_view_toggle_$mode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (mode == "list") Icons.Default.FormatListBulleted else Icons.Default.Map,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    if (dashboardViewMode == "map") {
                        // MAP VIEW: Displays available local repair tasks using user's location
                        JobDashboardMapView(
                            jobs = allJobs.ifEmpty { activeJobs },
                            userLat = 23.5880,
                            userLon = 58.3829,
                            onNavigateToJobDetail = onNavigateToJobDetail
                        )
                    } else {
                        // LIST VIEW: Ongoing Tasks List
                        if (activeJobs.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF162032),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "No ongoing tasks right now",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Post a job to get matched with verified technicians. All payments are 100% Escrow Protected.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onNavigateToCreateJob,
                                        colors = ButtonDefaults.buttonColors(containerColor = FixItPrimaryBlue),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Post a New Job", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                activeJobs.forEach { job ->
                                    DashboardActiveJobCard(
                                        job = job,
                                        onClick = { onNavigateToJobDetail(job.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // WHAT DO YOU NEED? SECTION (Matches Screenshot 1 & 14)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "What do you need?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "See all →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FixItPrimaryBlue,
                        modifier = Modifier.clickable { onNavigateToServiceSelect() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3-column Grid for 12 services
                val chunkedGrid = serviceGrid.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    chunkedGrid.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF162032),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(100.dp)
                                        .clickable {
                                            if (item.name == "More...") {
                                                onNavigateToServiceSelect()
                                            } else {
                                                onNavigateToServiceDescribe(item.name)
                                            }
                                        }
                                        .testTag("service_item_${item.name}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(item.iconColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.name,
                                                tint = item.iconColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = item.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // QUICK SERVICES (Matches Screenshot 1)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Services",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Saved Pros 💙",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FixItPrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickServiceCard(
                        title = "Parts Store",
                        subtitle = "30m delivery",
                        icon = Icons.Default.ShoppingCart,
                        iconColor = Color(0xFFFBBF24),
                        containerColor = Color(0xFF282317),
                        modifier = Modifier.weight(1f)
                    )
                    QuickServiceCard(
                        title = "Home Care",
                        subtitle = "Regular plans",
                        icon = Icons.Default.CalendarMonth,
                        iconColor = Color(0xFF38BDF8),
                        containerColor = Color(0xFF132338),
                        modifier = Modifier.weight(1f)
                    )
                    QuickServiceCard(
                        title = "Pro Feed",
                        subtitle = "Before/After",
                        icon = Icons.Default.AutoAwesome,
                        iconColor = Color(0xFFC084FC),
                        containerColor = Color(0xFF241738),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // TRUST BADGES ROW (Matches Screenshot 1 & 14)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrustBadge(
                        title = "Secure Pay",
                        subtitle = "Escrow-\nprotected",
                        icon = Icons.Default.Shield,
                        iconColor = Color(0xFF34D399),
                        modifier = Modifier.weight(1f)
                    )
                    TrustBadge(
                        title = "Verified Pros",
                        subtitle = "ID-checked",
                        icon = Icons.Default.Star,
                        iconColor = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                    TrustBadge(
                        title = "Fast Match",
                        subtitle = "Avg 8 mins",
                        icon = Icons.Default.Schedule,
                        iconColor = Color(0xFF60A5FA),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // EARN REWARDS BANNER (Matches Screenshot 14)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF241432),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3D2156)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToWallet() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9333EA).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CardGiftcard,
                                contentDescription = "Rewards",
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Earn Rewards!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Get 2% cashback on every completed job",
                                fontSize = 11.sp,
                                color = Color(0xFFD8B4FE)
                            )
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = Color(0xFFD8B4FE),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

private data class ServiceGridItem(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color
)

@Composable
private fun QuickServiceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.25f)),
        modifier = modifier.height(84.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = iconColor)
            Text(subtitle, fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun TrustBadge(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF162032),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
        modifier = modifier.height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 8.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DashboardActiveJobCard(
    job: Job,
    onClick: () -> Unit
) {
    val heldAmount = if (job.agreedLaborFee > 0) job.agreedLaborFee
    else if (job.escrowAmount > 0) job.escrowAmount
    else if (job.budgetMax > 0) job.budgetMax
    else (job.bountyPrice ?: 25.0)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF162032),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("dashboard_job_card_${job.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category & Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E3A8A)
                ) {
                    Text(
                        text = job.serviceCategory.ifBlank { "Service" }.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                val statusLabel = when (job.status) {
                    "open_for_bids" -> "OPEN FOR BIDS"
                    "in_progress" -> "IN PROGRESS"
                    "on_the_way" -> "ON THE WAY"
                    "arrived" -> "ARRIVED"
                    "verification_pending" -> "VERIFICATION PENDING"
                    "warranty_active" -> "WARRANTY ACTIVE"
                    "disputed" -> "IN ARBITRATION"
                    else -> job.status.replace("_", " ").uppercase()
                }

                val statusColor = when (job.status) {
                    "in_progress", "on_the_way" -> FixItAmber
                    "verification_pending" -> Color(0xFFF472B6)
                    "warranty_active" -> FixItEmerald
                    else -> Color(0xFF60A5FA)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Job Title
            Text(
                text = job.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // PROMINENT PAYMENT STATUS & ESCROW PROTECTED BADGE
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF102A22),
                border = androidx.compose.foundation.BorderStroke(1.dp, FixItEmerald.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("escrow_protected_status_badge_${job.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = "Escrow Protected",
                        tint = FixItEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Payment Status:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Escrow Protected",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FixItEmerald
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "%.1f OMR Locked".format(heldAmount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle & Action footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = job.locationName,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Text(
                    text = if (job.status == "open_for_bids") "View Quotes →" else "Track Task Live →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FixItPrimaryBlue
                )
            }
        }
    }
}

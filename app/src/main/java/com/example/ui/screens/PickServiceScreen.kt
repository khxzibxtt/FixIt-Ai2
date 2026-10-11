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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FixItPrimaryBlue

@Composable
fun PickServiceScreen(
    onNavigateBack: () -> Unit,
    onSelectService: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val homeServices = listOf(
        Pair("Electrician", Icons.Default.Bolt to Color(0xFFFBBF24)),
        Pair("Plumber", Icons.Default.Handyman to Color(0xFF38BDF8)),
        Pair("Carpenter", Icons.Default.Construction to Color(0xFFFB923C)),
        Pair("AC Repair", Icons.Default.AcUnit to Color(0xFF22D3EE)),
        Pair("Painter", Icons.Default.FormatPaint to Color(0xFFC084FC)),
        Pair("Handyman", Icons.Default.Build to Color(0xFF94A3B8)),
        Pair("Locksmith", Icons.Default.Key to Color(0xFFEAB308)),
        Pair("Pest Control", Icons.Default.PestControl to Color(0xFF4ADE80)),
        Pair("Home Cleaning", Icons.Default.AutoAwesome to Color(0xFF2DD4BF)),
        Pair("Gardening", Icons.Default.Park to Color(0xFF10B981))
    )

    val vehicleServices = listOf(
        Pair("Mechanic", Icons.Default.Settings to Color(0xFFF87171)),
        Pair("Car Wash", Icons.Default.LocalCarWash to Color(0xFF38BDF8)),
        Pair("Tow Truck", Icons.Default.LocalShipping to Color(0xFFFB923C)),
        Pair("Tire Change", Icons.Default.TireRepair to Color(0xFF94A3B8))
    )

    val techServices = listOf(
        Pair("Phone Repair", Icons.Default.Smartphone to Color(0xFFF472B6)),
        Pair("Laptop / PC", Icons.Default.Laptop to Color(0xFF38BDF8)),
        Pair("TV / Electronics", Icons.Default.Tv to Color(0xFFA855F7)),
        Pair("CCTV Install", Icons.Default.Videocam to Color(0xFF2DD4BF)),
        Pair("Network / WiFi", Icons.Default.Wifi to Color(0xFF4ADE80))
    )

    val professionalServices = listOf(
        Pair("Moving Help", Icons.Default.Inventory2 to Color(0xFFFB923C)),
        Pair("Delivery", Icons.Default.TwoWheeler to Color(0xFFF59E0B)),
        Pair("Tutor / Lessons", Icons.Default.School to Color(0xFF3B82F6)),
        Pair("Physiotherapy", Icons.Default.Favorite to Color(0xFFF43F5E)),
        Pair("Barber / Salon", Icons.Default.ContentCut to Color(0xFF06B6D4))
    )

    val otherServices = listOf(
        Pair("Tailoring", Icons.Default.DryCleaning to Color(0xFF818CF8)),
        Pair("Photography", Icons.Default.PhotoCamera to Color(0xFFC084FC)),
        Pair("Event Planning", Icons.Default.Celebration to Color(0xFFF472B6)),
        Pair("Home Cook", Icons.Default.Restaurant to Color(0xFFF87171)),
        Pair("Other Service", Icons.Default.WorkOutline to Color(0xFF38BDF8))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // BLUE HEADER (Matches Screenshot 4 & 13)
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

                    Column {
                        Text(
                            text = "What do you need?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Pick a service",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Segment Step Progress Line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                }
            }
        }

        // Search Field
        Box(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF162032),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
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
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search for a service...", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("service_search_input")
                    )
                }
            }
        }

        // Category Groups
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ServiceSectionGrid(
                    sectionTitle = "HOME",
                    services = homeServices.filter { it.first.contains(searchQuery, ignoreCase = true) },
                    onSelect = onSelectService
                )
            }

            item {
                ServiceSectionGrid(
                    sectionTitle = "VEHICLES",
                    services = vehicleServices.filter { it.first.contains(searchQuery, ignoreCase = true) },
                    onSelect = onSelectService
                )
            }

            item {
                ServiceSectionGrid(
                    sectionTitle = "TECH",
                    services = techServices.filter { it.first.contains(searchQuery, ignoreCase = true) },
                    onSelect = onSelectService
                )
            }

            item {
                ServiceSectionGrid(
                    sectionTitle = "PROFESSIONAL",
                    services = professionalServices.filter { it.first.contains(searchQuery, ignoreCase = true) },
                    onSelect = onSelectService
                )
            }

            item {
                ServiceSectionGrid(
                    sectionTitle = "OTHER",
                    services = otherServices.filter { it.first.contains(searchQuery, ignoreCase = true) },
                    onSelect = onSelectService
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun ServiceSectionGrid(
    sectionTitle: String,
    services: List<Pair<String, Pair<androidx.compose.ui.graphics.vector.ImageVector, Color>>>,
    onSelect: (String) -> Unit
) {
    if (services.isEmpty()) return

    Column {
        Text(
            text = sectionTitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val chunks = services.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            chunks.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { (name, iconPair) ->
                        val (icon, color) = iconPair
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF162032),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                            modifier = Modifier
                                .weight(1f)
                                .height(96.dp)
                                .clickable { onSelect(name) }
                                .testTag("pick_service_$name")
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = name,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Fill row if fewer than 3 items
                    if (rowItems.size < 3) {
                        for (i in 0 until (3 - rowItems.size)) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

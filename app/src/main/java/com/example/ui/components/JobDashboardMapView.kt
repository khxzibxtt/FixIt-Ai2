package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Job
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItBlue
import com.example.ui.theme.FixItEmerald
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun JobDashboardMapView(
    jobs: List<Job>,
    userLat: Double = 23.5880,
    userLon: Double = 58.3829,
    onNavigateToJobDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedJob by remember { mutableStateOf<Job?>(jobs.firstOrNull()) }

    // Pulsing animation for user's GPS position
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    // Filter jobs
    val filteredJobs = remember(jobs, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") {
            jobs
        } else {
            jobs.filter { it.serviceCategory.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    // Helper to calculate distance in km using Haversine formula
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("job_dashboard_map_view")
    ) {
        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val categories = listOf("All", "Plumbing", "HVAC", "Electrical", "Carpentry")
            categories.forEach { cat ->
                val isSelected = selectedCategoryFilter == cat
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) FixItBlue else Color(0xFF1E293B).copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) FixItBlue else Color(0xFF334155)
                    ),
                    modifier = Modifier.clickable { selectedCategoryFilter = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Interactive Map Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0A111F))
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                    ),
                    RoundedCornerShape(24.dp)
                )
        ) {
            // Stylized vector map canvas with roads, grid, coastlines, and pins
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(filteredJobs) {
                        detectTapGestures { tapOffset ->
                            // Hit test for job markers
                            val w = size.width
                            val h = size.height
                            var closest: Job? = null
                            var minDistance = Float.MAX_VALUE

                            filteredJobs.forEachIndexed { idx, job ->
                                val jobLat = job.latitude
                                val jobLon = job.longitude
                                // Project coordinates relative to user position
                                val offsetX = ((jobLon - userLon) * 2500f).toFloat()
                                val offsetY = (-(jobLat - userLat) * 2500f).toFloat()

                                val pinX = (w / 2f) + offsetX
                                val pinY = (h / 2f) + offsetY

                                val dist = sqrt((tapOffset.x - pinX) * (tapOffset.x - pinX) + (tapOffset.y - pinY) * (tapOffset.y - pinY))
                                if (dist < 80f && dist < minDistance) {
                                    minDistance = dist
                                    closest = job
                                }
                            }

                            if (closest != null) {
                                selectedJob = closest
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f
                val centerY = h / 2f

                // 1. Grid background
                val gridSpacing = 36f
                for (x in 0..(w / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0xFF16233B),
                        start = Offset(x * gridSpacing, 0f),
                        end = Offset(x * gridSpacing, h),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(h / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0xFF16233B),
                        start = Offset(0f, y * gridSpacing),
                        end = Offset(w, y * gridSpacing),
                        strokeWidth = 1f
                    )
                }

                // 2. Decorative road routes (Sultan Qaboos Hwy & Muscat Expressway)
                val roadPath1 = Path().apply {
                    moveTo(0f, h * 0.75f)
                    cubicTo(w * 0.3f, h * 0.65f, w * 0.6f, h * 0.45f, w, h * 0.35f)
                }
                drawPath(roadPath1, Color(0xFF1E3A5F), style = Stroke(width = 6f))
                drawPath(roadPath1, Color(0xFF38BDF8).copy(alpha = 0.4f), style = Stroke(width = 2f))

                val roadPath2 = Path().apply {
                    moveTo(w * 0.1f, h)
                    cubicTo(w * 0.35f, h * 0.5f, w * 0.55f, h * 0.55f, w * 0.9f, 0f)
                }
                drawPath(roadPath2, Color(0xFF1E3A5F), style = Stroke(width = 4f))

                // 3. User Current Location Radar Pulse
                drawCircle(
                    color = Color(0xFF0284C7).copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color(0xFF0284C7),
                    radius = 9f,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(centerX, centerY)
                )

                // 4. Job Markers
                filteredJobs.forEachIndexed { idx, job ->
                    val jobLat = job.latitude
                    val jobLon = job.longitude

                    val offsetX = ((jobLon - userLon) * 2500f).toFloat()
                    val offsetY = (-(jobLat - userLat) * 2500f).toFloat()

                    val pinX = (centerX + offsetX).coerceIn(40f, w - 40f)
                    val pinY = (centerY + offsetY).coerceIn(40f, h - 40f)

                    val isSelected = selectedJob?.id == job.id
                    val pinColor = when (job.serviceCategory.lowercase()) {
                        "plumbing" -> Color(0xFF0EA5E9)
                        "hvac" -> FixItAmber
                        "electrical" -> Color(0xFFF59E0B)
                        "carpentry" -> Color(0xFFEAB308)
                        else -> FixItEmerald
                    }

                    // Connecting dashed line to user location if selected
                    if (isSelected) {
                        drawLine(
                            color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                            start = Offset(centerX, centerY),
                            end = Offset(pinX, pinY),
                            strokeWidth = 2f
                        )
                    }

                    // Outer halo
                    drawCircle(
                        color = pinColor.copy(alpha = if (isSelected) 0.35f else 0.15f),
                        radius = if (isSelected) 24f else 18f,
                        center = Offset(pinX, pinY)
                    )

                    // Pin circle
                    drawCircle(
                        color = if (isSelected) Color.White else pinColor,
                        radius = if (isSelected) 13f else 10f,
                        center = Offset(pinX, pinY)
                    )

                    drawCircle(
                        color = pinColor,
                        radius = if (isSelected) 9f else 7f,
                        center = Offset(pinX, pinY)
                    )
                }
            }

            // Top Status Overlay: User location banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Muscat GPS Live (${filteredJobs.size} nearby tasks)",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Recenter FAB
            FloatingActionButton(
                onClick = {
                    selectedJob = filteredJobs.firstOrNull()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(38.dp),
                containerColor = Color(0xFF1E293B).copy(alpha = 0.9f),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center on me", modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Job Glassmorphic Preview Card
        AnimatedVisibility(visible = selectedJob != null) {
            selectedJob?.let { job ->
                val distance = calculateDistanceKm(userLat, userLon, job.latitude, job.longitude)
                val heldAmount = if (job.agreedLaborFee > 0) job.agreedLaborFee
                else if (job.escrowAmount > 0) job.escrowAmount
                else if (job.budgetMax > 0) job.budgetMax
                else 25.0

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToJobDetail(job.id) }
                        .testTag("map_job_card_${job.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FixItBlue.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = FixItBlue, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "%.1f km away • ${job.locationName}".format(distance),
                                        color = Color(0xFF93C5FD),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Escrow Protected Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = FixItEmerald.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FixItEmerald.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = FixItEmerald, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Escrow Protected",
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = job.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )

                        Text(
                            text = job.description,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            maxLines = 2,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Escrow Vault Held:",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "%.1f OMR Locked".format(heldAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FixItAmber
                                )
                            }

                            Button(
                                onClick = { onNavigateToJobDetail(job.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FixItBlue),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("View Details & Contact", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

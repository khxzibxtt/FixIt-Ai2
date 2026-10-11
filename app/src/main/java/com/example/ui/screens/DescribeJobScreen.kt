package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItPrimaryBlue
import com.example.ui.viewmodel.FixItViewModel

@Composable
fun DescribeJobScreen(
    category: String,
    viewModel: FixItViewModel,
    onNavigateBack: () -> Unit,
    onJobCreated: (String) -> Unit
) {
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiRewrittenText by viewModel.aiRewriteResult.collectAsState()

    var urgency by remember { mutableStateOf("This Week") } // "Emergency", "This Week", "Flexible"
    var descriptionText by remember { mutableStateOf("") }
    var requireWarranty by remember { mutableStateOf(true) }
    var setFixedPrice by remember { mutableStateOf(false) }
    var fixedPriceText by remember { mutableStateOf("25.0") }

    var mediaImages by remember {
        mutableStateOf(
            listOf(
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=300&q=80",
                "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=300&q=80",
                "https://images.unsplash.com/photo-1585704032915-c3400ca199e7?auto=format&fit=crop&w=300&q=80"
            )
        )
    }

    LaunchedEffect(aiRewrittenText) {
        if (!aiRewrittenText.isNullOrBlank()) {
            descriptionText = aiRewrittenText ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // BLUE HEADER (Matches Screenshot 5 & 12)
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

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Describe your job",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Text(
                        text = "Save Draft",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.clickable {
                            viewModel.setNotice("Job draft saved locally.")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Segment Progress Bar (2 of 3 completed)
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
                            .background(Color.White)
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

        // SCROLLABLE FORM
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URGENCY SECTION (Matches Screenshot 5 & 12)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF162032),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "URGENCY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Emergency
                        UrgencyPill(
                            title = "Emergency",
                            subtitle = "Right now",
                            icon = Icons.Default.NotificationsActive,
                            selected = urgency == "Emergency",
                            activeColor = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f),
                            onClick = { urgency = "Emergency" }
                        )

                        // This Week
                        UrgencyPill(
                            title = "This Week",
                            subtitle = "Within days",
                            icon = Icons.Default.Bolt,
                            selected = urgency == "This Week",
                            activeColor = FixItAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { urgency = "This Week" }
                        )

                        // Flexible
                        UrgencyPill(
                            title = "Flexible",
                            subtitle = "Anytime",
                            icon = Icons.Default.CalendarMonth,
                            selected = urgency == "Flexible",
                            activeColor = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f),
                            onClick = { urgency = "Flexible" }
                        )
                    }
                }
            }

            // DESCRIBE THE JOB CARD (Matches Screenshot 5)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF162032),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DESCRIBE THE JOB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )

                        // AI Improve Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E3A8A).copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6)),
                            modifier = Modifier
                                .clickable {
                                    viewModel.triggerAiRewrite(descriptionText, category)
                                }
                                .testTag("ai_improve_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isAiLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        color = Color(0xFF60A5FA),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Improving...", fontSize = 11.sp, color = Color(0xFF60A5FA))
                                } else {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AI Improve",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF60A5FA)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        placeholder = {
                            Text(
                                "Describe what you need from the $category...",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF22314A),
                            unfocusedBorderColor = Color(0xFF22314A),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("job_description_textarea"),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // MEDIA PROOF CARD (Matches Screenshot 12)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF162032),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Media Proof",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Add up to 5 images and 1 video to help pros bid accurately.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Images section (left half)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("IMAGES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text("${mediaImages.size} / 5", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Grid of images
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                mediaImages.take(2).forEachIndexed { idx, url ->
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0F172A))
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Photo",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(16.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                .clickable {
                                                    mediaImages = mediaImages.filterIndexed { i, _ -> i != idx }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                }

                                // Add button
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0F172A))
                                        .clickable {
                                            if (mediaImages.size < 5) {
                                                mediaImages = mediaImages + "https://images.unsplash.com/photo-1504148455328-c376907d081c?auto=format&fit=crop&w=300&q=80"
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add", tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // Video section (right half)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("VIDEO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text("1 / 1", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Video", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            // REQUIRE WARRANTY SWITCH (Matches Screenshot 12)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF162032),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Require Warranty",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ask vendors to provide a warranty for this job",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Switch(
                        checked = requireWarranty,
                        onCheckedChange = { requireWarranty = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FixItPrimaryBlue
                        )
                    )
                }
            }

            // SET FIXED PRICE SWITCH (Matches Screenshot 5 & 12)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF162032),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Set Fixed Price",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Name your budget - vendors compete at or below it",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Switch(
                            checked = setFixedPrice,
                            onCheckedChange = { setFixedPrice = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FixItPrimaryBlue
                            )
                        )
                    }

                    AnimatedVisibility(visible = setFixedPrice) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            OutlinedTextField(
                                value = fixedPriceText,
                                onValueChange = { fixedPriceText = it },
                                label = { Text("Your Budget (OMR)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // REVIEW JOB BUTTON (Matches Screenshot 5)
            Button(
                onClick = {
                    val finalDesc = descriptionText.ifBlank { "Diagnostic and repair needed for $category." }
                    val fixedPrice = if (setFixedPrice) fixedPriceText.toDoubleOrNull() ?: 25.0 else null
                    viewModel.postNewJob(
                        title = "$category Fix",
                        description = finalDesc,
                        category = category,
                        biddingMode = if (setFixedPrice) "bounty_bargain" else "blind_bidding",
                        bountyPrice = fixedPrice,
                        partsMode = "multi_receipt",
                        location = "Muscat, Oman",
                        onSuccess = { newId ->
                            onJobCreated(newId)
                        }
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FixItPrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("review_job_button")
            ) {
                Text(
                    text = "Review Job →",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun UrgencyPill(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, activeColor) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22314A)),
        modifier = modifier
            .height(86.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) activeColor else Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) activeColor else Color.White
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

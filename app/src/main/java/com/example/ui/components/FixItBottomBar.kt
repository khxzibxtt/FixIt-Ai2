package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FixItAmber
import com.example.ui.theme.FixItCyan
import com.example.ui.theme.FixItViolet
import com.example.ui.theme.LocalGlassStyle

@Composable
fun FixItBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onCenterPlusClicked: () -> Unit
) {
    val glass = LocalGlassStyle.current

    Surface(
        color = glass.cardBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, glass.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Home
            BottomNavItem(
                icon = if (currentTab == "home") Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                selected = currentTab == "home",
                onClick = { onTabSelected("home") },
                tag = "bottom_nav_home"
            )

            // Jobs
            BottomNavItem(
                icon = if (currentTab == "jobs") Icons.Filled.Work else Icons.Outlined.WorkOutline,
                label = "Jobs",
                selected = currentTab == "jobs",
                onClick = { onTabSelected("jobs") },
                tag = "bottom_nav_jobs"
            )

            // Center Floating Plus Button with Aurora Glass Glow
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(glass.accentPrimary, glass.accentSecondary)
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    .clickable { onCenterPlusClicked() }
                    .testTag("bottom_nav_plus"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Post New Job",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Chats
            BottomNavItem(
                icon = if (currentTab == "chats") Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                label = "Chats",
                selected = currentTab == "chats",
                onClick = { onTabSelected("chats") },
                tag = "bottom_nav_chats"
            )

            // Profile
            BottomNavItem(
                icon = if (currentTab == "profile") Icons.Filled.Person else Icons.Outlined.PersonOutline,
                label = "Profile",
                selected = currentTab == "profile",
                onClick = { onTabSelected("profile") },
                tag = "bottom_nav_profile"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val glass = LocalGlassStyle.current
    val animatedTint by animateColorAsState(
        targetValue = if (selected) glass.accentPrimary else glass.textSecondary,
        animationSpec = tween(200),
        label = "nav_item_tint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) glass.accentPrimary.copy(alpha = 0.16f) else Color.Transparent)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = animatedTint
        )
    }
}

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FixItViewModel
import com.example.util.NotificationHelper

@Composable
fun ProfileScreen(
    viewModel: FixItViewModel,
    onNavigateToAddresses: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToSupportChat: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val myJobs by viewModel.myConsumerJobs.collectAsState()
    val currentTheme by viewModel.appThemeMode.collectAsState()
    val glass = LocalGlassStyle.current

    val initials = remember(userProfile?.displayName) {
        val name = userProfile?.displayName?.trim() ?: "Client"
        val parts = name.split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        } else if (parts.isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else {
            "ME"
        }
    }

    var showAccountDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var editPhoneInput by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }
    var editOtpInput by remember { mutableStateOf("") }
    var editOtpSent by remember { mutableStateOf(false) }
    var editDialogNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(glass.backgroundBrush)
    ) {
        // GLASSMORPHIC HEADER
        Surface(
            color = glass.cardBackground,
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, glass.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                // Top row: Location pin + FixIt + Settings gear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(glass.surfaceHighlight)
                            .border(1.dp, glass.cardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = glass.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "FixIt Marketplace",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Muscat, Oman • Escrow Active",
                            fontSize = 11.sp,
                            color = glass.accentSecondary
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(glass.surfaceHighlight)
                            .border(1.dp, glass.cardBorder, CircleShape)
                            .clickable { showSettingsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = glass.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Profile card row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Glassmorphic Monogram Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(glass.accentPrimary, glass.accentSecondary)
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                            .shadow(8.dp, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile?.displayName ?: "Ahmed Al-Balushi",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = glass.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = FixItCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = userProfile?.phoneNumber?.ifBlank { userProfile?.phoneMasked ?: "+968 9595 6361" } ?: "+968 9595 6361",
                            fontSize = 13.sp,
                            color = glass.textSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = glass.accentPrimary.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, glass.accentPrimary.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (userProfile?.role == "vendor") "Certified Pro" else "Consumer Client",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = glass.accentPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showAccountDialog = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = glass.textSecondary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Edit Profile", fontSize = 11.sp, color = glass.textSecondary)
                            }
                        }
                    }
                }
            }
        }

        // SCROLLABLE BODY
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // LIVE THEME SELECTOR CARD
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = glass.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, glass.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(glass.accentPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = glass.accentPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Glass Appearance & Theme",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = "Choose your preferred ambient glass aesthetic",
                                    fontSize = 11.sp,
                                    color = glass.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppThemeMode.values().forEach { mode ->
                                val isSelected = currentTheme == mode
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) glass.accentPrimary.copy(alpha = 0.25f) else glass.surfaceHighlight,
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(1.2.dp, glass.accentPrimary)
                                    } else {
                                        androidx.compose.foundation.BorderStroke(1.2.dp, glass.cardBorder)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setThemeMode(mode) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = when (mode) {
                                                AppThemeMode.AURORA -> "Aurora"
                                                AppThemeMode.LIGHT -> "Pearl Light"
                                                AppThemeMode.DARK -> "Obsidian"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) glass.accentPrimary else glass.textPrimary
                                        )
                                        Text(
                                            text = if (isSelected) "Active" else "Select",
                                            fontSize = 9.sp,
                                            color = if (isSelected) glass.accentPrimary else glass.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3 STATS CARDS ROW (Wallet / Jobs / Rewards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCardItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        value = "${String.format("%.3f", userProfile?.walletBalance ?: 0.0)} OMR",
                        label = "My Wallet",
                        accent = FixItCyan,
                        glass = glass,
                        modifier = Modifier.weight(1f).clickable { onNavigateToWallet() }
                    )
                    StatCardItem(
                        icon = Icons.Default.TrendingUp,
                        value = "${myJobs.count { it.status == "completed" }}/${myJobs.size}",
                        label = "Completed",
                        accent = FixItEmerald,
                        glass = glass,
                        modifier = Modifier.weight(1f)
                    )
                    StatCardItem(
                        icon = Icons.Default.Security,
                        value = "100%",
                        label = "Escrow Shield",
                        accent = FixItAmber,
                        glass = glass,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // FixIt PLUS MEMBERSHIP BANNER
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = glass.cardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, FixItAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FixItAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = FixItAmber)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text("FixIt Plus Escrow Membership", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
                                Text("Active protection • zero service commissions", fontSize = 11.sp, color = glass.textSecondary)
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = FixItAmber,
                                modifier = Modifier.clickable { onNavigateToSubscriptions() }
                            ) {
                                Text("Details", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { 0.85f },
                            color = FixItAmber,
                            trackColor = glass.surfaceHighlight,
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            // ACCOUNT SECTION
            item {
                SectionGroup(title = "ACCOUNT & CREDENTIALS", glass = glass) {
                    ProfileMenuRow(
                        Icons.Default.PersonOutline,
                        "My Identity",
                        "Profile details and role",
                        glass = glass
                    ) {
                        showAccountDialog = true
                    }
                    HorizontalDivider(color = glass.cardBorder.let { Color.White.copy(alpha = 0.08f) })
                    val hasPhone = !userProfile?.phoneNumber.isNullOrBlank()
                    ProfileMenuRow(
                        Icons.Default.PhoneIphone,
                        "Verified Mobile Phone",
                        if (hasPhone) (userProfile?.phoneNumber ?: "") else "Tap to link phone with OTP",
                        badge = if (hasPhone) "Verified" else "Link OTP",
                        glass = glass
                    ) {
                        editPhoneInput = userProfile?.phoneNumber ?: ""
                        showAccountDialog = true
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ProfileMenuRow(
                        Icons.Default.MailOutline,
                        "Linked Email Address",
                        userProfile?.email?.ifBlank { "Tap to link manual email" } ?: "Tap to link manual email",
                        badge = if (!userProfile?.email.isNullOrBlank()) "Linked" else "Add Email",
                        glass = glass
                    ) {
                        showAccountDialog = true
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ProfileMenuRow(
                        Icons.Default.AccountCircle,
                        "Google Identity Sign-In",
                        "One-tap Credential Manager profile",
                        badge = if (userProfile?.isGoogleLinked == true || !userProfile?.photoUrl.isNullOrBlank()) "Connected" else "Sync",
                        glass = glass
                    ) {
                        viewModel.setNotice("Google Identity is connected.")
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ProfileMenuRow(
                        Icons.Default.Place,
                        "Saved Addresses",
                        "Default location & GPS presets",
                        glass = glass
                    ) {
                        onNavigateToAddresses()
                    }
                }
            }

            // PERKS & TOOLS SECTION
            item {
                SectionGroup(title = "PERKS & TOOLS", glass = glass) {
                    ProfileMenuRow(
                        Icons.Default.AccountBalanceWallet,
                        "My Wallet & Escrow Vault",
                        "Balance top-ups, escrow held funds",
                        glass = glass
                    ) {
                        onNavigateToWallet()
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ProfileMenuRow(
                        Icons.Default.PictureAsPdf,
                        "Export Audit Reports (PDF)",
                        "Issue logs, resolution workflows & milestones",
                        badge = "PDF",
                        glass = glass
                    ) {
                        onNavigateToReports()
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ProfileMenuRow(
                        Icons.Default.Tune,
                        "Settings & Preferences",
                        "Theme customization, notifications, language",
                        glass = glass
                    ) {
                        showSettingsDialog = true
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    ProfileMenuRow(
                        Icons.Default.HeadsetMic,
                        "FixIt Live Support",
                        "Direct dispute and arbitration concierge",
                        glass = glass
                    ) {
                        onNavigateToSupportChat()
                    }
                }
            }

            // LOG OUT BUTTON
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = FixItError.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, FixItError.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { viewModel.signOut() }
                        .testTag("logout_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Log out", tint = FixItError, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FixItError)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Account Details Dialog
        if (showAccountDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAccountDialog = false
                    editOtpSent = false
                    editOtpInput = ""
                    editDialogNotice = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = glass.accentPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("My FixIt Account", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = glass.textPrimary)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = glass.surfaceHighlight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Name: ${userProfile?.displayName ?: "Ahmed Al-Balushi"}", fontWeight = FontWeight.SemiBold, color = glass.textPrimary, fontSize = 13.sp)
                                Text("Email: ${userProfile?.email ?: "ahmed@fixit.om"}", color = glass.textSecondary, fontSize = 12.sp)
                                Text("Role: ${if (userProfile?.role == "vendor") "Certified Laborer" else "Client Consumer"}", color = FixItEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Account ID: ${userProfile?.userId ?: "N/A"}", color = glass.textSecondary, fontSize = 11.sp)
                            }
                        }

                        Text("Mobile Phone Number:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = glass.textPrimary)

                        OutlinedTextField(
                            value = editPhoneInput,
                            onValueChange = { editPhoneInput = it },
                            placeholder = { Text("+968 9595 6361") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = glass.accentPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!editOtpSent) {
                            Button(
                                onClick = {
                                    if (editPhoneInput.isNotBlank()) {
                                        val code = viewModel.sendOtp(editPhoneInput, "Profile Verification")
                                        editOtpSent = true
                                        editDialogNotice = "OTP code $code sent to device notification bar."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = glass.accentPrimary),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Send Verification OTP", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Enter 6-Digit OTP received:", fontSize = 12.sp, color = FixItEmerald, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = editOtpInput,
                                onValueChange = { if (it.length <= 6) editOtpInput = it },
                                placeholder = { Text("123456") },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = FixItEmerald) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    viewModel.savePhoneNumberForCurrentUser(editPhoneInput, editOtpInput) { success, msg ->
                                        if (success) {
                                            showAccountDialog = false
                                            editOtpSent = false
                                            editOtpInput = ""
                                        } else {
                                            editDialogNotice = msg
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = FixItEmerald),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Verify & Save Phone", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (editDialogNotice != null) {
                            Text(editDialogNotice ?: "", fontSize = 11.sp, color = FixItAmber)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showAccountDialog = false
                        editOtpSent = false
                        editOtpInput = ""
                        editDialogNotice = null
                    }) {
                        Text("Close", color = glass.textPrimary)
                    }
                },
                shape = RoundedCornerShape(26.dp),
                containerColor = glass.cardBackground
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = glass.accentPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Settings & Appearance", fontWeight = FontWeight.Bold, color = glass.textPrimary)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Theme Selection:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = glass.textPrimary
                        )

                        AppThemeMode.values().forEach { mode ->
                            val isSel = currentTheme == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSel) glass.accentPrimary.copy(alpha = 0.2f) else glass.surfaceHighlight)
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSel, onClick = { viewModel.setThemeMode(mode) })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(mode.title, fontWeight = FontWeight.SemiBold, color = glass.textPrimary, fontSize = 13.sp)
                                    Text(mode.subtitle, fontSize = 11.sp, color = glass.textSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "System: Edge-to-Edge Enabled • Cloud Firestore v2",
                            fontSize = 11.sp,
                            color = glass.textSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showSettingsDialog = false },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = glass.accentPrimary)
                    ) {
                        Text("Done")
                    }
                },
                shape = RoundedCornerShape(26.dp),
                containerColor = glass.cardBackground
            )
        }
    }
}

@Composable
private fun StatCardItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    accent: Color,
    glass: GlassStyle,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = glass.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, glass.cardBorder),
        modifier = modifier.height(90.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = glass.textPrimary)
            Text(label, fontSize = 10.sp, color = glass.textSecondary)
        }
    }
}

@Composable
private fun SectionGroup(
    title: String,
    glass: GlassStyle,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = glass.textSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = glass.cardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, glass.cardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), content = content)
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    glass: GlassStyle,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(glass.surfaceHighlight),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = glass.accentPrimary, modifier = Modifier.size(19.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = glass.textPrimary)
            Text(subtitle, fontSize = 11.sp, color = glass.textSecondary)
        }

        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = glass.accentPrimary.copy(alpha = 0.2f),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = glass.accentPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = glass.textSecondary, modifier = Modifier.size(18.dp))
    }
}

package com.example.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.FixItViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(viewModel: FixItViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentTheme by viewModel.appThemeMode.collectAsState()
    val glass = LocalGlassStyle.current

    // Auth Modes: 0 = Email & Pass, 1 = Phone OTP, 2 = Google One-Tap
    var authTab by remember { mutableStateOf(0) }
    // Sub-modes: "signin", "register", "forgot"
    var subMode by remember { mutableStateOf("signin") }

    // Inputs
    var emailInput by remember { mutableStateOf("client@fixit.om") }
    var phoneNumber by remember { mutableStateOf("+968 9595 6361") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("123456") }
    var newPassword by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var otpSent by remember { mutableStateOf(false) }
    var lastSentOtp by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Linking modal state
    var showLinkDialog by remember { mutableStateOf(false) }
    var linkTargetType by remember { mutableStateOf("phone") } // "phone" or "email"
    var linkInput by remember { mutableStateOf("") }
    var linkOtpInput by remember { mutableStateOf("") }
    var linkOtpSent by remember { mutableStateOf(false) }

    fun launchGoogleSignIn() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val serverClientId = try {
                    context.getString(R.string.default_web_client_id)
                } catch (e: Exception) {
                    "655198160623-n77fcuo06p8c4bqgrrlf4rrgk7c75g82.apps.googleusercontent.com"
                }

                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val googleIdToken = result.credential.data.getString("androidx.credentials.BUNDLE_KEY_ID_TOKEN")
                if (googleIdToken != null) {
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential)
                        .addOnSuccessListener {
                            isLoading = false
                            successMessage = "Google Sign-In successful!"
                        }
                        .addOnFailureListener { ex ->
                            isLoading = false
                            errorMessage = ex.localizedMessage ?: "Firebase authentication failed"
                        }
                } else {
                    isLoading = false
                    errorMessage = "ID token not received from Google Credential Manager."
                }
            } catch (e: GetCredentialCancellationException) {
                isLoading = false
                Log.w("AuthScreen", "Google Sign-in cancelled by user")
            } catch (e: GetCredentialException) {
                isLoading = false
                Log.e("AuthScreen", "CredentialManager exception", e)
                errorMessage = "Google Sign-in error: ${e.message}"
            } catch (e: Exception) {
                isLoading = false
                Log.e("AuthScreen", "Sign-in error", e)
                errorMessage = e.message
            }
        }
    }

    // Atmospheric Frosted Mesh Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(glass.backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top Bar: Live Theme Switcher Pill (Answers "whynis everything black and blue")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(glass.surfaceHighlight)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme",
                        tint = glass.accentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Glass Theme",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = glass.textPrimary
                    )
                }

                // 3-way Theme Pills
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(glass.cardBackground)
                        .border(1.dp, glass.cardBorder, RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AppThemeMode.values().forEach { mode ->
                        val isSelected = currentTheme == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) glass.accentPrimary.copy(alpha = 0.85f)
                                    else Color.Transparent
                                )
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = when (mode) {
                                    AppThemeMode.AURORA -> "Aurora"
                                    AppThemeMode.LIGHT -> "Pearl Light"
                                    AppThemeMode.DARK -> "Obsidian"
                                },
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else glass.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Glassmorphic Emblem with Ambient Iridescent Ring
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(glass.cardBackground)
                    .border(
                        2.dp,
                        Brush.linearGradient(
                            listOf(glass.accentPrimary, glass.accentSecondary, Color.White.copy(alpha = 0.6f))
                        ),
                        CircleShape
                    )
                    .shadow(16.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = "FixIt Marketplace",
                    tint = glass.accentPrimary,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "FixIt",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    letterSpacing = 1.sp
                ),
                color = glass.textPrimary
            )

            Text(
                text = "Decentralized Labor Marketplace",
                style = MaterialTheme.typography.bodyMedium,
                color = glass.accentSecondary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Escrow Security • Real-Time Database • Cross-Platform Credentials",
                style = MaterialTheme.typography.bodySmall,
                color = glass.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Auto-Fill Chips for Instant Seamless Verification
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(glass.surfaceHighlight)
                    .border(1.dp, glass.cardBorder, RoundedCornerShape(20.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = FixItAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Instant 1-Tap Login Profiles",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = glass.textPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {
                            authTab = 1
                            phoneNumber = "+968 9595 6361"
                            password = "password123"
                            subMode = "signin"
                        },
                        label = { Text("Ahmed (+968 9595 6361)", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(12.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = {
                            authTab = 0
                            emailInput = "client@fixit.om"
                            password = "password123"
                            subMode = "signin"
                        },
                        label = { Text("Fatima (Email)", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, null, modifier = Modifier.size(12.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glassmorphic Tab Selector: Email / Phone / Google
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = glass.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, glass.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf(
                        Triple(0, Icons.Default.Email, "Email & Pass"),
                        Triple(1, Icons.Default.PhoneIphone, "Mobile OTP"),
                        Triple(2, Icons.Default.AccountCircle, "Google")
                    )

                    tabs.forEach { (index, icon, label) ->
                        val isSelected = authTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    brush = if (isSelected) {
                                        Brush.linearGradient(
                                            listOf(glass.accentPrimary, glass.accentSecondary)
                                        )
                                    } else {
                                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                    }
                                )
                                .clickable {
                                    authTab = index
                                    errorMessage = null
                                    successMessage = null
                                    otpSent = false
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else glass.textSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else glass.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status alerts
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = FixItError.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, FixItError.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = FixItError, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = if (glass.isDark) Color(0xFFFCA5A5) else FixItError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (successMessage != null) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = FixItEmerald.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, FixItEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = FixItEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = successMessage ?: "",
                            color = if (glass.isDark) Color(0xFFA7F3D0) else Color(0xFF047857),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // MAIN ULTRA-ROUNDED GLASS AUTH CARD
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = glass.cardBackground,
                border = androidx.compose.foundation.BorderStroke(1.2.dp, glass.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, RoundedCornerShape(30.dp))
                    .testTag("auth_glass_card")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {

                    // Sub-mode segmented pills (Sign In / Register / Recover)
                    if (authTab != 2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(glass.surfaceHighlight)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val modes = listOf(
                                "signin" to "Sign In",
                                "register" to "Create Account",
                                "forgot" to "Recover"
                            )
                            modes.forEach { (mode, label) ->
                                val isModeActive = subMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isModeActive) glass.accentPrimary.copy(alpha = 0.9f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            subMode = mode
                                            errorMessage = null
                                            successMessage = null
                                            otpSent = false
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isModeActive) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isModeActive) Color.White else glass.textSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // --- TAB 0: EMAIL & PASSWORD ---
                    if (authTab == 0) {
                        if (subMode == "register") {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g. Fatima Al-Harthi") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = glass.accentPrimary) },
                                modifier = Modifier.fillMaxWidth().testTag("email_name_input"),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("name@example.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = glass.accentPrimary) },
                            modifier = Modifier.fillMaxWidth().testTag("email_input_field"),
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (subMode != "forgot") {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = glass.accentPrimary) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("email_password_input"),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Button
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                when (subMode) {
                                    "signin" -> {
                                        viewModel.signInWithEmailAndPassword(emailInput, password) { success, msg ->
                                            isLoading = false
                                            if (success) successMessage = msg else errorMessage = msg
                                        }
                                    }
                                    "register" -> {
                                        viewModel.registerWithEmailAndPassword(emailInput, password, displayName) { success, msg ->
                                            isLoading = false
                                            if (success) successMessage = msg else errorMessage = msg
                                        }
                                    }
                                    "forgot" -> {
                                        viewModel.sendPasswordResetEmail(emailInput) { success, msg ->
                                            isLoading = false
                                            if (success) successMessage = msg else errorMessage = msg
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("email_action_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = glass.accentPrimary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = when (subMode) {
                                        "signin" -> "Sign In with Email"
                                        "register" -> "Create Free Account"
                                        else -> "Send Password Reset Link"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // --- TAB 1: MOBILE & OTP ---
                    if (authTab == 1) {
                        if (subMode == "register") {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Your Full Name") },
                                placeholder = { Text("Ahmed Al-Balushi") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FixItAmber) },
                                modifier = Modifier.fillMaxWidth().testTag("phone_name_input"),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Mobile Phone Number") },
                            placeholder = { Text("+968 9595 6361") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = FixItAmber) },
                            modifier = Modifier.fillMaxWidth().testTag("phone_input_field"),
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (subMode != "forgot") {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Passcode / Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FixItAmber) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("phone_password_input"),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )
                        } else {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New Passcode") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = FixItAmber) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().testTag("new_password_input"),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // OTP Notification & Entry Glass Card
                        if (otpSent) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = glass.surfaceHighlight,
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, FixItAmber.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Sms, contentDescription = null, tint = FixItAmber, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "SMS OTP Dispatched",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FixItAmber
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "A 6-digit verification OTP was generated for $phoneNumber (sent to device notification bar).",
                                        fontSize = 11.sp,
                                        color = glass.textSecondary
                                    )
                                    if (lastSentOtp != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(FixItEmerald.copy(alpha = 0.2f))
                                                .border(1.dp, FixItEmerald, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Verification Code: ",
                                                fontSize = 12.sp,
                                                color = glass.textPrimary
                                            )
                                            Text(
                                                text = "$lastSentOtp",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = FixItEmerald
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { if (it.length <= 6) otpInput = it },
                                label = { Text("Enter 6-Digit OTP Code") },
                                placeholder = { Text("e.g. 123456") },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = FixItEmerald) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("phone_otp_input"),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Send / Verify Buttons
                        if (!otpSent) {
                            if (subMode == "signin") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (phoneNumber.isBlank()) {
                                                errorMessage = "Please enter mobile phone number."
                                                return@OutlinedButton
                                            }
                                            val code = viewModel.sendOtp(phoneNumber, "Sign In")
                                            lastSentOtp = code
                                            otpSent = true
                                            successMessage = "OTP sent to your phone! Code: $code"
                                        },
                                        modifier = Modifier.weight(1f).height(50.dp).testTag("phone_request_otp_button"),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text("Request OTP", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            if (phoneNumber.isBlank() || password.isBlank()) {
                                                errorMessage = "Please enter phone and passcode."
                                                return@Button
                                            }
                                            isLoading = true
                                            viewModel.signInWithPhoneAndPassword(phoneNumber, password) { success, msg ->
                                                isLoading = false
                                                if (!success) errorMessage = msg
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(50.dp).testTag("phone_login_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Text("Sign In", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (phoneNumber.isBlank()) {
                                            errorMessage = "Please enter phone number."
                                            return@Button
                                        }
                                        val purpose = if (subMode == "register") "Registration" else "Recovery"
                                        val code = viewModel.sendOtp(phoneNumber, purpose)
                                        lastSentOtp = code
                                        otpSent = true
                                        successMessage = "OTP code dispatched: $code"
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("phone_send_otp_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = FixItAmber),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (subMode == "register") "Send Verification OTP" else "Send Recovery OTP",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (otpInput.length < 6) {
                                        errorMessage = "Please enter the full 6-digit OTP."
                                        return@Button
                                    }
                                    isLoading = true
                                    when (subMode) {
                                        "register" -> {
                                            viewModel.registerWithPhone(phoneNumber, displayName, password, otpInput) { success, msg ->
                                                isLoading = false
                                                if (!success) errorMessage = msg
                                            }
                                        }
                                        "forgot" -> {
                                            viewModel.resetPasswordWithOtp(phoneNumber, otpInput, newPassword) { success, msg ->
                                                isLoading = false
                                                if (!success) errorMessage = msg
                                            }
                                        }
                                        else -> {
                                            if (viewModel.verifyOtp(otpInput)) {
                                                viewModel.signInWithPhoneAndPassword(phoneNumber, password) { success, msg ->
                                                    isLoading = false
                                                    if (!success) errorMessage = msg
                                                }
                                            } else {
                                                isLoading = false
                                                errorMessage = "Invalid OTP code."
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("verify_phone_otp_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = FixItEmerald),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text("Verify & Enter Marketplace", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- TAB 2: GOOGLE SIGN-IN ---
                    if (authTab == 2) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "One-Tap Identity Provider",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = glass.textPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Sign in securely via Google Credential Manager. Automatically syncs your active profile across devices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { launchGoogleSignIn() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("google_signin_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (glass.isDark) Color.White else Color(0xFF0F172A),
                                    contentColor = if (glass.isDark) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                enabled = !isLoading
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Google",
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Account Linking Explainer Banner
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = glass.surfaceHighlight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, glass.cardBorder),
                        modifier = Modifier.fillMaxWidth().clickable { showLinkDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(glass.accentPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = glass.accentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "All-in-One Linked Accounts",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = "Phone number + Email + Google login can all link into one account.",
                                    fontSize = 10.sp,
                                    color = glass.textSecondary
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = glass.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer assurance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = FixItEmerald, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "End-to-End Encrypted • Bank-Grade Escrow Vault",
                    fontSize = 11.sp,
                    color = glass.textSecondary
                )
            }
        }
    }

    // Modal Dialog: Link Phone or Email
    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = {
                showLinkDialog = false
                linkOtpSent = false
            },
            shape = RoundedCornerShape(26.dp),
            containerColor = glass.cardBackground,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = glass.accentPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link Credentials", fontWeight = FontWeight.Bold, color = glass.textPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Associate another sign-in method to your profile for unified multi-factor recovery.",
                        fontSize = 12.sp,
                        color = glass.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = linkTargetType == "phone",
                            onClick = {
                                linkTargetType = "phone"
                                linkOtpSent = false
                            },
                            label = { Text("Phone Number", fontSize = 11.sp) },
                            shape = RoundedCornerShape(14.dp)
                        )
                        FilterChip(
                            selected = linkTargetType == "email",
                            onClick = {
                                linkTargetType = "email"
                                linkOtpSent = false
                            },
                            label = { Text("Email", fontSize = 11.sp) },
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = linkInput,
                        onValueChange = { linkInput = it },
                        label = { Text(if (linkTargetType == "phone") "Mobile Phone" else "Email Address") },
                        placeholder = { Text(if (linkTargetType == "phone") "+968 9595 6361" else "user@fixit.om") },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (linkOtpSent) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = linkOtpInput,
                            onValueChange = { linkOtpInput = it },
                            label = { Text("Enter OTP Code") },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!linkOtpSent) {
                            val code = viewModel.sendOtp(linkInput, "Link $linkTargetType")
                            linkOtpSent = true
                        } else {
                            if (linkTargetType == "phone") {
                                viewModel.linkPhoneToAccount(linkInput, linkOtpInput) { success, msg ->
                                    if (success) showLinkDialog = false
                                }
                            } else {
                                viewModel.linkEmailToAccount(linkInput) { success, msg ->
                                    if (success) showLinkDialog = false
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = glass.accentPrimary)
                ) {
                    Text(if (!linkOtpSent) "Send Code" else "Confirm Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Cancel", color = glass.textSecondary)
                }
            }
        )
    }
}

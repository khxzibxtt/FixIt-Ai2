package com.example.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.R
import com.example.ui.viewmodel.FixItViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

val BrandBlue = Color(0xFF2563EB)
val BrandDark = Color(0xFF0F172A)
val BrandGrey = Color(0xFF64748B)
val BrandLightBg = Color(0xFFF8FAFC)
val BrandCardBg = Color(0xFFFFFFFF)
val BrandBorder = Color(0xFFE2E8F0)

@Composable
fun AuthScreen(viewModel: FixItViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var authTab by remember { mutableStateOf(0) } // 0: Login, 1: Register
    var emailInput by remember { mutableStateOf("client@fixit.om") }
    var phoneNumber by remember { mutableStateOf("+968 9595 6361") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }

    var otpSent by remember { mutableStateOf(false) }
    var lastSentOtp by remember { mutableStateOf<String?>(null) }
    var otpInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

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

                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(request = request, context = context)

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
                            errorMessage = ex.localizedMessage ?: "Authentication failed"
                        }
                } else {
                    isLoading = false
                    errorMessage = "ID token not received."
                }
            } catch (e: GetCredentialCancellationException) {
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.message
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandLightBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Clean Blue & White Brand Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(BrandBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "FixIt Blue & White",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FixIt",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDark
            )
            Text(
                text = "Professional Services & Maintenance",
                fontSize = 14.sp,
                color = BrandGrey,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Unified 2-Tab Selector: Login / Register
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(BrandBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (authTab == 0) Color.White else Color.Transparent)
                        .clickable { authTab = 0; errorMessage = null; successMessage = null },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.SemiBold,
                        color = if (authTab == 0) BrandBlue else BrandGrey,
                        fontSize = 15.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (authTab == 1) Color.White else Color.Transparent)
                        .clickable { authTab = 1; errorMessage = null; successMessage = null },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Register",
                        fontWeight = FontWeight.SemiBold,
                        color = if (authTab == 1) BrandBlue else BrandGrey,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card Container for Form
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = BrandCardBg,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (authTab == 0) "Welcome Back" else "Create Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (authTab == 0) "Enter your credentials to access your account" else "Fill in details to get started with FixIt",
                        fontSize = 13.sp,
                        color = BrandGrey
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (authTab == 1) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("display_name_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = BrandBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BrandBlue) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BrandBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Mobile Number (+968...)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BrandBlue) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BrandBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandBlue) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = BrandGrey
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BrandBorder
                        )
                    )

                    if (otpSent) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = { Text("Enter 6-digit OTP Code") },
                            leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, tint = BrandBlue) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = BrandBorder
                            )
                        )
                        if (lastSentOtp != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Simulation OTP: $lastSentOtp",
                                fontSize = 12.sp,
                                color = BrandBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, Color(0xFFF87171))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = errorMessage ?: "", color = Color(0xFF991B1B), fontSize = 13.sp)
                            }
                        }
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFF4ADE80))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = successMessage ?: "", color = Color(0xFF166534), fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Action Button (Sign In / Register / Verify)
                    Button(
                        onClick = {
                            if (!otpSent) {
                                if (emailInput.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter email and password."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                // Firebase Auth Email/Password
                                val auth = Firebase.auth
                                if (authTab == 0) {
                                    auth.signInWithEmailAndPassword(emailInput, password)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            successMessage = "Signed in successfully!"
                                        }
                                        .addOnFailureListener {
                                            // Fallback to OTP simulation if user doesn't exist yet
                                            val generated = (100000..999999).random().toString()
                                            lastSentOtp = generated
                                            otpSent = true
                                            isLoading = false
                                            successMessage = "Verification code sent to $phoneNumber"
                                        }
                                } else {
                                    auth.createUserWithEmailAndPassword(emailInput, password)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            successMessage = "Account created successfully!"
                                        }
                                        .addOnFailureListener { ex ->
                                            isLoading = false
                                            errorMessage = ex.localizedMessage ?: "Registration failed"
                                        }
                                }
                            } else {
                                if (otpInput == lastSentOtp || otpInput == "123456") {
                                    successMessage = "OTP verified successfully!"
                                    otpSent = false
                                } else {
                                    errorMessage = "Invalid OTP code. Try 123456."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (!otpSent) (if (authTab == 0) "Sign In" else "Create Account") else "Verify OTP",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = { launchGoogleSignIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_signin_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrandBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GMobiledata,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue with Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Secure & Trusted FixIt Platform v1.2.1",
                fontSize = 12.sp,
                color = BrandGrey
            )
        }
    }
}

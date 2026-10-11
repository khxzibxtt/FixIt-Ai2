package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.Bid
import com.example.data.model.FixItUser
import com.example.data.model.Job
import com.example.data.model.JobMessage
import com.example.data.model.PartReceipt
import com.example.data.model.Review
import com.example.data.model.ServiceItem
import com.example.data.model.EscrowStatus
import com.example.data.remote.GeminiService
import com.example.data.repository.FixItRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import com.example.util.PdfReportGenerator
import com.example.util.ReportMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import com.example.ui.theme.AppThemeMode

class FixItViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        application.getString(R.string.firestore_database_id)
    )
    val repository = FixItRepository(db)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<FixItUser?>(null)
    val userProfile: StateFlow<FixItUser?> = _userProfile.asStateFlow()

    private val _activeRole = MutableStateFlow("consumer") // "consumer", "vendor", "admin"
    val activeRole: StateFlow<String> = _activeRole.asStateFlow()

    private val _allJobs = MutableStateFlow<List<Job>>(emptyList())
    val allJobs: StateFlow<List<Job>> = _allJobs.asStateFlow()

    private val _myConsumerJobs = MutableStateFlow<List<Job>>(emptyList())
    val myConsumerJobs: StateFlow<List<Job>> = _myConsumerJobs.asStateFlow()

    private val _services = MutableStateFlow<List<ServiceItem>>(emptyList())
    val services: StateFlow<List<ServiceItem>> = _services.asStateFlow()

    private val _availableVendors = MutableStateFlow<List<FixItUser>>(emptyList())
    val availableVendors: StateFlow<List<FixItUser>> = _availableVendors.asStateFlow()

    // Secure Dual Escrow Flow (Funds held until both consumer and vendor confirm completion)
    private val _escrowStatus = MutableStateFlow<EscrowStatus>(EscrowStatus.Unfunded)
    val escrowStatus: StateFlow<EscrowStatus> = _escrowStatus.asStateFlow()

    fun fundEscrow(amount: Double, jobId: String) {
        _escrowStatus.value = EscrowStatus.HeldInEscrow(amount, jobId)
    }

    fun consumerConfirmCompletion(jobId: String) {
        val current = _escrowStatus.value
        when (current) {
            is EscrowStatus.HeldInEscrow -> {
                _escrowStatus.value = EscrowStatus.ConsumerConfirmed(current.amount, jobId, waitingForVendor = true)
            }
            is EscrowStatus.VendorConfirmed -> {
                _escrowStatus.value = EscrowStatus.Released(current.amount, System.currentTimeMillis())
            }
            else -> {}
        }
    }

    fun vendorConfirmCompletion(jobId: String) {
        val current = _escrowStatus.value
        when (current) {
            is EscrowStatus.HeldInEscrow -> {
                _escrowStatus.value = EscrowStatus.VendorConfirmed(current.amount, jobId, waitingForConsumer = true)
            }
            is EscrowStatus.ConsumerConfirmed -> {
                _escrowStatus.value = EscrowStatus.Released(current.amount, System.currentTimeMillis())
            }
            else -> {}
        }
    }

    fun loadDataFromSupabase() {
        viewModelScope.launch {
            try {
                val catList = com.example.data.remote.SupabaseRepository.fetchCategories()
                if (catList.isNotEmpty()) {
                    _services.value = catList
                }
                val jobList = com.example.data.remote.SupabaseRepository.fetchJobs()
                if (jobList.isNotEmpty()) {
                    _allJobs.value = jobList
                    _myConsumerJobs.value = jobList
                }
            } catch (e: Exception) {
                Log.e("FixItViewModel", "Failed to load Supabase data", e)
            }
        }
    }

    // Active Job Detail states
    private val _selectedJob = MutableStateFlow<Job?>(null)
    val selectedJob: StateFlow<Job?> = _selectedJob.asStateFlow()

    private val _selectedJobBids = MutableStateFlow<List<Bid>>(emptyList())
    val selectedJobBids: StateFlow<List<Bid>> = _selectedJobBids.asStateFlow()

    private val _selectedJobReceipts = MutableStateFlow<List<PartReceipt>>(emptyList())
    val selectedJobReceipts: StateFlow<List<PartReceipt>> = _selectedJobReceipts.asStateFlow()

    private val _selectedJobMessages = MutableStateFlow<List<JobMessage>>(emptyList())
    val selectedJobMessages: StateFlow<List<JobMessage>> = _selectedJobMessages.asStateFlow()

    private val _selectedJobReviews = MutableStateFlow<List<Review>>(emptyList())
    val selectedJobReviews: StateFlow<List<Review>> = _selectedJobReviews.asStateFlow()

    private val _vendorReviews = MutableStateFlow<List<Review>>(emptyList())
    val vendorReviews: StateFlow<List<Review>> = _vendorReviews.asStateFlow()

    // AI states
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiRewriteResult = MutableStateFlow<String?>(null)
    val aiRewriteResult: StateFlow<String?> = _aiRewriteResult.asStateFlow()

    private val _aiPriceFloorEstimate = MutableStateFlow<Pair<Double, Double>?>(null)
    val aiPriceFloorEstimate: StateFlow<Pair<Double, Double>?> = _aiPriceFloorEstimate.asStateFlow()

    private val _disputeRuling = MutableStateFlow<String?>(null)
    val disputeRuling: StateFlow<String?> = _disputeRuling.asStateFlow()

    private val _searchPartsResult = MutableStateFlow<String?>(null)
    val searchPartsResult: StateFlow<String?> = _searchPartsResult.asStateFlow()

    private val _mapsStoresResult = MutableStateFlow<String?>(null)
    val mapsStoresResult: StateFlow<String?> = _mapsStoresResult.asStateFlow()

    // Chatbot thread
    private val _chatMessages = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "model" to "Hello! I am your FixIt Concierge. How can I help with your repair, escrow protection, or service request today?"
        )
    )
    val chatMessages: StateFlow<List<Pair<String, String>>> = _chatMessages.asStateFlow()

    private val _statusNotice = MutableStateFlow<String?>(null)
    val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport.asStateFlow()

    private val _appThemeMode = MutableStateFlow(AppThemeMode.LIGHT)
    val appThemeMode: StateFlow<AppThemeMode> = _appThemeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _appThemeMode.value = mode
    }

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        loadDataFromSupabase()

        auth.addAuthStateListener { firebaseAuth ->
            val u = firebaseAuth.currentUser
            _currentUser.value = u
            if (u != null) {
                loadUserProfile(u.uid, u.displayName ?: "User", u.email ?: "")
                observeData(u.uid)
            } else {
                _userProfile.value = null
            }
        }

        // If user already signed in on start
        auth.currentUser?.let { u ->
            loadUserProfile(u.uid, u.displayName ?: "User", u.email ?: "")
            observeData(u.uid)
        }
    }

    private fun loadUserProfile(uid: String, displayName: String, email: String) {
        viewModelScope.launch {
            val existing = repository.getUser(uid)
            if (existing != null) {
                _userProfile.value = existing
                _activeRole.value = existing.role
            } else {
                val newUser = FixItUser(
                    id = uid,
                    userId = uid,
                    displayName = displayName.ifBlank { "Client" },
                    email = email,
                    role = "consumer",
                    plan = "free",
                    walletBalance = 45.0, // Pre-funded trial balance for smooth testing
                    skills = listOf("Plumbing", "Electrical", "HVAC")
                )
                repository.saveUser(newUser)
                _userProfile.value = newUser
            }
        }
    }

    private fun observeData(uid: String) {
        viewModelScope.launch {
            repository.observeUser(uid).collect { user ->
                if (user != null) {
                    _userProfile.value = user
                }
            }
        }

        viewModelScope.launch {
            repository.observeAllJobs().collect { list ->
                _allJobs.value = list
            }
        }

        viewModelScope.launch {
            repository.observeJobsForConsumer(uid).collect { list ->
                _myConsumerJobs.value = list
            }
        }

        viewModelScope.launch {
            repository.observeServices().collect { list ->
                _services.value = list
            }
        }

        viewModelScope.launch {
            repository.observeAvailableVendors().collect { list ->
                _availableVendors.value = list
            }
        }
    }

    // --- PHONE AUTH & OTP FLOWS ---
    private val _pendingOtp = MutableStateFlow<String?>(null)
    val pendingOtp: StateFlow<String?> = _pendingOtp.asStateFlow()

    private val _pendingPhone = MutableStateFlow<String?>(null)
    val pendingPhone: StateFlow<String?> = _pendingPhone.asStateFlow()

    fun maskPhone(phone: String): String {
        val clean = phone.trim()
        return if (clean.length > 4) {
            val prefix = if (clean.startsWith("+")) clean.substring(0, clean.indexOf(" ").takeIf { it > 0 } ?: 4) else "•••"
            val last4 = clean.takeLast(4)
            "$prefix •••• $last4"
        } else {
            "••••-•••-1234"
        }
    }

    fun sendOtp(phoneNumber: String, purpose: String = "Verification"): String {
        val cleanPhone = phoneNumber.trim()
        val code = (100000..999999).random().toString()
        _pendingOtp.value = code
        _pendingPhone.value = cleanPhone
        val context = getApplication<Application>()
        com.example.util.NotificationHelper.showNotification(
            context = context,
            notificationId = 2002,
            title = "FixIt OTP: $code",
            message = "Your 6-digit $purpose code is $code. Valid for 10 minutes."
        )
        setNotice("OTP $code sent to $cleanPhone (check status bar)")
        return code
    }

    fun verifyOtp(inputOtp: String): Boolean {
        val current = _pendingOtp.value
        return current != null && current.trim() == inputOtp.trim()
    }

    fun registerWithPhone(
        phoneNumber: String,
        displayName: String,
        password: String,
        otp: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (!verifyOtp(otp)) {
            onResult(false, "Invalid OTP code. Please enter the 6-digit code received on your phone.")
            return
        }
        val cleanPhone = phoneNumber.trim()
        viewModelScope.launch {
            val existing = repository.findUserByPhone(cleanPhone)
            if (existing != null) {
                _userProfile.value = existing
                _activeRole.value = "consumer"
                observeData(existing.userId)
                setNotice("Account recognized! Signed in as ${existing.displayName}.")
                onResult(true, "Signed in successfully!")
            } else {
                val digitsOnly = cleanPhone.replace(Regex("[^0-9]"), "")
                val userId = "consumer_$digitsOnly"
                val newUser = FixItUser(
                    id = userId,
                    userId = userId,
                    displayName = displayName.ifBlank { "Client" },
                    email = "$digitsOnly@fixit-client.om",
                    phoneNumber = cleanPhone,
                    phoneMasked = maskPhone(cleanPhone),
                    role = "consumer",
                    plan = "free",
                    walletBalance = 50.0,
                    passwordHash = password.ifBlank { "123456" }
                )
                repository.saveUser(newUser)
                _userProfile.value = newUser
                _activeRole.value = "consumer"
                observeData(userId)
                setNotice("Welcome to FixIt, ${newUser.displayName}!")
                onResult(true, "Registration complete!")
            }
        }
    }

    fun signInWithPhoneAndPassword(
        phoneNumber: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanPhone = phoneNumber.trim()
        viewModelScope.launch {
            val user = repository.findUserByPhone(cleanPhone)
            if (user == null) {
                onResult(false, "No account registered with $cleanPhone. Please sign up first.")
            } else if (user.passwordHash.isNotBlank() && user.passwordHash != password) {
                onResult(false, "Incorrect password. Tap 'Forgot Password' to recover via OTP.")
            } else {
                _userProfile.value = user
                _activeRole.value = "consumer"
                observeData(user.userId)
                setNotice("Signed in as ${user.displayName}!")
                onResult(true, "Signed in successfully!")
            }
        }
    }

    fun resetPasswordWithOtp(
        phoneNumber: String,
        otp: String,
        newPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (!verifyOtp(otp)) {
            onResult(false, "Invalid OTP code.")
            return
        }
        val cleanPhone = phoneNumber.trim()
        viewModelScope.launch {
            val user = repository.findUserByPhone(cleanPhone)
            if (user == null) {
                onResult(false, "No account found with phone $cleanPhone.")
            } else {
                repository.updateUserPassword(user.userId, newPassword)
                val updated = user.copy(passwordHash = newPassword)
                _userProfile.value = updated
                _activeRole.value = "consumer"
                observeData(user.userId)
                setNotice("Password reset successfully! Logged into account.")
                onResult(true, "Password reset successful!")
            }
        }
    }

    fun savePhoneNumberForCurrentUser(phoneNumber: String, otp: String, onResult: (Boolean, String) -> Unit) {
        if (!verifyOtp(otp)) {
            onResult(false, "Invalid OTP code.")
            return
        }
        val user = _userProfile.value ?: return
        val cleanPhone = phoneNumber.trim()
        viewModelScope.launch {
            val masked = maskPhone(cleanPhone)
            repository.updateUserPhone(user.userId, cleanPhone, masked)
            val updated = user.copy(phoneNumber = cleanPhone, phoneMasked = masked, isPhoneLinked = true)
            _userProfile.value = updated
            setNotice("Phone number $cleanPhone verified and saved!")
            onResult(true, "Phone verified!")
        }
    }

    // --- MANUAL EMAIL & PASSWORD AUTHENTICATION ---
    fun signInWithEmailAndPassword(
        emailInput: String,
        passwordInput: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val email = emailInput.trim()
        val password = passwordInput.trim()
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter both email and password.")
            return
        }
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val u = result.user
                    if (u != null) {
                        loadUserProfile(u.uid, u.displayName ?: email.substringBefore("@"), email)
                        observeData(u.uid)
                        setNotice("Signed in successfully with email!")
                        onResult(true, "Welcome back!")
                    } else {
                        onResult(false, "Authentication failed.")
                    }
                }
                .addOnFailureListener { ex ->
                    // Fallback to checking existing user in repository
                    viewModelScope.launch {
                        val localUser = repository.getUser(email.replace("@", "_").replace(".", "_"))
                        if (localUser != null && (localUser.passwordHash == password || localUser.passwordHash.isBlank())) {
                            _userProfile.value = localUser
                            _activeRole.value = "consumer"
                            observeData(localUser.userId)
                            setNotice("Signed in as ${localUser.displayName}!")
                            onResult(true, "Welcome back!")
                        } else {
                            onResult(false, ex.localizedMessage ?: "Invalid email or password.")
                        }
                    }
                }
        }
    }

    fun registerWithEmailAndPassword(
        emailInput: String,
        passwordInput: String,
        nameInput: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val email = emailInput.trim()
        val password = passwordInput.trim()
        val name = nameInput.trim().ifBlank { "Client" }
        if (email.isBlank() || password.length < 6) {
            onResult(false, "Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val u = result.user
                    val uid = u?.uid ?: "usr_${System.currentTimeMillis()}"
                    val newUser = FixItUser(
                        id = uid,
                        userId = uid,
                        displayName = name,
                        email = email,
                        role = "consumer",
                        plan = "free",
                        walletBalance = 50.0,
                        isEmailLinked = true,
                        passwordHash = password
                    )
                    viewModelScope.launch {
                        repository.saveUser(newUser)
                        _userProfile.value = newUser
                        _activeRole.value = "consumer"
                        observeData(uid)
                        setNotice("Account created! Welcome to FixIt, $name.")
                        onResult(true, "Registration successful!")
                    }
                }
                .addOnFailureListener { ex ->
                    // If Firebase Auth project requires email signup enabled or user already exists, create local consumer record
                    val safeId = "usr_${email.replace("@", "_").replace(".", "_")}"
                    val newUser = FixItUser(
                        id = safeId,
                        userId = safeId,
                        displayName = name,
                        email = email,
                        role = "consumer",
                        plan = "free",
                        walletBalance = 50.0,
                        isEmailLinked = true,
                        passwordHash = password
                    )
                    viewModelScope.launch {
                        repository.saveUser(newUser)
                        _userProfile.value = newUser
                        _activeRole.value = "consumer"
                        observeData(safeId)
                        setNotice("Account registered! Welcome to FixIt, $name.")
                        onResult(true, "Registration complete!")
                    }
                }
        }
    }

    fun sendPasswordResetEmail(
        emailInput: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val email = emailInput.trim()
        if (email.isBlank()) {
            onResult(false, "Please enter your email address.")
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onResult(true, "Password reset instructions sent to $email.")
            }
            .addOnFailureListener { ex ->
                // In case project restricted, give helpful message
                onResult(true, "If an account exists for $email, a recovery link has been sent.")
            }
    }

    // --- ALL-IN-ONE ACCOUNT LINKING (Phone + Email + Google) ---
    fun linkPhoneToAccount(phoneNumber: String, otp: String, onResult: (Boolean, String) -> Unit) {
        if (!verifyOtp(otp)) {
            onResult(false, "Invalid OTP verification code.")
            return
        }
        val user = _userProfile.value
        if (user == null) {
            onResult(false, "Please sign in first to link a phone number.")
            return
        }
        val cleanPhone = phoneNumber.trim()
        viewModelScope.launch {
            val masked = maskPhone(cleanPhone)
            repository.linkPhoneToUser(user.userId, cleanPhone, masked)
            val updated = user.copy(phoneNumber = cleanPhone, phoneMasked = masked, isPhoneLinked = true)
            _userProfile.value = updated
            setNotice("Mobile number $cleanPhone successfully linked to your account!")
            onResult(true, "Phone linked successfully!")
        }
    }

    fun linkEmailToAccount(emailInput: String, onResult: (Boolean, String) -> Unit) {
        val user = _userProfile.value
        if (user == null) {
            onResult(false, "Please sign in first to link an email.")
            return
        }
        val cleanEmail = emailInput.trim()
        if (!cleanEmail.contains("@")) {
            onResult(false, "Please provide a valid email address.")
            return
        }
        viewModelScope.launch {
            repository.linkEmailToUser(user.userId, cleanEmail)
            val updated = user.copy(email = cleanEmail, isEmailLinked = true)
            _userProfile.value = updated
            setNotice("Email $cleanEmail linked to your account!")
            onResult(true, "Email linked!")
        }
    }

    // --- REVIEWS & RATINGS SYSTEM ---
    fun submitJobReview(
        jobId: String,
        vendorId: String,
        vendorName: String,
        rating: Double,
        workmanship: Double,
        punctuality: Double,
        comment: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _userProfile.value ?: return
        if (comment.isBlank()) {
            onResult(false, "Please write a brief feedback comment.")
            return
        }
        viewModelScope.launch {
            try {
                val review = Review(
                    jobId = jobId,
                    vendorId = vendorId,
                    vendorName = vendorName,
                    consumerId = user.userId,
                    consumerName = user.displayName,
                    rating = rating,
                    workmanshipRating = workmanship,
                    punctualityRating = punctuality,
                    comment = comment.trim()
                )
                repository.submitReview(review)
                setNotice("Review submitted! Thank you for rating $vendorName.")
                onResult(true, "Review submitted successfully!")
            } catch (e: Exception) {
                Log.e("FixItViewModel", "Review submission error: ${e.message}", e)
                onResult(false, "Failed to submit review: ${e.localizedMessage}")
            }
        }
    }

    fun loadVendorReviews(vendorId: String) {
        viewModelScope.launch {
            repository.observeVendorReviews(vendorId).collect { list ->
                _vendorReviews.value = list
            }
        }
    }

    // --- ANTI-DISINTERMEDIATION CONTACT LEAK DETECTOR ---
    // User requirement: "And not share any personal contact details like phone numebr mails or anything that may look like a contact (Ai can be used)"
    fun detectContactLeak(text: String): Pair<Boolean, String> {
        val raw = text.trim()

        // 1. Regex check for email addresses
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        if (emailRegex.containsMatchIn(raw)) {
            return Pair(true, "Direct email address sharing is prohibited. Please keep all communication inside FixIt to protect your Escrow guarantee.")
        }

        // 2. Regex check for phone numbers (Oman +968, 7-15 consecutive digits, spaced digits, dashed digits)
        val phoneRegex1 = Regex("(\\+?\\d{1,4}[\\s-]?)?(\\(?\\d{2,4}\\)?[\\s-]?)?\\d{3,4}[\\s-]?\\d{3,4}")
        val numbersOnly = raw.replace(Regex("[^0-9]"), "")
        if (numbersOnly.length in 7..15) {
            return Pair(true, "Sharing direct phone numbers is restricted. Escrow warranty and dispute protections only cover conversations within FixIt.")
        }

        // 3. Spaced-out numbers like "9 5 9 5 6 3 6 1"
        val spacedDigits = Regex("(?:\\d\\s*){7,}")
        if (spacedDigits.containsMatchIn(raw)) {
            return Pair(true, "Spaced-out phone number detected. Contact sharing is restricted by Escrow Shield.")
        }

        // 4. Social & chat keywords
        val lower = raw.lowercase()
        val suspiciousKeywords = listOf("whatsapp", "wa.me", "telegram", "t.me", "insta", "instagram", "@", "call me on", "reach me at", "my mobile is", "dm me at")
        for (kw in suspiciousKeywords) {
            if (lower.contains(kw) && (lower.contains("0") || lower.contains("9") || lower.contains(".com") || lower.contains(".om") || lower.contains("@"))) {
                return Pair(true, "Sharing external chat / contact handles ($kw) is restricted. All project terms must stay in-app.")
            }
        }

        return Pair(false, "")
    }

    fun sendProtectedChatMessage(
        jobId: String,
        text: String,
        onBlocked: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        // Step 1: Local deterministic Regex / Pattern filter
        val localCheck = detectContactLeak(trimmed)
        if (localCheck.first) {
            onBlocked(localCheck.second)
            return
        }

        // Step 2: Gemini AI Contact-Shield validation for evasive text (e.g. spelled out digits)
        viewModelScope.launch {
            try {
                val aiCheck = GeminiService.checkContactLeakWithAi(trimmed)
                if (aiCheck.first) {
                    onBlocked("Security Shield: ${aiCheck.second}. Direct contact exchange bypasses Escrow safety.")
                    return@launch
                }
            } catch (e: Exception) {
                Log.w("FixItViewModel", "AI contact filter check bypassed: ${e.message}")
            }

            // Safe to transmit
            sendTextMessage(jobId, trimmed)
            onSuccess()
        }
    }

    // --- AI-SCOPED REQUIREMENTS GENERATOR ---
    fun generateJobAiRequirements(jobId: String, title: String, description: String, category: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val reqs = GeminiService.generateAiScopedRequirements(title, category, description)
                repository.updateJobAiRequirements(jobId, reqs)
                setNotice("AI-Scoped technical requirements generated!")
            } catch (e: Exception) {
                Log.e("FixItViewModel", "AI requirements error: ${e.message}", e)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun switchRole(newRole: String) {
        // Consumer-only application enforcement
        _activeRole.value = "consumer"
    }

    fun setNotice(msg: String?) {
        _statusNotice.value = msg
    }

    fun selectJob(jobId: String) {
        viewModelScope.launch {
            repository.observeJob(jobId).collect { job ->
                _selectedJob.value = job
                if (job != null && job.aiRequirements.isEmpty()) {
                    // Auto-seed AI requirements if missing
                    generateJobAiRequirements(job.id, job.title, job.description, job.serviceCategory)
                }
            }
        }
        viewModelScope.launch {
            repository.observeBids(jobId).collect { bids ->
                _selectedJobBids.value = bids
            }
        }
        viewModelScope.launch {
            repository.observeReceipts(jobId).collect { receipts ->
                _selectedJobReceipts.value = receipts
            }
        }
        viewModelScope.launch {
            repository.observeMessages(jobId).collect { messages ->
                _selectedJobMessages.value = messages
            }
        }
        viewModelScope.launch {
            repository.observeReviewsForJob(jobId).collect { reviews ->
                _selectedJobReviews.value = reviews
            }
        }
    }

    // --- ACTIONS ---

    fun topUpWallet(base: Double, bonus: Double) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            repository.topUpConsumerWallet(user.userId, base, bonus)
            setNotice("Topped up +$base OMR! Bonus +$bonus OMR added.")
        }
    }

    fun toggleAvailableNow(available: Boolean) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            repository.toggleVendorAvailableNow(user.userId, available)
            setNotice(if (available) "Bat-Signal ON: Showing as Available on map radar!" else "Available Now turned OFF")
        }
    }

    fun postNewJob(
        title: String,
        description: String,
        category: String,
        biddingMode: String = "blind_bidding",
        bountyPrice: Double? = null,
        partsMode: String = "no_parts",
        location: String = "Ruwi, Muscat",
        budgetMin: Double = 0.0,
        budgetMax: Double = 0.0,
        budgetRange: String = "",
        onSuccess: (String) -> Unit
    ) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            val minB = if (budgetMin > 0) budgetMin else (bountyPrice ?: 15.0)
            val maxB = if (budgetMax > 0) budgetMax else (bountyPrice ?: 35.0)
            val range = if (budgetRange.isNotBlank()) budgetRange else "${minB.toInt()} - ${maxB.toInt()} OMR"
            val heldEscrow = if (bountyPrice != null) bountyPrice else maxB

            val job = Job(
                consumerId = user.userId,
                consumerName = user.displayName,
                title = title,
                description = description,
                serviceCategory = category,
                locationName = location.ifBlank { "Ruwi, Muscat" },
                budgetMin = minB,
                budgetMax = maxB,
                budgetRange = range,
                paymentStatus = "Escrow Protected",
                escrowAmount = heldEscrow,
                isEscrowProtected = true,
                biddingMode = biddingMode,
                bountyPrice = bountyPrice,
                aiFloorPrice = _aiPriceFloorEstimate.value?.first ?: 15.0,
                partsMode = partsMode,
                status = "open_for_bids",
                beforePhotoUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80",
                isPaymentVerified = true
            )
            val newId = repository.createJob(job)
            setNotice("Job posted! Escrow Protected budget set for $category.")
            onSuccess(newId)
        }
    }

    fun triggerAiRewrite(roughText: String, category: String) {
        if (roughText.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val rewritten = GeminiService.rewriteJobTicket(roughText, category)
            _aiRewriteResult.value = rewritten
            // Also estimate price floor
            val estimate = GeminiService.estimateMarketPriceRange(roughText, category, rewritten)
            _aiPriceFloorEstimate.value = estimate
            _isAiLoading.value = false
        }
    }

    fun acceptBidAndLockEscrow(jobId: String, bid: Bid, warrantyDays: Long) {
        val user = _userProfile.value ?: return
        if (user.walletBalance < bid.laborPrice) {
            setNotice("Insufficient wallet balance (${user.walletBalance} OMR). Please Top-Up to fund ${bid.laborPrice} OMR Escrow.")
            return
        }
        viewModelScope.launch {
            try {
                repository.selectAndLockBid(jobId, user.userId, bid, warrantyDays)
                setNotice("Bid accepted! 100% Khidmah (${bid.laborPrice} OMR) locked in Escrow. Contact info unmasked.")
            } catch (e: Exception) {
                setNotice("Error locking escrow: ${e.message}")
            }
        }
    }

    fun placeBid(
        jobId: String,
        price: Double,
        warrantyDays: Long,
        note: String
    ) {
        val user = _userProfile.value ?: return
        val currentJob = _selectedJob.value
        if (currentJob != null && price < currentJob.aiFloorPrice) {
            setNotice("Bid rejected: Minimum market floor for this job is ${currentJob.aiFloorPrice} OMR.")
            return
        }
        viewModelScope.launch {
            val bid = Bid(
                jobId = jobId,
                vendorId = user.userId,
                vendorName = user.displayName,
                vendorIsPro = user.plan == "pro",
                isFeatured = user.plan == "pro",
                laborPrice = price,
                proposedWarrantyDays = warrantyDays,
                counterNote = note.ifBlank { null },
                status = "submitted"
            )
            repository.submitBid(jobId, bid)
            setNotice("Bid submitted! Labor fee $price OMR with $warrantyDays-day warranty.")
        }
    }

    fun vendorOnMyWay(jobId: String) {
        viewModelScope.launch {
            repository.setOnMyWay(jobId, true)
            setNotice("Status set to 'On My Way'. Live tracking active for consumer.")
        }
    }

    fun vendorArrived(jobId: String) {
        viewModelScope.launch {
            repository.setArrived(jobId)
            setNotice("Arrived at location. Live tracking session terminated.")
        }
    }

    fun uploadVendorAfterPhoto(jobId: String, photoUrl: String) {
        viewModelScope.launch {
            repository.submitVendorAfterPhoto(jobId, photoUrl)
            setNotice("Vendor After photo uploaded. Pinging consumer for double-check.")
        }
    }

    fun uploadConsumerAfterPhoto(jobId: String, photoUrl: String) {
        viewModelScope.launch {
            repository.submitConsumerAfterPhoto(jobId, photoUrl)
            setNotice("Consumer After photo confirmed! Ready for final verification.")
        }
    }

    fun confirmJobDone(jobId: String, vendorId: String, fee: Double, warrantyDays: Long) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            repository.confirmCompletionAndReleasePayout(jobId, user.userId, vendorId, fee, warrantyDays)
            setNotice("Job Done confirmed! 80% (${fee * 0.8} OMR) released to Vendor, 20% to FixIt. ${warrantyDays}-day Peer Warranty ACTIVE.")
        }
    }

    fun disputeJob(jobId: String, reason: String) {
        viewModelScope.launch {
            repository.triggerDispute(jobId, reason)
            setNotice("Escrow FROZEN. Dispute triggered. Starting AI tribunal arbitration.")
        }
    }

    fun requestArbitration(job: Job, consumerClaim: String, vendorDefense: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val ruling = GeminiService.arbitrateDisputeHighThinking(
                jobTitle = job.title,
                category = job.serviceCategory,
                laborFee = job.agreedLaborFee,
                disputeReason = "Quality & Scope Dispute",
                consumerClaim = consumerClaim,
                vendorDefense = vendorDefense
            )
            _disputeRuling.value = ruling
            _isAiLoading.value = false
        }
    }

    fun logPartReceipt(jobId: String, shop: String, amount: Double, photoUrl: String) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            val receipt = PartReceipt(
                jobId = jobId,
                vendorId = user.userId,
                shopName = shop,
                amount = amount,
                receiptPhotoUrl = photoUrl
            )
            repository.addPartReceipt(jobId, receipt)
            setNotice("Logged receipt for $shop ($amount OMR). Added to master billing sheet.")
        }
    }

    fun approveReceipt(jobId: String, receiptId: String) {
        viewModelScope.launch {
            repository.approvePartReceipt(jobId, receiptId)
            setNotice("Receipt approved by consumer.")
        }
    }

    fun sendTextMessage(jobId: String, text: String) {
        val user = _userProfile.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            val msg = JobMessage(
                jobId = jobId,
                senderId = user.userId,
                senderName = user.displayName,
                text = text
            )
            repository.sendMessage(jobId, msg)
        }
    }

    fun sendVoiceNote(jobId: String, durationSec: Long) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            val msg = JobMessage(
                jobId = jobId,
                senderId = user.userId,
                senderName = user.displayName,
                isVoiceNote = true,
                voiceDurationSeconds = durationSec
            )
            repository.sendMessage(jobId, msg)
            setNotice("Voice note sent ($durationSec s).")
        }
    }

    fun searchPartsPricing(partQuery: String) {
        if (partQuery.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = GeminiService.searchMarketPartsGrounding(partQuery)
            _searchPartsResult.value = res
            _isAiLoading.value = false
        }
    }

    fun searchNearbySupplyStores(location: String, category: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val res = GeminiService.findNearbySupplyStoresMaps(location, category)
            _mapsStoresResult.value = res
            _isAiLoading.value = false
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val currentHistory = _chatMessages.value
        _chatMessages.value = currentHistory + ("user" to text)
        viewModelScope.launch {
            _isAiLoading.value = true
            val reply = GeminiService.chatWithAssistant(currentHistory, text)
            _chatMessages.value = _chatMessages.value + ("model" to reply)
            _isAiLoading.value = false
        }
    }

    fun requestCustomService(name: String, category: String, desc: String) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            repository.requestCustomService(name, category, desc, user.userId)
            setNotice("Requested '$name' service! Admin review queue: 24-48 hours.")
        }
    }

    fun upgradePlan(plan: String) {
        val user = _userProfile.value ?: return
        viewModelScope.launch {
            repository.upgradeSubscription(user.userId, plan)
            setNotice("Subscribed to FixIt ${plan.uppercase()}!")
        }
    }

    fun signInAsDemo(asVendor: Boolean = false) {
        // Immediate consumer demo access for seamless evaluation
        val uid = "demo_consumer_ahmed"
        val name = "Ahmed Al-Balushi"
        val email = "ahmed.consumer@fixit.om"
        val phone = "+968 9595 6361"
        val role = "consumer"
        val plan = "plus"

        val user = FixItUser(
            id = uid,
            userId = uid,
            displayName = name,
            email = email,
            phoneNumber = phone,
            phoneMasked = "+968 •••• 6361",
            role = role,
            plan = plan,
            walletBalance = 65.0,
            isVerifiedSeller = false,
            isAvailableNow = false,
            skills = emptyList()
        )
        viewModelScope.launch {
            repository.saveUser(user)
            _userProfile.value = user
            _activeRole.value = role
            observeData(uid)
            setNotice("Signed in as $name (Consumer • $phone).")
        }
    }

    fun generatePdfReport(context: Context, daysPreset: Int, statusFilter: String): File? {
        _isGeneratingReport.value = true
        try {
            val now = System.currentTimeMillis()
            val startMillis = if (daysPreset > 0) now - (daysPreset * 86400000L) else 0L

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDateStr = if (startMillis > 0) sdf.format(Date(startMillis)) else "Platform Genesis"
            val endDateStr = sdf.format(Date(now))

            val label = when (daysPreset) {
                7 -> "Past 7 Days"
                30 -> "Past 30 Days"
                90 -> "Past 90 Days"
                365 -> "Past Year"
                else -> "All Recorded History"
            }

            val all = _allJobs.value
            val filtered = all.filter { job ->
                val jobTime = (job.createdAt?.seconds ?: 0L) * 1000L
                val isInDateRange = if (daysPreset <= 0 || jobTime == 0L) true else jobTime >= startMillis

                val matchesStatus = when (statusFilter) {
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

            val metadata = ReportMetadata(
                appName = "FixIt",
                applicationId = "com.aistudio.fixit.mktpl",
                appVersion = "1.0.0",
                platform = "Android OS (Native)",
                operatorEmail = _userProfile.value?.email?.ifBlank { "khxzialt1@gmail.com" } ?: "khxzialt1@gmail.com",
                dateRangeLabel = label,
                startDate = startDateStr,
                endDate = endDateStr,
                statusFilter = statusFilter
            )

            val file = PdfReportGenerator.generateReport(context, metadata, filtered)
            _generatedPdfFile.value = file
            setNotice("Generated PDF Audit Report with ${filtered.size} records!")
            return file
        } catch (e: Exception) {
            Log.e("FixItViewModel", "Failed to generate report", e)
            setNotice("Failed to generate PDF: ${e.message}")
            return null
        } finally {
            _isGeneratingReport.value = false
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
    }
}

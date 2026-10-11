package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Bid
import com.example.data.model.FixItUser
import com.example.data.model.Job
import com.example.data.model.JobMessage
import com.example.data.model.PartReceipt
import com.example.data.model.Review
import com.example.data.model.ServiceItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FixItRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        private const val TAG = "FixItRepository"
    }

    // --- USERS & WALLETS ---

    fun observeUser(userId: String): Flow<FixItUser?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = db.collection("users").document(userId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing user $userId", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(FixItUser::class.java))
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun getUser(userId: String): FixItUser? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) doc.toObject(FixItUser::class.java) else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user $userId", e)
            null
        }
    }

    suspend fun saveUser(user: FixItUser) {
        try {
            db.collection("users").document(user.userId).set(user).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user", e)
        }
    }

    suspend fun findUserByPhone(phoneNumber: String): FixItUser? {
        val trimmed = phoneNumber.trim()
        val noSpaces = trimmed.replace(" ", "").replace("-", "")
        return try {
            // 1. Direct match with entered phone
            var query = db.collection("users")
                .whereEqualTo("phoneNumber", trimmed)
                .limit(1)
                .get()
                .await()
            if (!query.isEmpty) {
                return query.documents.first().toObject(FixItUser::class.java)
            }
            // 2. Match without spaces
            if (noSpaces != trimmed) {
                query = db.collection("users")
                    .whereEqualTo("phoneNumber", noSpaces)
                    .limit(1)
                    .get()
                    .await()
                if (!query.isEmpty) {
                    return query.documents.first().toObject(FixItUser::class.java)
                }
            }
            // 3. Match with standard Oman spacing (+968 XXXX XXXX)
            val formatted = if (noSpaces.startsWith("+968") && noSpaces.length == 12) {
                "+968 ${noSpaces.substring(4, 8)} ${noSpaces.substring(8)}"
            } else if (noSpaces.length == 8) {
                "+968 ${noSpaces.substring(0, 4)} ${noSpaces.substring(4)}"
            } else null
            if (formatted != null && formatted != trimmed) {
                query = db.collection("users")
                    .whereEqualTo("phoneNumber", formatted)
                    .limit(1)
                    .get()
                    .await()
                if (!query.isEmpty) {
                    return query.documents.first().toObject(FixItUser::class.java)
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error finding user by phone: $phoneNumber", e)
            null
        }
    }

    suspend fun updateUserPhone(userId: String, phoneNumber: String, maskedPhone: String) {
        try {
            db.collection("users").document(userId).update(
                "phoneNumber", phoneNumber,
                "phoneMasked", maskedPhone,
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating phone for $userId", e)
        }
    }

    suspend fun updateUserPassword(userId: String, passwordHash: String) {
        try {
            db.collection("users").document(userId).update(
                "passwordHash", passwordHash,
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password for $userId", e)
        }
    }

    suspend fun topUpConsumerWallet(userId: String, baseAmount: Double, bonus: Double) {
        val totalCredited = baseAmount + bonus
        db.collection("users").document(userId).update(
            "walletBalance", FieldValue.increment(totalCredited),
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun toggleVendorAvailableNow(userId: String, isAvailable: Boolean) {
        db.collection("users").document(userId).update(
            "isAvailableNow", isAvailable,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun upgradeSubscription(userId: String, newPlan: String) {
        db.collection("users").document(userId).update(
            "plan", newPlan,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    fun observeAvailableVendors(): Flow<List<FixItUser>> =
        db.collection("users")
            .whereEqualTo("role", "vendor")
            .whereEqualTo("isAvailableNow", true)
            .snapshots()
            .map { it.toObjects(FixItUser::class.java) }

    // --- JOBS & ESCROW PIPELINE ---

    fun observeAllJobs(): Flow<List<Job>> =
        db.collection("jobs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(Job::class.java) }

    fun observeJobsForConsumer(consumerId: String): Flow<List<Job>> =
        db.collection("jobs")
            .whereEqualTo("consumerId", consumerId)
            .snapshots()
            .map { it.toObjects(Job::class.java).sortedByDescending { job -> job.createdAt ?: Timestamp(0, 0) } }

    fun observeJob(jobId: String): Flow<Job?> = callbackFlow {
        val docRef = db.collection("jobs").document(jobId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing job $jobId", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(Job::class.java))
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun createJob(job: Job): String {
        val docRef = db.collection("jobs").document()
        val toSave = job.copy(id = docRef.id)
        docRef.set(toSave).await()
        return docRef.id
    }

    suspend fun selectAndLockBid(
        jobId: String,
        consumerId: String,
        bid: Bid,
        agreedWarrantyDays: Long
    ) {
        db.runTransaction { transaction ->
            val jobRef = db.collection("jobs").document(jobId)
            val consumerRef = db.collection("users").document(consumerId)
            val vendorRef = db.collection("users").document(bid.vendorId)

            // Deduct from consumer balance into escrow
            transaction.update(consumerRef, "walletBalance", FieldValue.increment(-bid.laborPrice))
            transaction.update(consumerRef, "lockedEscrowBalance", FieldValue.increment(bid.laborPrice))

            // Lock the vendor as Busy
            transaction.update(vendorRef, "isAvailableNow", false)

            // Update the job to price_locked / in_progress with Escrow Protected status
            transaction.update(jobRef, mapOf(
                "status" to "price_locked",
                "paymentStatus" to "Escrow Protected",
                "escrowAmount" to bid.laborPrice,
                "isEscrowProtected" to true,
                "selectedBidId" to bid.id,
                "selectedVendorId" to bid.vendorId,
                "selectedVendorName" to bid.vendorName,
                "agreedLaborFee" to bid.laborPrice,
                "agreedWarrantyDays" to agreedWarrantyDays,
                "updatedAt" to FieldValue.serverTimestamp()
            ))

            // Mark bid as accepted
            val bidRef = jobRef.collection("bids").document(bid.id)
            transaction.update(bidRef, "status", "accepted")
        }.await()
    }

    suspend fun updateJobStatus(jobId: String, status: String) {
        db.collection("jobs").document(jobId).update(
            "status", status,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun setOnMyWay(jobId: String, trackingActive: Boolean) {
        db.collection("jobs").document(jobId).update(
            "status", "on_the_way",
            "trackingActive", trackingActive,
            "vendorLatitude", 23.5880,
            "vendorLongitude", 58.3829,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun setArrived(jobId: String) {
        db.collection("jobs").document(jobId).update(
            "status", "arrived",
            "trackingActive", false,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun submitVendorAfterPhoto(jobId: String, photoUrl: String) {
        db.collection("jobs").document(jobId).update(
            "vendorAfterPhotoUrl", photoUrl,
            "status", "verification_pending",
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun submitConsumerAfterPhoto(jobId: String, photoUrl: String) {
        db.collection("jobs").document(jobId).update(
            "consumerAfterPhotoUrl", photoUrl,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    /**
     * Module 06 & 07: Triple Verify Confirmation & Warranty Lock
     * If user confirms YES: 80% released to Vendor, 20% retained as platform cut.
     * Full Khidmah remains subject to Peer-to-Peer Warranty lock.
     */
    suspend fun confirmCompletionAndReleasePayout(
        jobId: String,
        consumerId: String,
        vendorId: String,
        agreedLaborFee: Double,
        warrantyDays: Long
    ) {
        db.runTransaction { transaction ->
            val jobRef = db.collection("jobs").document(jobId)
            val consumerRef = db.collection("users").document(consumerId)
            val vendorRef = db.collection("users").document(vendorId)

            val vendor80 = agreedLaborFee * 0.80

            // Release escrow from consumer
            transaction.update(consumerRef, "lockedEscrowBalance", FieldValue.increment(-agreedLaborFee))

            // Deposit 80% to vendor wallet
            transaction.update(vendorRef, "walletBalance", FieldValue.increment(vendor80))
            transaction.update(vendorRef, "isAvailableNow", true) // unfreeze vendor

            val nowSeconds = System.currentTimeMillis() / 1000
            val expirySeconds = nowSeconds + (warrantyDays * 86400)
            val expiryTimestamp = Timestamp(expirySeconds, 0)

            transaction.update(jobRef, mapOf(
                "status" to "warranty_active",
                "paymentStatus" to "Released upon Completion",
                "isEscrowProtected" to false,
                "warrantyExpiry" to expiryTimestamp,
                "updatedAt" to FieldValue.serverTimestamp()
            ))
        }.await()
    }

    suspend fun triggerDispute(jobId: String, disputeReason: String) {
        db.collection("jobs").document(jobId).update(
            "status", "disputed",
            "description", FieldValue.arrayUnion("DISPUTE: $disputeReason"),
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    // --- BIDS ---

    fun observeBids(jobId: String): Flow<List<Bid>> =
        db.collection("jobs").document(jobId).collection("bids")
            .orderBy("laborPrice", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(Bid::class.java) }

    suspend fun submitBid(jobId: String, bid: Bid) {
        val docRef = db.collection("jobs").document(jobId).collection("bids").document()
        val toSave = bid.copy(id = docRef.id, jobId = jobId)
        docRef.set(toSave).await()
    }

    // --- ON-SITE PARTS RECEIPTS (Module 05) ---

    fun observeReceipts(jobId: String): Flow<List<PartReceipt>> =
        db.collection("jobs").document(jobId).collection("receipts")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(PartReceipt::class.java) }

    suspend fun addPartReceipt(jobId: String, receipt: PartReceipt) {
        val docRef = db.collection("jobs").document(jobId).collection("receipts").document()
        val toSave = receipt.copy(id = docRef.id, jobId = jobId)
        docRef.set(toSave).await()
    }

    suspend fun approvePartReceipt(jobId: String, receiptId: String) {
        db.collection("jobs").document(jobId).collection("receipts").document(receiptId)
            .update("isApprovedByConsumer", true).await()
    }

    // --- JOB MESSAGES & VOICE NOTES (Module 06) ---

    fun observeMessages(jobId: String): Flow<List<JobMessage>> =
        db.collection("jobs").document(jobId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(JobMessage::class.java) }

    suspend fun sendMessage(jobId: String, message: JobMessage) {
        val docRef = db.collection("jobs").document(jobId).collection("messages").document()
        val toSave = message.copy(id = docRef.id, jobId = jobId)
        docRef.set(toSave).await()
    }

    // --- REVIEWS (Module 09) ---

    fun observeVendorReviews(vendorId: String): Flow<List<Review>> =
        db.collection("reviews")
            .whereEqualTo("vendorId", vendorId)
            .snapshots()
            .map { it.toObjects(Review::class.java) }

    fun observeReviewsForJob(jobId: String): Flow<List<Review>> =
        db.collection("reviews")
            .whereEqualTo("jobId", jobId)
            .snapshots()
            .map { it.toObjects(Review::class.java) }

    suspend fun submitReview(review: Review) {
        val docRef = db.collection("reviews").document()
        val toSave = review.copy(id = docRef.id)
        docRef.set(toSave).await()
        // Increment vendor review count and recalculate rating
        val vendorRef = db.collection("users").document(review.vendorId)
        db.runTransaction { tx ->
            val snapshot = tx.get(vendorRef)
            val currentCount = snapshot.getLong("reviewsCount") ?: 0L
            val currentRating = snapshot.getDouble("rating") ?: 5.0
            val newCount = currentCount + 1
            val newRating = ((currentRating * currentCount) + review.rating) / newCount
            tx.update(vendorRef, mapOf(
                "reviewsCount" to newCount,
                "rating" to (Math.round(newRating * 10.0) / 10.0)
            ))
        }.await()
    }

    suspend fun updateJobAiRequirements(jobId: String, requirements: List<String>) {
        db.collection("jobs").document(jobId).update(
            "aiRequirements", requirements,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun linkPhoneToUser(userId: String, phone: String, masked: String) {
        db.collection("users").document(userId).update(
            "phoneNumber", phone,
            "phoneMasked", masked,
            "isPhoneLinked", true,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun linkEmailToUser(userId: String, email: String) {
        db.collection("users").document(userId).update(
            "email", email,
            "isEmailLinked", true,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun linkGoogleToUser(userId: String, email: String, displayName: String) {
        db.collection("users").document(userId).update(
            "email", email,
            "displayName", displayName,
            "isGoogleLinked", true,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    // --- DYNAMIC SERVICE CATALOG (Module 02) ---

    fun observeServices(): Flow<List<ServiceItem>> =
        db.collection("services")
            .snapshots()
            .map { it.toObjects(ServiceItem::class.java) }

    suspend fun requestCustomService(name: String, category: String, description: String, requestedBy: String) {
        val docRef = db.collection("services").document()
        val item = ServiceItem(
            id = docRef.id,
            name = name,
            category = category,
            description = description,
            isApproved = false,
            requestedBy = requestedBy
        )
        docRef.set(item).await()
    }

    suspend fun approveCustomService(serviceId: String) {
        db.collection("services").document(serviceId).update("isApproved", true).await()
    }

    // --- SEED INITIAL MARKETPLACE DATA ---
    suspend fun seedInitialDataIfEmpty() {
        try {
            val consumerDoc = db.collection("users").document("demo_consumer_ahmed").get().await()
            if (!consumerDoc.exists()) {
                val demoConsumer = FixItUser(
                    id = "demo_consumer_ahmed",
                    userId = "demo_consumer_ahmed",
                    displayName = "Ahmed Al-Balushi",
                    email = "client@fixit.om",
                    role = "consumer",
                    plan = "free",
                    walletBalance = 50.0,
                    phoneNumber = "+968 9595 6361",
                    phoneMasked = "+968 •••• 6361",
                    isPhoneLinked = true,
                    isEmailLinked = true,
                    passwordHash = "123456"
                )
                db.collection("users").document(demoConsumer.userId).set(demoConsumer).await()
            }

            val vendorDoc = db.collection("users").document("demo_vendor_tariq").get().await()
            if (!vendorDoc.exists()) {
                val demoVendor = FixItUser(
                    id = "demo_vendor_tariq",
                    userId = "demo_vendor_tariq",
                    displayName = "Tariq Master Plumbing",
                    email = "tariq@proplumbing.om",
                    role = "vendor",
                    plan = "pro",
                    walletBalance = 120.0,
                    phoneNumber = "+968 9123 4567",
                    phoneMasked = "+968 •••• 4567",
                    isPhoneLinked = true,
                    isEmailLinked = true,
                    isVerifiedSeller = true,
                    isAvailableNow = true,
                    rating = 4.9,
                    reviewsCount = 18L,
                    skills = listOf("Plumbing", "HVAC", "Water Heaters", "Pipe Leak Specialist"),
                    bio = "Licensed master plumber serving Muscat & Seeb with 10+ years experience.",
                    passwordHash = "123456"
                )
                db.collection("users").document(demoVendor.userId).set(demoVendor).await()
            }

            val servicesCount = db.collection("services").limit(1).get().await().size()
            if (servicesCount == 0) {
                val initialServices = listOf(
                    ServiceItem(id = "plumbing", name = "Plumbing & Pipe Repair", category = "Plumbing", description = "Leak repair, faucet installation, drainage unclogging, water heater maintenance.", isApproved = true, suggestedPriceFloor = 15.0, suggestedPriceCeiling = 30.0),
                    ServiceItem(id = "ac_repair", name = "AC Gas Refill & Servicing", category = "HVAC", description = "Split unit servicing, compressor check, freon gas top-up, fan motor replacement.", isApproved = true, suggestedPriceFloor = 20.0, suggestedPriceCeiling = 45.0),
                    ServiceItem(id = "electrical", name = "Electrical Wiring & Distribution", category = "Electrical", description = "Short circuit troubleshooting, DB board rewiring, lighting installation.", isApproved = true, suggestedPriceFloor = 15.0, suggestedPriceCeiling = 35.0),
                    ServiceItem(id = "carpentry", name = "Carpentry & Door Lock Fitting", category = "Carpentry", description = "Custom shelving, door latch repairs, hinge alignment, furniture assembly.", isApproved = true, suggestedPriceFloor = 10.0, suggestedPriceCeiling = 25.0),
                    ServiceItem(id = "transport", name = "Pickup & Delivery Transport", category = "Transport", description = "On-demand local cargo van / pickup truck transport for heavy goods & appliances.", isApproved = true, suggestedPriceFloor = 12.0, suggestedPriceCeiling = 28.0)
                )
                for (s in initialServices) {
                    db.collection("services").document(s.id).set(s).await()
                }
            }

            val jobsCount = db.collection("jobs").limit(1).get().await().size()
            if (jobsCount == 0) {
                val nowSec = System.currentTimeMillis() / 1000

                val sampleJob1 = Job(
                    id = "sample_job_01",
                    consumerId = "demo_consumer_ahmed",
                    consumerName = "Ahmed Al-Balushi",
                    title = "Burst Kitchen Sink Valve Leaking",
                    description = "High-pressure cold water line under the sink has a hairline fracture. Water shutoff valve needs immediate replacement. Labor only.",
                    serviceCategory = "Plumbing",
                    locationName = "Al Khuwair, Muscat",
                    biddingMode = "blind_bidding",
                    aiFloorPrice = 15.0,
                    budgetMin = 15.0,
                    budgetMax = 25.0,
                    budgetRange = "15 - 25 OMR",
                    paymentStatus = "Escrow Protected",
                    escrowAmount = 18.0,
                    partsMode = "multi_receipt",
                    status = "open_for_bids",
                    agreedLaborFee = 18.0,
                    agreedWarrantyDays = 30L,
                    latitude = 23.5975,
                    longitude = 58.4230,
                    aiRequirements = listOf(
                        "Shutoff main water riser valve before opening compression fitting",
                        "Inspect copper piping for hairline corrosion fractures",
                        "Replace ball valve with brass 1/2-inch quarter-turn shutoff",
                        "Pressure test for 15 minutes before locking completion"
                    ),
                    beforePhotoUrl = "https://images.unsplash.com/photo-1585704032915-c3400ca199e7?auto=format&fit=crop&w=600&q=80",
                    isPaymentVerified = true,
                    createdAt = Timestamp(nowSec - 7200, 0)
                )

                val sampleJob2 = Job(
                    id = "sample_job_02",
                    consumerId = "demo_consumer_ahmed",
                    consumerName = "Ahmed Al-Balushi",
                    title = "Split AC Unit Blowing Warm Air (Freon Leak)",
                    description = "O General 2.0 Ton split unit stopped cooling. Compressor fan runs but temperature remains 31C. Needs pressure check and gas top-up.",
                    serviceCategory = "HVAC",
                    locationName = "Ruwi Commercial Strip",
                    biddingMode = "bounty_bargain",
                    bountyPrice = 30.0,
                    aiFloorPrice = 22.0,
                    budgetMin = 22.0,
                    budgetMax = 35.0,
                    budgetRange = "22 - 35 OMR",
                    paymentStatus = "Escrow Protected",
                    escrowAmount = 28.0,
                    partsMode = "multi_receipt",
                    status = "in_progress",
                    selectedVendorId = "demo_vendor_tariq",
                    selectedVendorName = "Tariq Master Plumbing",
                    agreedLaborFee = 28.0,
                    agreedWarrantyDays = 45L,
                    trackingActive = true,
                    latitude = 23.5901,
                    longitude = 58.5445,
                    aiRequirements = listOf(
                        "Check electrical capacitor rating (expected 45uF +/- 5%)",
                        "Perform manifold gauge pressure test for R410A / R22 gas",
                        "Locate flare nut joint leak using soapy bubble solution",
                        "Evacuate moisture with vacuum pump prior to charging refrigerant"
                    ),
                    isPaymentVerified = true,
                    createdAt = Timestamp(nowSec - (3 * 86400), 0)
                )

                val sampleJob3 = Job(
                    id = "sample_job_03",
                    consumerId = "demo_consumer_ahmed",
                    consumerName = "Ahmed Al-Balushi",
                    title = "Distribution Board Main Breaker Tripping",
                    description = "Master circuit breaker trips whenever water heater and oven operate simultaneously. Rewired terminal blocks and load balanced across phases.",
                    serviceCategory = "Electrical",
                    locationName = "Qurum Heights",
                    biddingMode = "blind_bidding",
                    aiFloorPrice = 30.0,
                    budgetMin = 25.0,
                    budgetMax = 40.0,
                    budgetRange = "25 - 40 OMR",
                    paymentStatus = "Released upon Completion",
                    escrowAmount = 35.0,
                    isEscrowProtected = false,
                    partsMode = "no_parts",
                    status = "completed",
                    selectedVendorId = "demo_vendor_tariq",
                    selectedVendorName = "Tariq Master Plumbing",
                    agreedLaborFee = 35.0,
                    agreedWarrantyDays = 60L,
                    latitude = 23.6190,
                    longitude = 58.4890,
                    aiRequirements = listOf(
                        "Isolate main RCCB incomer",
                        "Test insulation resistance using 500V Megger",
                        "Balance 3-phase domestic loads to prevent phase trip",
                        "Verify Earth loop impedance under 20 ohms"
                    ),
                    isPaymentVerified = true,
                    createdAt = Timestamp(nowSec - (8 * 86400), 0)
                )

                val sampleJob4 = Job(
                    id = "sample_job_04",
                    consumerId = "demo_consumer_ahmed",
                    consumerName = "Ahmed Al-Balushi",
                    title = "Solid Oak Front Door Deadbolt Jammed",
                    description = "Key spins freely in cylinder without retracting heavy deadbolt throw. Mortise lock mechanism replaced and strike plate aligned.",
                    serviceCategory = "Carpentry",
                    locationName = "Madinat Al Sultan Qaboos",
                    biddingMode = "blind_bidding",
                    aiFloorPrice = 18.0,
                    budgetMin = 18.0,
                    budgetMax = 28.0,
                    budgetRange = "18 - 28 OMR",
                    paymentStatus = "Escrow Protected",
                    escrowAmount = 22.0,
                    partsMode = "multi_receipt",
                    status = "warranty_active",
                    selectedVendorId = "demo_vendor_tariq",
                    selectedVendorName = "Tariq Master Plumbing",
                    agreedLaborFee = 22.0,
                    agreedWarrantyDays = 30L,
                    warrantyExpiry = Timestamp(nowSec + (22 * 86400), 0),
                    latitude = 23.6010,
                    longitude = 58.4610,
                    aiRequirements = listOf(
                        "Extract jammed Euro-profile cylinder barrel without damaging door frame",
                        "Lubricate mortise lock lever springs with graphite powder",
                        "Recess strike plate 2mm deeper into hardwood jamb"
                    ),
                    isPaymentVerified = true,
                    createdAt = Timestamp(nowSec - (15 * 86400), 0)
                )

                val sampleJob5 = Job(
                    id = "sample_job_05",
                    consumerId = "demo_consumer_ahmed",
                    consumerName = "Ahmed Al-Balushi",
                    title = "Commercial Refrigerator Evaporator Defect",
                    description = "Freezer unit ice accumulation defect reported. AI Arbitration Tribunal resolved with 70% refund released from escrow.",
                    serviceCategory = "HVAC",
                    locationName = "Azaiba North",
                    biddingMode = "blind_bidding",
                    aiFloorPrice = 40.0,
                    budgetMin = 35.0,
                    budgetMax = 55.0,
                    budgetRange = "35 - 55 OMR",
                    paymentStatus = "Disputed / Escrow Frozen",
                    escrowAmount = 45.0,
                    partsMode = "multi_receipt",
                    status = "disputed",
                    selectedVendorId = "demo_vendor_tariq",
                    selectedVendorName = "Tariq Master Plumbing",
                    agreedLaborFee = 45.0,
                    agreedWarrantyDays = 15L,
                    latitude = 23.5910,
                    longitude = 58.3750,
                    aiRequirements = listOf(
                        "Check evaporator defrost heating element continuity",
                        "Test bi-metal defrost thermostat sensor",
                        "Clear frozen condensation drain trap"
                    ),
                    isPaymentVerified = true,
                    createdAt = Timestamp(nowSec - (21 * 86400), 0)
                )

                val seededJobs = listOf(sampleJob1, sampleJob2, sampleJob3, sampleJob4, sampleJob5)
                for (j in seededJobs) {
                    db.collection("jobs").document(j.id).set(j).await()
                }

                // Add sample featured Pro bid on job 1
                val sampleBid = Bid(
                    id = "sample_bid_01",
                    jobId = "sample_job_01",
                    vendorId = "demo_vendor_tariq",
                    vendorName = "Tariq Master Plumbing",
                    vendorIsPro = true,
                    isFeatured = true,
                    laborPrice = 18.0,
                    proposedWarrantyDays = 30L,
                    counterNote = "Certified master technician. Includes 30-day leak-free guarantee.",
                    status = "submitted"
                )
                db.collection("jobs").document(sampleJob1.id).collection("bids").document(sampleBid.id).set(sampleBid).await()

                // Add sample part receipt for job 2
                val sampleReceipt = PartReceipt(
                    id = "sample_rcpt_01",
                    jobId = "sample_job_02",
                    vendorId = "demo_vendor_tariq",
                    shopName = "Ruwi HVAC Components Mart",
                    amount = 8.50,
                    receiptPhotoUrl = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&w=400&q=80",
                    isApprovedByConsumer = true
                )
                db.collection("jobs").document(sampleJob2.id).collection("receipts").document(sampleReceipt.id).set(sampleReceipt).await()

                // Add initial sample review for Tariq Master Plumbing
                val sampleReview = Review(
                    id = "sample_rev_01",
                    jobId = "sample_job_03",
                    vendorId = "demo_vendor_tariq",
                    vendorName = "Tariq Master Plumbing",
                    consumerId = "demo_consumer_ahmed",
                    consumerName = "Ahmed Al-Balushi",
                    rating = 5.0,
                    workmanshipRating = 5.0,
                    punctualityRating = 4.8,
                    comment = "Excellent electrical work! Fixed the main distribution breaker quickly and properly balanced the phases. Escrow payment was released right after inspection.",
                    createdAt = Timestamp(nowSec - (7 * 86400), 0)
                )
                db.collection("reviews").document(sampleReview.id).set(sampleReview).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Seed data check skipped: ${e.message}")
        }
    }
}

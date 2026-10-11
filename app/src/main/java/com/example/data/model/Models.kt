package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FixItUser(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val role: String = "consumer", // consumer, vendor, admin
    val plan: String = "free", // free, plus, pro
    val walletBalance: Double = 50.0,
    val lockedEscrowBalance: Double = 0.0,
    val bidTokens: Long = 5L,
    val isVerifiedSeller: Boolean = false,
    val isAvailableNow: Boolean = false,
    val skills: List<String> = emptyList(),
    val bio: String = "",
    val phoneMasked: String = "••••-•••-1234",
    val phoneNumber: String = "",
    val isPhoneLinked: Boolean = false,
    val isGoogleLinked: Boolean = false,
    val isEmailLinked: Boolean = false,
    val passwordHash: String = "",
    val rating: Double = 5.0,
    val reviewsCount: Long = 0L,
    val strikesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ServiceItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val isApproved: Boolean = true,
    val requestedBy: String = "",
    val suggestedPriceFloor: Double = 10.0,
    val suggestedPriceCeiling: Double = 30.0,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Job(
    @DocumentId val id: String = "",
    val consumerId: String = "",
    val consumerName: String = "",
    val title: String = "",
    val description: String = "",
    val serviceCategory: String = "",
    val locationName: String = "Ruwi, Muscat",
    val biddingMode: String = "blind_bidding", // blind_bidding, bounty_bargain
    val bountyPrice: Double? = null,
    val aiFloorPrice: Double = 15.0,
    val budgetMin: Double = 15.0,
    val budgetMax: Double = 35.0,
    val budgetRange: String = "15 - 35 OMR",
    val paymentStatus: String = "Escrow Protected",
    val escrowAmount: Double = 25.0,
    val isEscrowProtected: Boolean = true,
    val partsMode: String = "no_parts", // multi_receipt, escort_mode, no_parts
    val status: String = "open_for_bids", // awaiting_admin_verification, open_for_bids, price_locked, in_progress, on_the_way, arrived, verification_pending, warranty_active, completed, disputed, cancelled
    val selectedBidId: String? = null,
    val selectedVendorId: String? = null,
    val selectedVendorName: String? = null,
    val agreedLaborFee: Double = 0.0,
    val agreedWarrantyDays: Long = 15L,
    val warrantyExpiry: Timestamp? = null,
    val beforePhotoUrl: String? = null,
    val vendorAfterPhotoUrl: String? = null,
    val consumerAfterPhotoUrl: String? = null,
    val isPaymentVerified: Boolean = true,
    val paymentReceiptUrl: String? = null,
    val trackingActive: Boolean = false,
    val latitude: Double = 23.5880,
    val longitude: Double = 58.3829,
    val aiRequirements: List<String> = emptyList(),
    val vendorLatitude: Double? = null,
    val vendorLongitude: Double? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Bid(
    @DocumentId val id: String = "",
    val jobId: String = "",
    val vendorId: String = "",
    val vendorName: String = "",
    val vendorIsPro: Boolean = false,
    val isFeatured: Boolean = false,
    val laborPrice: Double = 0.0,
    val proposedWarrantyDays: Long = 15L,
    val counterNote: String? = null,
    val status: String = "submitted", // submitted, countered, accepted, rejected
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PartReceipt(
    @DocumentId val id: String = "",
    val jobId: String = "",
    val vendorId: String = "",
    val shopName: String = "",
    val amount: Double = 0.0,
    val receiptPhotoUrl: String = "",
    val isApprovedByConsumer: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class JobMessage(
    @DocumentId val id: String = "",
    val jobId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String? = null,
    val isVoiceNote: Boolean = false,
    val voiceDurationSeconds: Long? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Review(
    @DocumentId val id: String = "",
    val jobId: String = "",
    val vendorId: String = "",
    val vendorName: String = "",
    val consumerId: String = "",
    val consumerName: String = "",
    val rating: Double = 5.0,
    val workmanshipRating: Double = 5.0,
    val punctualityRating: Double = 5.0,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val amount: Double = 0.0,
    val isCredit: Boolean = true,
    val transactionType: String = "topup", // topup, escrow_lock, escrow_release, fee, payout
    val referenceJobId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

sealed class EscrowStatus {
    object Unfunded : EscrowStatus()
    data class HeldInEscrow(val amount: Double, val jobId: String) : EscrowStatus()
    data class ConsumerConfirmed(val amount: Double, val jobId: String, val waitingForVendor: Boolean = true) : EscrowStatus()
    data class VendorConfirmed(val amount: Double, val jobId: String, val waitingForConsumer: Boolean = true) : EscrowStatus()
    data class Released(val amount: Double, val releasedAt: Long) : EscrowStatus()
    data class Disputed(val reason: String) : EscrowStatus()
}


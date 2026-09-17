package com.upiapp.upipay.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import java.time.LocalDateTime
import java.math.BigDecimal

@JsonIgnoreProperties(value = ["hibernateLazyInitializer", "handler"])
@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val userId: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var phone: String,

    @Column(name = "upi_id", unique = true)
    var upiId: String? = null,

    // Email (Edit Profile)
    @Column(name = "email")
    var email: String? = null,

    @Column(
        name = "wallet_balance",
        nullable = false,
        precision = 12,
        scale = 2
    )
    var balance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "is_blocked", nullable = false)
    var blocked: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // ---------------------------
    // BANK DETAILS
    // ---------------------------

    @Column(name = "bank_name")
    var bankName: String? = null,

    @Column(name = "account_number")
    var accountNumber: String? = null,

    @Column(name = "ifsc_code")
    var ifscCode: String? = null,

    @Column(name = "account_holder_name")
    var accountHolderName: String? = null,

    @Column(name = "bank_linked")
    var bankLinked: Boolean = false,

    // ---------------------------
    // KYC DETAILS
    // ---------------------------

    @Column(name = "aadhaar_number")
    var aadhaarNumber: String? = null,

    @Column(name = "pan_number")
    var panNumber: String? = null,

    @Column(name = "kyc_verified")
    var kycVerified: Boolean = false,

    // 🔥🔥🔥 FINAL FIX (ADD THIS FIELD)
    @Column(name = "token", columnDefinition = "TEXT")
    var token: String? = null,

    @Column(name = "upi_pin")
    var upiPin: String? = null

) {

    /**
     * Automatically generate UPI ID before saving
     */
    @PrePersist
    fun generateUpiId() {
        if (upiId.isNullOrBlank()) {
            val normalizedPhone = phone.replace("+", "")
            val phoneWithoutCountryCode = if (normalizedPhone.startsWith("91")) {
                normalizedPhone.substring(2)
            } else {
                normalizedPhone
            }

            upiId = "$phoneWithoutCountryCode@ybl"
        }
    }
}
package com.upiapp.upipay.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.math.BigDecimal

@Entity
@Table(name = "merchants")
data class Merchant(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val merchantId: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var phone: String,

    @Column(name = "upi_id", nullable = false, unique = true)
    var upiId: String,

    // ---------------- BUSINESS DETAILS ----------------
    @Column(name = "business_name")
    var businessName: String? = null,

    @Column(name = "business_category")
    var businessCategory: String? = null,

    @Column(name = "business_address")
    var businessAddress: String? = null,

    @Column(name = "email")
    var email: String? = null,

    // ---------------- BANK DETAILS ----------------
    @Column(name = "bank_name")
    var bankName: String? = null,

    @Column(name = "account_number")
    var accountNumber: String? = null,

    @Column(name = "ifsc_code")
    var ifscCode: String? = null,

    @Column(name = "account_holder_name")
    var accountHolderName: String? = null,

    @Column(name = "settlement_account")
    var settlementAccount: Boolean = false,

    // 🔥 FIXED (NON-NULLABLE + COMMA ADDED)
    @Column(name = "bank_linked", nullable = false)
    var bankLinked: Boolean = false,

    // ---------------- KYC DETAILS ----------------
    @Column(name = "aadhaar_number")
    var aadhaarNumber: String? = null,

    @Column(name = "pan_number")
    var panNumber: String? = null,

    @Column(name = "kyc_status")
    var kycStatus: String = "pending",

    // ---------------- REVENUE ----------------
    @Column(nullable = false, precision = 14, scale = 2)
    var revenue: BigDecimal = BigDecimal.ZERO,

    @Column(name = "is_blocked")
    var blocked: Boolean = false,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
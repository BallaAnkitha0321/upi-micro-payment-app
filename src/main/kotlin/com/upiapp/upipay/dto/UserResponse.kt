package com.upiapp.upipay.dto

import java.math.BigDecimal

data class UserResponse(

    val userId: Long,

    val name: String?,        // ✅ make nullable (safe)

    val phone: String?,       // ✅ safe

    val upiId: String?,

    val email: String?,

    val bankName: String?,

    val accountNumber: String?,

    val ifscCode: String?,

    val balance: BigDecimal = BigDecimal.ZERO, // ✅ default safe

    val bankLinked: Boolean = false,           // ✅ default safe

    val kycVerified: Boolean = false,          // ✅ CRITICAL FIELD

    val isBlocked: Boolean = false,

    val token: String? = null                  // ✅ optional
)
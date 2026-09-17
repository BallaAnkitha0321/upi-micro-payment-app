package com.upiapp.upipay.dto

data class MerchantProfileResponse(
    val merchantId: Long,
    val name: String?,
    val email: String?,
    val upiId: String?,
    val businessName: String?,
    val businessCategory: String?,
    val businessAddress: String?,
    val bankLinked: Boolean,
    val kycStatus: String?,
    val isBlocked: Boolean,
    val bankName: String?,
    val accountNumber: String?,
    val ifscCode: String?,
)
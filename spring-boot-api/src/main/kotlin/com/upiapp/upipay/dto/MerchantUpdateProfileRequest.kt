package com.upiapp.upipay.dto

data class MerchantUpdateProfileRequest(
    val name: String,
    val email: String?,
    val businessName: String?,
    val businessCategory: String?,
    val businessAddress: String?
)
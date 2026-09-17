package com.upimicro.data.model

data class MerchantRegisterRequest(
    val name: String,
    val businessName: String?,
    val businessCategory: String?,
    val phone: String,
    val upiId: String
)

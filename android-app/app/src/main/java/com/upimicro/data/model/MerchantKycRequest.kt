package com.upimicro.data.model

data class MerchantKycRequest(
    val merchantId: Long,
    val aadhaarNumber: String,
    val panNumber: String
)
package com.upiapp.upipay.dto

data class MerchantRegisterRequest(
    val name: String,
    val phone: String,
    val businessName: String? = null,
    val businessCategory: String? = null
)
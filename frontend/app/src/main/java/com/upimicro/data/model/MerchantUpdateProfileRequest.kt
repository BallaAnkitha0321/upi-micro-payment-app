package com.upimicro.data.model

data class MerchantUpdateProfileRequest(

    val merchantId: Long,

    val name: String,

    val email: String,

    val businessName: String,

    val businessCategory: String,

    val businessAddress: String
)
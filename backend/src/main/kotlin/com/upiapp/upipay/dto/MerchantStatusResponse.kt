package com.upiapp.upipay.dto

data class MerchantStatusResponse(

    val merchantId: Long,

    val bankLinked: Boolean,

    val settlementAccount: Boolean,

    val kycStatus: String,

    val isBlocked: Boolean,

    val message: String
)   
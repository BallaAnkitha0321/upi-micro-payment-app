package com.upiapp.upipay.model

data class UserStatusResponse(
    val bankLinked: Boolean,
    val kycVerified: Boolean,
    val hasUpiPin: Boolean
)
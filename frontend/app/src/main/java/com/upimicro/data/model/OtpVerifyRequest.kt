package com.upimicro.data.model

data class OtpVerifyRequest(
    val phone: String,
    val otp: String
)
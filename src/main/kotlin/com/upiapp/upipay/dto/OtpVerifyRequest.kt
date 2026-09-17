package com.upiapp.upipay.dto

data class OtpVerifyRequest(
    val phone: String,
    val otp: String
)
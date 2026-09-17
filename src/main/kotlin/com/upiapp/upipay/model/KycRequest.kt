package com.upiapp.upipay.model

data class KycRequest(
    val userId: Long,
    val aadhaarNumber: String,
    val panNumber: String?
)
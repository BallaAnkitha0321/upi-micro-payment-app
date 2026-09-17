package com.upiapp.upipay.dto

data class MerchantKycRequest(
    val aadhaarNumber: String,
    val panNumber: String
)
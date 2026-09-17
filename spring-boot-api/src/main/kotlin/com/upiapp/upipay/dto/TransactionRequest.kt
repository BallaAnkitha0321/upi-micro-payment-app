package com.upiapp.upipay.dto

data class TransactionRequest(
    val userId: Long,
    val merchantUpiId: String,
    val amount: Double,
    val status: String,
    val pin: String
    
)
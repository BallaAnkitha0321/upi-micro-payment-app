package com.upimicro.data.model

data class TransactionRequest(
    val userId: Long,
    val merchantUpiId: String,
    val amount: Double,
    val status: String,          // 🔥 ADD THIS
    val txnRef: String,
    val rawResponse: String?,
    val responseCode: String?,
    val pin: String
)
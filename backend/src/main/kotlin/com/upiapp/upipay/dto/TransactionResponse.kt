package com.upiapp.upipay.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionResponse(

    val txnId: Long,

    val amount: BigDecimal,

    val status: String,

    val txnRef: String?,

    val createdAt: LocalDateTime,

    val fraudFlag: Boolean,

    val fraudReason: String?,

    val riskScore: Int,
    val senderName: String?,

    // 🔥 UI SUPPORT
    val receiverName: String?,

    val isQr: Boolean
)
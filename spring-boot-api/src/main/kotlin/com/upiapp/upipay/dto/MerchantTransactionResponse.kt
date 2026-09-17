package com.upiapp.upipay.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class MerchantTransactionResponse(
    val txnId: Long,
    val amount: BigDecimal,
    val status: String,
    val txnRef: String?,
    val createdAt: LocalDateTime,
    val fraudFlag: Boolean?,
    val riskScore: Int?
)
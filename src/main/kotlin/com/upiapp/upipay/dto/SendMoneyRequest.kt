package com.upiapp.upipay.dto

import java.math.BigDecimal

data class SendMoneyRequest(
    val senderPhone: String,
    val receiverUpiId: String,
    val amount: BigDecimal,
    val pin: String
)
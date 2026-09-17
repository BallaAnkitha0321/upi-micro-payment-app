package com.upiapp.upipay.dto

import java.math.BigDecimal

data class MerchantDashboardResponse(
    val totalEarnings: BigDecimal,
    val totalTransactions: Long,
    val todayEarnings: BigDecimal
)
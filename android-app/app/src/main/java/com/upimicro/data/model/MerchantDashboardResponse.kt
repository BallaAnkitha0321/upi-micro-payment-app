package com.upimicro.data.model

data class MerchantDashboardResponse(
    val businessName: String?,
    val totalEarnings: Double?,
    val totalTransactions: Long?,
    val todayEarnings: Double?
)
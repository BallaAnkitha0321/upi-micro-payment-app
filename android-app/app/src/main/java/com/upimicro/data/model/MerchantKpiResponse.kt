package com.upimicro.data.model

data class MerchantKpiResponse(
    val totalMerchants: Int,
    val activeMerchants: Int,
    val conversionRate: Double,
    val insight: String?
)

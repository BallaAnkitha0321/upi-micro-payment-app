package com.upiapp.upipay.dto

data class MerchantKpiResponse(
    val totalMerchants: Long,
    val activeMerchants: Long,
    val totalRevenue: Double,
    val topMerchantName: String,
    val topMerchantAmount: Double
)
package com.upiapp.upipay.dto

data class UserInsightsResponse(
    val totalUsers: Long,
    val activeUsers: Long,
    val conversionRate: Double
)
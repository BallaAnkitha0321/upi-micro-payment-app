package com.upimicro.data.model

data class UserInsightsResponse(
    val totalUsers: Long,
    val activeUsers: Long,
    val conversionRate: Double
)
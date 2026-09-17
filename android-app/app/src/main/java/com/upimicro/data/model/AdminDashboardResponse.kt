package com.upimicro.data.model

data class AdminDashboardResponse(

    val totalUsers: Int,

    val totalMerchants: Int,

    val totalTransactions: Int,

    val totalRevenue: Double,

    val failedTransactions: Int
)
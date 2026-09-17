package com.upiapp.upipay.dto

data class AdminDashboardResponse(

    val totalUsers: Long,

    val totalMerchants: Long,

    val totalTransactions: Long,

    val totalRevenue: Double,

    val failedTransactions: Long
)
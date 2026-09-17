package com.upiapp.upipay.dto

data class TransactionKpiResponse(

    val totalTransactions: Long,

    val successTransactions: Long,

    val failedTransactions: Long,

    val successRate: Double,

    // 🔥 better naming
    val insightMessage: String
)
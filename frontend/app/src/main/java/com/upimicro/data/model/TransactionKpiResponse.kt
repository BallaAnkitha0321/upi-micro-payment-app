package com.upimicro.data.model

data class TransactionKpiResponse(
    val totalTransactions: Long,
    val successTransactions: Long,
    val failedTransactions: Long,
    val successRate: Double,
    val insight: String
)
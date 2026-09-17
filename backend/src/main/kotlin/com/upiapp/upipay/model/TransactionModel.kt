package com.upiapp.upipay.model

data class TransactionModel(
    val txnId: Long,
    val amount: Double,
    val status: String,
    val txnRef: String,
    val createdAt: String,
    val fraudFlag: Boolean?,
    val riskScore: Int?
)
package com.upimicro.data.model

import com.google.gson.annotations.SerializedName

data class FraudTransaction(
    @SerializedName("txnId") val id: Long, // Supporting both common naming conventions
    val amount: Double,
    val riskScore: Int,
    val fraudReason: String?,
    val createdAt: String,
    val status: String?,
    val userId: Long?
)

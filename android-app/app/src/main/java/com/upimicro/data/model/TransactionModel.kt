package com.upimicro.data.model

import com.google.gson.annotations.SerializedName

data class TransactionModel(
    @SerializedName("txnId")
    val txnId: Long,
    
    @SerializedName("merchant")
    val merchant: MerchantModel?,
    
    @SerializedName("receiverName", alternate = ["receiver_name"])
    val receiverName: String?,
    
    @SerializedName("senderName", alternate = ["sender_name"])
    val senderName: String?,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("createdAt", alternate = ["created_at"])
    val createdAt: String,
    
    @SerializedName("txnRef", alternate = ["txn_ref"])
    val txnRef: String,
    
    @SerializedName("fraudFlag", alternate = ["fraud_flag"])
    val fraudFlag: Boolean? = false,
    
    @SerializedName("fraudReason", alternate = ["fraud_reason"])
    val fraudReason: String? = null,
    
    @SerializedName("riskScore", alternate = ["risk_score"])
    val riskScore: Int? = 0
)
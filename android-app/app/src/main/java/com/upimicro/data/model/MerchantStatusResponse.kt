package com.upimicro.data.model

import com.google.gson.annotations.SerializedName

data class MerchantStatusResponse(
    @SerializedName("merchantId")
    val merchantId: Long?,

    @SerializedName("bankLinked")
    val bankLinked: Boolean,

    @SerializedName("settlementAccount")
    val settlementAccount: String?,

    @SerializedName("kycStatus")
    val kycStatus: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("businessName")
    val businessName: String? = null,

    @SerializedName("todayEarnings")
    val todayEarnings: String? = null,

    @SerializedName("totalEarnings")
    val totalEarnings: String? = null,

    @SerializedName("totalTransactions")
    val totalTransactions: Int? = null
)

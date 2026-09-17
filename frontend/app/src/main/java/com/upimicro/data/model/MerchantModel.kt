package com.upimicro.data.model

import com.google.gson.annotations.SerializedName

data class MerchantModel(
    @SerializedName("merchantId")
    val merchantId: Long,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("upiId")
    val upiId: String?,
    
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("totalEarnings", alternate = ["amount", "revenue", "totalRevenue", "earnings", "total_earnings", "totalVolume"])
    val totalEarnings: Double? = 0.0
)
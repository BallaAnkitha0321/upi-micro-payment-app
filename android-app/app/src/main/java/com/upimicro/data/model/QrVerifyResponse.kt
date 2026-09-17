package com.upimicro.data.model

import com.google.gson.annotations.SerializedName

data class QrVerifyResponse(
    @SerializedName("valid")
    val valid: Boolean,
    
    @SerializedName("merchantName")
    val merchantName: String?,
    
    @SerializedName("merchantUpiId")
    val merchantUpiId: String?,
    
    @SerializedName("merchantId")
    val merchantId: Long? = null
)

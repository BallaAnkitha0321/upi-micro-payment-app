package com.upimicro.data.model

import com.google.gson.annotations.SerializedName

data class MerchantProfileResponse(

    @SerializedName("merchantId")
    val merchantId: Long? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("upiId")
    val upiId: String? = null,

    @SerializedName("bankLinked")
    val bankLinked: Boolean? = false,

    @SerializedName("accountNumber")
    val accountNumber: String? = null,

    @SerializedName("bankName")
    val bankName: String? = null,

    @SerializedName("ifscCode")
    val ifscCode: String? = null,

    @SerializedName("businessName")
    val businessName: String? = null,

    @SerializedName("businessCategory")
    val businessCategory: String? = null,

    @SerializedName("businessAddress")
    val businessAddress: String? = null,

    // 🔥 CRITICAL FIELD (DO NOT CHANGE NAME)
    @SerializedName("kycStatus")
    val kycStatus: String? = null,

    @SerializedName("blocked")
    val blocked: Boolean = false
)
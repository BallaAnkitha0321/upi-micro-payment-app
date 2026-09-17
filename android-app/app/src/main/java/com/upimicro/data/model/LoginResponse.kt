package com.upimicro.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("userId")
    val userId: Long? = null,

    @SerializedName("merchantId")
    val merchantId: Long? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("upiId")
    val upiId: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("bankName")
    val bankName: String? = null,

    @SerializedName("accountNumber")
    val accountNumber: String? = null,

    @SerializedName("ifscCode")
    val ifscCode: String? = null,

    @SerializedName("balance")
    val balance: Double? = null,

    // 🔥 FIX: MAKE OPTIONAL (VERY IMPORTANT)
    @SerializedName("bankLinked")
    val bankLinked: Boolean? = false,

    @SerializedName("kycVerified")
    val kycVerified: Boolean? = false,

    @SerializedName("upiPinSet")
    val upiPinSet: Boolean? = false,

    @SerializedName("token")
    val token: String? = null
)
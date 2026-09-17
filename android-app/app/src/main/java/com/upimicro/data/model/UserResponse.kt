package com.upimicro.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("name")
    val name: String? = null,   // ✅ safe

    @SerializedName("phone")
    val phone: String? = null,  // ✅ safe

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

    @SerializedName("bankLinked")
    val bankLinked: Boolean = false,

    // 🔥 MOST IMPORTANT FIELD
    @SerializedName("kycVerified")
    val kycVerified: Boolean = false,  // ✅ nullable fix

    @SerializedName("balance")
    val balance: Double = 0.0,

    @SerializedName("upiPinSet")
    val upiPinSet: Boolean = false,

    @SerializedName("blocked")
    val blocked: Boolean = false,

    @SerializedName("token")
    val token: String? = null
)
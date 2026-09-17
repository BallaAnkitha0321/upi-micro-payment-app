package com.upimicro.data.model

import com.google.gson.annotations.SerializedName

data class UpiVerifyResponse(

    @SerializedName("isValid")
    val isValid: Boolean,

    @SerializedName("name")
    val name: String?,

    @SerializedName("upiId")
    val upiId: String?,

    @SerializedName("type")
    val type: String?
)
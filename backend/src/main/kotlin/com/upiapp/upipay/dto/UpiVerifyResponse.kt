package com.upiapp.upipay.dto

data class UpiVerifyResponse(

    val isValid: Boolean,

    val name: String?,

    val upiId: String,

    val type: String
)
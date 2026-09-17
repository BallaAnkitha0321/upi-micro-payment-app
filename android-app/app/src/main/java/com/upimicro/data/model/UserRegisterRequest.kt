package com.upimicro.data.model

data class UserRegisterRequest(
    val name: String,
    val phone: String,
    val bankName: String,
    val accountNumber: String,
    val ifscCode: String,
    val accountHolderName: String
)
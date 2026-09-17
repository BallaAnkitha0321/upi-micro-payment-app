package com.upimicro.data.model

data class MerchantBankRequest(
    val bankName: String,
    val accountNumber: String,
    val ifscCode: String,
    val accountHolderName: String
)
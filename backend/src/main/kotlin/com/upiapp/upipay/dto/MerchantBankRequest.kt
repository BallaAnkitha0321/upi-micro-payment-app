package com.upiapp.upipay.dto

data class MerchantBankRequest(

    val bankName: String,

    val accountNumber: String,

    val ifscCode: String,

    val accountHolderName: String
)
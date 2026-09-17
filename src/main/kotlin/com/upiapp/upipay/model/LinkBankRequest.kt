package com.upiapp.upipay.model

data class LinkBankRequest(
    val userId: Long,
    val bankName: String,
    val accountNumber: String,
    val ifscCode: String,
    val accountHolderName: String? = null
)
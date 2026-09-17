package com.upimicro.data.model

data class MerchantTransaction(
    val txnId: Long,
    val user: UserInfo,
    val merchant: MerchantInfo,
    val amount: Double,
    val status: String,
    val txnRef: String?,
    val rawResponse: String?,
    val responseCode: String?,
    val createdAt: String
)

data class UserInfo(
    val userId: Long,
    val name: String,
    val phone: String
)

data class MerchantInfo(
    val merchantId: Long,
    val name: String,
    val upiId: String
)
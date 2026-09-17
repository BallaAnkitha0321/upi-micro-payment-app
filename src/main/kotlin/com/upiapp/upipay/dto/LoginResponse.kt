package com.upiapp.upipay.dto

data class LoginResponse(
    val role: String,
    val userId: Long? = null,
    val merchantId: Long? = null,
    val message: String? = null,
    val name: String? = null,
    val email: String? = null,
    val upiId: String? = null,
    val token: String? = null
)
package com.upiapp.upipay.dto

data class UserRegisterRequest(
    val name: String,
    val phone: String,
    val email: String?,
)
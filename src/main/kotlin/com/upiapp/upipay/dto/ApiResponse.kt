package com.upiapp.upipay.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)
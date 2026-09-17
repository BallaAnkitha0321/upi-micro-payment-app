package com.upiapp.upipay.exception

import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val message: String
)
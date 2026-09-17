package com.upimicro.data.model

data class UserStatusResponse(
    val userId: Long,
    val blocked: Boolean,
    val hasUpiPin: Boolean = false
)

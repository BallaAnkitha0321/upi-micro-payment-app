package com.upimicro.data.model

data class RoleCheckResponse(
    val userExists: Boolean,
    val merchantExists: Boolean,
    val userId: Long? = null,
    val merchantId: Long? = null
)
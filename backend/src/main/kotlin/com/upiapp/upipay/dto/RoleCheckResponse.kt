package com.upiapp.upipay.dto

data class RoleCheckResponse(
    val userExists: Boolean,
    val merchantExists: Boolean
)
package com.upimicro.data.model

data class KycRequest(

    val userId: Long,

    val aadhaarNumber: String,

    val panNumber: String?

)
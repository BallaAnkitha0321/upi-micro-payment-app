package com.upiapp.upipay.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "otp")
data class Otp(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val phone: String,

    val otpCode: String,

    val createdAt: LocalDateTime
)
package com.upiapp.upipay.notification

import jakarta.persistence.*

@Entity
@Table(name = "device_tokens")
data class DeviceToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Long,

    @Column(length = 500)
    val token: String
)
package com.upiapp.upipay.entity

import jakarta.persistence.*

@Entity
@Table(name = "admins")
data class Admin(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(name = "is_blocked")
    var blocked: Boolean? = false
)
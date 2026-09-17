package com.upiapp.upipay.repository

import com.upiapp.upipay.entity.Otp
import org.springframework.data.jpa.repository.JpaRepository

interface OtpRepository : JpaRepository<Otp, Long> {

    fun findTopByPhoneOrderByCreatedAtDesc(phone: String): Otp?

}
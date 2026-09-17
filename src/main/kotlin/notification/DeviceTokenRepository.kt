package com.upiapp.upipay.notification

import org.springframework.data.jpa.repository.JpaRepository

interface DeviceTokenRepository : JpaRepository<DeviceToken, Long> {

    fun findByUserId(userId: Long): List<DeviceToken>

}
package com.upiapp.upipay.notification

import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val deviceTokenRepository: DeviceTokenRepository
) {

    fun saveDeviceToken(userId: Long, token: String) {

        val deviceToken = DeviceToken(
            userId = userId,
            token = token
        )

        deviceTokenRepository.save(deviceToken)
    }

    fun getUserTokens(userId: Long): List<String> {

        return deviceTokenRepository
            .findByUserId(userId)
            .map { it.token }

    }
}
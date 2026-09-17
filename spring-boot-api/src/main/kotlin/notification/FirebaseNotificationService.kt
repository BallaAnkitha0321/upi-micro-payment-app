package com.upiapp.upipay.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FirebaseNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository
) {

    private val logger = LoggerFactory.getLogger(FirebaseNotificationService::class.java)

    fun sendNotificationToUser(
        userId: Long,
        title: String,
        body: String,
        screen: String
    ) {

        val devices = deviceTokenRepository.findByUserId(userId)

        if (devices.isEmpty()) {
            logger.warn("No device tokens found for userId={}", userId)
            return
        }

        devices.forEach { device ->

            try {

                val message = Message.builder()
                    .setToken(device.token)
                    .putData("title", title)
                    .putData("body", body)
                    .putData("screen", screen)
                    .putData("userId", userId.toString())
                    .build()

                val response = FirebaseMessaging.getInstance().send(message)

                logger.info(
                    "Notification sent successfully to userId={} token={} response={}",
                    userId,
                    device.token,
                    response
                )

            } catch (ex: Exception) {

                logger.error(
                    "Notification failed for userId={} token={}",
                    userId,
                    device.token,
                    ex
                )
            }
        }
    }
}
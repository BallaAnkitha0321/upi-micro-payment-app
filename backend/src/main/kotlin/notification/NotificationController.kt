package com.upiapp.upipay.notification

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val firebaseNotificationService: FirebaseNotificationService
) {

    @PostMapping("/register-device")
    fun registerDevice(
        @RequestParam userId: Long,
        @RequestParam token: String
    ): String {

        notificationService.saveDeviceToken(userId, token)

        return "Device registered successfully"
    }

    @PostMapping("/send-test")
    fun sendTestNotification(
        @RequestParam userId: Long
    ): String {

        firebaseNotificationService.sendNotificationToUser(
            userId,
            "UPI Payment",
            "Your payment was successful",
            "PAYMENT_SUCCESS"
        )

        return "Notification sent"
    }
}
package com.upiapp.upipay.notification

import org.springframework.stereotype.Service

@Service
class NotificationDispatcher(
    private val firebaseNotificationService: FirebaseNotificationService
) {

    fun dispatch(event: NotificationEvent) {

        firebaseNotificationService.sendNotificationToUser(
            event.userId,
            event.title,
            event.message,
            event.type.name
        )
    }
}
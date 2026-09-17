package com.upiapp.upipay.notification

data class NotificationEvent(

    val userId: Long,

    val type: NotificationType,

    val title: String,

    val message: String

)
package com.upimicro.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.upimicro.R
import com.upimicro.data.model.RegisterDeviceRequest
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.admin.FraudAlertsActivity
import com.upimicro.ui.merchant.MerchantDashboardActivity
import com.upimicro.ui.splash.SplashActivity
import com.upimicro.ui.user.UserDashboardActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM_TOKEN", "New token: $token")

        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        if (userId == -1L) {
            Log.d("FCM_REGISTER", "User not logged in")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitClient.getApi(applicationContext)

                val request = RegisterDeviceRequest(
                    userId = userId,
                    token = token
                )

                val response = api.registerDevice(request)

                if (response.isSuccessful) {
                    Log.d("FCM_REGISTER", "Device registered")
                } else {
                    Log.e("FCM_REGISTER", "Failed: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("FCM_REGISTER", "Error", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: "UPI Payment"
        val body = message.data["body"] ?: "Transaction update"
        val screen = message.data["screen"]

        val intent = when (screen) {
            "PAYMENT_SUCCESS" -> Intent(this, UserDashboardActivity::class.java)
            "PAYMENT_RECEIVED" -> Intent(this, MerchantDashboardActivity::class.java)
            "FRAUD_ALERT" -> Intent(this, FraudAlertsActivity::class.java)
            else -> Intent(this, SplashActivity::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        showNotification(title, body, pendingIntent)
    }

    private fun showNotification(
        title: String,
        body: String,
        pendingIntent: PendingIntent
    ) {

        val channelId = "upi_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "UPI Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("FCM_PERMISSION", "Permission not granted")
            return
        }

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
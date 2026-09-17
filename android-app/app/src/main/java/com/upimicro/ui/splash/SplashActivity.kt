package com.upimicro.ui.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.upimicro.R
import com.upimicro.data.model.RegisterDeviceRequest
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.admin.AdminDashboardActivity
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.ui.merchant.MerchantDashboardActivity
import com.upimicro.ui.role.RoleSelectionActivity
import com.upimicro.ui.user.UserDashboardActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)

        requestNotificationPermission()
        generateFcmToken()

        Handler(Looper.getMainLooper()).postDelayed({

            val token = sessionManager.getToken()
            val role = sessionManager.getRole()

            Log.d("SPLASH_DEBUG", "token=$token role=$role")

            if (token.isNotEmpty()) {

                when (role) {

                    "USER" -> navigate(UserDashboardActivity::class.java)

                    "MERCHANT" -> navigate(MerchantDashboardActivity::class.java)

                    "ADMIN" -> navigate(AdminDashboardActivity::class.java)

                    "ROLE_SELECTION" -> navigate(RoleSelectionActivity::class.java)

                    else -> {
                        // 🔥 FINAL FIX
                        // If role is empty / invalid / NEW → go to login safely
                        Log.e("SPLASH", "Invalid role → redirecting to login")
                        sessionManager.logout()
                        navigate(LoginActivity::class.java)
                    }
                }

            } else {
                navigate(LoginActivity::class.java)
            }

        }, 1500)
    }

    private fun navigate(destination: Class<*>) {
        val intent = Intent(this, destination)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun generateFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM_TOKEN", "Fetching token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("FCM_TOKEN", "Device Token: $token")
                getSharedPreferences("fcm", MODE_PRIVATE)
                    .edit()
                    .putString("token", token)
                    .apply()
            }
    }

    private fun registerTokenToBackend() {

        val fcmToken = getSharedPreferences("fcm", MODE_PRIVATE)
            .getString("token", null)

        val userId = sessionManager.getUserId()

        if (fcmToken == null || userId == -1L) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.getApi(this@SplashActivity)
                val request = RegisterDeviceRequest(
                    userId = userId,
                    token = fcmToken
                )
                apiService.registerDevice(request)
                Log.d("FCM_REGISTER", "Device registered")
            } catch (e: Exception) {
                Log.e("FCM_REGISTER", "Failed", e)
            }
        }
    }
}
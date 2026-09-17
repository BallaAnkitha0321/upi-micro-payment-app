package com.upimicro.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.MerchantLoginRequest
import com.upimicro.databinding.ActivityMerchantLoginBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.merchant.MerchantDashboardActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class MerchantLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.loginButton.setOnClickListener {
            loginMerchant()
        }
    }

    private fun loginMerchant() {

        val name = binding.nameEditText.text.toString().trim()
        val phone = sessionManager.getPhone()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter merchant name", Toast.LENGTH_LONG).show()
            return
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {

            try {

                val request = MerchantLoginRequest(
                    name = name,
                    phone = phone
                )

                val response =
                    RetrofitClient.getApi(this@MerchantLoginActivity)
                        .merchantLogin(request)

                if (response.isSuccessful && response.body() != null) {

                    val body = response.body()!!

                    val token = body.token ?: ""
                    val role = body.role ?: "MERCHANT"

                    // ✅ SAVE AUTH SESSION
                    sessionManager.saveFullSession(token, role, phone)

                    // ✅ SAVE MERCHANT DATA (🔥 FIXED)
                    sessionManager.saveMerchantSession(
                        merchantId = body.merchantId ?: -1L,
                        name = body.name ?: "",
                        email = body.email ?: "",     // ✅ IMPORTANT FIX
                        upiId = body.upiId ?: ""      // ✅ IMPORTANT FIX
                    )

                    Log.d("SESSION_DEBUG", "Merchant login success")

                    startActivity(
                        Intent(
                            this@MerchantLoginActivity,
                            MerchantDashboardActivity::class.java
                        )
                    )

                    finish()

                } else {

                    Toast.makeText(
                        this@MerchantLoginActivity,
                        "Merchant login failed",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@MerchantLoginActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                Log.e("LOGIN_ERROR", "Error", e)
            }
        }
    }
}
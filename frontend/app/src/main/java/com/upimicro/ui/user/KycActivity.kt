package com.upimicro.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.KycRequest
import com.upimicro.databinding.ActivityKycBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class KycActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKycBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKycBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        binding.submitKyc.setOnClickListener {

            val aadhaar = binding.aadhaar.text.toString().trim()
            val pan = binding.pan.text.toString().trim()

            if (aadhaar.length != 12) {
                binding.aadhaar.error = "Aadhaar must be 12 digits"
                return@setOnClickListener
            }

            if (pan.isNotEmpty() && !pan.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]{1}"))) {
                binding.pan.error = "Invalid PAN format (ABCDE1234F)"
                return@setOnClickListener
            }

            submitKyc(aadhaar, pan)
        }
    }

    private fun submitKyc(aadhaar: String, pan: String) {

        val userId = sessionManager.getUserId()
        val token = sessionManager.getToken()
        
        Log.d("AUTH_DEBUG", "Token: $token")

        if (token.isNullOrEmpty() || userId <= 0) {
            Toast.makeText(
                this,
                "Session expired, please login again",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        binding.submitKyc.isEnabled = false
        binding.submitKyc.text = "Submitting..."

        lifecycleScope.launch {
            try {
                // ✅ FIXED: Removed authHeader. RetrofitClient already adds Bearer token via interceptor.
                val response = RetrofitClient.getApi(this@KycActivity).updateKyc(
                    KycRequest(
                        userId = userId,
                        aadhaarNumber = aadhaar,
                        panNumber = pan.ifEmpty { null }
                    )
                )

                if (response.isSuccessful) {
                    sessionManager.saveKycVerified(true)

                    Toast.makeText(
                        this@KycActivity,
                        "KYC Submitted Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()

                } else if (response.code() == 401) {
                    Log.e("KYC_ERROR", "Unauthorized: ${response.code()} Body: ${response.errorBody()?.string()}")
                    
                    Toast.makeText(
                        this@KycActivity,
                        "Session expired, please login again",
                        Toast.LENGTH_LONG
                    ).show()

                    sessionManager.logout()
                    val intent = Intent(this@KycActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("KYC_ERROR", "Failed: ${response.code()} Body: ${response.errorBody()?.string()}")
                    Toast.makeText(
                        this@KycActivity,
                        "KYC Failed: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("KYC_EXCEPTION", "Error: ${e.message}", e)
                Toast.makeText(
                    this@KycActivity,
                    "Connection Error",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.submitKyc.isEnabled = true
                binding.submitKyc.text = "SUBMIT KYC"
            }
        }
    }
}

package com.upimicro.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.*
import com.upimicro.databinding.ActivityLoginBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.admin.AdminLoginActivity
import com.upimicro.ui.merchant.MerchantDashboardActivity
import com.upimicro.ui.role.RoleSelectionActivity
import com.upimicro.ui.user.UserDashboardActivity
import com.upimicro.utils.SessionManager
import com.upimicro.utils.DialogUtils
import com.upimicro.utils.PhoneUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    private var isOtpSent = false
    private var resendTimer: CountDownTimer? = null
    private var canResend = false
    private var mobileNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupPhoneInput()
        setupOtpInputs()

        // Admin Access - Long press on App Title
        binding.appTitle.setOnLongClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
            true
        }

        binding.sendOtpButton.setOnClickListener {
            if (!isOtpSent) {
                sendOtp()
            } else {
                verifyOtp()
            }
        }

        binding.verifyOtpButton.setOnClickListener {
            verifyOtp()
        }

        binding.resendOtpText.setOnClickListener {
            if (canResend) {
                sendOtp()
            }
        }

        binding.changeNumberText.setOnClickListener {
            isOtpSent = false
            binding.mobileSection.visibility = View.VISIBLE
            binding.otpSection.visibility = View.GONE
            clearOtpFields()
        }
    }

    private fun setupPhoneInput() {
        binding.ccp.registerCarrierNumberEditText(binding.mobileEditText)
        
        binding.mobileEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isValid = binding.ccp.isValidFullNumber
                binding.sendOtpButton.isEnabled = isValid
                binding.errorTextView.visibility = if (isValid || s.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
            }
        })
    }

    private fun setupOtpInputs() {
        val fields = arrayOf(binding.otp1, binding.otp2, binding.otp3, binding.otp4, binding.otp5, binding.otp6)
        for (i in fields.indices) {
            fields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < fields.size - 1) {
                            fields[i + 1].requestFocus()
                        } else {
                            verifyOtp()
                        }
                    }
                }
            })
            
            fields[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (fields[i].text.isNullOrEmpty() && i > 0) {
                        fields[i - 1].requestFocus()
                        fields[i - 1].text?.clear()
                        true
                    } else false
                } else false
            }
        }
    }

    private fun clearOtpFields() {
        binding.otp1.text?.clear()
        binding.otp2.text?.clear()
        binding.otp3.text?.clear()
        binding.otp4.text?.clear()
        binding.otp5.text?.clear()
        binding.otp6.text?.clear()
    }

    private fun getOtpFromFields(): String {
        return binding.otp1.text.toString() + binding.otp2.text.toString() + 
               binding.otp3.text.toString() + binding.otp4.text.toString() + 
               binding.otp5.text.toString() + binding.otp6.text.toString()
    }

    private fun sendOtp() {
        if (!binding.ccp.isValidFullNumber) {
            binding.mobileEditText.error = "Enter a valid phone number"
            return
        }

        mobileNumber = PhoneUtils.normalizePhone(binding.ccp.fullNumberWithPlus) ?: ""
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@LoginActivity)
                val response = api.sendOtp(OtpRequest(mobileNumber))

                if (response.isSuccessful) {
                    showOtpField()
                    startResendTimer()
                    showToast("OTP sent successfully")
                } else {
                    showToast("Failed to send OTP. Try again.")
                }
            } catch (e: Exception) {
                showToast("Network Error: ${e.message}")
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun verifyOtp() {
        val otp = getOtpFromFields()
        if (otp.length != 6) {
            showToast("Enter 6-digit OTP")
            return
        }

        binding.verifyOtpButton.isEnabled = false
        binding.verifyingLayout.visibility = View.VISIBLE
        hideKeyboard()

        val normalizedPhone = PhoneUtils.normalizePhone(mobileNumber) ?: ""

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@LoginActivity)
                val response = api.verifyOtp(OtpVerifyRequest(normalizedPhone, otp))

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // ✅ STEP 1: SAVE TOKEN & PHONE FIRST
                    sessionManager.saveToken(loginResponse.token)
                    sessionManager.savePhone(normalizedPhone)

                    // ✅ STEP 2: NAVIGATE BASED ON ROLE FROM BACKEND
                    val role = (loginResponse.role ?: "").trim().uppercase()
                    Log.d("LOGIN_DEBUG", "ROLE RECEIVED: $role")

                    showToast("Login Successful")
                    
                    // Added delay to allow time for a screenshot of the toast message
                    delay(3000)

                    when (role) {
                        "USER" -> {
                            sessionManager.saveUserSession(
                                userId = loginResponse.userId ?: 0L,
                                name = loginResponse.name,
                                email = loginResponse.email,
                                upiId = loginResponse.upiId
                            )
                            sessionManager.setRole("USER")
                            navigateToDashboard("USER")
                        }
                        "MERCHANT" -> {
                            sessionManager.saveMerchantSession(
                                merchantId = loginResponse.merchantId ?: 0L,
                                name = loginResponse.name,
                                email = loginResponse.email,
                                upiId = loginResponse.upiId
                            )
                            sessionManager.setRole("MERCHANT")
                            navigateToDashboard("MERCHANT")
                        }
                        "ROLE_SELECTION" -> {
                            sessionManager.setRole("ROLE_SELECTION")
                            startActivity(Intent(this@LoginActivity, RoleSelectionActivity::class.java).apply {
                                putExtra("phoneNumber", normalizedPhone)
                            })
                            finish()
                        }
                        else -> {
                            // Fallback if role is empty or unknown
                            startActivity(Intent(this@LoginActivity, RoleSelectionActivity::class.java).apply {
                                putExtra("phoneNumber", normalizedPhone)
                            })
                            finish()
                        }
                    }

                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("LOGIN_DEBUG", "RAW ERROR = $errorBody")
                    
                    if (errorBody.contains("ACCOUNT_RESTRICTED", ignoreCase = true)) {
                        DialogUtils.showBlockedDialog(this@LoginActivity)
                    } else {
                        showToast("Something went wrong. Please try again.")
                    }
                }

            } catch (e: Exception) {
                showToast("Network Error: ${e.message}")
            } finally {
                binding.verifyOtpButton.isEnabled = true
                binding.verifyingLayout.visibility = View.GONE
            }
        }
    }

    private fun navigateToDashboard(role: String) {
        val intent = if (role == "USER") {
            Intent(this, UserDashboardActivity::class.java)
        } else {
            Intent(this, MerchantDashboardActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun startResendTimer() {
        canResend = false
        resendTimer?.cancel()

        resendTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(ms: Long) {
                binding.resendOtpText.text = "Resend in ${ms / 1000}s"
                binding.resendOtpText.isEnabled = false
            }

            override fun onFinish() {
                binding.resendOtpText.text = "Resend OTP"
                binding.resendOtpText.isEnabled = true
                canResend = true
            }
        }.start()
    }

    private fun showOtpField() {
        isOtpSent = true
        binding.mobileSection.visibility = View.GONE
        binding.otpSection.visibility = View.VISIBLE
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.sendOtpButton.isEnabled = !isLoading
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        resendTimer?.cancel()
        super.onDestroy()
    }
}

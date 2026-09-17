package com.upimicro.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.*
import com.upimicro.databinding.ActivityOtpBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.role.RoleSelectionActivity
import com.upimicro.ui.user.UserDashboardActivity
import com.upimicro.ui.merchant.MerchantDashboardActivity
import com.upimicro.utils.SessionManager
import com.upimicro.utils.DialogUtils
import com.upimicro.utils.PhoneUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var sessionManager: SessionManager

    private var mobileNumber: String? = null
    private var resendTimer: CountDownTimer? = null
    private var canResend = false
    private var isVerifying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        mobileNumber = intent.getStringExtra("mobileNumber")

        if (mobileNumber.isNullOrEmpty()) {
            showToast("Mobile number missing")
            finish()
            return
        }

        startResendTimer()
        setupOtpInputLogic()

        binding.verifyOtpButton.setOnClickListener { verifyOtp() }

        binding.resendOtpText.setOnClickListener {
            if (!canResend) {
                showToast("Wait before requesting new OTP")
                return@setOnClickListener
            }
            resendOtp()
        }
    }

    private fun setupOtpInputLogic() {
        binding.otpEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) verifyOtp()
            }
        })

        binding.otpEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyOtp()
                true
            } else false
        }
    }

    private fun verifyOtp() {
        if (isVerifying) return

        val enteredOtp = binding.otpEditText.text.toString().trim()

        if (enteredOtp.length != 6) {
            binding.otpLayout.error = "Enter valid OTP"
            return
        }

        isVerifying = true
        setLoadingState(true)
        hideKeyboard()

        verifyOtpFromBackend(enteredOtp)
    }

    private fun verifyOtpFromBackend(otp: String) {
        val phone = mobileNumber ?: return
        val normalizedPhone = PhoneUtils.normalizePhone(phone) ?: phone

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@OtpActivity)
                val response = api.verifyOtp(OtpVerifyRequest(normalizedPhone, otp))

                Log.d("FINAL_DEBUG", "HTTP CODE: ${response.code()}")
                Log.d("FINAL_DEBUG", "BODY: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {

                    val body = response.body()!!
                    val token = body.token ?: ""

                    if (token.isEmpty()) {
                        showToast("Authentication failed")
                        resetState()
                        return@launch
                    }

                    // ✅ STEP 1: SAVE TOKEN & PHONE FIRST
                    sessionManager.saveToken(token)
                    sessionManager.savePhone(normalizedPhone)

                    // ✅ STEP 2: NAVIGATE BASED ON ROLE FROM BACKEND
                    val role = (body.role ?: "").trim().uppercase()
                    Log.d("FINAL_DEBUG", "ROLE RECEIVED: $role")

                    when (role) {
                        "USER" -> {
                            sessionManager.saveUserSession(
                                userId = body.userId ?: 0L,
                                name = body.name,
                                email = body.email,
                                upiId = body.upiId
                            )
                            sessionManager.setRole("USER")
                            navigateToDashboard("USER")
                        }
                        "MERCHANT" -> {
                            sessionManager.saveMerchantSession(
                                merchantId = body.merchantId ?: 0L,
                                name = body.name,
                                email = body.email,
                                upiId = body.upiId
                            )
                            sessionManager.setRole("MERCHANT")
                            navigateToDashboard("MERCHANT")
                        }
                        "ROLE_SELECTION" -> {
                            sessionManager.setRole("ROLE_SELECTION")
                            navigateToRoleSelection()
                        }
                        else -> {
                            // Only if backend doesn't send a valid role, we try detection
                            detectRolesAndNavigate()
                        }
                    }

                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("OTP_DEBUG", "RAW ERROR = $errorBody")

                    if (errorBody.contains("ACCOUNT_RESTRICTED", ignoreCase = true)) {
                        sessionManager.saveBlockedStatus(true)
                        DialogUtils.showBlockedDialog(this@OtpActivity)
                        finish()
                    } else if (errorBody.contains("INVALID_OTP", ignoreCase = true)) {
                        binding.otpLayout.error = "Invalid OTP. Please try again."
                        resetState()
                    } else {
                        showToast("Something went wrong. Please try again.")
                        resetState()
                    }
                }

            } catch (e: Exception) {
                Log.e("OTP_DEBUG", "EXCEPTION: ${e.message}")
                showToast("Something went wrong. Please try again.")
                resetState()
            }
        }
    }

    private suspend fun detectRolesAndNavigate() {
        val api = RetrofitClient.getApi(this@OtpActivity)
        try {
            val TAG = "ROLE_DETECTION"
            Log.d(TAG, "Starting role detection...")

            val userProfileDef = lifecycleScope.async { api.getUserProfile() }
            val merchantProfileDef = lifecycleScope.async { api.getMerchantProfile() }

            val userRes = userProfileDef.await()
            val merchantRes = merchantProfileDef.await()

            val availableRoles = mutableSetOf<String>()

            if (userRes.isSuccessful && userRes.body() != null) {
                val user = userRes.body()!!
                if (user.userId > 0) {
                    availableRoles.add("USER")
                    sessionManager.saveUserSession(user.userId, user.name, user.email, user.upiId)
                    sessionManager.setUserRegistered(true)
                }
            }

            if (merchantRes.isSuccessful && merchantRes.body() != null) {
                val merchant = merchantRes.body()!!
                if ((merchant.merchantId ?: 0L) > 0) {
                    availableRoles.add("MERCHANT")
                    sessionManager.saveMerchantSession(
                        merchant.merchantId ?: 0L,
                        merchant.name,
                        merchant.email,
                        merchant.upiId,
                        merchant.businessName
                    )
                    sessionManager.setMerchantRegistered(true)
                }
            }

            sessionManager.setAvailableRoles(availableRoles)
            val lastRole = sessionManager.getLastUsedRole()
            val token = sessionManager.getToken()
            val phone = sessionManager.getPhone()

            when {
                availableRoles.size > 1 -> {
                    if (lastRole.isNotEmpty() && availableRoles.contains(lastRole)) {
                        sessionManager.saveFullSession(token, lastRole, phone)
                        navigateToDashboard(lastRole)
                    } else {
                        sessionManager.setRole("ROLE_SELECTION")
                        navigateToRoleSelection()
                    }
                }
                availableRoles.contains("USER") -> {
                    sessionManager.saveFullSession(token, "USER", phone)
                    navigateToDashboard("USER")
                }
                availableRoles.contains("MERCHANT") -> {
                    sessionManager.saveFullSession(token, "MERCHANT", phone)
                    navigateToDashboard("MERCHANT")
                }
                else -> {
                    sessionManager.setRole("ROLE_SELECTION")
                    navigateToRoleSelection()
                }
            }

        } catch (e: Exception) {
            Log.e("ROLE_DETECTION", "Critical Error: ${e.message}", e)
            showToast("Failed to sync profile data")
            resetState()
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

    private fun navigateToRoleSelection() {
        val intent = Intent(this, RoleSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.verifyOtpButton.isEnabled = !isLoading
        binding.otpProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.otpEditText.isEnabled = !isLoading
    }

    private fun resetState() {
        isVerifying = false
        setLoadingState(false)
        binding.otpEditText.text?.clear()
    }

    private fun hideKeyboard() {
        currentFocus?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun resendOtp() {
        val phone = mobileNumber ?: return
        val normalizedPhone = PhoneUtils.normalizePhone(phone) ?: phone
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@OtpActivity)
                val response = api.sendOtp(OtpRequest(normalizedPhone))
                if (response.isSuccessful) {
                    showToast("OTP sent")
                    startResendTimer()
                } else {
                    showToast("Failed to send OTP")
                }
            } catch (e: Exception) {
                showToast("Network error")
            }
        }
    }

    private fun startResendTimer() {
        canResend = false
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(ms: Long) {
                binding.resendOtpText.text = "Resend in ${ms / 1000}s"
            }
            override fun onFinish() {
                binding.resendOtpText.text = "Resend OTP"
                canResend = true
            }
        }.start()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        resendTimer?.cancel()
        super.onDestroy()
    }
}

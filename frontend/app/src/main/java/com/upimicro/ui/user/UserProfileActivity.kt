package com.upimicro.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.R
import com.upimicro.databinding.ActivityUserProfileBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.ui.auth.RegistrationActivity
import com.upimicro.ui.merchant.MerchantDashboardActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var sessionManager: SessionManager
    private var isSyncing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupListeners()

        // 🔥 REMOVE OLD DATA ISSUE
        clearTemporaryUI()

        setupUI()
        setupRoleSwitch()
    }

    override fun onResume() {
        super.onResume()
        syncProfile() // 🔥 ALWAYS REFRESH FROM API
    }

    private fun clearTemporaryUI() {
        binding.kycTitleText.text = "Loading..."
        binding.kycStatusText.text = ""
    }

    private fun setupRoleSwitch() {
        // NEW CODE START
        binding.switchRoleCard.setOnClickListener {
            if (sessionManager.isMerchantRegistered()) {
                sessionManager.setRole("MERCHANT")
                val intent = Intent(this, MerchantDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, RegistrationActivity::class.java)
                intent.putExtra("selectedRole", "MERCHANT")
                intent.putExtra("preFillPhone", sessionManager.getPhone())
                startActivity(intent)
            }
        }
        // NEW CODE END
    }

    private fun syncProfile() {

        val phone = sessionManager.getFormattedPhone()
        if (phone.length <= 1) {
            redirectToLogin()
            return
        }

        isSyncing = true
        showLoading(true)

        lifecycleScope.launch {
            try {

                val response = RetrofitClient
                    .getApi(this@UserProfileActivity)
                    .getUserProfile()

                if (response.isSuccessful && response.body() != null) {

                    val user = response.body()!!
                    Log.d("PROFILE_SYNC", "API: $user")

                    // ✅ SAVE SESSION
                    sessionManager.saveUserSession(
                        user.userId,
                        user.name ?: "",
                        user.email ?: "",
                        user.upiId ?: ""
                    )

                    sessionManager.saveKycVerified(user.kycVerified == true)

                    sessionManager.saveBankLinked(
                        user.bankLinked == true,
                        user.bankName ?: "",
                        user.accountNumber ?: "",
                        user.ifscCode ?: ""
                    )

                    // 🔥 FORCE UI UPDATE
                    runOnUiThread {
                        updateUIFromApi(user)
                    }

                } else {
                    if (response.code() == 401) {
                        sessionManager.logout()
                        redirectToLogin()
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("PROFILE_SYNC", "Error: ${e.message}")
                Toast.makeText(this@UserProfileActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
            } finally {
                isSyncing = false
                showLoading(false)
            }
        }
    }

    private fun updateUIFromApi(user: com.upimicro.data.model.UserResponse) {

        binding.userNameText.text = user.name ?: "User"
        binding.userPhoneText.text = user.phone ?: ""
        binding.userEmailText.text = user.email ?: "Not Available"

        binding.upiIdText.text =
            if (!user.upiId.isNullOrEmpty()) user.upiId else "${user.phone}@ybl"

        if (user.bankLinked == true) {
            binding.bankDetailsCard.visibility = View.VISIBLE

            val acc = user.accountNumber ?: ""

            binding.bankNameText.text = user.bankName ?: ""
            binding.accountNumberText.text =
                if (acc.length >= 4) "XXXX XXXX ${acc.takeLast(4)}" else acc
            binding.ifscCodeText.text = user.ifscCode ?: ""

        } else {
            binding.bankDetailsCard.visibility = View.GONE
        }

        // 🔥 FINAL KYC FIX (THIS IS WORKING NOW)
        if (user.kycVerified == true) {
            binding.kycIcon.setImageResource(android.R.drawable.checkbox_on_background)
            binding.kycIcon.imageTintList = getColorStateList(R.color.successGreen)
            binding.kycTitleText.text = "KYC Verified"
            binding.kycStatusText.text = "Fully verified"
            binding.kycActionIcon.visibility = View.GONE
        } else {
            binding.kycIcon.setImageResource(android.R.drawable.ic_dialog_info)
            binding.kycIcon.imageTintList = getColorStateList(R.color.warningOrange)
            binding.kycTitleText.text = "Complete KYC"
            binding.kycStatusText.text = "Increase limits"
            binding.kycActionIcon.visibility = View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.profileProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.profileContainer.alpha = if (isLoading) 0.5f else 1f
        binding.editProfileBtn.isEnabled = !isLoading
        binding.kycCard.isEnabled = !isLoading
        binding.myQrLayout.isEnabled = !isLoading
    }

    private fun setupUI() {

        val phone = sessionManager.getPhone()
        val userName = sessionManager.getUserName()
        val email = sessionManager.getUserEmail()
        val upiId = sessionManager.getUserUpi()

        val bankLinked = sessionManager.isBankLinked()
        val kycVerified = sessionManager.isKycVerified()

        binding.userNameText.text = if (userName.isNotEmpty()) userName else "User"
        binding.userPhoneText.text = phone
        binding.userEmailText.text = if (email.isNotEmpty()) email else "Not Available"

        binding.upiIdText.text =
            if (upiId.isNotEmpty()) upiId else "$phone@ybl"

        if (bankLinked) {
            binding.bankDetailsCard.visibility = View.VISIBLE
            val acc = sessionManager.getAccountNumber()
            binding.bankNameText.text = sessionManager.getBankName()
            binding.accountNumberText.text =
                if (acc.length >= 4) "XXXX XXXX ${acc.takeLast(4)}" else acc
            binding.ifscCodeText.text = sessionManager.getIfscCode()
        } else {
            binding.bankDetailsCard.visibility = View.GONE
        }

        // INITIAL STATE (will be overridden by API)
        if (kycVerified) {
            binding.kycTitleText.text = "KYC Verified"
        } else {
            binding.kycTitleText.text = "Complete KYC"
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.logoutBtn.setOnClickListener {
            sessionManager.logout()
            redirectToLogin()
        }

        binding.myQrLayout.setOnClickListener {
            startActivity(Intent(this, MyQrActivity::class.java))
        }

        binding.kycCard.setOnClickListener {
            if (!sessionManager.isKycVerified()) {
                startActivity(Intent(this, KycActivity::class.java))
            } else {
                Toast.makeText(this, "KYC already completed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.editProfileBtn.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

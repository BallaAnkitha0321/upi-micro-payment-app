package com.upimicro.ui.merchant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.upimicro.R
import com.upimicro.data.model.MerchantProfileResponse
import com.upimicro.databinding.ActivityMerchantProfileBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.ui.auth.RegistrationActivity
import com.upimicro.ui.user.UserDashboardActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException

class MerchantProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantProfileBinding
    private lateinit var sessionManager: SessionManager
    private var currentKycStatus: String? = null
    private var currentProfile: MerchantProfileResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupListeners()
        setupRoleSwitch()
        loadProfile()
    }

    override fun onResume() {
        super.onResume()

        // 🔥 ALWAYS REFRESH (FIX)
        loadProfile()
    }

    private fun loadProfile() {

        val token = sessionManager.getToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {

                val response = RetrofitClient
                    .getApi(this@MerchantProfileActivity)
                    .getMerchantProfile()

                if (response.isSuccessful && response.body() != null) {

                    val merchant = response.body()!!
                    currentProfile = merchant

                    Log.d("PROFILE_DEBUG", "FULL_RESPONSE = $merchant")

                    // 🔥🔥🔥 CRITICAL FIX (SYNC SESSION)
                    sessionManager.saveBankLinked(
                        merchant.bankLinked == true,
                        merchant.bankName ?: "",
                        merchant.accountNumber ?: "",
                        merchant.ifscCode ?: ""
                    )

                    displayMerchantData(merchant)

                } else {
                    Toast.makeText(
                        this@MerchantProfileActivity,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                if (e is IOException) {
                    Toast.makeText(
                        this@MerchantProfileActivity,
                        "No internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MerchantProfileActivity,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupRoleSwitch() {
        // NEW CODE START
        binding.switchRoleCard.setOnClickListener {
            if (sessionManager.isUserRegistered()) {
                sessionManager.setRole("USER")
                val intent = Intent(this, UserDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, RegistrationActivity::class.java)
                intent.putExtra("selectedRole", "USER")
                intent.putExtra("preFillPhone", sessionManager.getPhone())
                startActivity(intent)
            }
        }
        // NEW CODE END
    }

    private fun displayMerchantData(merchant: MerchantProfileResponse) {

        binding.tvMerchantName.text = merchant.name ?: "-"
        // Clean UPI ID (Remove +91)
        binding.tvUpiId.text = merchant.upiId?.replace("+91", "") ?: "-"

        binding.businessNameText.text = merchant.businessName ?: "-"
        binding.categoryText.text = merchant.businessCategory ?: "-"
        binding.addressText.text = merchant.businessAddress ?: "-"

        currentKycStatus = merchant.kycStatus?.trim()?.uppercase()

        when (currentKycStatus) {

            "PENDING" -> {
                binding.kycStatusText.text = "Pending"
                binding.kycStatusText.setTextColor(
                    ContextCompat.getColor(this, R.color.warningOrange)
                )
            }

            "APPROVED", "VERIFIED" -> {
                binding.kycStatusText.text = "Verified"
                binding.kycStatusText.setTextColor(
                    ContextCompat.getColor(this, R.color.successGreen)
                )
            }

            "REJECTED" -> {
                binding.kycStatusText.text = "Rejected"
                binding.kycStatusText.setTextColor(
                    ContextCompat.getColor(this, R.color.errorRed)
                )
            }

            else -> {
                binding.kycStatusText.text = "Not Started"
                binding.kycStatusText.setTextColor(
                    ContextCompat.getColor(this, R.color.textLight)
                )
            }
        }

        if (currentKycStatus == "VERIFIED" || currentKycStatus == "APPROVED") {
            binding.kycSectionCard.isClickable = false
            binding.kycSectionCard.alpha = 0.7f
            binding.ivKycArrow.visibility = View.GONE
        } else {
            binding.kycSectionCard.isClickable = true
            binding.ivKycArrow.visibility = View.VISIBLE

            binding.kycSectionCard.setOnClickListener {
                startActivity(Intent(this, MerchantKycActivity::class.java))
            }
        }

        val isBankLinked = merchant.bankLinked == true

        updateBankUI(
            isBankLinked,
            merchant.bankName,
            merchant.accountNumber,
            merchant.ifscCode
        )
    }

    private fun updateBankUI(
        isBankLinked: Boolean,
        bankName: String?,
        accountNumber: String?,
        ifsc: String?
    ) {

        if (isBankLinked) {

            binding.bankWarningCard.visibility = View.GONE
            binding.tvLinkNow.visibility = View.GONE

            val acc = accountNumber ?: ""
            val maskedAccount = if (acc.length >= 4) {
                "XXXXXXXX${acc.takeLast(4)}"
            } else {
                "XXXXXXXXXXXX"
            }

            binding.tvBankName.text = bankName ?: "Bank Linked"
            binding.tvAccountNumber.text = maskedAccount
            binding.tvIfsc.text = "IFSC: ${ifsc ?: "--"}"

            binding.tvBankStatus.text = "✔ Receiving Payments"
            binding.tvBankStatus.setTextColor(
                ContextCompat.getColor(this, R.color.successGreen)
            )

            binding.bankSectionCard.isClickable = false
            binding.ivBankArrow.visibility = View.GONE

        } else {

            binding.bankWarningCard.visibility = View.VISIBLE
            binding.tvLinkNow.visibility = View.VISIBLE

            binding.tvBankName.text = "Not Linked"
            binding.tvAccountNumber.text = "Link your account"

            binding.tvBankStatus.text = "Link bank to receive payments"
            binding.tvBankStatus.setTextColor(
                ContextCompat.getColor(this, R.color.textLight)
            )

            binding.bankSectionCard.isClickable = true
            binding.ivBankArrow.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, MerchantEditProfileActivity::class.java).apply {
                putExtra("name", currentProfile?.name)
                putExtra("email", currentProfile?.email)
                putExtra("businessName", currentProfile?.businessName)
                putExtra("businessCategory", currentProfile?.businessCategory)
                putExtra("businessAddress", currentProfile?.businessAddress)
            }
            startActivity(intent)
        }

        binding.bankSectionCard.setOnClickListener {
            startActivity(Intent(this, MerchantLinkBankActivity::class.java))
        }

        binding.tvLinkNow.setOnClickListener {
            startActivity(Intent(this, MerchantLinkBankActivity::class.java))
        }

        binding.logoutBtn.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                sessionManager.logout()
                redirectToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

package com.upimicro.ui.merchant

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.upimicro.databinding.ActivityMerchantSettingsBinding
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.utils.SessionManager

class MerchantSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantSettingsBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        loadMerchantProfile()
        setupSettingsOptions()
        setupLogout()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // ✅ FIXED PROFILE DISPLAY
    private fun loadMerchantProfile() {

        val name = sessionManager.getMerchantName()
        val upi = sessionManager.getMerchantUpi()

        binding.merchantNameText.text =
            if (name.isEmpty()) "Merchant" else name

        binding.merchantUpiText.text =
            if (upi.isEmpty()) "UPI not set" else upi
    }

    private fun setupSettingsOptions() {

        binding.settingEditProfile.apply {
            settingIcon.setImageResource(android.R.drawable.ic_menu_edit)
            settingTitle.text = "Edit Profile"
            root.setOnClickListener {
                startActivity(
                    Intent(
                        this@MerchantSettingsActivity,
                        MerchantEditProfileActivity::class.java
                    )
                )
            }
        }

        binding.settingBankDetails.apply {
            settingIcon.setImageResource(android.R.drawable.ic_menu_manage)
            settingTitle.text = "Bank Account"
            root.setOnClickListener {
                startActivity(
                    Intent(
                        this@MerchantSettingsActivity,
                        MerchantLinkBankActivity::class.java
                    )
                )
            }
        }

        binding.settingKycStatus.apply {
            settingIcon.setImageResource(android.R.drawable.ic_menu_info_details)
            settingTitle.text = "KYC Verification"
            root.setOnClickListener {
                startActivity(
                    Intent(
                        this@MerchantSettingsActivity,
                        MerchantKycActivity::class.java
                    )
                )
            }
        }

        binding.settingChangePassword.apply {
            settingIcon.setImageResource(android.R.drawable.ic_lock_idle_lock)
            settingTitle.text = "Change Password"
            root.setOnClickListener {
                Toast.makeText(this@MerchantSettingsActivity, "Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }

        binding.settingNotifications.apply {
            settingIcon.setImageResource(android.R.drawable.ic_popup_reminder)
            settingTitle.text = "Push Notifications"
            root.setOnClickListener {
                Toast.makeText(this@MerchantSettingsActivity, "Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }

        binding.settingHelp.apply {
            settingIcon.setImageResource(android.R.drawable.ic_menu_help)
            settingTitle.text = "Help & Support"
        }

        binding.settingPrivacy.apply {
            settingIcon.setImageResource(android.R.drawable.ic_menu_view)
            settingTitle.text = "Privacy Policy"
        }

        binding.settingTerms.apply {
            settingIcon.setImageResource(android.R.drawable.ic_menu_agenda)
            settingTitle.text = "Terms of Service"
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {

        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->

                // ✅ FIXED HERE
                sessionManager.logout()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
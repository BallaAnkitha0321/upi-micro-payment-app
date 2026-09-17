package com.upimicro.ui.role

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.upimicro.databinding.ActivityRoleSelectionBinding
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.ui.auth.RegistrationActivity
import com.upimicro.ui.merchant.MerchantDashboardActivity
import com.upimicro.ui.user.UserDashboardActivity
import com.upimicro.utils.SessionManager

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        // NEW CODE START
        val isUserReg = sessionManager.isUserRegistered()
        val isMerchantReg = sessionManager.isMerchantRegistered()

        // User Section
        if (isUserReg) {
            binding.userButtonTitle.text = "Continue as User"
            binding.userStatus.text = "Account Found"
            binding.userButton.setOnClickListener {
                sessionManager.setRole("USER")
                navigateToDashboard(UserDashboardActivity::class.java)
            }
        } else {
            binding.userButtonTitle.text = "Register as User"
            binding.userStatus.text = "Not Registered"
            binding.userButton.setOnClickListener {
                navigateToRegistration("USER")
            }
        }

        // Merchant Section
        if (isMerchantReg) {
            binding.merchantButtonTitle.text = "Continue as Merchant"
            binding.merchantStatus.text = "Account Found"
            binding.merchantButton.setOnClickListener {
                sessionManager.setRole("MERCHANT")
                navigateToDashboard(MerchantDashboardActivity::class.java)
            }
        } else {
            binding.merchantButtonTitle.text = "Register as Merchant"
            binding.merchantStatus.text = "Not Registered"
            binding.merchantButton.setOnClickListener {
                navigateToRegistration("MERCHANT")
            }
        }
        // NEW CODE END
    }

    private fun navigateToDashboard(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToRegistration(role: String) {
        val intent = Intent(this, RegistrationActivity::class.java)
        intent.putExtra("selectedRole", role)
        // NEW CODE START
        intent.putExtra("preFillPhone", sessionManager.getPhone())
        // NEW CODE END
        startActivity(intent)
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

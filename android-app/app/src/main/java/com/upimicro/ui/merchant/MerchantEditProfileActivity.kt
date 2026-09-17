package com.upimicro.ui.merchant

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.MerchantUpdateProfileRequest
import com.upimicro.databinding.ActivityMerchantEditProfileBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class MerchantEditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantEditProfileBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupListeners()
        
        // Pre-fill fields from Intent data
        prefillData()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.etMerchantName.doAfterTextChanged { validateName() }
        binding.etEmail.doAfterTextChanged { validateEmail() }
        binding.etBusinessName.doAfterTextChanged { validateBusinessName() }
        binding.etBusinessCategory.doAfterTextChanged { validateCategory() }
        binding.etBusinessAddress.doAfterTextChanged { validateAddress() }
    }

    private fun prefillData() {
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val businessName = intent.getStringExtra("businessName")
        val businessCategory = intent.getStringExtra("businessCategory")
        val businessAddress = intent.getStringExtra("businessAddress")

        binding.etMerchantName.setText(name ?: "")
        binding.etEmail.setText(email ?: "")
        binding.etBusinessName.setText(businessName ?: "")
        binding.etBusinessCategory.setText(businessCategory ?: "")
        binding.etBusinessAddress.setText(businessAddress ?: "")
        
        // Clear errors after pre-filling
        clearAllErrors()
    }

    private fun clearAllErrors() {
        binding.tilMerchantName.error = null
        binding.tilEmail.error = null
        binding.tilBusinessName.error = null
        binding.tilBusinessCategory.error = null
        binding.tilBusinessAddress.error = null
    }

    private fun setupListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            if (validateAll()) {
                updateMerchantProfile()
            } else {
                showToast("Fix errors first")
            }
        }
    }

    private fun validateName(): Boolean {
        val name = binding.etMerchantName.text.toString().trim()
        return if (name.length < 3) {
            binding.tilMerchantName.error = "Invalid name"
            false
        } else {
            binding.tilMerchantName.error = null
            true
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email"
            false
        } else {
            binding.tilEmail.error = null
            true
        }
    }

    private fun validateBusinessName(): Boolean {
        val bName = binding.etBusinessName.text.toString().trim()
        return if (bName.isEmpty()) {
            binding.tilBusinessName.error = "Required"
            false
        } else {
            binding.tilBusinessName.error = null
            true
        }
    }

    private fun validateCategory(): Boolean {
        val cat = binding.etBusinessCategory.text.toString().trim()
        return if (cat.isEmpty()) {
            binding.tilBusinessCategory.error = "Required"
            false
        } else {
            binding.tilBusinessCategory.error = null
            true
        }
    }

    private fun validateAddress(): Boolean {
        val addr = binding.etBusinessAddress.text.toString().trim()
        return if (addr.isEmpty()) {
            binding.tilBusinessAddress.error = "Required"
            false
        } else {
            binding.tilBusinessAddress.error = null
            true
        }
    }

    private fun validateAll(): Boolean {
        return validateName() &&
                validateEmail() &&
                validateBusinessName() &&
                validateCategory() &&
                validateAddress()
    }

    private fun updateMerchantProfile() {

        val merchantId = sessionManager.getMerchantId()
        val token = sessionManager.getToken()

        if (merchantId == -1L || token.isNullOrEmpty()) {
            showToast("Session expired")
            return
        }

        val request = MerchantUpdateProfileRequest(
            merchantId = merchantId,
            name = binding.etMerchantName.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            businessName = binding.etBusinessName.text.toString().trim(),
            businessCategory = binding.etBusinessCategory.text.toString().trim(),
            businessAddress = binding.etBusinessAddress.text.toString().trim()
        )

        binding.btnUpdateProfile.isEnabled = false
        binding.btnUpdateProfile.text = "Saving..."

        lifecycleScope.launch {
            try {

                val response = RetrofitClient
                    .getApi(this@MerchantEditProfileActivity)
                    .updateMerchantProfile(request)

                if (response.isSuccessful) {

                    // ✅ FIXED SESSION SAVE
                    sessionManager.saveMerchantSession(
                        merchantId = merchantId,
                        name = request.name,
                        email = request.email,
                        upiId = sessionManager.getMerchantUpi()
                    )

                    showToast("Profile updated")
                    finish()

                } else {
                    showToast("Update failed")
                }

            } catch (e: Exception) {
                Log.e("EDIT_PROFILE", "Error", e)
                showToast("Network error")
            } finally {
                binding.btnUpdateProfile.isEnabled = true
                binding.btnUpdateProfile.text = "Save Changes"
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

package com.upimicro.ui.merchant

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.MerchantKycRequest
import com.upimicro.databinding.ActivityMerchantKycBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class MerchantKycActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantKycBinding
    private lateinit var sessionManager: SessionManager

    private val panPattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantKycBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.etPan.doAfterTextChanged {
            validatePan(it.toString())
        }

        binding.etAadhaar.doAfterTextChanged {
            validateAadhaar(it.toString())
        }
    }

    private fun validatePan(pan: String): Boolean {
        val upperPan = pan.uppercase()

        return when {
            upperPan.isEmpty() -> {
                binding.tilPan.error = "PAN is required"
                false
            }
            upperPan.length != 10 -> {
                binding.tilPan.error = "PAN must be 10 characters"
                false
            }
            !panPattern.matcher(upperPan).matches() -> {
                binding.tilPan.error = "Invalid PAN (ABCDE1234F)"
                false
            }
            else -> {
                binding.tilPan.error = null
                true
            }
        }
    }

    private fun validateAadhaar(aadhaar: String): Boolean {
        return when {
            aadhaar.isEmpty() -> {
                binding.tilAadhaar.error = "Aadhaar is required"
                false
            }
            aadhaar.length != 12 -> {
                binding.tilAadhaar.error = "Must be 12 digits"
                false
            }
            else -> {
                binding.tilAadhaar.error = null
                true
            }
        }
    }

    private fun setupListeners() {
        binding.btnSubmitKyc.setOnClickListener {
            if (validateAll()) {
                submitKyc()
            } else {
                Toast.makeText(this, "Fix errors first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateAll(): Boolean {
        return validatePan(binding.etPan.text.toString()) &&
                validateAadhaar(binding.etAadhaar.text.toString())
    }

    private fun submitKyc() {

        val aadhaar = binding.etAadhaar.text.toString().trim()
        val pan = binding.etPan.text.toString().trim().uppercase()

        val merchantId = sessionManager.getMerchantId()
        val token = sessionManager.getToken()

        if (merchantId == -1L || token.isNullOrEmpty()) {
            Toast.makeText(this, "Session expired. Login again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val request = MerchantKycRequest(
            merchantId = merchantId,
            aadhaarNumber = aadhaar,
            panNumber = pan
        )

        binding.btnSubmitKyc.isEnabled = false
        binding.btnSubmitKyc.text = "Submitting..."

        lifecycleScope.launch {
            try {

                val response = RetrofitClient
                    .getApi(this@MerchantKycActivity)
                    .updateMerchantKyc(request)

                if (response.isSuccessful) {

                    Toast.makeText(
                        this@MerchantKycActivity,
                        "KYC submitted successfully",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()

                } else {

                    Log.e("KYC", "API error: ${response.code()}")

                    Toast.makeText(
                        this@MerchantKycActivity,
                        "Try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Log.e("KYC", "Network error", e)

                Toast.makeText(
                    this@MerchantKycActivity,
                    "Network error",
                    Toast.LENGTH_SHORT
                ).show()

            } finally {

                binding.btnSubmitKyc.isEnabled = true
                binding.btnSubmitKyc.text = "Submit Verification"
            }
        }
    }
}

package com.upimicro.ui.merchant

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.MerchantBankRequest
import com.upimicro.databinding.ActivityMerchantLinkBankBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class MerchantLinkBankActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantLinkBankBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantLinkBankBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupListeners()

        binding.etIfsc.filters = arrayOf(
            InputFilter.AllCaps(),
            InputFilter.LengthFilter(11)
        )
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnLinkBank.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {

        val bankName = binding.etBankName.text.toString().trim()
        val accNo = binding.etAccountNumber.text.toString().trim()
        val ifsc = binding.etIfsc.text.toString().trim().uppercase()
        val holder = binding.etAccountHolder.text.toString().trim()

        if (bankName.isEmpty()) {
            binding.etBankName.error = "Bank name required"
            return
        }

        if (accNo.length !in 9..18) {
            binding.etAccountNumber.error = "Invalid account number"
            return
        }

        val ifscRegex = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")
        if (!ifscRegex.matches(ifsc)) {
            binding.etIfsc.error = "Invalid IFSC"
            return
        }

        if (holder.length < 3) {
            binding.etAccountHolder.error = "Invalid name"
            return
        }

        val request = MerchantBankRequest(
            bankName = bankName,
            accountNumber = accNo,
            ifscCode = ifsc,
            accountHolderName = holder
        )

        submitBankDetails(request)
    }

    private fun submitBankDetails(request: MerchantBankRequest) {

        binding.btnLinkBank.isEnabled = false
        binding.btnLinkBank.text = "Linking..."

        val token = sessionManager.getToken()

        if (token.isNullOrEmpty()) {
            showToast("Session expired. Login again")
            resetButton()
            return
        }

        lifecycleScope.launch {
            try {

                val response = RetrofitClient
                    .getApi(this@MerchantLinkBankActivity)
                    .linkMerchantBank(request)

                if (response.isSuccessful && response.body() != null) {

                    // 🔥 IMPORTANT FIX (REAL-TIME UI UPDATE)
                    sessionManager.saveBankLinked(
                        isLinked = true,
                        bankName = request.bankName,
                        accountNumber = request.accountNumber,
                        ifsc = request.ifscCode
                    )

                    Log.d("BANK_DEBUG", "Linked = ${sessionManager.isBankLinked()}")

                    showToast("Bank Linked Successfully")

                    // 🔥 JUST GO BACK (IMPORTANT FIX)
                    finish()

                } else {
                    showToast("Failed. Try again")
                }

            } catch (e: Exception) {
                Log.e("BANK_LINK", e.message ?: "Error")
                showToast("Network error")
            } finally {
                resetButton()
            }
        }
    }

    private fun resetButton() {
        binding.btnLinkBank.isEnabled = true
        binding.btnLinkBank.text = "Link Bank Account"
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

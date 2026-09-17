package com.upimicro.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.*
import com.upimicro.databinding.ActivityRegistrationBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.merchant.MerchantDashboardActivity
import com.upimicro.ui.user.UserDashboardActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var sessionManager: SessionManager

    private var phoneNumber: String = ""
    private var selectedRole: String = "USER"

    private val bankList = listOf(
        "Select Bank",
        "State Bank of India",
        "HDFC Bank",
        "ICICI Bank",
        "Axis Bank"
    )

    private val businessTypes = listOf(
        "Retail",
        "Food & Beverage",
        "Services",
        "Electronics",
        "Others"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // NEW CODE START
        phoneNumber = intent.getStringExtra("preFillPhone") ?: intent.getStringExtra("phoneNumber") ?: sessionManager.getPhone()
        // NEW CODE END
        selectedRole = intent.getStringExtra("selectedRole") ?: "USER"

        setupDropdowns()
        setupUIForRole()

        binding.ifscCode.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(11))

        binding.registerButton.setOnClickListener {
            handleRegistration()
        }
    }

    private fun setupDropdowns() {
        binding.bankDropdown.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bankList)
        )
        binding.bankDropdown.setText(bankList[0], false)

        binding.businessTypeDropdown.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, businessTypes)
        )
    }

    private fun setupUIForRole() {
        if (selectedRole == "MERCHANT") {
            binding.businessDetailsCard.visibility = View.VISIBLE
            binding.toolbar.title = "Merchant Registration"
        } else {
            binding.businessDetailsCard.visibility = View.GONE
            binding.toolbar.title = "User Registration"
        }
    }

    private fun handleRegistration() {
        val name = binding.nameEditText.text.toString().trim()
        val bank = binding.bankDropdown.text.toString().trim()
        val holder = binding.accountHolderName.text.toString().trim()
        val acc = binding.accountNumber.text.toString().trim()
        val ifsc = binding.ifscCode.text.toString().trim().uppercase()

        if (name.isEmpty() || bank == "Select Bank" || holder.isEmpty() || acc.isEmpty() || ifsc.isEmpty()) {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedRole == "MERCHANT") {
            val shopName = binding.shopNameEditText.text.toString().trim()
            val category = binding.businessTypeDropdown.text.toString().trim()
            if (shopName.isEmpty()) {
                Toast.makeText(this, "Enter shop name", Toast.LENGTH_SHORT).show()
                return
            }
            registerMerchant(name, shopName, category, bank, holder, acc, ifsc)
        } else {
            registerUser(name, bank, holder, acc, ifsc)
        }
    }

    private fun registerUser(name: String, bank: String, holder: String, acc: String, ifsc: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@RegistrationActivity)
                    .registerUser(
                        UserRegisterRequest(
                            name = name,
                            phone = phoneNumber,
                            bankName = bank,
                            accountNumber = acc,
                            ifscCode = ifsc,
                            accountHolderName = holder
                        )
                    )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body.token ?: ""
                    
                    sessionManager.saveFullSession(token, "USER", phoneNumber)
                    sessionManager.saveUserSession(body.userId ?: -1L, body.name, body.email, body.upiId)

                    val bankResponse = RetrofitClient.getApi(this@RegistrationActivity)
                        .linkBank(
                            LinkBankRequest(
                                userId = body.userId ?: -1L,
                                bankName = bank,
                                accountNumber = acc,
                                ifscCode = ifsc,
                                accountHolderName = holder
                            )
                        )

                    if (bankResponse.isSuccessful) {
                        sessionManager.saveBankLinked(true, bank, acc, ifsc)
                        showSuccessAndNavigate()
                    } else {
                        Toast.makeText(this@RegistrationActivity, "Bank link failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RegistrationActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegistrationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerMerchant(name: String, shopName: String, category: String, bank: String, holder: String, acc: String, ifsc: String) {
        lifecycleScope.launch {
            try {
                // We use phoneNumber as UPI ID for simulation if not generated
                val upiId = phoneNumber.replace("+", "") + "@pay"

                Log.d("REGISTER_DEBUG", "Category = $category")

                val response = RetrofitClient.getApi(this@RegistrationActivity)
                    .registerMerchant(
                        MerchantRegisterRequest(
                            name = name,
                            businessName = shopName,
                            businessCategory = category,
                            phone = phoneNumber,
                            upiId = upiId
                        )
                    )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val token = body.token ?: ""

                    sessionManager.saveFullSession(token, "MERCHANT", phoneNumber)
                    sessionManager.saveMerchantSession(
                        merchantId = body.merchantId ?: -1L,
                        name = body.name ?: name,
                        email = body.email ?: "",
                        upiId = body.upiId ?: upiId
                    )

                    val bankResponse = RetrofitClient.getApi(this@RegistrationActivity)
                        .linkMerchantBank(
                            MerchantBankRequest(
                                bankName = bank,
                                accountNumber = acc,
                                ifscCode = ifsc,
                                accountHolderName = holder
                            )
                        )

                    if (bankResponse.isSuccessful) {
                        sessionManager.saveBankLinked(true, bank, acc, ifsc)
                        showSuccessAndNavigate()
                    } else {
                        Toast.makeText(this@RegistrationActivity, "Merchant Bank link failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RegistrationActivity, "Merchant registration failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegistrationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessAndNavigate() {
        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToDashboard()
        }, 800)
    }

    private fun navigateToDashboard() {
        val intent = if (selectedRole == "MERCHANT") {
            Intent(this, MerchantDashboardActivity::class.java)
        } else {
            Intent(this, UserDashboardActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

package com.upimicro.ui.user

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.LinkBankRequest
import com.upimicro.databinding.ActivityLinkBankBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class LinkBankActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLinkBankBinding
    private lateinit var sessionManager: SessionManager

    private val bankList = listOf(
        "State Bank of India",
        "Indian Bank",
        "HDFC Bank",
        "ICICI Bank",
        "Axis Bank",
        "Canara Bank",
        "Punjab National Bank",
        "Bank of Baroda",
        "Kotak Mahindra Bank",
        "Union Bank"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLinkBankBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupBankDropdown()
        setupListeners()

        binding.ifscCode.filters = arrayOf(
            InputFilter.AllCaps(),
            InputFilter.LengthFilter(11)
        )
    }

    private fun setupBankDropdown() {

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            bankList
        )

        binding.bankDropdown.setAdapter(adapter)
    }

    private fun setupListeners() {

        binding.linkBankButton.setOnClickListener {

            val bank = binding.bankDropdown.text.toString()
            val acc = binding.accountNumber.text.toString()
            val confirm = binding.confirmAccountNumber.text.toString()
            val ifsc = binding.ifscCode.text.toString().uppercase()
            val holder = binding.accountHolderName.text.toString()

            if (bank.isEmpty() || acc.isEmpty() || confirm.isEmpty() || ifsc.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (acc != confirm) {
                Toast.makeText(this, "Account numbers do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val accountRegex = Regex("^[0-9]{8,18}$")
            if (!accountRegex.matches(acc)) {
                Toast.makeText(this, "Invalid account number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ifscRegex = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")
            if (!ifscRegex.matches(ifsc)) {
                Toast.makeText(this, "Invalid IFSC code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            linkBank(bank, acc, ifsc, holder)
        }
    }

    private fun linkBank(
        bank: String,
        account: String,
        ifsc: String,
        holder: String
    ) {

        val userId = sessionManager.getUserId()
        val token = sessionManager.getToken()

        if (userId == -1L || token.isNullOrEmpty()) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {

            try {

                val response = RetrofitClient
                    .getApi(this@LinkBankActivity)
                    .linkBank(
                        LinkBankRequest(
                            userId,
                            bank,
                            account,
                            ifsc,
                            holder
                        )
                    )

                if (response.isSuccessful && response.body() != null) {

                    val user = response.body()!!

                    // ✅ FIXED (no ? safe calls)
                    val finalBank = if (user.bankName.isNullOrEmpty()) bank else user.bankName
                    val finalAccount = if (user.accountNumber.isNullOrEmpty()) account else user.accountNumber

                    sessionManager.saveBankLinked(true, finalBank, finalAccount, ifsc)

                    Toast.makeText(
                        this@LinkBankActivity,
                        "Bank Linked Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@LinkBankActivity, UserDashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } else {

                    val errorMsg = response.errorBody()?.string()
                    Log.e("LINK_BANK", "Error: $errorMsg")

                    Toast.makeText(
                        this@LinkBankActivity,
                        "Link Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Log.e("LINK_BANK", "Exception: ${e.message}", e)

                Toast.makeText(
                    this@LinkBankActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

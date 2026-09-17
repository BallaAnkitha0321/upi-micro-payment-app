package com.upimicro.ui.user

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.TransactionRequest
import com.upimicro.databinding.ActivitySendMoneyBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.DialogUtils
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class SendMoneyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendMoneyBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var upiPaymentLauncher: ActivityResultLauncher<Intent>
    private lateinit var pinLauncher: ActivityResultLauncher<Intent>

    private val demoMode = true
    private var currentUpiId: String = ""
    private var currentAmount: Double = 0.0
    private var currentPin: String = ""
    private var userBalance: Double = 0.0
    private var receiverName: String? = null

    private var upiSearchJob: Job? = null
    private var isDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendMoneyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // 🔥 BLOCK CHECK ON ENTRY
        if (sessionManager.isUserBlocked()) {
            showBlockedAndExit()
            return
        }

        fetchUserBalance()
        setupLaunchers()
        setupListeners()
        handleIncomingData()
        setupRealTimeValidation()
        validateInputs()
    }

    // 🔥 SAFE BLOCK HANDLER
    private fun showBlockedAndExit() {
        if (!isDialogShown) {
            isDialogShown = true
            DialogUtils.showBlockedDialog(this)
        }
        finish()
    }

    private fun fetchUserBalance() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@SendMoneyActivity)
                    .getUserBalance(userId)

                if (response.isSuccessful) {
                    userBalance = response.body()?.balance ?: 0.0
                } else {
                    val error = response.errorBody()?.string()
                    if (handleApiError(error)) return@launch
                }

            } catch (e: Exception) {
                Log.e("UPI_DEBUG", "Balance Error: ${e.message}")
            }
        }
    }

    private fun handleIncomingData() {
        val prefillUpi = intent.getStringExtra("PREFILL_UPI")
        val prefillAmount = intent.getStringExtra("PREFILL_AMOUNT")
        val prefillName = intent.getStringExtra("PREFILL_NAME")

        if (!prefillUpi.isNullOrEmpty()) {
            val cleanUpi = prefillUpi.trim().lowercase().replace("+91", "")
            binding.upiEditText.setText(cleanUpi)
            receiverName = prefillName
            updateReceiverUi(cleanUpi, prefillName)
        }

        if (!prefillAmount.isNullOrEmpty()) {
            binding.amountEditText.setText(prefillAmount)
        }

        validateInputs()
    }

    private fun setupRealTimeValidation() {

        binding.upiEditText.addTextChangedListener {
            val upiId = it.toString().trim().lowercase().replace("+91", "")

            upiSearchJob?.cancel()

            if (isValidUpi(upiId)) {
                upiSearchJob = lifecycleScope.launch {
                    delay(400)
                    try {
                        val response = RetrofitClient.getApi(this@SendMoneyActivity)
                            .verifyUpi(upiId)

                        if (response.isSuccessful) {
                            val body = response.body()
                            receiverName = if (body?.isValid == true) body.name else "Merchant"
                            updateReceiverUi(upiId, receiverName)
                        } else {
                            val error = response.errorBody()?.string()
                            if (handleApiError(error)) return@launch
                            updateReceiverUi(upiId, "Merchant")
                        }
                    } catch (e: Exception) {
                        updateReceiverUi(upiId, "Merchant")
                    }
                }
            } else {
                binding.receiverCard.visibility = View.GONE
            }

            validateInputs()
        }

        binding.amountEditText.addTextChangedListener {
            validateInputs()
        }
    }

    private fun updateReceiverUi(upiId: String, name: String?) {
        binding.receiverCard.visibility = View.VISIBLE
        binding.receiverUpiText.text = upiId

        val displayName = name ?: "Merchant"
        binding.receiverNameText.text = displayName
        binding.receiverInitial.text = displayName.take(1).uppercase()

        binding.upiLayout.error = null
    }

    private fun isValidUpi(upiId: String): Boolean {
        return upiId.contains("@")
    }

    private fun validateInputs() {
        val upi = binding.upiEditText.text.toString().trim()
        val amount = binding.amountEditText.text.toString()
            .replace("₹", "")
            .trim()
            .toDoubleOrNull()

        val isValid = upi.isNotEmpty() && amount != null && amount > 0

        binding.payButton.isEnabled = isValid
        binding.payButton.alpha = if (isValid) 1f else 0.5f
    }

    private fun setupListeners() {
        binding.payButton.setOnClickListener {

            if (sessionManager.isUserBlocked()) {
                showBlockedAndExit()
                return@setOnClickListener
            }

            val upiId = binding.upiEditText.text.toString()
                .trim()
                .lowercase()
                .replace("+91", "")

            val amount = binding.amountEditText.text.toString()
                .replace("₹", "")
                .trim()
                .toDoubleOrNull() ?: 0.0

            currentUpiId = upiId
            currentAmount = amount

            showConfirmationDialog(upiId, amount.toString())
        }
    }

    private fun showConfirmationDialog(upiId: String, amount: String) {
        val displayName = receiverName ?: upiId

        AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage("Transfer ₹$amount to $displayName?")
            .setPositiveButton("Proceed") { _, _ ->
                val intent = Intent(this, PinActivity::class.java)
                intent.putExtra("USER_ID", sessionManager.getUserId())
                intent.putExtra(PinActivity.EXTRA_PURPOSE, PinActivity.PURPOSE_PAYMENT)
                pinLauncher.launch(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun launchUpiPayment(upiId: String, amount: String) {

        val nameParam = receiverName ?: upiId.split("@").getOrNull(0) ?: "Recipient"

        val upiUri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", nameParam)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = upiUri

        try {
            val chooser = Intent.createChooser(intent, "Pay with...")
            upiPaymentLauncher.launch(chooser)
        } catch (e: Exception) {
            if (demoMode) showSimulationDialog()
            else Toast.makeText(this, "No UPI app found", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLaunchers() {
        upiPaymentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                showSimulationDialog()
            }

        pinLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    currentPin = result.data?.getStringExtra("PIN") ?: ""
                    launchUpiPayment(currentUpiId, currentAmount.toString())
                }
            }
    }

    private fun showSimulationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Payment Result")
            .setMessage("Select payment outcome")
            .setPositiveButton("SUCCESS") { _, _ ->
                handleUpiResponse("Status=SUCCESS")
            }
            .setNegativeButton("FAILED") { _, _ ->
                handleUpiResponse("Status=FAILURE")
            }
            .setCancelable(false)
            .show()
    }

    private fun handleUpiResponse(response: String?) {

        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        val status =
            if (response?.contains("FAILURE", true) == true) "FAILED" else "SUCCESS"

        Toast.makeText(this, "Payment $status", Toast.LENGTH_SHORT).show()

        saveTransaction(userId, status, response)
    }

    private fun saveTransaction(userId: Long, status: String, rawResponse: String?) {

        val request = TransactionRequest(
            userId = userId,
            merchantUpiId = currentUpiId,
            amount = currentAmount,
            status = status,
            txnRef = "SIM" + System.currentTimeMillis(),
            rawResponse = rawResponse,
            responseCode = if (status == "SUCCESS") "00" else "01",
            pin = currentPin
        )

        lifecycleScope.launch {
            try {

                val response = RetrofitClient.getApi(this@SendMoneyActivity)
                    .payTransaction(request)

                if (response.isSuccessful) {
                    finish()
                } else {
                    val error = response.errorBody()?.string()
                    if (handleApiError(error)) return@launch
                }

            } catch (e: Exception) {
                Log.e("UPI_DEBUG", "Error: ${e.message}")
                finish()
            }
        }
    }

    // 🔥 RETURNS TRUE IF BLOCKED (IMPORTANT)
    private fun handleApiError(errorBody: String?): Boolean {
        val errorMsg = parseError(errorBody)

        if (errorMsg == "ACCOUNT_RESTRICTED") {
            sessionManager.saveBlockedStatus(true)
            showBlockedAndExit()
            return true
        } else {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            return false
        }
    }

    private fun parseError(error: String?): String {
        return try {
            if (error.isNullOrEmpty()) "Something went wrong"
            else {
                val json = JSONObject(error)
                json.optString("message", json.optString("error", "Something went wrong"))
            }
        } catch (e: Exception) {
            "Something went wrong"
        }
    }
}
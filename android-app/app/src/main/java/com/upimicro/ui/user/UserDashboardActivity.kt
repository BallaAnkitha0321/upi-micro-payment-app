package com.upimicro.ui.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.QrVerifyRequest
import com.upimicro.databinding.ActivityUserDashboardBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.ui.auth.SetPinActivity
import com.upimicro.utils.DialogUtils
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var qrScanLauncher: ActivityResultLauncher<Intent>
    private lateinit var pinLauncher: ActivityResultLauncher<Intent>

    private var isDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (sessionManager.getToken().isEmpty()) {
            redirectToLogin()
            return
        }

        if (sessionManager.isUserBlocked()) {
            showBlockedAndExit()
            return
        }

        setupLaunchers()
        setupListeners()
        setupSwipeRefresh()

        updateBalanceUI()
        refreshDashboardData()
    }

    private fun showBlockedAndExit() {
        if (!isDialogShown) {
            isDialogShown = true
            DialogUtils.showBlockedDialog(this)
        }
        finish()
    }

    private fun checkPinStatus(hasUpiPin: Boolean) {
        if (!hasUpiPin && !sessionManager.isPinSet()) {
            val intent = Intent(this, SetPinActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun refreshDashboardData() {
        val name = sessionManager.getUserName()
        binding.userNameText.text = "Welcome, ${if (name.isNotEmpty()) name else "User"}"
        loadUserProfile()
    }

    private fun redirectToLogin() {
        sessionManager.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupLaunchers() {
        qrScanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val qrData = result.data?.getStringExtra("QR_DATA")
                if (!qrData.isNullOrEmpty()) {
                    verifyAndHandleQr(qrData)
                }
            }
        }

        pinLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    }

    private fun verifyAndHandleQr(qrData: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@UserDashboardActivity)
                    .verifyQr(QrVerifyRequest(upiId = qrData))

                if (response.isSuccessful && response.body()?.valid == true) {
                    val data = response.body()
                    navigateToPay(
                        upiId = data?.merchantUpiId ?: qrData,
                        name = data?.merchantName,
                        amount = null,
                        merchantId = data?.merchantId
                    )
                } else {
                    val error = response.errorBody()?.string()
                    if (handleApiError(error)) return@launch
                    handleLocal_qr(qrData)
                }

            } catch (e: Exception) {
                handleLocal_qr(qrData)
            }
        }
    }

    private fun handleLocal_qr(qrData: String) {
        try {
            val uri = Uri.parse(qrData)

            if (uri.scheme == "upi") {
                val upiId = uri.getQueryParameter("pa")
                val amount = uri.getQueryParameter("am")
                val name = uri.getQueryParameter("pn")

                if (!upiId.isNullOrEmpty()) {
                    navigateToPay(upiId, name, amount, null)
                } else {
                    Toast.makeText(this, "Invalid UPI QR", Toast.LENGTH_SHORT).show()
                }

            } else if (qrData.contains("@")) {
                navigateToPay(qrData, null, null, null)
            } else {
                Toast.makeText(this, "Unrecognized QR format", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Failed to parse QR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToPay(upiId: String, name: String?, amount: String?, merchantId: Long?) {
        if (sessionManager.isUserBlocked()) {
            showBlockedAndExit()
            return
        }

        val intent = Intent(this, SendMoneyActivity::class.java)
        intent.putExtra("PREFILL_UPI", upiId)
        intent.putExtra("PREFILL_NAME", name)
        intent.putExtra("PREFILL_AMOUNT", amount)
        merchantId?.let { intent.putExtra("MERCHANT_ID", it) }
        startActivity(intent)
    }

    private fun loadUserProfile() {
        val userId = sessionManager.getUserId()

        if (userId == 0L) {
            redirectToLogin()
            return
        }

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@UserDashboardActivity)

                // ✅ FIXED (NO TOKEN HERE)
                val statusResponse = api.getUserStatus(userId)

                if (statusResponse.code() == 401) {
                    redirectToLogin()
                    return@launch
                }

                if (!statusResponse.isSuccessful) {
                    val error = statusResponse.errorBody()?.string()
                    if (handleApiError(error)) return@launch
                }

                if (statusResponse.isSuccessful && statusResponse.body() != null) {
                    val status = statusResponse.body()!!

                    if (status.hasUpiPin) {
                        sessionManager.savePinSet(true)
                    }

                    checkPinStatus(status.hasUpiPin)
                }

                // ✅ FIXED (NO TOKEN HERE)
                val profileResponse = api.getUserProfile()

                if (profileResponse.code() == 401) {
                    redirectToLogin()
                    return@launch
                }

                if (!profileResponse.isSuccessful) {
                    val error = profileResponse.errorBody()?.string()
                    if (handleApiError(error)) return@launch
                }

                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val user = profileResponse.body()!!

                    sessionManager.saveBlockedStatus(user.blocked)

                    if (user.blocked) {
                        showBlockedAndExit()
                        return@launch
                    }

                    sessionManager.saveUserSession(
                        user.userId,
                        user.name ?: "",
                        user.email ?: "",
                        user.upiId ?: ""
                    )

                    sessionManager.saveBankLinked(
                        user.bankLinked,
                        user.bankName ?: "",
                        user.accountNumber ?: "",
                        user.ifscCode ?: ""
                    )

                    sessionManager.saveKycVerified(user.kycVerified == true)

                    binding.userNameText.text = "Welcome, ${user.name ?: "User"}"
                }

            } catch (e: Exception) {
                Log.e("DASHBOARD", "Exception", e)
            }
        }
    }

    private fun updateBalanceUI() {
        binding.balanceText.text = "Check Balance"
        binding.toggleBalance.text = "View"
    }

    private fun setupListeners() {

        binding.profileContainer.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        binding.historyAction.setOnClickListener {
            startActivity(Intent(this, TransactionHistoryActivity::class.java))
        }

        binding.scanPayAction.setOnClickListener {
            if (sessionManager.isUserBlocked()) {
                showBlockedAndExit()
                return@setOnClickListener
            }
            qrScanLauncher.launch(Intent(this, ScanQRActivity::class.java))
        }

        binding.sendMoneyAction.setOnClickListener {
            if (sessionManager.isUserBlocked()) {
                showBlockedAndExit()
                return@setOnClickListener
            }
            startActivity(Intent(this, SendMoneyActivity::class.java))
        }

        binding.toggleBalance.setOnClickListener {
            val intent = Intent(this, PinActivity::class.java)
            intent.putExtra("USER_ID", sessionManager.getUserId())
            pinLauncher.launch(intent)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            refreshDashboardData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()

        if (sessionManager.getToken().isEmpty()) {
            redirectToLogin()
            return
        }

        if (sessionManager.isUserBlocked()) {
            showBlockedAndExit()
            return
        }

        refreshDashboardData()
        updateBalanceUI()
    }

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
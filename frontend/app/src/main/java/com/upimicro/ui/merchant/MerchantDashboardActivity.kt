package com.upimicro.ui.merchant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.R
import com.upimicro.data.model.TransactionModel
import com.upimicro.databinding.ActivityMerchantDashboardBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.ui.user.UserDashboardActivity
import com.upimicro.utils.DialogUtils
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class MerchantDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != "MERCHANT") {
            redirectToLogin()
            return
        }

        setupUI()
        setupClicks()
        loadAllData()
    }

    private fun setupClicks() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_qr -> {
                    if (sessionManager.isUserBlocked()) {
                        DialogUtils.showBlockedDialog(this)
                        false
                    } else {
                        val intent = Intent(this, QRDisplayActivity::class.java)
                        intent.putExtra("shopName", sessionManager.getMerchantName())
                        intent.putExtra("upiId", sessionManager.getMerchantUpi())
                        startActivity(intent)
                        true
                    }
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, MerchantTransactionHistoryActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, MerchantProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        binding.merchantProfileCard.setOnClickListener {
            startActivity(Intent(this, MerchantProfileActivity::class.java))
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadAllData()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.revenueCard.setOnClickListener {
            startActivity(Intent(this, MerchantTransactionHistoryActivity::class.java))
        }
    }

    private fun loadAllData() {
        fetchMerchantProfile()
        loadDashboardData()
        fetchTransactions()
        checkBankStatus()
    }

    private fun fetchMerchantProfile() {
        val token = sessionManager.getToken()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@MerchantDashboardActivity)
                    .getMerchantProfile()

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    
                    sessionManager.saveBlockedStatus(profile.blocked)
                    if (profile.blocked) {
                        DialogUtils.showBlockedDialog(this@MerchantDashboardActivity)
                    }

                    val displayName = profile.businessName ?: profile.name
                    binding.merchantNameText.text = displayName?.ifEmpty { "Merchant" } ?: "Merchant"
                    binding.merchantUpiText.text = profile.upiId?.ifEmpty { "UPI not set" } ?: "UPI not set"
                    
                    sessionManager.saveMerchantSession(
                        profile.merchantId ?: -1L,
                        profile.name ?: "",
                        profile.email ?: "",
                        profile.upiId ?: ""
                    )
                } else if (response.code() == 401) {
                    handleSessionExpired()
                }
            } catch (e: Exception) {
                Log.e("DASHBOARD", "Profile error", e)
            }
        }
    }

    private fun loadDashboardData() {
        val token = sessionManager.getToken()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@MerchantDashboardActivity)
                    .getMerchantDashboard()

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    binding.totalRevenueText.text = "₹${String.format(Locale.US, "%.2f", data.todayEarnings ?: 0.0)}"
                    binding.totalEarningsText.text = "Total: ₹${String.format(Locale.US, "%.2f", data.totalEarnings ?: 0.0)}"
                    binding.totalTxnText.text = "Transactions: ${data.totalTransactions ?: 0}"
                } else if (response.code() == 401) {
                    handleSessionExpired()
                }
            } catch (e: Exception) {
                Log.e("DASHBOARD", "Dashboard error", e)
            }
        }
    }

    private fun fetchTransactions() {
        val merchantId = sessionManager.getMerchantId()
        if (merchantId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient
                    .getApi(this@MerchantDashboardActivity)
                    .getTodayMerchantTransactions(merchantId)

                if (response.isSuccessful && response.body() != null) {
                    val transactions = response.body()?.data ?: emptyList()
                    showTodayPayments(transactions)
                }
            } catch (e: Exception) {
                Log.e("DASHBOARD", "Transaction error", e)
            }
        }
    }

    private fun showTodayPayments(list: List<TransactionModel>) {
        binding.todayPaymentsContainer.removeAllViews()
        if (list.isEmpty()) {
            binding.emptyStateText.visibility = View.VISIBLE
            return
        }
        binding.emptyStateText.visibility = View.GONE
        list.take(10).forEach { txn ->
            val view = layoutInflater.inflate(R.layout.item_today_payment, null)
            view.findViewById<TextView>(R.id.amountText).text = "₹${txn.amount}"
            view.findViewById<TextView>(R.id.userText).text = "From: ${txn.senderName ?: "Customer"}"
            view.findViewById<TextView>(R.id.timeText).text = 
                txn.createdAt.takeIf { it.length >= 16 }?.substring(11, 16) ?: "--:--"
            binding.todayPaymentsContainer.addView(view)
        }
    }

    private fun checkBankStatus() {
        val token = sessionManager.getToken()

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@MerchantDashboardActivity)
                    .getMerchantStatus()

                if (response.isSuccessful && response.body() != null) {
                    val status = response.body()!!
                    binding.bannerLinkBank.visibility = if (!status.bankLinked) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                Log.e("DASHBOARD", "Bank status error", e)
            }
        }
    }

    private fun setupUI() {
        binding.merchantNameText.text = sessionManager.getMerchantName().ifEmpty { "Merchant"}
        binding.merchantUpiText.text = sessionManager.getMerchantUpi().ifEmpty { "UPI not set"}

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.headerGreeting.text = when {
            hour < 12 -> "Good Morning,"
            hour < 17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
    }

    private fun handleSessionExpired() {
        Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
        sessionManager.logout()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin()
        } else {
            binding.bottomNav.selectedItemId = R.id.nav_home
            loadAllData()
            if (sessionManager.isUserBlocked()) {
                DialogUtils.showBlockedDialog(this)
            }
        }
    }
}

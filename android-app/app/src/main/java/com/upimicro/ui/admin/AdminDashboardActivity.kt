package com.upimicro.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.databinding.ActivityAdminDashboardBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        initViews()
        setupClicks()
        loadDashboard()

        binding.swipeRefresh.setOnRefreshListener {
            loadDashboard()
        }
    }

    private fun initViews() {
        binding.layoutUsers.tvTitle.text = "Total Users"
        binding.layoutMerchants.tvTitle.text = "Merchants"
        binding.layoutTransactions.tvTitle.text = "Transactions"
        binding.layoutRevenue.tvTitle.text = "Total Revenue"
    }

    private fun loadDashboard() {

        val token = sessionManager.getToken()

        if (token.isNullOrEmpty()) {
            showToast("Session expired. Please login again.")
            redirectToLogin()
            return
        }

        Log.d("ADMIN_API", "TOKEN = $token")

        binding.swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@AdminDashboardActivity)

                // ✅ FIX: Removed manual token argument as it's handled by Retrofit Interceptor
                val response = api.getAdminDashboard()

                if (response.isSuccessful) {

                    val data = response.body()

                    if (data != null) {
                        binding.layoutUsers.tvValue.text = data.totalUsers.toString()
                        binding.layoutMerchants.tvValue.text = data.totalMerchants.toString()
                        binding.layoutTransactions.tvValue.text = data.totalTransactions.toString()
                        binding.layoutRevenue.tvValue.text = "₹${data.totalRevenue.toInt()}"
                    } else {
                        showToast("No dashboard data available")
                    }

                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    showToast(errorMsg)
                }

            } catch (e: Exception) {
                Log.e("ADMIN_ERROR", e.message ?: "Unknown error")
                showToast("Something went wrong. Please try again.")
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    // 🔥 FIX: Clean error parsing (no ugly JSON in toast)
    private fun parseError(error: String?): String {
        return try {
            if (error.isNullOrEmpty()) {
                "Request failed"
            } else {
                val json = JSONObject(error)
                json.optString("message", "Request failed")
            }
        } catch (e: Exception) {
            "Request failed"
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, AdminLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupClicks() {

        binding.layoutUsers.root.setOnClickListener {
            startActivity(Intent(this, UserAnalyticsActivity::class.java))
        }

        binding.layoutMerchants.root.setOnClickListener {
            startActivity(Intent(this, MerchantAnalyticsActivity::class.java))
        }

        binding.layoutTransactions.root.setOnClickListener {
            startActivity(Intent(this, TransactionAnalyticsActivity::class.java))
        }

        binding.layoutRevenue.root.setOnClickListener {
            startActivity(Intent(this, RevenueAnalyticsActivity::class.java))
        }

        binding.cardFraud.setOnClickListener {
            startActivity(Intent(this, FraudAlertsActivity::class.java))
        }

        binding.btnManageUsers.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        binding.btnManageMerchants.setOnClickListener {
            startActivity(Intent(this, MerchantManagementActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            redirectToLogin()
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

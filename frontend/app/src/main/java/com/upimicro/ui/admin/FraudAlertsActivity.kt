package com.upimicro.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.upimicro.databinding.ActivityFraudAlertsBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class FraudAlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFraudAlertsBinding
    private lateinit var adapter: FraudTransactionAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFraudAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        loadFraudAlerts()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = FraudTransactionAdapter()
        binding.rvFraudAlerts.apply {
            layoutManager = LinearLayoutManager(this@FraudAlertsActivity)
            adapter = this@FraudAlertsActivity.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadFraudAlerts()
        }
    }

    private fun loadFraudAlerts() {
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.swipeRefresh.isRefreshing = true
                val api = RetrofitClient.getApi(this@FraudAlertsActivity)
                val response = api.getFraudTransactions()

                if (response.isSuccessful) {
                    val alerts = response.body() ?: emptyList()
                    adapter.submitList(alerts)
                    
                    binding.tvFraudCount.text = "${alerts.size} Alerts Found"
                    binding.tvEmptyState.visibility = if (alerts.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(this@FraudAlertsActivity, "Failed to load alerts", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@FraudAlertsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}

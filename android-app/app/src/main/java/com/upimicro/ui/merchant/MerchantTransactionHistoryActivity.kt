package com.upimicro.ui.merchant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.upimicro.data.model.TransactionModel
import com.upimicro.databinding.ActivityMerchantTransactionHistoryBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class MerchantTransactionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantTransactionHistoryBinding
    private lateinit var adapter: MerchantTransactionAdapter
    private lateinit var sessionManager: SessionManager
    private var allTransactions: List<TransactionModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFilters()

        loadTransactions()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = MerchantTransactionAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadTransactions()
        }
    }

    private fun setupFilters() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            
            when (checkedIds.first()) {
                binding.chipAll.id -> filterTransactions("ALL")
                binding.chipSuccess.id -> filterTransactions("SUCCESS")
                binding.chipFailed.id -> filterTransactions("FAILED")
            }
        }
    }

    private fun filterTransactions(status: String) {
        val filteredList = if (status == "ALL") {
            allTransactions
        } else {
            allTransactions.filter { it.status.equals(status, ignoreCase = true) }
        }
        
        adapter.submitList(filteredList)
        updateEmptyState(filteredList.isEmpty(), status)
    }

    private fun updateEmptyState(isEmpty: Boolean, filter: String) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        
        if (isEmpty) {
            when (filter) {
                "SUCCESS" -> {
                    binding.emptyStateTitle.text = "No successful payments"
                    binding.emptyStateSubtext.text = "You haven't received any successful payments yet"
                }
                "FAILED" -> {
                    binding.emptyStateTitle.text = "No failed payments"
                    binding.emptyStateSubtext.text = "You don't have any failed transactions"
                }
                else -> {
                    binding.emptyStateTitle.text = "No payments yet"
                    binding.emptyStateSubtext.text = "Start accepting payments using QR"
                }
            }
        }
    }

    private fun loadTransactions() {
        val token = sessionManager.getToken()
        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            handleSessionExpired()
            return
        }

        val merchantId = sessionManager.getMerchantId()
        
        Log.d("MERCHANT_DEBUG", "merchantId used = $merchantId")

        if (merchantId == -1L) {
             binding.swipeRefresh.isRefreshing = false
             Toast.makeText(this, "Invalid merchant session", Toast.LENGTH_SHORT).show()
             return
        }

        binding.swipeRefresh.isRefreshing = true
        binding.emptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient
                    .getApi(this@MerchantTransactionHistoryActivity)
                    .getMerchantTransactions(merchantId)

                if (response.isSuccessful && response.body() != null) {
                    // FIXED: Access .data from ApiResponse wrapper to avoid type mismatch
                    allTransactions = response.body()?.data ?: emptyList()
                    Log.d("HISTORY_DATA", "Loaded ${allTransactions.size} transactions")

                    val currentFilter = when (binding.filterChipGroup.checkedChipId) {
                        binding.chipSuccess.id -> "SUCCESS"
                        binding.chipFailed.id -> "FAILED"
                        else -> "ALL"
                    }
                    
                    filterTransactions(currentFilter)

                } else if (response.code() == 401) {
                    handleSessionExpired()
                } else {
                    Log.e("HISTORY", "API Error: ${response.code()}")
                    Toast.makeText(this@MerchantTransactionHistoryActivity, "Failed to load history", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("HISTORY", "Error: ${e.message}", e)
                Toast.makeText(this@MerchantTransactionHistoryActivity, "Connection error", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun handleSessionExpired() {
        sessionManager.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

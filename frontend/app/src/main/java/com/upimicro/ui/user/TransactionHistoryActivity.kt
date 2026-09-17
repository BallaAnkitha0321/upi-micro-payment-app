package com.upimicro.ui.user

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.upimicro.databinding.ActivityTransactionHistoryBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import com.upimicro.data.model.ApiResponse
import com.upimicro.data.model.TransactionModel
import kotlinx.coroutines.launch
import retrofit2.Response

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var adapter: TransactionAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        loadTransactions()
    }

    private fun setupRecyclerView() {

        adapter = TransactionAdapter()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun loadTransactions() {

        val userId = sessionManager.getUserId()

        Log.d("TXN_DEBUG", "Fetching for userId = $userId")

        if (userId == -1L) {
            Toast.makeText(this, "Invalid session", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {

                val response: Response<ApiResponse<List<TransactionModel>>> =
                    RetrofitClient
                        .getApi(this@TransactionHistoryActivity)
                        .getUserTransactions(userId)

                Log.d("TXN_DEBUG", "Response Code = ${response.code()}")

                if (response.isSuccessful) {

                    val body = response.body()

                    Log.d("TXN_DEBUG", "Full Response = $body")

                    val transactions: List<TransactionModel> =
                        body?.data ?: emptyList()

                    Log.d("TXN_DEBUG", "Transactions size = ${transactions.size}")

                    runOnUiThread {

                        // ✅ FIX: No null, direct list
                        adapter.submitList(transactions)

                        binding.recyclerView.visibility = View.VISIBLE
                    }

                    if (transactions.isEmpty()) {
                        Toast.makeText(
                            this@TransactionHistoryActivity,
                            "No transactions found",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@TransactionHistoryActivity,
                            "Loaded ${transactions.size} transactions",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {

                    Log.e("TXN_ERROR", "API Error: ${response.errorBody()?.string()}")

                    Toast.makeText(
                        this@TransactionHistoryActivity,
                        "Failed to load transactions",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Log.e("TXN_ERROR", "Error: ${e.message}", e)

                Toast.makeText(
                    this@TransactionHistoryActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
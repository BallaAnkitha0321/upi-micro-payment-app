package com.upimicro.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.*
import com.upimicro.databinding.ActivityQrAnalyticsBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import com.upimicro.data.model.DailyTransactionResponse


class QrAnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrAnalyticsBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQrAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        loadQrAnalytics()
    }

    private fun loadQrAnalytics() {
        val token = sessionManager.getToken()
        if (token.isEmpty()) return

        lifecycleScope.launch {

            try {
                val api = RetrofitClient.getApi(this@QrAnalyticsActivity)
                val response = api.getQrDaily()

                if (response.isSuccessful && response.body() != null) {
                    setupChart(response.body()!!)
                }

            } catch (e: Exception) {
                Toast.makeText(this@QrAnalyticsActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupChart(data: List<DailyTransactionResponse>) {

        val entries = ArrayList<Entry>()

        data.forEachIndexed { index, item ->
            val count = (item.count as? Number)?.toFloat() ?: 0f
            entries.add(Entry(index.toFloat(), count))
        }

        val dataSet = LineDataSet(entries, "QR Payments")
        dataSet.color = Color.parseColor("#FF9800")

        binding.qrChart.data = LineData(dataSet)
        binding.qrChart.invalidate()
    }
}

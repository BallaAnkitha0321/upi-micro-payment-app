package com.upimicro.ui.admin

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.upimicro.data.model.DailyTransactionResponse
import com.upimicro.data.model.TransactionKpiResponse
import com.upimicro.data.model.TransactionStatusResponse
import com.upimicro.databinding.ActivityTransactionAnalyticsBinding
import com.upimicro.databinding.ItemKpiCardBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Locale

class TransactionAnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionAnalyticsBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        window.statusBarColor = Color.parseColor("#F8F9FD")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        loadAllData()
    }

    private fun loadAllData() {
        val token = sessionManager.getToken()
        if (token.isEmpty()) return

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@TransactionAnalyticsActivity)
                
                // Fetch all data
                val kpiResponse = api.getTransactionKpis()
                val dailyResponse = api.getDailyTransactions()
                
                if (kpiResponse.isSuccessful && dailyResponse.isSuccessful) {
                    val kpi = kpiResponse.body()
                    val daily = dailyResponse.body() ?: emptyList()
                    
                    if (kpi != null) {
                        setupKpis(kpi)
                        
                        // ✅ Fix: Populate Pie Chart using KPI data
                        val statusData = mutableListOf<TransactionStatusResponse>()
                        statusData.add(TransactionStatusResponse("Success", kpi.successTransactions))
                        statusData.add(TransactionStatusResponse("Failed", kpi.failedTransactions))
                        
                        val pending = kpi.totalTransactions - (kpi.successTransactions + kpi.failedTransactions)
                        if (pending > 0) {
                            statusData.add(TransactionStatusResponse("Pending", pending))
                        }
                        
                        setupPieChart(statusData)
                    }
                    setupLineChart(binding.lineChart, daily, "Daily Transactions", "#1976D2")
                } else {
                    Toast.makeText(this@TransactionAnalyticsActivity, "Failed to load analytics data", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@TransactionAnalyticsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupKpis(data: TransactionKpiResponse) {
        // Total Transactions - Blue
        styleKpiCard(
            binding.totalTransactionsCard,
            "Total Txns",
            data.totalTransactions.toString(),
            Color.parseColor("#1E88E5")
        )

        // Failed - Red
        styleKpiCard(
            binding.failedTransactionsCard,
            "Failed",
            data.failedTransactions.toString(),
            Color.parseColor("#F44336")
        )

        // Success - Green
        styleKpiCard(
            binding.successTransactionsCard,
            "Success",
            data.successTransactions.toString(),
            Color.parseColor("#2E7D32")
        )

        updateInsight(data)
    }

    private fun updateInsight(data: TransactionKpiResponse) {
        binding.tvInsight.text = if (data.insight.isNullOrBlank()) "System stable" else data.insight
        val (bgColor, textColor) = when {
            data.successRate >= 90 -> Pair("#E8F5E9", "#2E7D32")
            data.successRate >= 70 -> Pair("#FFF3E0", "#EF6C00")
            else -> Pair("#FFEBEE", "#C62828")
        }
        binding.cvInsight.setCardBackgroundColor(Color.parseColor(bgColor))
        binding.tvInsight.setTextColor(Color.parseColor(textColor))
    }

    private fun styleKpiCard(cardBinding: ItemKpiCardBinding, title: String, value: String, color: Int) {
        cardBinding.tvTitle.text = title
        cardBinding.tvValue.text = value
        cardBinding.tvValue.setTextColor(color)
    }

    private fun setupLineChart(chart: LineChart, data: List<DailyTransactionResponse>, label: String, themeColor: String) {
        if (data.isEmpty()) {
            chart.clear()
            chart.setNoDataText("No data available")
            return
        }

        val sortedData = data.sortedBy { it.date }
        val entries = sortedData.mapIndexed { index, item -> Entry(index.toFloat(), item.count.toFloat()) }
        val labels = sortedData.map { 
            val parts = it.date.split("-")
            if (parts.size >= 3) "${parts[2]}/${parts[1]}" else it.date.takeLast(5)
        }

        val dataSet = LineDataSet(entries, label).apply {
            color = Color.parseColor(themeColor)
            setCircleColor(Color.parseColor(themeColor))
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(true)
            circleHoleColor = Color.WHITE
            circleHoleRadius = 2.5f
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            
            // Show integer values
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = value.toInt().toString()
            }
            
            setDrawFilled(true)
            fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#BBDEFB"), Color.TRANSPARENT)
            )
        }

        chart.apply {
            this.data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBorders(false)
            setExtraOffsets(10f, 10f, 10f, 10f)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
                textColor = Color.GRAY
                yOffset = 10f
                labelCount = sortedData.size
            }

            axisLeft.apply {
                setDrawGridLines(false)
                setDrawAxisLine(true)
                textColor = Color.GRAY
                axisMinimum = 0f
                // Show integer values on axis
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = value.toInt().toString()
                }
            }

            axisRight.isEnabled = false
            animateX(800)
            invalidate()
        }
    }

    private fun setupPieChart(data: List<TransactionStatusResponse>) {
        if (data.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.setNoDataText("No status data")
            return
        }

        val entries = data.map { PieEntry(it.count.toFloat(), it.status) }

        val dataSet = PieDataSet(entries, "").apply {
            val colors = data.map {
                when (it.status.uppercase()) {
                    "SUCCESS" -> Color.parseColor("#4CAF50")
                    "FAILED" -> Color.parseColor("#F44336")
                    else -> Color.parseColor("#FFC107")
                }
            }
            this.colors = colors
            sliceSpace = 3f
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(binding.pieChart)
        }

        binding.pieChart.apply {
            this.data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 60f
            transparentCircleRadius = 65f
            
            centerText = "Transaction\nStatus"
            setCenterTextSize(14f)
            setCenterTextColor(Color.parseColor("#455A64"))
            
            legend.isEnabled = true
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            
            animateY(1000)
            invalidate()
        }
    }
}

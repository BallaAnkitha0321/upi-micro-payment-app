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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.upimicro.data.model.RevenueResponse
import com.upimicro.databinding.ActivityRevenueAnalyticsBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Locale

class RevenueAnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRevenueAnalyticsBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevenueAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Setup UI
        window.statusBarColor = Color.parseColor("#F5F7FA")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        loadRevenueData()
    }

    private fun loadRevenueData() {
        val token = sessionManager.getToken()
        if (token.isEmpty()) return

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@RevenueAnalyticsActivity)
                val response = api.getRevenueAnalytics()

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()

                    // ✅ Use 'count' for calculation as it contains the revenue amount in this backend
                    val totalRevenue = data.sumOf { it.count.toDouble() }
                    binding.tvTotalRevenue.text =
                        String.format(Locale.getDefault(), "₹%.0f", totalRevenue)

                    // 2. Latest Revenue (last item)
                    if (data.isNotEmpty()) {
                        val latestAmount = data.last().count.toDouble()
                        binding.tvLatestRevenue.text =
                            String.format(Locale.getDefault(), "₹%.0f", latestAmount)
                    } else {
                        binding.tvTotalRevenue.text = "₹0"
                        binding.tvLatestRevenue.text = "₹0"
                    }

                    // 3. Setup Chart
                    setupChart(data)

                } else {
                    Toast.makeText(
                        this@RevenueAnalyticsActivity,
                        "Failed to fetch data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@RevenueAnalyticsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupChart(data: List<RevenueResponse>) {
        val chart: LineChart = binding.revenueChart
        
        if (data.isEmpty()) {
            chart.clear()
            chart.setNoDataText("No revenue data available")
            chart.invalidate()
            return
        }

        // Sort data by date
        val sortedData = data.sortedBy { it.date }

        val entries = sortedData.mapIndexed { index, item ->
            // ✅ Use 'count' as the value for the Y axis
            val value = item.count.toFloat()
            Entry(index.toFloat(), value)
        }

        val labels = sortedData.map { 
            val parts = it.date.split("-")
            if (parts.size >= 3) "${parts[2]}/${parts[1]}" else it.date.takeLast(5)
        }

        val dataSet = LineDataSet(entries, "Revenue trend").apply {
            color = Color.parseColor("#1E88E5")
            setCircleColor(Color.parseColor("#1E88E5"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(true)
            circleHoleColor = Color.WHITE
            circleHoleRadius = 2.5f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            
            // Value labels
            valueTextSize = 10f
            valueTextColor = Color.DKGRAY
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = "₹${value.toInt()}"
            }

            // Fill
            setDrawFilled(true)
            fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#BBDEFB"), Color.TRANSPARENT)
            )
        }

        chart.apply {
            this.data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setExtraOffsets(10f, 20f, 10f, 10f)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
                textColor = Color.GRAY
                yOffset = 10f
                labelCount = sortedData.size
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#EEEEEE")
                textColor = Color.GRAY
                axisMinimum = 0f
                xOffset = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String = "₹${value.toInt()}"
                }
            }

            axisRight.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }
}

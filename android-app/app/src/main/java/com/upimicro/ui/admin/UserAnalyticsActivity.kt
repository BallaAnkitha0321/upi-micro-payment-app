package com.upimicro.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.upimicro.data.model.DailyUserResponse
import com.upimicro.data.model.UserInsightsResponse
import com.upimicro.databinding.ActivityUserAnalyticsBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class UserAnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAnalyticsBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        loadUserAnalytics()
    }

    // ================= LOAD DATA =================
    private fun loadUserAnalytics() {
        val token = sessionManager.getToken()
        if (token.isEmpty()) return

        lifecycleScope.launch {

            try {
                val api = RetrofitClient.getApi(this@UserAnalyticsActivity)

                val insightsResponse = api.getUserInsights()
                val userResponse = api.getUserRegistrations()

                // ✅ KPI DATA
                if (insightsResponse.isSuccessful && insightsResponse.body() != null) {
                    bindInsights(insightsResponse.body()!!)
                } else {
                    Toast.makeText(this@UserAnalyticsActivity, "Insights API failed", Toast.LENGTH_SHORT).show()
                }

                // ✅ GRAPH DATA
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    setupUserChart(userResponse.body()!!)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@UserAnalyticsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ================= KPI =================
    private fun bindInsights(data: UserInsightsResponse) {

        binding.txtTotalUsers.text = data.totalUsers.toString()
        binding.txtActiveUsers.text = data.activeUsers.toString()
        binding.txtConversion.text = "${data.conversionRate}%"

        // ✅ CLEAN ACTIVE USERS (NO PERCENTAGE)
        binding.txtActiveSubText.text =
            when {
                data.activeUsers == 0L ->
                    "No active users"

                data.activeUsers == 1L ->
                    "1 user active"

                else ->
                    "${data.activeUsers} users active"
            }

        // ✅ INSIGHT
        binding.txtInsight.text =
            when {
                data.conversionRate < 20 -> "Low engagement ⚠️"
                data.conversionRate < 50 -> "Moderate growth 👍"
                else -> "Strong growth 🚀"
            }
    }

    // ================= GRAPH =================
    private fun setupUserChart(data: List<DailyUserResponse>) {

        if (data.isEmpty()) return

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        data.forEachIndexed { index, item ->

            entries.add(Entry(index.toFloat(), item.count.toFloat()))

            val parts = item.date.split("-")
            val label = if (parts.size == 3) "${parts[2]}/${parts[1]}" else item.date
            labels.add(label)
        }

        val dataSet = LineDataSet(entries, "New Users per Day")

        // 🎨 FINTECH STYLE
        dataSet.color = Color.parseColor("#2962FF")
        dataSet.setCircleColor(Color.parseColor("#2962FF"))
        dataSet.lineWidth = 3f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#2962FF")
        dataSet.fillAlpha = 40
        dataSet.valueTextSize = 11f

        // 🔥 REMOVE DECIMALS
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        // 🔥 CIRCLE STYLE
        dataSet.circleRadius = 5f
        dataSet.circleHoleRadius = 2f
        dataSet.setDrawCircleHole(true)

        val lineData = LineData(dataSet)
        binding.userChart.data = lineData

        // ================= X AXIS =================
        binding.userChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            labelRotationAngle = -45f
            setDrawGridLines(false)
            setLabelCount(labels.size, true)
        }

        // ================= Y AXIS =================
        binding.userChart.axisRight.isEnabled = false

        binding.userChart.axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        binding.userChart.apply {
            description.isEnabled = false
            setExtraOffsets(10f, 10f, 10f, 30f)
            animateX(1200)
            invalidate()
        }
    }
}

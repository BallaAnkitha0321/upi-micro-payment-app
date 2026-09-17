package com.upimicro.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.upimicro.data.model.MerchantKpiResponse
import com.upimicro.data.model.MerchantModel
import com.upimicro.databinding.ActivityMerchantAnalyticsBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.admin.adapter.TopMerchantAdapter
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class MerchantAnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMerchantAnalyticsBinding
    private lateinit var adapter: TopMerchantAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMerchantAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecycler()
        fetchMerchantKpis()
        fetchTopMerchants()
    }

    // ================= RECYCLER =================
    private fun setupRecycler() {
        adapter = TopMerchantAdapter(emptyList())

        binding.recyclerTopMerchants.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerTopMerchants.adapter = adapter
    }

    // ================= KPI =================
    private fun fetchMerchantKpis() {
        val token = sessionManager.getToken()
        if (token.isEmpty()) return

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@MerchantAnalyticsActivity)
                val response = api.getMerchantKpis()

                if (response.isSuccessful && response.body() != null) {
                    bindKpis(response.body()!!)
                } else {
                    Toast.makeText(
                        this@MerchantAnalyticsActivity,
                        "Failed to load KPIs",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@MerchantAnalyticsActivity,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun bindKpis(data: MerchantKpiResponse) {
        binding.tvTotalMerchants.text = data.totalMerchants.toString()
        binding.tvActiveMerchants.text = data.activeMerchants.toString()

        val conversionRate = if (data.totalMerchants > 0) {
            (data.activeMerchants.toDouble() / data.totalMerchants) * 100
        } else 0.0

        binding.tvConversion.text = String.format("%.2f%%", conversionRate)

        binding.tvInsight.text = when {
            conversionRate >= 70 -> "Excellent merchant growth 🚀"
            conversionRate >= 40 -> "Moderate growth 👍"
            else -> "Needs improvement ⚠️"
        }
    }

    // ================= TOP MERCHANTS =================
    private fun fetchTopMerchants() {
        val token = sessionManager.getToken()
        if (token.isEmpty()) return

        lifecycleScope.launch {

            binding.recyclerTopMerchants.visibility = View.GONE

            try {
                val api = RetrofitClient.getApi(this@MerchantAnalyticsActivity)
                val response = api.getTopMerchants()

                if (response.isSuccessful && response.body() != null) {

                    val data: List<MerchantModel> = response.body()!!

                    // ✅ Use actual data from MerchantModel (Total Earnings)
                    val mappedList = data.map { merchant ->
                        mapOf(
                            "merchantId" to merchant.merchantId,
                            "name" to (merchant.name ?: "Unknown"),
                            "upiId" to (merchant.upiId ?: ""),
                            "amount" to (merchant.totalEarnings ?: 0.0)
                        )
                    }

                    val sortedList = mappedList.sortedByDescending {
                        (it["amount"] as Double)
                    }

                    val top3 = sortedList.take(3)
                    val top10 = sortedList.take(10)

                    adapter.updateData(top10)
                    binding.recyclerTopMerchants.visibility = View.VISIBLE

                    setupBarChart(top3)

                } else {
                    Toast.makeText(
                        this@MerchantAnalyticsActivity,
                        "Failed to load merchants",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@MerchantAnalyticsActivity,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ================= CHART =================
    private fun setupBarChart(list: List<Map<String, Any>>) {

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        list.forEachIndexed { index, merchant ->

            val amount = merchant["amount"] as Double
            val name = merchant["name"].toString()

            entries.add(BarEntry(index.toFloat(), amount.toFloat()))

            val shortName = if (name.length > 10) {
                name.take(10) + "…"
            } else name

            labels.add(shortName)
        }

        val dataSet = BarDataSet(entries, "")
        dataSet.color = Color.parseColor("#2E7D32")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry?): String {
                return "₹${barEntry?.y?.toInt() ?: 0}"
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        val chart = binding.barChart
        chart.data = barData

        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val leftAxis = chart.axisLeft
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${value.toInt()}"
            }
        }

        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.animateY(800)
        chart.invalidate()
    }
}

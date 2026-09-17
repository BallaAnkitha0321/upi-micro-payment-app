package com.upimicro.ui.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.upimicro.R
import java.text.NumberFormat
import java.util.Locale

class TopMerchantAdapter(
    private var list: List<Map<String, Any>>
) : RecyclerView.Adapter<TopMerchantAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView = view.findViewById(R.id.txtRank)
        val merchantName: TextView = view.findViewById(R.id.txtMerchantName)
        val amount: TextView = view.findViewById(R.id.txtAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_merchant, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<Map<String, Any>>) {
        this.list = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        // ✅ Rank
        val rank = position + 1
        holder.rank.text = "#$rank"

        // ✅ Merchant Name (UPDATED)
        val name = item["name"]?.toString() ?: "Unknown Merchant"
        holder.merchantName.text = name

        // ✅ Amount (SAFE + FORMATTED ₹)
        val amountValue = (item["amount"] as? Number)?.toDouble() ?: 0.0

        val formattedAmount = NumberFormat
            .getNumberInstance(Locale("en", "IN"))
            .format(amountValue)

        holder.amount.text = "₹$formattedAmount"

        // ✅ Optional: Highlight Top 3
        when (rank) {
            1 -> holder.rank.setTextColor(0xFFFFD700.toInt()) // Gold
            2 -> holder.rank.setTextColor(0xFFC0C0C0.toInt()) // Silver
            3 -> holder.rank.setTextColor(0xFFCD7F32.toInt()) // Bronze
            else -> holder.rank.setTextColor(0xFF888888.toInt())
        }
    }
}

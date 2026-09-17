package com.upimicro.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.upimicro.data.model.FraudTransaction
import com.upimicro.databinding.ItemFraudAlertBinding
import java.util.Locale

class FraudTransactionAdapter : RecyclerView.Adapter<FraudTransactionAdapter.ViewHolder>() {

    private var list: List<FraudTransaction> = emptyList()

    fun submitList(newList: List<FraudTransaction>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemFraudAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFraudAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvAmount.text = String.format(Locale.getDefault(), "₹%.2f", item.amount)
            chipRiskScore.text = "Risk Score: ${item.riskScore}"
            tvReason.text = item.fraudReason ?: "Suspicious activity detected"
            tvTxnId.text = "TXN ID: #${item.id}"
            // Format date for better readability: 2026-03-20 12:00
            tvDate.text = item.createdAt.replace("T", " ").take(16)
        }
    }
}

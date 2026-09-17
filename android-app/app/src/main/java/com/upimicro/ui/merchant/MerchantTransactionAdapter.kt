package com.upimicro.ui.merchant

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.upimicro.R
import com.upimicro.data.model.TransactionModel
import com.upimicro.databinding.ItemMerchantTransactionBinding

class MerchantTransactionAdapter :
    RecyclerView.Adapter<MerchantTransactionAdapter.ViewHolder>() {

    private var list = emptyList<TransactionModel>()

    inner class ViewHolder(val binding: ItemMerchantTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMerchantTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val txn = list[position]
        val binding = holder.binding

        // 6. DEBUG (IMPORTANT)
        Log.d("TXN_DEBUG", "Sender: ${txn.senderName}")

        // 3. FIX ADAPTER BINDING
        // 5. OPTIONAL UI IMPROVEMENT: "From: Rahul Sharma"
        val sender = if (!txn.senderName.isNullOrBlank()) txn.senderName else "Customer"
        binding.nameText.text = "From: $sender"

        // 🎯 Date & Time
        val time = if (txn.createdAt.length >= 16) {
            txn.createdAt.substring(11, 16)
        } else {
            "--:--"
        }
        binding.dateText.text = "${txn.createdAt.substringBefore("T")} • $time"

        // 🎯 Amount
        binding.amountText.text = "₹${String.format("%.0f", txn.amount)}"

        // 🎯 Status & Colors
        binding.statusText.text = txn.status
        when (txn.status) {
            "SUCCESS" -> {
                binding.statusText.setTextColor(Color.parseColor("#0F9D58"))
                binding.statusText.setBackgroundResource(R.drawable.rounded_green_bg)
                binding.statusText.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#D1FAE5"))
            }
            "FAILED" -> {
                binding.statusText.setTextColor(Color.RED)
                binding.statusText.setBackgroundResource(R.drawable.rounded_green_bg)
                binding.statusText.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
            }
            else -> {
                binding.statusText.setTextColor(Color.GRAY)
                binding.statusText.setBackgroundResource(R.drawable.rounded_green_bg)
                binding.statusText.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F3F4F6"))
            }
        }
    }

    fun submitList(data: List<TransactionModel>) {
        list = data
        notifyDataSetChanged()
    }
}

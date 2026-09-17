package com.upimicro.ui.user

import android.graphics.Color
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.upimicro.data.model.TransactionModel
import com.upimicro.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var transactions: List<TransactionModel> = emptyList()

    fun submitList(list: List<TransactionModel>) {
        transactions = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: TransactionModel) {

            // ✅ FIXED NAME HANDLING (ROBUST)
            val receiverName = transaction.receiverName?.trim()

            binding.merchantName.text = when {
                receiverName.isNullOrEmpty() -> "Merchant"
                receiverName.equals("null", ignoreCase = true) -> "Merchant"
                else -> "Paid to: $receiverName"
            }

            // 🔥 DEBUG (REMOVE LATER)
            android.util.Log.d("TXN_DEBUG", "ReceiverName: ${transaction.receiverName}")

            val amount = transaction.amount
            val status = transaction.status.uppercase()
            val fraud = transaction.fraudFlag ?: false
            val riskScore = transaction.riskScore ?: 0

            binding.amount.text = "₹ %.2f".format(amount)
            binding.date.text = formatDate(transaction.createdAt)
            binding.status.text = status

            // Status Styling
            when (status) {
                "SUCCESS" -> binding.status.setTextColor(Color.parseColor("#2E7D32"))
                "FAILURE", "FAILED" -> binding.status.setTextColor(Color.parseColor("#D32F2F"))
                else -> binding.status.setTextColor(Color.parseColor("#EF6C00"))
            }

            // Fraud UI
            if (fraud) {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#FFF5F5"))
                binding.cardView.strokeColor = Color.parseColor("#FFCDD2")
                binding.cardView.strokeWidth = 4

                binding.fraudBadge.visibility = View.VISIBLE
                binding.fraudBadge.text = "⚠ Suspicious (Score: $riskScore)"

                binding.ivTransactionType.setImageResource(android.R.drawable.stat_sys_warning)
                binding.ivTransactionType.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#D32F2F"))
                binding.ivTransactionType.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#FFEAEA"))

            } else {
                binding.cardView.setCardBackgroundColor(Color.WHITE)
                binding.cardView.strokeColor = Color.parseColor("#F0F0F0")
                binding.cardView.strokeWidth = 2

                binding.fraudBadge.visibility = View.GONE

                binding.ivTransactionType.setImageResource(android.R.drawable.ic_menu_send)
                binding.ivTransactionType.imageTintList =
                    ColorStateList.valueOf(Color.parseColor("#2E7D32"))
                binding.ivTransactionType.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
            }
        }

        private fun formatDate(dateString: String?): String {
            if (dateString.isNullOrEmpty()) return "Unknown date"
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
                val date = parser.parse(dateString)
                formatter.format(date!!)
            } catch (e: Exception) {
                dateString
            }
        }
    }
}
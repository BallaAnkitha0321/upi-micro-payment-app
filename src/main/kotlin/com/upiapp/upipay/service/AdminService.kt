package com.upiapp.upipay.service

import com.upiapp.upipay.repository.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import com.upiapp.upipay.service.TransactionService
import com.upiapp.upipay.service.MerchantService
import java.math.BigDecimal
import com.upiapp.upipay.entity.Merchant

@Service
class AdminService(

    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val merchantRepository: MerchantRepository
) {

    // ================= DASHBOARD =================
    fun getDashboardSummary(): Map<String, Any> {

        val totalRevenue = transactionRepository.getTotalRevenue()

        return mapOf(
            "totalUsers" to userRepository.count(),
            "totalMerchants" to merchantRepository.count(),
            "totalTransactions" to transactionRepository.count(),
            "totalRevenue" to totalRevenue,
            "failedTransactions" to transactionRepository.countAllFailedTransactions()
        )
    }

    // ================= TRANSACTIONS =================

    fun getDailyTransactions(): List<Map<String, Any>> {
        return transactionRepository.getDailyTransactionStats().map { row ->

            val date = row[0]
            val count = (row[1] as Number).toLong()
            val amount = (row[2] as? BigDecimal) ?: BigDecimal.ZERO

            mapOf(
                "date" to date,
                "count" to count,
                "amount" to amount
            )
        }
    }

    fun getFilteredTransactions(days: Int): List<Map<String, Any>> {

        val startDate = LocalDateTime.now().minusDays(days.toLong())

        return transactionRepository.getDailyTransactionsFiltered(startDate).map {
            mapOf(
                "date" to it[0],
                "count" to (it[1] as Number).toLong()
            )
        }
    }

    fun getTransactionStatus(): List<Map<String, Any>> {
        return transactionRepository.getTransactionStatusStats().map {
            mapOf(
                "status" to it[0].toString(),
                "count" to (it[1] as Number).toLong()
            )
        }
    }

    // ================= MERCHANT =================

    fun getTopMerchants(): List<Map<String, Any>> {
        return transactionRepository.getTopMerchants().map {
            mapOf(
                "name" to it[0],
                "amount" to it[1]
            )
        }
    }

    // ================= USER =================

    fun getUserRegistrations(): List<Map<String, Any>> {
        return userRepository.getDailyUserRegistrations().map {
            mapOf(
                "date" to it[0],
                "count" to (it[1] as Number).toLong()
            )
        }
    }

    // ✅ FIXED HERE
    fun getActiveUsers(): Map<String, Any> {

        val sevenDaysAgo = LocalDateTime.now().minusDays(7)

        return mapOf(
            "activeUsers" to transactionRepository.countActiveUsersSince(sevenDaysAgo)
        )
    }

    // ================= QR =================

    fun getQrAnalytics(): Map<String, Any> {
        return mapOf(
            "qrTransactions" to transactionRepository.countQrTransactions()
        )
    }

    fun getQrDailyStats(): List<Map<String, Any>> {
        return transactionRepository.getDailyQrTransactions().map {
            mapOf(
                "date" to it[0],
                "count" to (it[1] as Number).toLong()
            )
        }
    }

    // ================= REVENUE =================

    fun getRevenueAnalytics(): List<Map<String, Any>> {
        return transactionRepository.getDailyRevenue().map {
            mapOf(
                "date" to it[0],
                "amount" to it[1]
            )
        }
    }

    fun blockUser(userId: Long): String {
        val user = userRepository.findById(userId).orElse(null)
            ?: return "User not found"

        if (user.blocked) return "User is already blocked"

        user.blocked = true
        userRepository.save(user)

        return "User blocked successfully"
    }
    fun unblockUser(userId: Long): String {
        val user = userRepository.findById(userId).orElse(null)
            ?: return "User not found"

        if (!user.blocked) return "User is not blocked"

        user.blocked = false
        userRepository.save(user)

        return "User unblocked successfully"
    }
    fun blockMerchant(merchantId: Long): String {
        val merchant = merchantRepository.findById(merchantId).orElse(null)
            ?: return "Merchant not found"

        if (merchant.blocked) return "Merchant is already blocked"

        merchant.blocked = true
        merchantRepository.save(merchant)

        return "Merchant blocked successfully"
    }
    fun unblockMerchant(merchantId: Long): String {
        val merchant = merchantRepository.findById(merchantId).orElse(null)
            ?: return "Merchant not found"

        if (!merchant.blocked) return "Merchant is not blocked"

        merchant.blocked = false
        merchantRepository.save(merchant)

        return "Merchant unblocked successfully"
    }
}
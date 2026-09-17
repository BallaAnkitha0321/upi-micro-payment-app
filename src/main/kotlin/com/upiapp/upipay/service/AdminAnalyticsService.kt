package com.upiapp.upipay.service

import com.upiapp.upipay.dto.*
import com.upiapp.upipay.entity.TxnStatus
import com.upiapp.upipay.repository.*
import com.upiapp.upipay.service.TransactionService
import com.upiapp.upipay.service.MerchantService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AdminAnalyticsService(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val merchantRepository: MerchantRepository
) {

    fun getUserInsights(): UserInsightsResponse {

        val totalUsers = userRepository.getTotalUsers()
        val activeUsers =
            transactionRepository.countActiveUsersSince(LocalDateTime.now().minusDays(7))

        val usersWithTransactions =
            transactionRepository.countUsersWithTransactions()

        val conversionRate =
            if (totalUsers == 0L) 0.0
            else (usersWithTransactions.toDouble() / totalUsers) * 100

        return UserInsightsResponse(
            totalUsers,
            activeUsers,
            String.format("%.2f", conversionRate).toDouble()
        )
    }

    fun getDailyUserRegistrations(): List<DailyUserResponse> =
        userRepository.getDailyUserRegistrations().map {
            DailyUserResponse(it[0].toString(), (it[1] as Number).toLong())
        }

    fun getTopMerchants(): List<Map<String, Any>> =
        transactionRepository.getTopMerchants().map {
            mapOf(
                "name" to it[0].toString(),
                "amount" to (it[1] as Number).toDouble()
            )
        }

    fun getMerchantKpis(): MerchantKpiResponse {

        val totalMerchants = merchantRepository.count()

        val activeMerchants =
            transactionRepository.countActiveMerchantsSince(LocalDateTime.now().minusDays(7))

        val totalRevenue = transactionRepository.getTotalRevenue()

        val top = transactionRepository.findTopMerchant()

        val name = if (top.isNotEmpty()) top[0][0].toString() else "N/A"
        val amount = if (top.isNotEmpty()) (top[0][1] as Number).toDouble() else 0.0

        return MerchantKpiResponse(
            totalMerchants,
            activeMerchants,
            totalRevenue,
            name,
            amount
        )
    }

    fun getTransactionKpis(): TransactionKpiResponse {

        val total = transactionRepository.count()
        val success = transactionRepository.countByStatus(TxnStatus.SUCCESS)
        val failed = transactionRepository.countByStatus(TxnStatus.FAILED)

        val rate =
            if (total > 0) (success.toDouble() / total) * 100 else 0.0

        return TransactionKpiResponse(
            total,
            success,
            failed,
            String.format("%.2f", rate).toDouble(),
            when {
                rate >= 90 -> "System healthy ✅"
                rate >= 70 -> "Minor failures ⚠️"
                else -> "High failure rate 🚨"
            }
        )
    }

    fun getDailyTransactions(): List<DailyTransactionResponse> =
        transactionRepository.getDailyTransactionStats().map {
            DailyTransactionResponse(it[0].toString(), (it[1] as Number).toLong())
        }

    fun getFilteredTransactions(days: Int): List<DailyTransactionResponse> =
        transactionRepository.getDailyTransactionsFiltered(LocalDateTime.now().minusDays(days.toLong()))
            .map {
                DailyTransactionResponse(it[0].toString(), (it[1] as Number).toLong())
            }

    fun getTransactionStatus(): List<Map<String, Any>> =
        transactionRepository.getTransactionStatusStats().map {
            mapOf(
                "status" to it[0].toString(),
                "count" to (it[1] as Number).toLong()
            )
        }

    // 🔥 FINAL REVENUE API
    fun getRevenueTrend(): List<DailyTransactionResponse> =
        transactionRepository.getDailyRevenue().map {
            DailyTransactionResponse(
                date = it[0].toString(),
                count = (it[1] as Number).toLong()
            )
        }
}
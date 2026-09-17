package com.upiapp.upipay.repository

import com.upiapp.upipay.entity.Transaction
import com.upiapp.upipay.entity.TxnStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.math.BigDecimal

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {

    // ================= BASIC =================
    fun existsByTxnRef(txnRef: String): Boolean

    // ✅ FIXED: Correct user-based fetching (USED IN HISTORY)
    fun findByUser_UserIdOrderByCreatedAtDesc(userId: Long): List<Transaction>

    // Merchant transactions
    fun findByMerchant_MerchantIdOrderByCreatedAtDesc(merchantId: Long): List<Transaction>

    // Status-based (used for admin / reconciliation)
    fun findByStatus(status: TxnStatus): List<Transaction>

    fun countByStatus(status: TxnStatus): Long


    // ================= FRAUD =================

    fun findByFraudFlagTrue(): List<Transaction>

    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.user.userId = :userId 
        AND t.createdAt >= :time
    """)
    fun findRecentTransactions(
        @Param("userId") userId: Long,
        @Param("time") time: LocalDateTime
    ): List<Transaction>


    // ================= ADMIN =================

    @Query("""
        SELECT COALESCE(SUM(t.amount),0)
        FROM Transaction t
        WHERE t.status = com.upiapp.upipay.entity.TxnStatus.SUCCESS
    """)
    fun getTotalRevenue(): Double

    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.status = com.upiapp.upipay.entity.TxnStatus.FAILED
    """)
    fun countAllFailedTransactions(): Long


    // ================= USER ANALYTICS =================

    @Query("""
        SELECT COUNT(DISTINCT t.user.userId)
        FROM Transaction t
        WHERE t.createdAt >= :since
    """)
    fun countActiveUsersSince(@Param("since") since: LocalDateTime): Long

    @Query("""
        SELECT COUNT(DISTINCT t.user.userId)
        FROM Transaction t
    """)
    fun countUsersWithTransactions(): Long


    // ================= MERCHANT ANALYTICS =================

    @Query("""
        SELECT t.merchant.name, SUM(t.amount)
        FROM Transaction t
        WHERE t.status = com.upiapp.upipay.entity.TxnStatus.SUCCESS
        GROUP BY t.merchant.name
        ORDER BY SUM(t.amount) DESC
    """)
    fun getTopMerchants(): List<Array<Any>>

    @Query("""
        SELECT COUNT(DISTINCT t.merchant.merchantId)
        FROM Transaction t
        WHERE t.status = com.upiapp.upipay.entity.TxnStatus.SUCCESS
        AND t.createdAt >= :since
    """)
    fun countActiveMerchantsSince(@Param("since") since: LocalDateTime): Long


    // ================= TRANSACTION ANALYTICS =================

    @Query("""
        SELECT DATE(t.createdAt), COUNT(t), SUM(t.amount)
        FROM Transaction t
        GROUP BY DATE(t.createdAt)
        ORDER BY DATE(t.createdAt)
    """)
    fun getDailyTransactionStats(): List<Array<Any>>

    @Query("""
        SELECT DATE(t.createdAt), COUNT(t)
        FROM Transaction t
        WHERE t.createdAt >= :startDate
        GROUP BY DATE(t.createdAt)
        ORDER BY DATE(t.createdAt)
    """)
    fun getDailyTransactionsFiltered(@Param("startDate") startDate: LocalDateTime): List<Array<Any>>

    @Query("""
        SELECT t.status, COUNT(t)
        FROM Transaction t
        GROUP BY t.status
    """)
    fun getTransactionStatusStats(): List<Array<Any>>


    // ================= QR =================

    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.isQr = true
    """)
    fun countQrTransactions(): Long


    // ================= REVENUE =================

    @Query("""
        SELECT DATE(t.createdAt), SUM(t.amount)
        FROM Transaction t
        WHERE t.status = com.upiapp.upipay.entity.TxnStatus.SUCCESS
        GROUP BY DATE(t.createdAt)
        ORDER BY DATE(t.createdAt)
    """)
    fun getDailyRevenue(): List<Array<Any>>


    // ================= MERCHANT DASHBOARD =================

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.merchant.merchantId = :merchantId
        AND t.status = com.upiapp.upipay.entity.TxnStatus.SUCCESS
    """)
    fun getTotalEarnings(@Param("merchantId") merchantId: Long): BigDecimal

    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.merchant.merchantId = :merchantId
        AND t.status = com.upiapp.upipay.entity.TxnStatus.SUCCESS
    """)
    fun getTotalTransactions(@Param("merchantId") merchantId: Long): Long

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.merchant.merchantId = :merchantId
        AND t.status = com.upiapp.upipay.entity.TxnStatus.SUCCESS
        AND t.createdAt >= :startOfDay
    """)
    fun getTodayEarningsFromStartOfDay(
        @Param("merchantId") merchantId: Long,
        @Param("startOfDay") startOfDay: LocalDateTime
    ): BigDecimal

    @Query("""
        SELECT t.merchant.name, SUM(t.amount)
        FROM Transaction t
        WHERE t.status = com.upiapp.upipay.entity.TxnStatus.SUCCESS
        GROUP BY t.merchant.name
        ORDER BY SUM(t.amount) DESC
    """)
    fun findTopMerchant(): List<Array<Any>>

    @Query("""
        SELECT DATE(t.createdAt), COUNT(t)
        FROM Transaction t
        WHERE t.isQr = true
        GROUP BY DATE(t.createdAt)
        ORDER BY DATE(t.createdAt)
    """)
    fun getDailyQrTransactions(): List<Array<Any>>

    fun findByMerchant_MerchantIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        merchantId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Transaction>
}
package com.upiapp.upipay.repository

import com.upiapp.upipay.entity.Merchant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.time.LocalDateTime
import org.springframework.data.repository.query.Param

interface MerchantRepository : JpaRepository<Merchant, Long> {

    fun findByPhone(phone: String): Merchant?

    fun findAllByPhone(phone: String): List<Merchant>

    @Query("SELECT m FROM Merchant m WHERE REPLACE(m.phone, '+', '') = REPLACE(:phone, '+', '')")
    fun findByPhoneSafe(phone: String): Merchant?

    // ✅ OLD METHOD (KEEP — DO NOT REMOVE)
    fun findByUpiId(upiId: String): Merchant?

    // ✅ NEW FIXED METHOD (USE THIS)
    @Query("SELECT m FROM Merchant m WHERE LOWER(TRIM(m.upiId)) = LOWER(TRIM(:upiId))")
    fun findByUpiIdSafe(@Param("upiId") upiId: String): Merchant?

    fun findByBlocked(blocked: Boolean): List<Merchant>

    @Query("SELECT COUNT(m) FROM Merchant m")
    fun countAllMerchants(): Long

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.merchant.merchantId = :merchantId
        AND t.status = 'SUCCESS'
    """)
    fun getTotalEarnings(@Param("merchantId") merchantId: Long): BigDecimal

    @Query("""
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.merchant.merchantId = :merchantId
        AND t.status = 'SUCCESS'
    """)
    fun getTotalTransactions(@Param("merchantId") merchantId: Long): Long

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.merchant.merchantId = :merchantId
        AND t.status = com.upiapp.upipay.entity.TxnStatus.SUCCESS
        AND t.createdAt >= :startOfDay
    """)
    fun getTodayEarnings(
        @Param("merchantId") merchantId: Long,
        @Param("startOfDay") startOfDay: LocalDateTime
    ): BigDecimal
}
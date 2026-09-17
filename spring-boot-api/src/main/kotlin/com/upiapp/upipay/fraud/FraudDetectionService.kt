package com.upiapp.upipay.fraud

import com.upiapp.upipay.entity.Transaction
import com.upiapp.upipay.notification.NotificationDispatcher
import com.upiapp.upipay.notification.NotificationEvent
import com.upiapp.upipay.notification.NotificationType
import com.upiapp.upipay.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class FraudDetectionService(

    private val transactionRepository: TransactionRepository,
    private val notificationDispatcher: NotificationDispatcher

) {

    private val logger = LoggerFactory.getLogger(FraudDetectionService::class.java)

    fun evaluateTransaction(txn: Transaction): Transaction {

        var riskScore = 0
        val reasons = mutableListOf<String>()

        // 🔹 RULE 1 — High Amount (> ₹3000) ✅ UPDATED
        val threshold = BigDecimal("3000")

        if (txn.amount.compareTo(threshold) > 0) {
            riskScore += 50
            reasons.add("High transaction amount")
        }

        // 🔹 RULE 2 — High Frequency (3+ transactions in 5 minutes)
        val fiveMinutesAgo = LocalDateTime.now().minusMinutes(5)

        val recentTransactions = transactionRepository
            .findRecentTransactions(txn.user.userId, fiveMinutesAgo)

        if (recentTransactions.size >= 3) {
            riskScore += 30
            reasons.add("Multiple transactions in short time")
        }

        // 🔹 FINAL DECISION
        val isFraud = riskScore >= 50

        txn.riskScore = riskScore
        txn.fraudFlag = isFraud
        txn.fraudReason = if (isFraud) reasons.joinToString(", ") else null

        // 🔥 Logging + Notification
        if (isFraud) {

            logger.warn(
                "Fraud detected txnRef={} riskScore={} reason={}",
                txn.txnRef,
                riskScore,
                txn.fraudReason
            )

            notificationDispatcher.dispatch(
                NotificationEvent(
                    userId = txn.user.userId,
                    type = NotificationType.FRAUD_ALERT,
                    title = "Fraud Alert",
                    message = "Suspicious transaction detected: ${txn.fraudReason}"
                )
            )
        }

        return txn
    }
}
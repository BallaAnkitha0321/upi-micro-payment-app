package com.upiapp.upipay.service

import com.upiapp.upipay.entity.*
import com.upiapp.upipay.repository.*
import com.upiapp.upipay.fraud.FraudDetectionService
import com.upiapp.upipay.notification.*
import com.upiapp.upipay.dto.TransactionResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalDate
import java.math.BigDecimal
import org.slf4j.LoggerFactory

@Service
class TransactionService(

    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val merchantRepository: MerchantRepository,
    private val fraudDetectionService: FraudDetectionService,
    private val notificationDispatcher: NotificationDispatcher

) {

    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    @Transactional
    fun saveTransaction(
        userId: Long,
        merchantUpiId: String,
        amountDouble: Double,
        status: String,
        txnRef: String,
        rawResponse: String?,
        responseCode: String?,
        timestamp: LocalDateTime,
        isQr: Boolean,
        isDemoMode: Boolean = true,
        upiPin: String? = null
    ): TransactionResponse {

        println("DEBUG_SAVE userId = $userId")
        

        if (amountDouble <= 0) {
            throw RuntimeException("Invalid amount")
        }

        val amount = BigDecimal.valueOf(amountDouble)
        val recentTxns = transactionRepository.findRecentTransactions(
    userId,
    LocalDateTime.now().minusSeconds(10)
)

if (recentTxns.any { 
    it.amount.compareTo(amount) == 0 &&
    it.merchant?.upiId?.trim()?.equals(merchantUpiId.trim(), ignoreCase = true) == true
}) {
    throw RuntimeException("Duplicate payment attempt detected")
}

        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found for transaction") }

        // 🔥 STANDARD BLOCK CHECK
        if (user.blocked) {
            logger.warn("Blocked user attempted transaction: userId=$userId")
            throw RuntimeException("ACCOUNT_RESTRICTED")
        }

        // PIN VALIDATION
        val requiresPin = responseCode == "00"

        if (requiresPin) {
            val pin = upiPin ?: throw RuntimeException("UPI PIN is required for payment")

            if (!userService.verifyUpiPin(user.phone, pin)) {
                throw RuntimeException("Invalid UPI PIN")
            }
        }

        logger.info("Resolving merchant using UPI: $merchantUpiId")

        val normalizedUpi = merchantUpiId.trim()

if (!normalizedUpi.contains("@")) {
    throw IllegalArgumentException("Invalid UPI ID format")
}

val merchant = merchantRepository.findByUpiIdSafe(normalizedUpi)
    ?: throw IllegalArgumentException("Invalid UPI ID: $merchantUpiId")

        logger.info("Merchant found: ${merchant.name}, UPI: ${merchant.upiId}")

        // 🔥 STANDARD BLOCK CHECK (FIXED)
        if (merchant.blocked) {
            logger.warn("Blocked merchant attempted transaction: merchantId=${merchant.merchantId}")
            throw RuntimeException("ACCOUNT_RESTRICTED")
        }

        val finalStatus = when (status.uppercase()) {
            "SUCCESS" -> TxnStatus.SUCCESS
            "FAILED" -> TxnStatus.FAILED
            else -> TxnStatus.FAILED
        }

        if (finalStatus == TxnStatus.SUCCESS && user.balance.compareTo(amount) >= 0) {
            user.balance = user.balance.subtract(amount)
            merchant.revenue = merchant.revenue.add(amount)

            userRepository.save(user)
            merchantRepository.save(merchant)

            logger.info("Balance deducted & merchant credited")
        }

        val transaction = Transaction(
            user = user,
            merchant = merchant,
            amount = amount,
            status = finalStatus,
            txnRef = txnRef,
            rawResponse = rawResponse,
            responseCode = responseCode,
            createdAt = timestamp,
            isQr = isQr,
            senderName = user.name?.takeIf { it.isNotBlank() } ?: "Customer",
            receiverName = merchant.businessName?.takeIf { it.isNotBlank() }
                ?: merchant.name?.takeIf { it.isNotBlank() }
                ?: "Merchant"
        )

        val evaluatedTransaction = fraudDetectionService.evaluateTransaction(transaction)

        val savedTransaction = transactionRepository.save(evaluatedTransaction)

        println("DEBUG_SAVE_SUCCESS txnId = ${savedTransaction.txnId}")

        if (savedTransaction.status == TxnStatus.SUCCESS) {
            notificationDispatcher.dispatch(
                NotificationEvent(
                    userId = user.userId,
                    type = NotificationType.PAYMENT_SUCCESS,
                    title = "Payment Successful",
                    message = "₹$amount paid to ${savedTransaction.receiverName}"
                )
            )
        }

        logger.info("Transaction saved: ${savedTransaction.txnRef}")
       
        return mapToResponse(savedTransaction)
    }

    private fun mapToResponse(txn: Transaction): TransactionResponse {
        return TransactionResponse(
            txnId = txn.txnId,
            amount = txn.amount,
            status = txn.status.name,
            txnRef = txn.txnRef,
            createdAt = txn.createdAt,
            fraudFlag = txn.fraudFlag,
            fraudReason = txn.fraudReason,
            riskScore = txn.riskScore,
            receiverName = txn.receiverName,
            senderName = txn.senderName,
            isQr = txn.isQr
        )
    }

    fun getUserTransactions(userId: Long): List<TransactionResponse> {

        logger.info("Fetching transactions for userId: $userId")

        val transactions =
            transactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)

        println("DEBUG_FETCH_COUNT = ${transactions.size}")

        return transactions.map { mapToResponse(it) }
    }

    fun getMerchantTransactions(merchantId: Long): List<TransactionResponse> {
        return transactionRepository
            .findByMerchant_MerchantIdOrderByCreatedAtDesc(merchantId)
            .map { mapToResponse(it) }
    }

    fun getTodayMerchantTransactions(merchantId: Long): List<TransactionResponse> {

        val startOfDay = LocalDate.now().atStartOfDay()
        val endOfDay = LocalDate.now().atTime(23, 59, 59)

        return transactionRepository
            .findByMerchant_MerchantIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                merchantId,
                startOfDay,
                endOfDay
            )
            .map { mapToResponse(it) }
    }
}
package com.upiapp.upipay.reconciliation

import com.upiapp.upipay.entity.TxnStatus
import com.upiapp.upipay.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class ReconciliationService(
    private val transactionRepository: TransactionRepository
) {

@Transactional
fun reconcilePendingTransactions() {

    val pendingTransactions =
        transactionRepository.findByStatus(TxnStatus.PENDING)

    if (pendingTransactions.isEmpty()) return

    for (txn in pendingTransactions) {

        // ✅ DO NOT change status
        // just mark as processed

        txn.rawResponse = "Processed (manual control)"

        transactionRepository.save(txn)
    }
}

    private fun simulateBankVerification(amount: BigDecimal): TxnStatus {

        val remainder = amount.remainder(BigDecimal.valueOf(2))

        return if (remainder.compareTo(BigDecimal.ZERO) == 0) {
            TxnStatus.SUCCESS
        } else {
            TxnStatus.FAILED   // ✅ FIXED
        }
    }
}
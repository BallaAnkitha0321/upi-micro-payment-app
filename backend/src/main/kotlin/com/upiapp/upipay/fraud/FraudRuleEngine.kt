package com.upiapp.upipay.fraud

import com.upiapp.upipay.entity.Transaction
import java.math.BigDecimal
import org.springframework.stereotype.Component

@Component
class FraudRuleEngine {

    fun evaluate(transaction: Transaction) {

        val limit = BigDecimal.valueOf(10000)

        if (transaction.amount.compareTo(limit) > 0) {

            transaction.fraudFlag = true
            transaction.fraudReason = "Large transaction"
            transaction.riskScore = 80
        }
    }
}
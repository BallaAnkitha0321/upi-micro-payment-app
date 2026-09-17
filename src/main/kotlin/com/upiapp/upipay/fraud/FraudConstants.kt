package com.upiapp.upipay.fraud

object FraudConstants {

    const val LARGE_AMOUNT_THRESHOLD = 50000.0

    const val RAPID_TXN_LIMIT = 3
    const val RAPID_TXN_MINUTES = 2

    const val FAILED_TXN_LIMIT = 5

    const val AUTO_BLOCK_RISK_SCORE = 100
}
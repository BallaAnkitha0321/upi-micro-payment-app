package com.upiapp.upipay.reconciliation

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReconciliationScheduler(
    private val reconciliationService: ReconciliationService
) {

    private val logger = LoggerFactory.getLogger(ReconciliationScheduler::class.java)

    /**
     * Reconciliation Scheduler
     *
     * Runs periodically to resolve PENDING transactions.
     *
     * Development: every 30 seconds
     * Production: change to 300000 (5 minutes)
     */

   // @Scheduled(fixedRate = 30000)
    fun runReconciliation() {

        logger.info("Reconciliation job started")

        try {

            reconciliationService.reconcilePendingTransactions()

            logger.info("Reconciliation job finished successfully")

        } catch (ex: Exception) {

            logger.error("Reconciliation job failed", ex)

        }
    }
}
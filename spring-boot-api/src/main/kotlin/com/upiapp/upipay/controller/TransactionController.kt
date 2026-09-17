package com.upiapp.upipay.controller
import com.upiapp.upipay.dto.TransactionRequest
import com.upiapp.upipay.dto.TransactionResponse
import com.upiapp.upipay.dto.ApiResponse
import com.upiapp.upipay.service.TransactionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.UUID


// ================= REQUEST MODEL =================


@RestController
@RequestMapping("/api/transactions")
class TransactionController(

    private val transactionService: TransactionService

) {

    // ================= SAVE TRANSACTION =================
    @PostMapping("/save")
    fun saveTransaction(
        @RequestBody request: TransactionRequest
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
    val txnRef = "TXN-" + UUID.randomUUID().toString().substring(0, 12)

val response = transactionService.saveTransaction(
    userId = request.userId,
    merchantUpiId = request.merchantUpiId,
    amountDouble = request.amount,
    status = "PENDING",
    txnRef = txnRef,
    rawResponse = "INITIATED",
    responseCode = "01",
    timestamp = LocalDateTime.now(),
    isQr = false,
    upiPin = request.pin
)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Transaction saved successfully",
                data = response
            )
        )
    }

    // ================= PAY =================
    @PostMapping("/pay")
    fun pay(
        @RequestBody request: TransactionRequest
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
    val txnRef = "TXN-" + UUID.randomUUID().toString().substring(0, 12)

       val response = transactionService.saveTransaction(
    userId = request.userId,
    merchantUpiId = request.merchantUpiId,
    amountDouble = request.amount,
    status = request.status,          // 🔥 CHANGE HERE
    txnRef = txnRef,
    rawResponse = "INITIATED",   // 🔥 CHANGE HERE
    responseCode = "01",         // 🔥 CHANGE HERE
    timestamp = LocalDateTime.now(),
    isQr = false,
    upiPin = request.pin
)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Payment successful",
                data = response
            )
        )
    }

    // ================= USER TRANSACTIONS =================
    @GetMapping("/user/{userId}")
    fun userTransactions(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<List<TransactionResponse>>> {

        val transactions = transactionService.getUserTransactions(userId)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = if (transactions.isEmpty())
                    "No transactions found"
                else
                    "Transactions fetched successfully",
                data = transactions
            )
        )
    }

    // ================= MERCHANT TRANSACTIONS =================
    @GetMapping("/merchant/{merchantId}")
    fun merchantTransactions(
        @PathVariable merchantId: Long
    ): ResponseEntity<ApiResponse<List<TransactionResponse>>> {

        val transactions = transactionService.getMerchantTransactions(merchantId)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = if (transactions.isEmpty())
                    "No transactions found"
                else
                    "Merchant transactions fetched successfully",
                data = transactions
            )
        )
    }

    // ================= TODAY MERCHANT =================
    @GetMapping("/merchant/{merchantId}/today")
    fun todayMerchantTransactions(
        @PathVariable merchantId: Long
    ): ResponseEntity<ApiResponse<List<TransactionResponse>>> {

        val transactions = transactionService.getTodayMerchantTransactions(merchantId)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = if (transactions.isEmpty())
                    "No transactions today"
                else
                    "Today's transactions fetched successfully",
                data = transactions
            )
        )
    }
}
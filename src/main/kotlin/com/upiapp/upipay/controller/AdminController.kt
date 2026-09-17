package com.upiapp.upipay.controller

import com.upiapp.upipay.dto.*
import com.upiapp.upipay.entity.Transaction
import com.upiapp.upipay.repository.*
import com.upiapp.upipay.security.JwtService
import com.upiapp.upipay.service.AdminService
import com.upiapp.upipay.service.AdminAnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
class AdminController(

    private val adminRepository: AdminRepository,
    private val userRepository: UserRepository,
    private val merchantRepository: MerchantRepository,
    private val transactionRepository: TransactionRepository,
    private val jwtService: JwtService,
    private val adminService: AdminService,
    private val adminAnalyticsService: AdminAnalyticsService

) {

    // ================= LOGIN =================
    @PostMapping("/login")
    fun login(@RequestBody request: Map<String, String>): Map<String, String> {

        val email = request["email"]?.trim()
            ?: throw RuntimeException("Email required")

        val password = request["password"]?.trim()
            ?: throw RuntimeException("Password required")

        val admin = adminRepository.findByEmail(email)
            ?: throw RuntimeException("Admin not found")

        if (password != "Moon143") {
            throw RuntimeException("Invalid credentials")
        }

        // ✅ FIX: use email as identity (String)
        val token = jwtService.generateToken(email)

        return mapOf(
            "token" to token,
            "message" to "Login Successful"
        )
    }

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    fun getDashboard(): AdminDashboardResponse {
        return AdminDashboardResponse(
            totalUsers = userRepository.count(),
            totalMerchants = merchantRepository.count(),
            totalTransactions = transactionRepository.count(),
            totalRevenue = transactionRepository.getTotalRevenue(),
            failedTransactions = transactionRepository.countAllFailedTransactions()
        )
    }

    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userRepository.findAll()
            .map { user ->
                UserResponse(
                    userId = user.userId,
                    name = user.name,
                    phone = user.phone,
                    upiId = user.upiId,
                    email = user.email,
                    bankName = user.bankName,
                    accountNumber = user.accountNumber,
                    ifscCode = user.ifscCode,
                    balance = user.balance,
                    bankLinked = user.bankLinked,
                    kycVerified = user.kycVerified,
                    isBlocked = user.blocked,
                    token = user.token
                )
            }

        return ResponseEntity.ok(users)
    }

    // ================= USER MANAGEMENT =================

    @PutMapping("/user/block/{userId}")
    fun blockUser(@PathVariable userId: Long): ResponseEntity<ApiResponse<String>> {
        val message = adminService.blockUser(userId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = message,
                data = message
            )
        )
    }

    @PutMapping("/user/unblock/{userId}")
    fun unblockUser(@PathVariable userId: Long): ResponseEntity<ApiResponse<String>> {
        val message = adminService.unblockUser(userId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = message,
                data = message
            )
        )
    }

    // ================= MERCHANT MANAGEMENT =================

    @PutMapping("/merchant/block/{merchantId}")
    fun blockMerchant(@PathVariable merchantId: Long): ResponseEntity<ApiResponse<String>> {
        val message = adminService.blockMerchant(merchantId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = message,
                data = message
            )
        )
    }

    @PutMapping("/merchant/unblock/{merchantId}")
    fun unblockMerchant(@PathVariable merchantId: Long): ResponseEntity<ApiResponse<String>> {
        val message = adminService.unblockMerchant(merchantId)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = message,
                data = message
            )
        )
    }

    // ================= FRAUD =================

    @GetMapping("/fraud-alerts")
    fun getFraudAlerts(): ResponseEntity<List<Transaction>> {
        val fraudTransactions = transactionRepository.findByFraudFlagTrue()
        return ResponseEntity.ok(fraudTransactions)
    }

    // ================= USER ANALYTICS =================

    @GetMapping("/analytics/user-insights")
    fun getUserInsights() =
        ResponseEntity.ok(adminAnalyticsService.getUserInsights())

    @GetMapping("/analytics/users/daily")
    fun getDailyUsers() =
        ResponseEntity.ok(adminAnalyticsService.getDailyUserRegistrations())

    @GetMapping("/analytics/users/active")
    fun getActiveUsers() =
        ResponseEntity.ok(adminService.getActiveUsers())

    // ================= MERCHANT ANALYTICS =================

    @GetMapping("/analytics/merchants/top")
    fun getTopMerchants() =
        ResponseEntity.ok(adminAnalyticsService.getTopMerchants())

    @GetMapping("/analytics/merchants/kpis")
    fun getMerchantKpis() =
        ResponseEntity.ok(adminAnalyticsService.getMerchantKpis())

    // ================= TRANSACTION ANALYTICS =================

    @GetMapping("/analytics/transactions/kpis")
    fun getTransactionKpis() =
        ResponseEntity.ok(adminAnalyticsService.getTransactionKpis())

    @GetMapping("/analytics/transactions/daily")
    fun getDailyTransactions() =
        ResponseEntity.ok(adminAnalyticsService.getDailyTransactions())

    @GetMapping("/analytics/transactions/filter")
    fun getFilteredTransactions(@RequestParam days: Int) =
        ResponseEntity.ok(adminAnalyticsService.getFilteredTransactions(days))

    @GetMapping("/analytics/transactions/status")
    fun getTransactionStatus() =
        ResponseEntity.ok(adminAnalyticsService.getTransactionStatus())

    // ================= REVENUE =================

    @GetMapping("/analytics/transactions/revenue")
    fun getRevenueTrend(): ResponseEntity<List<DailyTransactionResponse>> {
        return ResponseEntity.ok(adminAnalyticsService.getRevenueTrend())
    }

    // ================= QR =================

    @GetMapping("/analytics/qr")
    fun getQrAnalytics() =
        ResponseEntity.ok(adminService.getQrAnalytics())

    @GetMapping("/analytics/qr/daily")
    fun getQrDailyStats() =
        ResponseEntity.ok(adminService.getQrDailyStats())
}
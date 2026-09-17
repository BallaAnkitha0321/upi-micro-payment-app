package com.upiapp.upipay.controller

import com.upiapp.upipay.entity.Merchant
import com.upiapp.upipay.exception.ResourceNotFoundException
import com.upiapp.upipay.security.JwtService
import com.upiapp.upipay.service.MerchantService
import com.upiapp.upipay.dto.*
import com.upiapp.upipay.repository.MerchantRepository
import com.upiapp.upipay.service.TransactionService
import com.upiapp.upipay.util.PhoneUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/merchant")
class MerchantController(
    private val merchantService: MerchantService,
    private val transactionService: TransactionService,
    private val merchantRepository: MerchantRepository,
    private val jwtService: JwtService
) {

    private val logger = LoggerFactory.getLogger(MerchantController::class.java)

    // ================= AUTH =================

    data class LoginRequest(
        val name: String,
        val phone: String,
        val businessName: String? = null
    )

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Map<String, Any>> {

        val merchant = merchantService.loginOrRegister(
            name = request.name,
            phone = request.phone,
            businessName = request.businessName
        )

        val response = mapOf(
            "role" to "MERCHANT",
            "merchantId" to merchant.merchantId,
            "name" to merchant.name,
            "upiId" to merchant.upiId,
            "message" to "Login successful"
        )

        return ResponseEntity.ok(response)
    }

    // ================= HELPER =================

    private fun getCurrentMerchant(token: String): Merchant {

        val phone = jwtService.extractPhone(token)
            ?: throw IllegalArgumentException("Invalid or missing JWT token")

        logger.info("Extracted phone from JWT for merchant profile: $phone")

        val normalizedPhone = PhoneUtils.normalizePhone(phone)
            ?: throw IllegalArgumentException("Invalid phone format in token: $phone")

        logger.info("NORMALIZED TOKEN PHONE: $normalizedPhone")

        val merchant = merchantRepository.findByPhoneSafe(normalizedPhone)
            ?: merchantRepository.findAll().firstOrNull {
                PhoneUtils.normalizePhone(it.phone) == normalizedPhone
            }

        logger.info("MERCHANT FETCH RESULT FOR $normalizedPhone: $merchant")

        return merchant ?: throw ResourceNotFoundException("Merchant not found for $normalizedPhone")
    }

    // ================= BANK =================

    @PostMapping("/link-bank")
    fun linkBank(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MerchantBankRequest
    ): ResponseEntity<MerchantStatusResponse> {

        val merchant = getCurrentMerchant(token)
        val response = merchantService.linkBank(merchant.phone, request)

        return ResponseEntity.ok(response)
    }

    // ================= STATUS =================

    @GetMapping("/status")
    fun getMerchantStatus(@RequestHeader("Authorization") token: String): ResponseEntity<MerchantStatusResponse> {

        val merchant = getCurrentMerchant(token)
        val response = merchantService.getMerchantStatus(merchant.phone)

        return ResponseEntity.ok(response)
    }

    // ================= PROFILE =================

    @GetMapping("/profile")
    fun getProfile(@RequestHeader("Authorization") token: String): ResponseEntity<MerchantProfileResponse> {

        val merchant = getCurrentMerchant(token)
        logger.info("PROFILE REQUEST PHONE: ${merchant.phone}")

        return ResponseEntity.ok(merchantService.getMerchantProfile(merchant.phone))
    }

    // ================= KYC =================

    @PostMapping("/update-kyc")
    fun updateMerchantKyc(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MerchantKycRequest
    ): ResponseEntity<Map<String, String>> {

        val merchant = getCurrentMerchant(token)
        merchantService.updateKyc(merchant.merchantId, request)

        return ResponseEntity.ok(mapOf("message" to "KYC submitted successfully"))
    }

    @PutMapping("/approve-kyc/{merchantId}")
    fun approveKyc(@PathVariable merchantId: Long): ResponseEntity<Map<String, String>> {

        merchantService.approveKyc(merchantId)
        return ResponseEntity.ok(mapOf("message" to "KYC approved"))
    }

    @PutMapping("/reject-kyc/{merchantId}")
    fun rejectKyc(@PathVariable merchantId: Long): ResponseEntity<Map<String, String>> {

        merchantService.rejectKyc(merchantId)
        return ResponseEntity.ok(mapOf("message" to "KYC rejected"))
    }

    // ================= UPDATE PROFILE =================

    // 🔥🔥🔥 FINAL FIX HERE
    @PutMapping("/update-profile")
    fun updateMerchantProfile(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MerchantUpdateProfileRequest
    ): ResponseEntity<Map<String, String>> {

        val merchant = getCurrentMerchant(token)

        logger.info("UPDATE PROFILE API HIT for merchant ${merchant.merchantId}")
        logger.info("REQUEST = $request")

        merchantService.updateProfile(merchant.merchantId, request)

        return ResponseEntity.ok(mapOf("message" to "Profile updated successfully"))
    }

    // ================= DASHBOARD =================

    @GetMapping("/dashboard")
    fun getDashboard(@RequestHeader("Authorization") token: String): ResponseEntity<MerchantDashboardResponse> {

        val merchant = getCurrentMerchant(token)
        val response = merchantService.getMerchantDashboard(merchant.merchantId)

        return ResponseEntity.ok(response)
    }

    // ================= TRANSACTIONS =================

    @GetMapping("/transactions")
    fun getMerchantTransactions(@RequestHeader("Authorization") token: String): ResponseEntity<List<TransactionResponse>> {

        val merchant = getCurrentMerchant(token)
        val transactions = transactionService.getMerchantTransactions(merchant.merchantId)

        return ResponseEntity.ok(transactions)
    }
}

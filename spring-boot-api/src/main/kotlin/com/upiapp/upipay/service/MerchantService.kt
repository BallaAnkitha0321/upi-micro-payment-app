package com.upiapp.upipay.service

import com.upiapp.upipay.dto.*
import com.upiapp.upipay.entity.Merchant
import com.upiapp.upipay.exception.ResourceNotFoundException
import com.upiapp.upipay.repository.MerchantRepository
import com.upiapp.upipay.repository.TransactionRepository
import com.upiapp.upipay.util.PhoneUtils
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class MerchantService(
    private val merchantRepository: MerchantRepository,
    private val transactionRepository: TransactionRepository,
    private val entityManager: EntityManager
) {

    // ================= FIND =================
    private fun findMerchantByPhone(phone: String?): Merchant? {

        val normalized = PhoneUtils.normalizePhone(phone)
            ?: return null

        println("🔥 FIND MERCHANT WITH = $normalized")

        val safeMerchant = merchantRepository.findByPhoneSafe(normalized)
        if (safeMerchant != null) {
            return safeMerchant
        }

        return merchantRepository.findAll()
            .firstOrNull { PhoneUtils.normalizePhone(it.phone) == normalized }
    }

    private fun getMerchantByPhone(phone: String?): Merchant {
        return findMerchantByPhone(phone)
            ?: throw ResourceNotFoundException("Merchant not found for $phone")
    }

    // ================= LOGIN / REGISTER =================
    fun loginOrRegister(
        name: String,
        phone: String,
        businessName: String?
    ): Merchant {

        val normalizedPhone = PhoneUtils.normalizePhone(phone)
            ?: throw RuntimeException("Invalid phone")

        val existingMerchant = merchantRepository.findByPhoneSafe(normalizedPhone)
            ?: merchantRepository.findAll().firstOrNull { PhoneUtils.normalizePhone(it.phone) == normalizedPhone }

        if (existingMerchant != null) {
            println("✅ EXISTING MERCHANT LOGIN: $normalizedPhone")
            return existingMerchant
        }

        return try {
            val newMerchant = Merchant(
                name = name,
                phone = normalizedPhone,
                upiId = generateUpiId(normalizedPhone),
                businessName = businessName
            )

            val saved = merchantRepository.save(newMerchant)

            println("✅ NEW MERCHANT CREATED: ${saved.phone}")

            saved

        } catch (e: Exception) {
            println("🔥 MERCHANT SAVE ERROR: ${e.message}")

            val collisionMerchant = merchantRepository.findByPhoneSafe(normalizedPhone)
                ?: merchantRepository.findAll().firstOrNull { PhoneUtils.normalizePhone(it.phone) == normalizedPhone }

            if (collisionMerchant != null) {
                println("✅ MERCHANT FOUND AFTER COLLISION: ${collisionMerchant.phone}")
                return collisionMerchant
            }

            throw RuntimeException("Merchant registration failed: ${e.message}")
        }
    }

    private fun generateUpiId(phone: String?): String {
        val normalizedPhone = phone
            ?.replace("+", "")
            ?.trim()
            ?: throw IllegalArgumentException("Invalid phone")

        val phoneWithoutCountryCode = if (normalizedPhone.startsWith("91")) {
            normalizedPhone.substring(2)
        } else {
            normalizedPhone
        }

        return "$phoneWithoutCountryCode@ybl"
    }

    // ================= BANK =================
    @Transactional
    fun linkBank(phone: String, request: MerchantBankRequest): MerchantStatusResponse {

        val merchant = getMerchantByPhone(phone)

        if (request.accountNumber.isBlank() ||
            request.ifscCode.isBlank() ||
            request.bankName.isBlank() ||
            request.accountHolderName.isBlank()
        ) {
            throw RuntimeException("Invalid bank details")
        }

        merchant.bankName = request.bankName
        merchant.accountNumber = request.accountNumber
        merchant.ifscCode = request.ifscCode
        merchant.accountHolderName = request.accountHolderName
        merchant.bankLinked = true
        merchant.settlementAccount = true

        merchantRepository.save(merchant)

        val updatedMerchant = getMerchantByPhone(phone)

        return MerchantStatusResponse(
            merchantId = updatedMerchant.merchantId,
            bankLinked = updatedMerchant.bankLinked,
            settlementAccount = updatedMerchant.settlementAccount,
            kycStatus = updatedMerchant.kycStatus,
            isBlocked = updatedMerchant.blocked,
            message = "Bank linked successfully"
        )
    }

    // ================= STATUS =================
    fun getMerchantStatus(phone: String): MerchantStatusResponse {

        val merchant = getMerchantByPhone(phone)

        return MerchantStatusResponse(
            merchantId = merchant.merchantId,
            bankLinked = merchant.bankLinked,
            settlementAccount = merchant.settlementAccount,
            kycStatus = merchant.kycStatus,
            isBlocked = merchant.blocked,
            message = "Status fetched successfully"
        )
    }

    // ================= KYC =================
    fun updateKyc(merchantId: Long, request: MerchantKycRequest) {

        val merchant = getMerchantById(merchantId)

        merchant.aadhaarNumber = request.aadhaarNumber
        merchant.panNumber = request.panNumber
        merchant.kycStatus = "VERIFIED"

        merchantRepository.save(merchant)
    }

    fun approveKyc(merchantId: Long) {
        val merchant = getMerchantById(merchantId)
        merchant.kycStatus = "VERIFIED"
        merchantRepository.save(merchant)
    }

    fun rejectKyc(merchantId: Long) {
        val merchant = getMerchantById(merchantId)
        merchant.kycStatus = "REJECTED"
        merchantRepository.save(merchant)
    }

    // ================= PROFILE UPDATE =================
    fun updateProfile(merchantId: Long, request: MerchantUpdateProfileRequest) {

        val merchant = getMerchantById(merchantId)

        println("🔥 UPDATE PROFILE CALLED")
        println("Category RECEIVED = ${request.businessCategory}")

        if (request.name.isNotBlank()) {
            merchant.name = request.name
        }

        merchant.email = request.email

        merchant.businessName = request.businessName

        // 🔥 FIXED HERE (ALWAYS UPDATE)
        merchant.businessCategory = request.businessCategory?.trim()

        merchant.businessAddress = request.businessAddress?.trim()

        println("🔥 SAVING CATEGORY = ${merchant.businessCategory}")

        merchantRepository.save(merchant)

        println("✅ PROFILE UPDATED SUCCESSFULLY")
    }

    // ================= PROFILE =================
    fun getMerchantProfile(phone: String?): MerchantProfileResponse {

        val merchant = getMerchantByPhone(phone)

        println("🔥 PROFILE FETCH SUCCESS = ${merchant.phone}")

        return MerchantProfileResponse(
            merchantId = merchant.merchantId,
            name = merchant.name,
            email = merchant.email,
            upiId = merchant.upiId,
            businessName = merchant.businessName,
            businessCategory = merchant.businessCategory,
            businessAddress = merchant.businessAddress,
            bankLinked = merchant.bankLinked,
            kycStatus = merchant.kycStatus,
            isBlocked = merchant.blocked,
            bankName = merchant.bankName,
            accountNumber = merchant.accountNumber,
            ifscCode = merchant.ifscCode
        )
    }

    // ================= DASHBOARD =================
    fun getMerchantDashboard(merchantId: Long): MerchantDashboardResponse {

        val merchant = getMerchantById(merchantId)

        if (!merchant.bankLinked) {
            return MerchantDashboardResponse(
                totalEarnings = BigDecimal.ZERO,
                totalTransactions = 0,
                todayEarnings = BigDecimal.ZERO
            )
        }

        val totalEarnings =
            transactionRepository.getTotalEarnings(merchantId) ?: BigDecimal.ZERO

        val totalTransactions =
            transactionRepository.getTotalTransactions(merchantId) ?: 0

        val startOfDay = LocalDate.now().atStartOfDay()

        val todayEarnings =
            transactionRepository.getTodayEarningsFromStartOfDay(
                merchantId,
                startOfDay
            ) ?: BigDecimal.ZERO

        return MerchantDashboardResponse(
            totalEarnings = totalEarnings,
            totalTransactions = totalTransactions,
            todayEarnings = todayEarnings
        )
    }

    // ================= COMMON =================
    fun getMerchantById(merchantId: Long): Merchant {
        return merchantRepository.findById(merchantId)
            .orElseThrow {
                RuntimeException("Merchant not found for ID: $merchantId")
            }
    }
}
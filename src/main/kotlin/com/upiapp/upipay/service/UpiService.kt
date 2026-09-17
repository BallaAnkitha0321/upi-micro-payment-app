package com.upiapp.upipay.service

import com.upiapp.upipay.dto.UpiVerifyResponse
import com.upiapp.upipay.repository.MerchantRepository
import com.upiapp.upipay.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UpiService(
    private val userRepository: UserRepository,
    private val merchantRepository: MerchantRepository
) {

    fun verifyUpi(upiId: String): UpiVerifyResponse {

        // ✅ Normalize input
        val normalizedUpi = upiId.trim().lowercase()

        if (!normalizedUpi.matches(Regex("^[0-9]{10}@ybl$"))) {
            throw IllegalArgumentException("Invalid UPI format")
        }

        // ✅ CHECK USER FIRST (unchanged logic)
        val user = userRepository.findByUpiId(normalizedUpi)
        if (user != null) {
            return UpiVerifyResponse(
                isValid = true,
                name = user.name?.takeIf { it.isNotBlank() } ?: "User",
                upiId = normalizedUpi,
                type = "USER"
            )
        }

        // ✅ FIXED MERCHANT LOOKUP (IMPORTANT CHANGE)
        val merchant = merchantRepository.findByUpiIdSafe(normalizedUpi)
        if (merchant != null) {
            return UpiVerifyResponse(
                isValid = true,
                name = merchant.businessName?.takeIf { it.isNotBlank() }
                    ?: merchant.name?.takeIf { it.isNotBlank() }
                    ?: "Merchant",
                upiId = normalizedUpi,
                type = "MERCHANT"
            )
        }

        // ✅ NOT FOUND → RETURN DEFAULT (NO ERROR)
        return UpiVerifyResponse(
            isValid = false,
            name = null,
            upiId = normalizedUpi,
            type = "UNKNOWN"
        )
    }
}
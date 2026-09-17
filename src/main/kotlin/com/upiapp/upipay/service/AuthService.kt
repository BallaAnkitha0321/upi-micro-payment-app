package com.upiapp.upipay.service

import com.upiapp.upipay.dto.*
import com.upiapp.upipay.entity.Merchant
import com.upiapp.upipay.entity.User
import com.upiapp.upipay.repository.MerchantRepository
import com.upiapp.upipay.repository.UserRepository
import com.upiapp.upipay.security.JwtService
import com.upiapp.upipay.util.PhoneUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

@Service
class AuthService(

    private val userRepository: UserRepository,
    private val merchantRepository: MerchantRepository,
    private val otpService: OtpService,
    private val jwtService: JwtService

) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    private fun findUserByPhone(phone: String): User? {

        val normalized = PhoneUtils.normalizePhone(phone) ?: return null

        logger.info("Finding user by normalized phone: $normalized")

        val safeUser = userRepository.findByPhoneSafe(normalized)
        if (safeUser != null) return safeUser

        return userRepository.findAll()
            .firstOrNull { PhoneUtils.normalizePhone(it.phone) == normalized }
    }

    private fun findMerchantByPhone(phone: String): Merchant? {

        val normalized = PhoneUtils.normalizePhone(phone) ?: return null

        logger.info("Finding merchant by normalized phone: $normalized")

        val safeMerchant = merchantRepository.findByPhoneSafe(normalized)
        if (safeMerchant != null) return safeMerchant

        return merchantRepository.findAll()
            .firstOrNull { PhoneUtils.normalizePhone(it.phone) == normalized }
    }

    // ================= SEND OTP =================
    fun sendOtp(request: OtpRequest): ApiResponse<String> {

        val phone = PhoneUtils.normalizePhone(request.phone)
            ?: throw IllegalArgumentException("Invalid phone")

        val otp = otpService.generateOtp(phone)

        logger.info("OTP generated for $phone : $otp")

        return ApiResponse(
            success = true,
            message = "OTP sent successfully",
            data = "OTP sent successfully"
        )
    }

    // ================= VERIFY OTP =================
    fun verifyOtp(request: OtpVerifyRequest): LoginResponse {

        val phone = PhoneUtils.normalizePhone(request.phone)
            ?: throw IllegalArgumentException("Invalid phone")

        // 🔥 REAL OTP VALIDATION (USE YOUR SERVICE)
        val isValid = otpService.verifyOtp(phone, request.otp)

        if (!isValid) {
            logger.warn("Invalid OTP attempt for $phone")
            throw IllegalArgumentException("INVALID_OTP")
        }

        val user = findUserByPhone(phone)
        val merchant = findMerchantByPhone(phone)

        // 🔥 BLOCK CHECK
        if (user != null && user.blocked) {
            logger.warn("Blocked user login attempt: $phone")
            throw RuntimeException("ACCOUNT_RESTRICTED")
        }

        if (merchant != null && merchant.blocked) {
            logger.warn("Blocked merchant login attempt: $phone")
            throw RuntimeException("ACCOUNT_RESTRICTED")
        }

        val token = jwtService.generateToken(phone)

        if (user == null && merchant == null) {
            logger.info("New user → ROLE_SELECTION: $phone")

            return LoginResponse(
                role = "ROLE_SELECTION",
                userId = null,
                merchantId = null,
                name = null,
                email = null,
                upiId = null,
                message = "Select role",
                token = token
            )
        }

        if (user != null && merchant == null) {
            logger.info("User login: $phone")

            return LoginResponse(
                role = "USER",
                userId = user.userId,
                merchantId = null,
                name = user.name,
                email = user.email,
                upiId = user.upiId,
                message = "User login successful",
                token = token
            )
        }

        if (user == null && merchant != null) {
            logger.info("Merchant login: $phone")

            return LoginResponse(
                role = "MERCHANT",
                userId = null,
                merchantId = merchant.merchantId,
                name = merchant.name,
                email = merchant.email,
                upiId = merchant.upiId,
                message = "Merchant login successful",
                token = token
            )
        }

        logger.info("Both roles exist → ROLE_SELECTION: $phone")

        return LoginResponse(
            role = "ROLE_SELECTION",
            userId = user?.userId,
            merchantId = merchant?.merchantId,
            name = user?.name ?: merchant?.name,
            email = user?.email ?: merchant?.email,
            upiId = user?.upiId ?: merchant?.upiId,
            message = "Select role",
            token = token
        )
    }

    // ================= REGISTER USER =================
    fun registerUser(request: UserRegisterRequest): LoginResponse {

        val phone = PhoneUtils.normalizePhone(request.phone)
            ?: throw IllegalArgumentException("Invalid phone")

        val existingUser = findUserByPhone(phone)

        if (existingUser != null) {

            if (existingUser.blocked) {
                throw RuntimeException("ACCOUNT_RESTRICTED")
            }

            val token = jwtService.generateToken(phone)

            return LoginResponse(
                role = "USER",
                userId = existingUser.userId,
                merchantId = null,
                name = existingUser.name,
                email = existingUser.email,
                upiId = existingUser.upiId,
                message = "User already exists",
                token = token
            )
        }

        val randomBalance = Random.nextDouble(1000.0, 3500.0)

        val balance = BigDecimal(randomBalance)
            .setScale(2, RoundingMode.HALF_UP)

        val user = User(
            name = request.name,
            phone = phone,
            email = request.email,   // ✅ ADD THIS LINE
            balance = balance
        )

        val savedUser = userRepository.save(user)

        val token = jwtService.generateToken(phone)

        return LoginResponse(
            role = "USER",
            userId = savedUser.userId,
            merchantId = null,
            name = savedUser.name,
            email = savedUser.email,
            upiId = savedUser.upiId,
            message = "User Registered",
            token = token
        )
    }

    // ================= REGISTER MERCHANT =================
    fun registerMerchant(request: MerchantRegisterRequest): LoginResponse {

        if (request.name.isBlank() || request.phone.isBlank()) {
            throw RuntimeException("Name and Phone are required")
        }

        val phone = PhoneUtils.normalizePhone(request.phone)
            ?: throw IllegalArgumentException("Invalid phone")

        val existingMerchant = findMerchantByPhone(phone)

        if (existingMerchant != null) {

            if (existingMerchant.blocked) {
                throw RuntimeException("ACCOUNT_RESTRICTED")
            }

            val token = jwtService.generateToken(phone)

            return LoginResponse(
                role = "MERCHANT",
                userId = null,
                merchantId = existingMerchant.merchantId,
                name = existingMerchant.name,
                email = existingMerchant.email,
                upiId = existingMerchant.upiId,
                message = "Merchant already exists",
                token = token
            )
        }

        val upiId = generateUpiId(phone)

        val merchant = Merchant(
            name = request.name,
            phone = phone,
            upiId = upiId,
            businessName = request.businessName ?: request.name,
            businessCategory = request.businessCategory?.trim(),
            bankLinked = false,
            revenue = BigDecimal.ZERO
        )

        val savedMerchant = merchantRepository.save(merchant)

        val token = jwtService.generateToken(phone)

        return LoginResponse(
            role = "MERCHANT",
            userId = null,
            merchantId = savedMerchant.merchantId,
            name = savedMerchant.name,
            email = savedMerchant.email,
            upiId = savedMerchant.upiId,
            message = "Merchant Registered",
            token = token
        )
    }

    private fun generateUpiId(phone: String): String {
        val cleanPhone = phone
            .replace("+91", "")
            .replace(" ", "")
            .trim()

        return "$cleanPhone@ybl"
    }
}
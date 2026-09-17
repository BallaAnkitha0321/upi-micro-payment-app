package com.upiapp.upipay.controller

import com.upiapp.upipay.dto.*
import com.upiapp.upipay.service.AuthService
import com.upiapp.upipay.repository.UserRepository
import com.upiapp.upipay.repository.MerchantRepository
import com.upiapp.upipay.util.PhoneUtils
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val merchantRepository: MerchantRepository
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    // ================= SEND OTP =================
    @PostMapping("/send-otp")
    fun sendOtp(@RequestBody request: OtpRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            logger.info("Received OTP request for phone: ${request.phone}")

            val normalizedPhone = PhoneUtils.normalizePhone(request.phone)
                ?: throw IllegalArgumentException("Invalid phone format")

            logger.info("Normalized phone: $normalizedPhone")

            val updatedRequest = request.copy(phone = normalizedPhone)

            val response = authService.sendOtp(updatedRequest)

            logger.info("OTP sent successfully to $normalizedPhone")

            ResponseEntity.ok(response)

        } catch (e: IllegalArgumentException) {
            logger.error("Validation error: ${e.message}")

            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = e.message ?: "Invalid input",
                    data = e.message ?: "Invalid input"
                )
            )

        } catch (e: Exception) {
            logger.error("Send OTP failed: ${e.message}", e)

            ResponseEntity.internalServerError().body(
                ApiResponse(
                    success = false,
                    message = "Failed to send OTP",
                    data = "Failed to send OTP"
                )
            )
        }
    }

    // ================= VERIFY OTP =================
    @PostMapping("/verify-otp")
    fun verifyOtp(@RequestBody request: OtpVerifyRequest): ResponseEntity<LoginResponse> {
        return try {
            logger.info("OTP verification started for phone: ${request.phone}")

            val normalizedPhone = PhoneUtils.normalizePhone(request.phone)
                ?: throw IllegalArgumentException("Invalid phone format")

            logger.info("Normalized phone: $normalizedPhone")

            val updatedRequest = request.copy(phone = normalizedPhone)

            val response = authService.verifyOtp(updatedRequest)

            if (response.token.isNullOrEmpty()) {
                logger.error("JWT token generation failed for $normalizedPhone")

                return ResponseEntity.internalServerError().body(
                    LoginResponse(
                        role = "",
                        userId = null,
                        merchantId = null,
                        message = "Token generation failed",
                        name = null,
                        email = null,
                        upiId = null,
                        token = null
                    )
                )
            }

            val userExists = userRepository.findByPhoneSafe(normalizedPhone) != null
            val merchantExists = merchantRepository.findByPhoneSafe(normalizedPhone) != null

            logger.info("User exists: $userExists, Merchant exists: $merchantExists")

            val finalRole = when {
                userExists && merchantExists -> "ROLE_SELECTION"
                merchantExists -> "MERCHANT"
                userExists -> "USER"
                else -> "ROLE_SELECTION"
            }

            logger.info("Final role assigned: $finalRole")

            val fixedResponse = response.copy(role = finalRole)

            ResponseEntity.ok(fixedResponse)

        } catch (e: IllegalArgumentException) {

            logger.error("Validation error: ${e.message}")

            ResponseEntity.badRequest().body(
                LoginResponse(
                    role = "",
                    userId = null,
                    merchantId = null,
                    message = e.message ?: "Invalid input",
                    name = null,
                    email = null,
                    upiId = null,
                    token = null
                )
            )

        } catch (e: RuntimeException) {
            logger.error("Runtime exception: ${e.message}")
            throw e

        } catch (e: Exception) {

            logger.error("Verify OTP failed: ${e.message}", e)

            ResponseEntity.internalServerError().body(
                LoginResponse(
                    role = "",
                    userId = null,
                    merchantId = null,
                    message = "OTP verification failed",
                    name = null,
                    email = null,
                    upiId = null,
                    token = null
                )
            )
        }
    }

    // ================= USER REGISTER =================
    @PostMapping("/register/user")
    fun registerUser(@RequestBody request: UserRegisterRequest): ResponseEntity<LoginResponse> {
        return try {
            logger.info("User registration started: ${request.phone}")

            val normalizedPhone = PhoneUtils.normalizePhone(request.phone)
                ?: throw IllegalArgumentException("Invalid phone format")

            val updatedRequest = request.copy(phone = normalizedPhone)

            val response = authService.registerUser(updatedRequest)

            if (response.token.isNullOrEmpty()) {
                return ResponseEntity.internalServerError().body(
                    LoginResponse(
                        role = "",
                        userId = null,
                        merchantId = null,
                        message = "Token generation failed",
                        name = null,
                        email = null,
                        upiId = null,
                        token = null
                    )
                )
            }

            val fixedResponse = response.copy(role = "USER")

            ResponseEntity.ok(fixedResponse)

        } catch (e: IllegalArgumentException) {

            ResponseEntity.badRequest().body(
                LoginResponse(
                    role = "",
                    userId = null,
                    merchantId = null,
                    message = e.message ?: "Invalid input",
                    name = null,
                    email = null,
                    upiId = null,
                    token = null
                )
            )

        } catch (e: Exception) {

            ResponseEntity.internalServerError().body(
                LoginResponse(
                    role = "",
                    userId = null,
                    merchantId = null,
                    message = "User registration failed",
                    name = null,
                    email = null,
                    upiId = null,
                    token = null
                )
            )
        }
    }

    // ================= MERCHANT REGISTER =================
    @PostMapping("/register/merchant")
    fun registerMerchant(@RequestBody request: MerchantRegisterRequest): ResponseEntity<LoginResponse> {
        return try {

            val normalizedPhone = PhoneUtils.normalizePhone(request.phone)
                ?: throw IllegalArgumentException("Invalid phone format")

            val updatedRequest = request.copy(phone = normalizedPhone)

            val response = authService.registerMerchant(updatedRequest)

            if (response.token.isNullOrEmpty()) {
                return ResponseEntity.internalServerError().body(
                    LoginResponse(
                        role = "",
                        userId = null,
                        merchantId = null,
                        message = "Token generation failed",
                        name = null,
                        email = null,
                        upiId = null,
                        token = null
                    )
                )
            }

            val fixedResponse = response.copy(role = "MERCHANT")

            ResponseEntity.ok(fixedResponse)

        } catch (e: IllegalArgumentException) {

            ResponseEntity.badRequest().body(
                LoginResponse(
                    role = "",
                    userId = null,
                    merchantId = null,
                    message = e.message ?: "Invalid input",
                    name = null,
                    email = null,
                    upiId = null,
                    token = null
                )
            )

        } catch (e: Exception) {

            ResponseEntity.internalServerError().body(
                LoginResponse(
                    role = "",
                    userId = null,
                    merchantId = null,
                    message = "Merchant registration failed",
                    name = null,
                    email = null,
                    upiId = null,
                    token = null
                )
            )
        }
    }
}
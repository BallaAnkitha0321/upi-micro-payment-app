package com.upiapp.upipay.controller

import com.upiapp.upipay.dto.SetPinRequest
import com.upiapp.upipay.dto.UserResponse
import com.upiapp.upipay.dto.VerifyPinRequest
import com.upiapp.upipay.model.*
import com.upiapp.upipay.service.UserService
import com.upiapp.upipay.security.JwtService
import com.upiapp.upipay.util.PhoneUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val jwtService: JwtService
) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)

    // ================= GET USER PROFILE BY PHONE =================
    @GetMapping("/status/{phone}")
    fun getUserByPhone(@PathVariable phone: String): ResponseEntity<UserResponse> {

        val user = userService.getUserByPhone(phone)
        val response = userService.mapToUserResponse(user)

        return ResponseEntity.ok(response)
    }

    // ================= GET USER PROFILE FROM JWT =================
    @GetMapping("/profile")
    fun getProfile(@RequestHeader("Authorization") token: String): ResponseEntity<UserResponse> {

        val phone = jwtService.extractPhone(token)
            ?: throw IllegalArgumentException("Invalid or missing JWT token")

        val normalizedPhone = PhoneUtils.normalizePhone(phone)
            ?: throw IllegalArgumentException("Invalid phone format in token: $phone")

        logger.info("Extracted phone from JWT for user profile: $phone")
        logger.info("Normalized phone for lookup: $normalizedPhone")

        val user = userService.findUserByPhone(normalizedPhone)

        return if (user != null) {
            logger.info("User lookup result for $normalizedPhone: found")
            ResponseEntity.ok(userService.mapToUserResponse(user))
        } else {
            logger.info("User lookup result for $normalizedPhone: not found")
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // ================= GET USER BLOCK STATUS =================
    // 🔥 FIXED PATH (NO CONFLICT)
    @GetMapping("/status/id/{userId}")
    fun getUserBlockedStatus(@PathVariable userId: Long): ResponseEntity<UserBlockedStatusResponse> {

        val user = userService.getUserById(userId)

        val response = UserBlockedStatusResponse(
            userId = user.userId,
            blocked = user.blocked
        )

        return ResponseEntity.ok(response)
    }

    // ================= UPDATE PROFILE =================
    @PostMapping("/update-profile")
    fun updateProfile(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {

        val phone = jwtService.extractPhone(token)
            ?: throw RuntimeException("Invalid token")

        val user = userService.updateProfileByPhone(phone, request.name, request.email)

        val response = userService.mapToUserResponse(user)

        return ResponseEntity.ok(response)
    }

    // ================= LINK BANK =================
    @PostMapping("/link-bank")
    fun linkBank(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: LinkBankRequest
    ): ResponseEntity<UserResponse> {

        val phone = jwtService.extractPhone(token)
            ?: throw RuntimeException("Invalid token")

        val user = userService.linkBankByPhone(phone, request)

        val response = userService.mapToUserResponse(user)

        return ResponseEntity.ok(response)
    }

    // ================= KYC =================
    @PostMapping("/kyc")
    fun updateKyc(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: KycRequest
    ): ResponseEntity<UserResponse> {

        val phone = jwtService.extractPhone(token)
            ?: throw RuntimeException("Invalid token")

        val user = userService.updateKycByPhone(phone, request)

        val response = userService.mapToUserResponse(user)

        return ResponseEntity.ok(response)
    }

    // ================= CHECK BALANCE =================
    @GetMapping("/balance/{userId}")
    fun getUserBalance(@PathVariable userId: Long): ResponseEntity<Any> {
        val balance = userService.getUserBalance(userId)
        return ResponseEntity.ok(
            mapOf(
                "status" to "SUCCESS",
                "balance" to balance
            )
        )
    }

    @PostMapping("/set-pin")
    fun setPin(
        @RequestBody request: SetPinRequest,
        @AuthenticationPrincipal phone: String
    ): ResponseEntity<Any> {

        if (request.pin.length != 4) {
            return ResponseEntity.badRequest().body("PIN must be 4 digits")
        }

        userService.setUpiPin(phone, request.pin)

        return ResponseEntity.ok(mapOf("message" to "PIN set successfully"))
    }

    @PostMapping("/verify-pin")
    fun verifyPin(
        @RequestBody request: VerifyPinRequest,
        @AuthenticationPrincipal phone: String
    ): ResponseEntity<Any> {

        val isValid = userService.verifyUpiPin(phone, request.pin)

        return ResponseEntity.ok(mapOf("valid" to isValid))
    }
}

// ================= RESPONSE MODEL =================
data class UserBlockedStatusResponse(
    val userId: Long,
    val blocked: Boolean
)



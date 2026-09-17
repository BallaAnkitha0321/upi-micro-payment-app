package com.upiapp.upipay.service

import com.upiapp.upipay.entity.User
import com.upiapp.upipay.dto.UserResponse
import com.upiapp.upipay.exception.ResourceNotFoundException
import com.upiapp.upipay.model.*
import com.upiapp.upipay.repository.UserRepository
import com.upiapp.upipay.util.PhoneUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class UserService(
    private val userRepository: UserRepository
) {

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    // ================= LOGIN / REGISTER =================
    fun loginOrRegister(phone: String, name: String?): User {

        val normalizedPhone = PhoneUtils.normalizePhone(phone)
            ?: throw RuntimeException("Invalid phone")

        var user = userRepository.findByPhoneSafe(normalizedPhone)

        if (user == null) {
            user = User(
                phone = normalizedPhone,
                name = name ?: "User",
                balance = BigDecimal.ZERO,
                kycVerified = false,
                upiId = generateUpiId(normalizedPhone)
            )
        } else {
            if (!name.isNullOrBlank()) {
                user.name = name
            }
        }

        return userRepository.save(user)
    }

    // ================= GET USER =================
    fun findUserByPhone(phone: String): User? {

        val normalizedPhone = PhoneUtils.normalizePhone(phone)
            ?: return null

        val safeUser = userRepository.findByPhoneSafe(normalizedPhone)
        if (safeUser != null) {
            return safeUser
        }

        return userRepository.findAll()
            .firstOrNull { PhoneUtils.normalizePhone(it.phone) == normalizedPhone }
    }

    fun getUserByPhone(phone: String): User {
        return findUserByPhone(phone)
            ?: throw ResourceNotFoundException("User not found for $phone")
    }

    fun getUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }
    }

    // ================= UPDATE PROFILE =================
    fun updateProfileByPhone(
        phone: String,
        name: String?,
        email: String?
    ): User {

        val user = getUserByPhone(phone)

        if (!name.isNullOrBlank()) {
            user.name = name
        }

        if (!email.isNullOrBlank()) {
            user.email = email
        }

        return userRepository.save(user)
    }

    // ================= LINK BANK =================
    fun linkBankByPhone(phone: String, request: LinkBankRequest): User {

        val user = getUserByPhone(phone)

        user.bankName = request.bankName
        user.accountNumber = request.accountNumber
        user.ifscCode = request.ifscCode
        user.bankLinked = true

        return userRepository.save(user)
    }

    fun linkBank(request: LinkBankRequest): User {

        val user = getUserById(request.userId)

        user.bankName = request.bankName
        user.accountNumber = request.accountNumber
        user.ifscCode = request.ifscCode
        user.bankLinked = true

        return userRepository.save(user)
    }

    // ================= KYC =================
    fun updateKycByPhone(phone: String, request: KycRequest): User {

        val user = getUserByPhone(phone)

        user.kycVerified = true

        return userRepository.save(user)
    }

    fun updateKyc(request: KycRequest): User {

        val user = getUserById(request.userId)

        user.kycVerified = true

        return userRepository.save(user)
    }

    // ================= BALANCE =================
    fun getUserBalance(userId: Long): BigDecimal {

        val user = getUserById(userId)

        return user.balance ?: BigDecimal.ZERO
    }

    // ================= 🔥 NEW FIX =================
    fun mapToUserResponse(user: User): UserResponse {

        return UserResponse(
            userId = user.userId,
            name = user.name,
            phone = user.phone,
            upiId = user.upiId,
            email = user.email,
            bankName = user.bankName,
            accountNumber = user.accountNumber,
            ifscCode = user.ifscCode,
            balance = user.balance ?: BigDecimal.ZERO,
            bankLinked = user.bankLinked,
            kycVerified = user.kycVerified, // 🔥 MAIN FIX
            isBlocked = user.blocked,
            token = user.token
        )
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

    fun setUpiPin(phone: String, pin: String) {
        val user = userRepository.findByPhone(phone)
            ?: throw RuntimeException("User not found")

        user.upiPin = passwordEncoder.encode(pin)
        userRepository.save(user)
    }

    fun verifyUpiPin(phone: String, enteredPin: String): Boolean {
        val user = userRepository.findByPhone(phone)
            ?: throw RuntimeException("User not found")

        if (user.upiPin == null) {
            throw RuntimeException("PIN not set")
        }

        return passwordEncoder.matches(enteredPin, user.upiPin)
    }

    fun mapToUserStatusResponse(user: User): UserStatusResponse {
        val hasPin = user.upiPin != null

        return UserStatusResponse(
            bankLinked = user.bankLinked,
            kycVerified = user.kycVerified,
            hasUpiPin = hasPin
        )
    }
}

package com.upiapp.upipay.service

import com.upiapp.upipay.entity.Otp
import com.upiapp.upipay.repository.OtpRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class OtpService(
    private val otpRepository: OtpRepository
) {

    private val logger = LoggerFactory.getLogger(OtpService::class.java)

    // Generate OTP
    fun generateOtp(phone: String): String {

        val otp = Random.nextInt(100000, 999999).toString()

        logger.info("Generated OTP for phone={}", phone)

        saveOtp(phone, otp)

        return otp
    }

    // Save OTP in database
    fun saveOtp(phone: String, otp: String) {

        val otpEntity = Otp(
            phone = phone,
            otpCode = otp,
            createdAt = LocalDateTime.now()
        )

        otpRepository.save(otpEntity)

        logger.info("OTP saved in database for phone={}", phone)
    }

    // Verify OTP
    fun verifyOtp(phone: String, enteredOtp: String): Boolean {

        val latestOtp = otpRepository
            .findTopByPhoneOrderByCreatedAtDesc(phone)

        if (latestOtp == null) {

            logger.warn("OTP not found for phone={}", phone)
            return false
        }

        val expiryTime = latestOtp.createdAt.plusMinutes(5)

        if (LocalDateTime.now().isAfter(expiryTime)) {

            logger.warn("OTP expired for phone={}", phone)
            return false
        }

        val isValid = latestOtp.otpCode == enteredOtp

        if (isValid) {
            logger.info("OTP verified successfully for phone={}", phone)
        } else {
            logger.warn("Invalid OTP attempt for phone={}", phone)
        }

        return isValid
    }
}
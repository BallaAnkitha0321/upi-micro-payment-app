package com.upiapp.upipay.security

import com.upiapp.upipay.util.PhoneUtils
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {

    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    // 🔐 Strong secret key (must be at least 256 bits)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(
        "upipay-secret-key-for-jwt-token-generation-256-bits-minimum".toByteArray()
    )

    private val expirationTime = 1000 * 60 * 60 * 24 // 1 day

    // ================= GENERATE TOKEN =================
    // ✅ PHONE BASED TOKEN (FINAL FIX)

    fun generateToken(phone: String): String {
        val subject = PhoneUtils.normalizePhone(phone) ?: phone
        logger.info("Generating JWT token for subject=$subject (raw=$phone)")

        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    // ================= EXTRACT PHONE =================

    fun extractPhone(token: String): String? {
        return try {
            val rawSubject = extractAllClaims(cleanToken(token)).subject
            if (rawSubject.isNullOrBlank()) {
                logger.warn("JWT token subject is empty")
                return null
            }

            val normalizedSubject = PhoneUtils.normalizePhone(rawSubject)
            if (normalizedSubject != null) {
                logger.info("Extracted normalized phone from JWT subject=$rawSubject → $normalizedSubject")
                return normalizedSubject
            }

            logger.info("Extracted non-phone JWT subject=$rawSubject")
            return rawSubject
        } catch (e: Exception) {
            logger.warn("Failed to extract phone from JWT: ${e.message}")
            null
        }
    }

    // ================= VALIDATE TOKEN =================

    fun isTokenValid(token: String, phone: String): Boolean {
        return try {
            val extractedPhone = extractPhone(token)
            val normalizedPhone = PhoneUtils.normalizePhone(phone) ?: phone
            extractedPhone == normalizedPhone && !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }

    // ================= CHECK EXPIRY =================

    private fun isTokenExpired(token: String): Boolean {
        return extractAllClaims(cleanToken(token)).expiration.before(Date())
    }

    // ================= CLEAN TOKEN =================

    private fun cleanToken(token: String): String {
        return if (token.startsWith("Bearer ")) {
            token.substring(7)
        } else {
            token
        }
    }

    // ================= EXTRACT CLAIMS =================

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
}
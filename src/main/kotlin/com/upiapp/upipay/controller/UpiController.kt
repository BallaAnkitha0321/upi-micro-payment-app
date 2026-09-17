package com.upiapp.upipay.controller

import com.upiapp.upipay.dto.UpiVerifyResponse
import com.upiapp.upipay.service.UpiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/upi")
class UpiController(
    private val upiService: UpiService
) {

    // ✅ VERIFY / RESOLVE UPI (USED FOR NAME DISPLAY)
    @GetMapping("/verify")
    fun verifyUpi(@RequestParam upiId: String): ResponseEntity<UpiVerifyResponse> {

        println("🔥 UPI VERIFY HIT: $upiId")

        return try {

            val normalizedUpi = upiId.trim().lowercase()

            val response = upiService.verifyUpi(normalizedUpi)

            println("✅ UPI RESOLVED: ${response.name}")

            ResponseEntity.ok(response)

        } catch (e: Exception) {

            println("❌ UPI VERIFY FAILED: ${e.message}")

            // ✅ IMPORTANT: RETURN SAFE RESPONSE (NOT 400)
            ResponseEntity.ok(
                UpiVerifyResponse(
                    isValid = false,
                    name = null,
                    upiId = upiId,
                    type = "UNKNOWN"
                )
            )
        }
    }
}
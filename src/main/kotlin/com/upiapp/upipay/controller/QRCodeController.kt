package com.upiapp.upipay.controller

import com.upiapp.upipay.entity.Merchant
import com.upiapp.upipay.service.QRCodeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/qr")
class QRCodeController(
    private val qrCodeService: QRCodeService
) {

    @GetMapping("/verify")
    fun verifyQR(@RequestParam qrData: String): ResponseEntity<Merchant> {

        val merchant = qrCodeService.verifyQR(qrData)

        return if (merchant != null) {
            ResponseEntity.ok(merchant)
        } else {
            ResponseEntity.notFound().build()
        }

    }
}
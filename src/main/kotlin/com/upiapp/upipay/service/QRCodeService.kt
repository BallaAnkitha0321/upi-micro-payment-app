package com.upiapp.upipay.service

import com.upiapp.upipay.entity.Merchant
import com.upiapp.upipay.repository.MerchantRepository
import org.springframework.stereotype.Service
import java.net.URI

@Service
class QRCodeService(
    private val merchantRepository: MerchantRepository
) {

    fun verifyQR(qrData: String): Merchant? {

        val upiId = extractUpiId(qrData)

        if (upiId == null) {
            return null
        }

        return merchantRepository.findByUpiId(upiId)
    }

    private fun extractUpiId(qrData: String): String? {

        return try {

            if (qrData.startsWith("upi://")) {

                val uri = URI(qrData)
                val query = uri.query ?: return null

                val params = query.split("&")

                for (param in params) {

                    val parts = param.split("=")

                    if (parts.size == 2 && parts[0] == "pa") {
                        return parts[1]
                    }
                }

                null

            } else if (qrData.contains("@")) {

                qrData

            } else {

                null
            }

        } catch (e: Exception) {
            null
        }
    }
}
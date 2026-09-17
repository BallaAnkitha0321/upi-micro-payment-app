package com.upiapp.upipay.repository

import com.upiapp.upipay.entity.QRCode
import org.springframework.data.jpa.repository.JpaRepository

interface QRCodeRepository : JpaRepository<QRCode, Long> {

    fun findByQrData(qrData: String): QRCode?

    fun findByMerchantMerchantId(merchantId: Long): List<QRCode>
}
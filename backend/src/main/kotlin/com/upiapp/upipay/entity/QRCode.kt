package com.upiapp.upipay.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "qr_codes")
data class QRCode(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val qrId: Long = 0,

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    val merchant: Merchant,

    // Actual QR content (UPI URL or encoded payload)
    @Column(nullable = false, unique = true, length = 500)
    val qrData: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val qrType: QRType,

    // Amount only for dynamic QR
    val amount: Double? = null,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class QRType {
    STATIC,
    DYNAMIC
}
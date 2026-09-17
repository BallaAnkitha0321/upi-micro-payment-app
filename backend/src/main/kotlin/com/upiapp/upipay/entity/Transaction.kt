package com.upiapp.upipay.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime
import java.math.BigDecimal

@Entity
@Table(name = "transactions")
data class Transaction(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "txn_id")
    val txnId: Long = 0,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    var merchant: Merchant? = null,

    @Column(nullable = false, precision = 12, scale = 2)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TxnStatus = TxnStatus.PENDING,

    @Column(name = "txn_ref", unique = true)
    var txnRef: String? = null,

    @Column(name = "raw_response", columnDefinition = "TEXT")
    var rawResponse: String? = null,

    @Column(name = "response_code")
    var responseCode: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    // ================= FRAUD =================
    @Column(name = "fraud_flag", nullable = false)
    var fraudFlag: Boolean = false,

    @Column(name = "fraud_reason")
    var fraudReason: String? = null,

    @Column(name = "risk_score", nullable = false)
    var riskScore: Int = 0,

    @Column(name = "is_qr", nullable = false)
    var isQr: Boolean = false,

    // ================= NEW FIELDS (IMPORTANT) =================

    @Column(name = "sender_name")
    var senderName: String? = null,

    @Column(name = "receiver_name")
    var receiverName: String? = null
)
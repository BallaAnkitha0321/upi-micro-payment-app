package com.upiapp.upipay.util

object PhoneUtils {
    fun normalizePhone(phone: String?): String? {
        val digits = phone
            ?.replace("\\D".toRegex(), "")
            ?: return null

        if (digits.length < 10) {
            return null
        }

        return "+91${digits.takeLast(10)}"
    }
}

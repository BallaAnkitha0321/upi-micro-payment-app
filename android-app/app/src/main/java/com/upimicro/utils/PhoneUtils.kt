package com.upimicro.utils

object PhoneUtils {
    fun normalizePhone(phone: String?): String? {
        if (phone.isNullOrBlank()) return null

        var p = phone.trim().replace(" ", "")

        return when {
            p.startsWith("+91") -> p
            p.startsWith("91") && p.length == 12 -> "+$p"
            p.length == 10 -> "+91$p"
            else -> {
                if (p.startsWith("+")) p else "+91$p"
            }
        }
    }
}

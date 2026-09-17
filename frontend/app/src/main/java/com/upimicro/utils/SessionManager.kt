package com.upimicro.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME = "upipay_prefs"

        private const val KEY_TOKEN = "token"
        private const val KEY_ROLE = "role"
        private const val KEY_PHONE = "phone"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_AVAILABLE_ROLES = "available_roles"

        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_UPI = "user_upi"

        private const val KEY_MERCHANT_ID = "merchant_id"
        private const val KEY_MERCHANT_NAME = "merchant_name"
        private const val KEY_MERCHANT_EMAIL = "merchant_email"
        private const val KEY_MERCHANT_UPI = "merchant_upi"
        private const val KEY_MERCHANT_BUSINESS_NAME = "merchant_business_name"

        private const val KEY_BANK_LINKED = "bank_linked"
        private const val KEY_BANK_NAME = "bank_name"
        private const val KEY_ACCOUNT_NUMBER = "account_number"
        private const val KEY_IFSC_CODE = "ifsc_code"

        private const val KEY_KYC_VERIFIED = "kyc_verified"
        private const val KEY_PIN_SET = "pin_set"

        private const val KEY_IS_BLOCKED = "is_blocked"

        private const val KEY_BANNER_DISMISSED = "banner_dismissed"

        private const val KEY_IS_USER_REGISTERED = "is_user_registered"
        private const val KEY_IS_MERCHANT_REGISTERED = "is_merchant_registered"
        private const val KEY_LAST_USED_ROLE = "last_used_role"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ========================================
    // 🔐 AUTH
    // ========================================

    fun saveFullSession(token: String?, role: String, phone: String) {
        val finalToken = token ?: getToken()
        if (finalToken.isEmpty()) {
            Log.e("SESSION", "❌ Cannot save session: Token is empty")
            return
        }

        val editor = prefs.edit()
        editor.putString(KEY_TOKEN, finalToken)
        editor.putString(KEY_ROLE, role)
        editor.putString(KEY_PHONE, phone)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_LAST_USED_ROLE, role)
        
        // Critical for persistence
        val success = editor.commit()
        
        Log.d("SESSION", "✅ Full Session saved ($success): role=$role phone=$phone token=${finalToken.take(10)}...")
    }

    fun saveToken(token: String?) {
        if (token.isNullOrEmpty()) {
            Log.e("SESSION", "❌ Attempted to save null or empty token")
            return
        }
        val editor = prefs.edit()
        editor.putString(KEY_TOKEN, token)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        val success = editor.commit()
        Log.d("SESSION", "✅ Token saved ($success): ${token.take(10)}...")
    }

    fun getToken(): String = prefs.getString(KEY_TOKEN, "") ?: ""

    /**
     * isLoggedIn now only checks for token presence.
     * This allows unregistered users who just verified OTP to stay authenticated 
     * while they proceed to role selection or registration.
     */
    fun isLoggedIn(): Boolean {
        val token = getToken()
        val loggedIn = token.isNotEmpty()
        Log.d("SESSION", "isLoggedIn check: tokenPresent=${token.isNotEmpty()} -> Result: $loggedIn")
        return loggedIn
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, loggedIn).commit()
    }

    fun logout() {
        Log.d("SESSION", "⚠️ Logging out user - Clearing all preferences")
        prefs.edit().clear().commit()
    }

    // ========================================
    // ROLE + PHONE
    // ========================================

    fun setRole(role: String) {
        val editor = prefs.edit()
        editor.putString(KEY_ROLE, role)
        editor.putString(KEY_LAST_USED_ROLE, role)
        editor.commit()
        Log.d("SESSION", "Role updated to: $role")
    }

    fun getRole(): String = prefs.getString(KEY_ROLE, "") ?: ""

    fun getLastUsedRole(): String = prefs.getString(KEY_LAST_USED_ROLE, "") ?: ""

    fun isUserRegistered(): Boolean = prefs.getBoolean(KEY_IS_USER_REGISTERED, false)

    fun setUserRegistered(registered: Boolean) {
        prefs.edit().putBoolean(KEY_IS_USER_REGISTERED, registered).commit()
        if (registered) addAvailableRole("USER")
    }

    fun isMerchantRegistered(): Boolean = prefs.getBoolean(KEY_IS_MERCHANT_REGISTERED, false)

    fun setMerchantRegistered(registered: Boolean) {
        prefs.edit().putBoolean(KEY_IS_MERCHANT_REGISTERED, registered).commit()
        if (registered) addAvailableRole("MERCHANT")
    }

    private fun addAvailableRole(role: String) {
        val roles = getAvailableRoles().toMutableSet()
        roles.add(role)
        prefs.edit().putStringSet(KEY_AVAILABLE_ROLES, roles).commit()
    }

    fun setAvailableRoles(roles: Set<String>) {
        prefs.edit().putStringSet(KEY_AVAILABLE_ROLES, roles).commit()
    }

    fun getAvailableRoles(): Set<String> {
        return prefs.getStringSet(KEY_AVAILABLE_ROLES, emptySet()) ?: emptySet()
    }

    fun savePhone(phone: String) {
        prefs.edit().putString(KEY_PHONE, phone).commit()
    }

    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""

    fun getFormattedPhone(): String {
        val phone = getPhone()
        return PhoneUtils.normalizePhone(phone) ?: ""
    }

    // ========================================
    // 👤 USER
    // ========================================

    fun saveUserSession(userId: Long, name: String?, email: String?, upiId: String?) {
        val editor = prefs.edit()
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, name ?: "")
        editor.putString(KEY_USER_EMAIL, email ?: "")
        editor.putString(KEY_USER_UPI, upiId ?: "")
        editor.putBoolean(KEY_IS_USER_REGISTERED, true)
        val success = editor.commit()
        addAvailableRole("USER")
        Log.d("SESSION", "✅ User session saved ($success): ID=$userId")
    }

    fun getUserId(): Long {
        val id = prefs.getLong(KEY_USER_ID, -1)
        return if (id < 0) 0L else id
    }
    
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserUpi(): String = prefs.getString(KEY_USER_UPI, "") ?: ""

    // ========================================
    // 🏪 MERCHANT
    // ========================================

    fun saveMerchantSession(
        merchantId: Long,
        name: String?,
        email: String?,
        upiId: String?,
        businessName: String? = null
    ) {
        val editor = prefs.edit()
        editor.putLong(KEY_MERCHANT_ID, merchantId)
        editor.putString(KEY_MERCHANT_NAME, name ?: "")
        editor.putString(KEY_MERCHANT_EMAIL, email ?: "")
        editor.putString(KEY_MERCHANT_UPI, upiId ?: "")
        editor.putString(KEY_MERCHANT_BUSINESS_NAME, businessName ?: "")
        editor.putBoolean(KEY_IS_MERCHANT_REGISTERED, true)
        val success = editor.commit()
        addAvailableRole("MERCHANT")
        Log.d("SESSION", "✅ Merchant session saved ($success): ID=$merchantId")
    }

    fun getMerchantId(): Long {
        val id = prefs.getLong(KEY_MERCHANT_ID, -1)
        return if (id < 0) 0L else id
    }
    
    fun getMerchantName(): String = prefs.getString(KEY_MERCHANT_NAME, "") ?: ""
    fun getMerchantEmail(): String = prefs.getString(KEY_MERCHANT_EMAIL, "") ?: ""
    fun getMerchantUpi(): String = prefs.getString(KEY_MERCHANT_UPI, "") ?: ""
    fun getMerchantBusinessName(): String = prefs.getString(KEY_MERCHANT_BUSINESS_NAME, "") ?: ""

    // ========================================
    // 🏦 BANK & KYC
    // ========================================

    fun saveBankLinked(
        isLinked: Boolean,
        bankName: String? = null,
        accountNumber: String? = null,
        ifsc: String? = null
    ) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_BANK_LINKED, isLinked)
        bankName?.let { editor.putString(KEY_BANK_NAME, it) }
        accountNumber?.let { editor.putString(KEY_ACCOUNT_NUMBER, it) }
        ifsc?.let { editor.putString(KEY_IFSC_CODE, it) }
        editor.commit()
    }

    fun isBankLinked(): Boolean = prefs.getBoolean(KEY_BANK_LINKED, false)
    fun getBankName(): String = prefs.getString(KEY_BANK_NAME, "") ?: ""
    fun getAccountNumber(): String = prefs.getString(KEY_ACCOUNT_NUMBER, "") ?: ""
    fun getIfscCode(): String = prefs.getString(KEY_IFSC_CODE, "") ?: ""

    fun saveKycVerified(value: Boolean) {
        prefs.edit().putBoolean(KEY_KYC_VERIFIED, value).commit()
    }

    fun isKycVerified(): Boolean = prefs.getBoolean(KEY_KYC_VERIFIED, false)

    // ========================================
    // 🔢 PIN
    // ========================================

    fun savePinSet(isSet: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_SET, isSet).commit()
    }

    fun isPinSet(): Boolean = prefs.getBoolean(KEY_PIN_SET, false)

    // ========================================
    // 🚫 BLOCK STATUS
    // ========================================

    fun saveBlockedStatus(isBlocked: Boolean) {
        Log.d("SESSION", "Blocked status updated: $isBlocked")
        prefs.edit().putBoolean(KEY_IS_BLOCKED, isBlocked).commit()
    }

    fun isUserBlocked(): Boolean = prefs.getBoolean(KEY_IS_BLOCKED, false)

    // ========================================
    // UI STATE
    // ========================================

    fun setBannerDismissed(isDismissed: Boolean) {
        prefs.edit().putBoolean(KEY_BANNER_DISMISSED, isDismissed).commit()
    }

    fun isBannerDismissed(): Boolean = prefs.getBoolean(KEY_BANNER_DISMISSED, false)

    fun clearSession() {
        logout()
    }
}

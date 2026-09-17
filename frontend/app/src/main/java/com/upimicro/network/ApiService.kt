package com.upimicro.network

import com.upimicro.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------------- AUTH ----------------

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: OtpRequest): Response<GenericResponse>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<LoginResponse>

    @GET("api/auth/check-role/{phone}")
    suspend fun checkUserRole(@Path("phone") phone: String): Response<RoleCheckResponse>

    @POST("api/auth/register/user")
    suspend fun registerUser(@Body request: UserRegisterRequest): Response<LoginResponse>

    @POST("api/auth/register/merchant")
    suspend fun registerMerchant(@Body request: MerchantRegisterRequest): Response<LoginResponse>

    // ---------------- USER ----------------

    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<UserResponse>

    @GET("api/user/status/id/{userId}")
    suspend fun getUserStatus(
        @Path("userId") userId: Long
    ): Response<UserStatusResponse>

    @POST("api/user/update-profile")
    suspend fun updateUserProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserResponse>

    @POST("api/user/link-bank")
    suspend fun linkBank(
        @Body request: LinkBankRequest
    ): Response<UserResponse>

    @POST("api/user/kyc")
    suspend fun updateKyc(
        @Body request: KycRequest
    ): Response<GenericResponse>

    @GET("api/user/balance/{userId}")
    suspend fun getUserBalance(
        @Path("userId") userId: Long
    ): Response<BalanceResponse>

    @POST("api/user/set-pin")
    suspend fun setPin(
        @Body request: SetPinRequest
    ): Response<GenericResponse>

    @POST("api/user/verify-pin")
    suspend fun verifyPin(
        @Body request: VerifyPinRequest
    ): Response<VerifyPinResponse>

    // ---------------- TRANSACTIONS ----------------

    @POST("api/transactions/pay")
    suspend fun payTransaction(
        @Body request: TransactionRequest
    ): Response<ApiResponse<TransactionModel>>

    @GET("api/transactions/user/{userId}")
    suspend fun getUserTransactions(
        @Path("userId") userId: Long
    ): Response<ApiResponse<List<TransactionModel>>>

    // ---------------- MERCHANT ----------------

    @POST("api/merchant/login")
    suspend fun merchantLogin(@Body request: MerchantLoginRequest): Response<LoginResponse>

    @GET("api/merchant/status")
    suspend fun getMerchantStatus(): Response<MerchantStatusResponse>

    @POST("api/merchant/link-bank")
    suspend fun linkMerchantBank(
        @Body request: MerchantBankRequest
    ): Response<MerchantStatusResponse>

    @GET("api/merchant/profile")
    suspend fun getMerchantProfile(): Response<MerchantProfileResponse>

    @PUT("api/merchant/update-profile")
    suspend fun updateMerchantProfile(
        @Body request: MerchantUpdateProfileRequest
    ): Response<MerchantProfileResponse>

    @POST("api/merchant/update-kyc")
    suspend fun updateMerchantKyc(
        @Body request: MerchantKycRequest
    ): Response<GenericResponse>

    @GET("api/merchant/dashboard")
    suspend fun getMerchantDashboard(): Response<MerchantDashboardResponse>

    @GET("api/transactions/merchant/{merchantId}")
    suspend fun getMerchantTransactions(
        @Path("merchantId") merchantId: Long
    ): Response<ApiResponse<List<TransactionModel>>>

    @GET("api/transactions/merchant/{merchantId}/today")
    suspend fun getTodayMerchantTransactions(
        @Path("merchantId") merchantId: Long
    ): Response<ApiResponse<List<TransactionModel>>>

    // ---------------- ADMIN ----------------

    @GET("api/admin/users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @POST("api/admin/login")
    suspend fun adminLogin(@Body request: AdminLoginRequest): Response<AdminLoginResponse>

    @GET("api/admin/dashboard")
    suspend fun getAdminDashboard(): Response<AdminDashboardResponse>

    @GET("api/admin/fraud-alerts")
    suspend fun getFraudTransactions(): Response<List<FraudTransaction>>

    @PUT("api/admin/user/block/{userId}")
    suspend fun blockUser(
        @Path("userId") userId: Long
    ): Response<GenericResponse>

    @PUT("api/admin/user/unblock/{userId}")
    suspend fun unblockUser(
        @Path("userId") userId: Long
    ): Response<GenericResponse>

    @PUT("api/admin/merchant/block/{merchantId}")
    suspend fun blockMerchant(
        @Path("merchantId") merchantId: Long
    ): Response<GenericResponse>

    @PUT("api/admin/merchant/unblock/{merchantId}")
    suspend fun unblockMerchant(
        @Path("merchantId") merchantId: Long
    ): Response<GenericResponse>

    @GET("api/admin/analytics/merchants/kpis")
    suspend fun getMerchantKpis(): Response<MerchantKpiResponse>

    @GET("api/admin/analytics/merchants/top")
    suspend fun getTopMerchants(): Response<List<MerchantModel>>

    @GET("api/admin/analytics/qr/daily")
    suspend fun getQrDaily(): Response<List<DailyTransactionResponse>>

    @GET("api/admin/analytics/transactions/revenue")
    suspend fun getRevenueAnalytics(): Response<List<RevenueResponse>>

    @GET("api/admin/analytics/transactions/kpis")
    suspend fun getTransactionKpis(): Response<TransactionKpiResponse>

    @GET("api/admin/analytics/transactions/daily")
    suspend fun getDailyTransactions(): Response<List<DailyTransactionResponse>>

    @GET("api/admin/analytics/user-insights")
    suspend fun getUserInsights(): Response<UserInsightsResponse>

    @GET("api/admin/analytics/users/daily")
    suspend fun getUserRegistrations(): Response<List<DailyUserResponse>>

    // ---------------- UPI ----------------

    @GET("api/upi/verify")
    suspend fun verifyUpi(
        @Query("upiId") upiId: String
    ): Response<UpiVerifyResponse>

    @POST("api/upi/verify-qr")
    suspend fun verifyQr(
        @Body request: QrVerifyRequest
    ): Response<QrVerifyResponse>

    // ---------------- NOTIFICATIONS ----------------

    @POST("api/fcm/register")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest
    ): Response<GenericResponse>

    companion object {
        fun isAccountBlocked(errorBody: String?): Boolean {
            return try {
                if (errorBody.isNullOrEmpty()) false
                else errorBody.contains("ACCOUNT_RESTRICTED", ignoreCase = true)
            } catch (e: Exception) {
                false
            }
        }
    }
}
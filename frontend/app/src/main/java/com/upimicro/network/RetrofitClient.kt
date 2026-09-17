package com.upimicro.network

import android.content.Context
import android.util.Log
import com.upimicro.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    // =========================================
    // 🔥 OKHTTP CLIENT
    // =========================================

    private fun getClient(context: Context): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->

            val sessionManager = SessionManager(context)
            val originalRequest: Request = chain.request()
            val url = originalRequest.url.encodedPath

            Log.d("API_DEBUG", "➡️ Request: $url")

            // ✅ CLEAN PUBLIC API CHECK
            val isPublicApi =
                url.startsWith("/api/auth") ||
                        url.startsWith("/api/admin/login")

            val requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            if (!isPublicApi) {

                val token = sessionManager.getToken()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                    Log.d("AUTH_DEBUG", "✅ Token Attached")
                } else {
                    Log.e("AUTH_DEBUG", "❌ TOKEN MISSING → $url")
                }
            }

            val request = requestBuilder.build()

            val response = try {
                chain.proceed(request)
            } catch (e: Exception) {
                Log.e("API_DEBUG", "❌ Request failed: ${e.message}")
                throw e
            }

            Log.d("API_DEBUG", "⬅️ Response: ${response.code}")

            response
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // =========================================
    // 🔥 RETROFIT INSTANCE (NO STALE TOKEN ISSUE)
    // =========================================

    fun getApi(context: Context): ApiService {

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getClient(context.applicationContext))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
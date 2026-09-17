package com.upimicro.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.AdminLoginRequest
import com.upimicro.data.model.AdminLoginResponse
import com.upimicro.databinding.ActivityAdminLoginBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.auth.LoginActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import retrofit2.Response

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()

        binding.adminLoginButton.setOnClickListener {

            val email = binding.adminEmail.text.toString().trim()
            val password = binding.adminPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Enter email and password",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            loginAdmin(email, password)
        }
    }

    private fun setupToolbar() {
        // optional toolbar setup
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loginAdmin(email: String, password: String) {

        lifecycleScope.launch {

            try {

                val request = AdminLoginRequest(email, password)

                val response: Response<AdminLoginResponse> =
                    RetrofitClient.getApi(this@AdminLoginActivity)
                        .adminLogin(request)

                if (response.isSuccessful) {

                    val res = response.body()

                    if (res != null && !res.token.isNullOrEmpty()) {

                        val token = res.token ?: ""
                        val role = "ADMIN"

                        // 🔥 FINAL FIX → USE COMMON SESSION METHOD
                        sessionManager.saveFullSession(token, role, email)

                        Toast.makeText(
                            this@AdminLoginActivity,
                            "Admin Login Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this@AdminLoginActivity,
                                AdminDashboardActivity::class.java
                            )
                        )

                        finish()

                    } else {
                        Toast.makeText(
                            this@AdminLoginActivity,
                            "Invalid response from server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {

                    Toast.makeText(
                        this@AdminLoginActivity,
                        "Access Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@AdminLoginActivity,
                    "Login Failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
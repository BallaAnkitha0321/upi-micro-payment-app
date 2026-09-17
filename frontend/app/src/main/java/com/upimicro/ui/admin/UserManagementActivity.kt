package com.upimicro.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.databinding.ActivityUserManagementBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class UserManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnBlockUser.setOnClickListener {
            val userIdText = binding.etUserId.text.toString()
            if (userIdText.isEmpty()) {
                Toast.makeText(this, "Enter User ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                blockUser(userIdText.toLong())
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnUnblockUser.setOnClickListener {
            val userIdText = binding.etUserId.text.toString()
            if (userIdText.isEmpty()) {
                Toast.makeText(this, "Enter User ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                unblockUser(userIdText.toLong())
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun blockUser(userId: Long) {
        performUserAction(userId, isBlock = true)
    }

    private fun unblockUser(userId: Long) {
        performUserAction(userId, isBlock = false)
    }

    private fun performUserAction(userId: Long, isBlock: Boolean) {
        val token = sessionManager.getToken()
        if (token.isNullOrEmpty()) {
            redirectToLogin()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getApi(this@UserManagementActivity)
                
                val response = if (isBlock) {
                    api.blockUser(userId)
                } else {
                    api.unblockUser(userId)
                }

                if (response.isSuccessful) {
                    val msg = response.body()?.message ?: if (isBlock) "User blocked successfully" else "User unblocked successfully"
                    Toast.makeText(this@UserManagementActivity, msg, Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = parseError(response.errorBody()?.string())
                    Toast.makeText(this@UserManagementActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@UserManagementActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnBlockUser.isEnabled = !isLoading
        binding.btnUnblockUser.isEnabled = !isLoading
    }

    private fun parseError(error: String?): String {
        return try {
            if (error.isNullOrEmpty()) "Something went wrong"
            else {
                val json = JSONObject(error)
                json.optString("message", json.optString("error", "Something went wrong"))
            }
        } catch (e: Exception) {
            "Something went wrong"
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, AdminLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

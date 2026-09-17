package com.upimicro.ui.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.R
import com.upimicro.data.model.GenericResponse
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class MerchantManagementActivity : AppCompatActivity() {

    private lateinit var etMerchantId: EditText
    private lateinit var btnBlock: Button
    private lateinit var btnUnblock: Button
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merchant_management)

        sessionManager = SessionManager(this)

        etMerchantId = findViewById(R.id.etMerchantId)
        btnBlock = findViewById(R.id.btnBlockMerchant)
        btnUnblock = findViewById(R.id.btnUnblockMerchant)

        btnBlock.setOnClickListener {
            val id = etMerchantId.text.toString()
            if (id.isEmpty()) {
                Toast.makeText(this@MerchantManagementActivity, "Enter Merchant ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            blockMerchant(id.toLong())
        }

        btnUnblock.setOnClickListener {
            val id = etMerchantId.text.toString()
            if (id.isEmpty()) {
                Toast.makeText(this@MerchantManagementActivity, "Enter Merchant ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            unblockMerchant(id.toLong())
        }
    }

    private fun blockMerchant(id: Long) {
        val token = sessionManager.getToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session expired. Login again", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient
                    .getApi(this@MerchantManagementActivity)
                    .blockMerchant(id)

                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Success"

                    Toast.makeText(
                        this@MerchantManagementActivity,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    val errorBody = response.errorBody()?.string()

                    Toast.makeText(
                        this@MerchantManagementActivity,
                        errorBody ?: "Failed to block merchant",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@MerchantManagementActivity,
                    "Server error. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun unblockMerchant(id: Long) {
        val token = sessionManager.getToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session expired. Login again", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient
                    .getApi(this@MerchantManagementActivity)
                    .unblockMerchant(id)

                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Success"

                    Toast.makeText(
                        this@MerchantManagementActivity,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    val errorBody = response.errorBody()?.string()

                    Toast.makeText(
                        this@MerchantManagementActivity,
                        errorBody ?: "Failed to unblock merchant",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@MerchantManagementActivity,
                    "Server error. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, AdminLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

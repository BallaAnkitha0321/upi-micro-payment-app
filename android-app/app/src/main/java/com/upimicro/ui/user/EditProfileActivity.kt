package com.upimicro.ui.user

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.data.model.UpdateProfileRequest
import com.upimicro.databinding.ActivityEditProfileBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {

        val userId = sessionManager.getUserId()
        val phone = sessionManager.getPhone()
        val userName = sessionManager.getUserName()
        val email = sessionManager.getUserEmail()   // ✅ FIX

        binding.nameEditText.setText(
            if (userName.isEmpty()) "User $userId" else userName
        )

        binding.phoneEditText.setText(phone)
        binding.emailEditText.setText(email)

        binding.phoneEditText.isEnabled = false
    }

    private fun setupListeners() {

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.saveButton.setOnClickListener {

            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val userId = sessionManager.getUserId()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == -1L) {
                Toast.makeText(this, "Session expired. Login again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateProfile(userId, name, email)
        }
    }

    private fun updateProfile(userId: Long, name: String, email: String) {

        binding.saveButton.isEnabled = false
        binding.saveButton.text = "Updating..."

        val token = sessionManager.getToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {

                val response = RetrofitClient
                    .getApi(this@EditProfileActivity)
                    .updateUserProfile(
                        UpdateProfileRequest(name, email)
                    )

                if (response.isSuccessful && response.body() != null) {

                    val user = response.body()!!

                    // ✅ SAVE UPDATED SESSION
                    sessionManager.saveUserSession(
                        userId = userId,
                        name = user.name,
                        email = user.email,
                        upiId = user.upiId
                    )

                    Toast.makeText(
                        this@EditProfileActivity,
                        "Profile updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()

                } else {

                    Log.e("EDIT_PROFILE", "Error: ${response.code()}")

                    Toast.makeText(
                        this@EditProfileActivity,
                        "Update failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                Log.e("EDIT_PROFILE", "Error: ${e.message}")

                Toast.makeText(
                    this@EditProfileActivity,
                    "Server error",
                    Toast.LENGTH_SHORT
                ).show()

            } finally {

                binding.saveButton.isEnabled = true
                binding.saveButton.text = "Save Changes"
            }
        }
    }
}

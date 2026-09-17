package com.upimicro.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.databinding.ActivitySetPinBinding
import com.upimicro.data.model.SetPinRequest
import com.upimicro.network.RetrofitClient
import com.upimicro.ui.user.UserDashboardActivity
import com.upimicro.utils.SessionManager
import kotlinx.coroutines.launch

class SetPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetPinBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)

        setupPinInputs()
        setupConfirmPinInputs()

        binding.submitButton.setOnClickListener {
            val pin = getPinFromBoxes()
            val confirmPin = getConfirmPinFromBoxes()

            if (pin.length != 4) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != confirmPin) {
                Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitPin(pin)
        }
    }

    private fun setupPinInputs() {
        val boxes = arrayOf(binding.pin1, binding.pin2, binding.pin3, binding.pin4)
        setupAutoAdvance(boxes)
    }

    private fun setupConfirmPinInputs() {
        val boxes = arrayOf(binding.confirmPin1, binding.confirmPin2, binding.confirmPin3, binding.confirmPin4)
        setupAutoAdvance(boxes)
    }

    private fun setupAutoAdvance(boxes: Array<EditText>) {
        for (i in boxes.indices) {
            boxes[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < boxes.size - 1) {
                        boxes[i + 1].requestFocus()
                    }
                }
            })

            boxes[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (boxes[i].text.isEmpty() && i > 0) {
                        boxes[i - 1].requestFocus()
                        boxes[i - 1].setText("")
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }

    private fun getPinFromBoxes(): String {
        return binding.pin1.text.toString() +
                binding.pin2.text.toString() +
                binding.pin3.text.toString() +
                binding.pin4.text.toString()
    }

    private fun getConfirmPinFromBoxes(): String {
        return binding.confirmPin1.text.toString() +
                binding.confirmPin2.text.toString() +
                binding.confirmPin3.text.toString() +
                binding.confirmPin4.text.toString()
    }

    private fun submitPin(pin: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@SetPinActivity).setPin(SetPinRequest(pin))
                if (response.isSuccessful) {
                    sessionManager.savePinSet(true)
                    Toast.makeText(this@SetPinActivity, "PIN set successfully", Toast.LENGTH_SHORT).show()
                    
                    val intent = Intent(this@SetPinActivity, UserDashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@SetPinActivity, "Failed to set PIN", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SetPinActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
package com.upimicro.ui.user

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.upimicro.databinding.ActivityPinBinding
import com.upimicro.network.RetrofitClient
import com.upimicro.data.model.VerifyPinRequest
import kotlinx.coroutines.launch

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private var userId: Long = -1L
    private var purpose: String? = null

    companion object {
        const val EXTRA_PURPOSE = "EXTRA_PURPOSE"
        const val PURPOSE_BALANCE = "PURPOSE_BALANCE"
        const val PURPOSE_PAYMENT = "PURPOSE_PAYMENT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getLongExtra("USER_ID", -1L)
        purpose = intent.getStringExtra(EXTRA_PURPOSE) ?: PURPOSE_BALANCE

        setupToolbar()
        setupUi()
        setupPinInputs()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupUi() {
        if (purpose == PURPOSE_PAYMENT) {
            binding.pinDescriptionText.text = "Enter PIN to complete payment"
            binding.verifyButton.text = "CONFIRM & PAY"
        } else {
            binding.pinDescriptionText.text = "To view your account balance"
            binding.verifyButton.text = "VERIFY"
        }
    }

    private fun setupPinInputs() {
        val boxes = arrayOf(binding.pin1, binding.pin2, binding.pin3, binding.pin4)
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

    private fun setupListeners() {
        binding.verifyButton.setOnClickListener {
            val enteredPin = getPinFromBoxes()

            if (enteredPin.length != 4) {
                Toast.makeText(this, "Please enter 4-digit PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            verifyPinAndProceed(enteredPin)
        }
    }

    private fun verifyPinAndProceed(pin: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getApi(this@PinActivity).verifyPin(VerifyPinRequest(pin))
                if (response.isSuccessful && response.body()?.valid == true) {
                    if (purpose == PURPOSE_PAYMENT) {
                        val resultIntent = Intent()
                        resultIntent.putExtra("PIN", pin)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        fetchBalanceAndShowResult()
                    }
                } else {
                    Toast.makeText(this@PinActivity, "Invalid PIN", Toast.LENGTH_SHORT).show()
                    clearPinBoxes()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PinActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearPinBoxes() {
        binding.pin1.setText("")
        binding.pin2.setText("")
        binding.pin3.setText("")
        binding.pin4.setText("")
        binding.pin1.requestFocus()
    }

    private fun fetchBalanceAndShowResult() {

        if (userId == -1L) {
            Toast.makeText(this, "User session error", Toast.LENGTH_SHORT).show()
            return
        }

        binding.verifyButton.isEnabled = false
        binding.pin1.isEnabled = false
        binding.pin2.isEnabled = false
        binding.pin3.isEnabled = false
        binding.pin4.isEnabled = false

        lifecycleScope.launch {
            try {

                val response = RetrofitClient
                    .getApi(this@PinActivity)
                    .getUserBalance(userId)

                if (response.isSuccessful) {

                    val body = response.body()

                    if (body != null) {
                        val balance = body.balance

                        Log.d("UPI_DEBUG", "Balance fetched: $balance")

                        val intent = Intent(this@PinActivity, BalanceResultActivity::class.java)
                        intent.putExtra("BALANCE", balance)
                        startActivity(intent)

                        setResult(RESULT_OK)
                        finish()

                    } else {
                        Toast.makeText(
                            this@PinActivity,
                            "Empty response",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    val error = response.errorBody()?.string()
                    Toast.makeText(
                        this@PinActivity,
                        "Failed: ${error ?: "Unable to fetch balance"}",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.verifyButton.isEnabled = true
                    enablePinBoxes()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@PinActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

                binding.verifyButton.isEnabled = true
                enablePinBoxes()
            }
        }
    }

    private fun enablePinBoxes() {
        binding.pin1.isEnabled = true
        binding.pin2.isEnabled = true
        binding.pin3.isEnabled = true
        binding.pin4.isEnabled = true
    }
}

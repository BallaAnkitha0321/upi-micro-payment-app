package com.upimicro.ui.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.upimicro.databinding.ActivityBalanceResultBinding

class BalanceResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBalanceResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBalanceResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val balance = intent.getDoubleExtra("BALANCE", 0.0)
        binding.balanceValueText.text = "₹ %.2f".format(balance)

        setupToolbar()
        binding.doneButton.setOnClickListener { finish() }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}

package com.upimicro.ui.merchant

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.upimicro.R
import com.upimicro.databinding.ActivityQrBinding
import com.upimicro.utils.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder

class QRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrBinding
    private lateinit var sessionManager: SessionManager
    private var qrBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()

        binding.qrImage.setImageResource(R.drawable.ic_qr_placeholder)

        val initialQrData = intent.getStringExtra("QR_DATA")
        if (!initialQrData.isNullOrEmpty()) {
            qrBitmap = generateQr(initialQrData)
            binding.qrImage.setImageBitmap(qrBitmap)
            showQrActions()
        }

        binding.generateBtn.setOnClickListener {

            val amount = binding.amountInput.text.toString().trim()

            if (amount.isEmpty()) {
                binding.amountInputLayout.error = "Enter amount"
                return@setOnClickListener
            }

            binding.amountInputLayout.error = null

            val upi = sessionManager.getMerchantUpi()

            // 🔥 FIX: Use proper merchant name
            val name = sessionManager.getMerchantBusinessName().ifEmpty { 
                sessionManager.getMerchantName().ifEmpty { "Merchant" } 
            }

            if (upi.isNullOrEmpty()) {
                Toast.makeText(this, "Merchant profile incomplete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {

                val encodedName = URLEncoder.encode(name, "UTF-8")

                val qrData =
                    "upi://pay?pa=$upi&pn=$encodedName&am=$amount&cu=INR"

                qrBitmap = generateQr(qrData)

                binding.qrImage.setImageBitmap(qrBitmap)

                showQrActions()

                Toast.makeText(this, "QR Generated ₹$amount", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this, "Error generating QR", Toast.LENGTH_SHORT).show()
            }
        }

        binding.downloadBtn.setOnClickListener {
            qrBitmap?.let {
                saveQrToFile(it)
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shareBtn.setOnClickListener {
            qrBitmap?.let {
                val file = saveQrToFile(it)
                shareQr(file)
            }
        }

        binding.closeButton.setOnClickListener {
            finish()
        }
    }

    private fun showQrActions() {
        binding.qrInstruction.visibility = View.VISIBLE
        binding.actionContainer.visibility = View.VISIBLE
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun generateQr(data: String): Bitmap {

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 800, 800)

        val width = bitMatrix.width
        val height = bitMatrix.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }

        return bitmap
    }

    private fun saveQrToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "qr.png")

        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        return file
    }

    private fun shareQr(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Share QR"))
    }
}

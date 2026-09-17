package com.upimicro.ui.merchant

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.upimicro.databinding.ActivityQrdisplayBinding
import com.upimicro.utils.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder

class QRDisplayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrdisplayBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var qrBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQrdisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()

        val merchantId = sessionManager.getMerchantId()

        // 🔥 ONLY CHANGE: read from intent FIRST
        val intentName = intent.getStringExtra("shopName")
        val intentUpi = intent.getStringExtra("upiId")

        val merchantName = intentName ?: "Merchant $merchantId"
        val rawMerchantUpi = intentUpi ?: sessionManager.getMerchantUpi()
        
        // Clean UPI ID (Remove +91)
        val merchantUpi = rawMerchantUpi?.replace("+91", "")

        binding.merchantName.text = merchantName
        binding.merchantUpi.text = merchantUpi ?: ""

        Log.d("QR_DEBUG", "Name = $merchantName")
        Log.d("QR_DEBUG", "UPI = $merchantUpi")

        var qrData = intent.getStringExtra("QR_DATA")

        if (qrData.isNullOrBlank()) {
            if (!merchantUpi.isNullOrEmpty()) {
                val encodedName = URLEncoder.encode(merchantName, "UTF-8")
                // Ensure UPI in QR is also clean
                qrData = "upi://pay?pa=$merchantUpi&pn=$encodedName"
            }
        }

        if (qrData.isNullOrBlank()) {
            Toast.makeText(this, "Invalid QR Data", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        qrBitmap = generateQr(qrData)
        binding.qrImage.setImageBitmap(qrBitmap)

        binding.downloadButton.setOnClickListener {
            saveQrToFile(qrBitmap)
            Toast.makeText(this, "QR saved", Toast.LENGTH_SHORT).show()
        }

        binding.shareButton.setOnClickListener {
            val file = saveQrToFile(qrBitmap)
            shareQr(file)
        }

        binding.amountQrButton.setOnClickListener {
            Log.d("QR_DEBUG", "Opening Dynamic QR Screen")
            startActivity(Intent(this, QRActivity::class.java))
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun generateQr(data: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 800, 800)

        val width = bitMatrix.width
        val height = bitMatrix.height

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        return bmp
    }

    private fun saveQrToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "merchant_qr.png")

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

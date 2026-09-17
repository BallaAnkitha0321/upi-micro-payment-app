package com.upimicro.ui.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.upimicro.databinding.ActivityMyQrBinding
import com.upimicro.utils.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MyQrActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyQrBinding
    private lateinit var sessionManager: SessionManager
    private var qrBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        loadUserQR()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadUserQR() {
        val phone = sessionManager.getPhone()
        val userName = sessionManager.getUserName() ?: "User"

        if (phone.isNullOrEmpty()) {
            binding.txtUpi.text = "UPI not available"
            return
        }

        val upiId = "$phone@ybl"

        binding.txtUserName.text = userName
        binding.txtUpi.text = upiId

        qrBitmap = generateQR(upiId, userName)
        binding.imgQR.setImageBitmap(qrBitmap)
    }

    private fun generateQR(upiId: String, name: String): Bitmap? {
        return try {
            val qrData = "upi://pay?pa=$upiId&pn=$name&cu=INR"
            val writer = QRCodeWriter()
            val size = 800

            val bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupButtons() {
        binding.btnDownloadQR.setOnClickListener {
            val bitmap = qrBitmap ?: return@setOnClickListener
            saveImageToGallery(bitmap)
        }

        binding.btnShareQR.setOnClickListener {
            val bitmap = qrBitmap ?: return@setOnClickListener
            shareQR(bitmap)
        }

        binding.upiContainer.setOnClickListener {
            copyUpiToClipboard()
        }
    }

    private fun copyUpiToClipboard() {
        val upiId = binding.txtUpi.text.toString()
        if (upiId.isEmpty() || upiId == "UPI not available") return

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("UPI ID", upiId)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "My_UPI_QR_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UpiPay")
                }
                val imageUri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(this, "QR saved to Gallery", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save QR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareQR(bitmap: Bitmap) {
        try {
            val file = File(cacheDir, "shared_qr.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Scan to pay me via UPI: ${binding.txtUpi.text}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share QR via"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to share QR", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.upimicro.ui.user

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import android.util.Log

class QRCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        if (mediaImage != null) {

            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->

                    for (barcode in barcodes) {

                        val qrValue = barcode.rawValue

                        if (qrValue != null) {
                            onQrCodeDetected(qrValue)
                        }

                    }

                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
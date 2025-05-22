package com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility

interface QrCodeGenerator {
    suspend fun generateQrBitmap(config: QrStyleConfig): QrImageResult
}
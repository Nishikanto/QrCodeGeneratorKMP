package com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility

import androidx.compose.ui.graphics.ImageBitmap

sealed class QrImageResult {
    data class Android(val bitmap: ImageBitmap) : QrImageResult() // Bitmap for Android
    data class IOS(val imageBitmap: ImageBitmap?) : QrImageResult()      // NSImage for iOS
    data object Error : QrImageResult()
}
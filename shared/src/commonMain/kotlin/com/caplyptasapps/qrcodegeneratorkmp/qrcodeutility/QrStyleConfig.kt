package com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility

data class QrStyleConfig(
    val preparedText: String,
    val width: Int = 1024,
    val height: Int = 1024,
    val qrColor: Int = 0xFF000000.toInt(), // ARGB black
    val logoBgColor: Int = 0xFFFFFFFF.toInt(), // white
    val qrVisualCategory: QrVisualCategory = QrVisualCategory.STANDARD,
    val logoUri: String? = null,          // Use platform-specific Uri strings
    val backgroundUri: String? = null,
)
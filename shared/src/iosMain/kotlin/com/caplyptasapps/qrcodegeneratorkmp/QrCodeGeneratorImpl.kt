package com.caplyptasapps.qrcodegeneratorkmp

import androidx.compose.ui.graphics.toComposeImageBitmap
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrCodeGenerator
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrImageResult
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrStyleConfig
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.createCGImage
import platform.CoreImage.filterWithName
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setValue
import platform.UIKit.UIImage

class QrCodeGeneratorImpl : QrCodeGenerator {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun generateQrBitmap(config: QrStyleConfig): QrImageResult {
        val data = config.preparedText.toNSData() ?: return QrImageResult.Error

        val filter = CIFilter.filterWithName("CIQRCodeGenerator") as? CIFilter
        filter?.setDefaults()
        filter?.setValue(data, forKey = "inputMessage")
        filter?.setValue("H", forKey = "inputCorrectionLevel") // High correction

        val outputImage = filter?.outputImage ?: return QrImageResult.Error

        // Scale it up
        val transform = CGAffineTransformMakeScale(10.0, 10.0)
        val scaledImage = outputImage.imageByApplyingTransform(transform)

        val context = CIContext.context()
        val cgImage = context.createCGImage(scaledImage, fromRect = scaledImage.extent)
            ?: return QrImageResult.Error

        val uiImage = UIImage.imageWithCGImage(cgImage)

        return QrImageResult.IOS(uiImage.toSkiaImage()?.toComposeImageBitmap())
    }

    private fun String.toNSData(): NSData? =
        (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
}
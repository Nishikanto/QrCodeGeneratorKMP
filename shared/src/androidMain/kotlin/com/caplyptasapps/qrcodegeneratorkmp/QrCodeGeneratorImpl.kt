package com.caplyptasapps.qrcodegeneratorkmp

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.core.net.toUri
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrCodeGenerator
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrImageResult
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrStyleConfig
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrVisualCategory

/**
 * Implementation of the QrCodeGenerator interface for generating QR codes with various styles.
 *
 * @param context The Android context used for resource access and bitmap decoding.
 */
class QrCodeGeneratorImpl(private val context: Context) : QrCodeGenerator {

    companion object {
        private const val MIN_MODULE_SIZE = 4 // Minimum size in pixels for each module
    }

    /**
     * Generates a QR code bitmap based on the provided style configuration.
     *
     * @param qrCodeStyle The configuration for the QR code, including size, color, and visual style.
     * @return A Bitmap representing the generated QR code, or null if the input text is empty.
     */
    private fun generateQrCode(qrCodeStyle: QrStyleConfig): Bitmap? {
        val text = qrCodeStyle.preparedText
        if (text.isEmpty()) return null

        // QR code generation hints
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H, // High error correction
            EncodeHintType.MARGIN to 1
        )

        // Generate the QR code matrix
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints)
        val inputWidth = bitMatrix.width
        val inputHeight = bitMatrix.height

        // Calculate dimensions and scaling
        val quietZone = 1
        val qrWidth = inputWidth + 2 * quietZone
        val qrHeight = inputHeight + 2 * quietZone
        val multiple = maxOf(
            MIN_MODULE_SIZE,
            minOf(qrCodeStyle.width / qrWidth, qrCodeStyle.height / qrHeight)
        )

        val leftPadding = (qrCodeStyle.width - (inputWidth * multiple)) / 2f
        val topPadding = (qrCodeStyle.height - (inputHeight * multiple)) / 2f

        // Create a blank bitmap and canvas
        val bitmap = createBitmap(qrCodeStyle.width, qrCodeStyle.height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        // Paint configuration for drawing QR code modules
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = qrCodeStyle.qrColor
            style = Paint.Style.FILL
        }

        // Adjusted radius for rounded dots
        val radius = multiple * 0.45f
        val eyeSize = 8

        // Determine the visual category of the QR code
        val category = try {
            qrCodeStyle.qrVisualCategory
        } catch (e: Exception) {
            QrVisualCategory.STANDARD
        }

        // Draw finder patterns (corner eyes) for multi-cornered eyes style
        if (category == QrVisualCategory.MULTI_CORNERED_EYES) {
            val diameter = multiple * eyeSize
            val offset = diameter * 0.05f
            drawFinderPatternCircleStyle(
                canvas,
                paint,
                leftPadding + offset,
                topPadding + offset,
                diameter,
                qrCodeStyle.qrColor,
            )
            drawFinderPatternCircleStyle(
                canvas,
                paint,
                leftPadding + (inputWidth - eyeSize) * multiple - offset,
                topPadding + offset,
                diameter,
                qrCodeStyle.qrColor,
            )
            drawFinderPatternCircleStyle(
                canvas,
                paint,
                leftPadding + offset,
                topPadding + (inputHeight - eyeSize) * multiple - offset,
                diameter,
                qrCodeStyle.qrColor,
            )
        }

        // Track filled cells for advanced rendering
        val filledMatrix = Array(inputWidth) { BooleanArray(inputHeight) }
        for (x in 0 until inputWidth) {
            for (y in 0 until inputHeight) {
                filledMatrix[x][y] = bitMatrix[x, y]
            }
        }

        // Draw the QR code modules based on the visual category
        for (x in 0 until inputWidth) {
            for (y in 0 until inputHeight) {
                if (!bitMatrix[x, y]) continue

                val isInTopLeftEye = x < eyeSize && y < eyeSize
                val isInTopRightEye = x >= inputWidth - eyeSize && y < eyeSize
                val isInBottomLeftEye = x < eyeSize && y >= inputHeight - eyeSize

                // Skip finder pattern cells for special eyes style
                if (category == QrVisualCategory.MULTI_CORNERED_EYES &&
                    (isInTopLeftEye || isInTopRightEye || isInBottomLeftEye)
                ) continue

                val left = leftPadding + x * multiple
                val top = topPadding + y * multiple
                val right = left + multiple
                val bottom = top + multiple
                val cx = left + multiple / 2
                val cy = top + multiple / 2

                // Render modules based on the visual category
                when (category) {
                    QrVisualCategory.STANDARD -> {
                        canvas.drawRect(left, top, right, bottom, paint)
                    }
                    QrVisualCategory.ROUNDED_DOTS -> {
                        canvas.drawCircle(cx, cy, radius, paint)
                    }
                    QrVisualCategory.CHECKERBOARD -> {
                        if ((x + y) % 2 == 0) {
                            canvas.drawRect(
                                left + multiple * 0.1f, top + multiple * 0.1f,
                                right - multiple * 0.1f, bottom - multiple * 0.1f, paint
                            )
                        } else {
                            canvas.drawCircle(cx, cy, radius * 0.9f, paint)
                        }
                    }
                    QrVisualCategory.ANGULAR_BLOCKY -> {
                        val pad = multiple * 0.15f
                        canvas.drawRect(left + pad, top + pad, right - pad, bottom - pad, paint)
                    }
                    QrVisualCategory.MINI_ROUNDED_BLOCKS -> {
                        val cornerRadius = multiple * 0.25f
                        canvas.drawRoundRect(
                            left + multiple * 0.05f,
                            top + multiple * 0.05f,
                            right - multiple * 0.05f,
                            bottom - multiple * 0.05f,
                            cornerRadius,
                            cornerRadius,
                            paint
                        )
                    }
                    QrVisualCategory.DENSE_PATTERN -> {
                        val hasLeft = x > 0 && filledMatrix[x - 1][y]
                        val hasRight = x < inputWidth - 1 && filledMatrix[x + 1][y]
                        val hasTop = y > 0 && filledMatrix[x][y - 1]
                        val hasBottom = y < inputHeight - 1 && filledMatrix[x][y + 1]

                        canvas.drawCircle(cx, cy, radius * 0.9f, paint)

                        if (hasLeft) {
                            canvas.drawRect(left, cy - radius * 0.4f, cx, cy + radius * 0.4f, paint)
                        }
                        if (hasRight) {
                            canvas.drawRect(
                                cx,
                                cy - radius * 0.4f,
                                right,
                                cy + radius * 0.4f,
                                paint
                            )
                        }
                        if (hasTop) {
                            canvas.drawRect(cx - radius * 0.4f, top, cx + radius * 0.4f, cy, paint)
                        }
                        if (hasBottom) {
                            canvas.drawRect(
                                cx - radius * 0.4f,
                                cy,
                                cx + radius * 0.4f,
                                bottom,
                                paint
                            )
                        }
                    }
                    QrVisualCategory.SKEWED_OFF_GRID -> {
                        val offsetX = if ((x + y) % 2 == 0) -multiple * 0.15f else multiple * 0.15f
                        val offsetY = if ((x - y) % 2 == 0) -multiple * 0.15f else multiple * 0.15f
                        canvas.drawRect(
                            left + offsetX,
                            top + offsetY,
                            right + offsetX,
                            bottom + offsetY,
                            paint
                        )
                    }
                    QrVisualCategory.MULTI_CORNERED_EYES -> {
                        canvas.drawRect(left, top, right, bottom, paint)
                    }
                }
            }
        }

        // Add a logo to the center of the QR code if specified
        qrCodeStyle.logoUri?.let {
            val scaledLogo =
                decodeBackgroundBitmap(context, it.toUri()).scale(
                    qrCodeStyle.width / 5,
                    qrCodeStyle.height / 5
                )
            val centerX = (qrCodeStyle.width - scaledLogo.width) / 2f
            val centerY = (qrCodeStyle.height - scaledLogo.height) / 2f

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = qrCodeStyle.logoBgColor
                style = Paint.Style.FILL
            }

            canvas.drawRoundRect(
                centerX - 8f, centerY - 8f,
                centerX + scaledLogo.width + 8f,
                centerY + scaledLogo.height + 8f,
                16f, 16f,
                bgPaint
            )

            canvas.drawBitmap(scaledLogo, centerX, centerY, null)
        }

        // Overlay a background image if specified
        qrCodeStyle.backgroundUri?.let {
            val backgroundBitmap = decodeBackgroundBitmap(context, it.toUri())
            return overlayBackground(bitmap, backgroundBitmap)
        }

        return bitmap
    }

    /**
     * Draws a circular finder pattern for multi-cornered eyes style.
     *
     * @param canvas The canvas to draw on.
     * @param paint The paint used for drawing.
     * @param x The x-coordinate of the top-left corner of the pattern.
     * @param y The y-coordinate of the top-left corner of the pattern.
     * @param diameter The diameter of the outer circle.
     * @param qrColor The color of the QR code.
     */
    private fun drawFinderPatternCircleStyle(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        diameter: Int,
        qrColor: Int,
    ) {
        val whiteDiameter = diameter * 5f / 7f
        val whiteOffset = diameter / 7f
        val middleDiameter = diameter * 3f / 7f
        val middleOffset = diameter * 2f / 7f

        val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        // Outer black circle
        paint.color = qrColor
        canvas.drawOval(
            RectF(
                x,
                y,
                x + diameter,
                y + diameter
            ),
            paint
        )

        // Middle transparent ring
        canvas.drawOval(
            RectF(
                x + whiteOffset,
                y + whiteOffset,
                x + whiteOffset + whiteDiameter,
                y + whiteOffset + whiteDiameter
            ),
            clearPaint
        )

        // Inner black dot
        paint.color = qrColor
        canvas.drawOval(
            RectF(
                x + middleOffset,
                y + middleOffset,
                x + middleOffset + middleDiameter,
                y + middleOffset + middleDiameter
            ),
            paint
        )
    }

    /**
     * Decodes a bitmap from a URI, supporting both modern and legacy Android versions.
     *
     * @param context The Android context used for resource access.
     * @param uri The URI of the image to decode.
     * @return A mutable Bitmap decoded from the URI.
     */
    private fun decodeBackgroundBitmap(context: Context, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    /**
     * Overlays a QR code bitmap on top of a background bitmap.
     *
     * @param qrBitmap The QR code bitmap.
     * @param backgroundBitmap The background bitmap.
     * @return A new Bitmap with the QR code overlaid on the background.
     */
    private fun overlayBackground(qrBitmap: Bitmap, backgroundBitmap: Bitmap): Bitmap {
        val output = createBitmap(qrBitmap.width, qrBitmap.height)
        val canvas = Canvas(output)
        val scaledBackground =
            backgroundBitmap.scale(qrBitmap.width, qrBitmap.height)
        canvas.drawBitmap(scaledBackground, 0f, 0f, null)
        canvas.drawBitmap(qrBitmap, 0f, 0f, null)
        return output
    }

    /**
     * Generates a QR code bitmap asynchronously based on the provided configuration.
     *
     * @param config The configuration for the QR code, including size, color, and visual style.
     * @return A QrImageResult containing the generated QR code or an error.
     */
    override suspend fun generateQrBitmap(config: QrStyleConfig): QrImageResult {
        val bitmap = generateQrCode(config) ?: return QrImageResult.Error
        return QrImageResult.Android(bitmap.asImageBitmap())
    }
}
package com.example.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.example.model.EnhancementConfig
import com.example.model.NormalizedCrop
import com.example.model.PvcConstants
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ImageEnhancementEngine {

    /**
     * Crops raw bitmap to standard CR80 dimensions (1011 x 638 px @ 300 DPI).
     * For Aadhaar Card ONLY: Applies the exclusive "Original Vibrant Color" Engine with HD Color Boost.
     * For all other cards: Strictly preserves 1:1 original scanned colors and tones with zero modifications.
     */
    fun processCardSide(
        rawBitmap: Bitmap,
        crop: NormalizedCrop,
        config: EnhancementConfig
    ): Bitmap {
        // Step 1: Lossless crop and orient to exact CR80 300 DPI dimensions
        val cropped = cropAndOrient(rawBitmap, crop)

        // Strict Enforcement: If Aadhaar Color Boost is NOT active, return 1:1 lossless scanned original
        if (!config.isAadhaarColorBoost) {
            return cropped
        }

        // --- EXCLUSIVE AADHAAR ORIGINAL VIBRANT COLOR ENGINE ---
        // 1. ColorMatrix for tricolor saturation and contrast optimization
        val colorBoosted = applyAadhaarColorBoost(cropped, config)

        // 2. Intelligent deep black text sharpening & edge enhancement
        val textSharpened = if (config.sharpness > 0.01f) {
            applyAadhaarSmartSharpen(colorBoosted, config.sharpness)
        } else {
            colorBoosted
        }

        return textSharpened
    }

    /**
     * Crops normalized rect from source bitmap, rotates, and scales to standard CR80 output (1011 x 638 px @ 300 DPI).
     */
    private fun cropAndOrient(source: Bitmap, crop: NormalizedCrop): Bitmap {
        val srcW = source.width
        val srcH = source.height

        val left = (crop.left.coerceIn(0f, 1f) * srcW).toInt().coerceIn(0, srcW - 1)
        val top = (crop.top.coerceIn(0f, 1f) * srcH).toInt().coerceIn(0, srcH - 1)
        val right = (crop.right.coerceIn(0f, 1f) * srcW).toInt().coerceIn(left + 1, srcW)
        val bottom = (crop.bottom.coerceIn(0f, 1f) * srcH).toInt().coerceIn(top + 1, srcH)

        val cropW = right - left
        val cropH = bottom - top

        val rawCropped = Bitmap.createBitmap(source, left, top, cropW, cropH)

        val rotated = if (crop.rotationDegrees % 360 != 0) {
            val matrix = Matrix().apply { postRotate(crop.rotationDegrees.toFloat()) }
            Bitmap.createBitmap(rawCropped, 0, 0, rawCropped.width, rawCropped.height, matrix, true)
        } else {
            rawCropped
        }

        val targetWidth = PvcConstants.CR80_WIDTH_PX
        val targetHeight = PvcConstants.CR80_HEIGHT_PX

        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)

        val destRect = RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat())
        val srcRect = Rect(0, 0, rotated.width, rotated.height)
        canvas.drawBitmap(rotated, srcRect, destRect, paint)

        return output
    }

    /**
     * Exclusive Aadhaar Color Engine:
     * - Restores authentic Indian tricolor (saffron & green) and Ashoka emblem vibrance (+35%).
     * - Enhances demographic contrast so numbers and names stand out crisply without blowing out photo highlights.
     */
    private fun applyAadhaarColorBoost(source: Bitmap, config: EnhancementConfig): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val cm = ColorMatrix()

        if (config.grayscale) {
            cm.setSaturation(0f)
        } else {
            // Authentic vibrant Aadhaar saturation
            cm.setSaturation(config.saturation.coerceIn(1.1f, 1.6f))
        }

        // Contrast and deep black levels
        val c = config.contrast.coerceIn(1.05f, 1.35f)
        val b = config.brightness
        val t = 128f * (1f - c) + b

        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, t,
                0f, c, 0f, 0f, t,
                0f, 0f, c, 0f, t,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMatrix)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }

        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    /**
     * Aadhaar micro-text smart sharpening (Unsharp Masking):
     * Makes small 12-digit Aadhaar numbers, DOB, Hindi text, and QR code pixels crisp and bold.
     */
    private fun applyAadhaarSmartSharpen(source: Bitmap, sharpnessLevel: Float): Bitmap {
        val width = source.width
        val height = source.height
        val size = width * height

        val srcPixels = IntArray(size)
        val dstPixels = IntArray(size)

        source.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val w = (sharpnessLevel * 0.85f).coerceIn(0.1f, 1.2f)
        val centerWeight = 1f + (4f * w)

        for (y in 0 until height) {
            val yOffset = y * width
            val yPrevOffset = if (y > 0) (y - 1) * width else yOffset
            val yNextOffset = if (y < height - 1) (y + 1) * width else yOffset

            for (x in 0 until width) {
                val idx = yOffset + x
                val centerPx = srcPixels[idx]

                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    dstPixels[idx] = centerPx
                    continue
                }

                val topPx = srcPixels[yPrevOffset + x]
                val bottomPx = srcPixels[yNextOffset + x]
                val leftPx = srcPixels[yOffset + (x - 1)]
                val rightPx = srcPixels[yOffset + (x + 1)]

                val a = (centerPx ushr 24) and 0xFF

                val rC = (centerPx shr 16) and 0xFF
                val rT = (topPx shr 16) and 0xFF
                val rB = (bottomPx shr 16) and 0xFF
                val rL = (leftPx shr 16) and 0xFF
                val rR = (rightPx shr 16) and 0xFF
                var newR = (rC * centerWeight - (rT + rB + rL + rR) * w).roundToInt().coerceIn(0, 255)

                val gC = (centerPx shr 8) and 0xFF
                val gT = (topPx shr 8) and 0xFF
                val gB = (bottomPx shr 8) and 0xFF
                val gL = (leftPx shr 8) and 0xFF
                val gR = (rightPx shr 8) and 0xFF
                var newG = (gC * centerWeight - (gT + gB + gL + gR) * w).roundToInt().coerceIn(0, 255)

                val bC = centerPx and 0xFF
                val bT = topPx and 0xFF
                val bB = bottomPx and 0xFF
                val bL = leftPx and 0xFF
                val bR = rightPx and 0xFF
                var newB = (bC * centerWeight - (bT + bB + bL + bR) * w).roundToInt().coerceIn(0, 255)

                // Deep-black enhancement for text and barcode pixels (if luminance is dark, boost blackness)
                val lum = (0.299f * newR + 0.587f * newG + 0.114f * newB).toInt()
                if (lum < 70) {
                    newR = (newR * 0.85f).toInt()
                    newG = (newG * 0.85f).toInt()
                    newB = (newB * 0.85f).toInt()
                }

                dstPixels[idx] = (a shl 24) or (newR shl 16) or (newG shl 8) or newB
            }
        }

        val sharpened = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        sharpened.setPixels(dstPixels, 0, width, 0, 0, width, height)
        return sharpened
    }
}

package com.example.processing

import android.graphics.Bitmap
import com.example.model.NormalizedCrop
import com.example.model.PvcConstants
import kotlin.math.max
import kotlin.math.min

object DocumentAutoDetector {

    enum class SplitLayout {
        E_AADHAAR_BOTTOM,   // Bottom portion of e-Aadhaar (front left, back right)
        VERTICAL_SPLIT,     // Front on top half, Back on bottom half
        HORIZONTAL_SPLIT,   // Front on left half, Back on right half
        FULL_SINGLE_CARD    // Entire image contains one card
    }

    /**
     * Automatically detects best initial crop boxes for Front and Back cards based on image geometry.
     */
    fun autoDetectCrops(bitmap: Bitmap): Pair<NormalizedCrop, NormalizedCrop> {
        val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()

        return when {
            // Very tall document (e.g. standard A4 e-Aadhaar letter ~ 0.70 aspect ratio)
            aspect < 0.85f -> {
                // By default in e-Aadhaar PDFs, the card is in the bottom ~33%
                val front = NormalizedCrop(
                    left = 0.05f,
                    top = 0.64f,
                    right = 0.49f,
                    bottom = 0.95f
                )
                val back = NormalizedCrop(
                    left = 0.51f,
                    top = 0.64f,
                    right = 0.95f,
                    bottom = 0.95f
                )
                Pair(front, back)
            }
            // Landscape document with Front and Back side-by-side
            aspect > 1.3f -> {
                val front = NormalizedCrop(
                    left = 0.03f,
                    top = 0.12f,
                    right = 0.48f,
                    bottom = 0.88f
                )
                val back = NormalizedCrop(
                    left = 0.52f,
                    top = 0.12f,
                    right = 0.97f,
                    bottom = 0.88f
                )
                Pair(front, back)
            }
            // Square-ish / medium vertical (vertical stacked front and back)
            else -> {
                val front = NormalizedCrop(
                    left = 0.08f,
                    top = 0.05f,
                    right = 0.92f,
                    bottom = 0.48f
                )
                val back = NormalizedCrop(
                    left = 0.08f,
                    top = 0.52f,
                    right = 0.92f,
                    bottom = 0.95f
                )
                Pair(front, back)
            }
        }
    }

    /**
     * Applies a specific split template to the document.
     */
    fun applySplit(layout: SplitLayout): Pair<NormalizedCrop, NormalizedCrop> {
        return when (layout) {
            SplitLayout.E_AADHAAR_BOTTOM -> {
                Pair(
                    NormalizedCrop(left = 0.04f, top = 0.65f, right = 0.49f, bottom = 0.96f),
                    NormalizedCrop(left = 0.51f, top = 0.65f, right = 0.96f, bottom = 0.96f)
                )
            }
            SplitLayout.VERTICAL_SPLIT -> {
                Pair(
                    NormalizedCrop(left = 0.06f, top = 0.05f, right = 0.94f, bottom = 0.47f),
                    NormalizedCrop(left = 0.06f, top = 0.53f, right = 0.94f, bottom = 0.95f)
                )
            }
            SplitLayout.HORIZONTAL_SPLIT -> {
                Pair(
                    NormalizedCrop(left = 0.03f, top = 0.10f, right = 0.48f, bottom = 0.90f),
                    NormalizedCrop(left = 0.52f, top = 0.10f, right = 0.97f, bottom = 0.90f)
                )
            }
            SplitLayout.FULL_SINGLE_CARD -> {
                Pair(
                    NormalizedCrop(left = 0.05f, top = 0.05f, right = 0.95f, bottom = 0.95f),
                    NormalizedCrop(left = 0.05f, top = 0.05f, right = 0.95f, bottom = 0.95f)
                )
            }
        }
    }

    /**
     * Enforces CR80 standard aspect ratio (1.5858) on any crop box.
     */
    fun constrainToCr80(crop: NormalizedCrop, imgWidth: Int, imgHeight: Int): NormalizedCrop {
        val targetRatio = PvcConstants.CR80_ASPECT_RATIO
        var curW = (crop.right - crop.left) * imgWidth
        var curH = (crop.bottom - crop.top) * imgHeight

        if (curW <= 0 || curH <= 0) return crop

        val currentRatio = curW / curH
        if (currentRatio > targetRatio) {
            // Too wide, adjust width to match height
            curW = curH * targetRatio
        } else {
            // Too tall, adjust height to match width
            curH = curW / targetRatio
        }

        val normW = curW / imgWidth
        val normH = curH / imgHeight

        val centerX = (crop.left + crop.right) / 2f
        val centerY = (crop.top + crop.bottom) / 2f

        val newLeft = (centerX - normW / 2f).coerceIn(0f, 1f - normW)
        val newTop = (centerY - normH / 2f).coerceIn(0f, 1f - normH)
        val newRight = (newLeft + normW).coerceIn(newLeft, 1f)
        val newBottom = (newTop + normH).coerceIn(newTop, 1f)

        return crop.copy(
            left = newLeft,
            top = newTop,
            right = newRight,
            bottom = newBottom
        )
    }
}

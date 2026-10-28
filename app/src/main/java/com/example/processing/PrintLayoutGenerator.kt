package com.example.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.example.model.CutMarkStyle
import com.example.model.LayoutArrangement
import com.example.model.PaperSize
import com.example.model.PvcConstants
import com.example.model.ShopDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrintLayoutGenerator {

    data class LayoutResult(
        val canvasBitmap: Bitmap,
        val paperSize: PaperSize,
        val layout: LayoutArrangement
    )

    /**
     * Generates a 300 DPI ultra-high-resolution print sheet ready for printing or exporting.
     */
    fun generatePrintCanvas(
        frontCard: Bitmap,
        backCard: Bitmap,
        paperSize: PaperSize = PaperSize.A4,
        layout: LayoutArrangement = LayoutArrangement.SIDE_BY_SIDE_FRONT_BACK,
        cutMarkStyle: CutMarkStyle = CutMarkStyle.HAIRLINE_TICKS,
        shopDetails: ShopDetails = ShopDetails()
    ): LayoutResult {
        val width = paperSize.widthPx
        val height = paperSize.heightPx

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.WHITE)

        val cardW = PvcConstants.CR80_WIDTH_PX.toFloat()
        val cardH = PvcConstants.CR80_HEIGHT_PX.toFloat()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)

        when (layout) {
            LayoutArrangement.SINGLE_FRONT_ONLY -> {
                val startX = (width - cardW) / 2f
                val startY = (height - cardH) / 2f
                val cardRect = RectF(startX, startY, startX + cardW, startY + cardH)
                canvas.drawBitmap(frontCard, Rect(0, 0, frontCard.width, frontCard.height), cardRect, paint)
                drawCutGuidelines(canvas, cardRect, cutMarkStyle)
                drawSideLabel(canvas, cardRect, "FRONT SIDE")
            }
            LayoutArrangement.SINGLE_BACK_ONLY -> {
                val startX = (width - cardW) / 2f
                val startY = (height - cardH) / 2f
                val cardRect = RectF(startX, startY, startX + cardW, startY + cardH)
                canvas.drawBitmap(backCard, Rect(0, 0, backCard.width, backCard.height), cardRect, paint)
                drawCutGuidelines(canvas, cardRect, cutMarkStyle)
                drawSideLabel(canvas, cardRect, "BACK SIDE")
            }
            LayoutArrangement.STACKED_VERTICAL -> {
                val startX = (width - cardW) / 2f
                val gap = 60f
                val totalH = (cardH * 2) + gap
                val startY = if (paperSize == PaperSize.A4) 180f else (height - totalH) / 2f

                val frontRect = RectF(startX, startY, startX + cardW, startY + cardH)
                val backRect = RectF(startX, startY + cardH + gap, startX + cardW, startY + totalH)

                canvas.drawBitmap(frontCard, Rect(0, 0, frontCard.width, frontCard.height), frontRect, paint)
                canvas.drawBitmap(backCard, Rect(0, 0, backCard.width, backCard.height), backRect, paint)

                drawCutGuidelines(canvas, frontRect, cutMarkStyle)
                drawCutGuidelines(canvas, backRect, cutMarkStyle)

                drawSideLabel(canvas, frontRect, "FRONT SIDE")
                drawSideLabel(canvas, backRect, "BACK SIDE")
            }
            LayoutArrangement.MULTIPLE_CARDS_BATCH -> {
                // Batch Sheet on A4: 5 Card pairs (10 sides arranged 2 columns x 5 rows)
                val marginX = 80f
                val colGap = 50f
                val rowGap = 35f
                val scale = 0.95f // slightly scaled for 5 pairs fit
                val bCardW = cardW * scale
                val bCardH = cardH * scale

                val startY = 120f
                for (row in 0 until 5) {
                    val y = startY + row * (bCardH + rowGap)
                    val x1 = marginX
                    val x2 = marginX + bCardW + colGap

                    val fRect = RectF(x1, y, x1 + bCardW, y + bCardH)
                    val bRect = RectF(x2, y, x2 + bCardW, y + bCardH)

                    canvas.drawBitmap(frontCard, Rect(0, 0, frontCard.width, frontCard.height), fRect, paint)
                    canvas.drawBitmap(backCard, Rect(0, 0, backCard.width, backCard.height), bRect, paint)

                    drawCutGuidelines(canvas, fRect, cutMarkStyle)
                    drawCutGuidelines(canvas, bRect, cutMarkStyle)
                }
            }
            LayoutArrangement.SIDE_BY_SIDE_FRONT_BACK -> {
                // Standard cyber cafe placement: Side by side near top of sheet
                val gap = 60f
                val totalW = (cardW * 2) + gap
                val startX = (width - totalW) / 2f
                val startY = if (paperSize == PaperSize.PHOTO_4X6) {
                    // In 4x6, stack vertically because width is 1200 px
                    160f
                } else {
                    150f
                }

                if (paperSize == PaperSize.PHOTO_4X6) {
                    val left = (width - cardW) / 2f
                    val frontRect = RectF(left, startY, left + cardW, startY + cardH)
                    val backRect = RectF(left, startY + cardH + 80f, left + cardW, startY + (cardH * 2) + 80f)

                    canvas.drawBitmap(frontCard, Rect(0, 0, frontCard.width, frontCard.height), frontRect, paint)
                    canvas.drawBitmap(backCard, Rect(0, 0, backCard.width, backCard.height), backRect, paint)

                    drawCutGuidelines(canvas, frontRect, cutMarkStyle)
                    drawCutGuidelines(canvas, backRect, cutMarkStyle)

                    drawSideLabel(canvas, frontRect, "FRONT SIDE")
                    drawSideLabel(canvas, backRect, "BACK SIDE")
                } else {
                    val frontRect = RectF(startX, startY, startX + cardW, startY + cardH)
                    val backRect = RectF(startX + cardW + gap, startY, startX + totalW, startY + cardH)

                    canvas.drawBitmap(frontCard, Rect(0, 0, frontCard.width, frontCard.height), frontRect, paint)
                    canvas.drawBitmap(backCard, Rect(0, 0, backCard.width, backCard.height), backRect, paint)

                    drawCutGuidelines(canvas, frontRect, cutMarkStyle)
                    drawCutGuidelines(canvas, backRect, cutMarkStyle)

                    drawSideLabel(canvas, frontRect, "FRONT SIDE")
                    drawSideLabel(canvas, backRect, "BACK SIDE")
                }
            }
        }

        // Draw Shop Name, Contact, and Cyber Cafe Metadata Footer
        drawShopFooter(canvas, width, height, shopDetails)

        return LayoutResult(output, paperSize, layout)
    }

    private fun drawSideLabel(canvas: Canvas, rect: RectF, label: String) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 22f
            isFakeBoldText = true
        }
        canvas.drawText(label, rect.left, rect.top - 16f, labelPaint)
    }

    private fun drawCutGuidelines(canvas: Canvas, rect: RectF, style: CutMarkStyle) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (style) {
            CutMarkStyle.HAIRLINE_TICKS -> {
                paint.color = Color.parseColor("#334155")
                paint.strokeWidth = 2f
                paint.style = Paint.Style.STROKE

                val tickLen = 30f
                val offset = 4f

                // Top-Left
                canvas.drawLine(rect.left - offset - tickLen, rect.top, rect.left - offset, rect.top, paint)
                canvas.drawLine(rect.left, rect.top - offset - tickLen, rect.left, rect.top - offset, paint)

                // Top-Right
                canvas.drawLine(rect.right + offset, rect.top, rect.right + offset + tickLen, rect.top, paint)
                canvas.drawLine(rect.right, rect.top - offset - tickLen, rect.right, rect.top - offset, paint)

                // Bottom-Left
                canvas.drawLine(rect.left - offset - tickLen, rect.bottom, rect.left - offset, rect.bottom, paint)
                canvas.drawLine(rect.left, rect.bottom + offset, rect.left, rect.bottom + offset + tickLen, paint)

                // Bottom-Right
                canvas.drawLine(rect.right + offset, rect.bottom, rect.right + offset + tickLen, rect.bottom, paint)
                canvas.drawLine(rect.right, rect.bottom + offset, rect.right, rect.bottom + offset + tickLen, paint)
            }
            CutMarkStyle.DASHED_BORDER -> {
                paint.color = Color.parseColor("#64748B")
                paint.strokeWidth = 1.5f
                paint.style = Paint.Style.STROKE
                paint.pathEffect = DashPathEffect(floatArrayOf(10f, 6f), 0f)
                canvas.drawRect(rect, paint)
            }
            CutMarkStyle.SOLID_HAIRLINE -> {
                paint.color = Color.parseColor("#94A3B8")
                paint.strokeWidth = 1.0f
                paint.style = Paint.Style.STROKE
                canvas.drawRect(rect, paint)
            }
            CutMarkStyle.NONE -> {
                // No lines
            }
        }
    }

    private fun drawShopFooter(canvas: Canvas, canvasW: Int, canvasH: Int, details: ShopDetails) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 22f
        }

        val dateStr = if (details.showTimestamp) {
            val sdf = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault())
            " | Date: " + sdf.format(Date())
        } else ""

        val contactStr = if (details.contactNumber.isNotBlank()) " | Ph: ${details.contactNumber}" else ""
        val text = "${details.shopName}$contactStr$dateStr | CR80 (85.60 x 53.98 mm) @ 300 DPI"

        canvas.drawText(text, 80f, canvasH - 50f, paint)

        // Alignment center crosshair at top
        val alignPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1.5f
        }
        val midX = canvasW / 2f
        canvas.drawLine(midX, 20f, midX, 55f, alignPaint)
        canvas.drawLine(midX - 18f, 37.5f, midX + 18f, 37.5f, alignPaint)
    }
}

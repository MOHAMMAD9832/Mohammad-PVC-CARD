package com.example.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object PdfProcessor {

    data class PdfPageResult(
        val pages: List<Bitmap>,
        val pageCount: Int
    )

    /**
     * Renders pages of a PDF document at high resolution (e.g. ~250-300 DPI) for card extraction.
     */
    fun renderPdfPages(context: Context, pdfUri: Uri, maxPagesToRender: Int = 4): PdfPageResult {
        val tempFile = File(context.cacheDir, "temp_uploaded_${System.currentTimeMillis()}.pdf")
        try {
            // Copy Uri stream to temp file
            context.contentResolver.openInputStream(pdfUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return PdfPageResult(emptyList(), 0)

            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(pfd)
            val totalPages = pdfRenderer.pageCount
            val pagesToLoad = minOf(totalPages, maxPagesToRender)
            val renderedBitmaps = mutableListOf<Bitmap>()

            for (i in 0 until pagesToLoad) {
                val page = pdfRenderer.openPage(i)

                // Standard PDF points are 72 DPI. Target ~216 DPI (3x scale) for sharp preview and extraction
                val scale = 3.0f
                val renderWidth = (page.width * scale).roundToInt().coerceIn(800, 2400)
                val renderHeight = (page.height * scale).roundToInt().coerceIn(1000, 3200)

                val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                // Fill with white background (PDF pages can be transparent)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                page.close()

                renderedBitmaps.add(bitmap)
            }

            pdfRenderer.close()
            pfd.close()

            return PdfPageResult(renderedBitmaps, totalPages)
        } catch (e: Exception) {
            e.printStackTrace()
            return PdfPageResult(emptyList(), 0)
        } finally {
            tempFile.delete()
        }
    }
}

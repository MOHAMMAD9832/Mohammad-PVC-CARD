package com.example.processing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.model.PaperSize
import java.io.File
import java.io.FileOutputStream

object PrintHelperUtil {

    /**
     * Sends the layout bitmap directly to Android PrintManager for physical printing.
     */
    fun printDocument(
        context: Context,
        bitmap: Bitmap,
        paperSize: PaperSize,
        jobName: String = "PVC_CR80_PrintJob"
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                val pdfDoc = PdfDocument()
                // A4 dimensions in standard PDF 72 DPI points: 595 x 842
                val pageWidthPt = if (paperSize == PaperSize.PHOTO_4X6) 288 else 595
                val pageHeightPt = if (paperSize == PaperSize.PHOTO_4X6) 432 else 842

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, 1).create()
                val page = pdfDoc.startPage(pageInfo)

                val canvas = page.canvas
                val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                val dstRect = Rect(0, 0, pageWidthPt, pageHeightPt)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(bitmap, srcRect, dstRect, paint)

                pdfDoc.finishPage(page)

                try {
                    FileOutputStream(destination?.fileDescriptor).use { out ->
                        pdfDoc.writeTo(out)
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    pdfDoc.close()
                }
            }
        }

        val printAttributes = PrintAttributes.Builder()
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMediaSize(
                if (paperSize == PaperSize.PHOTO_4X6)
                    PrintAttributes.MediaSize.NA_INDEX_4X6
                else
                    PrintAttributes.MediaSize.ISO_A4
            )
            .build()

        printManager.print(jobName, printAdapter, printAttributes)
    }

    /**
     * Exports the 300 DPI print canvas to an exact-size PDF file.
     */
    fun exportToPdf(
        context: Context,
        bitmap: Bitmap,
        paperSize: PaperSize,
        fileName: String = "PVC_Card_Layout_300DPI.pdf"
    ): File {
        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outputFile = File(outputDir, fileName)

        val pdfDoc = PdfDocument()
        val pageWidthPt = if (paperSize == PaperSize.PHOTO_4X6) 288 else 595
        val pageHeightPt = if (paperSize == PaperSize.PHOTO_4X6) 432 else 842

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, 1).create()
        val page = pdfDoc.startPage(pageInfo)

        val canvas = page.canvas
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
        val dstRect = Rect(0, 0, pageWidthPt, pageHeightPt)
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)

        pdfDoc.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return outputFile
    }

    /**
     * Exports full 300 DPI high-resolution PNG or JPEG file.
     */
    fun exportToImage(
        context: Context,
        bitmap: Bitmap,
        isPng: Boolean = true,
        fileName: String? = null
    ): File {
        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val name = fileName ?: ("PVC_Card_300DPI_" + System.currentTimeMillis() + if (isPng) ".png" else ".jpg")
        val outputFile = File(outputDir, name)

        val format = if (isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val quality = if (isPng) 100 else 98

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(format, quality, out)
        }

        return outputFile
    }

    /**
     * Opens system share sheet for the exported PDF or Image.
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String = "Share PVC Card Print File") {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}

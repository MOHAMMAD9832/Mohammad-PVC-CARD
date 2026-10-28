package com.example.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import com.example.model.CardPreset

object SampleDocumentProvider {

    /**
     * Generates a realistic sample document for immediate 1-click cyber cafe testing.
     */
    fun createSampleDocument(preset: CardPreset): Bitmap {
        return when (preset) {
            CardPreset.AADHAAR -> createSampleAadhaarSheet()
            CardPreset.PAN -> createSamplePanCardSheet()
            CardPreset.VOTER_ID -> createSampleVoterCardSheet()
            CardPreset.RATION_CARD -> createSampleRationCardSheet()
        }
    }

    /**
     * Generates an official e-Aadhaar style sheet (1600 x 2260 px)
     * with the CR80 card section at the bottom (front on left, back on right).
     */
    private fun createSampleAadhaarSheet(): Bitmap {
        val width = 1600
        val height = 2260
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Top Header
        paint.color = Color.parseColor("#E0E7FF")
        canvas.drawRect(80f, 60f, 1520f, 180f, paint)

        paint.color = Color.parseColor("#1E3A8A")
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText("UNIQUE IDENTIFICATION AUTHORITY OF INDIA", 120f, 120f, paint)

        paint.textSize = 22f
        paint.color = Color.parseColor("#475569")
        paint.isFakeBoldText = false
        canvas.drawText("Government of India - e-Aadhaar Document (Mohammad PVC Card Studio Ready)", 120f, 155f, paint)

        // Text lines placeholder
        paint.color = Color.parseColor("#E2E8F0")
        for (i in 0..12) {
            val y = 240f + (i * 45f)
            canvas.drawRect(80f, y, (800 + (i % 4) * 180).toFloat(), y + 16f, paint)
        }

        // Cut line
        paint.color = Color.parseColor("#94A3B8")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.pathEffect = DashPathEffect(floatArrayOf(20f, 15f), 0f)
        val yCut = 1450f
        canvas.drawLine(60f, yCut, 1540f, yCut, paint)
        paint.pathEffect = null
        paint.style = Paint.Style.FILL

        paint.textSize = 24f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("-- Cut Here for Standard CR80 PVC Card Laminate / Print --", 480f, yCut - 15f, paint)

        // Bottom 33% card section
        val cardW = 680f
        val cardH = 428f
        val cardY = 1520f

        // Front Side (Left)
        drawAadhaarFront(canvas, 90f, cardY, cardW, cardH)

        // Back Side (Right)
        drawAadhaarBack(canvas, 830f, cardY, cardW, cardH)

        return bitmap
    }

    private fun drawAadhaarFront(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.parseColor("#FAFAFA")
        val cardRect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(cardRect, 20f, 20f, paint)

        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(cardRect, 20f, 20f, paint)
        paint.style = Paint.Style.FILL

        // Authentic Indian Tricolor header
        paint.color = Color.parseColor("#FF9933")
        canvas.drawRect(x, y, x + w, y + 28f, paint)
        paint.color = Color.parseColor("#138808")
        canvas.drawRect(x, y + 28f, x + w, y + 42f, paint)

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("Government of India / भारत सरकार", x + 130f, y + 78f, paint)

        // Ashoka Emblem
        paint.color = Color.parseColor("#1E3A8A")
        canvas.drawCircle(x + 70f, y + 75f, 32f, paint)
        paint.color = Color.WHITE
        paint.textSize = 16f
        canvas.drawText("INDIA", x + 50f, y + 80f, paint)

        // Photo slot
        val photoL = x + 35f
        val photoT = y + 130f
        val photoW = 160f
        val photoH = 195f
        paint.color = Color.parseColor("#DBEAFE")
        canvas.drawRoundRect(RectF(photoL, photoT, photoL + photoW, photoT + photoH), 12f, 12f, paint)

        paint.color = Color.parseColor("#3B82F6")
        canvas.drawCircle(photoL + 80f, photoT + 75f, 38f, paint)
        canvas.drawRoundRect(RectF(photoL + 30f, photoT + 125f, photoL + 130f, photoT + 185f), 15f, 15f, paint)

        // Citizen Details
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Rahul Sharma", x + 225f, y + 165f, paint)

        paint.textSize = 19f
        paint.color = Color.parseColor("#334155")
        paint.isFakeBoldText = false
        canvas.drawText("DOB / जन्म तिथि: 15/08/1992", x + 225f, y + 205f, paint)
        canvas.drawText("Gender / लिंग: MALE / पुरुष", x + 225f, y + 240f, paint)

        // QR Code Box
        val qrL = x + w - 170f
        val qrT = y + 135f
        paint.color = Color.parseColor("#0F172A")
        canvas.drawRoundRect(RectF(qrL, qrT, qrL + 140f, qrT + 140f), 8f, 8f, paint)
        paint.color = Color.WHITE
        canvas.drawRoundRect(RectF(qrL + 12f, qrT + 12f, qrL + 128f, qrT + 128f), 4f, 4f, paint)
        paint.color = Color.parseColor("#0F172A")
        canvas.drawRect(qrL + 25f, qrT + 25f, qrL + 65f, qrT + 65f, paint)
        canvas.drawRect(qrL + 75f, qrT + 25f, qrL + 115f, qrT + 65f, paint)
        canvas.drawRect(qrL + 25f, qrT + 75f, qrL + 65f, qrT + 115f, paint)

        // Red separator & Bold Aadhaar Number
        paint.color = Color.parseColor("#DC2626")
        canvas.drawRect(x, y + h - 75f, x + w, y + h - 70f, paint)

        paint.color = Color.parseColor("#B91C1C")
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText("5421  8963  2145", x + 160f, y + h - 25f, paint)
    }

    private fun drawAadhaarBack(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.parseColor("#FAFAFA")
        val cardRect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(cardRect, 20f, 20f, paint)

        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(cardRect, 20f, 20f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#FF9933")
        canvas.drawRect(x, y, x + w, y + 20f, paint)
        paint.color = Color.parseColor("#138808")
        canvas.drawRect(x, y + 20f, x + w, y + 30f, paint)

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 21f
        paint.isFakeBoldText = true
        canvas.drawText("Address / पता:", x + 40f, y + 65f, paint)

        paint.color = Color.parseColor("#334155")
        paint.textSize = 19f
        paint.isFakeBoldText = false
        canvas.drawText("S/O: Mohan Sharma, H.No 42B, Sector 18", x + 40f, y + 105f, paint)
        canvas.drawText("Gandhinagar, Bengaluru, Karnataka - 560001", x + 40f, y + 140f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 18f
        canvas.drawText("एस/ओ: मोहन शर्मा, मकान नं 42बी, सेक्टर 18", x + 40f, y + 180f, paint)
        canvas.drawText("गांधीनगर, बेंगलुरु, कर्नाटक - 560001", x + 40f, y + 215f, paint)

        val qrL = x + w - 210f
        val qrT = y + 70f
        paint.color = Color.parseColor("#0F172A")
        canvas.drawRoundRect(RectF(qrL, qrT, qrL + 180f, qrT + 180f), 8f, 8f, paint)

        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(x, y + h - 80f, x + w, y + h - 77f, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 17f
        canvas.drawText("Help: 1947 | help@uidai.gov.in | www.uidai.gov.in", x + 40f, y + h - 45f, paint)

        paint.color = Color.parseColor("#B91C1C")
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("5421  8963  2145", x + w - 260f, y + h - 45f, paint)
    }

    /**
     * Sample PAN Card Sheet (1800 x 1100 px)
     */
    private fun createSamplePanCardSheet(): Bitmap {
        val width = 1800
        val height = 1100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val cardW = 800f
        val cardH = 504f
        val cardY = 300f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(0f, 0f, width.toFloat(), 180f, paint)

        paint.color = Color.parseColor("#1E3A8A")
        paint.textSize = 36f
        paint.isFakeBoldText = true
        canvas.drawText("INCOME TAX DEPARTMENT - PERMANENT ACCOUNT NUMBER", 80f, 80f, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText("CR80 PVC Card Standard Layout • 1:1 Original Scanned Colors", 80f, 130f, paint)

        // Front Side
        drawPanFront(canvas, 60f, cardY, cardW, cardH)

        // Back Side
        drawPanBack(canvas, 920f, cardY, cardW, cardH)

        return bitmap
    }

    private fun drawPanFront(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.parseColor("#F0F9FF")
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 24f, 24f, paint)

        paint.color = Color.parseColor("#BAE6FD")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(rect, 24f, 24f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#0284C7")
        canvas.drawRoundRect(RectF(x, y, x + w, y + 80f), 24f, 24f, paint)
        canvas.drawRect(x, y + 40f, x + w, y + 80f, paint)

        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("INCOME TAX DEPARTMENT / आयकर विभाग", x + 120f, y + 48f, paint)

        val photoRect = RectF(x + 40f, y + 110f, x + 230f, y + 340f)
        paint.color = Color.parseColor("#E0F2FE")
        canvas.drawRoundRect(photoRect, 12f, 12f, paint)
        paint.color = Color.parseColor("#0284C7")
        canvas.drawCircle(x + 135f, y + 200f, 45f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("Name: VIKRAM SINGHANIA", x + 260f, y + 140f, paint)
        canvas.drawText("Father's Name: RAJESH SINGHANIA", x + 260f, y + 190f, paint)
        canvas.drawText("Date of Birth: 22/11/1988", x + 260f, y + 240f, paint)

        paint.color = Color.parseColor("#1E3A8A")
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText("PAN: ABCDS1234F", x + 260f, y + 310f, paint)

        // Signature strip
        paint.color = Color.WHITE
        val sigRect = RectF(x + 40f, y + 380f, x + 400f, y + 470f)
        canvas.drawRoundRect(sigRect, 8f, 8f, paint)
        paint.color = Color.parseColor("#94A3B8")
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(sigRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 22f
        canvas.drawText("Signature / हस्ताक्षर", x + 50f, y + 435f, paint)
    }

    private fun drawPanBack(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.parseColor("#F8FAFC")
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 24f, 24f, paint)

        paint.color = Color.parseColor("#CBD5E1")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(rect, 24f, 24f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#334155")
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("IMPORTANT INFORMATION / महत्वपूर्ण सूचना", x + 50f, y + 80f, paint)

        paint.textSize = 17f
        paint.color = Color.parseColor("#64748B")
        paint.isFakeBoldText = false
        canvas.drawText("1. This card is valid proof of identity across India.", x + 50f, y + 130f, paint)
        canvas.drawText("2. Possession or use of more than one PAN is illegal.", x + 50f, y + 170f, paint)

        paint.color = Color.parseColor("#0F172A")
        canvas.drawRoundRect(RectF(x + 50f, y + 270f, x + w - 50f, y + 390f), 8f, 8f, paint)
    }

    /**
     * Sample Voter Card (EPIC) Sheet
     */
    private fun createSampleVoterCardSheet(): Bitmap {
        val width = 1800
        val height = 1100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val cardW = 800f
        val cardH = 504f
        val cardY = 300f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#FEF3C7")
        canvas.drawRect(0f, 0f, width.toFloat(), 180f, paint)

        paint.color = Color.parseColor("#92400E")
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText("ELECTION COMMISSION OF INDIA / भारत निर्वाचन आयोग", 80f, 80f, paint)

        paint.color = Color.parseColor("#78350F")
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText("Elector's Photo Identity Card (EPIC) • 1:1 Original Color", 80f, 130f, paint)

        // Draw Front & Back
        drawVoterFront(canvas, 60f, cardY, cardW, cardH)
        drawVoterBack(canvas, 920f, cardY, cardW, cardH)

        return bitmap
    }

    private fun drawVoterFront(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#FFFBEB")
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 24f, 24f, paint)

        paint.color = Color.parseColor("#FDE68A")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(rect, 24f, 24f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#B45309")
        canvas.drawRect(x, y, x + w, y + 60f, paint)

        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("ELECTION COMMISSION OF INDIA", x + 80f, y + 40f, paint)

        paint.color = Color.parseColor("#1E3A8A")
        paint.textSize = 30f
        canvas.drawText("EPIC NO: WBF2194812", x + 40f, y + 110f, paint)

        // Photo slot
        val photoRect = RectF(x + 40f, y + 140f, x + 230f, y + 370f)
        paint.color = Color.parseColor("#FEF3C7")
        canvas.drawRoundRect(photoRect, 10f, 10f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("Elector: AMITAV CHATTERJEE", x + 260f, y + 180f, paint)
        canvas.drawText("Father: S. CHATTERJEE", x + 260f, y + 230f, paint)
        canvas.drawText("Gender: MALE", x + 260f, y + 280f, paint)
        canvas.drawText("DOB: 10/04/1985", x + 260f, y + 330f, paint)
    }

    private fun drawVoterBack(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#FFFBEB")
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 24f, 24f, paint)

        paint.color = Color.parseColor("#FDE68A")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(rect, 24f, 24f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("Address / पता:", x + 40f, y + 70f, paint)

        paint.textSize = 19f
        paint.color = Color.parseColor("#334155")
        paint.isFakeBoldText = false
        canvas.drawText("Plot 12, Park Street, Ward 64, Kolkata - 700016", x + 40f, y + 120f, paint)
        canvas.drawText("Assembly Constituency: 162 - Chowringhee", x + 40f, y + 170f, paint)
        canvas.drawText("Polling Station: 84 - St. Xavier's School", x + 40f, y + 220f, paint)
    }

    /**
     * Sample Ration Card / Other Sheet
     */
    private fun createSampleRationCardSheet(): Bitmap {
        val width = 1800
        val height = 1100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val cardW = 800f
        val cardH = 504f
        val cardY = 300f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#ECFDF5")
        canvas.drawRect(0f, 0f, width.toFloat(), 180f, paint)

        paint.color = Color.parseColor("#065F46")
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText("FOOD & CIVIL SUPPLIES DEPARTMENT / खाद्य विभाग", 80f, 80f, paint)

        paint.color = Color.parseColor("#047857")
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText("Digital Ration Card / NFSA Beneficiary • 1:1 Original Scanned Colors", 80f, 130f, paint)

        // Front & Back
        drawRationFront(canvas, 60f, cardY, cardW, cardH)
        drawRationBack(canvas, 920f, cardY, cardW, cardH)

        return bitmap
    }

    private fun drawRationFront(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#F0FDF4")
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 24f, 24f, paint)

        paint.color = Color.parseColor("#A7F3D0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(rect, 24f, 24f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#059669")
        canvas.drawRect(x, y, x + w, y + 60f, paint)

        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("DIGITAL RATION CARD (NFSA/PHH)", x + 60f, y + 40f, paint)

        paint.color = Color.parseColor("#065F46")
        paint.textSize = 28f
        canvas.drawText("RC NO: 194820194821", x + 40f, y + 110f, paint)

        val photoRect = RectF(x + 40f, y + 140f, x + 220f, y + 360f)
        paint.color = Color.parseColor("#DCFCE7")
        canvas.drawRoundRect(photoRect, 10f, 10f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("Head: FATIMA KHATOON", x + 250f, y + 180f, paint)
        canvas.drawText("Members: 4 (Adult: 2, Child: 2)", x + 250f, y + 230f, paint)
        canvas.drawText("Category: Priority Household (PHH)", x + 250f, y + 280f, paint)
        canvas.drawText("FPS Shop No: 40182", x + 250f, y + 330f, paint)
    }

    private fun drawRationBack(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#F0FDF4")
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 24f, 24f, paint)

        paint.color = Color.parseColor("#A7F3D0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(rect, 24f, 24f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("Family Members Details / पारिवारिक विवरण:", x + 40f, y + 70f, paint)

        paint.textSize = 19f
        paint.color = Color.parseColor("#334155")
        paint.isFakeBoldText = false
        canvas.drawText("1. Fatima Khatoon (Self) - UID: XXXX 2104", x + 40f, y + 120f, paint)
        canvas.drawText("2. Mohammed Salim (Spouse) - UID: XXXX 8912", x + 40f, y + 165f, paint)
        canvas.drawText("3. Ayan Salim (Son) - UID: XXXX 4192", x + 40f, y + 210f, paint)
        canvas.drawText("4. Zoya Salim (Daughter) - UID: XXXX 9081", x + 40f, y + 255f, paint)
    }
}

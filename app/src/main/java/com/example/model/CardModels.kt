package com.example.model

import android.graphics.Bitmap

/**
 * Standard CR80 PVC dimensions and 300 DPI resolution constants.
 * CR80: 85.60 mm x 53.98 mm (3.370" x 2.125").
 */
object PvcConstants {
    const val CR80_WIDTH_MM = 85.60f
    const val CR80_HEIGHT_MM = 53.98f
    const val CR80_ASPECT_RATIO = CR80_WIDTH_MM / CR80_HEIGHT_MM // ~1.58577f

    // 300 DPI calculations
    const val DPI = 300
    const val MM_TO_INCH = 1f / 25.4f
    
    // Exact pixels at 300 DPI (rounded to standard CR80 photo print dimensions)
    const val CR80_WIDTH_PX = 1011 // 85.60 mm * (300 / 25.4) = 1011.02 -> 1011 px
    const val CR80_HEIGHT_PX = 638 // 53.98 mm * (300 / 25.4) = 637.56 -> 638 px
    const val CR80_CORNER_RADIUS_PX = 38f // 3.18mm corner radius

    // A4 Paper at 300 DPI (210 x 297 mm)
    const val A4_WIDTH_PX = 2480
    const val A4_HEIGHT_PX = 3508

    // 4x6 Photo Paper at 300 DPI (4" x 6" = 101.6 x 152.4 mm)
    const val PHOTO_4X6_WIDTH_PX = 1200
    const val PHOTO_4X6_HEIGHT_PX = 1800
}

enum class CardPreset(
    val title: String,
    val subtitle: String,
    val isAadhaarColorBoostAllowed: Boolean,
    val defaultSharpness: Float,
    val defaultSaturation: Float,
    val defaultContrast: Float,
    val defaultBrightness: Float
) {
    AADHAAR(
        title = "Aadhaar Card",
        subtitle = "Original HD Color Boost • Deep Black Text & Tricolor",
        isAadhaarColorBoostAllowed = true,
        defaultSharpness = 0.45f,
        defaultSaturation = 1.35f,
        defaultContrast = 1.15f,
        defaultBrightness = 4f
    ),
    PAN(
        title = "PAN Card",
        subtitle = "1:1 Original Scanned Colors • Lossless 300 DPI",
        isAadhaarColorBoostAllowed = false,
        defaultSharpness = 0.0f,
        defaultSaturation = 1.0f,
        defaultContrast = 1.0f,
        defaultBrightness = 0f
    ),
    VOTER_ID(
        title = "Voter Card",
        subtitle = "1:1 Original Scanned Colors • Lossless 300 DPI",
        isAadhaarColorBoostAllowed = false,
        defaultSharpness = 0.0f,
        defaultSaturation = 1.0f,
        defaultContrast = 1.0f,
        defaultBrightness = 0f
    ),
    RATION_CARD(
        title = "Ration Card / Other",
        subtitle = "1:1 Original Scanned Colors • Lossless 300 DPI",
        isAadhaarColorBoostAllowed = false,
        defaultSharpness = 0.0f,
        defaultSaturation = 1.0f,
        defaultContrast = 1.0f,
        defaultBrightness = 0f
    )
}

enum class CardSide {
    FRONT,
    BACK
}

data class EnhancementConfig(
    val isAadhaarColorBoost: Boolean = false,
    val brightness: Float = 0f,        // -50 to +50
    val contrast: Float = 1.0f,        // 0.6 to 2.0
    val saturation: Float = 1.0f,      // 0.0 to 2.5
    val sharpness: Float = 0.0f,       // 0.0 to 1.0
    val antiAlias: Boolean = true,
    val grayscale: Boolean = false
) {
    companion object {
        fun fromPreset(preset: CardPreset): EnhancementConfig {
            return if (preset.isAadhaarColorBoostAllowed) {
                EnhancementConfig(
                    isAadhaarColorBoost = true,
                    brightness = preset.defaultBrightness,
                    contrast = preset.defaultContrast,
                    saturation = preset.defaultSaturation,
                    sharpness = preset.defaultSharpness,
                    antiAlias = true,
                    grayscale = false
                )
            } else {
                // Strictly 1:1 original scanned colors and tones for PAN, Voter, Ration / Other
                EnhancementConfig(
                    isAadhaarColorBoost = false,
                    brightness = 0f,
                    contrast = 1.0f,
                    saturation = 1.0f,
                    sharpness = 0.0f,
                    antiAlias = true,
                    grayscale = false
                )
            }
        }
    }
}

enum class PaperSize(val displayName: String, val widthPx: Int, val heightPx: Int, val description: String) {
    A4(
        displayName = "A4 Sheet (210 x 297 mm)",
        widthPx = PvcConstants.A4_WIDTH_PX,
        heightPx = PvcConstants.A4_HEIGHT_PX,
        description = "Standard printer sheet (300 DPI, 2480 x 3508 px)"
    ),
    PHOTO_4X6(
        displayName = "4x6 Photo Paper (10x15 cm)",
        widthPx = PvcConstants.PHOTO_4X6_WIDTH_PX,
        heightPx = PvcConstants.PHOTO_4X6_HEIGHT_PX,
        description = "Glossy photo paper for Canon/Epson photo printers"
    ),
    PVC_TRAY(
        displayName = "Epson/Canon PVC Tray",
        widthPx = 1200,
        heightPx = 1800,
        description = "Dedicated dual-card PVC printing tray"
    )
}

enum class LayoutArrangement(val displayName: String, val description: String) {
    SIDE_BY_SIDE_FRONT_BACK("Side-by-Side (Front + Back)", "Horizontal placement with cut margin"),
    STACKED_VERTICAL("Stacked (Front + Back)", "Vertical placement with fold/cut line"),
    SINGLE_FRONT_ONLY("Single Card (Front Only)", "Single CR80 card placed centered"),
    SINGLE_BACK_ONLY("Single Card (Back Only)", "Single CR80 back side placed centered"),
    MULTIPLE_CARDS_BATCH("Multiple Cards Sheet", "5 Cards (10 sides) array on A4 sheet")
}

enum class CutMarkStyle(val displayName: String) {
    HAIRLINE_TICKS("Hairline Corner Ticks (L-Marks)"),
    DASHED_BORDER("Subtle Dashed Hairline"),
    SOLID_HAIRLINE("Ultra-Thin Solid Hairline"),
    NONE("Clean / No Border")
}

data class ShopDetails(
    val shopName: String = "Mohammad PVC Card Studio",
    val contactNumber: String = "",
    val showTimestamp: Boolean = true,
    val showCutMarks: Boolean = true
)

/**
 * Normalized crop bounding box [0.0..1.0] relative to raw document image.
 */
data class NormalizedCrop(
    val left: Float = 0.05f,
    val top: Float = 0.05f,
    val right: Float = 0.95f,
    val bottom: Float = 0.95f,
    val rotationDegrees: Int = 0 // 0, 90, 180, 270
) {
    val width: Float get() = (right - left).coerceAtLeast(0.01f)
    val height: Float get() = (bottom - top).coerceAtLeast(0.01f)
    val aspectRatio: Float get() = width / height
}

data class CardSideData(
    val rawBitmap: Bitmap? = null,
    val crop: NormalizedCrop = NormalizedCrop(),
    val enhancement: EnhancementConfig = EnhancementConfig(),
    val processedBitmap: Bitmap? = null,
    val isAutoDetected: Boolean = false
)

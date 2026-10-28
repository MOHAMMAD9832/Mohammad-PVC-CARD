package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CardPreset
import com.example.model.CardSide
import com.example.model.CardSideData
import com.example.model.CutMarkStyle
import com.example.model.EnhancementConfig
import com.example.model.LayoutArrangement
import com.example.model.NormalizedCrop
import com.example.model.PaperSize
import com.example.model.ShopDetails
import com.example.processing.DocumentAutoDetector
import com.example.processing.ImageEnhancementEngine
import com.example.processing.PdfProcessor
import com.example.processing.PrintHelperUtil
import com.example.processing.PrintLayoutGenerator
import com.example.processing.SampleDocumentProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppScreen {
    HOME,
    CROP_EDITOR,
    ENHANCE,
    PRINT_LAYOUT
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val activePreset: CardPreset = CardPreset.AADHAAR,
    val sourceBitmap: Bitmap? = null,
    val pdfPages: List<Bitmap> = emptyList(),
    val currentPdfPageIndex: Int = 0,
    val frontSide: CardSideData = CardSideData(),
    val backSide: CardSideData = CardSideData(),
    val selectedSideForEditing: CardSide = CardSide.FRONT,
    val paperSize: PaperSize = PaperSize.A4,
    val layoutArrangement: LayoutArrangement = LayoutArrangement.SIDE_BY_SIDE_FRONT_BACK,
    val cutMarkStyle: CutMarkStyle = CutMarkStyle.HAIRLINE_TICKS,
    val shopDetails: ShopDetails = ShopDetails(shopName = "Mohammad PVC Card Studio"),
    val isProcessing: Boolean = false,
    val statusMessage: String? = null,
    val printSheetBitmap: Bitmap? = null,
    val isFlipped3D: Boolean = false,
    val isDarkMode: Boolean = true
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Pre-populate with realistic Sample Aadhaar to enable instant 1-click test drive
        loadSampleDocument(CardPreset.AADHAAR)
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
        if (screen == AppScreen.PRINT_LAYOUT) {
            generatePrintSheet()
        }
    }

    fun selectPreset(preset: CardPreset) {
        val frontConfig = EnhancementConfig.fromPreset(preset)
        val backConfig = EnhancementConfig.fromPreset(preset)
        _uiState.update { state ->
            state.copy(
                activePreset = preset,
                frontSide = state.frontSide.copy(enhancement = frontConfig),
                backSide = state.backSide.copy(enhancement = backConfig)
            )
        }
        reprocessCards()
    }

    fun loadSampleDocument(preset: CardPreset) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, statusMessage = "Loading Sample ${preset.title}...") }
            val sampleBitmap = withContext(Dispatchers.Default) {
                SampleDocumentProvider.createSampleDocument(preset)
            }
            initDocument(sampleBitmap, preset)
            _uiState.update { it.copy(isProcessing = false, statusMessage = null) }
        }
    }

    fun handleFileUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, statusMessage = "Reading document...") }
            try {
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val isPdf = mimeType.contains("pdf", ignoreCase = true) || uri.toString().endsWith(".pdf", ignoreCase = true)

                if (isPdf) {
                    val pdfResult = withContext(Dispatchers.IO) {
                        PdfProcessor.renderPdfPages(context, uri)
                    }
                    if (pdfResult.pages.isNotEmpty()) {
                        val firstPage = pdfResult.pages[0]
                        _uiState.update {
                            it.copy(
                                pdfPages = pdfResult.pages,
                                currentPdfPageIndex = 0
                            )
                        }
                        if (pdfResult.pages.size >= 2) {
                            initTwoPagePdf(pdfResult.pages[0], pdfResult.pages[1], _uiState.value.activePreset)
                        } else {
                            initDocument(firstPage, _uiState.value.activePreset)
                        }
                    } else {
                        _uiState.update { it.copy(statusMessage = "Could not render PDF.") }
                    }
                } else {
                    val bitmap = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                    if (bitmap != null) {
                        initDocument(bitmap, _uiState.value.activePreset)
                    } else {
                        _uiState.update { it.copy(statusMessage = "Unable to read image.") }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(statusMessage = "Error loading file: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    private fun initDocument(bitmap: Bitmap, preset: CardPreset) {
        val (frontCrop, backCrop) = DocumentAutoDetector.autoDetectCrops(bitmap)
        val defaultEnhancement = EnhancementConfig.fromPreset(preset)

        _uiState.update {
            it.copy(
                sourceBitmap = bitmap,
                frontSide = CardSideData(
                    rawBitmap = bitmap,
                    crop = frontCrop,
                    enhancement = defaultEnhancement,
                    isAutoDetected = true
                ),
                backSide = CardSideData(
                    rawBitmap = bitmap,
                    crop = backCrop,
                    enhancement = defaultEnhancement,
                    isAutoDetected = true
                ),
                activePreset = preset
            )
        }
        reprocessCards()
    }

    private fun initTwoPagePdf(page1: Bitmap, page2: Bitmap, preset: CardPreset) {
        val defaultCrop = NormalizedCrop(left = 0.05f, top = 0.05f, right = 0.95f, bottom = 0.95f)
        val defaultEnhancement = EnhancementConfig.fromPreset(preset)

        _uiState.update {
            it.copy(
                sourceBitmap = page1,
                frontSide = CardSideData(
                    rawBitmap = page1,
                    crop = defaultCrop,
                    enhancement = defaultEnhancement,
                    isAutoDetected = true
                ),
                backSide = CardSideData(
                    rawBitmap = page2,
                    crop = defaultCrop,
                    enhancement = defaultEnhancement,
                    isAutoDetected = true
                ),
                activePreset = preset
            )
        }
        reprocessCards()
    }

    fun setSelectedEditingSide(side: CardSide) {
        _uiState.update { it.copy(selectedSideForEditing = side) }
    }

    fun updateCrop(side: CardSide, crop: NormalizedCrop) {
        _uiState.update { state ->
            val updated = if (side == CardSide.FRONT) {
                state.frontSide.copy(crop = crop)
            } else {
                state.backSide.copy(crop = crop)
            }
            if (side == CardSide.FRONT) state.copy(frontSide = updated) else state.copy(backSide = updated)
        }
        reprocessCards()
    }

    fun rotateCrop(side: CardSide) {
        val curCrop = if (side == CardSide.FRONT) _uiState.value.frontSide.crop else _uiState.value.backSide.crop
        val newRotation = (curCrop.rotationDegrees + 90) % 360
        updateCrop(side, curCrop.copy(rotationDegrees = newRotation))
    }

    fun applyAutoSplit(layout: DocumentAutoDetector.SplitLayout) {
        val (front, back) = DocumentAutoDetector.applySplit(layout)
        _uiState.update { state ->
            state.copy(
                frontSide = state.frontSide.copy(crop = front),
                backSide = state.backSide.copy(crop = back)
            )
        }
        reprocessCards()
    }

    fun swapFrontAndBack() {
        _uiState.update { state ->
            val tempFront = state.frontSide
            state.copy(
                frontSide = state.backSide,
                backSide = tempFront
            )
        }
        reprocessCards()
    }

    fun toggle3DFlip() {
        _uiState.update { it.copy(isFlipped3D = !it.isFlipped3D) }
    }

    fun updateEnhancement(side: CardSide, update: (EnhancementConfig) -> EnhancementConfig) {
        // Enforce Aadhaar-only color modifications
        if (!_uiState.value.activePreset.isAadhaarColorBoostAllowed) {
            return
        }

        _uiState.update { state ->
            val curSideData = if (side == CardSide.FRONT) state.frontSide else state.backSide
            val newConfig = update(curSideData.enhancement)
            val updatedSideData = curSideData.copy(enhancement = newConfig)
            if (side == CardSide.FRONT) {
                state.copy(frontSide = updatedSideData)
            } else {
                state.copy(backSide = updatedSideData)
            }
        }
        reprocessCards()
    }

    fun toggleAadhaarColorBoost(side: CardSide) {
        if (!_uiState.value.activePreset.isAadhaarColorBoostAllowed) return
        updateEnhancement(side) { it.copy(isAadhaarColorBoost = !it.isAadhaarColorBoost) }
    }

    fun setPaperSize(size: PaperSize) {
        _uiState.update { it.copy(paperSize = size) }
        generatePrintSheet()
    }

    fun setLayoutArrangement(arrangement: LayoutArrangement) {
        _uiState.update { it.copy(layoutArrangement = arrangement) }
        generatePrintSheet()
    }

    fun setCutMarkStyle(style: CutMarkStyle) {
        _uiState.update { it.copy(cutMarkStyle = style) }
        generatePrintSheet()
    }

    fun updateShopDetails(shopName: String, contactNumber: String) {
        _uiState.update {
            it.copy(
                shopDetails = it.shopDetails.copy(
                    shopName = shopName,
                    contactNumber = contactNumber
                )
            )
        }
        generatePrintSheet()
    }

    fun reprocessCards() {
        viewModelScope.launch {
            val frontData = _uiState.value.frontSide
            val backData = _uiState.value.backSide

            val processedFront = withContext(Dispatchers.Default) {
                frontData.rawBitmap?.let {
                    ImageEnhancementEngine.processCardSide(it, frontData.crop, frontData.enhancement)
                }
            }
            val processedBack = withContext(Dispatchers.Default) {
                backData.rawBitmap?.let {
                    ImageEnhancementEngine.processCardSide(it, backData.crop, backData.enhancement)
                }
            }

            _uiState.update {
                it.copy(
                    frontSide = it.frontSide.copy(processedBitmap = processedFront),
                    backSide = it.backSide.copy(processedBitmap = processedBack)
                )
            }
            generatePrintSheet()
        }
    }

    fun generatePrintSheet() {
        viewModelScope.launch {
            val frontBm = _uiState.value.frontSide.processedBitmap ?: return@launch
            val backBm = _uiState.value.backSide.processedBitmap ?: return@launch
            val paper = _uiState.value.paperSize
            val layout = _uiState.value.layoutArrangement
            val cut = _uiState.value.cutMarkStyle
            val shop = _uiState.value.shopDetails

            val sheet = withContext(Dispatchers.Default) {
                PrintLayoutGenerator.generatePrintCanvas(
                    frontCard = frontBm,
                    backCard = backBm,
                    paperSize = paper,
                    layout = layout,
                    cutMarkStyle = cut,
                    shopDetails = shop
                )
            }
            _uiState.update { it.copy(printSheetBitmap = sheet.canvasBitmap) }
        }
    }

    fun triggerPrint(context: Context) {
        val sheet = _uiState.value.printSheetBitmap ?: return
        val paper = _uiState.value.paperSize
        PrintHelperUtil.printDocument(context, sheet, paper, "Mohammad_PVC_${_uiState.value.activePreset.name}")
    }

    fun exportPdf(context: Context, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, statusMessage = "Generating 300 DPI PDF...") }
            try {
                val sheet = _uiState.value.printSheetBitmap ?: return@launch
                val paper = _uiState.value.paperSize
                val file = withContext(Dispatchers.IO) {
                    PrintHelperUtil.exportToPdf(context, sheet, paper, "Mohammad_PVC_Layout_300DPI.pdf")
                }
                PrintHelperUtil.shareFile(context, file, "application/pdf", "Mohammad PVC Card 300 DPI PDF")
                onComplete("PDF Exported: ${file.name}")
            } catch (e: Exception) {
                onComplete("Failed to export PDF: ${e.message}")
            } finally {
                _uiState.update { it.copy(isProcessing = false, statusMessage = null) }
            }
        }
    }

    fun exportImage(context: Context, isPng: Boolean, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, statusMessage = "Exporting 300 DPI Image...") }
            try {
                val sheet = _uiState.value.printSheetBitmap ?: return@launch
                val file = withContext(Dispatchers.IO) {
                    PrintHelperUtil.exportToImage(
                        context,
                        sheet,
                        isPng,
                        "Mohammad_PVC_${System.currentTimeMillis()}.${if (isPng) "png" else "jpg"}"
                    )
                }
                PrintHelperUtil.shareFile(
                    context,
                    file,
                    if (isPng) "image/png" else "image/jpeg",
                    "Mohammad PVC Card 300 DPI Print"
                )
                onComplete("Exported ${if (isPng) "PNG" else "JPG"}: ${file.name}")
            } catch (e: Exception) {
                onComplete("Failed to export image: ${e.message}")
            } finally {
                _uiState.update { it.copy(isProcessing = false, statusMessage = null) }
            }
        }
    }
}

package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.CyberTopBar
import com.example.ui.screens.CropEditorScreen
import com.example.ui.screens.EnhanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrintLayoutScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()

            MyApplicationTheme(darkTheme = uiState.isDarkMode) {
                Scaffold(
                    topBar = {
                        CyberTopBar(
                            currentScreen = uiState.currentScreen,
                            isDarkMode = uiState.isDarkMode,
                            onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("main_cyber_top_bar")
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentScreen) {
                            AppScreen.HOME -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onSelectPreset = { viewModel.selectPreset(it) },
                                    onFileSelected = { uri -> viewModel.handleFileUri(context, uri) },
                                    onLoadSample = { viewModel.loadSampleDocument(it) },
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onToggleFlip = { viewModel.toggle3DFlip() },
                                    onSwapSides = { viewModel.swapFrontAndBack() },
                                    onDirectPrint = { viewModel.triggerPrint(context) }
                                )
                            }
                            AppScreen.CROP_EDITOR -> {
                                CropEditorScreen(
                                    uiState = uiState,
                                    onCropChange = { side, crop -> viewModel.updateCrop(side, crop) },
                                    onRotate = { viewModel.rotateCrop(it) },
                                    onAutoSplit = { viewModel.applyAutoSplit(it) },
                                    onSwapSides = { viewModel.swapFrontAndBack() },
                                    onSideSelected = { viewModel.setSelectedEditingSide(it) },
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                            AppScreen.ENHANCE -> {
                                EnhanceScreen(
                                    uiState = uiState,
                                    onSideSelected = { viewModel.setSelectedEditingSide(it) },
                                    onToggleAadhaarColorBoost = { viewModel.toggleAadhaarColorBoost(it) },
                                    onUpdateEnhancement = { side, update -> viewModel.updateEnhancement(side, update) },
                                    onResetToPreset = { viewModel.selectPreset(uiState.activePreset) },
                                    onToggleFlip = { viewModel.toggle3DFlip() },
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                            AppScreen.PRINT_LAYOUT -> {
                                PrintLayoutScreen(
                                    uiState = uiState,
                                    onPaperSizeSelected = { viewModel.setPaperSize(it) },
                                    onLayoutSelected = { viewModel.setLayoutArrangement(it) },
                                    onCutMarkSelected = { viewModel.setCutMarkStyle(it) },
                                    onUpdateShopDetails = { name, phone -> viewModel.updateShopDetails(name, phone) },
                                    onPrintClick = { viewModel.triggerPrint(context) },
                                    onExportPdfClick = {
                                        viewModel.exportPdf(context) { msg ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(msg)
                                            }
                                        }
                                    },
                                    onExportImageClick = { isPng ->
                                        viewModel.exportImage(context, isPng) { msg ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(msg)
                                            }
                                        }
                                    },
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        }

                        // Status / Loading Indicator Overlay
                        AnimatedVisibility(
                            visible = uiState.isProcessing,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium,
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .testTag("processing_indicator_banner")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = uiState.statusMessage ?: "Processing High-Res...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

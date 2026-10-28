package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CardSide
import com.example.model.NormalizedCrop
import com.example.processing.DocumentAutoDetector
import com.example.ui.AppScreen
import com.example.ui.UiState
import com.example.ui.components.DualCropCanvas

@Composable
fun CropEditorScreen(
    uiState: UiState,
    onCropChange: (CardSide, NormalizedCrop) -> Unit,
    onRotate: (CardSide) -> Unit,
    onAutoSplit: (DocumentAutoDetector.SplitLayout) -> Unit,
    onSwapSides: () -> Unit,
    onSideSelected: (CardSide) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.HOME)
    }

    val selectedSide = uiState.selectedSideForEditing
    val currentBitmap = uiState.sourceBitmap
    var lockAspect by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Dual Front / Back Selection Tabs
        TabRow(
            selectedTabIndex = if (selectedSide == CardSide.FRONT) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSide == CardSide.FRONT,
                onClick = { onSideSelected(CardSide.FRONT) },
                text = {
                    Text(
                        "FRONT SIDE (BLUE BOX)",
                        fontWeight = if (selectedSide == CardSide.FRONT) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("crop_tab_front")
            )
            Tab(
                selected = selectedSide == CardSide.BACK,
                onClick = { onSideSelected(CardSide.BACK) },
                text = {
                    Text(
                        "BACK SIDE (TEAL BOX)",
                        fontWeight = if (selectedSide == CardSide.BACK) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("crop_tab_back")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Split Action Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Auto",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )

            FilterChip(
                selected = false,
                onClick = { onAutoSplit(DocumentAutoDetector.SplitLayout.E_AADHAAR_BOTTOM) },
                label = { Text("e-Aadhaar Bottom", fontSize = 12.sp) },
                modifier = Modifier.testTag("split_chip_eaadhaar")
            )

            FilterChip(
                selected = false,
                onClick = { onAutoSplit(DocumentAutoDetector.SplitLayout.HORIZONTAL_SPLIT) },
                label = { Text("Side-by-Side", fontSize = 12.sp) },
                modifier = Modifier.testTag("split_chip_horizontal")
            )

            FilterChip(
                selected = false,
                onClick = { onAutoSplit(DocumentAutoDetector.SplitLayout.VERTICAL_SPLIT) },
                label = { Text("Vertical Stack", fontSize = 12.sp) },
                modifier = Modifier.testTag("split_chip_vertical")
            )

            FilterChip(
                selected = false,
                onClick = { onAutoSplit(DocumentAutoDetector.SplitLayout.FULL_SINGLE_CARD) },
                label = { Text("Single Card", fontSize = 12.sp) },
                modifier = Modifier.testTag("split_chip_single")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dual-Box Interactive Crop Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (currentBitmap != null) {
                DualCropCanvas(
                    sourceBitmap = currentBitmap,
                    frontCrop = uiState.frontSide.crop,
                    backCrop = uiState.backSide.crop,
                    activeSide = selectedSide,
                    onCropChange = { side, newCrop ->
                        val finalCrop = if (lockAspect) {
                            DocumentAutoDetector.constrainToCr80(newCrop, currentBitmap.width, currentBitmap.height)
                        } else {
                            newCrop
                        }
                        onCropChange(side, finalCrop)
                    },
                    onSelectSide = onSideSelected,
                    lockCr80Aspect = lockAspect
                )
            } else {
                Text(
                    text = "No document loaded. Please upload a file.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Controls Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                    onClick = { onRotate(selectedSide) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("crop_rotate_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "Rotate 90",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rotate", fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = onSwapSides,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("crop_swap_sides_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Swap", fontSize = 12.sp)
                }

                IconButton(
                    onClick = { lockAspect = !lockAspect },
                    modifier = Modifier.testTag("crop_lock_aspect_button")
                ) {
                    Icon(
                        imageVector = if (lockAspect) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock Ratio",
                        tint = if (lockAspect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Next button: If Aadhaar, go to Enhance; if other card, can go directly to Print Layout!
            Button(
                onClick = {
                    if (uiState.activePreset.isAadhaarColorBoostAllowed) {
                        onNavigate(AppScreen.ENHANCE)
                    } else {
                        onNavigate(AppScreen.PRINT_LAYOUT)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("crop_done_button")
            ) {
                Text(if (uiState.activePreset.isAadhaarColorBoostAllowed) "HD Enhance" else "Print Layout")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

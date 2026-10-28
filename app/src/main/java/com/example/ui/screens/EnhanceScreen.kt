package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CardPreset
import com.example.model.CardSide
import com.example.model.EnhancementConfig
import com.example.ui.AppScreen
import com.example.ui.UiState
import com.example.ui.components.PvcCardPreview

@Composable
fun EnhanceScreen(
    uiState: UiState,
    onSideSelected: (CardSide) -> Unit,
    onToggleAadhaarColorBoost: (CardSide) -> Unit,
    onUpdateEnhancement: (CardSide, (EnhancementConfig) -> EnhancementConfig) -> Unit,
    onResetToPreset: () -> Unit,
    onToggleFlip: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.HOME)
    }

    val selectedSide = uiState.selectedSideForEditing
    val currentSideData = if (selectedSide == CardSide.FRONT) uiState.frontSide else uiState.backSide
    val config = currentSideData.enhancement
    val isAadhaar = uiState.activePreset == CardPreset.AADHAAR
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Tab Row to switch between Front and Back card tuning
        TabRow(
            selectedTabIndex = if (selectedSide == CardSide.FRONT) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedSide == CardSide.FRONT,
                onClick = { onSideSelected(CardSide.FRONT) },
                text = {
                    Text(
                        "FRONT SIDE",
                        fontWeight = if (selectedSide == CardSide.FRONT) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("enhance_tab_front")
            )
            Tab(
                selected = selectedSide == CardSide.BACK,
                onClick = { onSideSelected(CardSide.BACK) },
                text = {
                    Text(
                        "BACK SIDE",
                        fontWeight = if (selectedSide == CardSide.BACK) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("enhance_tab_back")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real-Time 3D Card Preview
        PvcCardPreview(
            frontBitmap = uiState.frontSide.processedBitmap,
            backBitmap = uiState.backSide.processedBitmap,
            isFlipped = uiState.isFlipped3D,
            onFlipClick = onToggleFlip
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isAadhaar) {
            // EXCLUSIVE AADHAAR ORIGINAL VIBRANT COLOR ENGINE HERO CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (config.isAadhaarColorBoost)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (config.isAadhaarColorBoost)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Original HD Color Boost",
                                tint = if (config.isAadhaarColorBoost) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Original HD Color Boost",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Aadhaar Only",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Restores authentic tricolor flag, deep black text & crisp photo",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = config.isAadhaarColorBoost,
                        onCheckedChange = { onToggleAadhaarColorBoost(selectedSide) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("aadhaar_color_boost_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fine Tuning Sliders Card for Aadhaar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AADHAAR HD COLOR & TEXT TUNING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Text Sharpening (Unsharp Masking)
                    SliderRow(
                        icon = Icons.Default.Details,
                        label = "Deep Black Text Sharpening",
                        valueStr = "${(config.sharpness * 100).toInt()}%",
                        value = config.sharpness,
                        valueRange = 0.0f..1.0f,
                        onValueChange = { newVal ->
                            onUpdateEnhancement(selectedSide) { it.copy(sharpness = newVal) }
                        },
                        testTag = "slider_sharpness"
                    )

                    // 2. Tricolor Saturation
                    SliderRow(
                        icon = Icons.Default.ColorLens,
                        label = "Tricolor & Emblem Vibrance",
                        valueStr = "${(config.saturation * 100).toInt()}%",
                        value = config.saturation,
                        valueRange = 1.0f..2.0f,
                        onValueChange = { newVal ->
                            onUpdateEnhancement(selectedSide) { it.copy(saturation = newVal) }
                        },
                        testTag = "slider_saturation"
                    )

                    // 3. Contrast
                    SliderRow(
                        icon = Icons.Default.Contrast,
                        label = "Demographic Text Contrast",
                        valueStr = "${String.format("%.2f", config.contrast)}x",
                        value = config.contrast,
                        valueRange = 0.9f..1.5f,
                        onValueChange = { newVal ->
                            onUpdateEnhancement(selectedSide) { it.copy(contrast = newVal) }
                        },
                        testTag = "slider_contrast"
                    )

                    // 4. Brightness
                    SliderRow(
                        icon = Icons.Default.Brightness6,
                        label = "Background Brightness",
                        valueStr = "${config.brightness.toInt()}",
                        value = config.brightness,
                        valueRange = -30f..30f,
                        onValueChange = { newVal ->
                            onUpdateEnhancement(selectedSide) { it.copy(brightness = newVal) }
                        },
                        testTag = "slider_brightness"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onResetToPreset,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Aadhaar HD Preset", fontSize = 12.sp)
                    }
                }
            }
        } else {
            // FOR NON-AADHAAR CARDS (PAN, VOTER, RATION): 1:1 ORIGINAL SCANNED COLOR LOCK
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Original Color",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1:1 Original Scanned Colors Locked",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "For ${uiState.activePreset.title}, color modifications are strictly prohibited. The system maintains exact 1:1 lossless scanned tones with standard CR80 300 DPI layout formatting.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lossless CR80 Scaling: Active (1011 x 638 px @ 300 DPI)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Proceed to Print Layout Button
        Button(
            onClick = { onNavigate(AppScreen.PRINT_LAYOUT) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("enhance_proceed_print_button")
        ) {
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = "Print",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Proceed to Print Layout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SliderRow(
    icon: ImageVector,
    label: String,
    valueStr: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    testTag: String
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = valueStr,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CutMarkStyle
import com.example.model.LayoutArrangement
import com.example.model.PaperSize
import com.example.ui.AppScreen
import com.example.ui.UiState

@Composable
fun PrintLayoutScreen(
    uiState: UiState,
    onPaperSizeSelected: (PaperSize) -> Unit,
    onLayoutSelected: (LayoutArrangement) -> Unit,
    onCutMarkSelected: (CutMarkStyle) -> Unit,
    onUpdateShopDetails: (String, String) -> Unit,
    onPrintClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    onExportImageClick: (Boolean) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.HOME)
    }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showShopDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Paper Canvas Preview Card
        Text(
            text = "300 DPI PRINT SHEET PREVIEW (${uiState.paperSize.displayName})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val paperAspect = uiState.paperSize.widthPx.toFloat() / uiState.paperSize.heightPx.toFloat()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(paperAspect.coerceIn(0.6f, 1.4f))
                .shadow(8.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val printBm = uiState.printSheetBitmap
            if (printBm != null) {
                Image(
                    bitmap = printBm.asImageBitmap(),
                    contentDescription = "Print Canvas Sheet",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Configuration Card: Paper Size, Layout Arrangement, and Cut Marks
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // 1. Paper Size
                Text(
                    text = "PAPER SIZE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaperSize.values().forEach { size ->
                        val isSelected = uiState.paperSize == size
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPaperSizeSelected(size) },
                            label = { Text(size.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("paper_chip_${size.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Card Placement / Layout
                Text(
                    text = "CARD ARRANGEMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LayoutArrangement.values().forEach { layout ->
                        val isSelected = uiState.layoutArrangement == layout
                        FilterChip(
                            selected = isSelected,
                            onClick = { onLayoutSelected(layout) },
                            label = { Text(layout.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier.testTag("layout_chip_${layout.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Cut Mark Guides
                Text(
                    text = "CUT MARKS & GUIDELINES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CutMarkStyle.values().forEach { style ->
                        val isSelected = uiState.cutMarkStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCutMarkSelected(style) },
                            label = { Text(style.displayName, fontSize = 12.sp) },
                            modifier = Modifier.testTag("cut_chip_${style.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Shop Details Customizer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Shop",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Cyber Cafe Margin Stamp",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${uiState.shopDetails.shopName} ${if (uiState.shopDetails.contactNumber.isNotBlank()) "• Ph: " + uiState.shopDetails.contactNumber else ""}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showShopDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("edit_shop_button")
                    ) {
                        Text("Edit", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Direct Print Button (Calls Android PrintManager)
        Button(
            onClick = onPrintClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("direct_print_button")
        ) {
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = "Print",
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Direct Print via Android PrintManager", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Save / Export Row (PDF, PNG, JPG)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onExportPdfClick,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save PDF", fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = { onExportImageClick(true) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_png_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "PNG",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save PNG", fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = { onExportImageClick(false) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_jpg_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "JPG",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save JPG", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Shop Details Dialog
    if (showShopDialog) {
        var shopName by remember { mutableStateOf(uiState.shopDetails.shopName) }
        var phone by remember { mutableStateOf(uiState.shopDetails.contactNumber) }

        AlertDialog(
            onDismissRequest = { showShopDialog = false },
            title = { Text("Cyber Cafe Stamp Details") },
            text = {
                Column {
                    Text(
                        text = "This will appear in the subtle margin footer on the printed page.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop / Studio Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone / WhatsApp") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_phone_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateShopDetails(shopName, phone)
                        showShopDialog = false
                    },
                    modifier = Modifier.testTag("save_shop_details_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShopDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

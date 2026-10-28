package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CardSide
import com.example.model.NormalizedCrop
import kotlin.math.hypot

@Composable
fun DualCropCanvas(
    sourceBitmap: Bitmap,
    frontCrop: NormalizedCrop,
    backCrop: NormalizedCrop,
    activeSide: CardSide,
    onCropChange: (CardSide, NormalizedCrop) -> Unit,
    onSelectSide: (CardSide) -> Unit,
    lockCr80Aspect: Boolean = true,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        val imgWidth = sourceBitmap.width.toFloat()
        val imgHeight = sourceBitmap.height.toFloat()
        val imgAspect = imgWidth / imgHeight
        val containerAspect = containerWidth / containerHeight

        val displayWidth: Float
        val displayHeight: Float

        if (containerAspect > imgAspect) {
            displayHeight = containerHeight
            displayWidth = containerHeight * imgAspect
        } else {
            displayWidth = containerWidth
            displayHeight = containerWidth / imgAspect
        }

        val offsetX = (containerWidth - displayWidth) / 2f
        val offsetY = (containerHeight - displayHeight) / 2f

        val activeCrop = if (activeSide == CardSide.FRONT) frontCrop else backCrop
        val inactiveCrop = if (activeSide == CardSide.FRONT) backCrop else frontCrop

        // 0 = none, 1 = TL, 2 = TR, 3 = BL, 4 = BR, 5 = Center Move
        var activeDragHandle by remember { mutableStateOf(0) }
        val handleRadiusPx = 28f

        Image(
            bitmap = sourceBitmap.asImageBitmap(),
            contentDescription = "Document to crop",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("crop_canvas_overlay")
                .pointerInput(frontCrop, backCrop, activeSide, displayWidth, displayHeight) {
                    detectTapGestures { tapPos ->
                        val tapX = tapPos.x
                        val tapY = tapPos.y

                        // Check if tapped on inactive box
                        val inL = offsetX + (inactiveCrop.left * displayWidth)
                        val inT = offsetY + (inactiveCrop.top * displayHeight)
                        val inR = offsetX + (inactiveCrop.right * displayWidth)
                        val inB = offsetY + (inactiveCrop.bottom * displayHeight)

                        if (tapX in inL..inR && tapY in inT..inB) {
                            onSelectSide(if (activeSide == CardSide.FRONT) CardSide.BACK else CardSide.FRONT)
                        }
                    }
                }
                .pointerInput(activeCrop, displayWidth, displayHeight) {
                    detectDragGestures(
                        onDragStart = { startPos ->
                            val startX = startPos.x
                            val startY = startPos.y

                            val boxL = offsetX + (activeCrop.left * displayWidth)
                            val boxT = offsetY + (activeCrop.top * displayHeight)
                            val boxR = offsetX + (activeCrop.right * displayWidth)
                            val boxB = offsetY + (activeCrop.bottom * displayHeight)

                            fun isNear(hx: Float, hy: Float): Boolean =
                                hypot((startX - hx).toDouble(), (startY - hy).toDouble()) <= handleRadiusPx * 2.2f

                            activeDragHandle = when {
                                isNear(boxL, boxT) -> 1 // TL
                                isNear(boxR, boxT) -> 2 // TR
                                isNear(boxL, boxB) -> 3 // BL
                                isNear(boxR, boxB) -> 4 // BR
                                startX in boxL..boxR && startY in boxT..boxB -> 5 // Inside box
                                else -> 0
                            }
                        },
                        onDragEnd = { activeDragHandle = 0 },
                        onDragCancel = { activeDragHandle = 0 },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (activeDragHandle == 0 || displayWidth <= 0 || displayHeight <= 0) return@detectDragGestures

                            val dxNorm = dragAmount.x / displayWidth
                            val dyNorm = dragAmount.y / displayHeight

                            var newLeft = activeCrop.left
                            var newTop = activeCrop.top
                            var newRight = activeCrop.right
                            var newBottom = activeCrop.bottom

                            when (activeDragHandle) {
                                1 -> { // Top-Left
                                    newLeft = (activeCrop.left + dxNorm).coerceIn(0f, activeCrop.right - 0.05f)
                                    newTop = (activeCrop.top + dyNorm).coerceIn(0f, activeCrop.bottom - 0.05f)
                                }
                                2 -> { // Top-Right
                                    newRight = (activeCrop.right + dxNorm).coerceIn(activeCrop.left + 0.05f, 1f)
                                    newTop = (activeCrop.top + dyNorm).coerceIn(0f, activeCrop.bottom - 0.05f)
                                }
                                3 -> { // Bottom-Left
                                    newLeft = (activeCrop.left + dxNorm).coerceIn(0f, activeCrop.right - 0.05f)
                                    newBottom = (activeCrop.bottom + dyNorm).coerceIn(activeCrop.top + 0.05f, 1f)
                                }
                                4 -> { // Bottom-Right
                                    newRight = (activeCrop.right + dxNorm).coerceIn(activeCrop.left + 0.05f, 1f)
                                    newBottom = (activeCrop.bottom + dyNorm).coerceIn(activeCrop.top + 0.05f, 1f)
                                }
                                5 -> { // Move Center
                                    val w = activeCrop.right - activeCrop.left
                                    val h = activeCrop.bottom - activeCrop.top
                                    newLeft = (activeCrop.left + dxNorm).coerceIn(0f, 1f - w)
                                    newRight = newLeft + w
                                    newTop = (activeCrop.top + dyNorm).coerceIn(0f, 1f - h)
                                    newBottom = newTop + h
                                }
                            }

                            val updated = activeCrop.copy(
                                left = newLeft,
                                top = newTop,
                                right = newRight,
                                bottom = newBottom
                            )
                            onCropChange(activeSide, updated)
                        }
                    )
                }
        ) {
            // Draw Inactive Box first (dashed border)
            val inL = offsetX + (inactiveCrop.left * displayWidth)
            val inT = offsetY + (inactiveCrop.top * displayHeight)
            val inR = offsetX + (inactiveCrop.right * displayWidth)
            val inB = offsetY + (inactiveCrop.bottom * displayHeight)
            val inW = (inR - inL).coerceAtLeast(10f)
            val inH = (inB - inT).coerceAtLeast(10f)

            drawRect(
                color = secondaryColor.copy(alpha = 0.7f),
                topLeft = Offset(inL, inT),
                size = Size(inW, inH),
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw Active Box
            val boxL = offsetX + (activeCrop.left * displayWidth)
            val boxT = offsetY + (activeCrop.top * displayHeight)
            val boxR = offsetX + (activeCrop.right * displayWidth)
            val boxB = offsetY + (activeCrop.bottom * displayHeight)

            val boxW = (boxR - boxL).coerceAtLeast(10f)
            val boxH = (boxB - boxT).coerceAtLeast(10f)

            // Dim outer area
            val dimColor = Color.Black.copy(alpha = 0.45f)
            drawRect(dimColor, topLeft = Offset(0f, 0f), size = Size(size.width, boxT))
            drawRect(dimColor, topLeft = Offset(0f, boxB), size = Size(size.width, size.height - boxB))
            drawRect(dimColor, topLeft = Offset(0f, boxT), size = Size(boxL, boxH))
            drawRect(dimColor, topLeft = Offset(boxR, boxT), size = Size(size.width - boxR, boxH))

            // Active box border
            drawRect(
                color = primaryColor,
                topLeft = Offset(boxL, boxT),
                size = Size(boxW, boxH),
                style = Stroke(width = 3.dp.toPx())
            )

            // Rule of thirds
            val gridColor = Color.White.copy(alpha = 0.35f)
            val x1 = boxL + (boxW / 3f)
            val x2 = boxL + (boxW * 2f / 3f)
            val y1 = boxT + (boxH / 3f)
            val y2 = boxT + (boxH * 2f / 3f)

            drawLine(gridColor, Offset(x1, boxT), Offset(x1, boxB), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(x2, boxT), Offset(x2, boxB), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(boxL, y1), Offset(boxR, y1), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(boxL, y2), Offset(boxR, y2), strokeWidth = 1.dp.toPx())

            // 4 Corner Handles
            val handlePoints = listOf(
                Offset(boxL, boxT),
                Offset(boxR, boxT),
                Offset(boxL, boxB),
                Offset(boxR, boxB)
            )

            handlePoints.forEach { pt ->
                drawCircle(color = Color.White, radius = handleRadiusPx, center = pt)
                drawCircle(color = primaryColor, radius = handleRadiusPx - 4f, center = pt)
            }

            // Center indicator
            val centerPt = Offset(boxL + boxW / 2f, boxT + boxH / 2f)
            drawCircle(color = primaryColor.copy(alpha = 0.85f), radius = 10.dp.toPx(), center = centerPt)
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = centerPt)
        }

        // Card Info Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            val sideName = if (activeSide == CardSide.FRONT) "FRONT SIDE" else "BACK SIDE"
            val ratio = if (activeCrop.height > 0) activeCrop.width / activeCrop.height else 0f
            Text(
                text = "Editing $sideName • Tap other box to switch • Ratio: ${String.format("%.2f", ratio)}",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

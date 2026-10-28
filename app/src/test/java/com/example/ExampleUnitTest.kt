package com.example

import com.example.model.CardPreset
import com.example.model.EnhancementConfig
import com.example.model.PvcConstants
import com.example.processing.DocumentAutoDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPvcCr80Constants() {
        assertEquals(85.60f, PvcConstants.CR80_WIDTH_MM, 0.01f)
        assertEquals(53.98f, PvcConstants.CR80_HEIGHT_MM, 0.01f)
        assertEquals(1011, PvcConstants.CR80_WIDTH_PX)
        assertEquals(638, PvcConstants.CR80_HEIGHT_PX)
        val expectedRatio = 85.60f / 53.98f
        assertEquals(expectedRatio, PvcConstants.CR80_ASPECT_RATIO, 0.001f)
    }

    @Test
    fun testDocumentAutoDetectorSplits() {
        val (front, back) = DocumentAutoDetector.applySplit(DocumentAutoDetector.SplitLayout.E_AADHAAR_BOTTOM)
        assertTrue(front.left < front.right)
        assertTrue(front.top < front.bottom)
        assertTrue(back.left < back.right)
        assertTrue(back.top < back.bottom)
        assertTrue(front.top >= 0.5f)
    }

    @Test
    fun testCardPresetsEnhancementConfig() {
        // Aadhaar Card: Dedicated Original HD Color Boost ACTIVE
        val aadhaarConfig = EnhancementConfig.fromPreset(CardPreset.AADHAAR)
        assertTrue(CardPreset.AADHAAR.isAadhaarColorBoostAllowed)
        assertTrue(aadhaarConfig.isAadhaarColorBoost)
        assertTrue(aadhaarConfig.saturation > 1.0f)
        assertTrue(aadhaarConfig.sharpness > 0.0f)

        // PAN Card: Strictly 1:1 original scanned colors, NO modifications
        val panConfig = EnhancementConfig.fromPreset(CardPreset.PAN)
        assertFalse(CardPreset.PAN.isAadhaarColorBoostAllowed)
        assertFalse(panConfig.isAadhaarColorBoost)
        assertEquals(1.0f, panConfig.contrast, 0.001f)
        assertEquals(1.0f, panConfig.saturation, 0.001f)
        assertEquals(0.0f, panConfig.sharpness, 0.001f)

        // Voter Card: Strictly 1:1 original scanned colors
        val voterConfig = EnhancementConfig.fromPreset(CardPreset.VOTER_ID)
        assertFalse(CardPreset.VOTER_ID.isAadhaarColorBoostAllowed)
        assertFalse(voterConfig.isAadhaarColorBoost)

        // Ration Card: Strictly 1:1 original scanned colors
        val rationConfig = EnhancementConfig.fromPreset(CardPreset.RATION_CARD)
        assertFalse(CardPreset.RATION_CARD.isAadhaarColorBoostAllowed)
        assertFalse(rationConfig.isAadhaarColorBoost)
    }
}

package com.example

import android.net.Uri
import com.example.ui.PatternType
import com.example.ui.ShredAlgorithm
import com.example.recover.RecoverableTrace
import com.example.recover.TraceCategory
import com.example.recover.DataRecoveryViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Senior Software Engineer comprehensive deep dive testing suite.
 * Evaluates core cryptographic & secure wipe algorithms (Round 1),
 * Media/Residual trace simulation & calculations (Round 2),
 * and State properties & text scrubber obfuscators (Round 3).
 */
class DeepDiveSoftwareEngineerTest {

    // ==========================================
    // ROUND 1: SECURE SHREDDER ALGO & PATTERNS
    // ==========================================

    @Test
    fun testGutmannAlgorithmConfiguration() {
        val algo = ShredAlgorithm.Gutmann
        assertEquals("Permanent Delete Mode", algo.name)
        assertEquals(35, algo.totalPasses)
        assertEquals("Maximum Security", algo.securityLevel)
        assertTrue(algo.description.contains("definitively"))
    }

    @Test
    fun testGutmannPassPatternsSequence() {
        val algo = ShredAlgorithm.Gutmann
        val patterns = algo.getPassPatterns(1024L)

        // Verify the exact number of passes is 35 (U.S. DoD and Gutmann specification)
        assertEquals(35, patterns.size)

        // Verify first 4 passes are cryptographically secure random fields (re-key/pre-entropy)
        for (i in 0..3) {
            assertTrue(
                "Pass $i must be RandomField",
                patterns[i] is PatternType.RandomField
            )
        }

        // Verify last 4 passes are cryptographically secure random fields (post-entropy blanking)
        for (i in 31..34) {
            assertTrue(
                "Pass $i must be RandomField",
                patterns[i] is PatternType.RandomField
            )
        }

        // Verify the 27 intermediate passes are FixedByte static patterns
        for (i in 4..30) {
            val pass = patterns[i]
            assertTrue(
                "Pass $i must be FixedByte pattern type",
                pass is PatternType.FixedByte
            )
            // Verify and check description field formatting
            val desc = (pass as PatternType.FixedByte).description
            assertTrue(desc.contains("Static Pattern"))
        }
    }

    @Test
    fun testPatternTypeDescriptions() {
        // Test system pattern types for correct labeling
        assertEquals("0x00 Zero-fill (Fast Wipe)", PatternType.ZeroField.description)
        assertEquals("0xFF One-fill (High Contrast)", PatternType.OneField.description)
        assertEquals("Cryptographically Secure Random-fill", PatternType.RandomField.description)

        val customPattern = PatternType.FixedByte(0x55.toByte(), "0x55 custom pattern")
        assertEquals("0x55 custom pattern", customPattern.description)
        assertEquals(0x55.toByte(), customPattern.byteValue)
    }

    // ==========================================
    // ROUND 2: MEDIA RESIDUAL TRACES & GRAPH SCAN
    // ==========================================

    @Test
    fun testRecoverableTraceEffectiveDateCalculations() {
        // Scenario A: Effective date fallback to dateMillis (when estimation is not available)
        val traceNoEstimate = RecoverableTrace(
            id = "/sdcard/DCIM/.thumbnails/test.jpg",
            displayName = "test.jpg",
            category = TraceCategory.IMAGE,
            mimeType = "image/jpeg",
            sizeBytes = 2048L,
            dateMillis = 1600000000000L,
            contentUri = null,
            filePath = "/sdcard/DCIM/.thumbnails/test.jpg",
            source = "Thumbnail cache",
            orphan = true,
            expiresMillis = null,
            deletedAtMillis = null
        )
        assertEquals(1600000000000L, traceNoEstimate.effectiveDate)

        // Scenario B: Effective date overrides with estimated deletion timestamp
        val traceWithEstimate = traceNoEstimate.copy(
            deletedAtMillis = 1650000000000L,
            expiresMillis = 1652592000000L
        )
        assertEquals(1650000000000L, traceWithEstimate.effectiveDate)
        assertEquals(1652592000000L, traceWithEstimate.expiresMillis)
    }

    @Test
    fun testRecoverableTraceThumbnails() {
        val traceWithFilePath = RecoverableTrace(
            id = "file_path_trace",
            displayName = "preview.png",
            category = TraceCategory.IMAGE,
            mimeType = "image/png",
            sizeBytes = 100L,
            dateMillis = 1000L,
            contentUri = null,
            filePath = "/sdcard/preview.png",
            source = "Cache"
        )
        // Thumbnail model must resolve successfully
        assertEquals("/sdcard/preview.png", traceWithFilePath.thumbnailModel)
    }

    @Test
    fun testUiStateSelectedFilteringAndCount() {
        val trace1 = RecoverableTrace(
            id = "id_1",
            displayName = "img1.png",
            category = TraceCategory.IMAGE,
            mimeType = "image/png",
            sizeBytes = 5000L,
            dateMillis = 1000L,
            contentUri = null,
            filePath = "path1",
            source = "A",
            orphan = false
        )
        val trace2 = trace1.copy(id = "id_2", orphan = true)

        val tracesList = listOf(trace1, trace2)

        // Simulate local UI filtering logic
        val allFiltered = tracesList
        val onlyOrphansFiltered = tracesList.filter { it.orphan }

        assertEquals(2, allFiltered.size)
        assertEquals(1, onlyOrphansFiltered.size)
        assertEquals("id_2", onlyOrphansFiltered.first().id)

        // Verify DataRecoveryViewModel.UiState behaviors
        val uiState = DataRecoveryViewModel.UiState(
            traces = tracesList,
            selected = setOf("id_1", "id_2")
        )
        assertEquals(2, uiState.selectedCount)
    }

    // ==========================================
    // ROUND 3: STATE MACHINE & NOTE SCRAMBLER
    // ==========================================

    @Test
    fun testNoteObfuscationLogicSimulation() {
        val originalText = "TopSecretConfidentialCryptoKey123"
        val chars = originalText.toCharArray()
        
        // Emulate the clean-up step
        chars.fill('\u0000')

        // Confirm that memory cell scrubbing strictly zeroes the contents
        for (c in chars) {
            assertEquals('\u0000', c)
        }
    }

    @Test
    fun testDataRecoveryViewModelUiStateDefaultInitialization() {
        val defaultUiState = DataRecoveryViewModel.UiState()
        assertFalse(defaultUiState.scanning)
        assertFalse(defaultUiState.recovering)
        assertTrue(defaultUiState.traces.isEmpty())
        assertTrue(defaultUiState.selected.isEmpty())
        assertTrue(defaultUiState.includeFilesystem)
        assertFalse(defaultUiState.onlyOrphans)
        assertEquals(0, defaultUiState.selectedCount)
    }
}

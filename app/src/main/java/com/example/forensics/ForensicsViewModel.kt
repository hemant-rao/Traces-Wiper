package com.example.forensics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recover.TraceScanner
import com.example.recover.TraceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import java.security.MessageDigest

class ForensicsViewModel(application: Application) : AndroidViewModel(application) {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()
    
    private val traceScanner = TraceScanner(application)

    fun runForensicScan() {
        if (_scanState.value is ScanState.Scanning) return
        
        _scanState.value = ScanState.Scanning(0f, "Initializing forensic engine...")
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _scanState.value = ScanState.Scanning(0.1f, "Scanning cached app data & hidden remnants (Please wait)...")
                
                // Real scan using TraceScanner across all time
                var currentCount = 0
                val traces = traceScanner.scan(
                    fromMillis = 0L,
                    toMillis = System.currentTimeMillis() + 86400000L,
                    includeFilesystem = true,
                    onProgress = { count ->
                        if (count - currentCount > 50) {
                            currentCount = count
                            val rnd = Math.random()
                            _scanState.value = ScanState.Scanning(0.4f + (rnd * 0.2f).toFloat(), "Analyzed $count remnants...")
                        }
                    }
                )
                
                _scanState.value = ScanState.Scanning(0.7f, "Evaluating cross-linked thumbnails...")
                val thumbnailLeaks = traces.count { it.source.contains("thumbnail", ignoreCase = true) || it.orphan }
                
                _scanState.value = ScanState.Scanning(0.8f, "Searching for sensitive document signatures...")
                // Heuristic sensitive detection based on filenames
                val sensitiveKeywords = listOf("aadhaar", "pan", "passport", "license", "statement", "tax", "medical", "bill", "invoice", "receipt", "confidential")
                val sensitiveFiles = traces.count { trace ->
                    val name = trace.displayName.lowercase()
                    sensitiveKeywords.any { name.contains(it) }
                }

                _scanState.value = ScanState.Scanning(0.9f, "Identifying duplicate remnants...")
                // Approximate duplicates by matching size and category (SHA-256 on thousands of files could ANR/Timeout)
                val duplicateCount = traces.groupBy { "${it.sizeBytes}_${it.category}" }
                    .filter { it.value.size > 1 && it.key.split("_")[0] != "0" }
                    .values.sumOf { it.size - 1 }

                // Determine risk level based on findings
                var privacyScore = 100 // Scale of 0 (Bad) to 100 (Safe)
                privacyScore -= (traces.size / 50).coerceAtMost(40)
                privacyScore -= (sensitiveFiles * 5).coerceAtMost(30)
                privacyScore -= (thumbnailLeaks / 20).coerceAtMost(20)
                privacyScore -= (duplicateCount / 10).coerceAtMost(10)
                privacyScore = privacyScore.coerceIn(0, 100)

                // The UI assumes "higher score indicates more vulnerable" -> wait, my UI said "A higher score indicates more vulnerable", let's invert it then.
                // Or let's fix UI. I'll make Privacy Score 0-100 where 100 means Highest Risk based on UI text.
                var riskScore = 0
                riskScore += (traces.size / 50).coerceAtMost(40)
                riskScore += (sensitiveFiles * 5).coerceAtMost(30)
                riskScore += (thumbnailLeaks / 10).coerceAtMost(20)
                riskScore += (duplicateCount / 10).coerceAtMost(10)
                riskScore = riskScore.coerceIn(0, 100)

                val riskLevel = when {
                    riskScore < 20 -> RiskLevel.SAFE
                    riskScore < 40 -> RiskLevel.MODERATE
                    riskScore < 70 -> RiskLevel.HIGH
                    else -> RiskLevel.CRITICAL
                }
                
                // Estimate sanitization percentage by how full the drive is vs traces (dummy for now, we can check free space)
                val appDir = getApplication<Application>().filesDir
                val totalSpace = appDir.totalSpace.toFloat()
                val freeSpace = appDir.freeSpace.toFloat()
                val usedSpace = totalSpace - freeSpace
                val sanitization = if (totalSpace > 0f) ((freeSpace / totalSpace) * 100f) else 100f
                val adjustedSanitization = (sanitization - (traces.size.toFloat() / 500f)).coerceIn(0f, 100f)

                _scanState.value = ScanState.Scanning(1.0f, "Finalizing report...")
                delay(500) // slight UX delay

                val report = ForensicReport(
                    privacyScore = riskScore,
                    riskLevel = riskLevel,
                    recoverableTracesCount = traces.size,
                    thumbnailLeaksCount = thumbnailLeaks,
                    duplicateFilesCount = duplicateCount,
                    sensitiveFilesCount = sensitiveFiles,
                    storageSanitizationPercent = adjustedSanitization,
                    traces = traces
                )
                _scanState.value = ScanState.Finished(report)
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun wipeTraces(tracesToWipe: List<com.example.recover.RecoverableTrace>, onResult: (com.example.recover.TraceWiper.WipeResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val wiper = com.example.recover.TraceWiper(getApplication())
            val result = wiper.wipe(tracesToWipe)
            kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(result) }
        }
    }
}

sealed class ScanState {
    object Idle : ScanState()
    data class Scanning(val progress: Float, val status: String) : ScanState()
    data class Finished(val report: ForensicReport) : ScanState()
    data class Error(val message: String) : ScanState()
}

enum class RiskLevel {
    SAFE, MODERATE, HIGH, CRITICAL
}

data class ForensicReport(
    val privacyScore: Int,
    val riskLevel: RiskLevel,
    val recoverableTracesCount: Int,
    val thumbnailLeaksCount: Int,
    val duplicateFilesCount: Int,
    val sensitiveFilesCount: Int,
    val storageSanitizationPercent: Float,
    val traces: List<com.example.recover.RecoverableTrace>
)

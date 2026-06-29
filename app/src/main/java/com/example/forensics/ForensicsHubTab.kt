package com.example.forensics

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ShredderViewModel
import com.example.ui.OdioBookFamilySection
import com.example.ui.theme.*
import com.example.SwipeToExecuteButton

@Composable
fun ForensicsHubTab(
    viewModel: ShredderViewModel,
    isSystemBusy: Boolean,
    forensicsViewModel: ForensicsViewModel = viewModel()
) {
    val scanState by forensicsViewModel.scanState.collectAsState()
    val context = LocalContext.current
    var isClearing by remember { mutableStateOf(false) }
    var showTracesFilter by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    val showTracesUI = (scanState is ScanState.Finished) && showTracesFilter != null
    
    androidx.activity.compose.BackHandler(enabled = showTracesUI) {
        showTracesFilter = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (showTracesUI) Modifier else Modifier.verticalScroll(rememberScrollState()))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        
        // Header
        if (!showTracesUI) {
            Text(
                text = "PRIVACY FORENSIC SCANNER",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            
            Text(
                text = "Identify all traces of user data that may still exist on the device and estimate their recoverability, privacy impact, and forensic value.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        when (val state = scanState) {
            is ScanState.Idle -> {
                ForensicIntroCard()
                Spacer(modifier = Modifier.height(16.dp))
                SwipeToExecuteButton(
                    enabled = !isSystemBusy,
                    onExecuted = { forensicsViewModel.runForensicScan() },
                    initialText = "SLIDE TO START FORENSIC SCAN",
                    executedText = "INITIALIZING...",
                    buttonColor = NeonGreen
                )

                // §777 — "The OdioBook Family" cross-promotion section (shared
                // component across every family app). Replaces the older one-line
                // §776 attribution with a richer discovery surface: the OdioBook
                // logo, every sibling app, and clickable odiobook.com links.
                Spacer(modifier = Modifier.height(28.dp))
                OdioBookFamilySection(
                    currentAppTitle = "Dig Deep",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                // §778 — config-driven OdioBook banner (admin-controlled; no-op until
                // ads are enabled for "digdeep"). Bottom of the idle/home surface.
                com.example.ads.OdioBookAds.Banner(modifier = Modifier.padding(vertical = 8.dp))
            }
            is ScanState.Scanning -> {
                ForensicScanningCard(state)
            }
            is ScanState.Finished -> {
                if (showTracesFilter != null) {
                    val filteredTraces = when(showTracesFilter) {
                        "THUMBNAIL" -> state.report.traces.filter { it.source.contains("thumbnail", ignoreCase=true) || it.orphan }
                        "SENSITIVE" -> {
                            val kws = listOf("aadhaar", "pan", "passport", "license", "statement", "tax", "medical", "bill", "invoice", "receipt", "confidential")
                            state.report.traces.filter { t -> kws.any { t.displayName.lowercase().contains(it) } }
                        }
                        else -> state.report.traces
                    }
                    val filteredReport = state.report.copy(traces = filteredTraces)
                    
                    if (isClearing) {
                        CircularProgressIndicator(color = LaserRed)
                        Text("Securely wiping traces...", color = LaserRed, fontFamily = FontFamily.Monospace)
                    } else {
                        ForensicTracesView(
                            report = filteredReport,
                            isSystemBusy = isSystemBusy,
                            modifier = Modifier.weight(1f),
                            onBack = { showTracesFilter = null },
                            onClearTraces = { traces ->
                                isClearing = true
                                forensicsViewModel.wipeTraces(traces) { result ->
                                    isClearing = false
                                    if (result.needsUserConsent != null) {
                                        Toast.makeText(context, "Cannot auto-delete: Needs user consent via system prompt.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Wiped ${result.deleted} traces securely.", Toast.LENGTH_SHORT).show()
                                        // §778 — full-screen ad at a natural "task done" break (paced + capped 5/day).
                                        (context as? android.app.Activity)?.let { com.example.ads.OdioBookAds.maybeShowInterstitial(it) }
                                        showTracesFilter = null
                                        forensicsViewModel.runForensicScan()
                                    }
                                }
                            }
                        )
                    }
                } else {
                    ForensicDashboard(
                        report = state.report,
                        isSystemBusy = isSystemBusy,
                        onShowTraces = { filter -> showTracesFilter = filter }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SwipeToExecuteButton(
                        enabled = !isSystemBusy,
                        onExecuted = { forensicsViewModel.runForensicScan() },
                        initialText = "SLIDE TO RESCAN",
                        executedText = "INITIALIZING...",
                        buttonColor = SlateBorder,
                        textColor = Color.White
                    )
                }
            }
            is ScanState.Error -> {
                Text("Error: ${state.message}", color = LaserRed)
                SwipeToExecuteButton(
                    enabled = !isSystemBusy,
                    onExecuted = { forensicsViewModel.runForensicScan() },
                    initialText = "RETRY SCAN",
                    executedText = "INITIALIZING...",
                    buttonColor = LaserRed
                )
            }
        }
    }
}

@Composable
fun ForensicIntroCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CharcoalSurface, RoundedCornerShape(12.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("SCAN TARGETS", color = TerminalCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        
        ForensicTargetItem(Icons.Default.Search, "Recoverable Traces", "Analyze trash, cache, and lost directories.")
        ForensicTargetItem(Icons.Default.Warning, "Thumbnail Leaks", "Find previews of deleted underlying files.")
        ForensicTargetItem(Icons.Default.List, "Duplicate Waste", "Identify duplicated photos and document traces.")
        ForensicTargetItem(Icons.Default.Lock, "Sensitive Signatures", "Detect IDs, PAN, passbooks, and financial forms.")
    }
}

@Composable
fun ForensicTargetItem(icon: ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun ForensicScanningCard(state: ScanState.Scanning) {
    val animatedProgress by animateFloatAsState(targetValue = state.progress, animationSpec = tween(500))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CharcoalSurface, RoundedCornerShape(12.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            progress = animatedProgress,
            color = NeonGreen,
            strokeWidth = 4.dp,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "FORENSIC ANALYSIS IN PROGRESS",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = state.status,
            color = NeonGreen,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ForensicTracesView(report: ForensicReport, isSystemBusy: Boolean, modifier: Modifier = Modifier, onBack: () -> Unit, onClearTraces: (List<com.example.recover.RecoverableTrace>) -> Unit) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        com.example.CyberConfirmDialog(
            title = "WIPE TRACES",
            message = "You are about to securely wipe traces. This will permanently delete these files from your device. This action cannot be undone.",
            confirmText = "ACKNOWLEDGE AND WIPE",
            cancelText = "CANCEL",
            requireConfirmationWord = "WIPE",
            onConfirm = {
                onClearTraces(report.traces.take(100))
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("FOUND TRACES (${report.traces.size})", color = TextPrimary, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = !isSystemBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("WIPE TRACES", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Button(
                    onClick = onBack,
                    enabled = !isSystemBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateBorder),
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("BACK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
        
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(report.traces.take(100)) { trace ->
                Row(
                    modifier = Modifier.fillMaxWidth().background(CharcoalSurface, RoundedCornerShape(8.dp)).border(1.dp, SlateBorder, RoundedCornerShape(8.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = NeonGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(trace.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text("Category: ${trace.category} • Size: ${trace.sizeBytes / 1024} KB", color = TextSecondary, fontSize = 11.sp)
                        Text("Source: ${trace.source}", color = NeonGreen.copy(alpha=0.7f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ForensicDashboard(report: ForensicReport, isSystemBusy: Boolean, onShowTraces: (String) -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Privacy Score Card
        val (riskColor, riskText) = when (report.riskLevel) {
            RiskLevel.SAFE -> NeonGreen to "SAFE"
            RiskLevel.MODERATE -> Color(0xFFFBBF24) to "MODERATE RISK"
            RiskLevel.HIGH -> Color(0xFFF97316) to "HIGH RISK"
            RiskLevel.CRITICAL -> LaserRed to "CRITICAL RISK"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalSurface, RoundedCornerShape(12.dp))
                .border(2.dp, riskColor, RoundedCornerShape(12.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PRIVACY SCORE", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = "${report.privacyScore}/100",
                    color = riskColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = riskText,
                    color = riskColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A higher score indicates more vulnerable, recoverable traces exist on the device.",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        // Stats Grid
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ForensicStatBox(
                modifier = Modifier.weight(1f),
                title = "RECOVERABLE\nTRACES",
                value = report.recoverableTracesCount.toString(),
                color = if (report.recoverableTracesCount > 100) LaserRed else NeonGreen,
                onClick = { onShowTraces("ALL") }
            )
            ForensicStatBox(
                modifier = Modifier.weight(1f),
                title = "THUMBNAIL\nLEAKS",
                value = report.thumbnailLeaksCount.toString(),
                color = if (report.thumbnailLeaksCount > 50) Color(0xFFFBBF24) else NeonGreen,
                onClick = { onShowTraces("THUMBNAIL") }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ForensicStatBox(
                modifier = Modifier.weight(1f),
                title = "SENSITIVE\nSIGS",
                value = report.sensitiveFilesCount.toString(),
                color = if (report.sensitiveFilesCount > 0) LaserRed else NeonGreen,
                onClick = { onShowTraces("SENSITIVE") }
            )
            ForensicStatBox(
                modifier = Modifier.weight(1f),
                title = "STORAGE\nSANITIZATION",
                value = "${report.storageSanitizationPercent.toInt()}%",
                color = if (report.storageSanitizationPercent < 30f) LaserRed else NeonGreen,
                onClick = { onShowTraces("ALL") }
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onShowTraces("ALL") },
                enabled = !isSystemBusy,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.List, contentDescription = "View Traces", tint = CarbonDarkBg, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("VIEW TRACES", color = CarbonDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            
            Button(
                onClick = {
                    val jsonReport = """
                        {
                            "privacyScore": ${report.privacyScore},
                            "riskLevel": "${report.riskLevel}",
                            "recoverableTracesCount": ${report.recoverableTracesCount},
                            "thumbnailLeaksCount": ${report.thumbnailLeaksCount},
                            "sensitiveFilesCount": ${report.sensitiveFilesCount},
                            "duplicateFilesCount": ${report.duplicateFilesCount},
                            "storageSanitizationPercent": ${report.storageSanitizationPercent}
                        }
                    """.trimIndent()
                    
                    try {
                        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                        if (!downloadsDir.exists()) downloadsDir.mkdirs()
                        val file = java.io.File(downloadsDir, "Forensic_Report_${System.currentTimeMillis()}.json")
                        file.writeText(jsonReport)
                        Toast.makeText(context, "Exported to Downloads folder", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to export: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                enabled = !isSystemBusy,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SlateBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export Report", tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("EXPORT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun ForensicStatBox(modifier: Modifier = Modifier, title: String, value: String, color: Color, onClick: (() -> Unit)? = null) {
    Column(
        modifier = modifier
            .background(CharcoalSurface, RoundedCornerShape(12.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

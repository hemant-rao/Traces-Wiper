package com.example

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SelectedFileInfo
import com.example.ui.ShredAlgorithm
import com.example.ui.ShredProgressState
import com.example.ui.ShredderViewModel
import com.example.ui.theme.CarbonDarkBg
import com.example.ui.theme.ThemeState
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.LaserRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    ShredderAppScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ShredderAppScreen(
    modifier: Modifier = Modifier,
    viewModel: ShredderViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    
    // Exactly 4 structured tabs with high visual scanability icons
    val tabs = listOf(
        CyberTabItem("Files", Icons.Default.Delete, "Secure file wiping"),
        CyberTabItem("Text", Icons.Default.Lock, "Clean text scrubbing"),
        CyberTabItem("Deep Wipe", Icons.Default.Refresh, "Sanitize storage blocks"),
        CyberTabItem("History", Icons.Default.History, "Local session database")
    )

    val selectedFiles by viewModel.selectedFiles.collectAsStateWithLifecycle()
    val currentAlgo by viewModel.selectedAlgorithm.collectAsStateWithLifecycle()
    val progressState by viewModel.progressState.collectAsStateWithLifecycle()
    val historyLog by viewModel.historyState.collectAsStateWithLifecycle()
    
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val isDeepWiping by viewModel.isDeepWiping.collectAsStateWithLifecycle()
    val isShreddingNote by viewModel.isShreddingNote.collectAsStateWithLifecycle()

    val sweepProg by viewModel.sweepProgress.collectAsStateWithLifecycle()
    val sweepTitle by viewModel.sweepTitle.collectAsStateWithLifecycle()
    val sweepLogs by viewModel.sweepLogs.collectAsStateWithLifecycle()
    val scrambledText by viewModel.scrambledNotesText.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            viewModel.addFiles(uris)
            Toast.makeText(context, "${uris.size} sensitive file(s) loaded", Toast.LENGTH_SHORT).show()
        }
    }

    // Main layout with Box overlay container
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(CarbonDarkBg)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
        // Pristine, premium Brand Top Bar for localized security
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Shield Guard",
                    tint = NeonGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CYBERWIPE",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (0.5).sp
                    )
                    Text(
                        text = "100% OFFLINE • USER PRIVACY SECURED",
                        color = TerminalCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (0.3).sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Compact Theme Switcher (Elegant custom Box avoiding default M3 oversized minimum touch targets)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (ThemeState.isDarkTheme) Color(0xFF131A2A) else Color(0xFFE2E8F0),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            SlateBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { ThemeState.isDarkTheme = !ThemeState.isDarkTheme },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (ThemeState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (ThemeState.isDarkTheme) Color(0xFFFBBF24) else Color(0xFF475569),
                        modifier = Modifier.size(15.dp)
                    )
                }

                 // Micro Active badge (Sleek modern status pill)
                 Box(
                     modifier = Modifier
                         .background(NeonGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                         .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                         .padding(horizontal = 7.dp, vertical = 3.dp)
                 ) {
                     Text(
                         text = "SECURE LOCAL",
                         color = NeonGreen,
                         fontSize = 8.sp,
                         fontWeight = FontWeight.Bold,
                         fontFamily = FontFamily.SansSerif
                     )
                 }
            }
        }

        // Modern low-profile dashboard stats block
        SecureHeader(
            historyCount = historyLog.size,
            totalBytesShredded = historyLog.sumOf { it.originalSize },
            securityScore = if (historyLog.isEmpty()) "0%" else if (historyLog.any { it.passes >= 3 }) "A++ SECURE" else "BASIC"
        )

        Spacer(modifier = Modifier.height(6.dp))

        // State-of-the-art capsule segmented navigation control bar (Dynamic Modern SaaS styling)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(CharcoalSurface, RoundedCornerShape(12.dp))
                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, tabItem ->
                val isSelected = selectedTab == index
                val tabBackground = if (isSelected) NeonGreen.copy(alpha = 0.12f) else Color.Transparent
                val tabTextAndIconColor = if (isSelected) NeonGreen else TextSecondary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tabBackground)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp)
                        .testTag("tab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tabItem.icon,
                            contentDescription = tabItem.label,
                            tint = tabTextAndIconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tabItem.label,
                            color = tabTextAndIconColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Content Frame
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> FileShredderTab(
                    selectedFiles = selectedFiles,
                    currentAlgo = currentAlgo,
                    progressState = progressState,
                    onPickFiles = { filePickerLauncher.launch(arrayOf("*/*")) },
                    onRemoveFile = { viewModel.removeFile(it) },
                    onAlgoSelected = { viewModel.updateSelectedAlgorithm(it) },
                    onStartShred = { viewModel.startShredding() },
                    viewModel = viewModel
                )
                1 -> TextWiperTab(viewModel = viewModel)
                2 -> DeepWipeTab(viewModel = viewModel)
                3 -> HistoryAndGuideTab(
                    historyLog = historyLog,
                    onClearAll = { viewModel.clearHistory() },
                    viewModel = viewModel
                )
            }
        }
    }

    // Full-Page Overlay Cyber-Security Process Loader Overlay
    if (progressState.isShredding || isScanning || isDeepWiping || isShreddingNote) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE6030507)) // gorgeous extra-dark semi-transparent layer
                .clickable(enabled = true, onClick = {}) // absorbs touches to prevent double-clicks under overlay
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            val themeAccentColor = if (progressState.isShredding || isDeepWiping) LaserRed else NeonGreen
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(2.dp, themeAccentColor.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = themeAccentColor,
                            strokeWidth = 3.5.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        Icon(
                            imageVector = if (progressState.isShredding || isDeepWiping) Icons.Default.Warning else Icons.Default.Lock,
                            contentDescription = null,
                            tint = themeAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    val overlayTitleText = when {
                        progressState.isShredding -> "SECURE CLUSTER SHREDDING ENGAGED"
                        isDeepWiping -> "DEEP SPACE OVERWRITE ACTIVE"
                        isScanning -> "CYBER SECTOR STORAGE SCAN"
                        isShreddingNote -> "MEMETIC MEMORY SCRUBBER RUNNING"
                        else -> "SYSTEM ENCRYPTION PROTOCOL ACTIVE"
                    }

                    Text(
                        text = overlayTitleText,
                        color = themeAccentColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    if (progressState.isShredding) {
                        Text(
                            text = "Shredding: File ${progressState.currentFileIndex} of ${progressState.totalFilesCount}\n" +
                                   "Target node: ${progressState.currentFileName}",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PASS ${progressState.currentPass}/${progressState.totalPasses}",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${progressState.passProgress.toInt()}%",
                                    color = themeAccentColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressState.passProgress / 100f },
                                color = themeAccentColor,
                                trackColor = SlateBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    } else if (isDeepWiping) {
                        Text(
                            text = sweepTitle.uppercase(),
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FREE SPACE BLOCK PURGE",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${(sweepProg * 100).toInt()}%",
                                    color = themeAccentColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { sweepProg },
                                color = themeAccentColor,
                                trackColor = SlateBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    } else if (isScanning) {
                        Text(
                            text = "Inspecting directory tables and file trees for matching filter ranges...",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )
                    } else if (isShreddingNote) {
                        Text(
                            text = "Permuting text entropy array values before garbage collection release...",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )
                        
                        if (scrambledText.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .background(CarbonDarkBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = scrambledText,
                                    color = NeonGreen,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // System log box
                    if (progressState.isShredding || isDeepWiping) {
                        val logs = if (progressState.isShredding) progressState.logs else sweepLogs

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONSOLE TRACE STREAM",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .background(themeAccentColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, themeAccentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    color = themeAccentColor,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF030507)),
                            border = BorderStroke(1.dp, SlateBorder)
                        ) {
                            val lazyListState = rememberLazyListState()
                            LaunchedEffect(logs.size) {
                                if (logs.isNotEmpty()) {
                                    lazyListState.animateScrollToItem(logs.size - 1)
                                }
                            }

                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(logs) { log ->
                                    Text(
                                        text = log,
                                        color = when {
                                            log.startsWith("✅") || log.contains("SUCCESS") || log.contains("Success") -> NeonGreen
                                            log.startsWith("☠️") || log.contains("Target") || log.contains("Starting") -> ElectricAmber
                                            log.startsWith("⚠️") -> LaserRed
                                            else -> TextPrimary.copy(alpha = 0.8f)
                                        },
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp
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
}

data class CyberTabItem(
    val label: String,
    val icon: ImageVector,
    val description: String
)

@Composable
fun HistoryAndGuideTab(
    historyLog: List<com.example.data.ShredHistory>,
    onClearAll: () -> Unit,
    viewModel: ShredderViewModel
) {
    var insideTabSelection by remember { mutableStateOf(0) } // 0 = Shred Logs, 1 = Military Standards
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // State-of-the-art capsule segmented navigation control bar (Dynamic Modern SaaS styling)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalSurface, RoundedCornerShape(12.dp))
                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Shred Records", "Security Standards").forEachIndexed { index, title ->
                val isSelected = insideTabSelection == index
                val tabBackground = if (isSelected) NeonGreen.copy(alpha = 0.12f) else Color.Transparent
                val tabTextColor = if (isSelected) NeonGreen else TextSecondary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tabBackground)
                        .clickable { insideTabSelection = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = tabTextColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (insideTabSelection == 0) {
                ShredHistoryTab(historyLog, onClearAll, viewModel)
            } else {
                AlgorithmsTab()
            }
        }
    }
}

@Composable
fun SecureHeader(
    historyCount: Int,
    totalBytesShredded: Long,
    securityScore: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardStat(
                label = "SHREDDED",
                value = "$historyCount Assets",
                accentColor = TerminalCyan,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(SlateBorder)
            )
            DashboardStat(
                label = "TOTAL SECURE WIPED",
                value = formatBytes(totalBytesShredded),
                accentColor = ElectricAmber,
                modifier = Modifier.weight(1.2f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(22.dp)
                    .background(SlateBorder)
            )
            DashboardStat(
                label = "FORENSIC GRADE",
                value = securityScore,
                accentColor = NeonGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DashboardStat(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.widthIn(max = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FileShredderTab(
    selectedFiles: List<SelectedFileInfo>,
    currentAlgo: ShredAlgorithm,
    progressState: ShredProgressState,
    onPickFiles: () -> Unit,
    onRemoveFile: (SelectedFileInfo) -> Unit,
    onAlgoSelected: (ShredAlgorithm) -> Unit,
    onStartShred: () -> Unit,
    viewModel: ShredderViewModel
) {
    val lazyListState = rememberLazyListState()
    var showConfirmShredDialog by remember { mutableStateOf(false) }

    if (showConfirmShredDialog) {
        CyberConfirmDialog(
            title = "EXECUTE SECURE SHREDDING",
            message = "You are about to securely and permanently wipe ${selectedFiles.size} selected file(s). This operation utilizes the active algorithm (${currentAlgo.name} - ${currentAlgo.totalPasses} Passes) to completely overwrite storage partitions, making recovery mathematically impossible. \n\nAre you sure you want to proceed with permanent destruction?",
            confirmText = "CONFIRM AND SHRED",
            cancelText = "ABORT PROTOCOL",
            onConfirm = {
                showConfirmShredDialog = false
                onStartShred()
            },
            onDismiss = {
                showConfirmShredDialog = false
            }
        )
    }

    // Auto-scroll console logs to bottom
    LaunchedEffect(progressState.logs.size) {
        if (progressState.logs.isNotEmpty()) {
            lazyListState.animateScrollToItem(progressState.logs.size - 1)
        }
    }

    if (progressState.isShredding) {
        // Lock UI and show shredding terminal logging
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, LaserRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = LaserRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "SHREDDING PROTOCOL ACTIVE...",
                                color = LaserRed,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            "${progressState.currentFileIndex} / ${progressState.totalFilesCount}",
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = progressState.currentFileName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Security Swipe Active",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "Pass: ${progressState.currentPass}/${progressState.totalPasses}",
                            color = ElectricAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progressState.passProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = LaserRed,
                        trackColor = SlateBorder,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // Real-time Console Log Output Box (Gives complete validation)
            Text(
                "SHREDDING CONSOLE OUTPUT",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF040507)), // Terminal dark background
                border = BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(progressState.logs) { log ->
                        Text(
                            text = log,
                            color = when {
                                log.startsWith("ERROR") || log.startsWith("Crit") || log.contains("failed") -> LaserRed
                                log.startsWith("✅") || log.contains("Success") || log.contains("successfully") -> NeonGreen
                                log.startsWith("[Pass") || log.contains("PASS") -> ElectricAmber
                                else -> if (ThemeState.isDarkTheme) TextPrimary.copy(alpha = 0.85f) else Color(0xFFE2E8F0)
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    } else {
        // Setup View
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Explanatory warning note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LaserRed.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, LaserRed.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Absolute Warning",
                            tint = LaserRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "WARNING: Secure Shredding overwrites raw block partitions. Deleted materials are FORENSICALLY UNRECOVERABLE by any hardware or software methods. Use with absolute caution.",
                            color = LaserRed,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // File selection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "SENSITIVE ASSETS TO WIPE",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Button(
                                onClick = onPickFiles,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("select_files_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Load Files",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedFiles.isEmpty()) {
                            // Empty state guidance
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No sensitive files loaded. Tap 'LOAD FILES' to securely load files from local storage.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else {
                            // Display selected files list
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedFiles.forEach { fileInfo ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                CarbonDarkBg,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .border(1.dp, SlateBorder, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.0f)) {
                                            Text(
                                                fileInfo.name,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                viewModel.formatSize(fileInfo.size),
                                                color = TextSecondary,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        IconButton(
                                            onClick = { onRemoveFile(fileInfo) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove selection",
                                                tint = LaserRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "CLEAR ALL SELECTIONS",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .clickable { viewModel.clearSelections() }
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Attacker-Resistant Security Shield Status (Replaces raw algorithm selections)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security Active",
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SECURE SHIELD ACTIVE",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val securityFeatures = listOf(
                            "Memory Sanitizer" to "Zero-out RAM buffers immediately after shredding to prevent Heap dumps & memory hacking attacks.",
                            "Anti-Tamper Control" to "Monitors background threats during deletion sequences to enforce physical data quarantine.",
                            "Permanent Erasure" to "Raw partition-level overwrites to ensure no software or restore utilities can recover the deleted records."
                        )
                        
                        securityFeatures.forEach { (title, desc) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(6.dp)
                                        .background(NeonGreen, RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        title,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                    Text(
                                        desc,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }
                        }
                    }
                }
            }
             // Secure Shred Button Action with Dialog Confirmation
            item {
                Spacer(modifier = Modifier.height(8.dp))
                val pickerEnabled = selectedFiles.isNotEmpty() && !progressState.isShredding

                HoldToConfirmButton(
                    enabled = pickerEnabled,
                    onConfirmed = {
                        showConfirmShredDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("secure_shred_trigger")
                ) { progress ->
                    val buttonColor = if (pickerEnabled) LaserRed else SlateBorder
                    val textColor = if (pickerEnabled) TextPrimary else TextSecondary

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(
                                color = if (progress > 0f) LaserRed.copy(alpha = 0.25f * progress) else CharcoalSurface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (progress > 0f) LaserRed else buttonColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (progress > 0f && pickerEnabled) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(LaserRed.copy(alpha = 0.4f))
                                    .align(Alignment.CenterStart)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (progress > 0f) "HOLD PROTOCOL: ${(progress * 100).toInt()}%" else if (pickerEnabled) "HOLD TO INITIATE SHRED" else "LOAD ASSETS FOR SECURE DELETION",
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            if (pickerEnabled && progress == 0f) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap-holding permanently overwrites ${selectedFiles.size} loaded asset(s)",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TextWiperTab(viewModel: ShredderViewModel) {
    val notesInput by viewModel.notesInput.collectAsStateWithLifecycle()
    val isShreddingNote by viewModel.isShreddingNote.collectAsStateWithLifecycle()
    val scrambledText by viewModel.scrambledNotesText.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            border = BorderStroke(1.dp, SlateBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "SECURE MEMORY TEXT SCRUBBER",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Standard copy-pasted contents, raw passwords, or financial notes reside permanently in Android volatile heap memory. Type or paste sensitive details below. Wiping immediately scrambles buffer spaces, zeros out RAM arrays, and forces GC sanitization.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Input field
        OutlinedTextField(
            value = if (isShreddingNote) scrambledText else notesInput,
            onValueChange = {
                if (!isShreddingNote) {
                    viewModel.updateNotesInput(it)
                }
            },
            enabled = !isShreddingNote,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 320.dp)
                .testTag("secret_notes_input"),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = if (isShreddingNote) NeonGreen else TextPrimary
            ),
            placeholder = {
                Text(
                    "Enter highly sensitive data to shred from phone memory here (Private notes, API keys, passwords, transactions)...",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = SlateBorder,
                disabledBorderColor = LaserRed,
                focusedLabelColor = NeonGreen,
                cursorColor = NeonGreen
            ),
            shape = RoundedCornerShape(8.dp)
        )

        val buttonEnabled = notesInput.isNotEmpty() && !isShreddingNote
        HoldToConfirmButton(
            enabled = buttonEnabled,
            onConfirmed = {
                viewModel.shredNotes()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shred_notes_button")
        ) { progress ->
            val buttonColor = if (buttonEnabled) LaserRed else SlateBorder
            val textColor = if (buttonEnabled) TextPrimary else TextSecondary

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        color = if (progress > 0f) LaserRed.copy(alpha = 0.25f * progress) else CharcoalSurface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (progress > 0f) LaserRed else buttonColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (progress > 0f && buttonEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(LaserRed.copy(alpha = 0.4f))
                            .align(Alignment.CenterStart)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (progress > 0f) "SCRUBBING ENTROPY: ${(progress * 100).toInt()}%" else if (buttonEnabled) "HOLD TO SHRED & PURGE TEXT" else "ENTER SEED TO SECURE",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ShredHistoryTab(
    historyLog: List<com.example.data.ShredHistory>,
    onClearAll: () -> Unit,
    viewModel: ShredderViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "VERIFIED SCRUB RECORDS",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            if (historyLog.isNotEmpty()) {
                Text(
                    text = "CLEAR ALL LOGS",
                    color = LaserRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable { onClearAll() }
                        .padding(8.dp)
                        .testTag("clear_history_log_button")
                )
            }
        }

        if (historyLog.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No shred history records found.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    "Every asset you shred with DoD standard protocol outputs validation index logs here.",
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyLog) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CharcoalSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = log.fileName,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = viewModel.formatSize(log.originalSize),
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .background(NeonGreen.copy(alpha = 0.12f), RoundedCornerShape(2.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${log.algorithm} (${log.passes}P)",
                                        color = NeonGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val formattedDate = remember(log.timestamp) {
                                val date = java.util.Date(log.timestamp)
                                val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                                format.format(date)
                            }
                            Text(
                                text = formattedDate,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "VERIFIED INTACT",
                                color = TerminalCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlgorithmsTab() {
    val itemsGuide = listOf(
        GuideItem("How secure is File Shredding?", "Once deleted through this application, files and records are permanently and absolutely unrecoverable. Unlike normal deletion, which only removes file paths and leaves actual data on the system, secure shredding overwrites raw byte sectors with randomized high-entropy fields so no recovery utilities can ever resurrect your files."),
        GuideItem("Is a single rewrite pass sufficient to prevent recovery?", "Yes, for modern flash drives, a single-pass raw zero overwrite completely invalidates the storage cells, making file recovery impossible. For older memory modules, our secure engine overwrites the sectors multiple times with custom mathematical structures to ensure maximum security."),
        GuideItem("What are the internal secure standards used?", "Our secure shredding system uses industry-approved security procedures specifying strict multi-pass overwrite sequences. These protocols ensure storage cells are thoroughly sanitized so that they cannot be recovered by any software or physical utility."),
        GuideItem("Why is it impossible to recover after shredding?", "Our shredder erases metadata indexes as well as the actual binary contents. Since there is no physical or electronic registry of the file whatsoever, the deleted content is gone forever.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "UNDERSTANDING SECURE DATA WIPING",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(itemsGuide) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = item.title,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.contents,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

data class GuideItem(val title: String, val contents: String)

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

@Composable
fun HoldToConfirmButton(
    enabled: Boolean,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    holdDurationMs: Long = 1800L,
    content: @Composable (progress: Float) -> Unit
) {
    var isPressing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(isPressing) {
        if (isPressing) {
            val startTime = System.currentTimeMillis()
            while (isPressing && progress < 1f) {
                delay(16)
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed.toFloat() / holdDurationMs).coerceAtMost(1f)
            }
            if (progress >= 1f) {
                onConfirmed()
                isPressing = false
                progress = 0f
            }
        } else {
            while (progress > 0f) {
                delay(12)
                progress = (progress - 0.08f).coerceAtLeast(0f)
            }
        }
    }

    Box(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        isPressing = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressing = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content(progress)
    }
}

@Composable
fun CyberConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "CONFIRM AND EXECUTE",
    cancelText: String = "ABORT PROTOCOL",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        titleContentColor = LaserRed,
        textContentColor = TextPrimary,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = LaserRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title.uppercase(),
                    color = LaserRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Text(
                text = message,
                color = TextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 16.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = confirmText,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text(
                    text = cancelText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    )
}

@Composable
fun DeepWipeTab(viewModel: ShredderViewModel) {
    val context = LocalContext.current
    var subTab by remember { mutableStateOf(0) } // 0 Search & Destroy, 1 Deep Sector Wipe

    // Select suitable permissions depending on version of Android
    val permissionsToRequest = remember {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scannedFiles by viewModel.scannedFiles.collectAsStateWithLifecycle()
    val isDeepWiping by viewModel.isDeepWiping.collectAsStateWithLifecycle()
    val sweepProgress by viewModel.sweepProgress.collectAsStateWithLifecycle()
    val sweepLogs by viewModel.sweepLogs.collectAsStateWithLifecycle()
    val sweepTitle by viewModel.sweepTitle.collectAsStateWithLifecycle()
    val currentAlgo by viewModel.selectedAlgorithm.collectAsStateWithLifecycle()
    val progressState by viewModel.progressState.collectAsStateWithLifecycle()
    val isShreddingNote by viewModel.isShreddingNote.collectAsStateWithLifecycle()

    val isAnyProcessRunning = isScanning || isDeepWiping || progressState.isShredding || isShreddingNote

    val calendar = remember { java.util.Calendar.getInstance() }
    val currentYear = remember { calendar.get(java.util.Calendar.YEAR) }
    val currentMonth = remember { calendar.get(java.util.Calendar.MONTH) + 1 }
    val currentDay = remember { calendar.get(java.util.Calendar.DAY_OF_MONTH) }

    // Start date states initialized via Calendar for perfect underflow bounds safety matching the 7-day default preset
    val initialStartCal = remember {
        java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, -7)
        }
    }
    var startYear by remember { mutableStateOf(initialStartCal.get(java.util.Calendar.YEAR)) }
    var startMonth by remember { mutableStateOf(initialStartCal.get(java.util.Calendar.MONTH) + 1) }
    var startDay by remember { mutableStateOf(initialStartCal.get(java.util.Calendar.DAY_OF_MONTH)) }

    // End date states
    var endYear by remember { mutableStateOf(currentYear) }
    var endMonth by remember { mutableStateOf(currentMonth) }
    var endDay by remember { mutableStateOf(currentDay) }

    var selectedCategory by remember { mutableStateOf("ALL") }
    var activePreset by remember { mutableStateOf<Int?>(7) }

    fun updateDatesFromPreset(daysAgo: Int) {
        val cal = java.util.Calendar.getInstance()
        
        // Today is end date
        endYear = cal.get(java.util.Calendar.YEAR)
        endMonth = cal.get(java.util.Calendar.MONTH) + 1
        endDay = cal.get(java.util.Calendar.DAY_OF_MONTH)

        // Start date is x days ago
        cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
        startYear = cal.get(java.util.Calendar.YEAR)
        startMonth = cal.get(java.util.Calendar.MONTH) + 1
        startDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
        
        activePreset = daysAgo
    }

    val startMs = remember(startYear, startMonth, startDay) {
        getMillisForDate(startYear, startMonth, startDay, false)
    }
    val endMs = remember(endYear, endMonth, endDay) {
        getMillisForDate(endYear, endMonth, endDay, true)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Run scanning after requesting. Private internal sandboxed dirs can always be scanned even if denied
        viewModel.startSecureFileScan(startMs, endMs, selectedCategory)
    }

    // Background sweep processes are managed by the main full-screen progress overlay.
    // Main config view
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Sub tab selectors inside Deep Wipe cleaner (Dynamic Modern styling)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Search_Destroy", "Deep_Sector_Wipe").forEachIndexed { idx, label ->
                    val isSelected = subTab == idx
                    val bg = if (isSelected) NeonGreen.copy(alpha = 0.12f) else Color.Transparent
                    val tc = if (isSelected) NeonGreen else TextSecondary
                    val bc = if (isSelected) NeonGreen.copy(alpha = 0.25f) else Color.Transparent
                    val displayLabel = if (idx == 0) "Search & Destroy" else "Deep Sector Wipe"

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .border(1.dp, bc, RoundedCornerShape(10.dp))
                            .clickable(enabled = !isAnyProcessRunning) { subTab = idx }
                            .padding(vertical = 11.dp)
                            .testTag("deep_sub_tab_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayLabel,
                            color = tc,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }

        // Universal Date selector card (Premium Calendar picker box instead of +/- buttons)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "SET DISCOVERY DATE WINDOW",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 7, 30).forEach { days ->
                            val isSelected = activePreset == days
                            val bg = if (isSelected) NeonGreen else CarbonDarkBg
                            val bc = if (isSelected) NeonGreen else SlateBorder
                            val tc = if (isSelected) CarbonDarkBg else TerminalCyan
                            val displayLabel = when (days) {
                                0 -> "TODAY"
                                7 -> "7 DAYS"
                                30 -> "30 DAYS"
                                else -> "LAST $days DAYS"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bg)
                                    .border(1.dp, bc, RoundedCornerShape(6.dp))
                                    .clickable(enabled = !isAnyProcessRunning) { updateDatesFromPreset(days) }
                                    .padding(vertical = 8.dp)
                                    .testTag("preset_${days}_days"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayLabel,
                                    color = tc,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Calendar interactive selectors for start and end range
                    // START DATE SELECTION TUNER
                    Text(
                        "FROM: (Start Date)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CarbonDarkBg)
                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isAnyProcessRunning) {
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        startYear = year
                                        startMonth = month + 1
                                        startDay = day
                                        activePreset = null
                                    },
                                    startYear,
                                    startMonth - 1,
                                    startDay
                                ).show()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("start_date_picker_box")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Start Range (Tap to open Calendar)", color = TextSecondary, fontSize = 10.sp)
                                Text(
                                    text = String.format("%04d-%02d-%02d", startYear, startMonth, startDay),
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Start Date",
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // END DATE SELECTION TUNER
                    Text(
                        "TO: (End Date)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CarbonDarkBg)
                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isAnyProcessRunning) {
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        endYear = year
                                        endMonth = month + 1
                                        endDay = day
                                        activePreset = null
                                    },
                                    endYear,
                                    endMonth - 1,
                                    endDay
                                ).show()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("end_date_picker_box")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("End Range (Tap to open Calendar)", color = TextSecondary, fontSize = 10.sp)
                                Text(
                                    text = String.format("%04d-%02d-%02d", endYear, endMonth, endDay),
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select End Date",
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sub views depending on sub-tab index selection
        val startMs = getMillisForDate(startYear, startMonth, startDay, false)
        val endMs = getMillisForDate(endYear, endMonth, endDay, true)

        if (subTab == 0) {
            // Option 1: Search & Destroy Configurator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "DISCOVERY FILTER CATEGORIES",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Filters local directories matching specific system criteria.",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.SansSerif
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category grid list items
                        val categories = listOf("ALL", "IMAGE", "VIDEO", "AUDIO", "DOCUMENT")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.take(3).forEach { cat ->
                                val isChosen = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isChosen) TerminalCyan.copy(alpha = 0.12f) else CarbonDarkBg,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isChosen) TerminalCyan else SlateBorder,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable(enabled = !isAnyProcessRunning) { selectedCategory = cat }
                                        .padding(vertical = 8.dp)
                                        .testTag("cat_filter_$cat"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isChosen) TerminalCyan else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.drop(3).forEach { cat ->
                                val isChosen = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isChosen) TerminalCyan.copy(alpha = 0.12f) else CarbonDarkBg,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isChosen) TerminalCyan else SlateBorder,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable(enabled = !isAnyProcessRunning) { selectedCategory = cat }
                                        .padding(vertical = 8.dp)
                                        .testTag("cat_filter_$cat"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isChosen) TerminalCyan else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            // Filler slot for perfect look balance
                            Box(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                permissionLauncher.launch(permissionsToRequest)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("start_scan_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalCyan),
                            enabled = !isAnyProcessRunning,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = CarbonDarkBg, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "SCAN SELECTED DATE RANGE",
                                    color = CarbonDarkBg,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Scanned result tree elements
            if (scannedFiles.isNotEmpty()) {
                item {
                    val selectedCount = scannedFiles.count { it.isSelected }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DISCOVERED SCRAP REMNANTS (${scannedFiles.size})",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "SELECT ALL",
                            color = TerminalCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable(enabled = !isAnyProcessRunning) {
                                    val allSelected = scannedFiles.all { it.isSelected }
                                    viewModel.setAllScannedFilesSelected(!allSelected)
                                }
                                .padding(6.dp)
                        )
                    }
                }

                itemsIndexed(scannedFiles) { index, file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isAnyProcessRunning) { viewModel.toggleScannedFileSelected(index) },
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, if (file.isSelected) TerminalCyan.copy(alpha = 0.5f) else SlateBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CyberCheckbox(
                                checked = file.isSelected,
                                onCheckedChange = { viewModel.toggleScannedFileSelected(index) }
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(text = file.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Size: ${viewModel.formatSize(file.size)}", color = TextSecondary, fontSize = 10.sp)
                                    Text(text = "•", color = TextSecondary, fontSize = 10.sp)
                                    Text(text = file.category, color = TerminalCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(text = "•", color = TextSecondary, fontSize = 10.sp)
                                    val formattedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(file.modifiedDate))
                                    Text(text = formattedDate, color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    val selectedCount = scannedFiles.count { it.isSelected }
                    var showConfirmShredScanned by remember { mutableStateOf(false) }

                    if (showConfirmShredScanned) {
                        CyberConfirmDialog(
                            title = "CONFIRM DISCOVERY SHREDDING",
                            message = "You are about to permanently shred and destroy $selectedCount selected scanned target(s) matching your criteria. Files will be overwritten physically with high-entropy randomized patterns. Continue?",
                            onConfirm = {
                                showConfirmShredScanned = false
                                viewModel.startShreddingScannedFiles()
                            },
                            onDismiss = { showConfirmShredScanned = false }
                        )
                    }

                    Button(
                        onClick = { showConfirmShredScanned = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("shred_scanned_files_button"),
                        enabled = selectedCount > 0 && !isAnyProcessRunning,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SHRED AND DESTROY SELECTED ($selectedCount)",
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Select date and category scope, then click 'SCAN LOCAL DIRECTORIES' to inspect file nodes.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            // Option 2: Deep Sector Wipe (Old Forensic Space Overwriter)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LaserRed.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, LaserRed.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = LaserRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "DEEP SECTOR FREE-SPACE ERASER",
                                    color = LaserRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Any standard deleted files often continue to exist in empty block sectors of the storage partition. Running the deep sector free-space eraser writes physical high-entropy raw streams onto sectors modified during the target time window to permanently and absolutely destroy any remnants beyond any possibility of recovery.",
                                    color = LaserRed.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "ENGAGE PHYSICAL OVERWRITE",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Purges all deleted remnants from unallocated partition indexes.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        var showConfirmDeepWipe by remember { mutableStateOf(false) }

                        if (showConfirmDeepWipe) {
                            CyberConfirmDialog(
                                title = "ENGAGE PHYSICAL OVERWRITE",
                                message = "This will write high-entropy sweep vectors directly to unallocated sectors modified between the selected calendar dates. This will permanently neutralize recovered and deleted files remnants. Proceed?",
                                onConfirm = {
                                    showConfirmDeepWipe = false
                                    viewModel.startDeepCleanDeletedRange(startMs, endMs)
                                },
                                onDismiss = { showConfirmDeepWipe = false }
                            )
                        }

                        Button(
                            onClick = { showConfirmDeepWipe = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("deep_sector_wipe_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                            enabled = !isAnyProcessRunning,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Obliterate Remnants in Range",
                                    color = TextPrimary,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CyberCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(if (checked) NeonGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(6.dp))
            .border(1.5.dp, if (checked) NeonGreen else SlateBorder, RoundedCornerShape(6.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(NeonGreen, RoundedCornerShape(3.dp))
            )
        }
    }
}



fun getMillisForDate(y: Int, m: Int, d: Int, isEndOfDay: Boolean): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.YEAR, y)
    cal.set(java.util.Calendar.MONTH, m - 1)
    cal.set(java.util.Calendar.DAY_OF_MONTH, d)
    if (isEndOfDay) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
    } else {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

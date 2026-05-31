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
import androidx.compose.material.icons.filled.Warning
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
    val tabs = listOf("Wipe Files", "Wipe Text", "Shred History", "Algorithms")

    val selectedFiles by viewModel.selectedFiles.collectAsStateWithLifecycle()
    val currentAlgo by viewModel.selectedAlgorithm.collectAsStateWithLifecycle()
    val progressState by viewModel.progressState.collectAsStateWithLifecycle()
    val historyLog by viewModel.historyState.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            viewModel.addFiles(uris)
            Toast.makeText(context, "${uris.size} sensitive file(s) loaded", Toast.LENGTH_SHORT).show()
        }
    }

    // Main layout
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CarbonDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // High-fidelity Cyber Header
        SecureHeader(
            historyCount = historyLog.size,
            totalBytesShredded = historyLog.sumOf { it.originalSize },
            securityScore = if (historyLog.isEmpty()) "0%" else if (historyLog.any { it.passes >= 3 }) "A++ SECURE" else "BASIC"
        )

        // Custom Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CarbonDarkBg,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonGreen
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SlateBorder)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("tab_$index"),
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        // Active View Section with Animation
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
                2 -> ShredHistoryTab(historyLog = historyLog, onClearAll = { viewModel.clearHistory() }, viewModel = viewModel)
                3 -> AlgorithmsTab()
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
            .padding(16.dp),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security Active",
                        tint = NeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SECURE DESTRUCT SYSTEM",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (1).sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dashboard Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DashboardStat(
                    label = "SHRED WORKED",
                    value = "$historyCount Assets",
                    accentColor = TerminalCyan
                )
                DashboardStat(
                    label = "TOTAL SECURE WIPED",
                    value = formatBytes(totalBytesShredded),
                    accentColor = ElectricAmber
                )
                DashboardStat(
                    label = "FORENSIC GRADE",
                    value = securityScore,
                    accentColor = NeonGreen
                )
            }
        }
    }
}

@Composable
fun DashboardStat(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(modifier = Modifier.widthIn(max = 100.dp)) {
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
                            text = "Alg: ${currentAlgo.name}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Pass: ${progressState.currentPass}/${progressState.totalPasses}",
                            color = ElectricAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
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
                                else -> TextPrimary.copy(alpha = 0.85f)
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
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("select_files_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = CarbonDarkBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "LOAD FILES",
                                        color = CarbonDarkBg,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
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

            // Shredder Algorithm Option Picker
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
                            "SECURE ALGORITHM PROTOCOL",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val algos = listOf(
                            ShredAlgorithm.ZeroFill,
                            ShredAlgorithm.DoD3Pass,
                            ShredAlgorithm.DoD7Pass,
                            ShredAlgorithm.Gutmann
                        )

                        algos.forEach { algo ->
                            val isSelected = algo == currentAlgo
                            val borderCol = if (isSelected) NeonGreen else SlateBorder
                            val surfaceCol = if (isSelected) CarbonDarkBg else CharcoalSurface

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(surfaceCol)
                                    .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                                    .clickable { onAlgoSelected(algo) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = algo.name,
                                            color = if (isSelected) NeonGreen else TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) NeonGreen.copy(alpha = 0.15f) else SlateBorder,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                algo.securityLevel,
                                                color = if (isSelected) NeonGreen else TextSecondary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        algo.description,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hold-to-Shred Button Action
            item {
                Spacer(modifier = Modifier.height(8.dp))
                val pickerEnabled = selectedFiles.isNotEmpty() && !progressState.isShredding

                HoldToConfirmButton(
                    enabled = pickerEnabled,
                    onConfirmed = onStartShred,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("hold_to_shred_trigger")
                ) { holdProgress ->
                    val colorGradient = Brush.horizontalGradient(
                        colors = listOf(LaserRed, Color(0xFFC01F1F))
                    )
                    val progressGradient = Brush.horizontalGradient(
                        colors = listOf(NeonGreen, TerminalCyan)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (pickerEnabled) CharcoalSurface else CharcoalSurface.copy(alpha = 0.5f))
                            .border(
                                1.dp,
                                if (pickerEnabled) LaserRed else SlateBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .drawWithContent {
                                if (holdProgress > 0f) {
                                    drawRect(
                                        brush = progressGradient,
                                        size = size.copy(width = size.width * holdProgress)
                                    )
                                }
                                drawContent()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (holdProgress > 0f) {
                                Text(
                                    text = "ENGAGING SECURITY OVERWRITE... ${(holdProgress * 100).toInt()}%",
                                    color = CarbonDarkBg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                Text(
                                    text = if (pickerEnabled) "HOLD TO SECURELY SHRED" else "LOAD ASSETS KEYED TO PROTOCOL",
                                    color = if (pickerEnabled) LaserRed else TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (pickerEnabled) {
                                    Text(
                                        text = "Wipes ${selectedFiles.size} asset(s) with ${currentAlgo.name}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                .weight(1.0f)
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

        Button(
            onClick = {
                if (notesInput.isNotEmpty()) {
                    viewModel.shredNotes()
                } else {
                    Toast.makeText(context, "Please enter some sensitive text first", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = notesInput.isNotEmpty() && !isShreddingNote,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("shred_notes_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = LaserRed,
                disabledContainerColor = SlateBorder
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isShreddingNote) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "ZEROING MEMORY BUFFER...",
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "SHRED AND PURGE MEMORY CHAR-ARRAYS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
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
        GuideItem("How does Shredding prevent forensic restoration?", "When you delete a standard file, the OS only erases its path pointer, leaving physical block elements intact for forensic software to recover easily. Secure Shredding overwrites raw byte sectors in place using multiple cycles of zeros, high contrast fields, and secure randomized bytes before truncating metadata structures fully."),
        GuideItem("Is 1 Pass fast Zero-Fill sufficient?", "Yes, for modern Solid State Drives (SSDs), a single-pass full raw Zero-Fill or Random overwrite is sufficient to invalidate NAND flash translation tables, making electronic file recovery impossible. Traditional spinning magnetic media, however, might retain structural signatures of the bits on plate margins, which is why multi-pass standards exist."),
        GuideItem("DoD 5220.22-M military standards decoded", "Originating from the US Defense Security Service, DoD 5220.22-M specifies a 3-pass overwrite procedure (zeros, ones, followed by secure random bytes). This protocol ensures magnetic core layers are thoroughly disturbed at a molecular level, preventing laboratory physical recovery."),
        GuideItem("Peter Gutmann's exhaustive 35-pass wiping", "Created in 1996, the Gutmann algorithm targets old hard drives that utilized MFM/RLL magnetic coding formats. It runs 35 passes of custom mathematically paired pulses to safely counter magnetic force microscopy tracking. While highly prestigious, it is slower and redundant on modern SSD hardware.")
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

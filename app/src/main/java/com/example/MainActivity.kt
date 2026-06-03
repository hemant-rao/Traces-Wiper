package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
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
import com.example.recover.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val storagePermissionGranted = mutableStateOf(false)

    // Runtime permission popup for Android 10 and below (read/write external storage)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        storagePermissionGranted.value = hasRequiredPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        storagePermissionGranted.value = hasRequiredPermissions()
        setContent {
            MyApplicationTheme {
                val isGranted = storagePermissionGranted.value // verified
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    ShredderAppScreen(
                        modifier = Modifier.padding(innerPadding),
                        isStorageGranted = isGranted,
                        onRequestStoragePermission = { ensureFileAccessPermission() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        storagePermissionGranted.value = hasRequiredPermissions()
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Requests file-access permission so the shredder can physically delete files.
     * Android 11+ : opens the "All files access" permission screen (required to unlink
     * files outside the app sandbox). Android 10 and below: shows the standard
     * read/write storage runtime permission popup.
     */
    private fun ensureFileAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                } catch (e: Exception) {
                    try {
                        startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    } catch (_: Exception) {
                        // Settings screen unavailable on this device; picker still works.
                    }
                }
            }
        } else {
            val perms = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val needsRequest = perms.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (needsRequest) {
                storagePermissionLauncher.launch(perms)
            }
        }
    }
}

@Composable
fun ShredderAppScreen(
    modifier: Modifier = Modifier,
    viewModel: ShredderViewModel = viewModel(),
    isStorageGranted: Boolean = true,
    onRequestStoragePermission: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    
    // Exactly 4 structured tabs with high visual scanability icons
    val tabs = listOf(
        CyberTabItem("Files", Icons.Default.Delete, "Secure file wiping"),
        CyberTabItem("Text", Icons.Default.Lock, "Clean text scrubbing"),
        CyberTabItem("Deep Wipe", Icons.Default.Refresh, "Sanitize storage blocks"),
        CyberTabItem("History", Icons.Default.History, "Local session database"),
        CyberTabItem("Recoverable", Icons.Default.Search, "Find recoverable traces")
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

    // Launches the system delete-confirmation dialog requested by MediaStore.createDeleteRequest
    // when a wiped file cannot be unlinked directly, then reports the user's choice back.
    val deleteConsentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeleteConsentResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(Unit) {
        viewModel.deleteRequest.collect { intentSender ->
            deleteConsentLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
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

        CompactDashboardHeader(
            historyCount = historyLog.size,
            totalBytesShredded = historyLog.fold(0L) { acc, h -> val n = acc + h.originalSize.coerceAtLeast(0L); if (n < acc) Long.MAX_VALUE else n },
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
                    viewModel = viewModel,
                    isStorageGranted = isStorageGranted,
                    onRequestStoragePermission = onRequestStoragePermission
                )
                1 -> TextWiperTab(viewModel = viewModel)
                2 -> DeepWipeTab(viewModel = viewModel)
                3 -> HistoryAndGuideTab(
                    historyLog = historyLog,
                    onClearAll = { viewModel.clearHistory() },
                    viewModel = viewModel
                )
                4 -> {
                    val recoverableViewModel: com.example.recover.RecoverableTracesViewModel = viewModel()
                    com.example.recover.RecoverableTracesScreen(recoverableViewModel)
                }
            }
        }
    }

    // Full-Page Overlay Cyber-Security Process Loader Overlay
    if (isScanning || isDeepWiping || (progressState.isShredding && selectedTab == 2)) {
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
                        progressState.isShredding -> "PERMANENTLY DELETING FILES"
                        isDeepWiping -> "DEEP WIPE IN PROGRESS"
                        isScanning -> "SCANNING STORAGE"
                        isShreddingNote -> "DELETING TEXT SECURELY"
                        else -> "SYSTEM PROTOCOL ACTIVE"
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
                            text = "Destroyed: ${Math.max(0, progressState.currentFileIndex - 1)} | Remaining: ${progressState.totalFilesCount - progressState.currentFileIndex + 1}",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            val totalPasses = Math.max(1, progressState.totalPasses)
                            val passComponent = Math.max(0f, progressState.currentPass - 1f)
                            val overallProgress = ((passComponent + (progressState.passProgress / 100f)) / totalPasses).coerceIn(0f, 1f)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DESTROYING DATA",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${(overallProgress * 100).toInt()}%",
                                    color = themeAccentColor,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { overallProgress },
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
                                progress = { sweepProg.coerceIn(0f, 1f) },
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
                            text = "Searching through folders for files matching your selected dates...",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )
                    } else if (isShreddingNote) {
                        Text(
                            text = "Permanently wiping text from memory...",
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
fun CompactDashboardHeader(
    historyCount: Int,
    totalBytesShredded: Long,
    securityScore: String
) {
    var totalSpace by remember { mutableStateOf(1L) }
    var freeSpace by remember { mutableStateOf(1L) }
    var usedSpace by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        // Poll every 5 seconds to provide live updates of storage changes
        while(true) {
            try {
                val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
                val blockSize = stat.blockSizeLong
                totalSpace = stat.blockCountLong * blockSize
                freeSpace = stat.availableBlocksLong * blockSize
                usedSpace = totalSpace - freeSpace
            } catch (e: Exception) {
                // Ignore stat errors
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    val usedPercentage = if (totalSpace > 0) (usedSpace.toFloat() / totalSpace.toFloat()) else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Free Space Bar
            val freePercentage = 1f - usedPercentage
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).align(Alignment.BottomCenter).background(Color(0xFF1E293B)))
            Box(modifier = Modifier.fillMaxWidth(freePercentage.coerceIn(0f, 1f)).height(4.dp).align(Alignment.BottomCenter).background(NeonGreen))

            // Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shredded $historyCount",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Wiped ${formatBytesStatic(totalBytesShredded)}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Security $securityScore",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun formatBytesStatic(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
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
    viewModel: ShredderViewModel,
    isStorageGranted: Boolean = true,
    onRequestStoragePermission: () -> Unit = {}
) {
    var showConfirmShredDialog by remember { mutableStateOf(false) }

    if (showConfirmShredDialog) {
        CyberConfirmDialog(
            title = "EXECUTE SECURE SHREDDING",
            message = "You are about to permanently delete ${selectedFiles.size} selected file(s). This operation will overwrite your files ${currentAlgo.totalPasses} times, making them impossible to recover.\n\nAre you sure you want to proceed?",
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
                    
                    Spacer(modifier = Modifier.height(28.dp))

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
                        
                        val totalPasses = Math.max(1, progressState.totalPasses)
                        val passComponent = Math.max(0f, progressState.currentPass - 1f)
                        val overallProgress = ((passComponent + (progressState.passProgress / 100f)) / totalPasses).coerceIn(0f, 1f)
                            
                        Text(
                            text = "${(overallProgress * 100).toInt()}%",
                            color = ElectricAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val totalPasses = Math.max(1, progressState.totalPasses)
                    val passComponent = Math.max(0f, progressState.currentPass - 1f)
                    val overallProgress = ((passComponent + (progressState.passProgress / 100f)) / totalPasses).coerceIn(0f, 1f)
                    
                    LinearProgressIndicator(
                        progress = { overallProgress },
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
                            text = "WARNING: Secure Shredding overwrites file data with multiple random passes before deletion. Deleted materials are PERMANENTLY UNRECOVERABLE by any hardware or software methods. Use with absolute caution.",
                            color = LaserRed,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (!isStorageGranted) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("permission_prompt_card"),
                        colors = CardDefaults.cardColors(containerColor = ElectricAmber.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, ElectricAmber.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Permission Info",
                                    tint = ElectricAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "DIRECT SSD STORAGE ACCESS WIPE LIMITATION",
                                        color = ElectricAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Without 'All Files Access', the app cannot fully reach all files. Grant this permission to enable completely secure deletion that permanently removes all traces of your data.",
                                        color = TextPrimary.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onRequestStoragePermission,
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricAmber),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "GRANT DIRECT SECURE STORAGE ACCESS",
                                            color = Color.Black,
                                            fontSize = 10.sp,
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

            // Active overwrite algorithm selector (wires onAlgoSelected / currentAlgo)
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
                                "OVERWRITE ALGORITHM",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "We use the most powerful Permanent Delete standard to completely overwrite your files 35 times. Once deleted, they are gone forever and cannot be recovered by anyone.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily.SansSerif
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val algorithms = listOf(
                            ShredAlgorithm.Gutmann
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            algorithms.forEach { algo ->
                                val isSelected = algo == currentAlgo
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) NeonGreen.copy(alpha = 0.12f) else CarbonDarkBg,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonGreen else SlateBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onAlgoSelected(algo) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("algo_option_${algo.name}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = algo.name,
                                            color = if (isSelected) NeonGreen else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = algo.securityLevel,
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                (if (isSelected) NeonGreen else TextSecondary).copy(alpha = 0.12f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${algo.totalPasses}P",
                                            color = if (isSelected) NeonGreen else TextSecondary,
                                            fontSize = 10.sp,
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
                                    text = "Tap and hold to permanently delete ${selectedFiles.size} selected file(s)",
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
    val wipedLogs by viewModel.wipedTextLogs.collectAsStateWithLifecycle()
    val progressState by viewModel.progressState.collectAsStateWithLifecycle()
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
                    "SECURE TEXT DELETER",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Any text you copy, paste, or type on your phone can stay hidden in your device's memory for a long time. Use this tool to permanently delete sensitive text so no one can ever find it.\n\nExamples of what to delete here:\n• Passwords or secret PIN codes\n• Bank account numbers or card details\n• Private notes or messages you want to destroy completely",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
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
                color = TextPrimary
            ),
            placeholder = {
                Text(
                    "Paste your sensitive text here (e.g. passwords, bank details, private messages) to delete it forever...",
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

        val buttonEnabled = notesInput.isNotEmpty() && !isShreddingNote && !progressState.isShredding
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
                        text = if (progress > 0f) "DELETING: ${(progress * 100).toInt()}%" else if (buttonEnabled) "HOLD TO PERMANENTLY DELETE" else "ENTER TEXT TO DELETE",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (wipedLogs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "RECENTLY WIPED TEXT",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wipedLogs.forEach { log ->
                        Text(
                            text = log,
                            color = TerminalCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
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
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        CyberConfirmDialog(
            title = "CLEAR HISTORY",
            message = "This will permanently erase all wipe records. This action cannot be undone.",
            confirmText = "CONFIRM AND EXECUTE",
            cancelText = "ABORT PROTOCOL",
            onConfirm = {
                onClearAll()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }

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
                        .clickable { showClearDialog = true }
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
                    "Every file you permanently delete will show up in this history log.",
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
                                text = "WIPE VERIFIED",
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
fun ScannedFileListCard(
    file: com.example.ui.DeletedTrace,
    isSelected: Boolean,
    onToggle: () -> Unit,
    isProcessRunning: Boolean,
    onImageClick: () -> Unit,
    formattedSize: String,
    isDetailed: Boolean
) {
    val formattedDate = remember(file.deletedEstimateTime) { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(file.deletedEstimateTime ?: 0L)) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isProcessRunning) { onToggle() },
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, if (isSelected) TerminalCyan.copy(alpha = 0.5f) else SlateBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberCheckbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    enabled = !isProcessRunning
                )

                Spacer(modifier = Modifier.width(12.dp))

                if (isDetailed) {
                    val uri = file.uri ?: android.net.Uri.parse("file://${file.originalPath}")
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onImageClick() }
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1.0f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = file.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        if (!file.existsPhysically) {
                            Text(text = "[MISSING]", color = LaserRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        } else {
                            Text(text = "[RESIDUE]", color = TerminalCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Size: $formattedSize", color = TextSecondary, fontSize = 10.sp)
                        Text(text = "•", color = TextSecondary, fontSize = 10.sp)
                        Text(text = file.category, color = TerminalCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    if (isDetailed) {
                        Text(text = formattedDate, color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
            if (isDetailed && file.category == "IMAGE") { // Provide an even larger preview sometimes?
                 Spacer(modifier = Modifier.height(8.dp))
                 val uri = file.uri ?: android.net.Uri.parse("file://${file.originalPath}")
                 AsyncImage(
                     model = uri,
                     contentDescription = null,
                     modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp)).clickable { onImageClick() },
                     contentScale = ContentScale.Crop
                 )
            }
        }
    }
}

@Composable
fun ScannedFileGridCard(
    file: com.example.ui.DeletedTrace,
    isSelected: Boolean,
    onToggle: () -> Unit,
    isProcessRunning: Boolean,
    onImageClick: () -> Unit,
    formattedSize: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isProcessRunning) { onToggle() },
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, if (isSelected) TerminalCyan.copy(alpha = 0.5f) else SlateBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val uri = file.uri ?: android.net.Uri.parse("file://${file.originalPath}")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clickable { onImageClick() },
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    CyberCheckbox(
                        checked = isSelected,
                        onCheckedChange = { onToggle() },
                        enabled = !isProcessRunning
                    )
                }
                Box(
                    modifier = Modifier.align(Alignment.BottomStart).background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(text = formattedSize, color = TextPrimary, fontSize = 9.sp)
                }
            }
            Text(
                text = file.name,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AlgorithmsTab() {
    val itemsGuide = listOf(
        GuideItem("How does it work?", "When you delete a file normally, it's just hidden and can still be recovered. Our advanced algorithm completely deletes the data and makes it unrecoverable, so files can never be recovered again."),
        GuideItem("Is it completely safe?", "Yes! Your files will be securely deleted and permanently removed from your phone. Once deleted, they are gone forever."),
        GuideItem("Who can recover my files after this?", "No one. Not even advanced recovery software or professionals can bring back data after our Permanent Delete process is finished.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "HOW PERMANENT DELETE WORKS",
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
                        fontSize = 12.sp,
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
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
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
    val deepWipeViewModel: com.example.wipe.DeepWipeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    com.example.wipe.DeepWipeScreen(deepWipeViewModel)
}

@Composable
fun CyberCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(if (checked) NeonGreen.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(6.dp))
            .border(1.5.dp, if (checked) NeonGreen else SlateBorder, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
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

@Composable
fun PermissionBlockedScreen(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDarkBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
        ) {
            // Neon glowing shield header icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(LaserRed.copy(alpha = 0.12f), CircleShape)
                    .border(2.dp, LaserRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Shield Locked",
                    tint = LaserRed,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "STORAGE PERMISSION REQUIRED",
                color = LaserRed,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To securely overwrite, truncate, and physically destroy selected files beyond recovery, the Dig-Deep-Delete engine requires higher direct filesystem access.",
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your privacy is 100% guaranteed. All operations happen offline directly on your phone. No data ever leaves your device.",
                color = TextSecondary,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Premium Cyber Button
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("grant_permission_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "AUTHORIZE SECURE ACCESS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 1.5.dp,
                    color = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Awaiting authentication...",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

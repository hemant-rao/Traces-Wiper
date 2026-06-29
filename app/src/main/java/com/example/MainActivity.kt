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
import androidx.activity.SystemBarStyle
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BrightnessMedium
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.offset
import kotlin.math.roundToInt
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
import com.example.data.AppTexts
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
import com.example.forensics.*
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
        storagePermissionGranted.value = hasRequiredPermissions()
        // §778 — config-driven OdioBook ads (admin-controlled). Inits MobileAds +
        // fetches this app's slice; no-op until ads are enabled for "digdeep".
        com.example.ads.OdioBookAds.init(applicationContext, "digdeep")
        setContent {
            ThemeState.systemIsDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = ThemeState.isDarkTheme
            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }
            
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    AnimatedSplashScreen(onSplashFinished = { showSplash = false })
                } else {
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
fun AnimatedSplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Scale animation
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "SplashScale"
    )

    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = androidx.compose.animation.core.LinearEasing
        ),
        label = "SplashAlpha"
    )
    
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000L) // Wait for 2 seconds
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (ThemeState.isDarkTheme) com.example.ui.theme.CarbonDarkBg else Color(0xFFF8FAFC)), // Slate-50 for light mode
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha).scale(scale)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "App Logo",
                tint = NeonGreen,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Traces Wiper",
                color = com.example.ui.theme.NeonGreen,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Text(
                text = "MILITARY-GRADE SECURE ERASURE",
                color = NeonGreen,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
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
    var selectedTab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0) }

    androidx.activity.compose.BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    val density = LocalDensity.current
    var headerHeightPx by remember { mutableStateOf(0f) }
    var headerOffsetHeightPx by remember { mutableStateOf(0f) }

    val nestedScrollConnection = remember(headerHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (headerHeightPx == 0f) return Offset.Zero
                val delta = available.y
                val newOffset = headerOffsetHeightPx + delta
                if (delta < 0) { // scrolling down (collapsing)
                    val oldOffset = headerOffsetHeightPx
                    headerOffsetHeightPx = newOffset.coerceIn(-headerHeightPx, 0f)
                    val consumed = headerOffsetHeightPx - oldOffset
                    return Offset(0.0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (headerHeightPx == 0f) return Offset.Zero
                val delta = available.y
                if (delta > 0) { // scrolling up (expanding)
                    val oldOffset = headerOffsetHeightPx
                    headerOffsetHeightPx = (headerOffsetHeightPx + delta).coerceIn(-headerHeightPx, 0f)
                    val consumedPx = headerOffsetHeightPx - oldOffset
                    return Offset(0.0f, consumedPx)
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(selectedTab) {
        headerOffsetHeightPx = 0f
    }
    
    // Four top-level tabs; related tools are grouped into inner sub-tabs within each.
    val tabs = listOf(
        CyberTabItem("Forensics", androidx.compose.material.icons.Icons.Default.Search, "Privacy & Forensic Analysis"),
        CyberTabItem("Shred", Icons.Default.Delete, "Securely destroy data"),
        CyberTabItem("Recover", Icons.Default.Search, "Find & restore deleted data"),
        CyberTabItem("History", Icons.Default.History, "Logs & security standards")
    )

    val selectedFiles by viewModel.selectedFiles.collectAsStateWithLifecycle()
    val currentAlgo by viewModel.selectedAlgorithm.collectAsStateWithLifecycle()
    val progressState by viewModel.progressState.collectAsStateWithLifecycle()
    val historyLog by viewModel.historyState.collectAsStateWithLifecycle()
    
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val isDeepWiping by viewModel.isDeepWiping.collectAsStateWithLifecycle()
    val isShreddingNote by viewModel.isShreddingNote.collectAsStateWithLifecycle()

    val recoverableViewModel: com.example.recover.RecoverableTracesViewModel = viewModel()
    val recoverableUiState by recoverableViewModel.ui.collectAsStateWithLifecycle()
    val isRecoverableScanning = recoverableUiState.scanning
    val isRecoverableWiping = recoverableUiState.wiping

    val dataRecoveryViewModel: com.example.recover.DataRecoveryViewModel = viewModel()
    val dataRecoveryUiState by dataRecoveryViewModel.ui.collectAsStateWithLifecycle()
    val isDataRecoveryScanning = dataRecoveryUiState.scanning
    val isDataRecoveryRecovering = dataRecoveryUiState.recovering

    val deepWipeViewModel: com.example.wipe.DeepWipeViewModel = viewModel()
    val deepWipeState by deepWipeViewModel.state.collectAsStateWithLifecycle()
    val isDeepWipeActive = deepWipeState is com.example.wipe.DeepWipeViewModel.WipeState.Running

    val isAnyProcessRunning = progressState.isShredding || 
                              isScanning || 
                              isDeepWiping || 
                              isShreddingNote || 
                              isDeepWipeActive ||
                              isRecoverableScanning ||
                              isRecoverableWiping ||
                              isDataRecoveryScanning ||
                              isDataRecoveryRecovering

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

    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.addDirectory(uri)
            Toast.makeText(context, "Directory contents queued", Toast.LENGTH_SHORT).show()
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
                .zIndex(1f)
                .background(CarbonDarkBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_logo_shield),
                    contentDescription = "Traces Wiper Pro Logo",
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(48.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = AppTexts.APP_DISPLAY_NAME.uppercase(),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (0.5).sp
                    )
                    Text(
                        text = AppTexts.APP_TAGLINE,
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
                        .size(36.dp)
                        .background(
                            if (ThemeState.isDarkTheme) Color(0xFF131A2A) else Color(0xFFE2E8F0),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            SlateBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (isAnyProcessRunning) {
                                Toast.makeText(context, AppTexts.SYSTEM_BUSY_TOAST, Toast.LENGTH_SHORT).show()
                            } else {
                                ThemeState.themeMode = when (ThemeState.themeMode) {
                                    com.example.ui.theme.ThemeMode.SYSTEM -> com.example.ui.theme.ThemeMode.LIGHT
                                    com.example.ui.theme.ThemeMode.LIGHT -> com.example.ui.theme.ThemeMode.DARK
                                    com.example.ui.theme.ThemeMode.DARK -> com.example.ui.theme.ThemeMode.SYSTEM
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (ThemeState.themeMode) {
                            com.example.ui.theme.ThemeMode.SYSTEM -> Icons.Filled.BrightnessMedium
                            com.example.ui.theme.ThemeMode.LIGHT -> Icons.Default.LightMode
                            com.example.ui.theme.ThemeMode.DARK -> Icons.Default.DarkMode
                        },
                        contentDescription = "Toggle Theme",
                        tint = if (ThemeState.isDarkTheme) Color(0xFFFBBF24) else Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
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

        if (isAnyProcessRunning) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = NeonGreen,
                trackColor = Color.Transparent
            )
        }

        val showStatsHeader = selectedTab == 1

        // Intermediate sliding frame box with custom collapsing header
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .then(if (showStatsHeader) Modifier.nestedScroll(nestedScrollConnection) else Modifier)
        ) {
            val headerOffsetHeightDp = with(density) { headerOffsetHeightPx.toDp() }
            val headerHeightDp = with(density) { headerHeightPx.toDp() }

            // Content Frame - shifts up and expands to fill space dynamically as header collapses
            val paddingTopValue = if (showStatsHeader) {
                java.lang.Math.max(0f, (headerHeightDp + headerOffsetHeightDp).value).dp
            } else {
                0.dp
            }

            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingTopValue),
                transitionSpec = {
                    val duration = 280
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(animationSpec = tween(duration)))
                            .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeOut(animationSpec = tween(duration)))
                    } else {
                        (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn(animationSpec = tween(duration)))
                            .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it / 3 } + fadeOut(animationSpec = tween(duration)))
                    }
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> ForensicsHubTab(viewModel = viewModel, isSystemBusy = isAnyProcessRunning)
                    1 -> ShredHubTab(
                        selectedFiles = selectedFiles,
                        currentAlgo = currentAlgo,
                        progressState = progressState,
                        onPickFiles = { filePickerLauncher.launch(arrayOf("*/*")) },
                        onPickDirectory = { directoryPickerLauncher.launch(null) },
                        onRemoveFile = { viewModel.removeFile(it) },
                        onAlgoSelected = { viewModel.updateSelectedAlgorithm(it) },
                        onStartShred = { viewModel.startShredding() },
                        viewModel = viewModel,
                        isStorageGranted = isStorageGranted,
                        onRequestStoragePermission = onRequestStoragePermission,
                        isSystemBusy = isAnyProcessRunning
                    )
                    2 -> RecoverHubTab(isSystemBusy = isAnyProcessRunning)
                    3 -> HistoryAndGuideTab(
                        historyLog = historyLog,
                        onClearAll = { viewModel.clearHistory() },
                        viewModel = viewModel,
                        isSystemBusy = isAnyProcessRunning
                    )
                }
            }

            if (showStatsHeader) {
                // Collapsing Statistics Header overlay block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(0, headerOffsetHeightPx.roundToInt()) }
                        .onGloballyPositioned { coordinates ->
                            headerHeightPx = coordinates.size.height.toFloat()
                        }
                ) {
                    CompactDashboardHeader(
                        historyCount = historyLog.size,
                        totalBytesShredded = historyLog.fold(0L) { acc, h -> val n = acc + h.originalSize.coerceAtLeast(0L); if (n < acc) Long.MAX_VALUE else n },
                        securityScore = if (historyLog.isEmpty()) "0%" else if (historyLog.any { it.passes >= 3 }) "A++ SECURE" else "BASIC"
                    )
                }
            }
        }

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
                val tabBackground by animateColorAsState(
                    targetValue = if (isSelected) NeonGreen.copy(alpha = 0.12f) else Color.Transparent,
                    animationSpec = tween(240),
                    label = "TabBgColor"
                )
                val tabTextAndIconColor by animateColorAsState(
                    targetValue = if (isSelected) NeonGreen else TextSecondary,
                    animationSpec = tween(240),
                    label = "TabTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tabBackground)
                        .clickable {
                            if (isAnyProcessRunning) {
                                Toast.makeText(context, "🔒 Active secure execution. Navigation is locked.", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedTab = index
                            }
                        }
                        .padding(vertical = 6.dp)
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tabItem.label,
                            color = tabTextAndIconColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // Full-Page Overlay Cyber-Security Process Loader Overlay
    if (isScanning || isDeepWiping || progressState.isShredding || isShreddingNote) {
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
                        if (progressState.isShredding || isDeepWiping) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = themeAccentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_logo_shield),
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                modifier = Modifier
                                    .size(48.dp)
                            )
                        }
                    }

                    if (progressState.isShredding && progressState.totalFilesCount > 0) {
                        val progress = (progressState.currentFileIndex - 1).toFloat() / progressState.totalFilesCount.toFloat()
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = themeAccentColor,
                            trackColor = themeAccentColor.copy(alpha = 0.2f)
                        )
                    }

                    val overlayTitleText = when {
                        progressState.isShredding -> "PERMANENTLY DELETING FILES"
                        isDeepWiping -> "DEEP WIPE IN PROGRESS"
                        isScanning -> "SCANNING STORAGE"
                        isShreddingNote -> "DELETING TEXT SECURELY"
                        else -> AppTexts.SYSTEM_BUSY_ACTIVE
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
                            text = "Erased ${Math.max(0, progressState.currentFileIndex - 1)} of ${progressState.totalFilesCount} files permanently",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            val totalFiles = Math.max(1, progressState.totalFilesCount)
                            val currentFileIdx = Math.max(1, progressState.currentFileIndex)
                            val totalPasses = Math.max(1, progressState.totalPasses)
                            val passComponent = Math.max(0f, progressState.currentPass - 1f)
                            val fileProgress = ((passComponent + (progressState.passProgress / 100f)) / totalPasses).coerceIn(0f, 1f)
                            val overallProgress = (((currentFileIdx - 1) + fileProgress) / totalFiles).coerceIn(0f, 1f)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SECURE ERASING IN PROGRESS...",
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

/**
 * Reusable capsule segmented control used for the inner sub-tabs that group related
 * tools under a single top-level tab (Shred, Recover, History).
 */
@Composable
fun InnerTabBar(
    titles: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CharcoalSurface, RoundedCornerShape(12.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        titles.forEachIndexed { index, title ->
            val isSelected = selected == index
            val tabBackground by animateColorAsState(
                targetValue = if (isSelected) NeonGreen.copy(alpha = 0.12f) else Color.Transparent,
                animationSpec = tween(240),
                label = "TabBgColor"
            )
            val tabTextColor by animateColorAsState(
                targetValue = if (isSelected) NeonGreen else TextSecondary,
                animationSpec = tween(240),
                label = "TabTextColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tabBackground)
                    .clickable {
                        if (!enabled) {
                            Toast.makeText(context, "🔒 Active secure execution. Sub-navigation is locked.", Toast.LENGTH_SHORT).show()
                        } else {
                            onSelect(index)
                        }
                    }
                    .padding(vertical = 6.dp),
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
}

/**
 * "Shred" top-level tab. Groups the three destroy-data tools — secure file wiping,
 * text scrubbing and deep free-space wiping — into inner sub-tabs.
 */
@Composable
fun ShredHubTab(
    selectedFiles: List<SelectedFileInfo>,
    currentAlgo: ShredAlgorithm,
    progressState: ShredProgressState,
    onPickFiles: () -> Unit,
    onPickDirectory: () -> Unit,
    onRemoveFile: (SelectedFileInfo) -> Unit,
    onAlgoSelected: (ShredAlgorithm) -> Unit,
    onStartShred: () -> Unit,
    viewModel: ShredderViewModel,
    isStorageGranted: Boolean = true,
    onRequestStoragePermission: () -> Unit = {},
    isSystemBusy: Boolean = false
) {
    var innerTab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0) }
    
    androidx.activity.compose.BackHandler(enabled = innerTab != 0) {
        innerTab = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InnerTabBar(
            titles = listOf("Files", "Text", "Deep Wipe"),
            selected = innerTab,
            onSelect = { innerTab = it },
            enabled = !isSystemBusy
        )

        AnimatedContent(
            targetState = innerTab,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            transitionSpec = {
                val duration = 240
                if (targetState > initialState) {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(animationSpec = tween(duration)))
                        .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeOut(animationSpec = tween(duration)))
                } else {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn(animationSpec = tween(duration)))
                        .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it / 3 } + fadeOut(animationSpec = tween(duration)))
                }
            },
            label = "ShredHubTabTransition"
        ) { targetInnerTab ->
            when (targetInnerTab) {
                0 -> FileShredderTab(
                    selectedFiles = selectedFiles,
                    currentAlgo = currentAlgo,
                    progressState = progressState,
                    onPickFiles = onPickFiles,
                    onPickDirectory = onPickDirectory,
                    onRemoveFile = onRemoveFile,
                    onAlgoSelected = onAlgoSelected,
                    onStartShred = onStartShred,
                    viewModel = viewModel,
                    isStorageGranted = isStorageGranted,
                    onRequestStoragePermission = onRequestStoragePermission,
                    isSystemBusy = isSystemBusy
                )
                1 -> TextWiperTab(viewModel = viewModel, isSystemBusy = isSystemBusy)
                2 -> DeepWipeTab(viewModel = viewModel, isSystemBusy = isSystemBusy)
            }
        }
    }
}

/**
 * "Recover" top-level tab. Groups trace discovery and data restoration into inner sub-tabs.
 */
@Composable
fun RecoverHubTab(isSystemBusy: Boolean = false) {
    var innerTab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0) }
    
    androidx.activity.compose.BackHandler(enabled = innerTab != 0) {
        innerTab = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InnerTabBar(
            titles = listOf("Clean Traces", "Restore"),
            selected = innerTab,
            onSelect = { innerTab = it },
            enabled = !isSystemBusy
        )

        AnimatedContent(
            targetState = innerTab,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            transitionSpec = {
                val duration = 240
                if (targetState > initialState) {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(animationSpec = tween(duration)))
                        .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeOut(animationSpec = tween(duration)))
                } else {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn(animationSpec = tween(duration)))
                        .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it / 3 } + fadeOut(animationSpec = tween(duration)))
                }
            },
            label = "RecoverHubTabTransition"
        ) { targetInnerTab ->
            when (targetInnerTab) {
                0 -> {
                    val recoverableViewModel: com.example.recover.RecoverableTracesViewModel = viewModel()
                    com.example.recover.RecoverableTracesScreen(recoverableViewModel, isSystemBusy = isSystemBusy)
                }
                1 -> {
                    val dataRecoveryViewModel: com.example.recover.DataRecoveryViewModel = viewModel()
                    com.example.recover.DataRecoveryScreen(dataRecoveryViewModel, isSystemBusy = isSystemBusy)
                }
            }
        }
    }
}

@Composable
fun HistoryAndGuideTab(
    historyLog: List<com.example.data.ShredHistory>,
    onClearAll: () -> Unit,
    viewModel: ShredderViewModel,
    isSystemBusy: Boolean = false
) {
    var insideTabSelection by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0) } // 0 = Shred Records, 1 = Security Standards, 2 = Our Apps
    
    androidx.activity.compose.BackHandler(enabled = insideTabSelection != 0) {
        insideTabSelection = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InnerTabBar(
            titles = listOf("Shred Records", "Security Standards", "Our Apps"),
            selected = insideTabSelection,
            onSelect = { insideTabSelection = it },
            enabled = !isSystemBusy
        )

        AnimatedContent(
            targetState = insideTabSelection,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            transitionSpec = {
                val duration = 240
                if (targetState > initialState) {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(animationSpec = tween(duration)))
                        .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeOut(animationSpec = tween(duration)))
                } else {
                    (slideInHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn(animationSpec = tween(duration)))
                        .togetherWith(slideOutHorizontally(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it / 3 } + fadeOut(animationSpec = tween(duration)))
                }
            },
            label = "HistoryAndGuideTabTransition"
        ) { targetInsideTab ->
            when (targetInsideTab) {
                0 -> ShredHistoryTab(historyLog, onClearAll, viewModel, isSystemBusy = isSystemBusy)
                1 -> AlgorithmsTab()
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        com.example.ui.OdioBookFamilySection(
                            currentAppTitle = "Dig Deep",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
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
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, SlateBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (!isExpanded) {
                // Collapsed sleek state
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SHREDS: $historyCount  •  WIPED: ${formatBytesStatic(totalBytesShredded)}  •  SECURE: $securityScore",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "EXPAND ▼",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                // Fully expanded state with high fidelity metrics and storage breakdown
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppTexts.METRICS_TITLE,
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "COLLAPSE ▲",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Row 1: The Three High-Fidelity Security Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Metric 1: Shredded Count
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$historyCount",
                            color = NeonGreen,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SHREDDED",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(SlateBorder)
                    )

                    // Metric 2: Total Wiped
                    Column(
                        modifier = Modifier.weight(1.2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatBytesStatic(totalBytesShredded),
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "TOTAL WIPED",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(SlateBorder)
                    )

                    // Metric 3: Security Status
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = securityScore,
                            color = TerminalCyan,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SECURITY",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Thin separating horizontal line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SlateBorder.copy(alpha = 0.6f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Storage Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(NeonGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DEVICE STORAGE STATUS",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = String.format(java.util.Locale.US, "%.0f%% Full", usedPercentage * 100),
                            color = if (usedPercentage > 0.85f) LaserRed else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Modern visual storage progress bar with rounded borders and a neon vibe
                    LinearProgressIndicator(
                        progress = { if (totalSpace > 0) usedPercentage else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonGreen,
                        trackColor = SlateBorder.copy(alpha = 0.5f),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Clear layout showing used & free space
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Used: ${formatBytesStatic(usedSpace)}",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Free: ${formatBytesStatic(freeSpace)}",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
    onPickDirectory: () -> Unit,
    onRemoveFile: (SelectedFileInfo) -> Unit,
    onAlgoSelected: (ShredAlgorithm) -> Unit,
    onStartShred: () -> Unit,
    viewModel: ShredderViewModel,
    isStorageGranted: Boolean = true,
    onRequestStoragePermission: () -> Unit = {},
    isSystemBusy: Boolean = false
) {
    var showConfirmShredDialog by remember { mutableStateOf(false) }
    val showWarningNote by viewModel.showWarningNote.collectAsStateWithLifecycle()
    val showStorageWarningNote by viewModel.showStorageWarningNote.collectAsStateWithLifecycle()

    if (showConfirmShredDialog) {
        CyberConfirmDialog(
            title = AppTexts.SHRED_CONFIRM_TITLE,
            message = "You are about to permanently delete ${selectedFiles.size} selected file(s). " + AppTexts.SHRED_CONFIRM_MESSAGE.substringAfter("permanently delete the selected files. "),
            confirmText = "PERMANENTLY DELETE",
            cancelText = "CANCEL",
            requireConfirmationWord = "CONFIRM",
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
                .padding(vertical = 12.dp),
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
                                "SECURELY DELETING FILES...",
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
                    val totalFiles = Math.max(1, progressState.totalFilesCount)
                    val currentFileIdx = Math.max(1, progressState.currentFileIndex)
                    val totalPasses = Math.max(1, progressState.totalPasses)
                    val passComponent = Math.max(0f, progressState.currentPass - 1f)
                    val fileProgress = ((passComponent + (progressState.passProgress / 100f)) / totalPasses).coerceIn(0f, 1f)
                    val overallProgress = (((currentFileIdx - 1) + fileProgress) / totalFiles).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Secure Erasing Progress",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        
                        Text(
                            text = "${(overallProgress * 100).toInt()}%",
                            color = ElectricAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (showWarningNote) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Explanatory warning note
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LaserRed.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, LaserRed.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 36.dp, bottom = 12.dp),
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
                                    text = AppTexts.SHRED_WARNING_WARN,
                                    color = LaserRed,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissWarningNote() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Warning",
                                    tint = LaserRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!isStorageGranted && showStorageWarningNote) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("permission_prompt_card"),
                        colors = CardDefaults.cardColors(containerColor = ElectricAmber.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, ElectricAmber.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 36.dp, bottom = 12.dp)) {
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
                                            enabled = !isSystemBusy,
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricAmber),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.height(38.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            Text(
                                                text = "GRANT DIRECT SECURE STORAGE ACCESS",
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                            IconButton(
                                onClick = { viewModel.dismissStorageWarningNote() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Storage Limitation Info",
                                    tint = ElectricAmber,
                                    modifier = Modifier.size(16.dp)
                                )
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
                                color = NeonGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                IconButton(
                                    onClick = onPickDirectory,
                                    enabled = !isSystemBusy,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(1.dp, NeonGreen, RoundedCornerShape(12.dp))
                                        .testTag("select_directories_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = "Select Folders",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onPickFiles,
                                    enabled = !isSystemBusy,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(NeonGreen, RoundedCornerShape(12.dp))
                                        .testTag("select_files_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "Select Files",
                                        tint = CarbonDarkBg,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedFiles.isEmpty()) {
                            // Empty state guidance
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No sensitive assets queued.\nTap 'Folders' or 'Files' to add items.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else {
                            // Display selected files list
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 480.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                selectedFiles.forEach { fileInfo ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                CarbonDarkBg,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.0f)) {
                                            Text(
                                                fileInfo.name,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                viewModel.formatSize(fileInfo.size),
                                                color = TextSecondary,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        IconButton(
                                            onClick = { onRemoveFile(fileInfo) },
                                            enabled = !isSystemBusy,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove selection",
                                                tint = LaserRed,
                                                modifier = Modifier.size(20.dp)
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
                                        text = "CLEAR ALL",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .clickable(enabled = !isSystemBusy) { viewModel.clearSelections() }
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
                                "ADVANCED WIPING ENGINE",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            AppTexts.SHRED_DESCRIPTION_NOTE,
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
                                        .clickable(enabled = !isSystemBusy) { onAlgoSelected(algo) }
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
                                            text = "SECURE",
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
                val pickerEnabled = selectedFiles.isNotEmpty() && !progressState.isShredding && !isSystemBusy

                SwipeToExecuteButton(
                    enabled = pickerEnabled,
                    onExecuted = {
                        showConfirmShredDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("secure_shred_trigger"),
                    initialText = if (pickerEnabled) "SLIDE TO PERMANENTLY DELETE" else AppTexts.SELECT_FILES_PROMPT,
                    executedText = "INITIALIZING...",
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TextWiperTab(viewModel: ShredderViewModel, isSystemBusy: Boolean = false) {
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
            .padding(vertical = 12.dp),
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
                    AppTexts.TEXT_WIPER_EXPLANATION,
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
            enabled = !isShreddingNote && !isSystemBusy,
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
                    AppTexts.TEXT_WIPER_PLACEHOLDER,
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

        val buttonEnabled = notesInput.isNotEmpty() && !isShreddingNote && !progressState.isShredding && !isSystemBusy
        SwipeToExecuteButton(
            enabled = buttonEnabled,
            onExecuted = {
                viewModel.shredNotes()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shred_notes_button"),
            initialText = if (buttonEnabled) "SLIDE TO PERMANENTLY DELETE" else "ENTER TEXT TO DELETE",
            executedText = "DELETING..."
        )

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
    viewModel: ShredderViewModel,
    isSystemBusy: Boolean = false
) {
    var showClearDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(historyLog.size) {
        if (historyLog.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    if (showClearDialog) {
        CyberConfirmDialog(
            title = "CLEAR HISTORY",
            message = "This will permanently erase all wipe records. This action cannot be undone.",
            confirmText = "CONFIRM AND EXECUTE",
            cancelText = AppTexts.ABORT_ACTION,
            requireConfirmationWord = "CLEAR",
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
            .padding(vertical = 12.dp),
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
                    color = if (isSystemBusy) LaserRed.copy(alpha = 0.5f) else LaserRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable(enabled = !isSystemBusy) { showClearDialog = true }
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
                state = listState,
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
                                        text = "${log.algorithm} (SECURE)",
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
                            .clickable(enabled = !isProcessRunning) { onImageClick() }
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
                     modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp)).clickable(enabled = !isProcessRunning) { onImageClick() },
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
                    modifier = Modifier.fillMaxSize().clickable(enabled = !isProcessRunning) { onImageClick() },
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
        GuideItem(AppTexts.GUIDE_Q_HOW_IT_WORKS, AppTexts.GUIDE_A_HOW_IT_WORKS),
        GuideItem(AppTexts.GUIDE_Q_IS_IT_SAFE, AppTexts.GUIDE_A_IS_IT_SAFE),
        GuideItem(AppTexts.GUIDE_Q_CAN_FILES_RECOVER, AppTexts.GUIDE_A_CAN_FILES_RECOVER)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_logo_shield),
                        contentDescription = "Traces Wiper Premium Logo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier
                            .size(160.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "TRACES WIPER PRO",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "VERSION 1.0.0 • 100% OFFLINE SECURED",
                        color = NeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This premium engine utilizes our specialized deleting process to thoroughly neutralize unallocated memory fragments.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

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
fun SwipeToExecuteButton(
    enabled: Boolean,
    onExecuted: () -> Unit,
    modifier: Modifier = Modifier,
    initialText: String = "SLIDE TO EXECUTE",
    executedText: String = "EXECUTING...",
    buttonColor: Color = LaserRed,
    textColor: Color = TextPrimary,
    disabledColor: Color = SlateBorder
) {
    var swipeOffset by remember { mutableStateOf(0f) }
    var isExecuted by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    // We get actual width to bound the drag
    var componentWidthPx by remember { mutableStateOf(0f) }
    val thumbSizeDp = 30.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }

    val animatedOffset by animateFloatAsState(
        targetValue = swipeOffset,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(isExecuted) {
        if (isExecuted) {
            onExecuted()
            delay(1000)
            isExecuted = false
            swipeOffset = 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(if (enabled) CharcoalSurface else CharcoalSurface.copy(alpha=0.5f), RoundedCornerShape(19.dp))
            .border(1.dp, if (enabled) buttonColor else disabledColor, RoundedCornerShape(19.dp))
            .clip(RoundedCornerShape(19.dp))
            .onGloballyPositioned { coordinates ->
                componentWidthPx = coordinates.size.width.toFloat()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val maxSwipePx = if (componentWidthPx > thumbSizePx) componentWidthPx - thumbSizePx - with(density) { 8.dp.toPx() } else 0f
        val progress = if (maxSwipePx > 0f) (swipeOffset / maxSwipePx).coerceIn(0f, 1f) else 0f

        if (progress > 0f && enabled) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(buttonColor.copy(alpha = 0.4f))
                    .align(Alignment.CenterStart)
            )
        }

        // Text behind
        Text(
            text = if (isExecuted) executedText else initialText,
            color = if (enabled) textColor else TextSecondary,
            modifier = Modifier.align(Alignment.CenterOffset(progress)),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )

        // Draggable thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .padding(4.dp)
                .size(thumbSizeDp)
                .background(if (enabled) buttonColor else disabledColor, CircleShape)
                .pointerInput(enabled, maxSwipePx) {
                    if (!enabled || maxSwipePx == 0f) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (swipeOffset > maxSwipePx * 0.8f) {
                                swipeOffset = maxSwipePx
                                isExecuted = true
                            } else {
                                swipeOffset = 0f
                            }
                        },
                        onDragCancel = {
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (!isExecuted) {
                                swipeOffset = (swipeOffset + dragAmount).coerceIn(0f, maxSwipePx)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

fun Alignment.Companion.CenterOffset(progress: Float): Alignment {
    return androidx.compose.ui.BiasAlignment(horizontalBias = 0f, verticalBias = 0f)
}

@Composable
fun CyberConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "CONFIRM",
    cancelText: String = AppTexts.ABORT_ACTION,
    requireConfirmationWord: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var confirmationInput by androidx.compose.runtime.remember { mutableStateOf("") }
    val isConfirmed = requireConfirmationWord == null || confirmationInput.equals(requireConfirmationWord, ignoreCase = true)

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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = message,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 16.sp
                )
                
                if (requireConfirmationWord != null) {
                    androidx.compose.material3.OutlinedTextField(
                        value = confirmationInput,
                        onValueChange = { confirmationInput = it },
                        label = { Text("Type '$requireConfirmationWord' to proceed", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("confirmation_input"),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isConfirmed) {
                        onConfirm()
                        onDismiss()
                    }
                },
                enabled = isConfirmed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaserRed,
                    disabledContainerColor = LaserRed.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(38.dp).testTag("dialog_confirm_button")
            ) {
                Text(
                    text = confirmText,
                    color = if (isConfirmed) Color.White else Color.White.copy(alpha = 0.5f),
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text(
                    text = cancelText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun DeepWipeTab(viewModel: ShredderViewModel, isSystemBusy: Boolean = false) {
    val deepWipeViewModel: com.example.wipe.DeepWipeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    com.example.wipe.DeepWipeScreen(deepWipeViewModel, isSystemBusy = isSystemBusy)
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
                text = "To effectively eliminate selected files beyond recovery, the Dig-Deep-Delete engine requires higher direct filesystem access.",
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your privacy is our priority. All operations happen offline directly on your phone. No data ever leaves your device.",
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
                    .height(38.dp)
                    .testTag("grant_permission_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "AUTHORIZE SECURE ACCESS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
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

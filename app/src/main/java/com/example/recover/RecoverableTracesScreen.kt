package com.example.recover

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.CyberCheckbox
import com.example.data.AppTexts
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverableTracesScreen(vm: RecoverableTracesViewModel, isSystemBusy: Boolean = false) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val consent by vm.consentRequest.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val df = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showConfirm by remember { mutableStateOf(false) }

    // launcher for media read permissions
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* user may now scan */ }

    // launcher for the system delete-consent dialog (other apps' media)
    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result -> vm.onConsentHandled(result.resultCode == Activity.RESULT_OK) }

    LaunchedEffect(consent) {
        consent?.let { consentLauncher.launch(IntentSenderRequest.Builder(it).build()) }
    }

    fun pickDate(initial: Long, onPicked: (Long) -> Unit) {
        val c = Calendar.getInstance().apply { timeInMillis = initial }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val picked = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                onPicked(picked.timeInMillis)
            },
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    var isGrid by remember { mutableStateOf(true) }

    LazyVerticalGrid(
        columns = if (isGrid) GridCells.Adaptive(116.dp) else GridCells.Fixed(1),
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title Blocks
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = LaserRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DELETED DATA REMNANTS",
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = AppTexts.TRACE_WIPER_SUBTITLE,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Science explanations Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = AppTexts.TRACE_WIPER_HOW_TITLE,
                            color = LaserRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = AppTexts.TRACE_WIPER_HOW_SUBTEXT,
                            color = TextPrimary.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Date Picker row (label = File date)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From Card picker
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSystemBusy) CharcoalSurface.copy(alpha = 0.5f) else CharcoalSurface)
                            .border(1.dp, if (isSystemBusy) SlateBorder.copy(alpha = 0.5f) else SlateBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isSystemBusy) { pickDate(ui.fromMillis) { vm.setRange(it, ui.toMillis) } }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "FROM FILE DATE",
                                    color = if (isSystemBusy) TextSecondary.copy(alpha = 0.5f) else TextSecondary,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = df.format(Date(ui.fromMillis)),
                                    color = if (isSystemBusy) TextPrimary.copy(alpha = 0.5f) else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = if (isSystemBusy) LaserRed.copy(alpha = 0.4f) else LaserRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // To Card picker
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSystemBusy) CharcoalSurface.copy(alpha = 0.5f) else CharcoalSurface)
                            .border(1.dp, if (isSystemBusy) SlateBorder.copy(alpha = 0.5f) else SlateBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isSystemBusy) { pickDate(ui.toMillis) { vm.setRange(ui.fromMillis, it) } }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "TO FILE DATE",
                                    color = if (isSystemBusy) TextSecondary.copy(alpha = 0.5f) else TextSecondary,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = df.format(Date(ui.toMillis)),
                                    color = if (isSystemBusy) TextPrimary.copy(alpha = 0.5f) else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = if (isSystemBusy) LaserRed.copy(alpha = 0.4f) else LaserRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Filter switches
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Include filesystem row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = LaserRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Deep Scan (filesystem leftovers)",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Walks cached directories and system logs",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Switch(
                                checked = ui.includeFilesystem,
                                onCheckedChange = vm::setIncludeFilesystem,
                                enabled = !isSystemBusy,
                                colors = SwitchDefaults.colors(checkedThumbColor = LaserRed, checkedTrackColor = LaserRed.copy(alpha = 0.3f))
                            )
                        }

                        // Only show orphans row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = TerminalCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Only show deleted files' leftover previews",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Filters out existing physical system pictures",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Switch(
                                checked = ui.onlyOrphans,
                                onCheckedChange = vm::setOnlyOrphans,
                                enabled = !isSystemBusy,
                                colors = SwitchDefaults.colors(checkedThumbColor = TerminalCyan, checkedTrackColor = TerminalCyan.copy(alpha = 0.3f))
                            )
                        }
                    }
                }

                // Buttons triggers row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            permLauncher.launch(neededReadPermissions())
                            vm.scan()
                        },
                        enabled = !isSystemBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "SCAN STORAGE CHIPS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (ui.includeFilesystem && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                        !Environment.isExternalStorageManager()
                    ) {
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                        Uri.parse("package:" + context.packageName)
                                    )
                                )
                            },
                            enabled = !isSystemBusy,
                            border = BorderStroke(1.dp, ElectricAmber.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricAmber),
                            modifier = Modifier.height(38.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ALLOW ALL-FILES",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Message diagnostic Banner
                ui.message?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = ElectricAmber.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, ElectricAmber.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ElectricAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = it,
                                color = ElectricAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Scanning display telemetry
                if (ui.scanning) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TrashRedWarningBg),
                        border = BorderStroke(1.dp, LaserRed.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = LaserRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CHECKING INTERNAL COPIES & METADATA...",
                                color = LaserRed,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Wiping actual progress display
                if (ui.wiping) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TrashRedWarningBg),
                        border = BorderStroke(1.dp, LaserRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val percent = if (ui.totalWipeCount > 0) ui.currentWipedIndex.toFloat() / ui.totalWipeCount else 0f
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = LaserRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "WIPING DELETED FILE REMNANTS SECURELY...",
                                        color = LaserRed,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${ui.currentWipedIndex} / ${ui.totalWipeCount}",
                                    color = LaserRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            LinearProgressIndicator(
                                progress = { percent },
                                color = LaserRed,
                                trackColor = SlateBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }

                // Found status statistics
                if (ui.traces.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CharcoalSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(LaserRed.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${ui.traces.size} TRACES FOUND",
                                    color = LaserRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(TerminalCyan.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${ui.selectedCount} CHOSEN",
                                    color = TerminalCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { isGrid = !isGrid },
                                enabled = !isSystemBusy,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isGrid) Icons.Default.List else Icons.Default.GridView,
                                    contentDescription = "Toggle layout",
                                    tint = if (isSystemBusy) TextSecondary.copy(alpha = 0.5f) else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "SELECT ALL",
                                color = if (isSystemBusy) NeonGreen.copy(alpha = 0.5f) else NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .clickable(enabled = !isSystemBusy) { vm.selectAll() }
                                    .padding(6.dp)
                            )
                            Text(
                                text = "RESET",
                                color = if (isSystemBusy) LaserRed.copy(alpha = 0.5f) else LaserRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .clickable(enabled = !isSystemBusy) { vm.clearSelection() }
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }
        }

        // LazyGrid list rows
        items(
            items = ui.traces,
            key = { it.id },
            span = { if (isGrid) GridItemSpan(1) else GridItemSpan(maxLineSpan) }
        ) { trace ->
            if (isGrid) {
                TraceCell(
                    trace = trace,
                    selected = trace.id in ui.selected,
                    isSystemBusy = isSystemBusy,
                    onClick = { vm.toggle(trace.id) }
                )
            } else {
                TraceListItem(
                    trace = trace,
                    selected = trace.id in ui.selected,
                    isSystemBusy = isSystemBusy,
                    onClick = { vm.toggle(trace.id) }
                )
            }
        }

        // Execution Wipe Selected Button
        if (ui.traces.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Button(
                    onClick = { showConfirm = true },
                    enabled = ui.selectedCount > 0 && !ui.wiping && !isSystemBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Text(
                        text = if (ui.wiping) "SHREDDING PERSISTENT CELL TRACES..." else "WIPE CHOSEN EVIDENCE FOREVER (${ui.selectedCount})",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
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
                        text = "DESTROY ${ui.selectedCount} TRACES?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Text(
                    text = "This completely overwrites and unlinks the selected items. They will be immediately erased on disk and cannot be recovered. Are you absolutely sure?",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.SansSerif
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirm = false
                        vm.wipeSelected()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "DESTROY EVIDENCE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirm = false },
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(
                        text = AppTexts.ABORT_ACTION,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun TraceListItem(trace: RecoverableTrace, selected: Boolean, isSystemBusy: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSystemBusy) { onClick() },
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, if (selected) LaserRed.copy(alpha = 0.5f) else SlateBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
         ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.Black, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
            ) {
                when (trace.category) {
                    TraceCategory.IMAGE, TraceCategory.VIDEO ->
                        AsyncImage(
                            model = trace.thumbnailModel,
                            contentDescription = trace.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    else ->
                        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (trace.category == TraceCategory.DOCUMENT) "DOC" else "FILE",
                                color = TerminalCyan,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                }
                if (trace.orphan) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(LaserRed.copy(alpha = 0.85f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DELETED",
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                     text = trace.displayName,
                     color = TextPrimary,
                     style = MaterialTheme.typography.bodyMedium,
                     fontWeight = FontWeight.Bold,
                     maxLines = 1,
                     overflow = TextOverflow.Ellipsis
                 )
                 Text(
                     text = trace.source.uppercase(),
                     color = TextSecondary,
                     fontSize = 9.sp,
                     fontFamily = FontFamily.Monospace,
                     maxLines = 1,
                     overflow = TextOverflow.Ellipsis
                 )
             }
             CyberCheckbox(
                 checked = selected,
                 onCheckedChange = { onClick() },
                 enabled = !isSystemBusy
             )
        }
    }
}

@Composable
private fun TraceCell(trace: RecoverableTrace, selected: Boolean, isSystemBusy: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(116.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !isSystemBusy) { onClick() },
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, if (selected) LaserRed.copy(alpha = 0.5f) else SlateBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(Color.Black)
            ) {
                when (trace.category) {
                    TraceCategory.IMAGE, TraceCategory.VIDEO ->
                        AsyncImage(
                            model = trace.thumbnailModel,
                            contentDescription = trace.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    else ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = when (trace.category) {
                                    TraceCategory.DOCUMENT -> "DOC"
                                    else -> "FILE"
                                },
                                color = TerminalCyan,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                }
                if (trace.category == TraceCategory.VIDEO) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "▶",
                            color = NeonGreen,
                            fontSize = 10.sp
                        )
                    }
                }
                if (trace.orphan) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(LaserRed.copy(alpha = 0.85f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ORPHAN",
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    CyberCheckbox(
                        checked = selected,
                        onCheckedChange = { onClick() },
                        enabled = !isSystemBusy
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = trace.displayName,
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = trace.source,
                    color = TextSecondary,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private val TrashRedWarningBg = Color(0x1AEF4444)

/** Read permissions appropriate to the OS version. */
private fun neededReadPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO
        )
    else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

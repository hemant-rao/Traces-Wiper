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
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun DataRecoveryScreen(vm: DataRecoveryViewModel, isSystemBusy: Boolean = false) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val consent by vm.consentRequest.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val df = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showConfirm by remember { mutableStateOf(false) }

    // launcher for media read permissions — scan once the permission dialog resolves
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.scan() }

    // launcher for the system untrash-consent dialog (other apps' trashed media)
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
        // Explanatory header, date range, filters, scan trigger
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title Block
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECOVER DELETED DATA",
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = AppTexts.DATA_RECOVERY_SUBTITLE,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Science briefing box
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
                            text = AppTexts.DATA_RECOVERY_HOW_TITLE,
                            color = TerminalCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = AppTexts.DATA_RECOVERY_HOW_SUBTEXT,
                            color = TextPrimary.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Date Range selection selector rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From Date Item
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
                                tint = if (isSystemBusy) NeonGreen.copy(alpha = 0.4f) else NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // To Date Item
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
                                tint = if (isSystemBusy) NeonGreen.copy(alpha = 0.4f) else NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Filter switches custom styled cards
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
                        // Deep Switch Row
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
                                    tint = NeonGreen,
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
                                        text = "Scans hidden cache folders and app assets",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Switch(
                                checked = ui.includeFilesystem,
                                onCheckedChange = vm::setIncludeFilesystem,
                                enabled = !isSystemBusy,
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha = 0.3f))
                            )
                        }

                        // Orphans Switch Row
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
                                        text = "Orphans Previews Only",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Show leftover image/video cache copies",
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

                // Action Bar: Scan button, and All-files warning if deep scan with missing permission
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { permLauncher.launch(neededReadPermissionsForRecovery()) },
                        enabled = !isSystemBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "START SCAN PROCESS",
                            color = CarbonDarkBg,
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
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ALLOW ALL-FILES",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Diagnostic messaging flow
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

                // Scanning telemetry dynamic row
                if (ui.scanning) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TransparentPurpleGreyBg),
                        border = BorderStroke(1.dp, TerminalCyan.copy(alpha = 0.3f)),
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
                                color = TerminalCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "SEARCHING FOR DELETED FILE TRACES...",
                                color = TerminalCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Recovering actual progress display
                if (ui.recovering) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TransparentPurpleGreyBg),
                        border = BorderStroke(1.dp, TerminalCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val percent = if (ui.totalRecoverCount > 0) ui.currentRecoveredIndex.toFloat() / ui.totalRecoverCount else 0f
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
                                        color = TerminalCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "RECOVERING CHOSEN REMNANTS...",
                                        color = TerminalCyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${ui.currentRecoveredIndex} / ${ui.totalRecoverCount}",
                                    color = TerminalCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            LinearProgressIndicator(
                                progress = { percent },
                                color = TerminalCyan,
                                trackColor = SlateBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }

                // Header statistics row once loaded
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
                                    .background(NeonGreen.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${ui.traces.size} FOUND",
                                    color = NeonGreen,
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
                                    text = "${ui.selectedCount} SELECTED",
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
                                text = "ALL",
                                color = if (isSystemBusy) NeonGreen.copy(alpha = 0.5f) else NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .clickable(enabled = !isSystemBusy) { vm.selectAll() }
                                    .padding(6.dp)
                            )
                            Text(
                                text = "NONE",
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

        // List/Grid Items
        items(
            items = ui.traces,
            key = { it.id },
            span = { if (isGrid) GridItemSpan(1) else GridItemSpan(maxLineSpan) }
        ) { trace ->
            if (isGrid) {
                RecoverCell(
                    trace = trace,
                    selected = trace.id in ui.selected,
                    isSystemBusy = isSystemBusy,
                    onClick = { vm.toggle(trace.id) }
                )
            } else {
                RecoverListItem(
                    trace = trace,
                    selected = trace.id in ui.selected,
                    isSystemBusy = isSystemBusy,
                    onClick = { vm.toggle(trace.id) }
                )
            }
        }

        // Recover action button at the bottom
        if (ui.traces.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Button(
                    onClick = { showConfirm = true },
                    enabled = ui.selectedCount > 0 && !ui.recovering && !isSystemBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (ui.recovering) "RECOVERING SENSITIVE DATA..." else "RECOVER SELECTED SYSTEM TRACES (${ui.selectedCount})",
                        color = CarbonDarkBg,
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
            titleContentColor = NeonGreen,
            textContentColor = TextPrimary,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "RECOVER ${ui.selectedCount} TRACES?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Text(
                    text = "Selected pieces will be assembled and restored inside Download/${TraceRecoverer.RECOVERY_FOLDER}. Galleries may prompt permission to safely untrash items in-place.",
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
                        vm.recoverSelected()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "EXECUTE RESTORATION",
                        color = CarbonDarkBg,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirm = false },
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(
                        text = AppTexts.ABORT_ACTION,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun RecoverListItem(trace: RecoverableTrace, selected: Boolean, isSystemBusy: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSystemBusy) { onClick() },
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, if (selected) NeonGreen.copy(alpha = 0.5f) else SlateBorder),
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
                    text = trace.recoverDateLabel(),
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = trace.source.uppercase(),
                    color = TerminalCyan,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
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
private fun RecoverCell(trace: RecoverableTrace, selected: Boolean, isSystemBusy: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(116.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !isSystemBusy) { onClick() },
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, if (selected) NeonGreen.copy(alpha = 0.5f) else SlateBorder),
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
                                text = if (trace.category == TraceCategory.DOCUMENT) "DOC" else "FILE",
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
                    text = trace.recoverDateLabel(),
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

private val recoverDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

private fun RecoverableTrace.recoverDateLabel(): String =
    deletedAtMillis?.let { del -> "Del ~${recoverDateFormat.format(Date(del))}" }
        ?: "Date ${recoverDateFormat.format(Date(dateMillis))}"

private val TransparentPurpleGreyBg = Color(0x1A22D3EE)

/** Read permissions appropriate to the OS version. */
private fun neededReadPermissionsForRecovery(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO
        )
    else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

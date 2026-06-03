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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataRecoveryScreen(vm: DataRecoveryViewModel) {
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
        columns = if (isGrid) GridCells.Adaptive(108.dp) else GridCells.Fixed(1),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text("Recover Deleted Data", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Finds previously deleted files that still physically exist on your storage " +
                        "(trash & recycle bins, orphaned thumbnail/preview caches, app media leftovers, " +
                        "interrupted downloads, temp/backup copies and LOST.DIR fragments) and restores " +
                        "them. Readable items are copied into Download/${TraceRecoverer.RECOVERY_FOLDER}; " +
                        "trashed gallery items are restored in place after a one-tap system confirmation.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "This recovers data that is still on disk. It cannot carve raw bytes from already-" +
                        "overwritten storage — that requires a rooted device or a PC forensic tool. " +
                        "Run the scan as soon as possible after a deletion for the best results.",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { pickDate(ui.fromMillis) { vm.setRange(it, ui.toMillis) } }) {
                        Text("From: ${df.format(Date(ui.fromMillis))}")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { pickDate(ui.toMillis) { vm.setRange(ui.fromMillis, it) } }) {
                        Text("To: ${df.format(Date(ui.toMillis))}")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = ui.includeFilesystem, onCheckedChange = vm::setIncludeFilesystem)
                    Spacer(Modifier.width(8.dp))
                    Text("Deep scan (hidden / app leftovers)", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = ui.onlyOrphans, onCheckedChange = vm::setOnlyOrphans)
                    Spacer(Modifier.width(8.dp))
                    Text("Only show deleted files' leftover previews", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { permLauncher.launch(neededReadPermissionsForRecovery()) }) { Text("Scan") }
                    if (ui.includeFilesystem && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                        !Environment.isExternalStorageManager()
                    ) {
                        OutlinedButton(onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                    Uri.parse("package:" + context.packageName)
                                )
                            )
                        }) { Text("Allow all-files") }
                    }
                }
                ui.message?.let {
                    Text(it, modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.bodyMedium)
                }
                if (ui.scanning) {
                    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Scanning…")
                    }
                }
                if (ui.traces.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${ui.traces.size} found • ${ui.selectedCount} selected",
                            style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { isGrid = !isGrid }) {
                            Icon(if (isGrid) Icons.Default.List else Icons.Default.GridView, "Toggle layout")
                        }
                        TextButton(onClick = vm::selectAll) { Text("Select all") }
                        TextButton(onClick = vm::clearSelection) { Text("Clear") }
                    }
                }
            }
        }
        items(ui.traces, key = { it.id }, span = { if (isGrid) GridItemSpan(1) else GridItemSpan(maxLineSpan) }) { trace ->
            if (isGrid) {
                RecoverCell(trace = trace, selected = trace.id in ui.selected, onClick = { vm.toggle(trace.id) })
            } else {
                RecoverListItem(trace = trace, selected = trace.id in ui.selected, onClick = { vm.toggle(trace.id) })
            }
        }
        if (ui.traces.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Button(
                    onClick = { showConfirm = true },
                    enabled = ui.selectedCount > 0 && !ui.recovering,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp)
                ) { Text(if (ui.recovering) "Recovering…" else "Recover selected (${ui.selectedCount})") }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Recover ${ui.selectedCount} item(s)?") },
            text = {
                Text(
                    "Readable files are copied into Download/${TraceRecoverer.RECOVERY_FOLDER}. " +
                        "For trashed gallery items you'll get a system prompt to restore them in place."
                )
            },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; vm.recoverSelected() }) { Text("Recover") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RecoverListItem(trace: RecoverableTrace, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))) {
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
                            Text(if (trace.category == TraceCategory.DOCUMENT) "DOC" else "FILE", style = MaterialTheme.typography.labelSmall)
                        }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(trace.displayName, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(trace.recoverDateLabel(), style = MaterialTheme.typography.labelSmall)
                Text(trace.source, style = MaterialTheme.typography.labelSmall)
            }
            Checkbox(checked = selected, onCheckedChange = { onClick() })
        }
    }
}

@Composable
private fun RecoverCell(trace: RecoverableTrace, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(10.dp)).clickable { onClick() }.padding(2.dp)
    ) {
        Box(Modifier.size(104.dp).clip(RoundedCornerShape(10.dp))) {
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
                            if (trace.category == TraceCategory.DOCUMENT) "DOC" else "FILE",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
            }
            if (trace.category == TraceCategory.VIDEO) {
                Text("▶", modifier = Modifier.align(Alignment.Center))
            }
            Checkbox(
                checked = selected,
                onCheckedChange = { onClick() },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
        Text(
            trace.displayName,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(trace.recoverDateLabel(), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private val recoverDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

private fun RecoverableTrace.recoverDateLabel(): String =
    deletedAtMillis?.let { del -> "Deleted ~${recoverDateFormat.format(Date(del))}" }
        ?: "File date ${recoverDateFormat.format(Date(dateMillis))}"

/** Read permissions appropriate to the OS version. */
private fun neededReadPermissionsForRecovery(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO
        )
    else arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

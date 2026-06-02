package com.example.wipe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DeepWipeScreen(vm: DeepWipeViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Wipe Free Space", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Overwrites the free space on your internal storage so remnants of " +
                "previously deleted files become unrecoverable by common recovery tools.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("What this can and can't do", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    "\u2022 Internal storage is encrypted + flash, so much deleted data is already gone.\n" +
                        "\u2022 This pass is a best-effort extra measure, not a guarantee.\n" +
                        "\u2022 For a complete guaranteed wipe, do a factory reset (erases the encryption key).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        when (val s = state) {
            is DeepWipeViewModel.WipeState.Idle ->
                Button(onClick = vm::startFreeSpaceWipe) { Text("Start free-space wipe") }
            is DeepWipeViewModel.WipeState.Running -> {
                LinearProgressIndicator(progress = { s.fraction }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("${(s.fraction * 100).toInt()}%  \u2022  ${s.mbWritten} MB overwritten")
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = vm::cancel) { Text("Cancel") }
            }
            is DeepWipeViewModel.WipeState.Done -> {
                Text("Done \u2014 overwrote ${s.mbWritten} MB of free space.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = vm::startFreeSpaceWipe) { Text("Run again") }
            }
            is DeepWipeViewModel.WipeState.Cancelled -> {
                Text("Cancelled.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = vm::startFreeSpaceWipe) { Text("Start again") }
            }
            is DeepWipeViewModel.WipeState.Error -> {
                Text("Error: ${s.message}")
                Spacer(Modifier.height(8.dp))
                Button(onClick = vm::startFreeSpaceWipe) { Text("Retry") }
            }
        }
    }
}

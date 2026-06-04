package com.example.recover

import android.app.Application
import android.content.IntentSender
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Drives the Recovery tab. Shares [TraceScanner] with the Recoverable tab (same detection of
 * still-existing remnants), but the action is RESTORE — copy readable items into a recovery
 * folder and/or untrash MediaStore items in place — rather than wipe.
 */
class DataRecoveryViewModel(app: Application) : AndroidViewModel(app) {

    private val scanner = TraceScanner(app)
    private val recoverer = TraceRecoverer(app)

    data class UiState(
        val scanning: Boolean = false,
        val recovering: Boolean = false,
        val currentRecoveredIndex: Int = 0,
        val totalRecoverCount: Int = 0,
        val traces: List<RecoverableTrace> = emptyList(),
        val selected: Set<String> = emptySet(),
        val fromMillis: Long = defaultFrom(),
        val toMillis: Long = System.currentTimeMillis(),
        val includeFilesystem: Boolean = true,
        val onlyOrphans: Boolean = false,
        val message: String? = null
    ) {
        val selectedCount get() = selected.size
    }

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()
    private var allTraces: List<RecoverableTrace> = emptyList()

    // one-shot: launch this IntentSender to get user consent for in-place untrash
    private val _consentRequest = MutableStateFlow<IntentSender?>(null)
    val consentRequest: StateFlow<IntentSender?> = _consentRequest.asStateFlow()

    private var scanJob: Job? = null

    fun setRange(from: Long, to: Long) {
        _ui.value = _ui.value.copy(fromMillis = from, toMillis = to)
    }
    fun setIncludeFilesystem(v: Boolean) { _ui.value = _ui.value.copy(includeFilesystem = v) }
    fun setOnlyOrphans(v: Boolean) {
        _ui.value = _ui.value.copy(onlyOrphans = v, traces = applyFilter(allTraces, v), selected = emptySet())
    }

    private fun applyFilter(list: List<RecoverableTrace>, onlyOrphans: Boolean) =
        if (onlyOrphans) list.filter { it.orphan } else list

    fun toggle(id: String) {
        val s = _ui.value.selected.toMutableSet()
        if (!s.add(id)) s.remove(id)
        _ui.value = _ui.value.copy(selected = s)
    }
    fun selectAll() { _ui.value = _ui.value.copy(selected = _ui.value.traces.map { it.id }.toSet()) }
    fun clearSelection() { _ui.value = _ui.value.copy(selected = emptySet()) }

    fun scan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _ui.value = _ui.value.copy(
                scanning = true, message = null, traces = emptyList(), selected = emptySet()
            )
            val s = _ui.value
            val result = runCatching {
                scanner.scan(s.fromMillis, s.toMillis, s.includeFilesystem)
            }.getOrElse { emptyList() }
            allTraces = result
            _ui.value = _ui.value.copy(
                scanning = false,
                traces = applyFilter(allTraces, s.onlyOrphans),
                message = if (result.isEmpty()) "No recoverable data found in this date range." else null
            )
        }
    }

    fun recoverSelected() {
        val s = _ui.value
        val chosen = s.traces.filter { it.id in s.selected }
        if (chosen.isEmpty()) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                recovering = true,
                currentRecoveredIndex = 0,
                totalRecoverCount = chosen.size
            )
            val res = recoverer.recover(chosen) { current, total ->
                _ui.value = _ui.value.copy(
                    currentRecoveredIndex = current,
                    totalRecoverCount = total
                )
            }
            if (res.needsUserConsent != null) _consentRequest.value = res.needsUserConsent
            val parts = buildList {
                if (res.recovered > 0) add("Successfully restored ${res.recovered} file(s) to ${res.outputDir}.")
                if (res.needsUserConsent != null) add("Please approve the system prompt to restore the remaining items back to your Gallery.")
                if (res.recovered == 0 && res.needsUserConsent == null) add("No files could be recovered from the selected items.")
            }
            _ui.value = _ui.value.copy(recovering = false, message = parts.joinToString(" "))
        }
    }

    /** Call after the user responds to the system untrash-consent dialog. */
    fun onConsentHandled(approved: Boolean) {
        _consentRequest.value = null
        if (approved) scan() // refresh: untrashed items leave the recoverable set
    }

    companion object {
        private fun defaultFrom(): Long {
            val c = Calendar.getInstance(); c.add(Calendar.DAY_OF_YEAR, -30); return c.timeInMillis
        }
    }
}

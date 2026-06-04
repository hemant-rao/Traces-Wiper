package com.example.wipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeepWipeViewModel(app: Application) : AndroidViewModel(app) {

    sealed interface WipeState {
        data object Idle : WipeState
        data class Running(val fraction: Float, val mbWritten: Long) : WipeState
        data class Done(val mbWritten: Long) : WipeState
        data class Error(val message: String) : WipeState
        data object Cancelled : WipeState
    }

    private val wiper = FreeSpaceWiper()
    private val _state = MutableStateFlow<WipeState>(WipeState.Idle)
    val state: StateFlow<WipeState> = _state.asStateFlow()
    private var job: Job? = null

    private val _showWarning = MutableStateFlow(true)
    val showWarning: StateFlow<Boolean> = _showWarning.asStateFlow()

    fun dismissWarning() {
        _showWarning.value = false
    }

    fun startFreeSpaceWipe() {
        if (job?.isActive == true) return
        val ctx = getApplication<Application>()
        job = viewModelScope.launch {
            _state.value = WipeState.Running(0f, 0)
            val result = wiper.wipe(ctx.cacheDir) { p ->
                _state.value = WipeState.Running(p.fraction, p.bytesWritten / (1024 * 1024))
            }
            result
                .onSuccess { 
                    _state.value = WipeState.Done(it / (1024 * 1024))
                    android.widget.Toast.makeText(ctx, "✅ Free Space Wipe Complete", android.widget.Toast.LENGTH_SHORT).show()
                }
                .onFailure { _state.value = WipeState.Error(it.message ?: "Unknown error") }
        }
        job?.invokeOnCompletion { cause ->
            if (cause is CancellationException) {
                _state.value = WipeState.Cancelled
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(getApplication(), "Wipe Cancelled", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun cancel() { job?.cancel() }
}

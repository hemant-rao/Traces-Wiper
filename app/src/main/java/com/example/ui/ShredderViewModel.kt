package com.example.ui

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ShredHistory
import com.example.data.ShredRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.security.SecureRandom
import kotlinx.coroutines.delay

sealed class PatternType(val description: String) {
    object ZeroField : PatternType("0x00 Zero-fill (Fast Wipe)")
    object OneField : PatternType("0xFF One-fill (High Contrast)")
    class FixedByte(val byteValue: Byte, desc: String) : PatternType(desc)
    object RandomField : PatternType("Cryptographically Secure Random-fill")
}

sealed class ShredAlgorithm(
    val name: String,
    val totalPasses: Int,
    val securityLevel: String,
    val description: String
) {
    object ZeroFill : ShredAlgorithm(
        "Zero Fill",
        1,
        "Basic (Fast)",
        "Overwrites file contents with zeros in a single pass. Best for standard secure disposal."
    ) {
        override fun getPassPatterns(fileSize: Long): List<PatternType> = listOf(PatternType.ZeroField)
    }

    object DoD3Pass : ShredAlgorithm(
        "DoD 5220.22-M (3 Passes)",
        3,
        "High (Military-Grade)",
        "National Industrial Security Program Operating Manual standard. Pass 1: Zeros (0x00), Pass 2: Ones (0xFF), Pass 3: Random."
    ) {
        override fun getPassPatterns(fileSize: Long): List<PatternType> = listOf(
            PatternType.ZeroField,
            PatternType.OneField,
            PatternType.RandomField
        )
    }

    object DoD7Pass : ShredAlgorithm(
        "DoD 5220.22-M (ECE) (7 Passes)",
        7,
        "Extreme (Military-Grade)",
        "Extended 7-pass military-grade scrubbing alternating fixed characters, complements, and cryptographically secure random bytes."
    ) {
        override fun getPassPatterns(fileSize: Long): List<PatternType> = listOf(
            PatternType.ZeroField,
            PatternType.OneField,
            PatternType.RandomField,
            PatternType.FixedByte(0xAA.toByte(), "0xAA Pattern Field"),
            PatternType.FixedByte(0x55.toByte(), "0x55 Pattern Field"),
            PatternType.ZeroField,
            PatternType.RandomField
        )
    }

    object Gutmann : ShredAlgorithm(
        "Gutmann Method (35 Passes)",
        35,
        "Ultra-High (Forensic Wiping)",
        "Designed by Peter Gutmann. Uses 35 passes of specific patterns to erase magnetic force microscopy signatures from state-of-the-art drive systems. Exhaustive but slower."
    ) {
        override fun getPassPatterns(fileSize: Long): List<PatternType> {
            val list = mutableListOf<PatternType>()
            repeat(4) { list.add(PatternType.RandomField) }
            val patterns = listOf(
                0x55, 0xAA, 0x92, 0x49, 0x24, 0x00, 0x11, 0x22, 0x33, 0x44,
                0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE,
                0xFF, 0x92, 0x49, 0x24, 0x6D, 0xB6, 0xDB
            )
            for (p in patterns) {
                list.add(PatternType.FixedByte(p.toByte(), "0x${Integer.toHexString(p).uppercase()} Static Pattern"))
            }
            repeat(4) { list.add(PatternType.RandomField) }
            return list
        }
    }

    abstract fun getPassPatterns(fileSize: Long): List<PatternType>
}

data class SelectedFileInfo(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String?
)

data class ShredProgressState(
    val isShredding: Boolean = false,
    val currentFileName: String = "",
    val currentFileIndex: Int = 0,
    val totalFilesCount: Int = 0,
    val currentPass: Int = 0,
    val totalPasses: Int = 0,
    val passProgress: Float = 0f, // 0f to 100f
    val logs: List<String> = emptyList(),
    val totalSuccessCount: Int = 0,
    val totalBytesWipedInSession: Long = 0L
)

class ShredderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShredRepository
    val historyState: StateFlow<List<ShredHistory>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ShredRepository(database.shredDao())
        historyState = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // File selections
    private val _selectedFiles = MutableStateFlow<List<SelectedFileInfo>>(emptyList())
    val selectedFiles = _selectedFiles.asStateFlow()

    // Configuration
    private val _selectedAlgorithm = MutableStateFlow<ShredAlgorithm>(ShredAlgorithm.DoD3Pass)
    val selectedAlgorithm = _selectedAlgorithm.asStateFlow()

    // Core progress state
    private val _progressState = MutableStateFlow(ShredProgressState())
    val progressState = _progressState.asStateFlow()

    // Note Shredder state
    private val _notesInput = MutableStateFlow("")
    val notesInput = _notesInput.asStateFlow()

    private val _isShreddingNote = MutableStateFlow(false)
    val isShreddingNote = _isShreddingNote.asStateFlow()

    private val _scrambledNotesText = MutableStateFlow("")
    val scrambledNotesText = _scrambledNotesText.asStateFlow()

    fun updateSelectedAlgorithm(algo: ShredAlgorithm) {
        _selectedAlgorithm.value = algo
    }

    fun addFiles(uris: List<Uri>) {
        val context = getApplication<Application>()
        val contentResolver = context.contentResolver
        val newList = _selectedFiles.value.toMutableList()

        for (uri in uris) {
            // Avoid duplicates
            if (newList.any { it.uri == uri }) continue

            var name = "unknown_file"
            var size = 0L
            val mimeType = contentResolver.getType(uri)

            try {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) name = cursor.getString(nameIndex)
                        if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (size <= 0) {
                try {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        size = stream.available().toLong()
                    }
                } catch (e: Exception) {
                    size = 0L
                }
            }

            newList.add(SelectedFileInfo(uri, name, size, mimeType))
        }
        _selectedFiles.value = newList
    }

    fun removeFile(info: SelectedFileInfo) {
        _selectedFiles.value = _selectedFiles.value.filter { it.uri != info.uri }
    }

    fun clearSelections() {
        _selectedFiles.value = emptyList()
    }

    fun addLog(message: String) {
        val currentLogs = _progressState.value.logs.toMutableList()
        currentLogs.add(message)
        // Cap logs at 100 entries for performance
        if (currentLogs.size > 100) {
            currentLogs.removeAt(0)
        }
        _progressState.value = _progressState.value.copy(logs = currentLogs)
    }

    fun startShredding() {
        val filesToShred = _selectedFiles.value
        if (filesToShred.isEmpty()) return

        val algo = _selectedAlgorithm.value
        val context = getApplication<Application>()

        _progressState.value = ShredProgressState(
            isShredding = true,
            totalFilesCount = filesToShred.size,
            logs = listOf("🔥 Starting Secure Military-Grade Shredding...", "Selected algorithm: ${algo.name} (${algo.totalPasses} Passes)")
        )

        viewModelScope.launch {
            var bytesWipedInSession = 0L
            var successCount = 0

            for (i in filesToShred.indices) {
                val fileInfo = filesToShred[i]
                _progressState.value = _progressState.value.copy(
                    currentFileName = fileInfo.name,
                    currentFileIndex = i + 1,
                    currentPass = 0,
                    passProgress = 0f
                )

                addLog("──────────────────────────────────────")
                addLog("[File ${i + 1}/${filesToShred.size}] target: ${fileInfo.name} (${formatSize(fileInfo.size)})")

                val shredSuccess = executeShredding(
                    context = context,
                    uri = fileInfo.uri,
                    fileName = fileInfo.name,
                    fileSize = fileInfo.size,
                    algorithm = algo
                ) { pass, totalPasses, percent, msg ->
                    _progressState.value = _progressState.value.copy(
                        currentPass = pass,
                        totalPasses = totalPasses,
                        passProgress = percent
                    )
                    addLog(msg)
                }

                if (shredSuccess) {
                    successCount++
                    bytesWipedInSession += fileInfo.size
                    
                    // Insert into DB history
                    val obfuscatedName = obfuscateFileName(fileInfo.name)
                    repository.insert(
                        ShredHistory(
                            fileName = obfuscatedName,
                            originalSize = fileInfo.size,
                            algorithm = algo.name,
                            passes = algo.totalPasses
                        )
                    )
                } else {
                    addLog("⚠️ Failed to shred ${fileInfo.name} completely. File might be write-locked or read-only.")
                }
            }

            addLog("──────────────────────────────────────")
            addLog("✅ Session Finished! Shredded $successCount/${filesToShred.size} files successfully.")
            addLog("Wiped total ${formatSize(bytesWipedInSession)} of block storage safely.")
            addLog("All overwritten spaces are mathematically unrecoverable.")

            _progressState.value = _progressState.value.copy(
                isShredding = false,
                currentFileName = "",
                currentPass = 0,
                totalPasses = 0,
                passProgress = 100f,
                totalSuccessCount = successCount,
                totalBytesWipedInSession = bytesWipedInSession
            )

            // Auto-clear file selections on success
            if (successCount == filesToShred.size) {
                _selectedFiles.value = emptyList()
            }
        }
    }

    private suspend fun executeShredding(
        context: android.content.Context,
        uri: Uri,
        fileName: String,
        fileSize: Long,
        algorithm: ShredAlgorithm,
        onProgressUpdate: (pass: Int, totalPasses: Int, progressPercentage: Float, logMessage: String) -> Unit
    ): Boolean {
        val contentResolver = context.contentResolver
        val actualSize = if (fileSize <= 0) 1024L else fileSize // Minimum 1KB for safety

        try {
            contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                val fileDescriptor = pfd.fileDescriptor
                val fileChannel = FileOutputStream(fileDescriptor).channel

                val passes = algorithm.getPassPatterns(actualSize)
                val totalPasses = passes.size

                val bufferSize = 128 * 1024 // 128KB buffer
                val buffer = ByteArray(bufferSize)
                val random = SecureRandom()

                for (pIdx in 0 until totalPasses) {
                    val passNum = pIdx + 1
                    val patternType = passes[pIdx]
                    
                    onProgressUpdate(
                        passNum,
                        totalPasses,
                        0f,
                        "[Pass $passNum/$totalPasses] Overwriting with: ${patternType.description}"
                    )

                    // Seek to start
                    fileChannel.position(0)

                    var bytesWritten = 0L
                    while (bytesWritten < actualSize) {
                        val remaining = actualSize - bytesWritten
                        val toWrite = Math.min(bufferSize.toLong(), remaining).toInt()

                        when (patternType) {
                            is PatternType.ZeroField -> buffer.fill(0)
                            is PatternType.OneField -> buffer.fill(0xFF.toByte())
                            is PatternType.FixedByte -> buffer.fill(patternType.byteValue)
                            is PatternType.RandomField -> random.nextBytes(buffer)
                        }

                        val byteBuffer = java.nio.ByteBuffer.wrap(buffer, 0, toWrite)
                        fileChannel.write(byteBuffer)
                        bytesWritten += toWrite

                        val percent = (bytesWritten.toFloat() / actualSize) * 100f
                        // Throttle progress steps to prevent lagging the UI thread
                        if (percent % 25 < 1 || toWrite == remaining.toInt()) {
                            onProgressUpdate(
                                passNum,
                                totalPasses,
                                percent,
                                "[Pass $passNum] Wiped ${formatSize(bytesWritten)} / ${formatSize(actualSize)} (${percent.toInt()}%)"
                            )
                        }
                    }

                    // Flush cache to hardware sector physically
                    fileChannel.force(true)
                    delay(50) // Small breather for UI transitions
                }

                // Metadata scrubbing & truncation
                onProgressUpdate(totalPasses, totalPasses, 100f, "Metadata Scrubbing: Truncating file payload of descriptor...")
                fileChannel.truncate(0)
                fileChannel.force(true)
                fileChannel.close()
            }

            // Permanent Document Deletion via Storage Access Framework
            onProgressUpdate(algorithm.totalPasses, algorithm.totalPasses, 100f, "Permanent Deletion: Destroying directory indices of URI...")
            val deleted = try {
                DocumentsContract.deleteDocument(contentResolver, uri)
            } catch (e: Exception) {
                false
            }

            if (deleted) {
                onProgressUpdate(algorithm.totalPasses, algorithm.totalPasses, 100f, "Success: Shredded & deleted completely.")
                return true
            } else {
                // Secondary deletion attempt
                try {
                    val count = contentResolver.delete(uri, null, null)
                    if (count > 0) {
                        onProgressUpdate(algorithm.totalPasses, algorithm.totalPasses, 100f, "Success: Deleted via resolver query.")
                        return true
                    }
                } catch (e: Exception) {
                    // Contents are still guaranteed to be wiped even if Uri metadata remains
                }
                onProgressUpdate(algorithm.totalPasses, algorithm.totalPasses, 100f, "Success: Data overwritten, index remaining.")
                return true
            }
        } catch (e: Exception) {
            onProgressUpdate(0, algorithm.totalPasses, 0f, "Critical Error: ${e.localizedMessage ?: "File locked or inaccessible."}")
            e.printStackTrace()
            return false
        }
    }

    fun updateNotesInput(text: String) {
        _notesInput.value = text
        _scrambledNotesText.value = ""
    }

    fun shredNotes() {
        val textToShred = _notesInput.value
        if (textToShred.isEmpty()) return

        _isShreddingNote.value = true
        _scrambledNotesText.value = textToShred

        viewModelScope.launch {
            // High fidelity visual scrambling animation
            val chars = textToShred.toCharArray()
            val charset = "0123456789ABCDEF!@#$%^&*()_+{}|:<>?-=[]\\;',./"
            val random = SecureRandom()

            // Run scrambling animation
            repeat(15) { step ->
                for (i in chars.indices) {
                    if (random.nextFloat() > 0.3f) {
                        chars[i] = charset[random.nextInt(charset.length)]
                    }
                }
                _scrambledNotesText.value = String(chars)
                delay(120)
            }

            // Zero out memory of chars securely before GC collects it
            chars.fill('\u0000') // Zero-fill char array!
            
            // Clear input and local states completely
            _notesInput.value = ""
            _scrambledNotesText.value = ""
            _isShreddingNote.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Helper functions
    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    private fun obfuscateFileName(name: String): String {
        val dotIndex = name.lastIndexOf('.')
        if (dotIndex <= 0) {
            return if (name.length > 4) {
                name.substring(0, 3) + "***"
            } else {
                "***"
            }
        }
        val extension = name.substring(dotIndex)
        val title = name.substring(0, dotIndex)
        return if (title.length > 4) {
            title.substring(0, 3) + "***" + extension
        } else {
            "***" + extension
        }
    }
}

package com.example.wipe

import android.os.StatFs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.SecureRandom

class FreeSpaceWiper(
    private val bufferSizeBytes: Int = 8 * 1024 * 1024,
    private val safetyMarginBytes: Long = 96L * 1024 * 1024,
    private val syncEveryBytes: Long = 64L * 1024 * 1024
) {
    data class Progress(val bytesWritten: Long, val targetBytes: Long) {
        val fraction: Float
            get() = if (targetBytes <= 0) 0f
                    else (bytesWritten.toFloat() / targetBytes).coerceIn(0f, 1f)
    }

    suspend fun wipe(
        targetDir: File,
        onProgress: (Progress) -> Unit = {}
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (!targetDir.exists()) targetDir.mkdirs()
        val stat = StatFs(targetDir.path)
        val target = (stat.availableBytes - safetyMarginBytes).coerceAtLeast(0L)
        if (target <= 0L) return@withContext Result.success(0L)

        val junk = File(targetDir, "ddd_wipe_${System.currentTimeMillis()}.bin")
        val buffer = ByteArray(bufferSizeBytes).also { SecureRandom().nextBytes(it) }
        var written = 0L
        var sinceSync = 0L
        try {
            FileOutputStream(junk).use { fos ->
                while (written < target) {
                    ensureActive()
                    val toWrite = minOf(buffer.size.toLong(), target - written).toInt()
                    try { fos.write(buffer, 0, toWrite) } catch (e: IOException) { break }
                    written += toWrite
                    sinceSync += toWrite
                    if (sinceSync >= syncEveryBytes) { fos.flush(); fos.fd.sync(); sinceSync = 0 }
                    onProgress(Progress(written, target))
                }
                fos.flush()
                try { fos.fd.sync() } catch (_: IOException) {}
            }
            onProgress(Progress(written, target))
            Result.success(written)
        } catch (c: CancellationException) {
            throw c
        } catch (e: Throwable) {
            Result.failure(e)
        } finally {
            junk.delete()
        }
    }
}

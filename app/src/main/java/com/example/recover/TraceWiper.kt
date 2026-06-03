package com.example.recover

import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.security.SecureRandom

class TraceWiper(private val context: Context) {

    data class WipeResult(
        val deleted: Int,
        val needsUserConsent: IntentSender? // launch this, then refresh on return
    )

    /**
     * Securely wipe selected traces.
     * - File-backed (All Files Access): overwrite + delete directly.
     * - MediaStore we own: overwrite + delete directly.
     * - MediaStore owned by other apps (Android 11+): batched into one system delete
     *   request; launch the returned IntentSender for the user's one-tap consent.
     */
    suspend fun wipe(traces: List<RecoverableTrace>): WipeResult = withContext(Dispatchers.IO) {
        var deleted = 0
        val needConsent = ArrayList<Uri>()
        val resolver = context.contentResolver

        for (t in traces) {
            val uri = t.contentUri
            val path = t.filePath
            if (uri == null && path != null) {
                if (overwriteAndDeleteFile(File(path))) deleted++
                continue
            }
            if (uri != null) {
                // best-effort overwrite if writable
                runCatching {
                    resolver.openOutputStream(uri, "wt")?.use { out ->
                        val buf = ByteArray(512 * 1024).also { SecureRandom().nextBytes(it) }
                        var left = t.sizeBytes
                        while (left > 0) {
                            val n = minOf(buf.size.toLong(), left).toInt()
                            out.write(buf, 0, n); left -= n
                        }
                        out.flush()
                    }
                }
                try {
                    if (resolver.delete(uri, null, null) > 0) deleted++ else needConsent.add(uri)
                } catch (e: SecurityException) {
                    needConsent.add(uri) // needs system consent dialog
                }
            }
        }

        val sender: IntentSender? =
            if (needConsent.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                MediaStore.createDeleteRequest(resolver, needConsent).intentSender
            else null

        WipeResult(deleted, sender)
    }

    private fun overwriteAndDeleteFile(file: File): Boolean {
        if (!file.exists() || !file.isFile) return false
        runCatching {
            val len = file.length()
            val buf = ByteArray(512 * 1024).also { SecureRandom().nextBytes(it) }
            RandomAccessFile(file, "rws").use { raf ->
                var pos = 0L
                while (pos < len) {
                    val n = minOf(buf.size.toLong(), len - pos).toInt()
                    raf.seek(pos); raf.write(buf, 0, n); pos += n
                }
                raf.fd.sync()
            }
        }
        return file.delete()
    }
}

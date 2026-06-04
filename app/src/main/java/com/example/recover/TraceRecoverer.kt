package com.example.recover

import android.content.Context
import android.content.IntentSender
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TraceRecoverer(private val context: Context) {

    data class RecoverResult(
        val recovered: Int,                 // files copied into the recovery folder
        val outputDir: String?,             // where copies were written (null if none)
        val needsUserConsent: IntentSender? // launch this to untrash in-place, then refresh
    )

    /**
     * Recover selected traces — the inverse of [TraceWiper].
     *
     * Two honest, non-root paths (a normal app cannot carve raw deleted blocks like FoneLab's
     * rooted "deep scan"; it can only resurrect remnants that still physically exist):
     *  - Readable bytes (file remnants we can open, or our own/owned MediaStore items) are
     *    COPIED OUT into Download/Recovered_by_CyberWipe so the user gets a fresh, visible copy.
     *  - Trashed MediaStore items we can't read directly (owned by other apps) are batched into
     *    one system untrash request; launch the returned IntentSender for the user's one-tap
     *    consent, which restores them in place to their original gallery location.
     */
    suspend fun recover(
        traces: List<RecoverableTrace>,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): RecoverResult = withContext(Dispatchers.IO) {
        var recovered = 0
        val needUntrash = ArrayList<Uri>()
        val resolver = context.contentResolver

        val outDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            RECOVERY_FOLDER
        )
        var dirReady = false
        val scanPaths = ArrayList<String>()

        for (idx in traces.indices) {
            val t = traces[idx]
            onProgress(idx + 1, traces.size)
            // 1) Prefer a direct on-disk copy when we can read the bytes ourselves.
            val path = t.filePath
            val srcFile = path?.let { File(it) }
            if (srcFile != null && srcFile.canRead()) {
                if (!dirReady) dirReady = outDir.exists() || outDir.mkdirs()
                if (dirReady && copyOut(srcFile.inputStream(), t.displayName, outDir)?.also {
                        scanPaths.add(it.absolutePath)
                    } != null) recovered++
                continue
            }

            // 2) Otherwise try the content stream (works for items we own / can read).
            val uri = t.contentUri
            if (uri != null) {
                val stream = runCatching { resolver.openInputStream(uri) }.getOrNull()
                if (stream != null) {
                    if (!dirReady) dirReady = outDir.exists() || outDir.mkdirs()
                    if (dirReady && copyOut(stream, t.displayName, outDir)?.also {
                            scanPaths.add(it.absolutePath)
                        } != null) recovered++
                } else {
                    // Can't read it (e.g. another app's trashed media) — restore in place instead.
                    needUntrash.add(uri)
                }
            }
        }

        if (scanPaths.isNotEmpty()) {
            runCatching {
                MediaScannerConnection.scanFile(context, scanPaths.toTypedArray(), null, null)
            }
        }

        val sender: IntentSender? =
            if (needUntrash.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                MediaStore.createTrashRequest(resolver, needUntrash, false).intentSender
            else null

        RecoverResult(recovered, if (recovered > 0) outDir.absolutePath else null, sender)
    }

    /** Copy [input] into [dir], picking a non-colliding name based on [displayName]. */
    private fun copyOut(input: java.io.InputStream, displayName: String, dir: File): File? = runCatching {
        val dest = uniqueDest(dir, sanitize(displayName))
        input.use { ins -> dest.outputStream().use { out -> ins.copyTo(out, 256 * 1024) } }
        dest
    }.getOrNull()

    /** Strip path separators / illegal chars so a remnant name can't escape the recovery dir. */
    private fun sanitize(name: String): String {
        val cleaned = name.substringAfterLast('/').substringAfterLast('\\')
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
        return cleaned.ifEmpty { "recovered_file" }
    }

    /** Avoid clobbering an existing recovery: foo.jpg -> foo (1).jpg, foo (2).jpg … */
    private fun uniqueDest(dir: File, name: String): File {
        val first = File(dir, name)
        if (!first.exists()) return first
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var i = 1
        while (true) {
            val candidate = File(dir, "$base ($i)$ext")
            if (!candidate.exists()) return candidate
            i++
        }
    }

    companion object {
        const val RECOVERY_FOLDER = "Recovered_by_CyberWipe"
    }
}

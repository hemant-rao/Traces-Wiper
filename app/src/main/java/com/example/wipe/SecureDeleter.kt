package com.example.wipe

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.security.SecureRandom

class SecureDeleter(private val bufferSizeBytes: Int = 1 * 1024 * 1024) {

    suspend fun secureDelete(file: File): Boolean = withContext(Dispatchers.IO) {
        if (!file.exists() || !file.isFile) return@withContext false
        try {
            val len = file.length()
            val buf = ByteArray(bufferSizeBytes).also { SecureRandom().nextBytes(it) }
            RandomAccessFile(file, "rws").use { raf ->
                var pos = 0L
                while (pos < len) {
                    val n = minOf(buf.size.toLong(), len - pos).toInt()
                    raf.seek(pos); raf.write(buf, 0, n); pos += n
                }
                raf.fd.sync()
            }
        } catch (_: IOException) {}
        file.delete()
    }

    suspend fun secureDelete(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val doc = DocumentFile.fromSingleUri(context, uri) ?: return@withContext false
        val len = doc.length()
        try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                val buf = ByteArray(bufferSizeBytes).also { SecureRandom().nextBytes(it) }
                var written = 0L
                while (written < len) {
                    val n = minOf(buf.size.toLong(), len - written).toInt()
                    out.write(buf, 0, n); written += n
                }
                out.flush()
            }
        } catch (_: Exception) {}
        try { doc.delete() } catch (_: Exception) { false }
    }
}

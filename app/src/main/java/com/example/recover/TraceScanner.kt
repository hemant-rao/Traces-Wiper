package com.example.recover

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File

class TraceScanner(private val context: Context) {

    /** Curated dirs where deleted-but-leftover/cached media commonly survives. */
    private fun candidateDirs(): List<Pair<File, String>> {
        val root = android.os.Environment.getExternalStorageDirectory()
        fun d(p: String, label: String) = File(root, p) to label
        return listOf(
            d("DCIM/.thumbnails", "Orphaned Thumbnail"),
            d("Pictures/.thumbnails", "Orphaned Thumbnail"),
            d(".thumbnails", "Orphaned Thumbnail"),
            d("Android/media/com.whatsapp/WhatsApp/Media/.Statuses", "WhatsApp Leftover"),
            d("WhatsApp/Media/.Statuses", "WhatsApp Leftover"),
            d("WhatsApp/Media/WhatsApp Images/Sent", "WhatsApp Sent Remnant"),
            d("WhatsApp/Media/WhatsApp Video/Sent", "WhatsApp Sent Remnant"),
            d("Telegram", "Telegram Cache Remnant"),
            d("Download", "Download Remnant")
        )
    }

    /**
     * Scan for recoverable traces whose timestamp is within [fromMillis, toMillis].
     * @param includeFilesystem deep scan of curated dirs (needs All Files Access).
     */
    suspend fun scan(
        fromMillis: Long,
        toMillis: Long,
        includeFilesystem: Boolean,
        onProgress: (Int) -> Unit = {}
    ): List<RecoverableTrace> = withContext(Dispatchers.IO) {
        val out = LinkedHashMap<String, RecoverableTrace>() // dedup by key
        scanMediaStore(fromMillis, toMillis, out)
        scanOrphanedThumbnails(fromMillis, toMillis, out)
        onProgress(out.size)
        if (includeFilesystem && hasAllFilesAccess()) {
            for ((dir, label) in candidateDirs()) {
                ensureActive()
                walk(dir, label, fromMillis, toMillis, out, 0)
                onProgress(out.size)
            }
        }
        out.values.sortedByDescending { it.dateMillis }
    }

    private fun scanOrphanedThumbnails(from: Long, to: Long, out: MutableMap<String, RecoverableTrace>) {
        collectOrphans(
            android.provider.MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
            android.provider.MediaStore.Images.Thumbnails.IMAGE_ID,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            TraceCategory.IMAGE, from, to, out
        )
        collectOrphans(
            android.provider.MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
            android.provider.MediaStore.Video.Thumbnails.VIDEO_ID,
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            TraceCategory.VIDEO, from, to, out
        )
    }

    @Suppress("DEPRECATION")
    private fun collectOrphans(
        thumbUri: android.net.Uri,
        originalIdColumn: String,
        mediaUri: android.net.Uri,
        category: TraceCategory,
        from: Long,
        to: Long,
        out: MutableMap<String, RecoverableTrace>
    ) {
        val resolver = context.contentResolver
        // collect ids of originals that STILL exist
        val existing = HashSet<Long>()
        runCatching {
            resolver.query(mediaUri, arrayOf("_id"), null, null, null)?.use { c ->
                val idc = c.getColumnIndexOrThrow("_id")
                while (c.moveToNext()) existing.add(c.getLong(idc))
            }
        }
        // walk thumbnails; keep only those whose original is gone (orphaned)
        runCatching {
            val proj = arrayOf("_id", originalIdColumn, "_data")
            resolver.query(thumbUri, proj, null, null, null)?.use { c ->
                val idc = c.getColumnIndexOrThrow("_id")
                val origc = c.getColumnIndexOrThrow(originalIdColumn)
                val datac = c.getColumnIndexOrThrow("_data")
                while (c.moveToNext()) {
                    val originalId = c.getLong(origc)
                    if (existing.contains(originalId)) continue // original still here -> not orphan
                    val path = if (!c.isNull(datac)) c.getString(datac) else null
                    val ts = path?.let { java.io.File(it).lastModified() } ?: 0L
                    if (ts != 0L && (ts < from || ts > to)) continue
                    val key = path ?: "thumb:$thumbUri:${c.getLong(idc)}"
                    if (out.containsKey(key)) continue
                    out[key] = RecoverableTrace(
                        id = key,
                        displayName = path?.substringAfterLast('/') ?: "preview_$originalId",
                        category = category,
                        mimeType = "image/jpeg",
                        sizeBytes = path?.let { java.io.File(it).length() } ?: 0L,
                        dateMillis = ts,
                        contentUri = android.content.ContentUris.withAppendedId(thumbUri, c.getLong(idc)),
                        filePath = path,
                        source = "Deleted original — preview remains",
                        orphan = true
                    )
                }
            }
        }
    }

    private fun scanMediaStore(from: Long, to: Long, out: MutableMap<String, RecoverableTrace>) {
        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val signal = CancellationSignal()
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val args = Bundle().apply {
                // ONLY include trashed ("Recently deleted") items
                putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
                putString(
                    ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                    "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
                )
            }
            resolver.query(collection, projection, args, signal)
        } else {
            // For older versions, hard to specifically filter trashed in MediaStore without extra logic. 
            // We'll skip for older versions or keep it permissive. Sticking to version >= R constraint for Trash only.
            null
        } ?: return

        cursor.use { c ->
            val idC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val sizeC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val dateC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val dataC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val typeC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            while (c.moveToNext()) {
                val date = c.getLong(dateC) * 1000L
                if (date < from || date > to) continue
                val id = c.getLong(idC)
                val mime = if (!c.isNull(mimeC)) c.getString(mimeC) else null
                val uri = ContentUris.withAppendedId(collection, id)
                val path = if (!c.isNull(dataC)) c.getString(dataC) else null
                val key = path ?: uri.toString()
                out[key] = RecoverableTrace(
                    id = uri.toString(),
                    displayName = if (!c.isNull(nameC)) c.getString(nameC) else "(unknown)",
                    category = categoryFor(mime, c.getInt(typeC)),
                    mimeType = mime,
                    sizeBytes = c.getLong(sizeC),
                    dateMillis = date,
                    contentUri = uri,
                    filePath = path,
                    source = "Media library / Trash"
                )
            }
        }
    }

    private fun walk(
        dir: File, label: String, from: Long, to: Long,
        out: MutableMap<String, RecoverableTrace>, depth: Int
    ) {
        if (!dir.exists() || !dir.isDirectory || depth > 6) return
        val children = dir.listFiles() ?: return
        for (f in children) {
            if (f.isDirectory) { walk(f, label, from, to, out, depth + 1); continue }
            val ts = f.lastModified()
            if (ts < from || ts > to) continue
            val key = f.absolutePath
            if (out.containsKey(key)) continue
            val mime = guessMime(f.name)
            val isOrphan = key.contains("/.thumbnails/") || f.name.startsWith(".thumbdata")
            out[key] = RecoverableTrace(
                id = key,
                displayName = f.name,
                category = categoryFor(mime, null),
                mimeType = mime,
                sizeBytes = f.length(),
                dateMillis = ts,
                contentUri = null,
                filePath = key,
                source = if (isOrphan) "Thumbnail cache remnant" else label,
                orphan = isOrphan
            )
        }
    }

    private fun categoryFor(mime: String?, mediaType: Int?): TraceCategory {
        if (mediaType != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when (mediaType) {
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> return TraceCategory.IMAGE
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> return TraceCategory.VIDEO
                MediaStore.Files.FileColumns.MEDIA_TYPE_DOCUMENT -> return TraceCategory.DOCUMENT
            }
        }
        return when {
            mime == null -> TraceCategory.OTHER
            mime.startsWith("image/") -> TraceCategory.IMAGE
            mime.startsWith("video/") -> TraceCategory.VIDEO
            mime.startsWith("application/pdf") || mime.startsWith("text/") ||
                mime.contains("word") || mime.contains("document") ||
                mime.contains("sheet") || mime.contains("presentation") -> TraceCategory.DOCUMENT
            else -> TraceCategory.OTHER
        }
    }

    private fun guessMime(name: String): String? =
        android.webkit.MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(name.substringAfterLast('.', "").lowercase())

    private fun hasAllFilesAccess(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
            android.os.Environment.isExternalStorageManager()
}

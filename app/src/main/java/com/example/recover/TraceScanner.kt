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

    private data class ScanDir(val dir: File, val label: String, val collectAll: Boolean)

    /**
     * Curated dirs where deleted-but-leftover/cached media commonly survives.
     * - collectAll=false (media roots): only HIDDEN remnants (.trashed-*, .pending-*,
     *   .thumbdata*, .thumbnails) are collected, so live photos aren't mislabeled "deleted".
     * - collectAll=true (pure cache/leftover dirs): everything inside is already a remnant.
     */
    private fun candidateDirs(): List<ScanDir> {
        val root = android.os.Environment.getExternalStorageDirectory()
        fun remnant(p: String, label: String) = ScanDir(File(root, p), label, false)
        fun all(p: String, label: String) = ScanDir(File(root, p), label, true)
        return listOf(
            // Standard media roots — surface on-disk Trash (.trashed-*), pending, recycle-bin,
            // interrupted-download & temp/backup remnants only (live files are never collected).
            remnant("DCIM", "Camera folder"),
            remnant("Pictures", "Pictures folder"),
            remnant("Movies", "Movies folder"),
            remnant("Music", "Music folder"),
            remnant("Download", "Downloads folder"),
            remnant("Documents", "Documents folder"),
            remnant("Recordings", "Recordings folder"),
            remnant("Audio", "Audio folder"),
            // Filesystem-level recycle bins & fsck recovery dumps — every file inside is a
            // recoverable remnant of deleted/lost content.
            all("LOST.DIR", "Recovered file fragment (LOST.DIR)"),
            all(".Trash", "Recycle-bin leftover (recoverable)"),
            all(".trash", "Recycle-bin leftover (recoverable)"),
            all(".trashed", "Gallery trash (recoverable)"),
            all(".RecycleBin", "Recycle-bin leftover (recoverable)"),
            all("MIUI/Gallery/cloud/.trashBin", "Gallery recycle bin"),
            // Thumbnail / preview caches — may hold previews of already-deleted photos.
            all("DCIM/.thumbnails", "Thumbnail cache"),
            all("Pictures/.thumbnails", "Thumbnail cache"),
            all(".thumbnails", "Thumbnail cache"),
            // Messaging-app media leftovers — status/story caches, sent copies, voice notes.
            all("Android/media/com.whatsapp/WhatsApp/Media/.Statuses", "WhatsApp status leftover"),
            all("Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/Sent", "WhatsApp sent remnant"),
            all("Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video/Sent", "WhatsApp sent remnant"),
            all("Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes", "WhatsApp voice-note remnant"),
            all("Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses", "WhatsApp Business status leftover"),
            all("WhatsApp/Media/.Statuses", "WhatsApp status leftover"),
            all("WhatsApp/Media/WhatsApp Images/Sent", "WhatsApp sent remnant"),
            all("WhatsApp/Media/WhatsApp Video/Sent", "WhatsApp sent remnant"),
            all("WhatsApp/Media/WhatsApp Voice Notes", "WhatsApp voice-note remnant"),
            all("Android/media/org.telegram.messenger/Telegram", "Telegram cache remnant"),
            all("Android/media/org.telegram.messenger.web/Telegram", "Telegram cache remnant"),
            all("Telegram", "Telegram cache remnant"),
            all("Signal/Backups", "Signal backup remnant")
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
        // The MediaStore.*.Thumbnails tables are deprecated and empty on Android 10+ (Q);
        // orphaned previews there are now found via the .thumbnails / .thumbdata filesystem walk.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) scanOrphanedThumbnails(fromMillis, toMillis, out)
        onProgress(out.size)
        
        // Collect currently active (non-trashed) media files and IDs from MediaStore
        val activePaths = HashSet<String>()
        val activeIds = HashSet<Long>()
        runCatching {
            val resolver = context.contentResolver
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA
            )
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                "is_trashed = 0"
            } else {
                null
            }
            resolver.query(collection, projection, selection, null, null)?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    activeIds.add(id)
                    val data = if (!c.isNull(dataCol)) c.getString(dataCol) else null
                    if (data != null) {
                        activePaths.add(data.lowercase())
                    }
                }
            }
        }

        if (includeFilesystem && hasAllFilesAccess()) {
            for (sd in candidateDirs()) {
                ensureActive()
                walk(sd.dir, sd.label, sd.collectAll, fromMillis, toMillis, out, 0, activePaths, activeIds)
                onProgress(out.size)
            }
        }
        out.values.sortedByDescending { it.effectiveDate }
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

    private suspend fun scanMediaStore(from: Long, to: Long, out: MutableMap<String, RecoverableTrace>) {
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
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            // Exact auto-purge time for trashed items (seconds). Lets us estimate the deletion date.
            MediaStore.Files.FileColumns.DATE_EXPIRES
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

        try {
            cursor.use { c ->
                val idC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val mimeC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val sizeC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val dataC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val typeC = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val expC = c.getColumnIndex(MediaStore.Files.FileColumns.DATE_EXPIRES)
                while (c.moveToNext()) {
                    kotlin.coroutines.coroutineContext.ensureActive()
                    val date = c.getLong(dateC) * 1000L
                    val expiresMillis = if (expC >= 0 && !c.isNull(expC)) c.getLong(expC) * 1000L else null
                    val deletedAt = expiresMillis?.let { it - TRASH_RETENTION_MILLIS }
                    // These are trashed items, so filter on the estimated deletion date when we have it.
                    val effective = deletedAt ?: date
                    if (effective < from || effective > to) continue
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
                        source = "Recently deleted (Trash)",
                        expiresMillis = expiresMillis,
                        deletedAtMillis = deletedAt
                    )
                }
            }
        } catch (e: Exception) {
            signal.cancel()
            throw e
        }
    }

    private fun walk(
        dir: File, label: String, collectAll: Boolean, from: Long, to: Long,
        out: MutableMap<String, RecoverableTrace>, depth: Int,
        activePaths: HashSet<String>, activeIds: HashSet<Long>
    ) {
        if (!dir.exists() || !dir.isDirectory || depth > 6) return
        val children = dir.listFiles() ?: return
        for (f in children) {
            if (f.isDirectory) {
                walk(f, label, collectAll, from, to, out, depth + 1, activePaths, activeIds)
                continue
            }
            
            // Skip the file completely if it is registered as an active, non-deleted media file in the system
            val filePathLower = f.absolutePath.lowercase()
            if (activePaths.contains(filePathLower)) {
                continue
            }

            // Skip files in thumbnail caches if they correspond to images/videos that still actively exist
            val name = f.name
            val lowerPath = f.absolutePath.lowercase()
            if (lowerPath.contains("/.thumbnails") || name.startsWith(".thumbdata")) {
                val numbers = Regex("\\d+").findAll(name).mapNotNull { it.value.toLongOrNull() }.toList()
                if (numbers.any { activeIds.contains(it) }) {
                    continue // Original media file is still active, so this is not a deleted trace
                }
            }

            val (source, isRemnant) = classify(f, label)
            // In media roots we only collect hidden deleted-remnants, never live files.
            if (!collectAll && !isRemnant) continue
            val ts = f.lastModified()
            // ".trashed-<expiryEpoch>-name" encodes when the system will auto-purge it; back it
            // out by the retention window to estimate when the user actually deleted the file.
            val expiresMillis = trashExpiryMillis(f.name)
            val deletedAt = expiresMillis?.let { it - TRASH_RETENTION_MILLIS }
            val effective = deletedAt ?: ts
            if (effective < from || effective > to) continue
            val key = f.absolutePath
            if (out.containsKey(key)) continue
            val display = prettyName(f.name)
            val mime = guessMime(display)
            out[key] = RecoverableTrace(
                id = key,
                displayName = display,
                category = categoryFor(mime, null),
                mimeType = mime,
                sizeBytes = f.length(),
                dateMillis = ts,
                contentUri = null,
                filePath = key,
                source = source,
                orphan = isRemnant,
                expiresMillis = expiresMillis,
                deletedAtMillis = deletedAt
            )
        }
    }

    /** Classify a file as a deleted-data remnant by its (hidden) name/location. */
    private fun classify(f: File, dirLabel: String): Pair<String, Boolean> {
        val name = f.name
        val lowerName = name.lowercase()
        val lowerPath = f.absolutePath.lowercase()
        return when {
            name.startsWith(".trashed-") -> "Recently-deleted file (Trash remnant on disk)" to true
            name.startsWith(".pending-") -> "Incomplete / pending file remnant" to true
            name.startsWith(".thumbdata") -> "Thumbnail cache — may hold deleted-photo previews" to true
            lowerPath.contains("/.thumbnails/") -> "Orphaned thumbnail of a deleted file" to true
            lowerPath.contains("/lost.dir/") -> "Recovered file fragment (LOST.DIR)" to true
            isRecycleBinPath(lowerPath) -> "Recycle-bin leftover (recoverable)" to true
            isPartialDownload(lowerName) -> "Interrupted download leftover (recoverable)" to true
            isTempOrBackup(lowerName) -> "Temporary / backup leftover (recoverable)" to true
            else -> dirLabel to false
        }
    }

    /** A file sitting inside any common gallery/file-manager recycle-bin folder. */
    private fun isRecycleBinPath(lowerPath: String): Boolean =
        lowerPath.contains("/.trash/") || lowerPath.contains("/.trashed/") ||
            lowerPath.contains("/.trashbin/") || lowerPath.contains("/.recyclebin/") ||
            lowerPath.contains("/\$recycle.bin/") || lowerPath.contains("/recycle.bin/")

    /** Half-finished download (browsers/managers leave these when a transfer is interrupted). */
    private fun isPartialDownload(lowerName: String): Boolean =
        PARTIAL_SUFFIXES.any { lowerName.endsWith(it) }

    /** Editor/app temp or backup copies that linger after the "real" file is saved/removed. */
    private fun isTempOrBackup(lowerName: String): Boolean =
        lowerName.endsWith("~") || TEMP_SUFFIXES.any { lowerName.endsWith(it) }

    // Android renames trashed/pending media to ".trashed-<expiryEpoch>-<original>" /
    // ".pending-<epoch>-<original>"; group 1 = expiry epoch (seconds), group 2 = original name.
    private val hiddenRemnantName = Regex("^\\.(?:trashed|pending)-(\\d+)-(.+)$")
    private fun prettyName(name: String): String =
        hiddenRemnantName.find(name)?.groupValues?.get(2) ?: name

    /** Exact auto-purge time (ms) parsed from a ".trashed-<epoch>-…" filename, else null. */
    private fun trashExpiryMillis(name: String): Long? =
        if (name.startsWith(".trashed-"))
            hiddenRemnantName.find(name)?.groupValues?.getOrNull(1)?.toLongOrNull()?.let { it * 1000L }
        else null

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

    companion object {
        // Android's default "Recently deleted" retention before a trashed item is purged.
        // The expiry timestamp is exact; deletion time = expiry − this window (an estimate,
        // since OEMs/versions may use a different period). Shown to the user as "~deleted".
        const val TRASH_RETENTION_DAYS = 30L
        private const val TRASH_RETENTION_MILLIS = TRASH_RETENTION_DAYS * 24L * 60L * 60L * 1000L

        // Interrupted-download markers recovery tools resurrect as partial files.
        private val PARTIAL_SUFFIXES = listOf(
            ".crdownload", ".part", ".partial", ".download", ".opdownload", ".filepart", ".aria2"
        )
        // Editor/app temp & backup copies left behind next to (or after) the real file.
        private val TEMP_SUFFIXES = listOf(".tmp", ".temp", ".bak", ".old", ".orig")
    }
}

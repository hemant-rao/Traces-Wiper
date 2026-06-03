package com.example.recover

import android.net.Uri

enum class TraceCategory { IMAGE, VIDEO, DOCUMENT, OTHER }

/** A still-existing leftover/cached/trashed file that recovery tools could surface. */
data class RecoverableTrace(
    val id: String,            // stable id (uri string or absolute file path)
    val displayName: String,
    val category: TraceCategory,
    val mimeType: String?,
    val sizeBytes: Long,
    val dateMillis: Long,      // file's OWN timestamp (NOT a deletion date)
    val contentUri: Uri?,      // non-null if from MediaStore
    val filePath: String?,     // non-null if from a filesystem walk
    val source: String,         // human label: "Trash", "WhatsApp status", etc.
    val orphan: Boolean = false,
    val expiresMillis: Long? = null,     // EXACT auto-purge time from trash metadata, if known
    val deletedAtMillis: Long? = null    // ESTIMATED deletion time (expiry − trash retention)
) {
    /** What Coil should load for the thumbnail. */
    val thumbnailModel: Any? get() = contentUri ?: filePath

    /**
     * Date used for range-filtering & sorting. Uses the estimated deletion time when we have
     * trash metadata; otherwise falls back to the file's own timestamp (the best we can know).
     */
    val effectiveDate: Long get() = deletedAtMillis ?: dateMillis
}

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
    val orphan: Boolean = false
) {
    /** What Coil should load for the thumbnail. */
    val thumbnailModel: Any? get() = contentUri ?: filePath
}

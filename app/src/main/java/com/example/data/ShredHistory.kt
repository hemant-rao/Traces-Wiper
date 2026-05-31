package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shred_history")
data class ShredHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val originalSize: Long,
    val algorithm: String,
    val passes: Int,
    val timestamp: Long = System.currentTimeMillis()
)

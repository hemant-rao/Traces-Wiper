package com.example.data

import kotlinx.coroutines.flow.Flow

class ShredRepository(private val shredDao: ShredDao) {
    val allHistory: Flow<List<ShredHistory>> = shredDao.getAllHistory()

    suspend fun insert(record: ShredHistory) {
        shredDao.insertHistory(record)
    }

    suspend fun clearAll() {
        shredDao.clearAllHistory()
    }
}

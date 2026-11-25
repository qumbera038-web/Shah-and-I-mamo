package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val workspaceId: String,
    val category: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

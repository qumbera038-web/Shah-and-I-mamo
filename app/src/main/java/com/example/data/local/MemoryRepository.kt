package com.example.data.local

import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val memories: Flow<List<MemoryEntity>> = memoryDao.observeAll()

    suspend fun ensureStarterNotes() {
        if (memoryDao.count() == 0) {
            memoryDao.insertAll(
                listOf(
                    MemoryEntity(
                        title = "Shahgee V200 Foundation",
                        content = "Local-first ALiRaZa AI Studio restored with voice interface, Text-to-Speech feedback, and 14 structured workspaces. Built in memory of Ali Raza.",
                        workspaceId = "ai_brain",
                        category = "Foundation",
                        isPinned = true
                    ),
                    MemoryEntity(
                        title = "Safety & Confirmation Policy",
                        content = "Sensitive operations (code execution, trading orders, bulk file changes) stay gated behind explicit review, audit trails, and recoverable backups.",
                        workspaceId = "security_guardian",
                        category = "Governance",
                        isPinned = false
                    )
                )
            )
        }
    }

    suspend fun addMemory(
        title: String,
        content: String,
        workspaceId: String = "memory_knowledge",
        category: String = "Note",
        isPinned: Boolean = false
    ) {
        memoryDao.insert(
            MemoryEntity(
                title = title.trim(),
                content = content.trim(),
                workspaceId = workspaceId,
                category = category,
                isPinned = isPinned
            )
        )
    }

    suspend fun togglePinned(memory: MemoryEntity) {
        memoryDao.setPinned(memory.id, !memory.isPinned)
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteById(id)
    }
}

package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks student reading progress, page bookmarks, and completion
 * for NCERT Library chapters.
 *
 * Local-first architecture: Works completely offline.
 */
@Entity(tableName = "ncert_progress")
data class NcertProgressEntity(
    @PrimaryKey
    val chapterId: String, // e.g. "class11_physics_part1_ch01"
    val bookId: String, // e.g. "class11_physics_part1"
    val subjectId: String, // e.g. "class11_physics"
    val classNumber: Int, // 9, 10, 11, 12
    val lastPageRead: Int = 1,
    val totalPages: Int = 1,
    val progressPercentage: Int = 0,
    val isBookmarked: Boolean = false,
    val isCompleted: Boolean = false,
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val startedAt: Long = System.currentTimeMillis()
)
